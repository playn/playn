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

import playn.core.InternalTransform;

/**
 * An abstraction over bulk buffers for use with {@link GLShader} and {@link GLProgram}.
 */
public interface GLBuffer {

  /** A buffer of 32-bit floats. */
  interface Float extends GLBuffer {
    /** Adds a single value to this buffer.
     * @return this buffer for call chaining. */
    Float add(float value);

    /** Adds the supplied pair of values to this buffer.
     * @return this buffer for call chaining. */
    Float add(float x, float y);

    /** Adds the supplied values to this buffer.
     * @param data the values to be added.
     * @return this buffer for call chaining. */
    Float add(float[] data);

    /** Adds the supplied values to this buffer.
     * @param data the values to be added.
     * @param offset the offset into {@code data} at which to start adding.
     * @param length the number of values from {@code data} to add.
     * @return this buffer for call chaining. */
    Float add(float[] data, int offset, int length);
  }

  /** A buffer of 16-bit unsigned integers. */
  interface Short extends GLBuffer {
    /** Adds a single value to this buffer.
     * @return this buffer for call chaining. */
    Short add(int value);

    /** Adds the supplied pair of values to this buffer.
     * @return this buffer for call chaining. */
    Short add(int x, int y);

    /** Adds the supplied values to this buffer.
     * @param data the values to be added.
     * @return this buffer for call chaining. */
    Short add(short[] data);

    /** Adds the supplied values to this buffer.
     * @param data the values to be added.
     * @param offset the offset into {@code data} at which to start adding.
     * @param length the number of values from {@code data} to add.
     * @return this buffer for call chaining. */
    Short add(short[] data, int offset, int length);

    /** Issues a draw elements call using this buffer to define the elements. */
    void drawElements(int mode, int count);
  }

  /** Returns the total capacity of this buffer (number of floats, shorts, etc. it can hold). */
  int capacity();

  /** Returns the current position of the buffer pointer (which is also the number of elements
   * currently occupied in the buffer). */
  int position();

  /** The size of the data in this buffer, in bytes. */
  int byteSize();

  /** Advances the position of this buffer without writing any data to it. */
  void skip(int count);

  /** Expands the buffer to accommodate the specified new capacity. */
  void expand(int capacity);

  /** Issues the GL call to bind this buffer. */
  void bind(int target);

  /** Sends this buffer's data to GL, using glBufferData. A call to {@link #bind} must precede this
   * call. This resets the buffer offset to zero and prepares the buffer for the accumulation of new
   * elements.
   * @return the number of elements in the buffer at the time it was bound. */
  int send(int target, int usage);

  /** Resets this buffer's position to zero. */
  void reset();

  /** Releases any GL resources used by this buffer. The buffer may not be used subsequently. */
  void destroy();
}
