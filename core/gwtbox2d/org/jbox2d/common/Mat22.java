/*******************************************************************************
 * Copyright (c) 2011, Daniel Murphy All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the <organization> nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL DANIEL MURPHY BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/*
 * JBox2D - A Java Port of Erin Catto's Box2D
 * 
 * JBox2D homepage: http://jbox2d.sourceforge.net/ Box2D homepage:
 * http://www.box2d.org
 * 
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not claim
 * that you wrote the original software. If you use this software in a product,
 * an acknowledgment in the product documentation would be appreciated but is
 * not required. 2. Altered source versions must be plainly marked as such, and
 * must not be misrepresented as being the original software. 3. This notice may
 * not be removed or altered from any source distribution.
 */
/*
 * Modified for gwtbox2d by removing 2 vectors and just using 4 floats (m11, m12,
 * m21, m22)
 */
package org.jbox2d.common;

// updated to rev 100

/**
 * A 2-by-2 matrix. Stored in column-major order.
 */
public class Mat22 {

  public float m11, m12, m21, m22;

  /** Convert the matrix to printable format. */
  @Override
  public String toString() {
    String s = "";
    s += "[" + m11 + "," + m21 + "]\n";
    s += "[" + m12 + "," + m22 + "]";
    return s;
  }

  /**
   * Construct zero matrix. Note: this is NOT an identity matrix! djm fixed
   * double allocation problem
   */
  public Mat22() {
  }

  /**
   * Create a matrix with given vectors as columns.
   * 
   * @param c1 Column 1 of matrix
   * @param c2 Column 2 of matrix
   */
  public Mat22(final Vec2 c1, final Vec2 c2) {
    m11 = c1.x;
    m12 = c1.y;
    m21 = c2.x;
    m22 = c2.y;
  }

  /**
   * Create a matrix from four floats.
   * 
   * @param col1x
   * @param col2x
   * @param col1y
   * @param col2y
   */
  public Mat22(final float col1x, final float col2x, final float col1y, final float col2y) {
    m11 = col1x;
    m12 = col1y;
    m21 = col2x;
    m22 = col2y;
  }

  /**
   * Set as a copy of another matrix.
   * 
   * @param m Matrix to copy
   */
  public final Mat22 set(final Mat22 m) {
    m11 = m.m11;
    m12 = m.m12;
    m21 = m.m21;
    m22 = m.m22;
    return this;
  }

  public final Mat22 set(final float col1x, final float col2x, final float col1y, final float col2y) {
    m11 = col1x;
    m12 = col1y;
    m21 = col2x;
    m22 = col2y;
    return this;
  }

  /**
   * Return a clone of this matrix. djm fixed double allocation
   */
  // @Override // changed for forplay
  public final Mat22 clone() {
    return new Mat22(m11, m21, m12, m22);
  }

  /**
   * Set as a matrix representing a rotation.
   * 
   * @param angle Rotation (in radians) that matrix represents.
   */
  public final void set(final float angle) {
    final float c = (float) Math.cos(angle);
    final float s = (float) Math.sin(angle);
    m11 = c;
    m21 = -s;
    m12 = s;
    m22 = c;
  }

  /**
   * Set as the identity matrix.
   */
  public final void setIdentity() {
    m11 = 1f;
    m21 = 0f;
    m12 = 0f;
    m22 = 1f;
  }

  /**
   * Set as the zero matrix.
   */
  public final void setZero() {
    m11 = 0f;
    m12 = 0f;
    m21 = 0f;
    m22 = 0f;
  }

  /**
   * Extract the angle from this matrix (assumed to be a rotation matrix).
   * 
   * @return
   */
  public final float getAngle() {
    return (float) Math.atan2(m12, m11);
  }

  /**
   * Set by column vectors.
   * 
   * @param c1 Column 1
   * @param c2 Column 2
   */
  public final void set(final Vec2 c1, final Vec2 c2) {
    m11 = c1.x;
    m21 = c2.x;
    m12 = c1.y;
    m22 = c2.y;
  }

