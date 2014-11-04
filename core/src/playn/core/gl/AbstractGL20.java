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
package playn.core.gl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * A helper class that bridges all the Java array functions to their {@link Buffer} counterparts
 * given an implementation of {@link Buffers}.
 */
public abstract class AbstractGL20 implements GL20 {

  /**
   * A helper class for bridging between Java arrays and buffers when implementing {@link GL20}.
   */
  public static abstract class Buffers {

    public IntBuffer intBuffer = createIntBuffer(32);
    public FloatBuffer floatBuffer = createFloatBuffer(32);
    public ByteBuffer byteBuffer = createByteBuffer(256);

    public void setIntBuffer(final int[] source, final int offset, final int length) {
      resizeIntBuffer(length);
      intBuffer.put(source, offset, length);
      intBuffer.rewind();
    }
    public void setFloatBuffer(final float[] source, final int offset, final int length) {
      resizeFloatBuffer(length);
      floatBuffer.put(source, offset, length);
      floatBuffer.rewind();
    }
    public void setByteBuffer(final byte[] source, final int offset, final int length) {
      resizeByteBuffer(length);
      byteBuffer.put(source, offset, length);
      byteBuffer.rewind();
    }

    public void resizeByteBuffer(final int length) {
      final int cap = byteBuffer.capacity();
      if (cap < length) {
        byteBuffer = createByteBuffer(newCap(cap, length));
      } else {
        byteBuffer.position(0);
      }
      byteBuffer.limit(length);
    }
    public void resizeIntBuffer(final int length) {
      final int cap = intBuffer.capacity();
      if (cap < length) {
        intBuffer = createIntBuffer(newCap(cap, length));
      } else {
        intBuffer.position(0);
      }
      intBuffer.limit(length);
    }
    public void resizeFloatBuffer(final int length) {
      final int cap = floatBuffer.capacity();
      if (cap < length) {
        floatBuffer = createFloatBuffer(newCap(cap, length));
      } else {
        floatBuffer.position(0);
      }
      floatBuffer.limit(length);
    }

    public abstract ByteBuffer createByteBuffer (int size);
    public IntBuffer createIntBuffer (int size) {
      return createByteBuffer(size*4).asIntBuffer();
    }
    public FloatBuffer createFloatBuffer (int size) {
      return createByteBuffer(size*4).asFloatBuffer();
    }

    private int newCap (int cap, int length) {
      int newLength = cap << 1;
      while (newLength < length) {
        newLength <<= 1;
      }
      return newLength;
    }
  }

  protected final Buffers bufs;

  protected AbstractGL20 (Buffers buffers) {
    bufs = buffers;
  }

  @Override
  public void glDeleteBuffers(int n, int[] buffers, int offset) {
    bufs.setIntBuffer(buffers, offset, n);
    glDeleteBuffers(n, bufs.intBuffer);
  }

  @Override
  public void glDeleteFramebuffers(int n, int[] framebuffers, int offset) {
    bufs.setIntBuffer(framebuffers, offset, n);
    glDeleteFramebuffers(n, bufs.intBuffer);
  }

  @Override
  public void glDeleteRenderbuffers(int n, int[] renderbuffers, int offset) {
    bufs.setIntBuffer(renderbuffers, offset, n);
    glDeleteRenderbuffers(n, bufs.intBuffer);
  }

  @Override
  public void glDeleteTextures(int n, int[] textures, int offset) {
    bufs.setIntBuffer(textures, offset, n);
    glDeleteTextures(n, bufs.intBuffer);
  }

  @Override
  public void glGenBuffers(int n, int[] buffers, int offset) {
    bufs.resizeIntBuffer(n);
    glGenBuffers(n, bufs.intBuffer);
    bufs.intBuffer.get(buffers, offset, n);
  }

  @Override
  public void glGenFramebuffers(int n, int[] framebuffers, int offset) {
    bufs.resizeIntBuffer(n);
    glGenFramebuffers(n, bufs.intBuffer);
    bufs.intBuffer.get(framebuffers, offset, n);
  }

  @Override
  public void glGenRenderbuffers(int n, int[] renderbuffers, int offset) {
    bufs.resizeIntBuffer(n);
    glGenRenderbuffers(n, bufs.intBuffer);
    bufs.intBuffer.get(renderbuffers, offset, n);
  }

  @Override
  public void glGenTextures(int n, int[] textures, int offset) {
    bufs.resizeIntBuffer(n);
    glGenTextures(n, bufs.intBuffer);
    bufs.intBuffer.get(textures, offset, n);
  }

