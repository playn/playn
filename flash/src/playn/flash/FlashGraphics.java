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

import java.util.HashMap;
import java.util.Map;

import flash.display.StageAlign;
import flash.display.StageScaleMode;
import flash.display.Sprite;

import pythagoras.f.MathUtil;

import playn.core.Asserts;
import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Gradient;
import playn.core.Graphics;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.PlayN;
import playn.core.SurfaceImage;
import playn.core.SurfaceLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.TextWrap;
import playn.core.gl.GL20;
import playn.core.gl.GLContext;

class FlashGraphics implements Graphics {

  private static final String HEIGHT_TEXT =
    "THEQUICKBROWNFOXJUMPEDOVERTHELAZYDOGthequickbrownfoxjumpedoverthelazydog";
  private static final String EMWIDTH_TEXT = "m";

  private final Map<Font,FlashFontMetrics> fontMetrics = new HashMap<Font,FlashFontMetrics>();
  private FlashCanvas.CanvasElement dummyCanvas;
  private FlashCanvas.Context2d dummyCtx;
  private FlashCanvas canvas;
  private FlashCanvas.Context2d ctx;

  static String cssColorString(int color) {
    double a = ((color >> 24) & 0xff) / 255.0;
    int r = (color >> 16) & 0xff;
    int g = (color >> 8) & 0xff;
    int b = (color >> 0) & 0xff;
    return "rgba(" + r + "," + g + "," + b + "," + a + ")";
  }

  FlashFontMetrics getFontMetrics(Font font) {
    FlashFontMetrics metrics = fontMetrics.get(font);
    if (metrics == null) {
      String italic = "normal";
      String bold = "normal";
      switch (font.style()) {
          case BOLD:        bold = "bold";   break;
          case ITALIC:      italic = "italic"; break;
          case BOLD_ITALIC: bold = "bold"; italic = "italic"; break;
          default: break; // nada
      }

      dummyCtx.setFont(italic + " " + bold + " " + font.size() + " " + font.name());
      metrics = new FlashFontMetrics(dummyCtx.measureText(HEIGHT_TEXT).getHeight(),
              dummyCtx.measureText(EMWIDTH_TEXT).getWidth());
      fontMetrics.put(font, metrics);
    }
    return metrics;
  }

  protected FlashGraphics() {
    rootLayer = FlashGroupLayer.getRoot();
    FlashCanvas.CanvasElement canvasElement = FlashCanvas.CanvasElement.create();
    canvas = new FlashCanvas(screenWidth(), screenHeight(), canvasElement.getContext());
    ctx = canvas.getContext2d();
    ctx.resize(screenWidth(), screenHeight());
    Sprite.getRootSprite().getStage().setScaleMode(StageScaleMode.NO_SCALE);
    Sprite.getRootSprite().getStage().setStageAlign(StageAlign.TOP_LEFT);
    Sprite.getRootSprite().addChild((Sprite) canvasElement.cast());
    PlayN.log().info("Graphics System Initialized: Dimensions ("
        + screenWidth() + " x " + screenHeight() + ")");
    dummyCanvas = FlashCanvas.CanvasElement.create();
    dummyCtx = dummyCanvas.getContext();
  }

  FlashGroupLayer rootLayer;

  /**
   * Changes the size of the Flash stage on which PlayN is running.
   */
  public void setSize(int width, int height) {
    ctx.resize(width, height);
  }

  @Override
  public GroupLayer rootLayer() {
    return rootLayer;
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new FlashGroupLayer();
  }

  @Override
  public GroupLayer.Clipped createGroupLayer(float width, float height) {
    throw new UnsupportedOperationException("Clipped group layer not supported by Flash");
  }

  @Override @Deprecated
  public SurfaceLayer createSurfaceLayer(float width, float height) {
    return new FlashSurfaceLayer(width, height);
  }

  @Override
  public ImmediateLayer.Clipped createImmediateLayer(
      int width, int height, ImmediateLayer.Renderer renderer) {
    return new FlashImmediateLayerCanvas.Clipped(ctx, width, height, renderer);
  }

  @Override
  public ImmediateLayer createImmediateLayer(ImmediateLayer.Renderer renderer) {
    return new FlashImmediateLayerCanvas(ctx, renderer);
  }

  @Override
  public ImageLayer createImageLayer() {
    return new FlashImageLayer();
  }

  @Override
  public ImageLayer createImageLayer(Image image) {
    return new FlashImageLayer(image);
  }

  @Override
  public CanvasImage createImage(float width, float height) {
    return new FlashCanvasImage(createCanvas(width, height));
  }

  @Override
  public SurfaceImage createSurface(float width, float height) {
    return new FlashSurfaceImageCanvas(createCanvas(width, height));
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1,
      int[] colors, float[] positions) {
    Asserts.checkArgument(colors.length == positions.length);
    return new FlashGradient();
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors,
      float[] positions) {
    Asserts.checkArgument(colors.length == positions.length);

    return new FlashGradient();
  }

  @Override
  public Font createFont(String name, Font.Style style, float size) {
    return new FlashFont(this, name, style, size);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    return new FlashTextLayout(dummyCtx, text, format);
  }

  @Override
  public TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
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
    return screenWidth();
  }

  @Override
  public int height() {
    return screenHeight();
  }

  @Override
  public float scaleFactor() {
    return 1;
  }

  @Override
  public GL20 gl20() {
    throw new UnsupportedOperationException();
  }

  @Override
  public GLContext ctx() {
    return null;
  }

  public void paint() {
    rootLayer.update();
  }

  private FlashCanvas createCanvas(float width, float height) {
    return new FlashCanvas(width, height, FlashCanvas.CanvasElement.create(
                             MathUtil.iceil(width), MathUtil.iceil(height)).getContext());
  }
}
