/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.core;

/**
 * TODO
 */
public class Transform {

  /**
   * TODO
   */
  public static final Transform IDENTITY = new Transform(1, 0, 0, 1, 0, 0);

  private float m00, m01, m10, m11;
  private float tx, ty;

  /**
   * TODO
   */
  public Transform() {
    setIdentity();
  }

  /**
   * TODO
   */
  public Transform(float m00, float m01, float m10, float m11, float tx, float ty) {
    set(m00, m01, m10, m11, tx, ty);
  }

  /**
   * TODO
   */
  public Transform(Transform toCopy) {
    copy(toCopy);
  }

  /**
   * TODO
   */
  public void copy(Transform toCopy) {
    m00 = toCopy.m00; m01 = toCopy.m01;
    m10 = toCopy.m10; m11 = toCopy.m11;
    tx  = toCopy.tx; ty  = toCopy.ty;
  }

  /**
   * TODO
   */
  public float m00() {
    return m00;
  }

  /**
   * TODO
   */
  public float m01() {
    return m01;
  }

  /**
   * TODO
   */
  public float m10() {
    return m10;
  }

  /**
   * TODO
   */
  public float m11() {
    return m11;
  }

  /**
   * TODO
   */
  public void rotate(float angle) {
    float sr = (float) Math.sin(angle);
    float cr = (float) Math.cos(angle);
    transform(cr, sr, -sr, cr, 0, 0);
  }

  /**
   * TODO
   */
  public void scale(float sx, float sy) {
    m00 *= sx; m10 *= sy;
    m01 *= sx; m11 *= sy;
    tx  *= sx; ty  *= sy;
  }

  /**
   * TODO
   */
  public void set(float m00, float m01, float m10, float m11, float tx, float ty) {
    this.m00 = m00; this.m01 = m01;
    this.m10 = m10; this.m11 = m11;
    this.tx  = tx; this.ty  = ty;
  }

  /**
   * TODO
   */
  public void setIdentity() {
    m00 = 1; m10 = 0;
    m01 = 0; m11 = 1;
    tx  = 0; ty  = 0;
  }

  /**
   * TODO
   */
  public void setM00(float m00) {
    this.m00 = m00;
  }

  /**
   * TODO
   */
  public void setM01(float m01) {
    this.m01 = m01;
  }

  /**
   * TODO
   */
  public void setM10(float m10) {
    this.m10 = m10;
  }

  /**
   * TODO
   */
  public void setM11(float m11) {
    this.m11 = m11;
  }

  /**
   * TODO
   */
  public void setTx(float tx) {
    this.tx = tx;
  }

  /**
   * TODO
   */
  public void setTy(float ty) {
    this.ty = ty;
  }

  /**
   * TODO
   */
  public void setTranslation(float tx, float ty) {
    setTx(tx);
    setTy(ty);
  }

  /**
   * TODO
   * 
   * TODO: This method does not preserve shear. Is that worth the trouble?
   */
  public void setRotation(float angle) {
    // Decompose scale.
    float sx = len(m00, m01);
    float sy = len(m10, m11);

    // Apply rotation and scale together.
    float sr = (float) Math.sin(angle);
    float cr = (float) Math.cos(angle);
    m00 = cr * sx;  m01 = sr * sx;
    m10 = -sr * sy; m11 = cr * sy;
  }

  /**
   * TODO
   */
  public void setScale(float s) {
    setScale(s, s);
  }

  /**
   * TODO
   */
  public void setScale(float sx, float sy) {
    // Normalize to scale = 1.
    float osx = len(m00, m01);
    float osy = len(m10, m11);
    m00 /= osx; m01 /= osx;
    m10 /= osy; m11 /= osy;

    // Then re-apply.
    m00 *= sx; m01 *= sx;
    m10 *= sy; m11 *= sy;
  }

  @Override
  public String toString() {
    return "[" +
      m00() + " " + m01() + "\n " +
      m10() + " " + m11() + "\n " +
      tx() + " " + ty() +
    "]";
  }

  /**
   * TODO
   */
  public void transform(float m00, float m01, float m10, float m11, float tx, float ty) {
    transform(m00, m01, m10, m11, tx, ty, this);
  }

  /**
   * TODO
   */
  public void transform(float m00, float m01, float m10, float m11, float tx, float ty,
      Transform out) {
    float out00 = this.m00 * m00 + this.m10 * m01, out10 = this.m00 * m10 + this.m10 * m11;
    float out01 = this.m01 * m00 + this.m11 * m01, out11 = this.m01 * m10 + this.m11 * m11;

    float outx = this.m00 * tx + this.m10 * ty + this.tx;
    float outy = this.m01 * tx + this.m11 * ty + this.ty;

    out.m00 = out00; out.m01 = out01;
    out.m10 = out10; out.m11 = out11;
    out.tx = outx; out.ty = outy;
  }

  /**
   * TODO
   */
  public void transform(Transform t) {
    transform(t.m00, t.m01, t.m10, t.m11, t.tx, t.ty);
  }

  /**
   * TODO
   */
  public void transform(Transform t, Transform out) {
    transform(t.m00, t.m01, t.m10, t.m11, t.tx, t.ty, out);
  }

  /**
   * TODO
   */
  public void translate(float x, float y) {
    transform(1, 0, 0, 1, x, y);
  }

  /**
   * TODO
   */
  public float tx() {
    return tx;
  }

  /**
   * TODO
   */
  public float ty() {
    return ty;
  }

  private float len(float x, float y) {
    return (float) Math.sqrt(x * x + y * y);
  }
}
