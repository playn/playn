/**
 * Copyright 2011 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.html;

import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int16Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.webgl.client.WebGLBuffer;
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLProgram;
import com.google.gwt.webgl.client.WebGLRenderbuffer;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLShader;
import com.google.gwt.webgl.client.WebGLTexture;
import com.google.gwt.webgl.client.WebGLUniformLocation;

import static com.google.gwt.webgl.client.WebGLRenderingContext.ARRAY_BUFFER;
import static com.google.gwt.webgl.client.WebGLRenderingContext.BYTE;
import static com.google.gwt.webgl.client.WebGLRenderingContext.COMPILE_STATUS;
import static com.google.gwt.webgl.client.WebGLRenderingContext.ELEMENT_ARRAY_BUFFER;
import static com.google.gwt.webgl.client.WebGLRenderingContext.FLOAT;
import static com.google.gwt.webgl.client.WebGLRenderingContext.INT;
import static com.google.gwt.webgl.client.WebGLRenderingContext.LINK_STATUS;
import static com.google.gwt.webgl.client.WebGLRenderingContext.ONE;
import static com.google.gwt.webgl.client.WebGLRenderingContext.SHORT;
import static com.google.gwt.webgl.client.WebGLRenderingContext.STREAM_DRAW;
import static com.google.gwt.webgl.client.WebGLRenderingContext.UNPACK_PREMULTIPLY_ALPHA_WEBGL;
import static com.google.gwt.webgl.client.WebGLRenderingContext.UNSIGNED_BYTE;
import static com.google.gwt.webgl.client.WebGLRenderingContext.UNSIGNED_SHORT;

import playn.core.GL20;

/**
 * WebGL implementation of GL20. Maintains a set of VBOs to translate the NIO buffer based version
 * of glVertexAttribPointer to the VBO based version.
 *
 * @author Stefan Haustein
 */
public final class HtmlGL20 extends GL20 {

  static class VertexAttribArrayState {
    int type;
    int size;
    int stride;
    boolean normalize;
    Buffer nioBuffer;
    int nioBufferPosition;
    int nioBufferLimit;
    WebGLBuffer webGlBuffer;
  }

  static final class IntMap<T extends JavaScriptObject> extends JavaScriptObject {
    protected IntMap() {
      super();
    }

    public static native <T extends JavaScriptObject> IntMap<T> create() /*-{
      return [undefined];
    }-*/;

    public native T get(int key) /*-{
      return this[key];
    }-*/;

    public native void put(int key, T value) /*-{
      this[key] = value;
    }-*/;

    public native int add(T value) /*-{
      this.push(value);
      return this.length - 1;
    }-*/;

    public native T remove(int key) /*-{
      var value = this[key];
      delete this[key];
      return value;
      }-*/;
  }


  static final int VERTEX_ATTRIB_ARRAY_COUNT = 5; //  position, color, texture0, texture1, normals

  private final IntMap<WebGLProgram> programs = IntMap.create();
  private final IntMap<WebGLShader> shaders = IntMap.create();
  private final IntMap<WebGLBuffer> buffers = IntMap.create();
  private final IntMap<WebGLFramebuffer> frameBuffers = IntMap.create();
  private final IntMap<WebGLRenderbuffer> renderBuffers = IntMap.create();
  private final IntMap<WebGLTexture> textures = IntMap.create();
  private final IntMap<IntMap<WebGLUniformLocation>> uniforms = IntMap.create();
  private int currProgram = 0;

  private int enabledArrays = 0;
  private int previouslyEnabledArrays = 0;
  private int useNioBuffer = 0;

  private VertexAttribArrayState[] vertexAttribArrayState =
    new VertexAttribArrayState[VERTEX_ATTRIB_ARRAY_COUNT];

  private WebGLBuffer elementBuffer;
  private WebGLBuffer boundArrayBuffer;
  private WebGLBuffer requestedArrayBuffer;
  private WebGLBuffer boundElementArrayBuffer;
  private WebGLBuffer requestedElementArrayBuffer;

  private WebGLRenderingContext gl;

