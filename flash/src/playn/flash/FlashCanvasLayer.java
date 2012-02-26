/*
 * Copyright 2010 Google Inc.
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

import com.google.gwt.core.client.JavaScriptObject;

import playn.core.Asserts;

import flash.display.BitmapData;
import flash.gwt.FlashImport;
import flash.display.Sprite;

import playn.core.Canvas;
import playn.core.CanvasLayer;

/**
 *
 */
public class FlashCanvasLayer extends FlashLayer implements CanvasLayer {

  private FlashCanvas canvas;

  public FlashCanvasLayer(int width, int height) {
    super((Sprite) CanvasElement.create(width, height).cast());
    canvas = new FlashCanvas(width, height, ((CanvasElement) display().cast()).getContext());
  }


  /* (non-Javadoc)
   * @see playn.core.CanvasLayer#canvas()
   */
  @Override
  public Canvas canvas() {
    // TODO Auto-generated method stub
    return canvas;
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

  @FlashImport({"com.googlecode.flashcanvas.CanvasRenderingContext2D"})
  final static class Context2d extends JavaScriptObject {

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

    public native void setTransform(float m11, float m12, float m21, float m22, float dx, float dy) /*-{
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

    public native void quadraticCurveTo(
        double cpx, double cpy, double x, double y) /*-{
      this.quadraticCurveTo(cpx, cpy, x, y);
    }-*/;

    public native void close() /*-{
      this.close();
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

  @Override
  public float width() {
    Asserts.checkNotNull(canvas, "Canvas must not be null");
    return canvas.width();
  }

  @Override
  public float height() {
    Asserts.checkNotNull(canvas, "Canvas must not be null");
    return canvas.height();
  }

  @Override
  public float scaledWidth() {
    return transform().scaleX() * width();
  }

  @Override
  public float scaledHeight() {
    return transform().scaleY() * height();
  }
}
