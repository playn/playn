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
package forplay.android;

import java.util.LinkedList;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import forplay.core.Asserts;
import forplay.core.Gradient;
import forplay.core.Image;
import forplay.core.Path;
import forplay.core.Pattern;

class AndroidCanvas implements forplay.core.Canvas {
  private static Matrix m = new Matrix();
  private static Rect rect = new Rect();
  private static RectF rectf = new RectF();

  private final Canvas canvas;
  private LinkedList<AndroidSurfaceState> paintStack = new LinkedList<AndroidSurfaceState>();

  AndroidCanvas(Canvas canvas) {
    this.canvas = canvas;
    paintStack.addFirst(new AndroidSurfaceState());
  }

  @Override
  public void clear() {
    canvas.drawColor(0, PorterDuff.Mode.SRC);
  }

  @Override
  public void clip(Path clipPath) {
    Asserts.checkArgument(clipPath instanceof AndroidPath);
    canvas.clipPath(((AndroidPath) clipPath).path);
  }

  @Override
  public void drawImage(Image img, float x, float y) {
    Asserts.checkArgument(img instanceof AndroidImage);
    AndroidImage aimg = (AndroidImage) img;
    if (aimg.getBitmap() != null) {
      canvas.drawBitmap(aimg.getBitmap(), x, y, currentState().prepareImage());
    }
  }

  @Override
  public void drawImage(Image img, float x, float y, float w, float h) {
    Asserts.checkArgument(img instanceof AndroidImage);
    AndroidImage aimg = (AndroidImage) img;
    if (aimg.getBitmap() != null) {
      rectf.set(x, y, x + w, y + h);
      canvas.drawBitmap(aimg.getBitmap(), null, rectf, currentState().prepareImage());
    }
  }

  @Override
  public void drawImage(Image img, float dx, float dy, float dw, float dh, float sx, float sy,
      float sw, float sh) {
    Asserts.checkArgument(img instanceof AndroidImage);
    AndroidImage aimg = (AndroidImage) img;
    if (aimg.getBitmap() != null) {
      rect.set((int) sx, (int) sy, (int) (sx + sw), (int) (sy + sh));
      rectf.set(dx, dy, dx + dw, dy + dh);
      canvas.drawBitmap(aimg.getBitmap(), rect, rectf, currentState().prepareImage());
    }
  }

  @Override
  public void drawImageCentered(Image image, float dx, float dy) {
    drawImage(image, dx - image.width() / 2, dy - image.height() / 2);
  }

  @Override
  public void drawLine(float x0, float y0, float x1, float y1) {
    canvas.drawLine(x0, y0, x1, y1, currentState().prepareStroke());
  }

  @Override
  public void drawPoint(float x, float y) {
    canvas.drawPoint(x, y, currentState().prepareStroke());
  }

  @Override
  public void drawText(String text, float x, float y) {
    canvas.drawText(text, x, y, currentState().prepareFill());
  }

  @Override
  public void fillCircle(float x, float y, float radius) {
    canvas.drawCircle(x, y, radius, currentState().prepareFill());
  }

  @Override
  public void fillPath(Path path) {
    Asserts.checkArgument(path instanceof AndroidPath);
    canvas.drawPath(((AndroidPath) path).path, currentState().prepareFill());
  }

  @Override
  public void fillRect(float x, float y, float width, float height) {
    float left = x;
    float top = y;
    float right = left + width;
    float bottom = top + height;
    canvas.drawRect(left, top, right, bottom, currentState().prepareFill());
  }

  @Override
  public int height() {
    return canvas.getHeight();
  }

  @Override
  public void restore() {
    canvas.restore();
    paintStack.removeFirst();

    Asserts.check(paintStack.size() > 0, "Unbalanced save/restore");
  }

  @Override
  public void rotate(float angle) {
    canvas.rotate(rad2deg(angle));
  }

  @Override
  public void save() {
    canvas.save();
    paintStack.addFirst(new AndroidSurfaceState(currentState()));
  }

  @Override
  public void scale(float x, float y) {
    canvas.scale(x, y);
  }

  public void setAlpha(float alpha) {
    currentState().setAlpha(alpha);
  }

  public float alpha() {
    return currentState().alpha;
  }

  @Override
  public void setCompositeOperation(Composite composite) {
    currentState().setCompositeOperation(composite);
  }

  @Override
  public void setFillColor(int color) {
    currentState().setFillColor(color);
  }

  @Override
  public void setFillGradient(Gradient gradient) {
    Asserts.checkArgument(gradient instanceof AndroidGradient);
    currentState().setFillGradient((AndroidGradient) gradient);
  }

  @Override
  public void setFillPattern(Pattern pattern) {
    Asserts.checkArgument(pattern instanceof AndroidPattern);
    currentState().setFillPattern((AndroidPattern) pattern);
  }

  @Override
  public void setLineCap(LineCap cap) {
    currentState().setLineCap(cap);
  }

  @Override
  public void setLineJoin(LineJoin join) {
    currentState().setLineJoin(join);
  }

  @Override
  public void setMiterLimit(float miter) {
    currentState().setMiterLimit(miter);
  }

  @Override
  public void setStrokeColor(int color) {
    currentState().setStrokeColor(color);
  }

  @Override
  public void setStrokeWidth(float strokeWidth) {
    currentState().setStrokeWidth(strokeWidth);
  }

  @Override
  public void setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    Matrix m = new Matrix();
    // TODO(jgw): Is this the right order?
    m.setValues(new float[] {m11, m12, 0, m21, 0, m22, dx, dy, 1});
    canvas.setMatrix(m);
  }

  @Override
  public void strokeCircle(float x, float y, float radius) {
    canvas.drawCircle(x, y, radius, currentState().prepareStroke());
  }

  @Override
  public void strokePath(Path path) {
    Asserts.checkArgument(path instanceof AndroidPath);
    canvas.drawPath(((AndroidPath) path).path, currentState().prepareStroke());
  }

  @Override
  public void strokeRect(float x, float y, float width, float height) {
    float left = x;
    float top = y;
    float right = left + width;
    float bottom = top + height;
    canvas.drawRect(left, top, right, bottom, currentState().prepareStroke());
  }

  @Override
  public void transform(float m11, float m12, float m21, float m22, float dx, float dy) {
    m.setValues(new float[] {m11, m21, dx, m12, m22, dy, 0, 0, 1});
    canvas.concat(m);
  }

  @Override
  public void translate(float x, float y) {
    canvas.translate(x, y);
  }

  @Override
  public int width() {
    return canvas.getWidth();
  }

  private AndroidSurfaceState currentState() {
    return paintStack.peek();
  }

  private float rad2deg(double deg) {
    return (float) (deg * 360 / (2 * Math.PI));
  }
}
