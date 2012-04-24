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
package playn.html;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;

import playn.core.Asserts;
import playn.core.Canvas;
import playn.core.Gradient;
import playn.core.Image;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.TextLayout;

class HtmlCanvas implements Canvas {

  interface Drawable {
    void draw(Context2d ctx, float x, float y, float width, float height);
    void draw(Context2d ctx, float sx, float sy, float sw, float sh,
              float dx, float dy, float dw, float dh);
  }

  private final CanvasElement canvas;
  private final Context2d ctx;
  private final int width, height;
  private boolean dirty = true;

  HtmlCanvas(int width, int height) {
    this(Document.get().createElement("canvas").<CanvasElement>cast(), width, height);
  }

  HtmlCanvas(CanvasElement canvas, int width, int height) {
    this(canvas, canvas.getContext2d(), width, height);
    canvas.setWidth(width);
    canvas.setHeight(height);
  }

  HtmlCanvas(Context2d ctx, int width, int height) {
    this(null, ctx, width, height);
  }

  private HtmlCanvas(CanvasElement canvas, Context2d ctx, int width, int height) {
    this.canvas = canvas;
    this.width = width;
    this.height = height;
    this.ctx = ctx;
  }

  @Override
  public Canvas clear() {
    ctx.clearRect(0, 0, width, height);
    dirty = true;
    return this;
  }

  @Override
  public Canvas clip(Path path) {
    Asserts.checkArgument(path instanceof HtmlPath);
    ((HtmlPath) path).replay(ctx);
    ctx.clip();
    return this;
  }

  @Override
  public Path createPath() {
    return new HtmlPath();
  }

  @Override
  public Canvas drawImage(Image img, float x, float y) {
    return drawImage(img, x, y, img.width(), img.height());
  }

  @Override
  public Canvas drawImage(Image img, float x, float y, float w, float h) {
    Asserts.checkArgument(img instanceof Drawable);
    ((Drawable) img).draw(ctx, x, y, w, h);
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawImage(Image img, float dx, float dy, float dw, float dh,
      float sx, float sy, float sw, float sh) {
    Asserts.checkArgument(img instanceof Drawable);
    ((Drawable) img).draw(ctx, sx, sy, sw, sh, dx, dy, dw, dh);
    dirty = true;
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
    ctx.beginPath();
    ctx.moveTo(x0, y0);
    ctx.lineTo(x1, y1);
    ctx.stroke();
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawPoint(float x, float y) {
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.lineTo(x, y);
    ctx.stroke();
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawText(String text, float x, float y) {
    ctx.fillText(text, x, y);
    dirty = true;
    return this;
  }

  @Override
  public Canvas drawText(TextLayout layout, float x, float y) {
    ((HtmlTextLayout)layout).draw(ctx, x, y);
    dirty = true;
    return this;
  }

  @Override
  public Canvas fillCircle(float x, float y, float radius) {
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, 2 * Math.PI);
    ctx.fill();
    dirty = true;
    return this;
  }

  @Override
  public Canvas fillPath(Path path) {
    Asserts.checkArgument(path instanceof HtmlPath);
    ((HtmlPath) path).replay(ctx);
    ctx.fill();
    dirty = true;
    return this;
  }

  @Override
  public Canvas fillRect(float x, float y, float w, float h) {
    ctx.fillRect(x, y, w, h);
    dirty = true;
    return this;
  }

  @Override
  public Canvas fillRoundRect(float x, float y, float w, float h, float radius) {
    addRoundRectPath(x, y, width, height, radius);
    ctx.fill();
    dirty = true;
    return this;
  }

  @Override
  public final int height() {
    return height;
  }

  @Override
  public Canvas restore() {
    ctx.restore();
    return this;
  }

  @Override
  public Canvas rotate(float radians) {
    ctx.rotate(radians);
    return this;
  }

  @Override
  public Canvas save() {
    ctx.save();
    return this;
  }

  @Override
  public Canvas scale(float x, float y) {
    ctx.scale(x, y);
    return this;
  }

  @Override
  public Canvas setAlpha(float alpha) {
    ctx.setGlobalAlpha(alpha);
    return this;
  }

  @Override
  public Canvas setCompositeOperation(Canvas.Composite composite) {
    ctx.setGlobalCompositeOperation(convertComposite(composite));
    return this;
  }

  @Override
  public Canvas setFillColor(int color) {
    ctx.setFillStyle(HtmlGraphics.cssColor(color));
    return this;
  }

  @Override
  public Canvas setFillGradient(Gradient gradient) {
    Asserts.checkArgument(gradient instanceof HtmlGradient);
    ctx.setFillStyle(((HtmlGradient) gradient).gradient);
    return this;
  }

  @Override
  public Canvas setFillPattern(Pattern pattern) {
    Asserts.checkArgument(pattern instanceof HtmlPattern);
    ctx.setFillStyle(((HtmlPattern) pattern).pattern(ctx));
    return this;
  }

  @Override
  public Canvas setLineCap(LineCap cap) {
    ctx.setLineCap(convertLineCap(cap));
    return this;
  }

  @Override
  public Canvas setLineJoin(LineJoin join) {
    ctx.setLineJoin(convertLineJoin(join));
    return this;
  }

  @Override
  public Canvas setMiterLimit(float miter) {
    ctx.setMiterLimit(miter);
    return this;
  }

  @Override
  public Canvas setStrokeColor(int color) {
    ctx.setStrokeStyle(HtmlGraphics.cssColor(color));
    return this;
  }

  @Override
  public Canvas setStrokeWidth(float w) {
    ctx.setLineWidth(w);
    return this;
  }

  @Override
  public Canvas setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    ctx.setTransform(m11, m12, m21, m22, dx, dy);
    return this;
  }

  @Override
  public Canvas strokeCircle(float x, float y, float radius) {
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, 2 * Math.PI);
    ctx.stroke();
    dirty = true;
    return this;
  }

