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

import static com.google.gwt.webgl.client.WebGLRenderingContext.ARRAY_BUFFER;
import static com.google.gwt.webgl.client.WebGLRenderingContext.BYTE;
import static com.google.gwt.webgl.client.WebGLRenderingContext.ELEMENT_ARRAY_BUFFER;
import static com.google.gwt.webgl.client.WebGLRenderingContext.FLOAT;
import static com.google.gwt.webgl.client.WebGLRenderingContext.INT;
import static com.google.gwt.webgl.client.WebGLRenderingContext.SHORT;
import static com.google.gwt.webgl.client.WebGLRenderingContext.STREAM_DRAW;
import static com.google.gwt.webgl.client.WebGLRenderingContext.UNSIGNED_BYTE;
import static com.google.gwt.webgl.client.WebGLRenderingContext.UNSIGNED_SHORT;


import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import playn.core.util.Buffers;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.typedarrays.client.ArrayBufferView;
import com.google.gwt.typedarrays.client.Float32Array;
import com.google.gwt.typedarrays.client.Int16Array;
import com.google.gwt.typedarrays.client.Int32Array;
import com.google.gwt.typedarrays.client.Int8Array;
import com.google.gwt.typedarrays.client.Uint16Array;
import com.google.gwt.typedarrays.client.Uint8Array;
import com.google.gwt.webgl.client.WebGLBuffer;
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLObject;
import com.google.gwt.webgl.client.WebGLProgram;
import com.google.gwt.webgl.client.WebGLRenderbuffer;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLShader;
import com.google.gwt.webgl.client.WebGLTexture;
import com.google.gwt.webgl.client.WebGLUniformLocation;

/**
 * WebGL implementation of GL20. Maintains a set of VBOs to translate the NIO buffer based version
 * of glVertexAttribPointer to the VBO based version.
 *
 * @author Stefan Haustein
 */
public final class HtmlGL20 implements playn.core.gl.GL20 {

  static final int VERTEX_ATTRIB_ARRAY_COUNT = 5; //  position, color, texture0, texture1, normals

  enum WebGLObjectType {
    NULL, BUFFER, FRAME_BUFFER, PROGRAM, RENDER_BUFFER, SHADER, TEXTURE, UNIFORM_LOCATION,
  }

  private int previouslyEnabledArrays = 0;
  private int enabledArrays = 0;
  private int useNioBuffer = 0;

  private final WebGLRenderingContext gl;
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

  HtmlGL20(WebGLRenderingContext gl) {
    this.gl = gl;

    webGLObjects.push(null);
    webGLObjectTypes.push(WebGLObjectType.NULL.ordinal());

    elementBuffer = gl.createBuffer();

    for (int i = 0; i < VERTEX_ATTRIB_ARRAY_COUNT; i++) {
      VertexAttribArrayState data = new VertexAttribArrayState();
      data.webGlBuffer = gl.createBuffer();
      vertexAttribArrayState[i] = data;
    }
  }

  protected boolean isObjectType(int index,  WebGLObjectType type) {
    return webGLObjectTypes.get(index) == type.ordinal();
  }

  private WebGLBuffer getBuffer(int index) {
    return (WebGLBuffer) webGLObjects.get(index);
  }

  private WebGLFramebuffer getFramebuffer(int index) {
    return (WebGLFramebuffer) webGLObjects.get(index);
  }

  private WebGLProgram getProgram(int index) {
    return (WebGLProgram) webGLObjects.get(index);
  }

  private WebGLRenderbuffer getRenderbuffer(int index) {
    return (WebGLRenderbuffer) webGLObjects.get(index);
  }

  private WebGLShader getShader(int index) {
    return (WebGLShader) webGLObjects.get(index);
  }

  protected WebGLUniformLocation getUniformLocation(int index) {
    return (WebGLUniformLocation) webGLObjects.get(index);
  }