  /** Returns the inverted Mat22 - does NOT invert the matrix locally! */
  public final Mat22 invert() {
    final float a = m11;
    final float b = m21;
    final float c = m12;
    final float d = m22;
    float det = a * d - b * c;
    if (det != 0) {
      det = 1f / det;
    }
    return new Mat22(det * d, -det * b, -det * c, det * a);
  }

  public final Mat22 invertLocal() {
    final float a = m11;
    final float b = m21;
    final float c = m12;
    final float d = m22;
    float det = a * d - b * c;
    if (det != 0) {
      det = 1f / det;
    }
    m11 = det * d;
    m21 = -det * b;
    m12 = -det * c;
    m22 = det * a;
    return this;
  }

  public final void invertToOut(final Mat22 out) {
    final float a = m11;
    final float b = m21;
    final float c = m12;
    final float d = m22;
    float det = a * d - b * c;
    if (det != 0) {
      det = 1f / det;
    }
    out.m11 = det * d;
    out.m21 = -det * b;
    out.m12 = -det * c;
    out.m22 = det * a;
  }

  /**
   * Return the matrix composed of the absolute values of all elements. djm:
   * fixed double allocation
   * 
   * @return Absolute value matrix
   */
  public final Mat22 abs() {
    return new Mat22(Math.abs(m11), Math.abs(m21), Math.abs(m12), Math.abs(m22));
  }

  /* djm: added */
  public final void absLocal() {
    m11 = Math.abs(m11);
    m12 = Math.abs(m12);
    m21 = Math.abs(m21);
    m22 = Math.abs(m22);
  }

  /**
   * Return the matrix composed of the absolute values of all elements.
   * 
   * @return Absolute value matrix
   */
  public final static Mat22 abs(final Mat22 R) {
    return R.abs();
  }

  /* djm created */
  public static void absToOut(final Mat22 R, final Mat22 out) {
    out.m11 = R.m11;
    out.m12 = R.m12;
    out.m21 = R.m21;
    out.m22 = R.m22;
  }

  /**
   * Multiply a vector by this matrix.
   * 
   * @param v Vector to multiply by matrix.
   * @return Resulting vector
   */
  public final Vec2 mul(final Vec2 v) {
    return new Vec2(m11 * v.x + m21 * v.y, m12 * v.x + m22 * v.y);
  }

  /* djm added */
  public final void mulToOut(final Vec2 v, final Vec2 out) {
    final float tempy = m12 * v.x + m22 * v.y;
    out.x = m11 * v.x + m21 * v.y;
    out.y = tempy;
  }

  /**
   * Multiply another matrix by this one (this one on left). djm optimized
   * 
   * @param R
   * @return
   */
  public final Mat22 mul(final Mat22 R) {
    /*
     * Mat22 C = new Mat22();C.set(this.mul(R.col1), this.mul(R.col2));return C;
     */
    final Mat22 C =
        new Mat22(m11 * R.m11 + m21 * R.m12, m11 * R.m21 + m21 * R.m22, m12 * R.m11 + m22 * R.m12,
            m12 * R.m21 + m22 * R.m22);
    return C;
  }

  public final Mat22 mulLocal(final Mat22 R) {
    mulToOut(R, this);
    return this;
  }

  /* djm: created */
  public final void mulToOut(final Mat22 R, final Mat22 out) {
    final float tempy1 = this.m12 * R.m11 + this.m22 * R.m12;
    final float tempx1 = this.m11 * R.m11 + this.m21 * R.m12;
    out.m11 = tempx1;
    out.m12 = tempy1;
    final float tempy2 = this.m12 * R.m21 + this.m22 * R.m22;
    final float tempx2 = this.m11 * R.m21 + this.m21 * R.m22;
    out.m21 = tempx2;
    out.m22 = tempy2;
  }