  @Override
  public Canvas strokePath(Path path) {
    Asserts.checkArgument(path instanceof HtmlPath);
    ((HtmlPath) path).replay(ctx);
    ctx.stroke();
    dirty = true;
    return this;
  }

  @Override
  public Canvas strokeRect(float x, float y, float w, float h) {
    ctx.strokeRect(x, y, w, h);
    dirty = true;
    return this;
  }

  @Override
  public Canvas strokeRoundRect(float x, float y, float w, float h, float radius) {
    addRoundRectPath(x, y, width, height, radius);
    ctx.stroke();
    dirty = true;
    return this;
  }

  @Override
  public Canvas transform(float m11, float m12, float m21, float m22, float dx,
      float dy) {
    ctx.transform(m11, m12, m21, m22, dx, dy);
    return this;
  }

  @Override
  public Canvas translate(float x, float y) {
    ctx.translate(x, y);
    return this;
  }

  @Override
  public final int width() {
    return width;
  }

  CanvasElement canvas() {
    return canvas;
  }

  void clearDirty() {
    dirty = false;
  }

  boolean dirty() {
    return dirty;
  }

  private void addRoundRectPath(float x, float y, float width, float height, float radius) {
    float midx = x + width/2, midy = y + height/2, maxx = x + width, maxy = y + height;
    ctx.beginPath();
    ctx.moveTo(x, midy);
    ctx.arcTo(x, y, midx, y, radius);
    ctx.arcTo(maxx, y, maxx, midy, radius);
    ctx.arcTo(maxx, maxy, midx, maxy, radius);
    ctx.arcTo(x, maxy, x, midy, radius);
    ctx.closePath();
  }

  private String convertComposite(Canvas.Composite composite) {
    switch (composite) {
      case SRC:
        return "copy";
      case DST_ATOP:
        return "destination-atop";
      case SRC_OVER:
        return "source-over";
      case DST_OVER:
        return "destination-over";
      case SRC_IN:
        return "source-in";
      case DST_IN:
        return "destination-in";
      case SRC_OUT:
        return "source-out";
      case DST_OUT:
        return "destination-out";
      case SRC_ATOP:
        return "source-atop";
      case XOR:
        return "xor";
    }
    return "copy";
  }

  private Context2d.LineCap convertLineCap(LineCap cap) {
    switch (cap) {
      case BUTT:
        return Context2d.LineCap.BUTT;
      case ROUND:
        return Context2d.LineCap.ROUND;
      case SQUARE:
        return Context2d.LineCap.SQUARE;
    }
    return Context2d.LineCap.SQUARE;
  }

  private Context2d.LineJoin convertLineJoin(LineJoin join) {
    switch (join) {
      case BEVEL:
        return Context2d.LineJoin.BEVEL;
      case MITER:
        return Context2d.LineJoin.MITER;
      case ROUND:
        return Context2d.LineJoin.ROUND;
    }
    return Context2d.LineJoin.ROUND;
  }
}
