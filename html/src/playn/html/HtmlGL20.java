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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.webgl.client.WebGLBuffer;
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLObject;
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

  enum WebGLObjectType {
    NULL, BUFFER, FRAME_BUFFER, PROGRAM, RENDER_BUFFER, SHADER, TEXTURE, UNIFORM_LOCATION,
  }

  class VertexAttribArrayState {
    int type;
    int size;
    int stride;
    boolean normalize;
    Buffer nioBuffer;
    int nioBufferPosition;
    int nioBufferLimit;
    WebGLBuffer webGlBuffer;
  }

  static final int VERTEX_ATTRIB_ARRAY_COUNT = 5; //  position, color, texture0, texture1, normals

  private int enabledArrays = 0;
  private int previouslyEnabledArrays = 0;
  private int useNioBuffer = 0;

  private VertexAttribArrayState[] vertexAttribArrayState =
    new VertexAttribArrayState[VERTEX_ATTRIB_ARRAY_COUNT];

  @SuppressWarnings("unchecked")
  private JsArray<WebGLObject> webGLObjects = (JsArray<WebGLObject>) JsArray.createArray();
  private JsArrayInteger webGLObjectTypes = (JsArrayInteger) JsArrayInteger.createArray();

  private WebGLBuffer elementBuffer;
  private WebGLBuffer boundArrayBuffer;
  private WebGLBuffer requestedArrayBuffer;
  private WebGLBuffer boundElementArrayBuffer;
  private WebGLBuffer requestedElementArrayBuffer;

  private WebGLRenderingContext glc;

  public HtmlGL20 () {
    super(new Buffers() {
      public ByteBuffer createByteBuffer(int size) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
      }
    }, HtmlUrlParameters.checkGLErrors);
  }

  protected int createObject(WebGLObject object, WebGLObjectType type) {
    // TODO (haustein) keep track of empty positions.
//    for (int i = 0; i < webGLObjects.size(); i++) {
//      if (webGLObjects.get(i) == null) {
//        webGLObjects.set(i, container);
//        return i;
//      }
//    }
    webGLObjects.push(object);
    webGLObjectTypes.push(type.ordinal());
    return webGLObjects.length() - 1;
  }

  protected void deleteObject(int index, WebGLObjectType type) {
    WebGLObject object = webGLObjects.get(index);
    webGLObjects.set(index, null);
    webGLObjectTypes.set(index, WebGLObjectType.NULL.ordinal());
    switch(type) {
      case BUFFER:
        glc.deleteBuffer((WebGLBuffer) object);
        break;
      case FRAME_BUFFER:
        glc.deleteFramebuffer((WebGLFramebuffer) object);
        break;
      case PROGRAM:
        glc.deleteProgram((WebGLProgram) object);
        break;
      case RENDER_BUFFER:
        glc.deleteRenderbuffer((WebGLRenderbuffer) object);
        break;
      case SHADER:
        glc.deleteShader((WebGLShader) object);
        break;
      case TEXTURE:
        glc.deleteTexture((WebGLTexture) object);
        break;
      default:
        break;
    }
  }

  protected void deleteObjects(int count, IntBuffer indices, WebGLObjectType type) {
    for (int i = 0; i < count; i++) {
      int index = indices.get(indices.position() + i);
      deleteObject(index, type);
    }
  }

  protected void deleteObjects(int count, int[] indices, int offset, WebGLObjectType type) {
    for (int i = 0; i < count; i++) {
      deleteObject(indices[offset+i], type);
    }
  }

