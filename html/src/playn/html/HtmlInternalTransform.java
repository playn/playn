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
package playn.html;

import com.google.gwt.typedarrays.client.Float32Array;

import pythagoras.f.AbstractTransform;
import pythagoras.f.AffineTransform;
import pythagoras.f.FloatMath;
import pythagoras.f.IPoint;
import pythagoras.f.IVector;
import pythagoras.f.Point;
import pythagoras.f.Transform;
import pythagoras.f.Vector;
import pythagoras.util.NoninvertibleTransformException;

import playn.core.InternalTransform;

/**
 * An {@link InternalTransform} implementation that uses JavaScript typed arrays.
 */
public class HtmlInternalTransform extends AbstractTransform implements InternalTransform {
  /** The identity transform, don't modify it! */
  public static final HtmlInternalTransform IDENTITY = new HtmlInternalTransform();

  /** Creates an identity transform. */
  public HtmlInternalTransform() {
    this(new float[] {1, 0, 0, 1, 0, 0});
  }

  // Pythagoras Transform implementation

  @Override
  public float uniformScale() {
    // the square root of the signed area of the parallelogram spanned by the axis vectors
    float cp = m00() * m11() - m01() * m10();
    return (cp < 0f) ? -FloatMath.sqrt(-cp) : FloatMath.sqrt(cp);
  }

  @Override
  public float scaleX() {
    return FloatMath.sqrt(m00() * m00() + m01() * m01());
  }

  @Override
  public float scaleY() {
    return FloatMath.sqrt(m10() * m10() + m11() * m11());
  }

  @Override
  public float rotation() {
    // use the iterative polar decomposition algorithm described by Ken Shoemake:
    // http://www.cs.wisc.edu/graphics/Courses/838-s2002/Papers/polar-decomp.pdf

    // start with the contents of the upper 2x2 portion of the matrix
    float n00 = m00(), n10 = m10();
    float n01 = m01(), n11 = m11();
    for (int ii = 0; ii < 10; ii++) {
      // store the results of the previous iteration
      float o00 = n00, o10 = n10;
      float o01 = n01, o11 = n11;

      // compute average of the matrix with its inverse transpose
      float det = o00 * o11 - o10 * o01;
      if (Math.abs(det) == 0f) {
        // determinant is zero; matrix is not invertible
        throw new NoninvertibleTransformException(this.toString());
      }
      float hrdet = 0.5f / det;
      n00 = +o11 * hrdet + o00 * 0.5f;
      n10 = -o01 * hrdet + o10 * 0.5f;

      n01 = -o10 * hrdet + o01 * 0.5f;
      n11 = +o00 * hrdet + o11 * 0.5f;

      // compute the difference; if it's small enough, we're done
      float d00 = n00 - o00, d10 = n10 - o10;
      float d01 = n01 - o01, d11 = n11 - o11;
      if (d00 * d00 + d10 * d10 + d01 * d01 + d11 * d11 < FloatMath.EPSILON) {
        break;
      }
    }
    // now that we have a nice orthogonal matrix, we can extract the rotation
    return FloatMath.atan2(n01, n00);
  }

  @Override
  public float tx() {
    return matrix.get(4);
  }

  @Override
  public float ty() {
    return matrix.get(5);
  }

  @Override
  public Transform setUniformScale(float scale) {
    return setScale(scale, scale);
  }

  @Override
  public Transform setScaleX(float scaleX) {
    // normalize the scale to 1, then re-apply
    float osx = scaleX();
    setM00((m00() / osx) * scaleX);
    setM01((m01() / osx) * scaleX);
    return this;
  }

  @Override
  public Transform setScaleY(float scaleY) {
    // normalize the scale to 1, then re-apply
    float osy = scaleY();
    setM10((m10() / osy) * scaleY);
    setM11((m11() / osy) * scaleY);
    return this;
  }

  @Override
  public Transform setRotation(float angle) {
    // extract the scale, then reapply rotation and scale together
    float sx = scaleX(), sy = scaleY();
    float sina = FloatMath.sin(angle), cosa = FloatMath.cos(angle);
    setM00(cosa * sx);
    setM01(sina * sx);
    setM10(-sina * sy);
    setM11(cosa * sy);
    return this;
  }

  @Override
  public Transform setTranslation(float tx, float ty) {
    setTx(tx);
    setTy(ty);
    return this;
  }

  @Override
  public Transform setTx(float tx) {
    matrix.set(4, tx);
    return this;
  }

  @Override
  public Transform setTy(float ty) {
    matrix.set(5, ty);
    return this;
  }

  @Override
  public Transform setTransform(float m00, float m01, float m10, float m11, float tx, float ty) {
    matrix.set(new float[] {m00, m01, m10, m11, tx, ty});
    return this;
  }

