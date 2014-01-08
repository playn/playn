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
package playn.ios;

import cli.OpenTK.Graphics.ES20.GL;
import cli.OpenTK.Graphics.ES20.PixelFormat;
import cli.OpenTK.Graphics.ES20.PixelInternalFormat;
import cli.OpenTK.Graphics.ES20.PixelType;
import cli.OpenTK.Graphics.ES20.TextureTarget;
import cli.System.Runtime.InteropServices.GCHandle;
import cli.System.Runtime.InteropServices.GCHandleType;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import playn.core.gl.GL20;

// Extremely incomplete!
public class IOSGL20 implements GL20 {

  @Override
  public String getPlatformGLExtensions() {
    // String extensions = GL.GetString(GL._EXTENSIONS);
    // return extensions;
    throw new UnsupportedOperationException();
  }

  @Override
  public int getSwapInterval() {
    // return 0;
    throw new UnsupportedOperationException();
  }

  @Override
  public void glActiveTexture(int texture) {
    // GL.ActiveTexture(wrap(texture));
    throw new UnsupportedOperationException();
  }

  @Override
  public void glAttachShader(int program, int shader) {
    // GL.AttachShader(program, shader);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glBindAttribLocation(int program, int index, String name) {
    // GL.BindAttribLocation(program, index, name);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glBindBuffer(int target, int buffer) {
    // GL.BindBuffer(target, buffer);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glBindFramebuffer(int target, int framebuffer) {
    // GL.BindFramebuffer(target, framebuffer);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glBindRenderbuffer(int target, int renderbuffer) {
    // GL.BindRenderbuffer(target, renderbuffer);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glBindTexture(int target, int texture) {
    // GL.BindTexture(wrap(target), texture);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glBlendColor(float red, float green, float blue, float alpha) {
    // GL.BlendColor(red, green, blue, alpha);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glBlendEquation(int mode) {
    // GL.BlendEquation(wrap(mode));
    throw new UnsupportedOperationException();
  }

