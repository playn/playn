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

import pythagoras.f.AffineTransform;
import pythagoras.f.Transform;
import pythagoras.f.Transforms;

/**
 * The default {@link InternalTransform} instance, used on non-JavaScript platforms.
 */
public class StockInternalTransform extends AffineTransform implements InternalTransform {
  /** The identity transform, don't modify it! */
  public static final StockInternalTransform IDENTITY = new StockInternalTransform();

  public StockInternalTransform() {
  }

  public StockInternalTransform(float m00, float m01, float m10, float m11, float tx, float ty) {
    super(m00, m01, m10, m11, tx, ty);
  }

  @Override
  public float m00() {
    return m00;
  }

  @Override
  public float m01() {
    return m01;
  }

  @Override
  public float m10() {
    return m10;
  }

  @Override
  public float m11() {
    return m11;
  }

  @Override
  public InternalTransform set(Transform other) {
    StockInternalTransform ot = (StockInternalTransform) other;
    setTransform(ot.m00, ot.m01, ot.m10, ot.m11, ot.tx, ot.ty);
    return this;
  }

  @Override
  public InternalTransform concatenate(Transform other, float originX, float originY) {
    StockInternalTransform ot = (StockInternalTransform) other;
    return concatenate(ot.m00, ot.m01, ot.m10, ot.m11, ot.tx, ot.ty, originX, originY);
  }

  @Override
  public InternalTransform concatenate(float m00, float m01, float m10, float m11, float tx,
      float ty, float originX, float originY) {
    Transforms.multiply(this, m00, m01, m10, m11, tx, ty, this);
    if (originX != 0 || originY != 0) translate(-originX, -originY);
    return this;
  }

  @Override
  public InternalTransform preConcatenate(InternalTransform other) {
    Transforms.multiply(other.m00(), other.m01(), other.m10(), other.m11(), other.tx(), other.ty(),
                        this, this);
    return this;
  }

  @Override
  public InternalTransform copy () {
    return new StockInternalTransform(m00, m01, m10, m11, tx, ty);
  }
}
