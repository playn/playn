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
import flash.gwt.FlashImport;

import com.google.gwt.core.client.JavaScriptObject;

import pythagoras.f.MathUtil;

import playn.core.AbstractCanvas;
import playn.core.Asserts;
import playn.core.Canvas;
import playn.core.Gradient;
import playn.core.Image;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.TextLayout;

class FlashCanvas extends AbstractCanvas {

  @FlashImport({"com.googlecode.flashcanvas.CanvasRenderingContext2D"})
  final static class Context2d extends JavaScriptObject {

    public native void setGlobalCompositeOperation(String composite) /*-{
       this.globalCompositeOperation = composite;
    }-*/;

    public native void arc(float x, float y, float radius, float sa, float ea,
                           boolean anticlockwise) /*-{
        this.arc(x, y, radius, sa, ea, anticlockwise);
    }-*/;

    public native void setStrokeWidth(float w) /*-{
        this.lineWidth = w;
    }-*/;

    /**
     * Enum for text baseline style.
     */
    public enum TextBaseline {
      ALPHABETIC("alphabetic"), BOTTOM("bottom"), HANGING("hanging"), IDEOGRAPHIC("ideographic"),
      MIDDLE("middle"), TOP("top");

      private final String value;

      private TextBaseline(String value) {
        this.value = value;
      }

      public String getValue() {
        return value;
      }
    }

    protected Context2d() {}

    public native void resize(int x, int y) /*-{
      this.resize(x,y);
    }-*/;

    public native void beginPath() /*-{
      this.beginPath();
    }-*/;

    public native void moveTo(double x, double y) /*-{
      this.moveTo(x, y);
    }-*/;

    public native void lineTo(double x, double y) /*-{
      this.lineTo(x, y);
    }-*/;

    public native void stroke() /*-{
      this.stroke();
    }-*/;

    public native void clip() /*-{
      this.clip();
    }-*/;

    public native void setGlobalAlpha(float alpha) /*-{
        this.globalAlpha = alpha;
    }-*/;

    public native void setStrokeStyle(String color) /*-{
      this.strokeStyle = color;
    }-*/;

     public native void setFillStyle(String color) /*-{
      this.fillStyle = color;
    }-*/;

    /**
     * @param bitmapData
     * @param x
     * @param y
     */
    public native void drawImage(BitmapData bitmapData, float x, float y) /*-{
      this._renderImage(bitmapData, [x, y]);
    }-*/;

    public native void drawImage(BitmapData bitmapData, float x, float y, float w, float h) /*-{
      this._renderImage(bitmapData, [x, y, w, h]);
    }-*/;

    public native void drawImage(BitmapData bitmapData, float x, float y, float w, float h,
        float sx, float sy, float sw, float sh) /*-{
      this._renderImage(bitmapData, [sx, sy, sw, sh, x, y, w, h]);
    }-*/;
    /**
     *
     */
    public native void restore() /*-{
      this.restore();
    }-*/;

    public native void save() /*-{
      this.save();
    }-*/;

    /**
     * @param radians
     */
    public native void rotate(float radians) /*-{
      // TODO Auto-generated method stub
      this.rotate(radians);
    }-*/;

    public native void scale(float sx, float sy) /*-{
    // TODO Auto-generated method stub
      this.scale(sx, sy);
    }-*/;

    public native void translate(float tx, float ty) /*-{
    // TODO Auto-generated method stub
      this.translate(tx, ty);
    }-*/;

    /**
     * @param m11
     * @param m12
     * @param m21
     * @param m22
     * @param dx
     * @param dy
     */
    public native void transform(float m11, float m12, float m21, float m22, float dx, float dy) /*-{
      // TODO Auto-generated method stub
      this.transform(m11, m12, m21, m22, dx, dy);
    }-*/;

    public native void setTransform(float m11, float m12, float m21, float m22,
                                    float dx, float dy) /*-{
    // TODO Auto-generated method stub
      this.setTransform(m11, m12, m21, m22, dx, dy);
    }-*/;

    public native void fillText(String text, float x, float y) /*-{
      this.fillText(text, x, y);
    }-*/;