  @Override
  public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
    // GL.BlendEquationSeparate(wrap(modeRGB), wrap(modeAlpha));
    throw new UnsupportedOperationException();
  }

  @Override
  public void glBlendFunc(int sfactor, int dfactor) {
    // GL.BlendFunc(wrap(sfactor), wrap(dfactor));
    throw new UnsupportedOperationException();
  }

  @Override
  public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
    // GL.BlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glBufferData(int target, int size, Buffer data, int usage) {
    // GL.BufferData(target, size, data, usage);
    throw new UnsupportedOperationException();

  }

  @Override
  public void glBufferSubData(int target, int offset, int size, Buffer data) {
    // GL.BufferSubData(target, offset, size, data);
    throw new UnsupportedOperationException();

  }

  @Override
  public int glCheckFramebufferStatus(int target) {
    // return GL.CheckFramebufferStatus(target);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glClear(int mask) {
    // GL.Clear(mask);
    throw new UnsupportedOperationException();

  }

  @Override
  public void glClearColor(float red, float green, float blue, float alpha) {
    // GL.ClearColor(red, green, blue, alpha);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glClearDepth(double depth) {
    // GL.ClearDepthf((float) depth);
    throw new UnsupportedOperationException();

  }

  @Override
  public void glClearDepthf(float depth) {
    // GL.ClearDepthf(depth);
    throw new UnsupportedOperationException();

  }

  @Override
  public void glClearStencil(int s) {
    // GL.ClearStencil(s);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
    // GL.ColorMask(red, green, blue, alpha);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCompileShader(int shader) {
    // GL.CompileShader(shader);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCompressedTexImage2D(int target, int level, int internalformat, int width,
         int height, int border, int imageSize, Buffer data) {
    // GL.CompressedTexImage2D(target, level, internalformat, width, height, border, imageSize,
    //     data);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCompressedTexImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
         int arg6, int arg7) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCompressedTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
         int arg6, int arg7, Buffer arg8) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCompressedTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
         int arg6, int arg7, int arg8) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width,
         int height, int format, int imageSize, Buffer data) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCompressedTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
         int arg6, int arg7, int arg8) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCompressedTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
         int arg6, int arg7, int arg8, int arg9, Buffer arg10) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCompressedTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
         int arg6, int arg7, int arg8, int arg9, int arg10) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width,
         int height, int border) {
    // GL.CopyTexImage2D(target, level, internalformat, x, y, width, height, border);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y,
         int width, int height) {
    // GL.CopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCopyTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
         int arg6, int arg7, int arg8) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int glCreateProgram() {
    // return GL.CreateProgram();
    throw new UnsupportedOperationException();
  }

  @Override
  public int glCreateShader(int type) {
    // return GL.CreateShader(type);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glCullFace(int mode) {
    // GL.CullFace(mode);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDeleteBuffers(int n, int[] buffers, int offset) {
    // GL.DeleteBuffers(n, buffers, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDeleteBuffers(int n, IntBuffer buffers) {
    // GL.DeleteBuffers(n, buffers);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDeleteFramebuffers(int n, int[] framebuffers, int offset) {
    // GL.DeleteFramebuffers(n, framebuffers, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
    // GL.DeleteFramebuffers(n, framebuffers);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDeleteProgram(int program) {
    // GL.DeleteProgram(program);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDeleteRenderbuffers(int n, int[] renderbuffers, int offset) {
    // GL.DeleteRenderbuffers(n, renderbuffers, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
    // GL.DeleteRenderbuffers(n, renderbuffers);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDeleteShader(int shader) {
    // GL.DeleteShader(shader);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDeleteTextures(int n, int[] textures, int offset) {
    // GL.DeleteTextures(n, textures, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDeleteTextures(int n, IntBuffer textures) {
    // GL.DeleteTextures(n, textures);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDepthFunc(int func) {
    // GL.DepthFunc(func);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDepthMask(boolean flag) {
    // GL.DepthMask(flag);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDepthRange(double zNear, double zFar) {
    // GL.DepthRangef((float) zNear, (float) zFar);
    throw new UnsupportedOperationException();

  }

  @Override
  public void glDepthRangef(float zNear, float zFar) {
    // GL.DepthRangef(zNear, zFar);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDetachShader(int program, int shader) {
    // GL.DetachShader(program, shader);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDisable(int cap) {
    // GL.Disable(cap);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDisableVertexAttribArray(int index) {
    // GL.DisableVertexAttribArray(index);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDrawArrays(int mode, int first, int count) {
    // GL.DrawArrays(mode, first, count);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDrawElements(int mode, int count, int type, Buffer indices) {
    // GL.DrawElements(mode, count, type, indices);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glDrawElements(int mode, int count, int type, int offset) {
    // GL.DrawElements(mode, count, type, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glEnable(int cap) {
    // GL.Enable(cap);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glEnableVertexAttribArray(int index) {
    // GL.EnableVertexAttribArray(index);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glFinish() {
    // GL.Finish();
    throw new UnsupportedOperationException();
  }

  @Override
  public void glFlush() {
    // GL.Flush();
    throw new UnsupportedOperationException();
  }

  @Override
  public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget,
         int renderbuffer) {
    // GL.FramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture,
         int level) {
    // GL.FramebufferTexture2D(target, attachment, textarget, texture, level);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glFramebufferTexture3D(int target, int attachment, int textarget, int texture,
         int level, int zoffset) {
    // GL.FramebufferTexture2D(target, attachment, textarget, texture, level);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glFrontFace(int mode) {
    // GL.FrontFace(mode);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGenBuffers(int n, int[] buffers, int offset) {
    // GL.GenBuffers(n, buffers, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGenBuffers(int n, IntBuffer buffers) {
    // GL.GenBuffers(n, buffers);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGenerateMipmap(int target) {
    // GL.GenerateMipmap(target);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGenFramebuffers(int n, int[] framebuffers, int offset) {
    // GL.GenFramebuffers(n, framebuffers, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGenFramebuffers(int n, IntBuffer framebuffers) {
    // GL.GenFramebuffers(n, framebuffers);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGenRenderbuffers(int n, int[] renderbuffers, int offset) {
    // GL.GenRenderbuffers(n, renderbuffers, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
    // GL.GenRenderbuffers(n, renderbuffers);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGenTextures(int n, int[] textures, int offset) {
    // GL.GenTextures(n, textures, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGenTextures(int n, IntBuffer textures) {
    // GL.GenTextures(n, textures);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetActiveAttrib(int program, int index, int bufsize, int[] length,
         int lengthOffset, int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name,
         int nameOffset) {
    // GL.GetActiveAttrib(program, index, bufsize, length, lengthOffset, size, sizeOffset, type,
    //     typeOffset, name, nameOffset);
    throw new UnsupportedOperationException();

  }

  @Override
  public void glGetActiveAttrib(int program, int index, int bufsize, IntBuffer length,
         IntBuffer size, IntBuffer type, ByteBuffer name) {
    // int namePos = name.position();
    // GL.GetActiveAttrib(program, index, bufsize, length, size, type, name.get());
    // name.position(namePos);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetActiveUniform(int program, int index, int bufsize, int[] length,
         int lengthOffset, int[] size, int sizeOffset, int[] type, int typeOffset, byte[] name,
         int nameOffset) {
    // GL.GetActiveUniform(program, index, bufsize, length, lengthOffset, size, sizeOffset,
    //     type, typeOffset, name, nameOffset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetActiveUniform(int program, int index, int bufsize, IntBuffer length,
         IntBuffer size, IntBuffer type, ByteBuffer name) {
    // int namePos = name.position();
    // GL.GetActiveUniform(program, index, bufsize, length, size, type, name.get());
    // name.position(namePos);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetAttachedShaders(int program, int maxcount, int[] count, int countOffset,
         int[] shaders, int shadersOffset) {
    // GL.GetAttachedShaders(program, maxcount, count, countOffset, shaders, shadersOffset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetAttachedShaders(int program, int maxcount, IntBuffer count, IntBuffer shaders) {
    // GL.GetAttachedShaders(program, maxcount, count, shaders);
    throw new UnsupportedOperationException();
  }

  @Override
  public int glGetAttribLocation(int program, String name) {
    // return GL.GetAttribLocation(program, name);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean glGetBoolean(int pname) {
    // byte[] out = new byte[1];
    // glGetBooleanv(pname, out, 0);
    // return out[0] != GL_FALSE;
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetBooleanv(int pname, byte[] params, int offset) {
    // ByteBuffer buffer = ByteBuffer.wrap(params, offset, params.length - offset);
    // GL.GetBooleanv(pname, buffer.asIntBuffer());
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetBooleanv(int pname, ByteBuffer params) {
    // GL.GetBooleanv(pname, params.asIntBuffer());
    throw new UnsupportedOperationException();
  }

  @Override
  public int glGetBoundBuffer(int arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetBufferParameteriv(int target, int pname, int[] params, int offset) {
    // GL.GetBufferParameteriv(target, pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
    // GL.GetBufferParameteriv(target, pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public int glGetError() {
    // return GL.GetError();
    throw new UnsupportedOperationException();
  }

  @Override
  public float glGetFloat(int pname) {
    // float[] out = new float[1];
    // GL.GetFloatv(pname, out, 0);
    // return out[0];
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetFloatv(int pname, float[] params, int offset) {
    // GL.GetFloatv(pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetFloatv(int pname, FloatBuffer params) {
    // GL.GetFloatv(pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname,
         int[] params, int offset) {
    // GL.GetFramebufferAttachmentParameteriv(target, attachment, pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname,
         IntBuffer params) {
    // GL.GetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public int glGetInteger(int pname) {
    // int[] out = new int[1];
    // GL.GetIntegerv(pname, out, 0);
    // return out[0];
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetIntegerv(int pname, int[] params, int offset) {
    // GL.GetIntegerv(pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetIntegerv(int pname, IntBuffer params) {
    // GL.GetIntegerv(pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetProgramBinary(int arg0, int arg1, int[] arg2, int arg3, int[] arg4, int arg5,
         Buffer arg6) {
    throw new RuntimeException("glGetProgramBinary not supported.");
  }

  @Override
  public void glGetProgramBinary(int arg0, int arg1, IntBuffer arg2, IntBuffer arg3, Buffer arg4) {
    throw new RuntimeException("glGetProgramBinary not supported.");
  }

  @Override
  public void glGetProgramInfoLog(int program, int bufsize, int[] length, int lengthOffset,
         byte[] infolog, int infologOffset) {
    // String log = GL.GetProgramInfoLog(program);
    // byte[] byteArray = log.getBytes();
    // for (int i = 0; i < bufsize && i < byteArray.length; i++) {
    //   infolog[i + infologOffset] = byteArray[i];
    // }
    // length[lengthOffset] = log.length();
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetProgramInfoLog(int program, int bufsize, IntBuffer length, ByteBuffer infolog) {
    // glGetProgramInfoLog(program, bufsize, length.array(), infolog.position(), infolog.array(),
    //     infolog.position());
    throw new UnsupportedOperationException();
  }

  @Override
  public String glGetProgramInfoLog(int program) {
    // return GL.GetProgramInfoLog(program);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetProgramiv(int program, int pname, int[] params, int offset) {
    // GL.GetProgramiv(program, pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetProgramiv(int program, int pname, IntBuffer params) {
    // GL.GetProgramiv(program, pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetRenderbufferParameteriv(int target, int pname, int[] params, int offset) {
    // GL.GetRenderbufferParameteriv(target, pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
    // GL.GetRenderbufferParameteriv(target, pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetShaderInfoLog(int shader, int bufsize, int[] length, int lengthOffset,
         byte[] infolog, int infologOffset) {
    // String log = GL.GetShaderInfoLog(shader);
    // byte[] byteArray = log.getBytes();
    // for (int i = 0; i < bufsize && i < byteArray.length; i++) {
    //   infolog[i + infologOffset] = byteArray[i];
    // }
    // length[lengthOffset] = log.length();
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetShaderInfoLog(int shader, int bufsize, IntBuffer length, ByteBuffer infolog) {
    // glGetShaderInfoLog(shader, bufsize, length.array(), length.position(), infolog.array(),
    //     infolog.position());
  }

  @Override
  public String glGetShaderInfoLog(int shader) {
    // return GL.GetShaderInfoLog(shader);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetShaderiv(int shader, int pname, int[] params, int offset) {
    // GL.GetShaderiv(shader, pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetShaderiv(int shader, int pname, IntBuffer params) {
    // GL.GetShaderiv(shader, pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, int[] range,
         int rangeOffset, int[] precision, int precisionOffset) {
    // GL.GetShaderPrecisionFormat(shadertype, precisiontype, range, rangeOffset, precision,
    //     precisionOffset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range,
         IntBuffer precision) {
    // GL.GetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetShaderSource(int shader, int bufsize, int[] length, int lengthOffset,
         byte[] source, int sourceOffset) {
    // GL.GetShaderSource(shader, bufsize, length, lengthOffset, source, sourceOffset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetShaderSource(int shader, int bufsize, IntBuffer length, ByteBuffer source) {
    // GL.GetShaderSource(shader, bufsize, length.array(), 0, source.array(), 0);
    throw new UnsupportedOperationException();
  }

  @Override
  public String glGetString(int name) {
    // return GL.GetString(name);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetTexParameterfv(int target, int pname, float[] params, int offset) {
    // GL.GetTexParameterfv(target, pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
    // GL.GetTexParameterfv(target, pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetTexParameteriv(int target, int pname, int[] params, int offset) {
    // GL.GetTexParameteriv(target, pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
    // GL.GetTexParameteriv(target, pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetUniformfv(int program, int location, float[] params, int offset) {
    // GL.GetUniformfv(program, location, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetUniformfv(int program, int location, FloatBuffer params) {
    // GL.GetUniformfv(program, location, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetUniformiv(int program, int location, int[] params, int offset) {
    // GL.GetUniformiv(program, location, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetUniformiv(int program, int location, IntBuffer params) {
    // GL.GetUniformiv(program, location, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public int glGetUniformLocation(int program, String name) {
    // return GL.GetUniformLocation(program, name);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetVertexAttribfv(int index, int pname, float[] params, int offset) {
    // GL.GetVertexAttribfv(index, pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
    // GL.GetVertexAttribfv(index, pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetVertexAttribiv(int index, int pname, int[] params, int offset) {
    // GL.GetVertexAttribiv(index, pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
    // GL.GetVertexAttribiv(index, pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glHint(int target, int mode) {
    // GL.Hint(target, mode);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean glIsBuffer(int buffer) {
    // return GL.IsBuffer(buffer);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean glIsEnabled(int cap) {
    // return GL.IsEnabled(cap);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean glIsFramebuffer(int framebuffer) {
    // return GL.IsFramebuffer(framebuffer);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean glIsProgram(int program) {
    // return GL.IsProgram(program);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean glIsRenderbuffer(int renderbuffer) {
    // return GL.IsRenderbuffer(renderbuffer);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean glIsShader(int shader) {
    // return GL.IsShader(shader);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean glIsTexture(int texture) {
    // return GL.IsTexture(texture);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean glIsVBOArrayEnabled() {
    // return isExtensionAvailable("vertex_buffer_object");
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean glIsVBOElementEnabled() {
    // return glIsVBOArrayEnabled();
    throw new UnsupportedOperationException();
  }

  @Override
  public void glLineWidth(float width) {
    // GL.LineWidth(width);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glLinkProgram(int program) {
    // GL.LinkProgram(program);
    throw new UnsupportedOperationException();
  }

  @Override
  public ByteBuffer glMapBuffer(int arg0, int arg1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glPixelStorei(int pname, int param) {
    // GL.PixelStorei(pname, param);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glPolygonOffset(float factor, float units) {
    // GL.PolygonOffset(factor, units);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glProgramBinary(int arg0, int arg1, Buffer arg2, int arg3) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
    // GL.ReadPixels(x, y, width, height, format, type, pixels);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glReadPixels(int x, int y, int width, int height, int format, int type,
         int pixelsBufferOffset) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glReleaseShaderCompiler() {
    // GL.ReleaseShaderCompiler();
    throw new UnsupportedOperationException();
  }

  @Override
  public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
    // GL.RenderbufferStorage(target, internalformat, width, height);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glSampleCoverage(float value, boolean invert) {
    // GL.SampleCoverage(value, invert);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glScissor(int x, int y, int width, int height) {
    // GL.Scissor(x, y, width, height);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glShaderBinary(int n, int[] shaders, int offset, int binaryformat, Buffer binary,
         int length) {
    // GL.ShaderBinary(n, shaders, offset, binaryformat, binary, length);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
    // GL.ShaderBinary(n, shaders, binaryformat, binary, length);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glShaderSource(int shader, int count, String[] strings, int[] length, int lengthOffset) {
    // int totalLength = 0;
    // for (int i = lengthOffset; i < length.length; i++) {
    //   totalLength += length[i];
    // }
    // StringBuilder builder = new StringBuilder(totalLength);

    // for (int j = 0; j < count; j++) {
    //   builder.append(strings[j], 0, length[j]);
    // }

    // GL.ShaderSource(shader, builder.toString());
    throw new UnsupportedOperationException();
  }

  @Override
  public void glShaderSource(int shader, int count, String[] strings, IntBuffer length) {
    // glShaderSource(shader, count, strings, length.array(), 0);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glShaderSource(int shader, String string) {
    // GL.ShaderSource(shader, string);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glStencilFunc(int func, int ref, int mask) {
    // GL.StencilFunc(wrap(func), ref, mask);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
    // GL.StencilFuncSeparate(face, func, ref, mask);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glStencilMask(int mask) {
    // GL.StencilMask(mask);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glStencilMaskSeparate(int face, int mask) {
    // GL.StencilMaskSeparate(face, mask);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glStencilOp(int fail, int zfail, int zpass) {
    // GL.StencilOp(fail, zfail, zpass);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
    // GL.StencilOpSeparate(face, fail, zfail, zpass);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexImage2D(int target, int level, int internalformat, int width, int height,
         int border, int format, int type, Buffer pixels) {
    if (pixels != null) {
      GCHandle handle = createHandle(pixels);
      GL.TexImage2D(TextureTarget.wrap(target), level, PixelInternalFormat.wrap(internalformat),
                    width, height, border, PixelFormat.wrap(format), PixelType.wrap(type),
                    handle.AddrOfPinnedObject());
      handle.Free();
    } else {
      GL.TexImage2D(TextureTarget.wrap(target), level, PixelInternalFormat.wrap(internalformat),
                    width, height, border, PixelFormat.wrap(format), PixelType.wrap(type), null);
    }
  }

  @Override
  public void glTexImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6,
         int arg7, int arg8) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6,
         int arg7, int arg8, Buffer arg9) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6,
         int arg7, int arg8, int arg9) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexParameterf(int target, int pname, float param) {
    // GL.TexParameterf(target, pname, param);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexParameterfv(int target, int pname, float[] params, int offset) {
    // GL.TexParameterfv(target, pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexParameterfv(int target, int pname, FloatBuffer params) {
    // GL.TexParameterfv(target, pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexParameteri(int target, int pname, int param) {
    // GL.TexParameteri(target, pname, param);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexParameteriv(int target, int pname, int[] params, int offset) {
    // GL.TexParameteriv(target, pname, params, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexParameteriv(int target, int pname, IntBuffer params) {
    // GL.TexParameteriv(target, pname, params);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width,
         int height, int format, int type, Buffer pixels) {
    // GL.TexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexSubImage2D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6,
         int arg7, int arg8) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6,
         int arg7, int arg8, int arg9, Buffer arg10) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glTexSubImage3D(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6,
         int arg7, int arg8, int arg9, int arg10) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform1f(int location, float x) {
    // GL.Uniform1f(location, x);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform1fv(int location, int count, float[] v, int offset) {
    // GL.Uniform1fv(location, count, v, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform1fv(int location, int count, FloatBuffer v) {
    // GL.Uniform1fv(location, count, v);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform1i(int location, int x) {
    // GL.Uniform1i(location, x);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform1iv(int location, int count, int[] v, int offset) {
    // GL.Uniform1iv(location, count, v, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform1iv(int location, int count, IntBuffer v) {
    // GL.Uniform1iv(location, count, v);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform2f(int location, float x, float y) {
    // GL.Uniform2f(location, x, y);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform2fv(int location, int count, float[] v, int offset) {
    // GL.Uniform2fv(location, count, v, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform2fv(int location, int count, FloatBuffer v) {
    // GL.Uniform2fv(location, count, v);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform2i(int location, int x, int y) {
    // GL.Uniform2i(location, x, y);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform2iv(int location, int count, int[] v, int offset) {
    // GL.Uniform2iv(location, count, v, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform2iv(int location, int count, IntBuffer v) {
    // GL.Uniform2iv(location, count, v);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform3f(int location, float x, float y, float z) {
    // GL.Uniform3f(location, x, y, z);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform3fv(int location, int count, float[] v, int offset) {
    // GL.Uniform3fv(location, count, v, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform3fv(int location, int count, FloatBuffer v) {
    // GL.Uniform3fv(location, count, v);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform3i(int location, int x, int y, int z) {
    // GL.Uniform3i(location, x, y, z);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform3iv(int location, int count, int[] v, int offset) {
    // GL.Uniform3iv(location, count, v, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform3iv(int location, int count, IntBuffer v) {
    // GL.Uniform3iv(location, count, v);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform4f(int location, float x, float y, float z, float w) {
    // GL.Uniform4f(location, x, y, z, w);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform4fv(int location, int count, float[] v, int offset) {
    // GL.Uniform4fv(location, count, v, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform4fv(int location, int count, FloatBuffer v) {
    // GL.Uniform4fv(location, count, v);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform4i(int location, int x, int y, int z, int w) {
    // GL.Uniform4i(location, x, y, z, w);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform4iv(int location, int count, int[] v, int offset) {
    // GL.Uniform4iv(location, count, v, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniform4iv(int location, int count, IntBuffer v) {
    // GL.Uniform4iv(location, count, v);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value,
      int offset) {
    // GL.UniformMatrix2fv(location, count, transpose, value, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value) {
    // GL.UniformMatrix2fv(location, count, transpose, value);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value,
      int offset) {
    // GL.UniformMatrix3fv(location, count, transpose, value, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value) {
    // GL.UniformMatrix3fv(location, count, transpose, value);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value,
      int offset) {
    // GL.UniformMatrix4fv(location, count, transpose, value, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value) {
    // GL.UniformMatrix4fv(location, count, transpose, value);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean glUnmapBuffer(int arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void glUseProgram(int program) {
    // GL.UseProgram(program);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glValidateProgram(int program) {
    // GL.ValidateProgram(program);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib1f(int indx, float x) {
    // GL.VertexAttrib1f(indx, x);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib1fv(int indx, float[] values, int offset) {
    // GL.VertexAttrib1fv(indx, values, offset);

    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib1fv(int indx, FloatBuffer values) {
    // GL.VertexAttrib1fv(indx, values);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib2f(int indx, float x, float y) {
    // GL.VertexAttrib2f(indx, x, y);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib2fv(int indx, float[] values, int offset) {
    // GL.VertexAttrib2fv(indx, values, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib2fv(int indx, FloatBuffer values) {
    // GL.VertexAttrib2fv(indx, values);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib3f(int indx, float x, float y, float z) {
    // GL.VertexAttrib3f(indx, x, y, z);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib3fv(int indx, float[] values, int offset) {
    // GL.VertexAttrib3fv(indx, values, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib3fv(int indx, FloatBuffer values) {
    // GL.VertexAttrib3fv(indx, values);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
    // GL.VertexAttrib4f(indx, x, y, z, w);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib4fv(int indx, float[] values, int offset) {
    // GL.VertexAttrib4fv(indx, values, offset);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttrib4fv(int indx, FloatBuffer values) {
    // GL.VertexAttrib4fv(indx, values);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride,
       Buffer ptr) {
    // GL.VertexAttribPointer(indx, size, type, normalized, stride, ptr);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride,
       int ptr) {
    // GL.VertexAttribPointer(indx, size, type, normalized, stride, ptr);
    throw new UnsupportedOperationException();
  }

  @Override
  public void glViewport(int x, int y, int width, int height) {
    // GL.Viewport(x, y, width, height);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasGLSL() {
    // return true;
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isExtensionAvailable(String extension) {
    // String extensions = GL.GetString(GL._EXTENSIONS);
    // return extensions.contains(extension);
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isFunctionAvailable(String function) {
    // Method[] functions = this.getClass().getMethods();
    // for (int i = 0; i < functions.length; i++) {
    //   if (function == functions[i].getName())
    //     return true;
    // }
    // return false;
    throw new UnsupportedOperationException();
  }

  /** Allocates a pinned GCHandle pointing at a buffer's data. Remember to free it! */
  private static GCHandle createHandle (Buffer buffer)
  {
    Object array;
    if (buffer instanceof ByteBuffer)
      array = ((ByteBuffer)buffer).array();
    else if (buffer instanceof ShortBuffer)
      array = ((ShortBuffer)buffer).array();
    else if (buffer instanceof IntBuffer)
      array = ((IntBuffer)buffer).array();
    else if (buffer instanceof FloatBuffer)
      array = ((FloatBuffer)buffer).array();
    else if (buffer instanceof DoubleBuffer)
      array = ((DoubleBuffer)buffer).array();
    else
      throw new IllegalArgumentException();
    return GCHandle.Alloc(array, GCHandleType.wrap(GCHandleType.Pinned));
  }
}
