/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.robovm.rt.bro.Struct;
import org.robovm.rt.bro.ptr.BytePtr;
import playn.core.gl.AbstractGL20;

public class RoboGL20 extends AbstractGL20 {

  public RoboGL20() {
    super(new Buffers() {
      public ByteBuffer createByteBuffer(int size) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
      }
    });
  }

  public String getPlatformGLExtensions() {
    throw new RuntimeException("Not implemented");
  }
  public int getSwapInterval() {
    throw new RuntimeException("Not implemented");
  }
  public void glActiveTexture(int texture) {
    OpenGLES.glActiveTexture(texture);
  }
  public void glAttachShader(int program, int shader) {
    OpenGLES.glAttachShader(program, shader);
  }
  public void glBindAttribLocation(int program, int index, String name) {
    throw new RuntimeException("Not implemented");
  }
  public void glBindBuffer(int target, int buffer) {
    OpenGLES.glBindBuffer(target, buffer);
  }
  public void glBindFramebuffer(int target, int frameBuffer) {
    OpenGLES.glBindFramebuffer(target, frameBuffer);
  }
  public void glBindRenderbuffer(int target, int renderBuffer) {
    throw new RuntimeException("Not implemented");
  }
  public void glBindTexture(int target, int texture) {
    OpenGLES.glBindTexture(target, texture);
  }
  public void glBlendColor(float red, float green, float blue, float alpha) {
    throw new RuntimeException("Not implemented");
  }
  public void glBlendEquation(int mode) {
    throw new RuntimeException("Not implemented");
  }
  public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
    throw new RuntimeException("Not implemented");
  }
  public void glBlendFunc(int sfactor, int dfactor) {
    OpenGLES.glBlendFunc(sfactor, dfactor);
  }
  public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
    throw new RuntimeException("Not implemented");
  }
  public void glBufferData(int target, int size, Buffer data, int usage) {
    OpenGLES.glBufferData(target, size, data, usage);
  }
  public void glBufferSubData(int target, int offset, int size, Buffer data) {
    throw new RuntimeException("Not implemented");
  }
  public int glCheckFramebufferStatus(int target) {
    throw new RuntimeException("Not implemented");
  }
  public void glClear(int mask) {
    OpenGLES.glClear(mask);
  }
  public void glClearColor(float red, float green, float blue, float alpha) {
    OpenGLES.glClearColor(red, green, blue, alpha);
  }
  public void glClearDepth(double depth) {
    throw new RuntimeException("Not implemented");
  }
  public void glClearDepthf(float depth) {
    throw new RuntimeException("Not implemented");
  }
  public void glClearStencil(int s) {
    throw new RuntimeException("Not implemented");
  }
  public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
    throw new RuntimeException("Not implemented");
  }
  public void glCompileShader(int shader) {
    OpenGLES.glCompileShader(shader);
  }
  public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data) {
    throw new RuntimeException("Not implemented");
  }
  public void glCompressedTexImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7) {
    throw new RuntimeException("Not implemented");
  }
  public void glCompressedTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, Buffer arg8) {
    throw new RuntimeException("Not implemented");
  }
  public void glCompressedTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("Not implemented");
  }
  public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data) {
    throw new RuntimeException("Not implemented");
  }
  public void glCompressedTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("Not implemented");
  }
  public void glCompressedTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, Buffer arg10) {
    throw new RuntimeException("Not implemented");
  }
  public void glCompressedTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10) {
    throw new RuntimeException("Not implemented");
  }
  public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border) {
    throw new RuntimeException("Not implemented");
  }
  public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
    throw new RuntimeException("Not implemented");
  }
  public void glCopyTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("Not implemented");
  }
  public int glCreateProgram() {
    return OpenGLES.glCreateProgram();
  }
  public int glCreateShader(int type) {
    return OpenGLES.glCreateShader(type);
  }
  public void glCullFace(int mode) {
    throw new RuntimeException("Not implemented");
  }
  public void glDeleteBuffers(int n, IntBuffer buffers) {
    OpenGLES.glDeleteBuffers(n, buffers);
  }
  public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
    OpenGLES.glDeleteFramebuffers(n, framebuffers);
  }
  public void glDeleteProgram(int program) {
    OpenGLES.glDeleteProgram(program);
  }
  public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
    throw new RuntimeException("Not implemented");
  }
  public void glDeleteShader(int shader) {
    OpenGLES.glDeleteShader(shader);
  }
  public void glDeleteTextures(int n, IntBuffer textures) {
    OpenGLES.glDeleteTextures(n, textures);
  }
  public void glDepthFunc(int func) {
    throw new RuntimeException("Not implemented");
  }
  public void glDepthMask(boolean flag) {
    throw new RuntimeException("Not implemented");
  }
  public void glDepthRange(double zNear, double zFar) {
    throw new RuntimeException("Not implemented");
  }
  public void glDepthRangef(float zNear, float zFar) {
    throw new RuntimeException("Not implemented");
  }
  public void glDetachShader(int program, int shader) {
    throw new RuntimeException("Not implemented");
  }
  public void glDisable(int cap) {
    OpenGLES.glDisable(cap);
  }
  public void glDisableVertexAttribArray(int index) {
    OpenGLES.glDisableVertexAttribArray(index);
  }
  public void glDrawArrays(int mode, int first, int count) {
    throw new RuntimeException("Not implemented");
  }
  public void glDrawElements(int mode, int count, int type, Buffer indices) {
    OpenGLES.glDrawElements(mode, count, type, indices);
  }
  public void glDrawElements(int mode, int count, int type, int offset) {
    OpenGLES.glDrawElements(mode, count, type, offset);
  }
  public void glEnable(int cap) {
    OpenGLES.glEnable(cap);
  }
  public void glEnableVertexAttribArray(int index) {
    OpenGLES.glEnableVertexAttribArray(index);
  }
  public void glFinish() {
    OpenGLES.glFinish();
  }
  public void glFlush() {
    OpenGLES.glFlush();
  }
  public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
    throw new RuntimeException("Not implemented");
  }
  public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
    OpenGLES.glFramebufferTexture2D(target, attachment, textarget, texture, level);
  }
  public void glFramebufferTexture3D(int target, int attachment, int textarget, int texture, int level, int zoffset) {
    throw new RuntimeException("Not implemented");
  }
  public void glFrontFace(int mode) {
    throw new RuntimeException("Not implemented");
  }
  public void glGenBuffers(int n, IntBuffer buffers) {
    OpenGLES.glGenBuffers(n, buffers);
  }
  public void glGenerateMipmap(int target) {
    OpenGLES.glGenerateMipmap(target);
  }
  public void glGenFramebuffers(int n, IntBuffer framebuffers) {
    OpenGLES.glGenFramebuffers(n, framebuffers);
  }
  public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
    OpenGLES.glGenRenderbuffers(n, renderbuffers);
  }
  public void glGenTextures(int n, IntBuffer textures) {
    OpenGLES.glGenTextures(n, textures);
  }
  public void glGetActiveAttrib(int program, int index, int bufsize, int[] length, int lengthOffset, int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
    throw new RuntimeException("Not implemented");
  }
  public void glGetActiveAttrib(int program, int index, int bufsize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name) {
    throw new RuntimeException("Not implemented");
  }
  public void glGetActiveUniform(int program, int index, int bufsize, int[] length, int lengthOffset, int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name, int nameOffset) {
    throw new RuntimeException("Not implemented");
  }
  public void glGetActiveUniform(int program, int index, int bufsize, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name) {
    throw new RuntimeException("Not implemented");
  }
  public void glGetAttachedShaders(int program, int maxcount, IntBuffer count, IntBuffer shaders) {
    throw new RuntimeException("Not implemented");
  }
  public int glGetAttribLocation(int program, String name) {
    return OpenGLES.glGetAttribLocation(program, name);
  }
  public boolean glGetBoolean(int pname) {
    OpenGLES.glGetBooleanv(pname, bufs.byteBuffer);
    return bufs.byteBuffer.get(0) == 1;
  }
  public void glGetBooleanv(int pname, byte[] params, int offset) {
    throw new RuntimeException("Not implemented");
  }
  public void glGetBooleanv(int pname, ByteBuffer params) {
    throw new RuntimeException("Not implemented");
  }
  public int glGetBoundBuffer(int arg0) {
    throw new RuntimeException("Not implemented");
  }
  public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("Not implemented");
  }
  public int glGetError() {
    return OpenGLES.glGetError();
  }
  public float glGetFloat(int pname) {
    OpenGLES.glGetFloatv(pname, bufs.floatBuffer);
    return bufs.floatBuffer.get(0);
  }
  public void glGetFloatv(int pname, FloatBuffer params) {
    OpenGLES.glGetFloatv(pname, params);
  }
  public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
    throw new RuntimeException("Not implemented");
  }
  public int glGetInteger(int pname) {
    OpenGLES.glGetIntegerv(pname, bufs.intBuffer);
    return bufs.intBuffer.get(0);
  }
  public void glGetIntegerv(int pname, IntBuffer params) {
    OpenGLES.glGetIntegerv(pname, params);
  }
  public void glGetProgramBinary(int arg0, int arg1, IntBuffer arg2, IntBuffer arg3, Buffer arg4) {
    throw new RuntimeException("Not implemented");
  }
  public void glGetProgramInfoLog(int program, int bufsize, IntBuffer length, ByteBuffer infolog) {
    throw new RuntimeException("Not implemented");
  }
  public String glGetProgramInfoLog(int program) {
    ByteBuffer bbuf = bufs.createByteBuffer(MAX_LOG_SIZE);
    glGetProgramInfoLog(program, MAX_LOG_SIZE, bufs.intBuffer, bbuf);
    return toString(bbuf, bufs.intBuffer.get(0));
  }
  public void glGetProgramiv(int program, int pname, IntBuffer params) {
    OpenGLES.glGetProgramiv(program, pname, params);
  }
  public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("Not implemented");
  }
  public void glGetShaderInfoLog(int shader, int bufsize, IntBuffer length, ByteBuffer infolog) {
    throw new RuntimeException("Not implemented");
  }
  public String glGetShaderInfoLog(int shader) {
    ByteBuffer bbuf = bufs.createByteBuffer(MAX_LOG_SIZE);
    glGetShaderInfoLog(shader, MAX_LOG_SIZE, bufs.intBuffer, bbuf);
    return toString(bbuf, bufs.intBuffer.get(0));
  }
  public void glGetShaderiv(int shader, int pname, IntBuffer params) {
    OpenGLES.glGetShaderiv(shader, pname, params);
  }
  public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, int[] range, int rangeOffset, int[] precision, int precisionOffset) {
    throw new RuntimeException("Not implemented");
  }
  public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
    throw new RuntimeException("Not implemented");
  }
  public void glGetShaderSource(int shader, int bufsize, int[] length, int lengthOffset, byte[] source, int sourceOffset) {
    throw new RuntimeException("Not implemented");
  }
  public void glGetShaderSource(int shader, int bufsize, IntBuffer length, ByteBuffer source) {
    throw new RuntimeException("Not implemented");
  }
  public String glGetString(int name) {
    return OpenGLES.glGetString(name);
  }
  public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
    OpenGLES.glGetTexParameterfv(target, pname, params);
  }
  public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
    OpenGLES.glGetTexParameteriv(target, pname, params);
  }
  public void glGetUniformfv(int program, int location, FloatBuffer params) {
    OpenGLES.glGetUniformfv(program, location, params);
  }
  public void glGetUniformiv(int program, int location, IntBuffer params) {
    OpenGLES.glGetUniformiv(program, location, params);
  }
  public int glGetUniformLocation(int program, String name) {
    return OpenGLES.glGetUniformLocation(program, name);
  }
  public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
    OpenGLES.glGetVertexAttribfv(index, pname, params);
  }
  public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
    OpenGLES.glGetVertexAttribiv(index, pname, params);
  }
  public void glHint(int target, int mode) {
    OpenGLES.glHint(target, mode);
  }
  public boolean glIsBuffer(int buffer) {
    return OpenGLES.glIsBuffer(buffer);
  }
  public boolean glIsEnabled(int cap) {
    return OpenGLES.glIsEnabled(cap);
  }
  public boolean glIsFramebuffer(int framebuffer) {
    return OpenGLES.glIsFramebuffer(framebuffer);
  }
  public boolean glIsProgram(int program) {
    return OpenGLES.glIsProgram(program);
  }
  public boolean glIsRenderbuffer(int renderbuffer) {
    return OpenGLES.glIsRenderbuffer(renderbuffer);
  }
  public boolean glIsShader(int shader) {
    return OpenGLES.glIsShader(shader);
  }
  public boolean glIsTexture(int texture) {
    return OpenGLES.glIsTexture(texture);
  }
  public boolean glIsVBOArrayEnabled() {
    throw new RuntimeException("Not implemented");
  }
  public boolean glIsVBOElementEnabled() {
    throw new RuntimeException("Not implemented");
  }
  public void glLineWidth(float width) {
    OpenGLES.glLineWidth(width);
  }
  public void glLinkProgram(int program) {
    OpenGLES.glLinkProgram(program);
  }
  public ByteBuffer glMapBuffer(int arg0, int arg1) {
    throw new RuntimeException("Not implemented");
  }
  public void glPixelStorei(int pname, int param) {
    OpenGLES.glPixelStorei(pname, param);
  }
  public void glPolygonOffset(float factor, float units) {
    throw new RuntimeException("Not implemented");
  }
  public void glProgramBinary(int arg0, int arg1, Buffer arg2, int arg3) {
    throw new RuntimeException("Not implemented");
  }
  public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
    throw new RuntimeException("Not implemented");
  }
  public void glReadPixels(int x, int y, int width, int height, int format, int type, int pixelsBufferOffset) {
    throw new RuntimeException("Not implemented");
  }
  public void glReleaseShaderCompiler() {
    throw new RuntimeException("Not implemented");
  }
  public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
    throw new RuntimeException("Not implemented");
  }
  public void glSampleCoverage(float value, boolean invert) {
    throw new RuntimeException("Not implemented");
  }
  public void glScissor(int x, int y, int width, int height) {
    OpenGLES.glScissor(x, y, width, height);
  }
  public void glShaderBinary(int n, int[] shaders, int offset, int binaryformat, Buffer binary, int length) {
    throw new RuntimeException("Not implemented");
  }
  public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
    throw new RuntimeException("Not implemented");
  }
  public void glShaderSource(int shader, int count, String[] strings, int[] length, int lengthOffset) {
    throw new RuntimeException("Not implemented");
  }
  public void glShaderSource(int shader, int count, String[] strings, IntBuffer length) {
    throw new RuntimeException("Not implemented");
  }
  public void glShaderSource(int shader, String source) {
    BytePtr.BytePtrPtr sources = Struct.allocate(BytePtr.BytePtrPtr.class, 1);
    sources.next(0).set(BytePtr.toBytePtrAsciiZ(source));
    OpenGLES.glShaderSource(shader, 1, sources, null);
  }
  public void glStencilFunc(int func, int ref, int mask) {
    throw new RuntimeException("Not implemented");
  }
  public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
    throw new RuntimeException("Not implemented");
  }
  public void glStencilMask(int mask) {
    throw new RuntimeException("Not implemented");
  }
  public void glStencilMaskSeparate(int face, int mask) {
    throw new RuntimeException("Not implemented");
  }
  public void glStencilOp(int fail, int zfail, int zpass) {
    throw new RuntimeException("Not implemented");
  }
  public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
    throw new RuntimeException("Not implemented");
  }
  public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels) {
    OpenGLES.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
  }
  public void glTexImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("Not implemented");
  }
  public void glTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, Buffer arg9) {
    throw new RuntimeException("Not implemented");
  }
  public void glTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9) {
    throw new RuntimeException("Not implemented");
  }
  public void glTexParameterf(int target, int pname, float param) {
    throw new RuntimeException("Not implemented");
  }
  public void glTexParameterfv(int target, int pname, FloatBuffer params) {
    throw new RuntimeException("Not implemented");
  }
  public void glTexParameteri(int target, int pname, int param) {
    OpenGLES.glTexParameteri(target, pname, param);
  }
  public void glTexParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("Not implemented");
  }
  public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels) {
    OpenGLES.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
  }
  public void glTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8) {
    throw new RuntimeException("Not implemented");
  }
  public void glTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, Buffer arg10) {
    throw new RuntimeException("Not implemented");
  }
  public void glTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10) {
    throw new RuntimeException("Not implemented");
  }
  public void glUniform1f(int location, float x) {
    OpenGLES.glUniform1f(location, x);
  }
  public void glUniform1fv(int location, int count, FloatBuffer v) {
    OpenGLES.glUniform1fv(location, count, v);
  }
  public void glUniform1i(int location, int x) {
    OpenGLES.glUniform1i(location, x);
  }
  public void glUniform1iv(int location, int count, IntBuffer v) {
    OpenGLES.glUniform1iv(location, count, v);
  }
  public void glUniform2f(int location, float x, float y) {
    OpenGLES.glUniform2f(location, x, y);
  }
  public void glUniform2fv(int location, int count, FloatBuffer v) {
    OpenGLES.glUniform2fv(location, count, v);
  }
  public void glUniform2i(int location, int x, int y) {
    OpenGLES.glUniform2i(location, x, y);
  }
  public void glUniform2iv(int location, int count, IntBuffer v) {
    throw new RuntimeException("Not implemented");
  }
  public void glUniform3f(int location, float x, float y, float z) {
    OpenGLES.glUniform3f(location, x, y, z);
  }
  public void glUniform3fv(int location, int count, FloatBuffer v) {
    OpenGLES.glUniform3fv(location, count, v);
  }
  public void glUniform3i(int location, int x, int y, int z) {
    OpenGLES.glUniform3i(location, x, y, z);
  }
  public void glUniform3iv(int location, int count, IntBuffer v) {
    throw new RuntimeException("Not implemented");
  }
  public void glUniform4f(int location, float x, float y, float z, float w) {
    OpenGLES.glUniform4f(location, x, y, z, w);
  }
  public void glUniform4fv(int location, int count, FloatBuffer v) {
    OpenGLES.glUniform4fv(location, count, v);
  }
  public void glUniform4i(int location, int x, int y, int z, int w) {
    OpenGLES.glUniform4i(location, x, y, z, w);
  }
  public void glUniform4iv(int location, int count, IntBuffer v) {
    throw new RuntimeException("Not implemented");
  }
  public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value) {
    OpenGLES.glUniformMatrix2fv(location, count, transpose, value);
  }
  public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value) {
    OpenGLES.glUniformMatrix3fv(location, count, transpose, value);
  }
  public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value) {
    OpenGLES.glUniformMatrix4fv(location, count, transpose, value);
  }
  public boolean glUnmapBuffer(int arg0) {
    throw new RuntimeException("Not implemented");
  }
  public void glUseProgram(int program) {
    OpenGLES.glUseProgram(program);
  }
  public void glValidateProgram(int program) {
    throw new RuntimeException("Not implemented");
  }
  public void glVertexAttrib1f(int indx, float x) {
    throw new RuntimeException("Not implemented");
  }
  public void glVertexAttrib1fv(int indx, FloatBuffer values) {
    throw new RuntimeException("Not implemented");
  }
  public void glVertexAttrib2f(int indx, float x, float y) {
    throw new RuntimeException("Not implemented");
  }
  public void glVertexAttrib2fv(int indx, FloatBuffer values) {
    throw new RuntimeException("Not implemented");
  }
  public void glVertexAttrib3f(int indx, float x, float y, float z) {
    throw new RuntimeException("Not implemented");
  }
  public void glVertexAttrib3fv(int indx, FloatBuffer values) {
    throw new RuntimeException("Not implemented");
  }
  public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
    throw new RuntimeException("Not implemented");
  }
  public void glVertexAttrib4fv(int indx, FloatBuffer values) {
    throw new RuntimeException("Not implemented");
  }
  public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
    OpenGLES.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
  }
  public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr) {
    OpenGLES.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
  }
  public void glViewport(int x, int y, int width, int height) {
    OpenGLES.glViewport(x, y, width, height);
  }
  public boolean hasGLSL() {
    throw new RuntimeException("Not implemented");
  }
  public boolean isExtensionAvailable(String extension) {
    throw new RuntimeException("Not implemented");
  }
  public boolean isFunctionAvailable(String function) {
    throw new RuntimeException("Not implemented");
  }

  private static final int MAX_LOG_SIZE = 8192;

  private static String toString (ByteBuffer bbuf, int length) {
    byte[] data = new byte[length];
    bbuf.get(data, 0, length);
    return new String(data);
  }
}
