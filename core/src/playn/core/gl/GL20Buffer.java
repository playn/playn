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
    public void skip(int count) {
      buffer.position(position()+count);
    }

    @Override
    public void expand(int capacity) {
      // make sure we're not trying to expand this buffer while it has unflushed data
      Asserts.checkState(buffer == null || buffer.position() == 0);
      ByteBuffer raw = ByteBuffer.allocateDirect(capacity * bytesPerElement()).
        order(ByteOrder.nativeOrder());
      buffer = raw.asFloatBuffer();
    }

    @Override
    public Float add(float value) {
      buffer.put(value);
      return this;
    }

    @Override
    public Float add(float x, float y) {
      buffer.put(x).put(y);
      return this;
    }

    @Override
    public Float add(float[] data) {
      buffer.put(data, 0, data.length);
      return this;
    }

    @Override
    public Float add(float[] data, int offset, int length) {
      buffer.put(data, offset, length);
      return this;
    }

    @Override
    public void reset() {
      buffer.position(0);
    }

    @Override
    public String toString() {
      return "floatbuf:" + bufferId;
    }

    protected Buffer buffer() {
      return buffer;
    }

    protected int bytesPerElement() {
      return 4;
    }
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
    public void skip(int count) {
      buffer.position(position()+count);
    }

    @Override
    public void expand(int capacity) {
      // make sure we're not trying to expand this buffer while it has unflushed data
      Asserts.checkState(buffer == null || buffer.position() == 0);
      buffer = ByteBuffer.allocateDirect(capacity * bytesPerElement()).
        order(ByteOrder.nativeOrder()).asShortBuffer();
    }

    @Override
    public Short add(int value) {
      buffer.put((short)value);
      return this;
    }

    @Override
    public Short add(int x, int y) {
      buffer.put((short)x).put((short)y);
      return this;
    }

    @Override
    public Short add(short[] data) {
      buffer.put(data, 0, data.length);
      return this;
    }

    @Override
    public Short add(short[] data, int offset, int length) {
      buffer.put(data, offset, length);
      return this;
    }

    @Override
    public void drawElements(int mode, int count) {
      gl.glDrawElements(mode, count, GL20.GL_UNSIGNED_SHORT, 0);
    }

    @Override
    public void reset() {
      buffer.position(0);
    }

    @Override
    public String toString() {
      return "shortbuf:" + bufferId;
    }

    protected Buffer buffer() {
      return buffer;
    }

    protected int bytesPerElement() {
      return 2;
    }
  }

  protected final GL20 gl;
  protected final int bufferId;

  @Override
  public int byteSize() {
    return position() * bytesPerElement();
  }

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

  protected abstract int bytesPerElement();

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