  /**
   * Multiply another matrix by the transpose of this one (transpose of this one
   * on left). djm: optimized
   * 
   * @param B
   * @return
   */
  public final Mat22 mulTrans(final Mat22 B) {
    /*
     * Vec2 c1 = new Vec2(Vec2.dot(this.col1, B.col1), Vec2.dot(this.col2,
     * B.col1)); Vec2 c2 = new Vec2(Vec2.dot(this.col1, B.col2),
     * Vec2.dot(this.col2, B.col2)); Mat22 C = new Mat22(); C.set(c1, c2);
     * return C;
     */

    return new Mat22(m11 * B.m11 + m12 * B.m12, m11 * B.m21 + m12 * B.m22, m21 * B.m11 + m22
        * B.m12, m21 * B.m21 + m22 * B.m22);
  }

  public final Mat22 mulTransLocal(final Mat22 B) {
    mulTransToOut(B, this);
    return this;
  }

  /* djm added */
  public final void mulTransToOut(final Mat22 B, final Mat22 out) {
    /*
     * out.col1.x = Vec2.dot(this.col1, B.col1); out.col1.y =
     * Vec2.dot(this.col2, B.col1); out.col2.x = Vec2.dot(this.col1, B.col2);
     * out.col2.y = Vec2.dot(this.col2, B.col2);
     */
    out.m11 = this.m11 * B.m11 + this.m12 * B.m12;
    out.m12 = this.m21 * B.m11 + this.m22 * B.m12;
    out.m21 = this.m11 * B.m21 + this.m12 * B.m22;
    out.m22 = this.m21 * B.m21 + this.m22 * B.m22;
  }

  /**
   * Multiply a vector by the transpose of this matrix.
   * 
   * @param v
   * @return
   */
  public final Vec2 mulTrans(final Vec2 v) {
    // return new Vec2(Vec2.dot(v, col1), Vec2.dot(v, col2));
    return new Vec2((v.x * m11 + v.y * m12), (v.x * m21 + v.y * m22));
  }

  /* djm added */
  public final void mulTransToOut(final Vec2 v, final Vec2 out) {
    /*
     * out.x = Vec2.dot(v, col1); out.y = Vec2.dot(v, col2);
     */
    out.x = v.x * m11 + v.y * m12;
    out.y = v.x * m21 + v.y * m22;
  }

  /**
   * Add this matrix to B, return the result.
   * 
   * @param B
   * @return
   */
  public final Mat22 add(final Mat22 B) {
    // return new Mat22(col1.add(B.col1), col2.add(B.col2));
    return new Mat22(m11 + B.m11, m21 + B.m21, m12 + B.m12, m22 + B.m22);
  }

  /**
   * Add B to this matrix locally.
   * 
   * @param B
   * @return
   */
  public final Mat22 addLocal(final Mat22 B) {
    // col1.addLocal(B.col1);
    // col2.addLocal(B.col2);
    m11 += B.m11;
    m12 += B.m12;
    m21 += B.m21;
    m22 += B.m22;
    return this;
  }

  /**
   * Solve A * x = b where A = this matrix.
   * 
   * @return The vector x that solves the above equation.
   */
  public final Vec2 solve(final Vec2 b) {
    final float a11 = m11;
    final float a12 = m21;
    final float a21 = m12;
    final float a22 = m22;
    float det = a11 * a22 - a12 * a21;
    if (det != 0.0f) {
      det = 1.0f / det;
    }
    final Vec2 x = new Vec2(det * (a22 * b.x - a12 * b.y), det * (a11 * b.y - a21 * b.x));
    return x;
  }

  /* djm added */
  public final void solveToOut(final Vec2 b, final Vec2 out) {
    final float a11 = m11;
    final float a12 = m21;
    final float a21 = m12;
    final float a22 = m22;
    float det = a11 * a22 - a12 * a21;
    if (det != 0.0f) {
      det = 1.0f / det;
    }
    final float tempy = det * (a11 * b.y - a21 * b.x);
    out.x = det * (a22 * b.x - a12 * b.y);
    out.y = tempy;
  }