  public HtmlGL20 () {
    super(new Buffers() {
      public ByteBuffer createByteBuffer(int size) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
      }
    }, HtmlUrlParameters.checkGLErrors);
  }

  public Float32Array copy (FloatBuffer buffer) {
    return ((Float32Array)((HasArrayBufferView)buffer).getTypedArray()).subarray(buffer.position(), buffer.remaining());
  }

  public Int16Array copy (ShortBuffer buffer) {
    return ((Int16Array)((HasArrayBufferView)buffer).getTypedArray()).subarray(buffer.position(), buffer.remaining());
  }

  public Int32Array copy (IntBuffer buffer) {
    return ((Int32Array)((HasArrayBufferView)buffer).getTypedArray()).subarray(buffer.position(), buffer.remaining());
  }

  private static int getElementSize(Buffer buffer) {
    if ((buffer instanceof FloatBuffer) || (buffer instanceof IntBuffer)) return 4;
    else if (buffer instanceof ShortBuffer) return 2;
    else if (buffer instanceof ByteBuffer) return 1;
    else throw new RuntimeException("Unrecognized buffer type: " + buffer.getClass());
  }

  /**
   * Returns the typed array of the given native buffer. Set byteSize to -1 to use remaining().
   */
  private ArrayBufferView getTypedArray(Buffer buffer, int type, int byteSize) {
    if (!(buffer instanceof HasArrayBufferView)) {
      throw new RuntimeException("Native buffer required " + buffer);
    }
    HasArrayBufferView arrayHolder = (HasArrayBufferView) buffer;
    int bufferElementSize = arrayHolder.getElementSize();

    ArrayBufferView webGLArray = arrayHolder.getTypedArray();
    if (byteSize == -1) {
      byteSize = buffer.remaining() * bufferElementSize;
    }
    if (byteSize == buffer.capacity() * bufferElementSize && type == arrayHolder.getElementType()) {
      return webGLArray;
    }

    int byteOffset = webGLArray.byteOffset() + buffer.position() * bufferElementSize;

    switch (type) {
      case FLOAT:
        return TypedArrays.createFloat32Array(webGLArray.buffer(), byteOffset, byteSize / 4);
      case UNSIGNED_BYTE:
        return TypedArrays.createUint8Array(webGLArray.buffer(), byteOffset, byteSize);
      case UNSIGNED_SHORT:
        return TypedArrays.createUint16Array(webGLArray.buffer(), byteOffset, byteSize / 2);
      case INT:
        return TypedArrays.createInt32Array(webGLArray.buffer(), byteOffset, byteSize / 4);
      case SHORT:
        return TypedArrays.createInt16Array(webGLArray.buffer(), byteOffset, byteSize / 2);
      case BYTE:
        return TypedArrays.createInt8Array(webGLArray.buffer(), byteOffset, byteSize);
      default:
        throw new IllegalArgumentException("Type: " + type);
    }
  }

  private int getTypeSize(int type) {
    switch(type) {
      case GL_FLOAT:
      case GL_INT:
        return 4;
      case GL_SHORT:
      case GL_UNSIGNED_SHORT:
        return 2;
      case GL_BYTE:
      case GL_UNSIGNED_BYTE:
        return 1;
      default:
        throw new IllegalArgumentException();
    }
  }

  private WebGLUniformLocation getUniformLocation (int location) {
    return uniforms.get(currProgram).get(location);
  }

  void init (WebGLRenderingContext gl) {
    // TODO: do we always want to do this?
    gl.pixelStorei(UNPACK_PREMULTIPLY_ALPHA_WEBGL, ONE);
    this.gl = gl;

    elementBuffer = gl.createBuffer();

    for (int ii = 0; ii < VERTEX_ATTRIB_ARRAY_COUNT; ii++) {
      VertexAttribArrayState data = new VertexAttribArrayState();
      data.webGlBuffer = gl.createBuffer();
      vertexAttribArrayState[ii] = data;
    }
  }

  /**
   * The content of non-VBO buffers may be changed between the glVertexAttribPointer call
   * and the glDrawXxx call. Thus, we need to defer copying them to a VBO buffer until just
   * before the actual glDrawXxx call.
   */
  protected void prepareDraw() {
    VertexAttribArrayState previousNio = null;
    int previousElementSize = 0;

    if (useNioBuffer == 0 && enabledArrays == previouslyEnabledArrays) {
      return;
    }

    for(int i = 0; i < VERTEX_ATTRIB_ARRAY_COUNT; i++) {
      int mask = 1 << i;
      int enabled = enabledArrays & mask;
      if (enabled != (previouslyEnabledArrays & mask)) {
        if (enabled != 0) {
          gl.enableVertexAttribArray(i);
        } else {
          gl.disableVertexAttribArray(i);
        }
      }
      if (enabled != 0 && (useNioBuffer & mask) != 0) {
        VertexAttribArrayState data = vertexAttribArrayState[i];
        if (previousNio != null && previousNio.nioBuffer == data.nioBuffer &&
            previousNio.nioBufferLimit >= data.nioBufferLimit) {
          if (boundArrayBuffer != previousNio.webGlBuffer) {
            gl.bindBuffer(ARRAY_BUFFER, previousNio.webGlBuffer);
            boundArrayBuffer = data.webGlBuffer;
          }
          gl.vertexAttribPointer(i, data.size, data.type, data.normalize, data.stride,
                                 data.nioBufferPosition * previousElementSize);
        } else {
          if (boundArrayBuffer != data.webGlBuffer) {
            gl.bindBuffer(ARRAY_BUFFER, data.webGlBuffer);
            boundArrayBuffer = data.webGlBuffer;
          }
          int elementSize = getElementSize(data.nioBuffer);
          int savePosition = data.nioBuffer.position();
          if (data.nioBufferPosition * elementSize < data.stride) {
            data.nioBuffer.position(0);
            gl.bufferData(ARRAY_BUFFER, getTypedArray(data.nioBuffer, data.type, data.nioBufferLimit *
                                                      elementSize), STREAM_DRAW);
            gl.vertexAttribPointer(i, data.size, data.type, data.normalize, data.stride,
                                   data.nioBufferPosition * elementSize);
            previousNio = data;
            previousElementSize = elementSize;
          } else {
            data.nioBuffer.position(data.nioBufferPosition);
            gl.bufferData(ARRAY_BUFFER, getTypedArray(data.nioBuffer, data.type,
                                                      (data.nioBufferLimit - data.nioBufferPosition) *
                                                      elementSize), STREAM_DRAW);
            gl.vertexAttribPointer(i, data.size, data.type, data.normalize, data.stride, 0);
          }
          data.nioBuffer.position(savePosition);
        }
      }
    }
    previouslyEnabledArrays = enabledArrays;
  }

  //
  //
  // Public methods. Please keep ordered -----------------------------------------------------------------------------
  //
  //

  @Override
  public int getSwapInterval() {
    throw new RuntimeException("NYI getSwapInterval");
  }

  @Override
  public void glActiveTexture(int texture) {
    gl.activeTexture(texture);
  }

  @Override
  public void glAttachShader(int program, int shader) {
    WebGLProgram glProgram = programs.get(program);
    WebGLShader glShader = shaders.get(shader);
    gl.attachShader(glProgram, glShader);
  }

  @Override
  public void glBindAttribLocation(int program, int index, String name) {
    WebGLProgram glProgram = programs.get(program);
    gl.bindAttribLocation(glProgram, index, name);
  }

  @Override
  public void glBindBuffer(int target, int buffer) {
    // Yes, bindBuffer is so expensive that this makes sense..
    WebGLBuffer webGlBuf = buffers.get(buffer);
    if (target == GL_ARRAY_BUFFER) {
      requestedArrayBuffer = webGlBuf;
    } else if (target == GL_ELEMENT_ARRAY_BUFFER) {
      requestedElementArrayBuffer = webGlBuf;
    } else {
      gl.bindBuffer(target, webGlBuf);
    }
  }

  @Override
  public void glBindFramebuffer(int target, int framebuffer) {
    gl.bindFramebuffer(target, frameBuffers.get(framebuffer));
  }

  @Override
  public void glBindRenderbuffer(int target, int renderbuffer) {
    gl.bindRenderbuffer(target, renderBuffers.get(renderbuffer));
  }

  @Override
  public void glBindTexture (int target, int texture) {
    gl.bindTexture(target, textures.get(texture));
  }

  @Override
  public void glBlendColor(float red, float green, float blue, float alpha) {
    gl.blendColor(red, green, blue, alpha);
  }

  @Override
  public void glBlendEquation(int mode) {
    gl.blendEquation(mode);
  }

  @Override
  public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
    gl.blendEquationSeparate(modeRGB, modeAlpha);
  }

  @Override
  public void glBlendFunc (int sfactor, int dfactor) {
    gl.blendFunc(sfactor, dfactor);
  }

  @Override
  public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha,
                                            int dstAlpha) {
    gl.blendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
  }

  @Override
  public void glBufferData(int target, int byteSize, Buffer data, int usage) {
    if (target == GL_ARRAY_BUFFER) {
      if (requestedArrayBuffer != boundArrayBuffer) {
        gl.bindBuffer(target, requestedArrayBuffer);
        boundArrayBuffer = requestedArrayBuffer;
      }
    } else if (target == GL_ELEMENT_ARRAY_BUFFER) {
      if (requestedElementArrayBuffer != boundElementArrayBuffer) {
        gl.bindBuffer(target, requestedElementArrayBuffer);
        boundElementArrayBuffer = requestedElementArrayBuffer;
      }
    }
    gl.bufferData(target, getTypedArray(data, GL_BYTE, byteSize), usage);
  }

  @Override
  public void glBufferSubData(int target, int offset, int size, Buffer data) {
    if (target == GL_ARRAY_BUFFER && requestedArrayBuffer != boundArrayBuffer) {
      gl.bindBuffer(target, requestedArrayBuffer);
      boundArrayBuffer = requestedArrayBuffer;
    }
    throw new RuntimeException("NYI glBufferSubData");
  }

  @Override
  public int glCheckFramebufferStatus(int target) {
    return gl.checkFramebufferStatus(target);
  }

  @Override
  public final void glClear(int mask) {
    gl.clear(mask);
  }

  @Override
  public final void glClearColor(float red, float green, float blue, float alpha) {
    gl.clearColor(red, green, blue, alpha);
  }

  @Override
  public void glClearDepth(double depth) {
    gl.clearDepth((float) depth);
  }

  @Override
  public void glClearDepthf(float depth) {
    gl.clearDepth(depth);
  }

  @Override
  public void glClearStencil(int s) {
    gl.clearStencil(s);
  }

  @Override
  public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
    gl.colorMask(red, green, blue, alpha);
  }

  @Override
  public void glCompileShader(int shader) {
    WebGLShader glShader = shaders.get(shader);
    gl.compileShader(glShader);
  }

  @Override
  public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data) {
    throw new RuntimeException("NYI glCompressedTexImage2D");
  }

  @Override
  public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data) {
    throw new RuntimeException("NYI glCompressedTexSubImage2D");
  }

  @Override
  public void glCompressedTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, Buffer arg8) {
    throw new RuntimeException("NYI glCompressedTexImage3D");
  }

  @Override
  public void glCompressedTexImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7) {
    throw new RuntimeException("NYI glCompressedTexImage2D");
  }

  @Override
  public void glCompressedTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI glCompressedTexImage3D");
  }

  @Override
  public void glCompressedTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI glCompressedTexSubImage2D");
  }

  @Override
  public void glCompressedTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10) {
    throw new RuntimeException("NYI glCompressedTexSubImage3D");
  }

  @Override
  public void glCompressedTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, Buffer arg10) {
    throw new RuntimeException("NYI glCompressedTexSubImage3D");
  }

  @Override
  public void glCopyTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI glCopyTexSubImage3D");
  }

  @Override
  public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border) {
    gl.copyTexImage2D(target, level, internalformat, x, y, width, height, border);
  }

  @Override
  public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
    gl.copyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
  }

  @Override
  public int glCreateProgram() {
    WebGLProgram program = gl.createProgram();
    return programs.add(program);
  }

  @Override
  public int glCreateShader(int type) {
    WebGLShader shader = gl.createShader(type);
    return shaders.add(shader);
  }

  @Override
  public final void glCullFace(int mode) {
    gl.cullFace(mode);
  }

  @Override
  public void glDeleteBuffers(int n, IntBuffer buffers) {
    int pos = buffers.position();
    for (int i = 0; i < n; i++) {
      int id = buffers.get(pos + i);
      WebGLBuffer buffer = this.buffers.remove(id);
      gl.deleteBuffer(buffer);
    }
  }

  @Override
  public void glDeleteBuffers(int n, int[] buffers, int offset) {
    for (int i = 0; i < n; i++) {
      int id = buffers[i + offset];
      WebGLBuffer buffer = this.buffers.remove(id);
      gl.deleteBuffer(buffer);
    }
  }

  @Override
  public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
    int pos = framebuffers.position();
    for (int i = 0; i < n; i++) {
      int id = framebuffers.get(pos + i);
      WebGLFramebuffer fb = this.frameBuffers.remove(id);
      gl.deleteFramebuffer(fb);
    }
  }

  @Override
  public void glDeleteFramebuffers(int n, int[] framebuffers, int offset) {
    for (int i = 0; i < n; i++) {
      int id = framebuffers[i + offset];
      WebGLFramebuffer fb = this.frameBuffers.remove(id);
      gl.deleteFramebuffer(fb);
     }
  }

  @Override
  public void glDeleteProgram(int program) {
    WebGLProgram prog = programs.remove(program);
    gl.deleteProgram(prog);
  }

  @Override
  public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
    int pos = renderbuffers.position();
    for (int i = 0; i < n; i++) {
      int id = renderbuffers.get(pos + i);
      WebGLRenderbuffer rb = this.renderBuffers.remove(id);
      gl.deleteRenderbuffer(rb);
    }
  }


  @Override
  public void glDeleteRenderbuffers(int n, int[] renderbuffers, int offset) {
    for (int i = 0; i < n; i++) {
      int id = renderbuffers[i + offset];
      WebGLRenderbuffer rb = this.renderBuffers.remove(id);
      gl.deleteRenderbuffer(rb);
    }
  }

  @Override
  public void glDeleteShader(int shader) {
    WebGLShader sh = shaders.remove(shader);
    gl.deleteShader(sh);
  }

  @Override
  public void glDeleteTextures(int n, IntBuffer textures) {
    int pos = textures.position();
    for (int i = 0; i < n; i++) {
      int id = textures.get(pos + i);
      WebGLTexture texture = this.textures.remove(id);
      gl.deleteTexture(texture);
    }
  }

  @Override
  public void glDeleteTextures(int n, int[] textures, int offset) {
    for (int i = 0; i < n; i++) {
      int id = textures[i + offset];
      WebGLTexture texture = this.textures.remove(id);
      gl.deleteTexture(texture);
    }
  }

  @Override
  public void glDepthFunc(int func) {
    gl.depthFunc(func);
  }

  @Override
 public void glDepthMask (boolean flag) {
    gl.depthMask(flag);
  }


  @Override
  public void glDepthRange(double zNear, double zFar) {
    gl.depthRange((float) zNear, (float) zFar);
  }

  @Override
  public void glDepthRangef(float zNear, float zFar) {
    gl.depthRange(zNear, zFar);
  }

  @Override
  public void glDetachShader(int program, int shader) {
    gl.detachShader(programs.get(program), shaders.get(shader));
  }

  @Override
  public void glDisable(int cap) {
    gl.disable(cap);
  }

  @Override
  public void glDisableVertexAttribArray(int index) {
    enabledArrays &= ~(1 << index);
  }

  @Override
  public void glDrawArrays(int mode, int first, int count) {
    prepareDraw();
    gl.drawArrays(mode, first, count);
  }

  @Override
  public void glDrawElements(int mode, int count, int type, Buffer indices) {
    prepareDraw();
    if (boundElementArrayBuffer != elementBuffer) {
      gl.bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer);
      boundElementArrayBuffer = elementBuffer;
    }
    gl.bufferData(ELEMENT_ARRAY_BUFFER, getTypedArray(indices, type, count * getTypeSize(type)),
                  STREAM_DRAW);