  protected void deleteObject(int index, WebGLObjectType type) {
    WebGLObject object = webGLObjects.get(index);
    webGLObjects.set(index, null);
    webGLObjectTypes.set(index, WebGLObjectType.NULL.ordinal());
    switch(type) {
    case BUFFER:
      gl.deleteBuffer((WebGLBuffer) object);
      break;
    case FRAME_BUFFER:
      gl.deleteFramebuffer((WebGLFramebuffer) object);
      break;
    case PROGRAM:
      gl.deleteProgram((WebGLProgram) object);
      break;
    case RENDER_BUFFER:
      gl.deleteRenderbuffer((WebGLRenderbuffer) object);
      break;
    case SHADER:
      gl.deleteShader((WebGLShader) object);
      break;
    case TEXTURE:
      gl.deleteTexture((WebGLTexture) object);
      break;
    }
  }

  protected void deleteObjects(int count, IntBuffer indices, WebGLObjectType type) {
    for (int i = 0; i < count; i++) {
      int index = indices.get(indices.position() + i);
      deleteObject(index, type);
    }
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

  protected WebGLObject genObject(WebGLObjectType type) {
    switch(type) {
    case BUFFER:
      return gl.createBuffer();
    case FRAME_BUFFER:
      return gl.createFramebuffer();
    case PROGRAM:
      return gl.createProgram();
    case RENDER_BUFFER:
      return gl.createRenderbuffer();
    case TEXTURE:
      return gl.createTexture();
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
          int elementSize = Buffers.getElementSize(data.nioBuffer);
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

  /**
   * Returns the typed array of the given native buffer.
   * Set byteSize to -1 to use remaining()
   */
  protected ArrayBufferView getTypedArray(Buffer buffer, int type, int byteSize) {
    if (!(buffer instanceof HasArrayBufferView)) {
      throw new RuntimeException("Native buffer required.");
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

    int byteOffset = webGLArray.getByteOffset() + buffer.position() * bufferElementSize;

    switch (type) {
    case FLOAT:
      return Float32Array.create(webGLArray.getBuffer(), byteOffset, byteSize / 4);
    case UNSIGNED_BYTE:
      return Uint8Array.create(webGLArray.getBuffer(), byteOffset, byteSize);
    case UNSIGNED_SHORT:
      return Uint16Array.create(webGLArray.getBuffer(), byteOffset, byteSize / 2);
    case INT:
      return Int32Array.create(webGLArray.getBuffer(), byteOffset, byteSize / 4);
    case SHORT:
      return Int16Array.create(webGLArray.getBuffer(), byteOffset, byteSize / 2);
    case BYTE:
      return Int8Array.create(webGLArray.getBuffer(), byteOffset, byteSize);
    default:
      throw new IllegalArgumentException("Type: " + type);
    }

  }

  @Override
  public void glDeleteTextures(int n, IntBuffer texnumBuffer) {
    deleteObjects(n, texnumBuffer, WebGLObjectType.TEXTURE);
  }

  @Override
  public void glDepthFunc(int func) {
    gl.depthFunc(func);
  }

  @Override
  public void glDepthMask(boolean b) {
    gl.depthMask(b);
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

  @Override
  public void glFinish() {
//    HtmlPlatform.log.info("glFinish()");
    gl.finish();
  }

  @Override
  public String glGetString(int id) {
    // TODO: Where is getParameter()?
    // String s = gl.getParameter(id);
    //return s == null ? "" : s;
    return "glGetString not implemented";
  }

  @Override
  public void glPixelStorei(int i, int j) {
    gl.pixelStorei(i, j);
  }

  @Override
  public void glTexParameteri(int glTexture2d, int glTextureMinFilter, int glFilterMin) {
    gl.texParameteri(glTexture2d, glTextureMinFilter, glFilterMin);
  }

  @Override
  public void glBindTexture(int target, int textureId) {
    gl.bindTexture(target, (WebGLTexture) webGLObjects.get(textureId));
  }

  @Override
  public final void glBlendFunc(int a, int b) {
    gl.blendFunc(a, b);
  }

  @Override
  public final void glClear(int mask) {
    gl.clear(mask);
  }


  @Override
  public final int glGetError() {
    return gl.getError();
  }

  @Override
  public final void glClearColor(float f, float g, float h, float i) {
    gl.clearColor(f, g, h, i);
  }

  @Override
  public void glDrawArrays(int mode, int first, int count) {
    prepareDraw();
    gl.drawArrays(mode, first, count);
  }

  @Override
  public final void glScissor(int i, int j, int width, int height) {
    gl.scissor(i, j, width, height);
  }

  @Override
  public void glTexParameterf(int target, int pname, float param) {
    gl.texParameterf(target, pname, param);
  }

  @Override
  public final void glCullFace(int c) {
    gl.cullFace(c);
  }

  @Override
  public void glViewport(int x, int y, int w, int h) {
    gl.viewport(x, y, w, h);
//    checkError("glViewport");
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

  public void glGenerateMipmap(int t) {
    gl.generateMipmap(t);
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
  public void glCompressedTexImage2D(int target, int level, int internalformat,
                                     int width, int height, int border, int imageSize, Buffer data) {
    throw new RuntimeException("NYI glCompressedTexImage2D");
  }

  @Override
  public void glCompressedTexSubImage2D(int target, int level, int xoffset,
                                        int yoffset, int width, int height, int format, int imageSize,
                                        Buffer data) {
    throw new RuntimeException("NYI glCompressedTexSubImage2D");
  }

  @Override
  public void glCopyTexImage2D(int target, int level, int internalformat, int x,
                               int y, int width, int height, int border) {
    gl.copyTexImage2D(target, level, internalformat, x, y, width, height, border);
  }

  @Override
  public void glCopyTexSubImage2D(int target, int level, int xoffset,
                                  int yoffset, int x, int y, int width, int height) {
    gl.copyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
  }

  @Override
  public void glDepthRangef(float zNear, float zFar) {
    gl.depthRange(zNear, zFar);
  }

  @Override
  public void glFlush() {
//    HtmlPlatform.log.info("glFlush");
    gl.flush();
  }

  @Override
  public void glFrontFace(int mode) {
    gl.frontFace(mode);
  }

  @Override
  public void glGenTextures(int n, IntBuffer textures) {
    genObjects(n, textures, WebGLObjectType.TEXTURE);
  }

  @Override
  public void glGetIntegerv(int pname, IntBuffer params) {
    Int32Array result = (Int32Array) gl.getParameterv(pname);
    int pos = params.position();
    int len = result.getLength();
    for (int i = 0; i < len; i++) {
      params.put(pos + i, result.get(i));
    }
  }

  @Override
  public void glHint(int target, int mode) {
    gl.hint(target, mode);
  }

  @Override
  public void glLineWidth(float width) {
    gl.lineWidth(width);
  }

  @Override
  public void glPolygonOffset(float factor, float units) {
    gl.polygonOffset(factor, units);
  }

  @Override
  public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
    gl.readPixels(x, y, width, height, format, type, getTypedArray(pixels, type, -1));
  }

  @Override
  public void glStencilFunc(int func, int ref, int mask) {
    gl.stencilFunc(func, ref, mask);
  }

  @Override
  public void glStencilMask(int mask) {
    gl.stencilMask(mask);
  }

  @Override
  public void glStencilOp(int fail, int zfail, int zpass) {
    gl.stencilOp(fail, zfail, zpass);
  }

  /*
    public void glTexImage2d(int target, int level, int internalformat,
    int format, int type, ImageElement image) {
    // log("setting texImage2d; image: " + image.getSrc());
    gl.texImage2D(target, level, internalformat, format, type, image);
    checkError("texImage2D");
    }

    public void glTexImage2d(int target, int level, int internalformat,
    int format, int type, CanvasElement image) {
    // log("setting texImage2d; image: " + image.getSrc());
    gl.texImage2D(target, level, internalformat, format, type, image);
    checkError("texImage2D");
    }
  */

  @Override
  public void glTexImage2D(int target, int level, int internalformat, int width,
                           int height, int border, int format, int type, Buffer pixels) {
    gl.texImage2D(target, level, internalformat,
                  width, height, border, format, type, getTypedArray(pixels, type, -1));
  }

//  @Override
//  public void glTexImage2D(int target, int level, int internalformat, int format, int type, Image image) {
//      gl.texImage2D(target, level, internalformat, format, type, ((HtmlImage) image).img);
//  }

  @Override
  public void glTexSubImage2D(int target, int level, int xoffset, int yoffset,
                              int width, int height, int format, int type, Buffer pixels) {
    gl.texSubImage2D(target, level, xoffset, yoffset, width, height, format, type,
                     getTypedArray(pixels, type, -1));
  }

  @Override
  public void glAttachShader(int program, int shader) {
    gl.attachShader((WebGLProgram) webGLObjects.get(program),
                    (WebGLShader) webGLObjects.get(shader));
  }

  @Override
  public void glBindAttribLocation(int program, int index, String name) {
    gl.bindAttribLocation((WebGLProgram) webGLObjects.get(program), index, name);
  }

  @Override
  public void glBindBuffer(int target, int buffer) {
    // Yes, bindBuffer is so expensive that this makes sense..
    WebGLBuffer webGlBuf = getBuffer(buffer);
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
    gl.bindFramebuffer(target, getFramebuffer(framebuffer));
  }

  @Override
  public void glBindRenderbuffer(int target, int renderbuffer) {
    gl.bindRenderbuffer(target, getRenderbuffer(renderbuffer));
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
  public void glCompileShader(int shader) {
    gl.compileShader(getShader(shader));
  }

  @Override
  public int glCreateProgram() {
    return createObject(gl.createProgram(), WebGLObjectType.PROGRAM);
  }

  @Override
  public int glCreateShader(int type) {
    return createObject(gl.createShader(type), WebGLObjectType.SHADER);
  }

  @Override
  public void glDeleteBuffers(int n, IntBuffer buffers) {
    deleteObjects(n, buffers, WebGLObjectType.BUFFER);
  }

  @Override
  public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
    deleteObjects(n, framebuffers, WebGLObjectType.FRAME_BUFFER);
  }

  @Override
  public void glDeleteProgram(int program) {
    deleteObject(program, WebGLObjectType.PROGRAM);
  }

  @Override
  public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
    deleteObjects(n, renderbuffers, WebGLObjectType.RENDER_BUFFER);
  }

  @Override
  public void glDeleteShader(int shader) {
    deleteObject(shader, WebGLObjectType.SHADER);
  }

  @Override
  public void glDetachShader(int program, int shader) {
    gl.detachShader(getProgram(program), getShader(shader));
  }

  @Override
  public void glDisableVertexAttribArray(int index) {
    enabledArrays &= ~(1 << index);
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
  public void glEnableVertexAttribArray(int index) {
    enabledArrays |= (1 << index);
  }

  @Override
  public void glFramebufferRenderbuffer(int target, int attachment,
                                        int renderbuffertarget, int renderbuffer) {
    gl.framebufferRenderbuffer(target, attachment, renderbuffertarget,
                               getRenderbuffer(renderbuffer));
  }

  @Override
  public void glFramebufferTexture2D(int target, int attachment, int textarget,
                                     int texture, int level) {
    glFramebufferTexture2D(target, attachment, textarget, texture, level);
  }

  @Override
  public void glGenBuffers(int n, IntBuffer buffers) {
    genObjects(n, buffers, WebGLObjectType.BUFFER);
  }

  @Override
  public void glGenFramebuffers(int n, IntBuffer framebuffers) {
    genObjects(n, framebuffers, WebGLObjectType.FRAME_BUFFER);
  }

  @Override
  public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
    genObjects(n, renderbuffers, WebGLObjectType.RENDER_BUFFER);
  }

  @Override
  public int glGetAttribLocation(int program, String name) {
    return gl.getAttribLocation(getProgram(program), name);
  }

  @Override
  public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("NYI glGetBufferParameteriv");
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
    throw new RuntimeException("NYI glGetProgramiv");
  }

  @Override
  public String glGetProgramInfoLog(int program) {
    return gl.getProgramInfoLog(getProgram(program));
  }

  @Override
  public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("NYI glGetRenderbufferParameteriv");
  }

  @Override
  public void glGetShaderiv(int shader, int pname, IntBuffer params) {
    throw new RuntimeException("NYI glGetShaderiv");
  }

  @Override
  public String glGetShaderInfoLog(int shader) {
    return gl.getShaderInfoLog(getShader(shader));
  }

  @Override
  public void glGetShaderPrecisionFormat(int shadertype, int precisiontype,
                                         IntBuffer range, IntBuffer precision) {
    throw new RuntimeException("NYI glGetShaderInfoLog");
  }

  @Override
  public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
    throw new RuntimeException("NYI glGetTexParameterfv");
  }

  @Override
  public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("NYI glGetTexParameteriv");
  }

  @Override
  public void glGetUniformfv(int program, int location, FloatBuffer params) {
    throw new RuntimeException("NYI glGetUniformfv");
  }

  @Override
  public void glGetUniformiv(int program, int location, IntBuffer params) {
    throw new RuntimeException("NYI glGetUniformiv");
  }

  @Override
  public int glGetUniformLocation(int program, String name) {
    return createObject(gl.getUniformLocation(getProgram(program), name),
                        WebGLObjectType.UNIFORM_LOCATION);
  }

  @Override
  public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
    throw new RuntimeException("NYI glGetVertexAttribfv");
  }

