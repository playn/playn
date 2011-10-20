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
package playn.core;

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
  public Surface clear() {
    canvas.clear();
    return this;
  }

  @Override
  public Surface drawImage(Image image, float dx, float dy) {
    canvas.drawImage(image, dx, dy);
    return this;
  }

  @Override
  public Surface drawImage(Image image, float dx, float dy, float dw, float dh) {
    canvas.drawImage(image, dx, dy, dw, dh);
    return this;
  }

  @Override
  public Surface drawImage(Image image, float dx, float dy, float dw, float dh, float sx, float sy,
      float sw, float sh) {
    canvas.drawImage(image, dx, dy, dw, dh, sx, sy, sw, sh);
    return this;
  }

  public Surface drawImageCentered(Image image, float dx, float dy) {
    canvas.drawImageCentered(image, dx, dy);
    return this;
  }

  @Override
  public Surface drawLine(float x0, float y0, float x1, float y1, float width) {
    canvas.save();
    canvas.setStrokeWidth(width);
    canvas.drawLine(x0, y0, x1, y1);
    canvas.restore();
    return this;
  }

  @Override
  public Surface fillRect(float x, float y, float width, float height) {
    canvas.fillRect(x, y, width, height);
    return this;
  }

  @Override
  public int height() {
    return canvas.height();
  }

  @Override
  public Surface restore() {
    canvas.restore();
    return this;
  }

  @Override
  public Surface rotate(float radians) {
    canvas.rotate(radians);
    return this;
  }

  @Override
  public Surface save() {
    canvas.save();
    return this;
  }

  @Override
  public Surface scale(float sx, float sy) {
    canvas.scale(sx, sy);
    return this;
  }

  @Override
  public Surface setFillColor(int color) {
    canvas.setFillColor(color);
    canvas.setStrokeColor(color);
    return this;
  }

  @Override
  public Surface setFillPattern(Pattern pattern) {
    canvas.setFillPattern(pattern);
    return this;
  }

  @Override
  public Surface setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    canvas.setTransform(m11, m12, m21, m22, dx, dy);
    return this;
  }

  @Override
  public Surface transform(float m11, float m12, float m21, float m22, float dx, float dy) {
    canvas.transform(m11, m12, m21, m22, dx, dy);
    return this;
  }

  @Override
  public Surface translate(float x, float y) {
    canvas.translate(x, y);
    return this;
  }

  @Override
  public int width() {
    return canvas.width();
  }
}