//    if ("ModelPart".equals(debugInfo)) {
//      HtmlPlatform.log.info("drawElements f. ModelPart; count: " + count);
//    }
    gl.drawElements(mode, count, type, 0);
  }


  @Override
  public void glDrawElements(int mode, int count, int type, int indices) {
    prepareDraw();
    if (requestedElementArrayBuffer != boundElementArrayBuffer) {
      gl.bindBuffer(GL_ELEMENT_ARRAY_BUFFER, requestedElementArrayBuffer);
      boundElementArrayBuffer = requestedElementArrayBuffer;
    }
    gl.drawElements(mode, count, type, indices);
  }

  @Override
  public void glEnable(int cap) {
    gl.enable(cap);
  }

  @Override
  public void glEnableVertexAttribArray(int index) {
    enabledArrays |= (1 << index);
  }

  @Override
  public void glFinish() {
    gl.finish();
  }

  @Override
  public void glFlush() {
    gl.flush();
  }

  @Override
  public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
    gl.framebufferRenderbuffer(target, attachment, renderbuffertarget, renderBuffers.get(renderbuffer));
  }

  @Override
  public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
    gl.framebufferTexture2D(target, attachment, textarget, textures.get(texture), level);
  }

  @Override
  public void glFramebufferTexture3D(int target, int attachment, int textarget, int texture, int level, int zoffset) {
    throw new RuntimeException("NYI glFramebufferTexture3D");
  }

  @Override
  public void glFrontFace(int mode) {
    gl.frontFace(mode);
  }

  @Override
  public int glGenBuffer () {
    WebGLBuffer buffer = gl.createBuffer();
    return buffers.add(buffer);
  }

  @Override
  public void glGenBuffers(int n, IntBuffer buffers) {
    int pos = buffers.position();
    for (int i = 0; i < n; i++) {
      WebGLBuffer buffer = gl.createBuffer();
      int id = this.buffers.add(buffer);
      buffers.put(pos + i, id);
    }
  }

  @Override
  public void glGenBuffers(int n, int[] buffers, int offset) {
    for (int i = 0; i < n; i++) {
      WebGLBuffer buffer = gl.createBuffer();
      int id = this.buffers.add(buffer);
      buffers[i + offset] = id;
    }
  }

  @Override
  public void glGenerateMipmap (int target) {
    gl.generateMipmap(target);
  }

  @Override
  public void glGenFramebuffers (int n, IntBuffer framebuffers) {
   int pos = framebuffers.position();
   for (int i = 0; i < n; i++) {
      WebGLFramebuffer fb = gl.createFramebuffer();
      int id = this.frameBuffers.add(fb);
      framebuffers.put(pos + i, id);
    }
  }

  @Override
  public void glGenFramebuffers(int n, int[] framebuffers, int offset) {
    for (int i = 0; i < n; i++) {
      WebGLFramebuffer fb = gl.createFramebuffer();
      int id = this.frameBuffers.add(fb);
      framebuffers[i + offset] = id;
    }
  }

  @Override
  public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
    int pos = renderbuffers.position();
    for (int i = 0; i < n; i++) {
      WebGLRenderbuffer rb = gl.createRenderbuffer();
      int id = this.renderBuffers.add(rb);
      renderbuffers.put(pos + i, id);
    }
  }

  @Override
  public void glGenRenderbuffers(int n, int[] renderbuffers, int offset) {
    for (int i = 0; i < n; i++) {
      WebGLRenderbuffer rb = gl.createRenderbuffer();
      int id = this.renderBuffers.add(rb);
      renderbuffers[i + offset] = id;
    }
  }

  @Override
  public void glGenTextures(int n, IntBuffer textures) {
    int pos = textures.position();
    for (int i = 0; i < n; i++) {
      WebGLTexture texture = gl.createTexture();
      int id = this.textures.add(texture);
      textures.put(pos + i, id);
    }
  }

  @Override
  public void glGenTextures(int n, int[] textures, int offset) {
    for (int i = 0; i < n; i++) {
      WebGLTexture texture = gl.createTexture();
      int id = this.textures.add(texture);
      textures[i + offset] = id;
    }
  }


  @Override
  public void glGetActiveAttrib(int program, int index, int bufsize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name) {
    throw new RuntimeException("NYI glGetActiveAttrib");
  }

  @Override
  public void glGetActiveAttrib(int program, int index, int bufsize, int[] length, int lengthOffset, int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
    throw new RuntimeException("NYI glGetActiveAttrib");
  }

  @Override
  public void glGetAttachedShaders(int program, int maxcount, IntBuffer count, IntBuffer shaders) {
    throw new RuntimeException("NYI glGetAttachedShaders");
  }

  @Override
  public void glGetAttachedShaders(int program, int maxcount, int[] count, int countOffset, int[] shaders, int shadersOffset) {
    throw new RuntimeException("NYI glGetAttachedShaders");
  }

  @Override
  public void glGetActiveUniform(int program, int index, int bufsize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name) {
    throw new RuntimeException("NYI glGetActiveUniform");
  }

  @Override
  public void glGetActiveUniform(int program, int index, int bufsize, int[] length, int lengthOffset, int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
    throw new RuntimeException("NYI glGetActiveUniform");
  }

  @Override
  public int glGetAttribLocation(int program, String name) {
    WebGLProgram prog = programs.get(program);
    return gl.getAttribLocation(prog, name);
  }

  @Override
  public boolean glGetBoolean(int pname) {
    return gl.getParameterb(pname);
  }

  @Override
  public void glGetBooleanv(int pname, ByteBuffer params) {
    throw new RuntimeException("NYI glGetBooleanv");
  }

  @Override
  public int glGetBoundBuffer(int arg0) {
    throw new RuntimeException("NYI glGetBoundBuffer");
  }

  @Override
  public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
    params.put(params.position(), gl.getBufferParameter(target, pname));
  }

  @Override
  public final int glGetError() {
    return gl.getError();
  }

  @Override
  public int glGetInteger(int pname) {
    return gl.getParameteri(pname);
  }

  @Override
  public void glGetIntegerv(int pname, IntBuffer params) {
    Int32Array result = (Int32Array) gl.getParameterv(pname);
    int pos = params.position();
    int len = result.length();
    for (int i = 0; i < len; i++) {
      params.put(pos + i, result.get(i));
    }
  }

  @Override
  public float glGetFloat(int pname) {
    return gl.getParameterf(pname);
  }

  @Override
  public void glGetFloatv(int pname, FloatBuffer params) {
    throw new RuntimeException("NYI glGetFloatv");
  }

  @Override
  public void glGetFramebufferAttachmentParameteriv(int target, int attachment,
                                                              int pname, IntBuffer params) {
    throw new RuntimeException("NYI glGetFramebufferAttachmentParameteriv");
  }

  @Override
  public void glGetProgramiv(int program, int pname, IntBuffer params) {
    if (pname == GL_LINK_STATUS) {
      params.put(gl.getProgramParameterb(programs.get(program), LINK_STATUS) ? GL_TRUE : GL_FALSE);
    } else {
      throw new RuntimeException("NYI glGetProgramiv");
    }
  }

  @Override
  public String glGetProgramInfoLog(int program) {
    return gl.getProgramInfoLog(programs.get(program));
  }

  @Override
  public void glGetProgramBinary(int arg0, int arg1, IntBuffer arg2, IntBuffer arg3, Buffer arg4) {
    throw new RuntimeException("NYI glGetProgramBinary");
  }

  @Override
  public void glGetProgramInfoLog(int program, int bufsize, IntBuffer length, ByteBuffer infolog) {
    throw new RuntimeException("NYI glGetProgramInfoLog");
  }

  @Override
  public void glGetProgramiv(int program, int pname, int[] params, int offset) {
    if (pname == GL_LINK_STATUS) params[offset] = gl.getProgramParameterb(programs.get(program), LINK_STATUS) ? GL_TRUE : GL_FALSE;
    else throw new RuntimeException("NYI glGetProgramiv: " + pname);
  }

  @Override
  public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("NYI glGetRenderbufferParameteriv");
  }

  @Override
  public void glGetShaderInfoLog(int shader, int bufsize, IntBuffer length, ByteBuffer infolog) {
    throw new RuntimeException("NYI glGetShaderInfoLog");
  }

  @Override
  public void glGetShaderiv(int shader, int pname, int[] params, int offset) {
    if (pname == GL_COMPILE_STATUS) params[offset] = gl.getShaderParameterb(shaders.get(shader), COMPILE_STATUS) ? GL_TRUE : GL_FALSE;
    else throw new RuntimeException("NYI glGetShaderiv: " + pname);
  }

  @Override
  public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, int[] range, int rangeOffset, int[] precision, int precisionOffset) {
    throw new RuntimeException("NYI glGetShaderPrecisionFormat");
  }
  @Override
  public void glGetShaderSource(int shader, int bufsize, int[] length, int lengthOffset, byte[] source, int sourceOffset) {
    throw new RuntimeException("NYI glGetShaderSource");
  }
  @Override
  public void glGetShaderSource(int shader, int bufsize, IntBuffer length, ByteBuffer source) {
    throw new RuntimeException("NYI glGetShaderSource");
  }

  @Override
  public void glGetShaderiv(int shader, int pname, IntBuffer params) {
    if (pname == GL_COMPILE_STATUS) {
      params.put(gl.getShaderParameterb(shaders.get(shader), COMPILE_STATUS) ? GL_TRUE : GL_FALSE);
    } else {
      throw new RuntimeException("NYI glGetShaderiv: " + pname);
    }
  }

  @Override
  public String glGetShaderInfoLog(int shader) {
    return gl.getShaderInfoLog(shaders.get(shader));
  }

  @Override
  public void glGetShaderPrecisionFormat(int shadertype, int precisiontype,
                                                   IntBuffer range, IntBuffer precision) {
    throw new RuntimeException("NYI glGetShaderInfoLog");
  }

  @Override
  public String glGetString(int id) {
    return gl.getParameterString(id);
  }

  @Override
  public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
    params.put(params.position(), gl.getTexParameter(target, pname));
  }

  @Override
  public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
    params.put(params.position(), gl.getTexParameter(target, pname));
  }

  @Override
  public void glGetUniformfv(int program, int location, FloatBuffer params) {
    Float32Array v = gl.getUniformv(programs.get(program), uniforms.get(program).get(location));
    for (int i = 0; i < v.length(); i++) {
      params.put(params.position() + i, v.get(i));
    }
  }

  @Override
  public void glGetUniformiv(int program, int location, IntBuffer params) {
    Int32Array v = gl.getUniformv(programs.get(program), uniforms.get(program).get(location));
    for (int i = 0; i < v.length(); i++) {
      params.put(params.position() + i, v.get(i));
    }
  }

  @Override
  public int glGetUniformLocation(int program, String name) {
    WebGLUniformLocation location = gl.getUniformLocation(programs.get(program), name);
    IntMap<WebGLUniformLocation> progUniforms = uniforms.get(program);
    if (progUniforms == null) {
      progUniforms = IntMap.<WebGLUniformLocation>create();
      uniforms.put(program, progUniforms);
    }
    // FIXME check if uniform already stored.
    int id = progUniforms.add(location);
    return id;
  }

  @Override
  public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
    Float32Array v = gl.getVertexAttribv(index, pname);
    for (int i = 0; i < v.length(); i++) {
      params.put(params.position() + i, v.get(i));
    }
  }

  @Override
  public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
    throw new UnsupportedOperationException("NYI glGetVertexAttribiv: WebGL getVertexAttribv always returns a float buffer.");
  }

  @Override
  public void glHint(int target, int mode) {
    gl.hint(target, mode);
  }

  @Override
  public boolean glIsBuffer(int buffer) {
    return gl.isBuffer(buffers.get(buffer));
  }

  @Override
  public boolean glIsEnabled(int cap) {
    return gl.isEnabled(cap);
  }

  @Override
  public boolean glIsFramebuffer(int framebuffer) {
    return gl.isFramebuffer(frameBuffers.get(framebuffer));
  }

  @Override
  public boolean glIsProgram(int program) {
    return gl.isProgram(programs.get(program));
  }

  @Override
  public boolean glIsRenderbuffer(int renderbuffer) {
    return gl.isRenderbuffer(renderBuffers.get(renderbuffer));
  }

  @Override
  public boolean glIsShader(int shader) {
    return gl.isShader(shaders.get(shader));
  }

  @Override
  public boolean glIsTexture(int texture) {
    return gl.isTexture(textures.get(texture));
  }

  @Override
  public boolean glIsVBOArrayEnabled() {
    throw new RuntimeException("NYI glIsVBOArrayEnabled");
  }

  @Override
  public boolean glIsVBOElementEnabled() {
    throw new RuntimeException("NYI glIsVBOElementEnabled");
  }

  @Override
  public void glLineWidth(float width) {
    gl.lineWidth(width);
  }

  @Override
  public void glLinkProgram(int program) {
    gl.linkProgram(programs.get(program));
  }

  @Override
  public ByteBuffer glMapBuffer(int arg0, int arg1) {
    throw new RuntimeException("NYI glMapBuffer");
  }

  @Override
  public void glPixelStorei(int i, int j) {
    gl.pixelStorei(i, j);
  }

  @Override
  public String getPlatformGLExtensions() {
    throw new RuntimeException("NYI getPlatformGLExtensions");
  }

  @Override
  public void glPolygonOffset(float factor, float units) {
    gl.polygonOffset(factor, units);
  }

  @Override
  public void glProgramBinary(int arg0, int arg1, Buffer arg2, int arg3) {
    throw new RuntimeException("NYI glProgramBinary");
  }

  @Override
  public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
    gl.readPixels(x, y, width, height, format, type, getTypedArray(pixels, type, -1));
  }

  @Override
  public void glReadPixels(int x, int y, int width, int height, int format, int type, int pixelsBufferOffset) {
    throw new RuntimeException("NYI glReadPixels");
  }

  @Override
  public void glReleaseShaderCompiler() {
    throw new RuntimeException("NYI glReleaseShaderCompiler");
  }

  @Override
  public void glRenderbufferStorage(int target, int internalformat, int width,
                                              int height) {
    gl.renderbufferStorage(target, internalformat, width, height);
  }

  @Override
  public void glSampleCoverage(float value, boolean invert) {
    gl.sampleCoverage(value, invert);
  }

  @Override
  public final void glScissor(int x, int y, int width, int height) {
    gl.scissor(x, y, width, height);
  }

  @Override
  public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
    throw new RuntimeException("NYI glReleaseShaderCompiler");
  }

  @Override
  public void glShaderBinary(int n, int[] shaders, int offset, int binaryformat, Buffer binary, int length) {
    throw new RuntimeException("NYI glShaderBinary");
  }

  @Override
  public void glShaderSource(int shader, int count, String[] strings, IntBuffer length) {
    throw new RuntimeException("NYI glShaderSource");
  }

  @Override
  public void glShaderSource(int shader, int count, String[] strings, int[] length, int lengthOffset) {
    throw new RuntimeException("NYI glShaderSource");
  }

  @Override
  public void glShaderSource(int shader, String string) {
    gl.shaderSource(shaders.get(shader), string);
  }

  @Override
  public void glStencilFunc(int func, int ref, int mask) {
    gl.stencilFunc(func, ref, mask);
  }

  @Override
  public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
    gl.stencilFuncSeparate(face, func, ref, mask);
  }

  @Override
  public void glStencilMask(int mask) {
    gl.stencilMask(mask);
  }

  @Override
  public void glStencilMaskSeparate(int face, int mask) {
    gl.stencilMaskSeparate(face, mask);
  }

  @Override
  public void glStencilOp(int fail, int zfail, int zpass) {
    gl.stencilOp(fail, zfail, zpass);
  }

  @Override
  public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
    gl.stencilOpSeparate(face, fail, zfail, zpass);
  }

  @Override
  public void glTexImage2D(int target, int level, int internalformat, int width,
                                     int height, int border, int format, int type, Buffer pixels) {
    ArrayBufferView buffer = (pixels == null) ? null : getTypedArray(pixels, type, -1);
    gl.texImage2D(target, level, internalformat, width, height, border, format, type, buffer);
  }

  @Override
  public void glTexImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI glTexImage2D");
  }

  public void glTexImage2D(int target, int level, int internalformat, int format, int type, ImageElement image) {
    gl.texImage2D(target, level, internalformat, format, type, image);
    checkError("texImage2D");
  }

  public void glTexImage2D(int target, int level, int internalformat, int format, int type, CanvasElement image) {
    gl.texImage2D(target, level, internalformat, format, type, image);
    checkError("texImage2D");
  }

  @Override
  public void glTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, Buffer arg9) {
    throw new RuntimeException("NYI glTexImage3D");
  }

  @Override
  public void glTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9) {
    throw new RuntimeException("NYI glTexImage3D");
  }

  @Override
  public void glTexParameteri(int glTexture2d, int glTextureMinFilter, int glFilterMin) {
    gl.texParameteri(glTexture2d, glTextureMinFilter, glFilterMin);
  }

  @Override
  public void glTexParameterf(int target, int pname, float param) {
    gl.texParameterf(target, pname, param);
  }

  @Override
  public void glTexParameterfv(int target, int pname, FloatBuffer params) {
    throw new RuntimeException("NYI glTexParameterfv");
  }

  @Override
  public void glTexParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("NYI glTexParameteriv");
  }

  @Override
  public void glTexSubImage2D(int target, int level, int xoffset, int yoffset,
                                        int width, int height, int format, int type, Buffer pixels) {
    gl.texSubImage2D(target, level, xoffset, yoffset, width, height, format, type,
            getTypedArray(pixels, type, -1));
  }

  @Override
  public void glTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI glTexSubImage2D");
  }

  @Override
  public void glTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, Buffer arg10) {
    throw new RuntimeException("NYI glTexSubImage3D");
  }

  @Override
  public void glTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10) {
    throw new RuntimeException("NYI glTexSubImage3D");
  }

  @Override
  public boolean glUnmapBuffer(int arg0) {
    throw new RuntimeException("NYI glUnmapBuffer");
  }

  @Override
  public void glUniform1f(int location, float x) {
    gl.uniform1f(getUniformLocation(location), x);
  }

  @Override
  public void glUniform1fv(int location, int count, FloatBuffer v) {
    throw new RuntimeException("NYI glUniform1fv");
  }

  @Override
  public void glUniform1i(int location, int x) {
    gl.uniform1i(getUniformLocation(location), x);
  }

  @Override
  public void glUniform1iv(int location, int count, IntBuffer v) {
    gl.uniform1iv(getUniformLocation(location), (Int32Array) getTypedArray(v, GL_INT, count * 4));
  }

  @Override
  public void glUniform2f(int location, float x, float y) {
    gl.uniform2f(getUniformLocation(location), x, y);
  }

  @Override
  public void glUniform2fv(int location, int count, FloatBuffer v) {
    gl.uniform2fv(getUniformLocation(location),
            (Float32Array) getTypedArray(v, GL_FLOAT, count * 2 * 4));
  }

  @Override
  public void glUniform2i(int location, int x, int y) {
    gl.uniform2i(getUniformLocation(location), x, y);
  }

  @Override
  public void glUniform2iv(int location, int count, IntBuffer v) {
    throw new RuntimeException("NYI glUniform2iv");
  }

  @Override
  public void glUniform3f(int location, float x, float y, float z) {
    gl.uniform3f(getUniformLocation(location), x, y, z);
  }

  @Override
  public void glUniform3fv(int location, int count, FloatBuffer v) {
    throw new RuntimeException("NYI glUniform3fv");
  }

  @Override
  public void glUniform3i(int location, int x, int y, int z) {
    gl.uniform3i(getUniformLocation(location), x, y, z);
  }

  @Override
  public void glUniform3iv(int location, int count, IntBuffer v) {
    throw new RuntimeException("NYI glUniform3fi");
  }

  @Override
  public void glUniform4f(int location, float x, float y, float z, float w) {
    gl.uniform4f(getUniformLocation(location), x, y, z, w);
  }

  @Override
  public void glUniform4fv(int location, int count, FloatBuffer v) {
    gl.uniform4fv(getUniformLocation(location),
            (Float32Array) getTypedArray(v, GL_FLOAT, 4 * 4 * count));
  }

  @Override
  public void glUniform4i(int location, int x, int y, int z, int w) {
    gl.uniform4i(getUniformLocation(location), x, y, z, w);
  }

  @Override
  public void glUniform4iv(int location, int count, IntBuffer v) {
    throw new RuntimeException("NYI glUniform4iv");
  }

  @Override
  public void glUniformMatrix2fv(int location, int count, boolean transpose,
                                           FloatBuffer value) {
    throw new RuntimeException("NYI glUniformMatrix2fv");
  }

  @Override
  public void glUniformMatrix3fv(int location, int count, boolean transpose,
                                           FloatBuffer value) {
    throw new RuntimeException("NYI glUniformMatrix3fv");
  }

  @Override
  public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value) {
    gl.uniformMatrix4fv(getUniformLocation(location), transpose,
            (Float32Array) getTypedArray(value, GL_FLOAT, count * 16 * 4));
  }

  @Override
  public void glUseProgram(int program) {
    currProgram = program;
    gl.useProgram(programs.get(program));
  }

  @Override
  public void glValidateProgram(int program) {
    gl.validateProgram(programs.get(program));
  }

  @Override
  public void glVertexAttrib1f(int indx, float x) {
    gl.vertexAttrib1f(indx, x);
  }

  @Override
  public void glVertexAttrib1fv(int indx, FloatBuffer values) {
    gl.vertexAttrib1fv(indx, copy(values));
  }

  @Override
  public void glVertexAttrib2f(int indx, float x, float y) {
    gl.vertexAttrib2f(indx, x, y);
  }

  @Override
  public void glVertexAttrib2fv(int indx, FloatBuffer values) {
    gl.vertexAttrib2fv(indx, copy(values));
  }

  @Override
  public void glVertexAttrib3f(int indx, float x, float y, float z) {
    gl.vertexAttrib3f(indx, x, y, z);
  }

  @Override
  public void glVertexAttrib3fv(int indx, FloatBuffer values) {
    gl.vertexAttrib3fv(indx, copy(values));
  }

  @Override
  public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
    gl.vertexAttrib4f(indx, x, y, z, w);
  }

  @Override
  public void glVertexAttrib4fv(int indx, FloatBuffer values) {
    gl.vertexAttrib4fv(indx, copy(values));
  }

  // arrayId (index) is in the range 0..GL_MAX_VERTEX_ATTRIBS-1
  @Override
  public void glVertexAttribPointer(int arrayId, int size, int type, boolean normalize,
                                              int byteStride, Buffer nioBuffer) {

    VertexAttribArrayState data = vertexAttribArrayState[arrayId];

//    HtmlPlatform.log.info("glVertexAttribPointer Data size: " + nioBuffer.remaining());
    useNioBuffer |= 1 << arrayId;
    data.nioBuffer = nioBuffer;
    data.nioBufferPosition = nioBuffer.position();
    data.nioBufferLimit = nioBuffer.limit();
    data.size = size;
    data.type = type;
    data.normalize = normalize;
    data.stride = byteStride == 0 ? size * getTypeSize(type) : byteStride;
  }

  @Override
  public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr) {
    useNioBuffer &= ~(1 << indx);
    if (boundArrayBuffer != requestedArrayBuffer) {
      gl.bindBuffer(GL_ARRAY_BUFFER, requestedArrayBuffer);
      boundArrayBuffer = requestedArrayBuffer;
    }

    gl.vertexAttribPointer(indx, size, type, normalized, stride, ptr);
  }

  @Override
  public void glViewport(int x, int y, int w, int h) {
    gl.viewport(x, y, w, h);
  }

  @Override
  public boolean isExtensionAvailable(String extension) {
    throw new RuntimeException("NYI isExtensionAvailable");
  }
  @Override
  public boolean isFunctionAvailable(String function) {
    throw new RuntimeException("NYI isFunctionAvailable");
  }

  @Override
  public boolean hasGLSL() {
    return true;
  }
}
