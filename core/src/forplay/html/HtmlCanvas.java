/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.html;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;

import forplay.core.Asserts;
import forplay.core.Gradient;
import forplay.core.Image;
import forplay.core.Path;
import forplay.core.Pattern;
import forplay.core.Canvas;

class HtmlCanvas implements Canvas {

  private final CanvasElement canvas;
  private final Context2d ctx;
  private final int width, height;
  private boolean dirty = true;

  HtmlCanvas(int width, int height) {
    this(Document.get().createElement("canvas").<CanvasElement>cast(), width, height);
  }

  HtmlCanvas(CanvasElement canvas, int width, int height) {
    this.canvas = canvas;
    this.width = width;
    this.height = height;
    canvas.setWidth(width);
    canvas.setHeight(height);
    ctx = canvas.getContext2d();
  }

  @Override
  public void clear() {
    ctx.clearRect(0, 0, width, height);
    dirty = true;
  }

  @Override
  public void clip(Path path) {
    Asserts.checkArgument(path instanceof HtmlPath);
    ((HtmlPath) path).replay(ctx);
    ctx.clip();
  }

  @Override
  public void drawImage(Image img, float x, float y) {
    Asserts.checkArgument(img instanceof HtmlImage);
    ctx.drawImage(((HtmlImage) img).img, x, y);
    dirty = true;
  }

  @Override
  public void drawImage(Image img, float x, float y, float w, float h) {
    Asserts.checkArgument(img instanceof HtmlImage);
    ctx.drawImage(((HtmlImage) img).img, x, y, w, h);
    dirty = true;
  }

  @Override
  public void drawImage(Image img, float dx, float dy, float dw, float dh,
      float sx, float sy, float sw, float sh) {
    Asserts.checkArgument(img instanceof HtmlImage);
    ctx.drawImage(((HtmlImage) img).img, sx, sy, sw, sh, dx,
        dy, dw, dh);
    dirty = true;
  }

  @Override
  public void drawImageCentered(Image img, float x, float y) {
    drawImage(img, x - img.width()/2, y - img.height()/2);
    dirty = true;
  }

  @Override
  public void drawLine(float x0, float y0, float x1, float y1) {
    ctx.beginPath();
    ctx.moveTo(x0, y0);
    ctx.lineTo(x1, y1);
    ctx.stroke();
    dirty = true;
  }

  @Override
  public void drawPoint(float x, float y) {
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.lineTo(x, y);
    ctx.stroke();
    dirty = true;
  }

  @Override
  public void drawText(String text, float x, float y) {
    ctx.fillText(text, x, y);
    dirty = true;
  }

  @Override
  public void fillCircle(float x, float y, float radius) {
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, 2 * Math.PI);
    ctx.fill();
    dirty = true;
  }

  @Override
  public void fillPath(Path path) {
    Asserts.checkArgument(path instanceof HtmlPath);
    ((HtmlPath) path).replay(ctx);
    ctx.fill();
    dirty = true;
  }

  @Override
  public void fillRect(float x, float y, float w, float h) {
    ctx.fillRect(x, y, w, h);
    dirty = true;
  }

  @Override
  public final int height() {
    return height;
  }

  @Override
  public void restore() {
    ctx.restore();
  }

  @Override
  public void rotate(float radians) {
    ctx.rotate(radians);
  }

  @Override
  public void save() {
    ctx.save();
  }

  @Override
  public void scale(float x, float y) {
    ctx.scale(x, y);
  }

  @Override
  public void setCompositeOperation(Canvas.Composite composite) {
    ctx.setGlobalCompositeOperation(convertComposite(composite));
  }

  @Override
  public void setFillColor(int color) {
    ctx.setFillStyle(HtmlGraphics.cssColor(color));
  }

  @Override
  public void setFillGradient(Gradient gradient) {
    Asserts.checkArgument(gradient instanceof HtmlGradient);
    ctx.setFillStyle(((HtmlGradient) gradient).gradient);
  }

  @Override
  public void setFillPattern(Pattern pattern) {
    Asserts.checkArgument(pattern instanceof HtmlPattern);
    ctx.setFillStyle(((HtmlPattern) pattern).pattern);
  }

  @Override
  public void setLineCap(LineCap cap) {
    ctx.setLineCap(convertLineCap(cap));
  }

  @Override
  public void setLineJoin(LineJoin join) {
    ctx.setLineJoin(convertLineJoin(join));
  }

  @Override
  public void setMiterLimit(float miter) {
    ctx.setMiterLimit(miter);
  }

  @Override
  public void setStrokeColor(int color) {
    ctx.setStrokeStyle(HtmlGraphics.cssColor(color));
  }

  @Override
  public void setStrokeWidth(float w) {
    ctx.setLineWidth(w);
  }

  @Override
  public void setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    ctx.setTransform(m11, m12, m21, m22, dx, dy);
  }

  @Override
  public void strokeCircle(float x, float y, float radius) {
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, 2 * Math.PI);
    ctx.stroke();
    dirty = true;
  }

  @Override
  public void strokePath(Path path) {
    Asserts.checkArgument(path instanceof HtmlPath);
    ((HtmlPath) path).replay(ctx);
    ctx.stroke();
    dirty = true;
  }

  @Override
  public void strokeRect(float x, float y, float w, float h) {
    ctx.strokeRect(x, y, w, h);
    dirty = true;
  }

  @Override
  public void transform(float m11, float m12, float m21, float m22, float dx,
      float dy) {
    ctx.transform(m11, m12, m21, m22, dx, dy);
  }

  @Override
  public void translate(float x, float y) {
    ctx.translate(x, y);
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
        return "destionation-in";
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
