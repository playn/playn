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
package playn.ios;

import cli.System.IntPtr;
import cli.System.Runtime.InteropServices.GCHandle;
import cli.System.Runtime.InteropServices.GCHandleType;

import cli.OpenTK.Graphics.ES20.BeginMode;
import cli.OpenTK.Graphics.ES20.BufferTarget;
import cli.OpenTK.Graphics.ES20.BufferUsage;
import cli.OpenTK.Graphics.ES20.DrawElementsType;
import cli.OpenTK.Graphics.ES20.GL;

import playn.core.gl.GLBuffer;

public abstract class IOSGLBuffer implements GLBuffer {

  public static class FloatImpl extends IOSGLBuffer implements GLBuffer.Float {
    protected float[] data;
    protected GCHandle handle;

    public FloatImpl(int capacity) {
      expand(capacity);
    }

    @Override
    public int capacity() {
      return data.length;
    }

    @Override
    public void expand(int capacity) {
      if (handle != null)
        handle.Free();
      data = new float[capacity];
      handle = GCHandle.Alloc(data, GCHandleType.wrap(GCHandleType.Pinned));
    }

    @Override
    public void skip(int count) {
      position += count;
    }

    @Override
    public void reset() {
      position = 0;
    }

    @Override
    public void destroy() {
      super.destroy();
      if (handle != null)
        handle.Free();
    }

    @Override
    public float[] array() {
      return data;
    }

    @Override
    public Float add(float value) {
      data[position++] = value;
      return this;
    }

    @Override
    public Float add(float x, float y) {
      data[position++] = x;
      data[position++] = y;
      return this;
    }

    @Override
    public Float add(float[] data) {
      System.arraycopy(data, 0, this.data, position, data.length);
      position += data.length;
      return this;
    }

    @Override
    public Float add(float[] data, int offset, int length) {
      System.arraycopy(data, offset, this.data, position, length);
      position += length;
      return this;
    }

    @Override
    IntPtr pointer() {
      return handle.AddrOfPinnedObject();
    }

    @Override
    int bytesPerElement() {
      return 4;
    }
  }

  /** A buffer of 16-bit unsigned integers. */
  public static class ShortImpl extends IOSGLBuffer implements GLBuffer.Short {
    protected short[] data;
    protected GCHandle handle;

    public ShortImpl(int capacity) {
      expand(capacity);
    }

    @Override
    public int capacity() {
      return data.length;
    }

    @Override
    public void expand(int capacity) {
      if (handle != null)
        handle.Free();
      data = new short[capacity];
      handle = GCHandle.Alloc(data, GCHandleType.wrap(GCHandleType.Pinned));
    }

    @Override
    public void skip(int count) {
      position += count;
    }

    @Override
    public void reset() {
      position = 0;
    }

    @Override
    public void destroy() {
      super.destroy();
      if (handle != null)
        handle.Free();
    }

    @Override
    public short[] array() {
      return data;
    }

    @Override
    public Short add(int value) {
      data[position++] = (short) value;
      return this;
    }

    @Override
    public Short add(int x, int y) {
      data[position++] = (short) x;
      data[position++] = (short) y;
      return this;
    }

    @Override
    public Short add(short[] data) {
      System.arraycopy(data, 0, this.data, position, data.length);
      position += data.length;
      return this;
    }

    @Override
    public Short add(short[] data, int offset, int length) {
      System.arraycopy(data, offset, this.data, position, length);
      position += length;
      return this;
    }

    @Override
    public void drawElements(int mode, int count) {
      GL.DrawElements(BeginMode.wrap(mode), count,
                      DrawElementsType.wrap(DrawElementsType.UnsignedShort), new IntPtr(0));
    }

    @Override
    IntPtr pointer() {
      return handle.AddrOfPinnedObject();
    }

    @Override
    int bytesPerElement() {
      return 2;
    }
  }

  private final int bufferId;
  protected int position;

  @Override
  public int position() {
    return position;
  }

  @Override
  public int byteSize() {
    return position * bytesPerElement();
  }

  @Override
  public void bind(int target) {
    GL.BindBuffer(BufferTarget.wrap(target), bufferId);
  }

  @Override
  public int send(int target, int usage) {
    // TODO: why is byteSize an IntPtr? File MonoTouch bug?
    GL.BufferData(BufferTarget.wrap(target), new IntPtr(byteSize()), pointer(),
                  BufferUsage.wrap(usage));
    int oposition = position;
    position = 0;
    return oposition;
  }

  @Override
  public void flush() {
    // nothing needed here because we have no backing NIO buffer
  }

  @Override
  public void destroy() {
    GL.DeleteBuffers(1, new int[] { bufferId });
  }

  protected IOSGLBuffer() {
    int[] buffers = new int[1];
    GL.GenBuffers(1, buffers);
    this.bufferId = buffers[0];
  }

  abstract IntPtr pointer();

  abstract int bytesPerElement();
}
