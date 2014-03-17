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

/**
 * {@link GLBuffer} implementation based on {@code java.nio} and usable with {@link GL20}.
 */
public abstract class GL20Buffer implements GLBuffer {

  public static class FloatImpl extends GL20Buffer implements GLBuffer.Float {
    float[] staging;
    int stagingPos;
    FloatBuffer buffer;

    public FloatImpl(GL20 gl, int capacity) {
      super(gl);
      expand(capacity);
    }

    @Override
    public int capacity() {
      return staging.length;
    }

    @Override
    public int position() {
      return stagingPos;
    }

    @Override
    public void skip(int count) {
      stagingPos += count;
    }

    @Override
    public void expand(int capacity) {
      // make sure we're not trying to expand this buffer while it has unflushed data
      assert stagingPos == 0;
      ByteBuffer raw = ByteBuffer.allocateDirect(capacity * bytesPerElement()).
        order(ByteOrder.nativeOrder());
      buffer = raw.asFloatBuffer();
      staging = new float[capacity];
    }

    @Override
    public void reset() {
      stagingPos = 0;
    }

    @Override
    public void flush() {
      buffer.position(0);
      buffer.put(staging, 0, stagingPos);
      stagingPos = 0;
    }

    @Override
    public float[] array() {
      return staging;
    }

    @Override
    public Float add(float value) {
      staging[stagingPos++] = value;
      return this;
    }

    @Override
    public Float add(float x, float y) {
      staging[stagingPos++] = x;
      staging[stagingPos++] = y;
      return this;
    }

    @Override
    public Float add(float[] data) {
      System.arraycopy(data, 0, staging, stagingPos, data.length);
      stagingPos += data.length;
      return this;
    }

    @Override
    public Float add(float[] data, int offset, int length) {
      System.arraycopy(data, offset, staging, stagingPos, length);
      stagingPos += length;
      return this;
    }

    @Override
    public String toString() {
      return "floatbuf:" + bufferId;
    }

    @Override
    protected Buffer buffer() {
      return buffer;
    }

    @Override
    protected int bytesPerElement() {
      return 4;
    }
  }

  public static class ShortImpl extends GL20Buffer implements GLBuffer.Short {
    short[] staging;
    int stagingPos;
    ShortBuffer buffer;

    public ShortImpl(GL20 gl, int capacity) {
      super(gl);
      expand(capacity);
    }

    @Override
    public int capacity() {
      return staging.length;
    }

    @Override
    public int position() {
      return stagingPos;
    }

    @Override
    public void skip(int count) {
      stagingPos += count;
    }

    @Override
    public void expand(int capacity) {
      // make sure we're not trying to expand this buffer while it has unflushed data
      assert stagingPos == 0;
      buffer = ByteBuffer.allocateDirect(capacity * bytesPerElement()).
        order(ByteOrder.nativeOrder()).asShortBuffer();
      staging = new short[capacity];
    }

    @Override
    public void reset() {
      stagingPos = 0;
    }

    @Override
    public void flush() {
      buffer.position(0);
      buffer.put(staging, 0, stagingPos);
      stagingPos = 0;
    }

    @Override
    public short[] array() {
      return staging;
    }

    @Override
    public Short add(int value) {
      staging[stagingPos++] = (short)value;
      return this;
    }

    @Override
    public Short add(int x, int y) {
      staging[stagingPos++] = (short)x;
      staging[stagingPos++] = (short)y;
      return this;
    }

    @Override
    public Short add(short[] data) {
      System.arraycopy(data, 0, staging, stagingPos, data.length);
      stagingPos += data.length;
      return this;
    }

    @Override
    public Short add(short[] data, int offset, int length) {
      System.arraycopy(data, offset, staging, stagingPos, length);
      stagingPos += length;
      return this;
    }

    @Override
    public void drawElements(int mode, int count) {
      gl.glDrawElements(mode, count, GL20.GL_UNSIGNED_SHORT, 0);
    }

    @Override
    public String toString() {
      return "shortbuf:" + bufferId;
    }

    @Override
    protected Buffer buffer() {
      return buffer;
    }

    @Override
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
    int count = position(), byteSize = byteSize();
    flush();
    Buffer buffer = buffer();
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
