/**
 * Copyright 2010 The PlayN Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.flash;

import com.google.gwt.canvas.dom.client.CssColor;

import flash.display.StageScaleMode;
import flash.display.Sprite;

import playn.core.Asserts;
import playn.core.CanvasImage;
import playn.core.CanvasLayer;
import playn.core.Font;
import playn.core.PlayN;
import playn.core.Gradient;
import playn.core.Graphics;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.SurfaceLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;

class FlashGraphics implements Graphics {

  static CssColor cssColor(int color) {
    return CssColor.make(cssColorString(color));
  }

  static String cssColorString(int color) {
    double a = ((color >> 24) & 0xff) / 255.0;
    int r = (color >> 16) & 0xff;
    int g = (color >> 8) & 0xff;
    int b = (color >> 0) & 0xff;
    return "rgba(" + r + "," + g + "," + b + "," + a + ")";
  }
  

  protected FlashGraphics() {
    rootLayer = FlashGroupLayer.getRoot();
    setSize (screenWidth(), screenHeight());
    Sprite.getRootSprite().getStage().setScaleMode(StageScaleMode.EXACT_FIT);
    PlayN.log().info("Graphics System Initialized: Dimensions (" 
        + screenWidth() + " x " + screenHeight() + ")");
  }

  FlashGroupLayer rootLayer;
  @Override
  public GroupLayer rootLayer() {
    return rootLayer;
  }

  @Override
  public CanvasLayer createCanvasLayer(int width, int height) {
    return new FlashCanvasLayer(width, height);  
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new FlashGroupLayer();
  }

  @Override
  public SurfaceLayer createSurfaceLayer(int width, int height) {
    return new FlashSurfaceLayer(width, height);
  }

  @Override
  public ImageLayer createImageLayer(Image image) {
    return new FlashImageLayer(image);
  }

  @Override
  public CanvasImage createImage(int w, int h) {
    FlashCanvas surface = new FlashCanvas(w, h, null);
    return new FlashCanvasImage(surface);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1,
      int[] colors, float[] positions) {
    Asserts.checkArgument(colors.length == positions.length);
    return new FlashGradient();
  }

  @Override
  public Path createPath() {
    return new FlashPath();
  }

  @Override
  public Pattern createPattern(Image img) {
    Asserts.checkArgument(img instanceof FlashImage);
    return new FlashPattern(img);
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors,
      float[] positions) {
    Asserts.checkArgument(colors.length == positions.length);

    return new FlashGradient();
  }

  @Override
  public Font createFont(String name, Font.Style style, float size) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int screenHeight() {
    return Sprite.getRootSprite().getStage().getStageHeight();
  }

  @Override
  public int screenWidth() {
    return Sprite.getRootSprite().getStage().getStageWidth();
  }

  @Override
  public int width() {
    return Sprite.getRootSprite().getWidth();
  }

  @Override
  public int height() {
    return Sprite.getRootSprite().getHeight();
  }

  @Override
  public void setSize(int width, int height) {
    PlayN.log().info("Setting size " + width + "x" + height);
    Sprite.getRootSprite().setWidth(width);
    Sprite.getRootSprite().setHeight(height);
  }

  public void updateLayers() {
    rootLayer.update();
  }

  /* (non-Javadoc)
   * @see playn.core.Graphics#createImageLayer()
   */
  @Override
  public ImageLayer createImageLayer() {
    return new FlashImageLayer();
  }
}
