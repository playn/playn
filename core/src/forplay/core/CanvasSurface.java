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
 * Implementation class, to be used by platforms that implement Surface as just
 * a special case of Canvas.
 */
public class CanvasSurface implements Surface {

  private final Canvas canvas;

  public CanvasSurface(Canvas canvas) {
    this.canvas = canvas;
  }

  @Override
  public void clear() {
    canvas.clear();
  }

  @Override
  public void drawImage(Image image, float dx, float dy) {
    canvas.drawImage(image, dx, dy);
  }

  @Override
  public void drawImage(Image image, float dx, float dy, float dw, float dh) {
    canvas.drawImage(image, dx, dy, dw, dh);
  }

  @Override
  public void drawImage(Image image, float dx, float dy, float dw, float dh, float sx, float sy,
      float sw, float sh) {
    canvas.drawImage(image, dx, dy, dw, dh, sx, sy, sw, sh);
  }

  public void drawImageCentered(Image image, float dx, float dy) {
    canvas.drawImageCentered(image, dx, dy);
  }

  @Override
  public void drawLine(float x0, float y0, float x1, float y1, float width) {
    canvas.save();
    canvas.setStrokeWidth(width);
    canvas.drawLine(x0, y0, x1, y1);
    canvas.restore();
  }

  @Override
  public void fillRect(float x, float y, float width, float height) {
    canvas.fillRect(x, y, width, height);
  }

  @Override
  public int height() {
    return canvas.height();
  }

  @Override
  public void restore() {
    canvas.restore();
  }

  @Override
  public void rotate(float radians) {
    canvas.rotate(radians);
  }

  @Override
  public void save() {
    canvas.save();
  }

  @Override
  public void scale(float sx, float sy) {
    canvas.scale(sx, sy);
  }

  @Override
  public void setFillColor(int color) {
    canvas.setFillColor(color);
    canvas.setStrokeColor(color);
  }

  @Override
  public void setFillPattern(Pattern pattern) {
    canvas.setFillPattern(pattern);
  }

  @Override
  public void setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    canvas.setTransform(m11, m12, m21, m22, dx, dy);
  }

  @Override
  public void transform(float m11, float m12, float m21, float m22, float dx, float dy) {
    canvas.transform(m11, m12, m21, m22, dx, dy);
  }

  @Override
  public void translate(float x, float y) {
    canvas.translate(x, y);
  }

  @Override
  public int width() {
    return canvas.width();
  }
}