  public final static Vec2 mul(final Mat22 R, final Vec2 v) {
    // return R.mul(v);
    return new Vec2(R.m11 * v.x + R.m21 * v.y, R.m12 * v.x + R.m22 * v.y);
  }

  /* djm added */
  public final static void mulToOut(final Mat22 R, final Vec2 v, final Vec2 out) {
    // R.mulToOut(v,out);
    final float tempy = R.m12 * v.x + R.m22 * v.y;
    out.x = R.m11 * v.x + R.m21 * v.y;
    out.y = tempy;
  }

  public final static Mat22 mul(final Mat22 A, final Mat22 B) {
    // return A.mul(B);
    return new Mat22(A.m11 * B.m11 + A.m21 * B.m12, A.m11 * B.m21 + A.m21 * B.m22, A.m12 * B.m11
        + A.m22 * B.m12, A.m12 * B.m21 + A.m22 * B.m22);
  }

  /* djm added */
  public final static void mulToOut(final Mat22 A, final Mat22 B, final Mat22 out) {
    final float tempy1 = A.m12 * B.m11 + A.m22 * B.m12;
    final float tempx1 = A.m11 * B.m11 + A.m21 * B.m12;
    final float tempy2 = A.m12 * B.m21 + A.m22 * B.m22;
    final float tempx2 = A.m11 * B.m21 + A.m21 * B.m22;
    out.m11 = tempx1;
    out.m12 = tempy1;
    out.m21 = tempx2;
    out.m22 = tempy2;
  }

  public final static Vec2 mulTrans(final Mat22 R, final Vec2 v) {
    return new Vec2((v.x * R.m11 + v.y * R.m12), (v.x * R.m21 + v.y * R.m22));
    // return new Vec2((v.x * R.col1.x + v.y * R.col1.y), (v.x * R.col2.x + v.y
    // * R.col2.y));
  }

  /* djm added */
  public final static void mulTransToOut(final Mat22 R, final Vec2 v, final Vec2 out) {
    // R.mulTransToOut(v, out);
    float outx = v.x * R.m11 + v.y * R.m12;
    out.y = v.x * R.m21 + v.y * R.m22;
    out.x = outx;
  }

  public final static Mat22 mulTrans(final Mat22 A, final Mat22 B) {
    // return A.mulTrans(B);
    return new Mat22(A.m11 * B.m11 + A.m12 * B.m12, A.m11 * B.m21 + A.m12 * B.m22, A.m21 * B.m11
        + A.m22 * B.m12, A.m21 * B.m21 + A.m22 * B.m22);
  }

  /* djm added */
  public final static void mulTransToOut(final Mat22 A, final Mat22 B, final Mat22 out) {
    final float x1 = A.m11 * B.m11 + A.m12 * B.m12;
    final float y1 = A.m21 * B.m11 + A.m22 * B.m12;
    final float x2 = A.m11 * B.m21 + A.m12 * B.m22;
    final float y2 = A.m21 * B.m21 + A.m22 * B.m22;

    out.m11 = x1;
    out.m12 = y1;
    out.m21 = x2;
    out.m22 = y2;
  }

  public final static Mat22 createRotationalTransform(float angle) {
    final float c = (float) Math.cos(angle);
    final float s = (float) Math.sin(angle);
    return new Mat22(c, -s, s, c);
  }

  public final static void createRotationalTransform(float angle, Mat22 out) {
    final float c = (float) Math.cos(angle);
    final float s = (float) Math.sin(angle);
    out.m11 = c;
    out.m21 = -s;
    out.m12 = s;
    out.m22 = c;
  }

  public final static Mat22 createScaleTransform(float scale) {
    return new Mat22(scale, 0, 0, scale);
  }

  public final static void createScaleTransform(float scale, Mat22 out) {
    // TODO(pdr): shouldn't this explicitly set the off-diag to 0? This is
    // copied from the original box2d but I think it's a mistake.
    out.m11 = scale;
    out.m22 = scale;
  }
}