    public native void rect(float x, float y, float w, float h) /*-{
        this.rect(x, y, w, h);
    }-*/;

    public native void fillRect(float x, float y, float w, float h) /*-{
      this.fillRect(x, y, w, h);
    }-*/;

    public native void strokeText(String text, float x, float y) /*-{
      this.strokeText(text, x, y);
    }-*/;

    public native void arcTo(double curX, double curY, double x, double y, double radius)  /*-{
      this.arcTo(curX, curY, x, y, radius);
    }-*/;

    public native void bezierCurveTo(double c1x, double c1y, double c2x, double c2y,
                                     double x, double y)  /*-{
      this.bezierCurveTo(c1x, c1y, c2x, c2y, x, y);
    }-*/;

    public native void quadraticCurveTo(
        double cpx, double cpy, double x, double y) /*-{
      this.quadraticCurveTo(cpx, cpy, x, y);
    }-*/;

    public native void closePath() /*-{
      this.closePath();
    }-*/;

    public native void fill() /*-{
      this.fill();
    }-*/;

    public native void strokeRect(float x, float y, float w, float h) /*-{
      this.strokeRect(x, y, w, h);
    }-*/;

    public native BitmapData bitmapData() /*-{
      return this.canvas.bitmapData;
    }-*/;

    public native void clearRect(int x, int y, int width, int height) /*-{
      this.clearRect(x, y, width, height);
    }-*/;

    public native void setLineWidth(float width) /*-{
      this.lineWidth = width;
    }-*/;

    public native void setTextBaseline(String baseline) /*-{
      this.textBaseline = baseline;
    }-*/;

    public native void setFont(String font) /*-{
      this.font = font;
    }-*/;

    final static class Measure extends JavaScriptObject {
      protected Measure(){}

      public native int getWidth() /*-{
        return this.width;
      }-*/;

      public native int getHeight() /*-{
        return this.height-4;
      }-*/;
    }

    public native Measure measureText(String line) /*-{
      return this.measureText(line);
    }-*/;
  }

  @FlashImport({"com.googlecode.flashcanvas.Canvas"})
  final static class CanvasElement extends JavaScriptObject {
    protected CanvasElement() {}

    public static native CanvasElement create() /*-{
      return new com.googlecode.flashcanvas.Canvas();
    }-*/;

    public static native CanvasElement create(int width, int height) /*-{
      return new com.googlecode.flashcanvas.Canvas(width, height);
    }-*/;

    public final native Context2d getContext() /*-{
      return this.getContext("2d");
    }-*/;
  }

  private boolean dirty = true;
  private final Context2d context2d;

  FlashCanvas(float width, float height, Context2d context2d) {
    super(width, height);
    this.context2d = context2d;
  }

  @Override
  public Canvas clear() {
    context2d.clearRect(0, 0, MathUtil.iceil(width), MathUtil.iceil(height));
    dirty = true;
    return this;
  }

  @Override
  public Canvas clearRect(float x, float y, float width, float height) {
    context2d.clearRect(MathUtil.ifloor(x), MathUtil.ifloor(y),
                        MathUtil.iceil(width), MathUtil.iceil(height));
    dirty = true;
    return this;
  }

  @Override
  public Canvas clip(Path path) {
    // TODO!
    return this;
  }

  @Override
  public Canvas clipRect(float x, float y, float width, float height) {
    // TODO!
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
    addRoundRectPath(x, y, w, h, radius);
    context2d.fill();
    dirty = true;
    return this;
  }

  @Override
  public Canvas fillText(TextLayout layout, float x, float y) {
    ((FlashTextLayout) layout).fill(context2d, x, y);
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
    context2d.setFillStyle(FlashGraphics.cssColorString(color));
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
    context2d.setStrokeStyle(FlashGraphics.cssColorString(color));
    return this;
  }

  @Override
  public Canvas setStrokeWidth(float w) {
    context2d.setStrokeWidth(w);
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
    addRoundRectPath(x, y, w, h, radius);
    context2d.stroke();
    dirty = true;
    return this;
  }

  @Override
  public Canvas strokeText(TextLayout layout, float x, float y) {
    ((FlashTextLayout) layout).stroke(context2d, x, y);
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
