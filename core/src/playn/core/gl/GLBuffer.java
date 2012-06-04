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

    /** Adds the matrix for the supplied transform to this buffer.
     * @return this buffer for call chaining. */
    Float add(InternalTransform xform);

    /** Adds the supplied pair of values to this buffer.
     * @return this buffer for call chaining. */
    Float add(float x, float y);

    /** Adds the specified transform to this buffer.
     * @return this buffer for call chaining. */
    Float add(float m00, float m01, float m10, float m11, float tx, float ty);
  }

  /** A buffer of 16-bit unsigned integers. */
  interface Short extends GLBuffer {
    /** Adds a single value to this buffer.
     * @return this buffer for call chaining. */
    Short add(int value);

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

  /** Expands the buffer to accommodate the specified new capacity. */
  void expand(int capacity);

  /** Issues the GL call to bind this buffer. */
  void bind(int target);

  /** Writes this buffer's data to GL in preparation for rendering. This resets the buffer offset
   * to zero and prepares the buffer for the accumulation of new elements.
   * @return the number of elements in the buffer at the time it was bound. */
  int flush(int target, int usage);
}