  @Override
  public Transform uniformScale(float scale) {
    return scale(scale, scale);
  }

  @Override
  public Transform scaleX(float scaleX) {
    setM00(m00() * scaleX);
    setM01(m01() * scaleX);
    setTx(tx() * scaleX);
    return this;
  }

  @Override
  public Transform scaleY(float scaleY) {
    setM10(m10() * scaleY);
    setM11(m11() * scaleY);
    setTy(ty() * scaleY);
    return this;
  }

  @Override
  public Transform rotate(float angle) {
    float sina = FloatMath.sin(angle), cosa = FloatMath.cos(angle);
    return multiply(cosa, sina, -sina, cosa, 0, 0, this, this);
  }

  @Override
  public Transform translate(float tx, float ty) {
    return multiply(this, 1, 0, 0, 1, tx, ty, this);
  }

  @Override
  public Transform translateX(float tx) {
    return multiply(this, 1, 0, 0, 1, tx, 0, this);
  }

  @Override
  public Transform translateY(float ty) {
    return multiply(this, 1, 0, 0, 1, 0, ty, this);
  }

  @Override
  public Transform invert() {
    float m00 = m00(), m01 = m01(), m10 = m10(), m11 = m11(), tx = tx(), ty = ty();
    float det = m00 * m11 - m10 * m01;
    if (Math.abs(det) == 0f) {
      // determinant is zero; matrix is not invertible
      throw new NoninvertibleTransformException(this.toString());
    }
    float rdet = 1f / det;
    return new HtmlInternalTransform(new float[] {
      +m11 * rdet,                  -m10 * rdet,
      -m01 * rdet,                  +m00 * rdet,
      (m10 * ty - m11 * tx) * rdet, (m01 * tx - m00 * ty) * rdet
    });
  }

  @Override
  public Transform concatenate(Transform other) {
    if (other instanceof HtmlInternalTransform) {
      return multiply(this, (HtmlInternalTransform) other, new HtmlInternalTransform());
    } else {
      HtmlInternalTransform oaff = new HtmlInternalTransform(other);
      return multiply(this, oaff, oaff);
    }
  }

  @Override
  public Transform preConcatenate(Transform other) {
    if (other instanceof HtmlInternalTransform) {
      return multiply((HtmlInternalTransform) other, this, new HtmlInternalTransform());
    } else {
      HtmlInternalTransform oaff = new HtmlInternalTransform(other);
      return multiply(oaff, this, oaff);
    }
  }

  @Override
  public Transform lerp(Transform other, float t) {
    HtmlInternalTransform o = (other instanceof HtmlInternalTransform)
        ? (HtmlInternalTransform) other : new HtmlInternalTransform(other);
    float m00 = m00(), m01 = m01(), m10 = m10(), m11 = m11(), tx = tx(), ty = ty();
    float o00 = o.m00(), o01 = o.m01(), o10 = o.m10(), o11 = o.m11(), otx = o.tx(), oty = o.ty();
    return new HtmlInternalTransform(new float[] {
      m00 + t * (o00 - m00), m01 + t * (o01 - m01),
      m10 + t * (o10 - m10), m11 + t * (o11 - m11),
      tx  + t * (otx -  tx),  ty + t * (oty -  ty)
    });
  }

  @Override
  public Point transform(IPoint p, Point into) {
    float x = p.x(), y = p.y();
    return into.set(m00() * x + m10() * y + tx(), m01() * x + m11() * y + ty());
  }

  @Override
  public void transform(IPoint[] src, int srcOff, Point[] dst, int dstOff, int count) {
    for (int ii = 0; ii < count; ii++) {
      transform(src[srcOff++], dst[dstOff++]);
    }
  }

  @Override
  public void transform(float[] src, int srcOff, float[] dst, int dstOff, int count) {
    for (int ii = 0; ii < count; ii++) {
      float x = src[srcOff++], y = src[srcOff++];
      dst[dstOff++] = m00() * x + m10() * y + tx();
      dst[dstOff++] = m01() * x + m11() * y + ty();
    }
  }

  @Override
  public Point inverseTransform(IPoint p, Point into) {
    float m00 = m00(), m01 = m01(), m10 = m10(), m11 = m11();
    float x = p.x() - tx(), y = p.y() - ty();
    float det = m00 * m11 - m01 * m10;
    if (Math.abs(det) == 0f) {
      // determinant is zero; matrix is not invertible
      throw new NoninvertibleTransformException(this.toString());
    }
    float rdet = 1 / det;
    return into.set((x * m11 - y * m10) * rdet, (y * m00 - x * m01) * rdet);
  }

  @Override
  public Vector transform(IVector v, Vector into) {
    float x = v.x(), y = v.y();
    return into.set(m00() * x + m10() * y, m01() * x + m11() * y);
  }

