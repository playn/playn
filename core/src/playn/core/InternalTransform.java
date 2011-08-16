/**
 * Copyright 2011 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core;

import pythagoras.f.Transform;

/**
 * Exposes internals of our transform to the graphics backends.
 */
public interface InternalTransform extends Transform {
  /** Returns the m00 (x-scale) component of the affine transform matrix. */
  float m00();

  /** Returns the m01 (y-shear) component of the affine transform matrix. */
  float m01();

  /** Returns the m10 (x-shear) component of the affine transform matrix. */
  float m10();

  /** Returns the m11 (y-scale) component of the affine transform matrix. */
  float m11();

  /**
   * Configures this transform to be equal to the supplied other.
   */
  InternalTransform set(Transform other);

  /**
   * Concatenates the supplied layer transform onto this transform, accounting for the specified
   * origin offset.
   */
  InternalTransform concatenate(Transform other, float originX, float originY);

  /**
   * Concatenates the supplied transform onto this transform, accounting for the specified origin
   * offset.
   */
  InternalTransform concatenate(float m00, float m01, float m10, float m11, float tx, float ty,
      float originX, float originY);
}
