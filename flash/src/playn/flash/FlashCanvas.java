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
  public void clear() {
    dirty = true;
  }

  @Override
  public void clip(Path path) {
  }

  @Override
  public void drawImage(Image img, float x, float y) {
    Asserts.checkArgument(img instanceof FlashImage);
    dirty = true;
    PlayN.log().info("Drawing image " + ((FlashImage) img).bitmapData());
    context2d.drawImage(((FlashImage) img).bitmapData(), x, y);
  }

  @Override
  public void drawImage(Image img, float x, float y, float w, float h) {
    Asserts.checkArgument(img instanceof FlashImage);
    dirty = true;
  }

  @Override
  public void drawImage(Image img, float dx, float dy, float dw, float dh,
      float sx, float sy, float sw, float sh) {
    Asserts.checkArgument(img instanceof FlashImage);
    dirty = true;
  }

  @Override
  public void drawImageCentered(Image img, float x, float y) {
    drawImage(img, x - img.width()/2, y - img.height()/2);
    dirty = true;
  }

  @Override
  public void drawLine(float x0, float y0, float x1, float y1) {
    dirty = true;
  }

  @Override
  public void drawPoint(float x, float y) {
    dirty = true;
  }

  @Override
  public void drawText(String text, float x, float y) {
    context2d.strokeText(text, x, y);
    context2d.fillText(text, x, y);
    dirty = true;
  }

  @Override
  public void drawText(TextLayout layout, float x, float y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void fillCircle(float x, float y, float radius) {
    dirty = true;
  }

  @Override
  public void fillPath(Path path) {
    dirty = true;
  }

  @Override
  public void fillRect(float x, float y, float w, float h) {
    context2d.fillRect(x, y, w, h);
    dirty = true;
  }

  @Override
  public final int height() {
    return height;
  }

  @Override
  public void restore() {
    context2d.restore();
  }

  @Override
  public void rotate(float radians) {
    context2d.rotate(radians);
  }

  @Override
  public void save() {
    context2d.save();
  }

  @Override
  public void scale(float x, float y) {
    context2d.scale(x, y);
  }

  @Override
  public void setCompositeOperation(Composite composite) {
  }

  @Override
  public void setFillColor(int color) {
    context2d.setFillStyle("rgba(" 
        + ((color >> 16) & 0xff) + "," 
        + ((color >> 8) & 0xff) + ","
        + (color & 0xff) + ","
        + ((color >> 24) & 0xff) + ")");
    
  }

  @Override
  public void setFillGradient(Gradient gradient) {
  }

  @Override
  public void setFillPattern(Pattern pattern) {
  }

  @Override
  public void setLineCap(LineCap cap) {
  }

  @Override
  public void setLineJoin(LineJoin join) {
  }

  @Override
  public void setMiterLimit(float miter) {
  }

  @Override
  public void setStrokeColor(int color) { 
    context2d.setStrokeStyle("rgba(" 
        + ((color >> 16) & 0xff) + "," 
        + ((color >> 8) & 0xff) + ","
        + (color & 0xff) + ","
        + ((color >> 24) & 0xff) + ")");
  }

  @Override
  public void setStrokeWidth(float w) {
  }

  @Override
  public void setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    context2d.setTransform(m11, m12, m21, m22, dx, dy);
  }

  @Override
  public void strokeCircle(float x, float y, float radius) {
    dirty = true;
  }

  @Override
  public void strokePath(Path path) {
    dirty = true;
  }

  @Override
  public void strokeRect(float x, float y, float w, float h) {
    dirty = true;
  }

  @Override
  public void transform(float m11, float m12, float m21, float m22, float dx,
      float dy) {
    context2d.transform(m11, m12, m21, m22, dx, dy);
  }

  @Override
  public void translate(float x, float y) {
    context2d.translate(x, y);
  }

  @Override
  public final int width() {
    return width;
  }


  void clearDirty() {
    dirty = false;
  }

  boolean dirty() {
    return dirty;
  }

 
}