  @Override
  public Vector inverseTransform(IVector v, Vector into) {
    float m00 = m00(), m01 = m01(), m10 = m10(), m11 = m11();
    float x = v.x(), y = v.y();
    float det = m00 * m11 - m01 * m10;
    if (Math.abs(det) == 0f) {
      // determinant is zero; matrix is not invertible
      throw new NoninvertibleTransformException(this.toString());
    }
    float rdet = 1 / det;
    return into.set((x * m11 - y * m10) * rdet, (y * m00 - x * m01) * rdet);
  }

  @Override
  public Transform clone() {
    return new HtmlInternalTransform(Float32Array.create(matrix));
  }

  @Override
  public int generality() {
    return AffineTransform.GENERALITY;
  }

  @Override
  public String toString() {
    return "affine [" + FloatMath.toString(m00()) + " " + FloatMath.toString(m01()) + " "
        + FloatMath.toString(m10()) + " " + FloatMath.toString(m11()) + " " + translation() + "]";
  }

  // PlayN InternalLayer implementation

  public Float32Array matrix() {
    return matrix;
  }

  @Override
  public float m00() {
    return matrix.get(0);
  }

  @Override
  public float m01() {
    return matrix.get(1);
  }

  @Override
  public float m10() {
    return matrix.get(2);
  }

  @Override
  public float m11() {
    return matrix.get(3);
  }

  @Override
  public InternalTransform set(Transform other) {
    matrix.set(((HtmlInternalTransform) other).matrix);
    return this;
  }

  @Override
  public InternalTransform concatenate(Transform other, float originX, float originY) {
    HtmlInternalTransform ot = (HtmlInternalTransform) other;
    return concatenate(ot.m00(), ot.m01(), ot.m10(), ot.m11(), ot.tx(), ot.ty(), originX, originY);
  }

  @Override
  public InternalTransform concatenate(float m00, float m01, float m10, float m11, float tx,
      float ty, float originX, float originY) {
    translate(originX, originY);
    multiply(this, m00, m01, m10, m11, tx - originX, ty - originY, this);
    translate(-originX, -originY);
    return this;
  }

  // private bits

  private Float32Array matrix;

  private HtmlInternalTransform(Transform other) {
    float scaleX = other.scaleX(), scaleY = other.scaleY(), angle = other.rotation();
    float sina = FloatMath.sin(angle), cosa = FloatMath.cos(angle);
    setM00(cosa * scaleX);
    setM01(sina * scaleY);
    setM10(-sina * scaleX);
    setM11(cosa * scaleY);
    setTx(other.tx());
    setTy(other.ty());
  }

  private HtmlInternalTransform(float[] matrix) {
    this(Float32Array.create(matrix));
  }

  private HtmlInternalTransform(Float32Array matrix) {
    this.matrix = matrix;
  }

  private void setM00(float value) {
    matrix.set(0, value);
  }

  private void setM01(float value) {
    matrix.set(1, value);
  }

  private void setM10(float value) {
    matrix.set(2, value);
  }

  private void setM11(float value) {
    matrix.set(3, value);
  }

  private static HtmlInternalTransform multiply(HtmlInternalTransform a, HtmlInternalTransform b,
      HtmlInternalTransform into) {
    return multiply(a.m00(), a.m01(), a.m10(), a.m11(), a.tx(), a.ty(),
        b.m00(), b.m01(), b.m10(), b.m11(), b.tx(), b.ty(), into);
  }

  private static HtmlInternalTransform multiply(HtmlInternalTransform a, float m00, float m01,
      float m10, float m11, float tx, float ty, HtmlInternalTransform into) {
    return multiply(a.m00(), a.m01(), a.m10(), a.m11(), a.tx(), a.ty(), m00, m01, m10, m11, tx, ty,
        into);
  }

  private static HtmlInternalTransform multiply(float m00, float m01, float m10, float m11,
      float tx, float ty, HtmlInternalTransform b, HtmlInternalTransform into) {
    return multiply(m00, m01, m10, m11, tx, ty, b.m00(), b.m01(), b.m10(), b.m11(), b.tx(), b.ty(),
        into);
  }

  private static HtmlInternalTransform multiply(float am00, float am01, float am10, float am11,
      float atx, float aty, float bm00, float bm01, float bm10, float bm11, float btx, float bty,
      HtmlInternalTransform into) {
    into.setTransform(
      am00 * bm00 + am10 * bm01, am01 * bm00 + am11 * bm01,
      am00 * bm10 + am10 * bm11, am01 * bm10 + am11 * bm11,
      am00 * btx + am10 * bty + atx, am01 * btx + am11 * bty + aty);
    return into;
  }
}
