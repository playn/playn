/**
 * Copyright 2010 The ForPlay Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package forplay.android;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import forplay.core.Gradient;
import forplay.core.Image;
import forplay.core.Path;
import forplay.core.Pattern;

import java.util.LinkedList;

class AndroidCanvas implements forplay.core.Canvas {

  private final Canvas canvas;
  private LinkedList<AndroidSurfaceState> paintStack = new LinkedList<AndroidSurfaceState>();

  AndroidCanvas(Canvas canvas) {
    this.canvas = canvas;
    paintStack.addFirst(new AndroidSurfaceState());
  }

  @Override
  public void clear() {
    canvas.drawColor(0);
  }

  @Override
  public void clip(Path clipPath) {
    assert clipPath instanceof AndroidPath;
    canvas.clipPath(((AndroidPath) clipPath).path);
  }

  @Override
  public void drawImage(Image img, float x, float y) {
    assert img instanceof AndroidImage;
    AndroidImage aimg = (AndroidImage) img;
    if (aimg.getBitmap() != null) {
      canvas.drawBitmap(aimg.getBitmap(), x, y, null);
    }
  }

  @Override
  public void drawImage(Image img, float x, float y, float w, float h) {
    assert img instanceof AndroidImage;
    AndroidImage aimg = (AndroidImage) img;
    if (aimg.getBitmap() != null) {
      canvas.drawBitmap(aimg.getBitmap(), null, new RectF(x, y, w, h), null);
    }
  }

  @Override
  public void drawImage(Image img, float dx, float dy, float dw, float dh, float sx, float sy, float sw, float sh) {
    assert img instanceof AndroidImage;
    AndroidImage aimg = (AndroidImage) img;
    if (aimg.getBitmap() != null) {
      Rect src = new Rect((int)sx, (int)sy, (int)sw, (int)sh);
      RectF dst = new RectF(dx, dy, dw, dh);
      canvas.drawBitmap(aimg.getBitmap(), src, dst, null);
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
    assert path instanceof AndroidPath;
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

    assert paintStack.size() > 0 : "Unbalanced save/restore";
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
    assert gradient instanceof AndroidGradient;
    currentState().setFillGradient((AndroidGradient) gradient);
  }

  @Override
  public void setFillPattern(Pattern pattern) {
    assert pattern instanceof AndroidPattern;
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
    m.setValues(new float[] { m11, m12, 0, m21, 0, m22, dx, dy, 1 });
    canvas.setMatrix(m);
  }

  @Override
  public void strokeCircle(float x, float y, float radius) {
    canvas.drawCircle(x, y, radius, currentState().prepareStroke());
  }

  @Override
  public void strokePath(Path path) {
    assert path instanceof AndroidPath;
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
    Matrix m = new Matrix();
    m.setValues(new float[] {
        m11, m21, dx,
        m12, m22, dy,
        0, 0, 1,
    });
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