  @Override
  public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
    throw new RuntimeException("NYI glGetVertexAttribiv");
  }

  @Override
  public boolean glIsBuffer(int buffer) {
    return isObjectType(buffer, WebGLObjectType.BUFFER);
  }

  @Override
  public boolean glIsEnabled(int cap) {
    return gl.isEnabled(cap);
  }

  @Override
  public boolean glIsFramebuffer(int framebuffer) {
    return isObjectType(framebuffer, WebGLObjectType.FRAME_BUFFER);
  }

  @Override
  public boolean glIsProgram(int program) {
    return isObjectType(program, WebGLObjectType.PROGRAM);
  }

  @Override
  public boolean glIsRenderbuffer(int renderbuffer) {
    return isObjectType(renderbuffer, WebGLObjectType.FRAME_BUFFER);
  }

  @Override
  public boolean glIsShader(int shader) {
    return isObjectType(shader, WebGLObjectType.SHADER);
  }

  @Override
  public boolean glIsTexture(int texture) {
    return isObjectType(texture, WebGLObjectType.TEXTURE);
  }

  @Override
  public void glLinkProgram(int program) {
    gl.linkProgram(getProgram(program));
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
  public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
    throw new RuntimeException("NYI glReleaseShaderCompiler");
  }

  @Override
  public void glShaderSource(int shader, String string) {
    gl.shaderSource(getShader(shader), string);
  }

  @Override
  public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
    gl.stencilFuncSeparate(face, func, ref, mask);
  }

  @Override
  public void glStencilMaskSeparate(int face, int mask) {
    gl.stencilMaskSeparate(face, mask);
  }

  @Override
  public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
    gl.stencilOpSeparate(face, fail, zfail, zpass);
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
    gl.useProgram(getProgram(program));
  }

  @Override
  public void glValidateProgram(int program) {
    gl.validateProgram(getProgram(program));
  }

  @Override
  public void glVertexAttrib1f(int indx, float x) {
    gl.vertexAttrib1f(indx, x);
  }

  @Override
  public void glVertexAttrib1fv(int indx, FloatBuffer values) {
    throw new RuntimeException("NYI glVertexAttrib1fv");
  }

  @Override
  public void glVertexAttrib2f(int indx, float x, float y) {
    gl.vertexAttrib2f(indx, x, y);
  }

  @Override
  public void glVertexAttrib2fv(int indx, FloatBuffer values) {
    throw new RuntimeException("NYI glVertexAttrib2fv");
  }

  @Override
  public void glVertexAttrib3f(int indx, float x, float y, float z) {
    gl.vertexAttrib3f(indx, x, y, z);
  }

  @Override
  public void glVertexAttrib3fv(int indx, FloatBuffer values) {
    throw new RuntimeException("NYI glVertexAttrib3fv");
  }

  @Override
  public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
    gl.vertexAttrib4f(indx, x, y, z, w);
  }

  @Override
  public void glVertexAttrib4fv(int indx, FloatBuffer values) {
    throw new RuntimeException("NYI glVertexAttrib4fv");
  }

  @Override
  public void glVertexAttribPointer(int indx, int size, int type, boolean normalized,
                                    int stride, int ptr) {
    useNioBuffer &= ~(1 << indx);
    if (boundArrayBuffer != requestedArrayBuffer) {
      gl.bindBuffer(GL_ARRAY_BUFFER, requestedArrayBuffer);
      boundArrayBuffer = requestedArrayBuffer;
    }

    gl.vertexAttribPointer(indx, size, type, normalized, stride, ptr);
  }

  @Override
  public void glDisable(int cap) {
    gl.disable(cap);
  }


  @Override
  public void glEnable(int cap) {
    gl.enable(cap);
  }

  @Override
  public void glActiveTexture(int texture) {
    gl.activeTexture(texture);
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

  @Override
  public String getPlatformGLExtensions() {
    throw new RuntimeException("NYI");
  }

  @Override
  public int getSwapInterval() {
    throw new RuntimeException("NYI");
  }


  @Override
  public void glClearDepth(double depth) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glCompressedTexImage3D(int arg0, int arg1, int arg2, int arg3,
                                     int arg4, int arg5, int arg6, int arg7, Buffer arg8) {
    throw new RuntimeException("NYI");

  }

  @Override
  public void glCompressedTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
                                        int arg6, int arg7, int arg8, int arg9, Buffer arg10) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glCopyTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
                                  int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glDeleteBuffers(int n, int[] buffers, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glDeleteFramebuffers(int n, int[] framebuffers, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glDeleteRenderbuffers(int n, int[] renderbuffers, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glDeleteTextures(int n, int[] textures, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glDepthRange(double zNear, double zFar) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glFramebufferTexture3D(int target, int attachment, int textarget, int texture,
                                     int level, int zoffset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGenBuffers(int n, int[] buffers, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGenFramebuffers(int n, int[] framebuffers, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGenRenderbuffers(int n, int[] renderbuffers, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGenTextures(int n, int[] textures, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetActiveAttrib(int program, int index, int bufsize, int[] length, int lengthOffset,
                                int[] size, int sizeOffset, int[] type, int typeOffset,
                                byte[] name, int nameOffset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetActiveAttrib(int program, int index, int bufsize, IntBuffer length, IntBuffer size,
                                IntBuffer type, ByteBuffer name) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetActiveUniform(int program, int index, int bufsize, int[] length, int lengthOffset,
                                 int[] size, int sizeOffset, int[] type, int typeOffset,
                                 byte[] name, int nameOffset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetActiveUniform(int program, int index, int bufsize, IntBuffer length,
                                 IntBuffer size, IntBuffer type, ByteBuffer name) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetAttachedShaders(int program, int maxcount, int[] count, int countOffset,
                                   int[] shaders, int shadersOffset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetAttachedShaders(int program, int maxcount, IntBuffer count, IntBuffer shaders) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetBooleanv(int pname, byte[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetBooleanv(int pname, ByteBuffer params) {
    throw new RuntimeException("NYI");
  }

  @Override
  public int glGetBoundBuffer(int arg0) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetBufferParameteriv(int target, int pname, int[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetFloatv(int pname, float[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname,
                                                    int[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetIntegerv(int pname, int[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetProgramBinary(int arg0, int arg1, int[] arg2, int arg3, int[] arg4, int arg5,
                                 Buffer arg6) {
    throw new RuntimeException("NYI");

  }

  @Override
  public void glGetProgramBinary(int arg0, int arg1, IntBuffer arg2, IntBuffer arg3, Buffer arg4) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetProgramInfoLog(int program, int bufsize, int[] length, int lengthOffset,
                                  byte[] infolog, int infologOffset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetProgramInfoLog(int program, int bufsize, IntBuffer length, ByteBuffer infolog) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetProgramiv(int program, int pname, int[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetRenderbufferParameteriv(int target, int pname, int[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetShaderInfoLog(int shader, int bufsize, int[] length, int lengthOffset,
                                 byte[] infolog, int infologOffset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetShaderInfoLog(int shader, int bufsize, IntBuffer length, ByteBuffer infolog) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetShaderiv(int shader, int pname, int[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, int[] range,
                                         int rangeOffset, int[] precision, int precisionOffset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetShaderSource(int shader, int bufsize, int[] length, int lengthOffset,
                                byte[] source, int sourceOffset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetShaderSource(int shader, int bufsize, IntBuffer length, ByteBuffer source) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetTexParameterfv(int target, int pname, float[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetTexParameteriv(int target, int pname, int[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetUniformfv(int program, int location, float[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetUniformiv(int program, int location, int[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetVertexAttribfv(int index, int pname, float[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glGetVertexAttribiv(int index, int pname, int[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public boolean glIsVBOArrayEnabled() {
    throw new RuntimeException("NYI");
  }

  @Override
  public boolean glIsVBOElementEnabled() {
    throw new RuntimeException("NYI");
  }

  @Override
  public ByteBuffer glMapBuffer(int arg0, int arg1) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glProgramBinary(int arg0, int arg1, Buffer arg2, int arg3) {
    throw new RuntimeException("NYI");
  }


  @Override
  public void glShaderBinary(int n, int[] shaders, int offset, int binaryformat, Buffer binary,
                             int length) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glShaderSource(int shader, int count, String[] strings, int[] length,
                             int lengthOffset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glShaderSource(int shader, int count, String[] strings, IntBuffer length) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4,
                           int arg5, int arg6, int arg7, int arg8, Buffer arg9) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glTexParameterfv(int target, int pname, float[] params, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glTexParameteriv(int target, int pname, int[] params, int offset) {
    throw new RuntimeException("NYI");
  }


  @Override
  public void glTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
                              int arg6, int arg7, int arg8, int arg9, Buffer arg10) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glUniform1fv(int location, int count, float[] v, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glUniform1iv(int location, int count, int[] v, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glUniform2fv(int location, int count, float[] v, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glUniform2iv(int location, int count, int[] v, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glUniform3fv(int location, int count, float[] v, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glUniform3iv(int location, int count, int[] v, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glUniform4fv(int location, int count, float[] v, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glUniform4iv(int location, int count, int[] v, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value,
                                 int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value,
                                 int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value,
                                 int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public boolean glUnmapBuffer(int arg0) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glVertexAttrib1fv(int indx, float[] values, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glVertexAttrib2fv(int indx, float[] values, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glVertexAttrib3fv(int indx, float[] values, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glVertexAttrib4fv(int indx, float[] values, int offset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public boolean hasGLSL() {
    throw new RuntimeException("NYI");
  }

  @Override
  public boolean isExtensionAvailable(String extension) {
    throw new RuntimeException("NYI");
  }

  @Override
  public boolean isFunctionAvailable(String function) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glCompressedTexImage2D(int arg0, int arg1, int arg2, int arg3,
                                     int arg4, int arg5, int arg6, int arg7) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glCompressedTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
                                     int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glCompressedTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
                                        int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glCompressedTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
                                        int arg6, int arg7, int arg8, int arg9, int arg10) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glReadPixels(int x, int y, int width, int height, int format, int type,
                           int pixelsBufferOffset) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glTexImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6,
                           int arg7, int arg8) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4,
                           int arg5, int arg6, int arg7, int arg8, int arg9) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4,
                              int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void glTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
                              int arg6, int arg7, int arg8, int arg9, int arg10) {
    throw new RuntimeException("NYI");
  }
}