protected WebGLObject genObject(WebGLObjectType type) {
    switch(type) {
      case BUFFER:
        return glc.createBuffer();
      case FRAME_BUFFER:
        return glc.createFramebuffer();
      case PROGRAM:
        return glc.createProgram();
      case RENDER_BUFFER:
        return glc.createRenderbuffer();
      case TEXTURE:
        return glc.createTexture();
      default:
        throw new RuntimeException("genObject(s) not supported for type " + type);
    }
  }

  protected void genObjects(int count, int[] names, int offset, WebGLObjectType type) {
    // createObject loop
    for (int i = 0; i < count; i++) {
      WebGLObject object = genObject(type);
      names[i + offset] = createObject(object, type);
    }
  }

  protected void genObjects(int count, IntBuffer names, WebGLObjectType type) {
    // createObject loop
    for (int i = 0; i < count; i++) {
      WebGLObject object = genObject(type);
      names.put(i + names.position(), createObject(object, type));
    }
  }

  private WebGLBuffer getBuffer (int index) {
    return (WebGLBuffer) webGLObjects.get(index);
  }

  private static int getElementSize(Buffer buffer) {
    if ((buffer instanceof FloatBuffer) || (buffer instanceof IntBuffer)) return 4;
    else if (buffer instanceof ShortBuffer) return 2;
    else if (buffer instanceof ByteBuffer) return 1;
    else throw new RuntimeException("Unrecognized buffer type: " + buffer.getClass());
  }

  private WebGLFramebuffer getFramebuffer (int index) {
    return (WebGLFramebuffer) webGLObjects.get(index);
  }

  private WebGLProgram getProgram (int index) {
    return (WebGLProgram) webGLObjects.get(index);
  }

  private WebGLRenderbuffer getRenderbuffer(int index) {
    return (WebGLRenderbuffer) webGLObjects.get(index);
  }

  private WebGLShader getShader(int index) {
    return (WebGLShader) webGLObjects.get(index);
  }

  protected WebGLTexture getTexture(int index) {
    return (WebGLTexture) webGLObjects.get(index);
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

  protected WebGLUniformLocation getUniformLocation(int index) {
    return (WebGLUniformLocation) webGLObjects.get(index);
  }

  void init (WebGLRenderingContext glc) {
    // TODO: do we always want to do this?
    glc.pixelStorei(UNPACK_PREMULTIPLY_ALPHA_WEBGL, ONE);
    this.glc = glc;

    webGLObjects.push(null);
    webGLObjectTypes.push(WebGLObjectType.NULL.ordinal());
    elementBuffer = glc.createBuffer();

    for (int ii = 0; ii < VERTEX_ATTRIB_ARRAY_COUNT; ii++) {
      VertexAttribArrayState data = new VertexAttribArrayState();
      data.webGlBuffer = glc.createBuffer();
      vertexAttribArrayState[ii] = data;
    }
  }

  protected boolean isObjectType (int index,  WebGLObjectType type) {
    return webGLObjectTypes.get(index) == type.ordinal();
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
          glc.enableVertexAttribArray(i);
        } else {
          glc.disableVertexAttribArray(i);
        }
      }
      if (enabled != 0 && (useNioBuffer & mask) != 0) {
        VertexAttribArrayState data = vertexAttribArrayState[i];
        if (previousNio != null && previousNio.nioBuffer == data.nioBuffer &&
            previousNio.nioBufferLimit >= data.nioBufferLimit) {
          if (boundArrayBuffer != previousNio.webGlBuffer) {
            glc.bindBuffer(ARRAY_BUFFER, previousNio.webGlBuffer);
            boundArrayBuffer = data.webGlBuffer;
          }
          glc.vertexAttribPointer(i, data.size, data.type, data.normalize, data.stride,
                                 data.nioBufferPosition * previousElementSize);
        } else {
          if (boundArrayBuffer != data.webGlBuffer) {
            glc.bindBuffer(ARRAY_BUFFER, data.webGlBuffer);
            boundArrayBuffer = data.webGlBuffer;
          }
          int elementSize = getElementSize(data.nioBuffer);
          int savePosition = data.nioBuffer.position();
          if (data.nioBufferPosition * elementSize < data.stride) {
            data.nioBuffer.position(0);
            glc.bufferData(ARRAY_BUFFER, getTypedArray(data.nioBuffer, data.type, data.nioBufferLimit *
                                                      elementSize), STREAM_DRAW);
            glc.vertexAttribPointer(i, data.size, data.type, data.normalize, data.stride,
                                   data.nioBufferPosition * elementSize);
            previousNio = data;
            previousElementSize = elementSize;
          } else {
            data.nioBuffer.position(data.nioBufferPosition);
            glc.bufferData(ARRAY_BUFFER, getTypedArray(data.nioBuffer, data.type,
                                                      (data.nioBufferLimit - data.nioBufferPosition) *
                                                      elementSize), STREAM_DRAW);
            glc.vertexAttribPointer(i, data.size, data.type, data.normalize, data.stride, 0);
          }
          data.nioBuffer.position(savePosition);
        }
      }
    }

    previouslyEnabledArrays = enabledArrays;
  }

  //
  //
  // Public methos. Please keep ordered -----------------------------------------------------------------------------
  //
  //

  @Override public int getSwapInterval() {
    throw new RuntimeException("NYI getSwapInterval");
  }

  @Override public void glActiveTexture(int texture) {
    glc.activeTexture(texture);
  }

  @Override public void glAttachShader(int program, int shader) {
    glc.attachShader((WebGLProgram) webGLObjects.get(program),
            (WebGLShader) webGLObjects.get(shader));
  }

  @Override public void glBindAttribLocation(int program, int index, String name) {
    glc.bindAttribLocation((WebGLProgram) webGLObjects.get(program), index, name);
  }

  @Override public void glBindBuffer(int target, int buffer) {
    // Yes, bindBuffer is so expensive that this makes sense..
    WebGLBuffer webGlBuf = getBuffer(buffer);
    if (target == GL_ARRAY_BUFFER) {
      requestedArrayBuffer = webGlBuf;
    } else if (target == GL_ELEMENT_ARRAY_BUFFER) {
      requestedElementArrayBuffer = webGlBuf;
    } else {
      glc.bindBuffer(target, webGlBuf);
    }
  }

  @Override public void glBindFramebuffer(int target, int framebuffer) {
    glc.bindFramebuffer(target, getFramebuffer(framebuffer));
  }

  @Override public void glBindRenderbuffer(int target, int renderbuffer) {
    glc.bindRenderbuffer(target, getRenderbuffer(renderbuffer));
  }

  @Override public void glBindTexture(int target, int textureId) {
    glc.bindTexture(target, getTexture(textureId));
  }

  @Override public void glBlendColor(float red, float green, float blue, float alpha) {
    glc.blendColor(red, green, blue, alpha);
  }

  @Override public void glBlendEquation(int mode) {
    glc.blendEquation(mode);
  }

  @Override public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
    glc.blendEquationSeparate(modeRGB, modeAlpha);
  }

  @Override public final void glBlendFunc(int a, int b) {
    glc.blendFunc(a, b);
  }

  @Override public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha,
                                            int dstAlpha) {
    glc.blendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
  }

  @Override public void glBufferData(int target, int byteSize, Buffer data, int usage) {
    if (target == GL_ARRAY_BUFFER) {
      if (requestedArrayBuffer != boundArrayBuffer) {
        glc.bindBuffer(target, requestedArrayBuffer);
        boundArrayBuffer = requestedArrayBuffer;
      }
    } else if (target == GL_ELEMENT_ARRAY_BUFFER) {
      if (requestedElementArrayBuffer != boundElementArrayBuffer) {
        glc.bindBuffer(target, requestedElementArrayBuffer);
        boundElementArrayBuffer = requestedElementArrayBuffer;
      }
    }
    glc.bufferData(target, getTypedArray(data, GL_BYTE, byteSize), usage);
  }

  @Override public void glBufferSubData(int target, int offset, int size, Buffer data) {
    if (target == GL_ARRAY_BUFFER && requestedArrayBuffer != boundArrayBuffer) {
      glc.bindBuffer(target, requestedArrayBuffer);
      boundArrayBuffer = requestedArrayBuffer;
    }
    throw new RuntimeException("NYI glBufferSubData");
  }

  @Override public int glCheckFramebufferStatus(int target) {
    return glc.checkFramebufferStatus(target);
  }

  @Override public final void glClear(int mask) {
    glc.clear(mask);
  }

  @Override public final void glClearColor(float f, float g, float h, float i) {
    glc.clearColor(f, g, h, i);
  }

  @Override public void glClearDepth(double depth) {
    throw new RuntimeException("NYI glClearDepth");
  }

  @Override public void glClearDepthf(float depth) {
    glc.clearDepth(depth);
  }

  @Override public void glClearStencil(int s) {
    glc.clearStencil(s);
  }

  @Override public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
    glc.colorMask(red, green, blue, alpha);
  }

  @Override public void glCompileShader(int shader) {
    glc.compileShader(getShader(shader));
  }

  @Override public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data) {
    throw new RuntimeException("NYI glCompressedTexImage2D");
  }

  @Override public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data) {
    throw new RuntimeException("NYI glCompressedTexSubImage2D");
  }

  @Override public void glCompressedTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, Buffer arg8) {
    throw new RuntimeException("NYI glCompressedTexImage3D");
  }

  @Override public void glCompressedTexImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7) {
    throw new RuntimeException("NYI glCompressedTexImage2D");
  }

  @Override public void glCompressedTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI glCompressedTexImage3D");
  }

  @Override public void glCompressedTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI glCompressedTexSubImage2D");
  }

  @Override public void glCompressedTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10) {
    throw new RuntimeException("NYI glCompressedTexSubImage3D");
  }

  @Override public void glCompressedTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, Buffer arg10) {
    throw new RuntimeException("NYI glCompressedTexSubImage3D");
  }

  @Override public void glCopyTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI glCopyTexSubImage3D");
  }

  @Override public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border) {
    glc.copyTexImage2D(target, level, internalformat, x, y, width, height, border);
  }
  @Override public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
    glc.copyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
  }

  @Override public int glCreateProgram() {
    return createObject(glc.createProgram(), WebGLObjectType.PROGRAM);
  }

  @Override public int glCreateShader(int type) {
    return createObject(glc.createShader(type), WebGLObjectType.SHADER);
  }

  @Override public final void glCullFace(int c) {
    glc.cullFace(c);
  }

  @Override public void glDrawArrays(int mode, int first, int count) {
    prepareDraw();
    glc.drawArrays(mode, first, count);
  }

  @Override public void glDepthFunc(int func) {
    glc.depthFunc(func);
  }

  @Override public void glDepthMask(boolean b) {
    glc.depthMask(b);
  }

  @Override public void glDrawElements(int mode, int count, int type, Buffer indices) {
    prepareDraw();
    if (boundElementArrayBuffer != elementBuffer) {
      glc.bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer);
      boundElementArrayBuffer = elementBuffer;
    }
    glc.bufferData(ELEMENT_ARRAY_BUFFER, getTypedArray(indices, type, count * getTypeSize(type)),
                  STREAM_DRAW);
