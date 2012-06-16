/**
 * Copyright 2012 The PlayN Authors
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
package playn.core.gl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import playn.core.Asserts;
import playn.core.InternalTransform;

/**
 * {@link GLBuffer} implementation based on {@code java.nio} and usable with {@link GL20}.
 */
public abstract class GL20Buffer implements GLBuffer {

  public static class FloatImpl extends GL20Buffer implements GLBuffer.Float {
    FloatBuffer buffer;

    public FloatImpl(GL20 gl, int capacity) {
      super(gl);
      expand(capacity);
    }

    @Override
    public int capacity() {
      return buffer.capacity();
    }

    @Override
    public int position() {
      return buffer.position();
    }

    @Override
    public int byteSize() {
      return position() * BYTES_PER_FLOAT;
    }

    @Override
    public void skip(int count) {
      buffer.position(position()+count);
    }

    @Override
    public void expand(int capacity) {
      // make sure we're not trying to expand this buffer while it has unflushed data
      Asserts.checkState(buffer == null || buffer.position() == 0);
      buffer = ByteBuffer.allocateDirect(capacity * BYTES_PER_FLOAT).
        order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    @Override
    public Float add(float value) {
      buffer.put(value);
      return this;
    }

    @Override
    public Float add(InternalTransform xform) {
      // TODO: optimize?
      return add(xform.m00(), xform.m01(), xform.m10(), xform.m11(), xform.tx(), xform.ty());
    }

    @Override
    public Float add(float x, float y) {
      buffer.put(x).put(y);
      return this;
    }

    @Override
    public Float add(float m00, float m01, float m10, float m11, float tx, float ty) {
      buffer.put(m00).put(m01).put(m10).put(m11).put(tx).put(ty);
      return this;
    }

    @Override
    public Float add(float[] data, int offset, int length) {
      buffer.put(data, offset, length);
      return this;
    }

    protected Buffer buffer() {
      return buffer;
    }

    private static final int BYTES_PER_FLOAT = 4;
  }

  public static class ShortImpl extends GL20Buffer implements GLBuffer.Short {
    ShortBuffer buffer;

    public ShortImpl(GL20 gl, int capacity) {
      super(gl);
      expand(capacity);
    }

    @Override
    public int capacity() {
      return buffer.capacity();
    }

    @Override
    public int position() {
      return buffer.position();
    }

    @Override
    public int byteSize() {
      return position() * BYTES_PER_SHORT;
    }

    @Override
    public void skip(int count) {
      buffer.position(position()+count);
    }

    @Override
    public void expand(int capacity) {
      // make sure we're not trying to expand this buffer while it has unflushed data
      Asserts.checkState(buffer == null || buffer.position() == 0);
      buffer = ByteBuffer.allocateDirect(capacity * BYTES_PER_SHORT).
        order(ByteOrder.nativeOrder()).asShortBuffer();
    }

    @Override
    public Short add(int value) {
      buffer.put((short)value);
      return this;
    }

    @Override
    public void drawElements(int mode, int count) {
      gl.glDrawElements(mode, count, GL20.GL_UNSIGNED_SHORT, 0);
    }

    protected Buffer buffer() {
      return buffer;
    }

    private static final int BYTES_PER_SHORT = 2;
  }

  protected final GL20 gl;
  private final int bufferId;

  @Override
  public void bind(int target) {
    gl.glBindBuffer(target, bufferId);
  }

  @Override
  public int send(int target, int usage) {
    Buffer buffer = buffer();
    int count = buffer.position(), byteSize = byteSize();
    buffer.position(0);
    gl.glBufferData(target, byteSize, buffer, usage);
    return count;
  }

  @Override
  public void destroy() {
    gl.glDeleteBuffers(1, new int[] { bufferId }, 0);
  }

  protected abstract Buffer buffer();

  protected GL20Buffer(GL20 gl) {
    this.gl = gl;
    this.bufferId = genBufferId(gl);
  }

  private static int genBufferId(GL20 gl) {
    int[] ids = new int[1];
    gl.glGenBuffers(1, ids, 0);
    return ids[0];
  }
}
