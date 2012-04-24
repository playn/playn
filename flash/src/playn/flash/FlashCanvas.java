/**
 * Copyright 2010 The PlayN Authors
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
package playn.flash;

import flash.display.BitmapData;
import playn.flash.FlashCanvasLayer.Context2d;

import playn.core.Asserts;
import playn.core.Canvas;
import playn.core.PlayN;
import playn.core.Gradient;
import playn.core.Image;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.TextLayout;

class FlashCanvas implements Canvas {


  private final int width, height;
  private boolean dirty = true;
  private final Context2d context2d;

  FlashCanvas(int width, int height, Context2d context2d) {
    this.width = width;
    this.height = height;
    this.context2d = context2d;
  }

  @Override
  public Canvas clear() {
    dirty = true;
    return this;
  }

  @Override
  public Canvas clip(Path path) {
    return this;
  }

  @Override
  public Path createPath() {
    return new FlashPath();
  }

  @Override
  public Canvas drawImage(Image img, float x, float y) {
    Asserts.checkArgument(img instanceof FlashImage);
    dirty = true;
    PlayN.log().info("Drawing image " + ((FlashImage) img).bitmapData());
    context2d.drawImage(((FlashImage) img).bitmapData(), x, y);
    return this;
  }

  @Override
  public Canvas drawImage(Image img, float x, float y, float w, float h) {
    Asserts.checkArgument(img instanceof FlashImage);
    context2d.drawImage(((FlashImage) img).bitmapData(), x, y, w, h);
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawImage(Image img, float dx, float dy, float dw, float dh,
      float sx, float sy, float sw, float sh) {
    Asserts.checkArgument(img instanceof FlashImage);
    dirty = true;
    context2d.drawImage(((FlashImage) img).bitmapData(), dx, dy, dw, dh, sx, sy, sw, sh);
    return this;
  }

  @Override
  public Canvas drawImageCentered(Image img, float x, float y) {
    drawImage(img, x - img.width()/2, y - img.height()/2);
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawLine(float x0, float y0, float x1, float y1) {
    context2d.beginPath();
    context2d.moveTo(x0, y0);
    context2d.lineTo(x1, y1);
    context2d.stroke();
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawPoint(float x, float y) {
    context2d.fillRect(x, y, 1, 1);
    dirty = true;
    return this;
  }

  @Override
  public Canvas fillRoundRect(float x, float y, float w, float h, float radius) {
    addRoundRectPath(x, y, width, height, radius);
    context2d.fill();
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawText(String text, float x, float y) {
    context2d.strokeText(text, x, y);
    context2d.fillText(text, x, y);
    dirty = true;
    return this;
  }

    @Override
    public Canvas drawText(TextLayout layout, float x, float y) {
        ((FlashTextLayout) layout).draw(context2d, x, y);
        dirty = true;
        return this;
    }

  @Override
  public Canvas fillCircle(float x, float y, float radius) {
    dirty = true;
    context2d.beginPath();
    context2d.arc(x, y, radius, 0, (float) (Math.PI*2), true);
    context2d.closePath();
    context2d.fill();
    return this;
  }

  @Override
  public Canvas fillPath(Path path) {
    ((FlashPath) path).replay(context2d);
    context2d.fill();
    dirty = true;
    return this;
  }

  @Override
  public Canvas fillRect(float x, float y, float w, float h) {
    context2d.fillRect(x, y, w, h);
    dirty = true;
    return this;
  }

  @Override
  public final int height() {
    return height;
  }

  @Override
  public Canvas restore() {
    context2d.restore();
    return this;
  }

  @Override
  public Canvas rotate(float radians) {
    context2d.rotate(radians);
    return this;
  }

  @Override
  public Canvas save() {
    context2d.save();
    return this;
  }

  @Override
  public Canvas scale(float x, float y) {
    context2d.scale(x, y);
    return this;
  }

  @Override
  public Canvas setAlpha(float alpha) {
    context2d.setGlobalAlpha(alpha);
    return this;
  }

  @Override
  public Canvas setCompositeOperation(Composite composite) {
    context2d.setGlobalCompositeOperation(composite.name().toLowerCase().replace('_', '-'));
    return this;
  }

  @Override
  public Canvas setFillColor(int color) {
    context2d.setFillStyle("rgba("
        + ((color >> 16) & 0xff) + ","
        + ((color >> 8) & 0xff) + ","
        + (color & 0xff) + ","
        + ((color >> 24) & 0xff)/255.0 + ")");
    return this;
  }

  @Override
  public Canvas setFillGradient(Gradient gradient) {
    return this;
  }

  @Override
  public Canvas setFillPattern(Pattern pattern) {
    return this;
  }

  @Override
  public Canvas setLineCap(LineCap cap) {
    return this;
  }

  @Override
  public Canvas setLineJoin(LineJoin join) {
    return this;
  }

  @Override
  public Canvas setMiterLimit(float miter) {
    return this;
  }

  @Override
  public Canvas setStrokeColor(int color) {
    context2d.setStrokeStyle("rgba("
        + ((color >> 16) & 0xff) + ","
        + ((color >> 8) & 0xff) + ","
        + (color & 0xff) + ","
        + ((color >> 24) & 0xff) + ")");
    return this;
  }

  @Override
  public Canvas setStrokeWidth(float w) {
    context2d.setStrokeWidth(w);
    return this;
  }

  @Override
  public Canvas setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    context2d.setTransform(m11, m12, m21, m22, dx, dy);
    return this;
  }

  @Override
  public Canvas strokeCircle(float x, float y, float radius) {
    dirty = true;
    return this;
  }

  @Override
  public Canvas strokePath(Path path) {
    ((FlashPath) path).replay(context2d);
    context2d.stroke();
    dirty = true;
    return this;
  }

  @Override
  public Canvas strokeRect(float x, float y, float w, float h) {
    context2d.strokeRect(x, y, w, h);
    dirty = true;
    return this;
  }

  @Override
  public Canvas strokeRoundRect(float x, float y, float w, float h, float radius) {
    addRoundRectPath(x, y, width, height, radius);
    context2d.stroke();
    dirty = true;
    return this;
  }

  @Override
  public Canvas transform(float m11, float m12, float m21, float m22, float dx,
      float dy) {
    context2d.transform(m11, m12, m21, m22, dx, dy);
    return this;
  }

  @Override
  public Canvas translate(float x, float y) {
    context2d.translate(x, y);
    return this;
  }

  @Override
  public final int width() {
    return width;
  }

  public void quadraticCurveTo(float cpx, float cpy, float x, float y) {
     context2d.quadraticCurveTo(cpx, cpy, x, y);
  }

  public void lineTo(float x, float y) {
    context2d.lineTo(x, y);
  }

  public void moveTo(float x, float y) {
    context2d.moveTo((int) x, (int) y);
  }

  public void close() {
    context2d.closePath();
  }

  public BitmapData bitmapData() {
    return context2d.bitmapData();
  }

  public Context2d getContext2d() {
    return context2d;
  }

  void clearDirty() {
    dirty = false;
  }

  boolean dirty() {
    return dirty;
  }

  private void addRoundRectPath(float x, float y, float width, float height, float radius) {
    float midx = x + width/2, midy = y + height/2, maxx = x + width, maxy = y + height;
    context2d.beginPath();
    context2d.moveTo(x, midy);
    context2d.arcTo(x, y, midx, y, radius);
    context2d.arcTo(maxx, y, maxx, midy, radius);
    context2d.arcTo(maxx, maxy, midx, maxy, radius);
    context2d.arcTo(x, maxy, x, midy, radius);
    context2d.closePath();
  }
}