//    if ("ModelPart".equals(debugInfo)) {
//      HtmlPlatform.log.info("drawElements f. ModelPart; count: " + count);
//    }
    glc.drawElements(mode, count, type, 0);
  }
  @Override public void glDepthRangef(float zNear, float zFar) {
    glc.depthRange(zNear, zFar);
  }

  @Override public void glDeleteBuffers(int n, IntBuffer buffers) {
    deleteObjects(n, buffers, WebGLObjectType.BUFFER);
  }

  @Override public void glDeleteBuffers(int n, int[] buffers, int offset) {
    deleteObjects(n, buffers, offset, WebGLObjectType.BUFFER);
  }

  @Override public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
    deleteObjects(n, framebuffers, WebGLObjectType.FRAME_BUFFER);
  }

  @Override public void glDeleteFramebuffers(int n, int[] framebuffers, int offset) {
    deleteObjects(n, framebuffers, offset, WebGLObjectType.FRAME_BUFFER);
  }

  @Override public void glDeleteProgram(int program) {
    deleteObject(program, WebGLObjectType.PROGRAM);
  }

  @Override public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
    deleteObjects(n, renderbuffers, WebGLObjectType.RENDER_BUFFER);
  }

  @Override public void glDeleteTextures(int n, IntBuffer texnumBuffer) {
    deleteObjects(n, texnumBuffer, WebGLObjectType.TEXTURE);
  }

  @Override public void glDeleteRenderbuffers(int n, int[] renderbuffers, int offset) {
    deleteObjects(n, renderbuffers, offset, WebGLObjectType.RENDER_BUFFER);
  }

  @Override public void glDeleteShader(int shader) {
    deleteObject(shader, WebGLObjectType.SHADER);
  }

  @Override public void glDeleteTextures(int n, int[] textures, int offset) {
    deleteObjects(n, textures, offset, WebGLObjectType.TEXTURE);
  }

  @Override public void glDepthRange(double zNear, double zFar) {
    throw new RuntimeException("NYI glDepthRange");
  }

  @Override public void glDetachShader(int program, int shader) {
    glc.detachShader(getProgram(program), getShader(shader));
  }

  @Override public void glDisable(int cap) {
    glc.disable(cap);
  }

  @Override public void glDisableVertexAttribArray(int index) {
    enabledArrays &= ~(1 << index);
  }

  @Override public void glDrawElements(int mode, int count, int type, int indices) {
    prepareDraw();
    if (requestedElementArrayBuffer != boundElementArrayBuffer) {
      glc.bindBuffer(GL_ELEMENT_ARRAY_BUFFER, requestedElementArrayBuffer);
      boundElementArrayBuffer = requestedElementArrayBuffer;
    }
    glc.drawElements(mode, count, type, indices);
  }

  @Override public void glEnable(int cap) {
    glc.enable(cap);
  }

  @Override public void glEnableVertexAttribArray(int index) {
    enabledArrays |= (1 << index);
  }

  @Override public void glFinish() {
    glc.finish();
  }

  @Override public void glFlush() {
    glc.flush();
  }

  @Override public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
    glc.framebufferRenderbuffer(target, attachment, renderbuffertarget,
            getRenderbuffer(renderbuffer));
  }
  @Override public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
    glc.framebufferTexture2D(target, attachment, textarget, getTexture(texture), level);
  }

  @Override public void glFramebufferTexture3D(int target, int attachment, int textarget, int texture, int level, int zoffset) {
    throw new RuntimeException("NYI glFramebufferTexture3D");
  }

  @Override public void glFrontFace(int mode) {
    glc.frontFace(mode);
  }

  @Override public void glGenerateMipmap(int t) {
    glc.generateMipmap(t);
  }

  @Override public void glGenBuffers(int n, IntBuffer buffers) {
    genObjects(n, buffers, WebGLObjectType.BUFFER);
  }

  @Override public void glGenFramebuffers(int n, IntBuffer framebuffers) {
    genObjects(n, framebuffers, WebGLObjectType.FRAME_BUFFER);
  }
  @Override public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
    genObjects(n, renderbuffers, WebGLObjectType.RENDER_BUFFER);
  }
  @Override public void glGenTextures(int n, IntBuffer textures) {
    genObjects(n, textures, WebGLObjectType.TEXTURE);
  }

  @Override public void glGenBuffers(int n, int[] buffers, int offset) {
    genObjects(n, buffers, offset, WebGLObjectType.BUFFER);
  }
  @Override public void glGenFramebuffers(int n, int[] framebuffers, int offset) {
    genObjects(n, framebuffers, offset, WebGLObjectType.FRAME_BUFFER);
  }
  @Override public void glGenRenderbuffers(int n, int[] renderbuffers, int offset) {
    genObjects(n, renderbuffers, offset, WebGLObjectType.RENDER_BUFFER);
  }
  @Override public void glGenTextures(int n, int[] textures, int offset) {
    genObjects(n, textures, offset, WebGLObjectType.TEXTURE);
  }

  @Override public void glGetActiveAttrib(int program, int index, int bufsize, int[] length, int lengthOffset, int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
    throw new RuntimeException("NYI glGetActiveAttrib");
  }

  @Override public void glGetActiveAttrib(int program, int index, int bufsize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name) {
    throw new RuntimeException("NYI glGetActiveAttrib");
  }

  @Override public void glGetAttachedShaders(int program, int maxcount, IntBuffer count, IntBuffer shaders) {
    throw new RuntimeException("NYI glGetAttachedShaders");
  }

  @Override public void glGetAttachedShaders(int program, int maxcount, int[] count, int countOffset, int[] shaders, int shadersOffset) {
    throw new RuntimeException("NYI glGetAttachedShaders");
  }

  @Override public void glGetActiveUniform(int program, int index, int bufsize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name) {
    throw new RuntimeException("NYI glGetActiveUniform");
  }

  @Override public void glGetActiveUniform(int program, int index, int bufsize, int[] length, int lengthOffset, int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
    throw new RuntimeException("NYI glGetActiveUniform");
  }

  @Override public int glGetAttribLocation(int program, String name) {
    return glc.getAttribLocation(getProgram(program), name);
  }

  @Override public boolean glGetBoolean(int pname) {
    return glc.getParameterb(pname);
  }
  @Override public void glGetBooleanv(int pname, ByteBuffer params) {
    throw new RuntimeException("NYI glGetBooleanv");
  }

  @Override public int glGetBoundBuffer(int arg0) {
    throw new RuntimeException("NYI glGetBoundBuffer");
  }
  @Override public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
    params.put(params.position(), glc.getBufferParameter(target, pname));
  }

  @Override public final int glGetError() {
    return glc.getError();
  }

  @Override public int glGetInteger(int pname) {
    return glc.getParameteri(pname);
  }

  @Override public void glGetIntegerv(int pname, IntBuffer params) {
    Int32Array result = (Int32Array) glc.getParameterv(pname);
    int pos = params.position();
    int len = result.length();
    for (int i = 0; i < len; i++) {
      params.put(pos + i, result.get(i));
    }
  }

  @Override public float glGetFloat(int pname) {
    return glc.getParameterf(pname);
  }

  @Override public void glGetFloatv(int pname, FloatBuffer params) {
    throw new RuntimeException("NYI glGetFloatv");
  }

  @Override public void glGetFramebufferAttachmentParameteriv(int target, int attachment,
                                                              int pname, IntBuffer params) {
    throw new RuntimeException("NYI glGetFramebufferAttachmentParameteriv");
  }

  @Override public void glGetProgramiv(int program, int pname, IntBuffer params) {
    if (pname == GL_LINK_STATUS) {
      params.put(glc.getProgramParameterb(getProgram(program), LINK_STATUS) ? GL_TRUE : GL_FALSE);
    } else {
      throw new RuntimeException("NYI glGetProgramiv");
    }
  }

  @Override public String glGetProgramInfoLog(int program) {
    return glc.getProgramInfoLog(getProgram(program));
  }

  @Override public void glGetProgramBinary(int arg0, int arg1, IntBuffer arg2, IntBuffer arg3, Buffer arg4) {
    throw new RuntimeException("NYI glGetProgramBinary");
  }
  @Override public void glGetProgramInfoLog(int program, int bufsize, IntBuffer length, ByteBuffer infolog) {
    throw new RuntimeException("NYI glGetProgramInfoLog");
  }

  @Override public void glGetProgramiv(int program, int pname, int[] params, int offset) {
    if (pname == GL_LINK_STATUS) params[offset] = glc.getProgramParameterb(getProgram(program), LINK_STATUS) ? GL_TRUE : GL_FALSE;
    else throw new RuntimeException("NYI glGetProgramiv: " + pname);
  }

  @Override public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("NYI glGetRenderbufferParameteriv");
  }

  @Override public void glGetShaderInfoLog(int shader, int bufsize, IntBuffer length, ByteBuffer infolog) {
    throw new RuntimeException("NYI glGetShaderInfoLog");
  }

  @Override public void glGetShaderiv(int shader, int pname, int[] params, int offset) {
    if (pname == GL_COMPILE_STATUS) params[offset] = glc.getShaderParameterb(getShader(shader), COMPILE_STATUS) ? GL_TRUE : GL_FALSE;
    else throw new RuntimeException("NYI glGetShaderiv: " + pname);
  }

  @Override public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, int[] range, int rangeOffset, int[] precision, int precisionOffset) {
    throw new RuntimeException("NYI glGetShaderPrecisionFormat");
  }
  @Override public void glGetShaderSource(int shader, int bufsize, int[] length, int lengthOffset, byte[] source, int sourceOffset) {
    throw new RuntimeException("NYI glGetShaderSource");
  }
  @Override public void glGetShaderSource(int shader, int bufsize, IntBuffer length, ByteBuffer source) {
    throw new RuntimeException("NYI glGetShaderSource");
  }

  @Override public void glGetShaderiv(int shader, int pname, IntBuffer params) {
    if (pname == GL_COMPILE_STATUS) {
      params.put(glc.getShaderParameterb(getShader(shader), COMPILE_STATUS) ? GL_TRUE : GL_FALSE);
    } else {
      throw new RuntimeException("NYI glGetShaderiv: " + pname);
    }
  }

  @Override public String glGetShaderInfoLog(int shader) {
    return glc.getShaderInfoLog(getShader(shader));
  }

  @Override public void glGetShaderPrecisionFormat(int shadertype, int precisiontype,
                                                   IntBuffer range, IntBuffer precision) {
    throw new RuntimeException("NYI glGetShaderInfoLog");
  }

  @Override public String glGetString(int id) {
    return glc.getParameterString(id);
  }

  @Override public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
    params.put(params.position(), glc.getTexParameter(target, pname));
  }
  @Override public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
    params.put(params.position(), glc.getTexParameter(target, pname));
  }

  @Override public void glGetUniformfv(int program, int location, FloatBuffer params) {
    Float32Array v = glc.getUniformv(getProgram(program), getUniformLocation(location));
    for (int i = 0; i < v.length(); i++) {
      params.put(params.position() + i, v.get(i));
    }
  }
  @Override public void glGetUniformiv(int program, int location, IntBuffer params) {
    Int32Array v = glc.getUniformv(getProgram(program), getUniformLocation(location));
    for (int i = 0; i < v.length(); i++) {
      params.put(params.position() + i, v.get(i));
    }
  }

  @Override public int glGetUniformLocation(int program, String name) {
    return createObject(glc.getUniformLocation(getProgram(program), name),
            WebGLObjectType.UNIFORM_LOCATION);
  }

  @Override public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
    Float32Array v = glc.getVertexAttribv(index, pname);
    for (int i = 0; i < v.length(); i++) {
      params.put(params.position() + i, v.get(i));
    }
  }

  @Override public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
    throw new UnsupportedOperationException("NYI glGetVertexAttribiv: WebGL getVertexAttribv always returns a float buffer.");
  }

  @Override public void glHint(int target, int mode) {
    glc.hint(target, mode);
  }

  @Override public boolean glIsBuffer(int buffer) {
    return isObjectType(buffer, WebGLObjectType.BUFFER);
  }

  @Override public boolean glIsEnabled(int cap) {
    return glc.isEnabled(cap);
  }

  @Override public boolean glIsFramebuffer(int framebuffer) {
    return isObjectType(framebuffer, WebGLObjectType.FRAME_BUFFER);
  }

  @Override public boolean glIsShader(int shader) {
    return isObjectType(shader, WebGLObjectType.SHADER);
  }
  @Override public boolean glIsTexture(int texture) {
    return isObjectType(texture, WebGLObjectType.TEXTURE);
  }

  @Override public boolean glIsProgram(int program) {
    return isObjectType(program, WebGLObjectType.PROGRAM);
  }

  @Override public boolean glIsRenderbuffer(int renderbuffer) {
    return isObjectType(renderbuffer, WebGLObjectType.FRAME_BUFFER);
  }
  @Override public boolean glIsVBOArrayEnabled() {
    throw new RuntimeException("NYI glIsVBOArrayEnabled");
  }

  @Override public boolean glIsVBOElementEnabled() {
    throw new RuntimeException("NYI glIsVBOElementEnabled");
  }

  @Override public void glLineWidth(float width) {
    glc.lineWidth(width);
  }

  @Override public void glLinkProgram(int program) {
    glc.linkProgram(getProgram(program));
  }

  @Override public ByteBuffer glMapBuffer(int arg0, int arg1) {
    throw new RuntimeException("NYI glMapBuffer");
  }

  @Override public void glPixelStorei(int i, int j) {
    glc.pixelStorei(i, j);
  }

  @Override public String getPlatformGLExtensions() {
    throw new RuntimeException("NYI getPlatformGLExtensions");
  }

  @Override public void glPolygonOffset(float factor, float units) {
    glc.polygonOffset(factor, units);
  }

  @Override public void glProgramBinary(int arg0, int arg1, Buffer arg2, int arg3) {
    throw new RuntimeException("NYI glProgramBinary");
  }

  @Override public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
    glc.readPixels(x, y, width, height, format, type, getTypedArray(pixels, type, -1));
  }

  @Override public void glReadPixels(int x, int y, int width, int height, int format, int type, int pixelsBufferOffset) {
    throw new RuntimeException("NYI glReadPixels");
  }

  @Override public void glReleaseShaderCompiler() {
    throw new RuntimeException("NYI glReleaseShaderCompiler");
  }

  @Override public void glRenderbufferStorage(int target, int internalformat, int width,
                                              int height) {
    glc.renderbufferStorage(target, internalformat, width, height);
  }

  @Override public void glSampleCoverage(float value, boolean invert) {
    glc.sampleCoverage(value, invert);
  }

  @Override public final void glScissor(int i, int j, int width, int height) {
    glc.scissor(i, j, width, height);
  }

  @Override public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
    throw new RuntimeException("NYI glReleaseShaderCompiler");
  }

  @Override public void glShaderBinary(int n, int[] shaders, int offset, int binaryformat, Buffer binary, int length) {
    throw new RuntimeException("NYI glShaderBinary");
  }

  @Override public void glShaderSource(int shader, int count, String[] strings, IntBuffer length) {
    throw new RuntimeException("NYI glShaderSource");
  }

  @Override public void glShaderSource(int shader, int count, String[] strings, int[] length, int lengthOffset) {
    throw new RuntimeException("NYI glShaderSource");
  }

  @Override public void glShaderSource(int shader, String string) {
    glc.shaderSource(getShader(shader), string);
  }

  @Override public void glStencilFunc(int func, int ref, int mask) {
    glc.stencilFunc(func, ref, mask);
  }

  @Override public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
    glc.stencilFuncSeparate(face, func, ref, mask);
  }

  @Override public void glStencilMask(int mask) {
    glc.stencilMask(mask);
  }

  @Override public void glStencilMaskSeparate(int face, int mask) {
    glc.stencilMaskSeparate(face, mask);
  }

  @Override public void glStencilOp(int fail, int zfail, int zpass) {
    glc.stencilOp(fail, zfail, zpass);
  }

  @Override public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
    glc.stencilOpSeparate(face, fail, zfail, zpass);
  }

  @Override public void glTexImage2D(int target, int level, int internalformat, int width,
                                     int height, int border, int format, int type, Buffer pixels) {
    ArrayBufferView buffer = (pixels == null) ? null : getTypedArray(pixels, type, -1);
    glc.texImage2D(target, level, internalformat, width, height, border, format, type, buffer);
  }

  @Override public void glTexImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI glTexImage2D");
  }

  public void glTexImage2D(int target, int level, int internalformat, int format, int type, ImageElement image) {
    glc.texImage2D(target, level, internalformat, format, type, image);
    checkError("texImage2D");
  }

  public void glTexImage2D(int target, int level, int internalformat, int format, int type, CanvasElement image) {
    glc.texImage2D(target, level, internalformat, format, type, image);
    checkError("texImage2D");
  }

  @Override public void glTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, Buffer arg9) {
    throw new RuntimeException("NYI glTexImage3D");
  }

  @Override public void glTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9) {
    throw new RuntimeException("NYI glTexImage3D");
  }

  @Override public void glTexParameteri(int glTexture2d, int glTextureMinFilter, int glFilterMin) {
    glc.texParameteri(glTexture2d, glTextureMinFilter, glFilterMin);
  }

  @Override public void glTexParameterf(int target, int pname, float param) {
    glc.texParameterf(target, pname, param);
  }

  @Override public void glTexParameterfv(int target, int pname, FloatBuffer params) {
    throw new RuntimeException("NYI glTexParameterfv");
  }

  @Override public void glTexParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("NYI glTexParameteriv");
  }

  @Override public void glTexSubImage2D(int target, int level, int xoffset, int yoffset,
                                        int width, int height, int format, int type, Buffer pixels) {
    glc.texSubImage2D(target, level, xoffset, yoffset, width, height, format, type,
            getTypedArray(pixels, type, -1));
  }

  @Override public void glTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI glTexSubImage2D");
  }

  @Override public void glTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, Buffer arg10) {
    throw new RuntimeException("NYI glTexSubImage3D");
  }

  @Override public void glTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10) {
    throw new RuntimeException("NYI glTexSubImage3D");
  }

  @Override public boolean glUnmapBuffer(int arg0) {
    throw new RuntimeException("NYI glUnmapBuffer");
  }

  @Override public void glUniform1f(int location, float x) {
    glc.uniform1f(getUniformLocation(location), x);
  }

  @Override public void glUniform1fv(int location, int count, FloatBuffer v) {
    throw new RuntimeException("NYI glUniform1fv");
  }

  @Override public void glUniform1i(int location, int x) {
    glc.uniform1i(getUniformLocation(location), x);
  }

  @Override public void glUniform1iv(int location, int count, IntBuffer v) {
    glc.uniform1iv(getUniformLocation(location), (Int32Array) getTypedArray(v, GL_INT, count * 4));
  }

  @Override public void glUniform2f(int location, float x, float y) {
    glc.uniform2f(getUniformLocation(location), x, y);
  }

  @Override public void glUniform2fv(int location, int count, FloatBuffer v) {
    glc.uniform2fv(getUniformLocation(location),
            (Float32Array) getTypedArray(v, GL_FLOAT, count * 2 * 4));
  }

  @Override public void glUniform2i(int location, int x, int y) {
    glc.uniform2i(getUniformLocation(location), x, y);
  }

  @Override public void glUniform2iv(int location, int count, IntBuffer v) {
    throw new RuntimeException("NYI glUniform2iv");
  }

  @Override public void glUniform3f(int location, float x, float y, float z) {
    glc.uniform3f(getUniformLocation(location), x, y, z);
  }

  @Override public void glUniform3fv(int location, int count, FloatBuffer v) {
    throw new RuntimeException("NYI glUniform3fv");
  }

  @Override public void glUniform3i(int location, int x, int y, int z) {
    glc.uniform3i(getUniformLocation(location), x, y, z);
  }

  @Override public void glUniform3iv(int location, int count, IntBuffer v) {
    throw new RuntimeException("NYI glUniform3fi");
  }

  @Override public void glUniform4f(int location, float x, float y, float z, float w) {
    glc.uniform4f(getUniformLocation(location), x, y, z, w);
  }

  @Override public void glUniform4fv(int location, int count, FloatBuffer v) {
    glc.uniform4fv(getUniformLocation(location),
            (Float32Array) getTypedArray(v, GL_FLOAT, 4 * 4 * count));
  }

  @Override public void glUniform4i(int location, int x, int y, int z, int w) {
    glc.uniform4i(getUniformLocation(location), x, y, z, w);
  }

  @Override public void glUniform4iv(int location, int count, IntBuffer v) {
    throw new RuntimeException("NYI glUniform4iv");
  }

  @Override public void glUniformMatrix2fv(int location, int count, boolean transpose,
                                           FloatBuffer value) {
    throw new RuntimeException("NYI glUniformMatrix2fv");
  }

  @Override public void glUniformMatrix3fv(int location, int count, boolean transpose,
                                           FloatBuffer value) {
    throw new RuntimeException("NYI glUniformMatrix3fv");
  }

  @Override public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value) {
    glc.uniformMatrix4fv(getUniformLocation(location), transpose,
            (Float32Array) getTypedArray(value, GL_FLOAT, count * 16 * 4));
  }

  @Override public void glUseProgram(int program) {
    glc.useProgram(getProgram(program));
  }

  @Override public void glValidateProgram(int program) {
    glc.validateProgram(getProgram(program));
  }

  @Override public void glVertexAttrib1f(int indx, float x) {
    glc.vertexAttrib1f(indx, x);
  }

  @Override public void glVertexAttrib1fv(int indx, FloatBuffer values) {
    throw new RuntimeException("NYI glVertexAttrib1fv");
  }
  @Override public void glVertexAttrib2f(int indx, float x, float y) {
    glc.vertexAttrib2f(indx, x, y);
  }

  @Override public void glVertexAttrib2fv(int indx, FloatBuffer values) {
    throw new RuntimeException("NYI glVertexAttrib2fv");
  }

  @Override public void glVertexAttrib3f(int indx, float x, float y, float z) {
    glc.vertexAttrib3f(indx, x, y, z);
  }

  @Override public void glVertexAttrib3fv(int indx, FloatBuffer values) {
    throw new RuntimeException("NYI glVertexAttrib3fv");
  }

  @Override public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
    glc.vertexAttrib4f(indx, x, y, z, w);
  }

  @Override public void glVertexAttrib4fv(int indx, FloatBuffer values) {
    throw new RuntimeException("NYI glVertexAttrib4fv");
  }

  // arrayId (index) is in the range 0..GL_MAX_VERTEX_ATTRIBS-1
  @Override public void glVertexAttribPointer(int arrayId, int size, int type, boolean normalize,
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

  @Override public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr) {
    useNioBuffer &= ~(1 << indx);
    if (boundArrayBuffer != requestedArrayBuffer) {
      glc.bindBuffer(GL_ARRAY_BUFFER, requestedArrayBuffer);
      boundArrayBuffer = requestedArrayBuffer;
    }

    glc.vertexAttribPointer(indx, size, type, normalized, stride, ptr);
  }

  @Override public void glViewport(int x, int y, int w, int h) {
    glc.viewport(x, y, w, h);
//    checkError("glViewport");
  }

  @Override public boolean isExtensionAvailable(String extension) {
    throw new RuntimeException("NYI isExtensionAvailable");
  }
  @Override public boolean isFunctionAvailable(String function) {
    throw new RuntimeException("NYI isFunctionAvailable");
  }

  @Override public boolean hasGLSL() {
    return true;
  }
}