  @Override
  public void glGetAttachedShaders(int program, int maxcount, int[] count, int countOffset, int[] shaders, int shadersOffset) {
    int countLength = count.length - countOffset;
    bufs.resizeIntBuffer(countLength);
    int shadersLength = shaders.length - shadersOffset;
    IntBuffer intBuffer2 = bufs.createIntBuffer(shadersLength);
    glGetAttachedShaders(program, maxcount, bufs.intBuffer, intBuffer2);
    bufs.intBuffer.get(count, countOffset, countLength);
    intBuffer2.get(shaders, shadersOffset, shadersLength);
  }

  @Override
  public void glGetBooleanv(int pname, byte[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeByteBuffer(length);
    glGetBooleanv(pname, bufs.byteBuffer);
    bufs.byteBuffer.get(params, offset, length);
  }

  @Override
  public void glGetBufferParameteriv(int target, int pname, int[] params, int offset) {
    int length = params.length - offset;
    bufs.resizeIntBuffer(length);
    glGetBufferParameteriv(target, pname, bufs.intBuffer);
    bufs.intBuffer.get(params, offset, length);
  }

  @Override
  public void glGetFloatv(int pname, float[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeFloatBuffer(length);
    glGetFloatv(pname, bufs.floatBuffer);
    bufs.floatBuffer.get(params, offset, length);
  }

  @Override
  public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, int[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeIntBuffer(length);
    glGetFramebufferAttachmentParameteriv(target, attachment, pname, bufs.intBuffer);
    bufs.intBuffer.get(params, offset, length);
  }

  @Override
  public void glGetIntegerv(int pname, int[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeIntBuffer(length);
    glGetIntegerv(pname, bufs.intBuffer);
    bufs.intBuffer.get(params, offset, length);
  }

  @Override
  public void glGetProgramBinary(int program, int bufsize, int[] length, int lengthOffset, int[] binaryformat, int binaryformatOffset, Buffer binary) {
    final int lengthLength = bufsize - lengthOffset;
    bufs.resizeIntBuffer(lengthLength);

    final int binaryformatLength = bufsize - binaryformatOffset;
    final IntBuffer intBuffer2 = bufs.createIntBuffer(binaryformatLength);
    glGetProgramBinary(program, bufsize, bufs.intBuffer, intBuffer2, binary);

    // Return length, binaryformat
    bufs.intBuffer.get(length, lengthOffset, lengthLength);
    intBuffer2.get(binaryformat, binaryformatOffset, binaryformatLength);
  }

  @Override
  public void glGetProgramInfoLog(int program, int bufsize, int[] length, int lengthOffset, byte[] infolog, int infologOffset) {
    final int intLength = length.length - lengthOffset;
    bufs.resizeIntBuffer(intLength);

    final int byteLength = bufsize - infologOffset;
    bufs.resizeByteBuffer(byteLength);

    glGetProgramInfoLog(program, bufsize, bufs.intBuffer, bufs.byteBuffer);
    // length is the length of the infoLog string being returned
    bufs.intBuffer.get(length, lengthOffset, intLength);
    // infoLog is the char array of the infoLog
    bufs.byteBuffer.get(infolog, byteLength, infologOffset);
  }

  @Override
  public void glGetProgramiv(int program, int pname, int[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeIntBuffer(length);
    glGetProgramiv(program, pname, bufs.intBuffer);
    bufs.intBuffer.get(params, offset, length);
  }

  @Override
  public void glGetRenderbufferParameteriv(int target, int pname, int[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeIntBuffer(length);
    glGetRenderbufferParameteriv(target, pname, bufs.intBuffer);
    bufs.intBuffer.get(params, offset, length);
  }

  @Override
  public void glGetShaderInfoLog(int shader, int bufsize, int[] length, int lengthOffset, byte[] infolog, int infologOffset) {
    final int intLength = length.length - lengthOffset;
    bufs.resizeIntBuffer(intLength);
    final int byteLength = bufsize - infologOffset;
    bufs.resizeByteBuffer(byteLength);
    glGetShaderInfoLog(shader, bufsize, bufs.intBuffer, bufs.byteBuffer);
    // length is the length of the infoLog string being returned
    bufs.intBuffer.get(length, lengthOffset, intLength);
    // infoLog is the char array of the infoLog
    bufs.byteBuffer.get(infolog, byteLength, infologOffset);
  }

  @Override
  public void glGetShaderiv(int shader, int pname, int[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeIntBuffer(length);
    glGetShaderiv(shader, pname, bufs.intBuffer);
    bufs.intBuffer.get(params, offset, length);
  }

  @Override
  public void glGetTexParameterfv(int target, int pname, float[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeFloatBuffer(length);
    glGetTexParameterfv(target, pname, bufs.floatBuffer);
    bufs.floatBuffer.get(params, offset, length);
  }

  @Override
  public void glGetTexParameteriv(int target, int pname, int[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeIntBuffer(length);
    glGetTexParameteriv(target, pname, bufs.intBuffer);
    bufs.intBuffer.get(params, offset, length);
  }

  @Override
  public void glGetUniformfv(int program, int location, float[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeFloatBuffer(length);
    glGetUniformfv(program, location, bufs.floatBuffer);
    bufs.floatBuffer.get(params, offset, length);
  }

  @Override
  public void glGetUniformiv(int program, int location, int[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeIntBuffer(length);
    glGetUniformiv(program, location, bufs.intBuffer);
    bufs.intBuffer.get(params, offset, length);
  }

  @Override
  public void glGetVertexAttribfv(int index, int pname, float[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeFloatBuffer(length);
    glGetVertexAttribfv(index, pname, bufs.floatBuffer);
    bufs.floatBuffer.get(params, offset, length);
  }

  @Override
  public void glGetVertexAttribiv(int index, int pname, int[] params, int offset) {
    final int length = params.length - offset;
    bufs.resizeIntBuffer(length);
    glGetVertexAttribiv(index, pname, bufs.intBuffer);
    bufs.intBuffer.get(params, offset, length);
  }

  @Override
  public void glTexParameterfv(int target, int pname, float[] params, int offset) {
    final int length = params.length - offset;
    bufs.setFloatBuffer(params, offset, length);
    glTexParameterfv(target, pname, bufs.floatBuffer);
  }

  @Override
  public void glTexParameteriv(int target, int pname, int[] params, int offset) {
    final int length = params.length - offset;
    bufs.setIntBuffer(params, offset, length);
    glTexParameteriv(target, pname, bufs.intBuffer);
  }

  @Override
  public void glUniform1fv(int location, int count, float[] v, int offset) {
    bufs.setFloatBuffer(v, offset, count);
    glUniform1fv(location, count, bufs.floatBuffer);
  }

  @Override
  public void glUniform1iv(int location, int count, int[] v, int offset) {
    bufs.setIntBuffer(v, offset, count);
    glUniform1iv(location, count, bufs.intBuffer);
  }

  @Override
  public void glUniform2fv(int location, int count, float[] v, int offset) {
    bufs.setFloatBuffer(v, offset, count);
    glUniform2fv(location, count, bufs.floatBuffer);
  }

  @Override
  public void glUniform2iv(int location, int count, int[] v, int offset) {
    bufs.setIntBuffer(v, offset, count);
    glUniform2iv(location, count, bufs.intBuffer);
  }

  @Override
  public void glUniform3fv(int location, int count, float[] v, int offset) {
    bufs.setFloatBuffer(v, offset, count);
    glUniform3fv(location, count, bufs.floatBuffer);
  }

  @Override
  public void glUniform3iv(int location, int count, int[] v, int offset) {
    bufs.setIntBuffer(v, offset, count);
    glUniform3iv(location, count, bufs.intBuffer);
  }

  @Override
  public void glUniform4fv(int location, int count, float[] v, int offset) {
    bufs.setFloatBuffer(v, offset, count);
    glUniform4fv(location, count, bufs.floatBuffer);
  }

  @Override
  public void glUniform4iv(int location, int count, int[] v, int offset) {
    bufs.setIntBuffer(v, offset, count);
    glUniform4iv(location, count, bufs.intBuffer);
  }

  @Override
  public void glUniformMatrix2fv(int location, int count, boolean transpose,
                                 float[] value, int offset) {
    bufs.setFloatBuffer(value, offset, 2*2*count);
    glUniformMatrix2fv(location, count, transpose, bufs.floatBuffer);
  }

  @Override
  public void glUniformMatrix3fv(int location, int count, boolean transpose,
                                 float[] value, int offset) {
    bufs.setFloatBuffer(value, offset, 3*3*count);
    glUniformMatrix3fv(location, count, transpose, bufs.floatBuffer);
  }

  @Override
  public void glUniformMatrix4fv(int location, int count, boolean transpose,
                                 float[] value, int offset) {
    bufs.setFloatBuffer(value, offset, 4*4*count);
    glUniformMatrix4fv(location, count, transpose, bufs.floatBuffer);
  }

  @Override
  public void glVertexAttrib1fv(int indx, float[] values, int offset) {
    glVertexAttrib1f(indx, values[indx + offset]);
  }

  @Override
  public void glVertexAttrib2fv(int indx, float[] values, int offset) {
    glVertexAttrib2f(indx, values[indx + offset], values[indx + 1 + offset]);
  }

  @Override
  public void glVertexAttrib3fv(int indx, float[] values, int offset) {
    glVertexAttrib3f(indx, values[indx + offset], values[indx + 1 + offset], values[indx + 2 + offset]);
  }

  @Override
  public void glVertexAttrib4fv(int indx, float[] values, int offset) {
    glVertexAttrib4f(indx, values[indx + offset], values[indx + 1 + offset], values[indx + 2 + offset], values[indx + 3 + offset]);
  }
}
