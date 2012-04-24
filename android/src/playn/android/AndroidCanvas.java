/**
 * Copyright 2011 The PlayN Authors
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
package playn.android;

import java.util.LinkedList;

import playn.core.Asserts;
import playn.core.Canvas;
import playn.core.Gradient;
import playn.core.Image;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.TextLayout;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;

class AndroidCanvas implements Canvas {
  private static Matrix m = new Matrix();
  private static Rect rect = new Rect();
  private static RectF rectf = new RectF();

  private final android.graphics.Canvas canvas;
  private boolean dirty = true;

  private LinkedList<AndroidCanvasState> paintStack = new LinkedList<AndroidCanvasState>();

  AndroidCanvas(Bitmap bitmap) {
    canvas = new android.graphics.Canvas(bitmap);
    paintStack.addFirst(new AndroidCanvasState());
  }

  @Override
  public Canvas clear() {
    canvas.drawColor(0, PorterDuff.Mode.SRC);
    dirty = true;
    return this;
  }

  @Override
  public Canvas clip(Path clipPath) {
    Asserts.checkArgument(clipPath instanceof AndroidPath);
    canvas.clipPath(((AndroidPath) clipPath).path);
    return this;
  }

  @Override
  public Path createPath() {
    return new AndroidPath();
  }

  @Override
  public Canvas drawImage(Image img, float x, float y) {
    drawImage(img, x, y, img.width(), img.height());
    return this;
  }

  @Override
  public Canvas drawImage(Image img, float x, float y, float w, float h) {
    drawImage(img, x, y, w, h, 0, 0, img.width(), img.height());
    return this;
  }

  @Override
  public Canvas drawImage(Image img, float dx, float dy, float dw, float dh, float sx, float sy,
      float sw, float sh) {
    Asserts.checkArgument(img instanceof AndroidImage);
    rect.set((int) sx, (int) sy, (int) (sx + sw), (int) (sy + sh));
    rectf.set(dx, dy, dx + dw, dy + dh);
    canvas.drawBitmap(((AndroidImage) img).bitmap(), rect, rectf, currentState().prepareImage());
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawImageCentered(Image image, float dx, float dy) {
    drawImage(image, dx - image.width() / 2, dy - image.height() / 2);
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawLine(float x0, float y0, float x1, float y1) {
    canvas.drawLine(x0, y0, x1, y1, currentState().prepareStroke());
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawPoint(float x, float y) {
    canvas.drawPoint(x, y, currentState().prepareStroke());
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawText(String text, float x, float y) {
    canvas.drawText(text, x, y, currentState().prepareFill());
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawText(TextLayout layout, float x, float y) {
    ((AndroidTextLayout)layout).draw(canvas, x, y);
    dirty = true;
    return this;
  }

  @Override
  public Canvas fillCircle(float x, float y, float radius) {
    canvas.drawCircle(x, y, radius, currentState().prepareFill());
    dirty = true;
    return this;
  }

  @Override
  public Canvas fillPath(Path path) {
    Asserts.checkArgument(path instanceof AndroidPath);
    canvas.drawPath(((AndroidPath) path).path, currentState().prepareFill());
    dirty = true;
    return this;
  }

  @Override
  public Canvas fillRect(float x, float y, float width, float height) {
    float left = x;
    float top = y;
    float right = left + width;
    float bottom = top + height;
    canvas.drawRect(left, top, right, bottom, currentState().prepareFill());
    dirty = true;
    return this;
  }

  @Override
  public Canvas fillRoundRect(float x, float y, float width, float height, float radius) {
    rectf.set(x, y, width, height);
    canvas.drawRoundRect(rectf, radius, radius, currentState().prepareFill());
    dirty = true;
    return this;
  }

  @Override
  public int height() {
    return canvas.getHeight();
  }

  @Override
  public Canvas restore() {
    canvas.restore();
    paintStack.removeFirst();

    Asserts.check(paintStack.size() > 0, "Unbalanced save/restore");
    return this;
  }

  @Override
  public Canvas rotate(float angle) {
    canvas.rotate(rad2deg(angle));
    return this;
  }

  @Override
  public Canvas save() {
    canvas.save();
    paintStack.addFirst(new AndroidCanvasState(currentState()));
    return this;
  }

  @Override
  public Canvas scale(float x, float y) {
    canvas.scale(x, y);
    return this;
  }

  @Override
  public Canvas setAlpha(float alpha) {
    currentState().setAlpha(alpha);
    return this;
  }

  public float alpha() {
    return currentState().alpha;
  }

  @Override
  public Canvas setCompositeOperation(Composite composite) {
    currentState().setCompositeOperation(composite);
    return this;
  }

  @Override
  public Canvas setFillColor(int color) {
    currentState().setFillColor(color);
    return this;
  }

  @Override
  public Canvas setFillGradient(Gradient gradient) {
    Asserts.checkArgument(gradient instanceof AndroidGradient);
    currentState().setFillGradient((AndroidGradient) gradient);
    return this;
  }

  @Override
  public Canvas setFillPattern(Pattern pattern) {
    Asserts.checkArgument(pattern instanceof AndroidPattern);
    currentState().setFillPattern((AndroidPattern) pattern);
    return this;
  }

  @Override
  public Canvas setLineCap(LineCap cap) {
    currentState().setLineCap(cap);
    return this;
  }

  @Override
  public Canvas setLineJoin(LineJoin join) {
    currentState().setLineJoin(join);
    return this;
  }

  @Override
  public Canvas setMiterLimit(float miter) {
    currentState().setMiterLimit(miter);
    return this;
  }

  @Override
  public Canvas setStrokeColor(int color) {
    currentState().setStrokeColor(color);
    return this;
  }

  @Override
  public Canvas setStrokeWidth(float strokeWidth) {
    currentState().setStrokeWidth(strokeWidth);
    return this;
  }

  @Override
  public Canvas setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    Matrix m = new Matrix();
    // TODO(jgw): Is this the right order?
    m.setValues(new float[] {m11, m12, 0, m21, 0, m22, dx, dy, 1});
    canvas.setMatrix(m);
    return this;
  }

  @Override
  public Canvas strokeCircle(float x, float y, float radius) {
    canvas.drawCircle(x, y, radius, currentState().prepareStroke());
    dirty = true;
    return this;
  }

  @Override
  public Canvas strokePath(Path path) {
    Asserts.checkArgument(path instanceof AndroidPath);
    canvas.drawPath(((AndroidPath) path).path, currentState().prepareStroke());
    dirty = true;
    return this;
  }

  @Override
  public Canvas strokeRect(float x, float y, float width, float height) {
    float left = x;
    float top = y;
    float right = left + width;
    float bottom = top + height;
    canvas.drawRect(left, top, right, bottom, currentState().prepareStroke());
    dirty = true;
    return this;
  }

  @Override
  public Canvas strokeRoundRect(float x, float y, float width, float height, float radius) {
    rectf.set(x, y, width, height);
    canvas.drawRoundRect(rectf, radius, radius, currentState().prepareStroke());
    dirty = true;
    return this;
  }

  @Override
  public Canvas transform(float m11, float m12, float m21, float m22, float dx, float dy) {
    m.setValues(new float[] {m11, m21, dx, m12, m22, dy, 0, 0, 1});
    canvas.concat(m);
    return this;
  }

  @Override
  public Canvas translate(float x, float y) {
    canvas.translate(x, y);
    return this;
  }

  @Override
  public int width() {
    return canvas.getWidth();
  }

  void clearDirty() {
    dirty = false;
  }

  boolean dirty() {
    return dirty;
  }

  private AndroidCanvasState currentState() {
    return paintStack.peek();
  }

  private float rad2deg(double deg) {
    return (float) (deg * 360 / (2 * Math.PI));
  }
}
