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
package forplay.flash;

import com.google.gwt.core.client.JavaScriptObject;

import flash.display.BitmapData;
import flash.gwt.FlashImport;
import flash.display.Sprite;

import forplay.core.Canvas;
import forplay.core.CanvasLayer;

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
   * @see forplay.core.CanvasLayer#canvas()
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
    protected Context2d() {}
    public native void resize(int x, int y) /*-{
      this.resize(x,y);
    }-*/;


     public native void beginPath() /*-{
      this.beginPath();
    }-*/;

    public native void moveto(int x, int y) /*-{
      this.moveTo(x, y);
    }-*/;

    public native void lineto(int x, int y) /*-{
      this.lineTo(x, y);
    }-*/;

     public native void stroke() /*-{
      this.stroke();
    }-*/;

     public native void setStrokeStyle(String color) /*-{
      this.strokeStyle = color;
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
      this.translate(sx, sy);
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
    
  }

}
