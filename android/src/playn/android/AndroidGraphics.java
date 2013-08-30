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
package playn.android;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.util.Pair;

import pythagoras.f.IPoint;
import pythagoras.f.MathUtil;
import pythagoras.f.Point;

import playn.core.Asserts;
import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Gradient;
import playn.core.GroupLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GL20;
import playn.core.gl.GLContext;
import playn.core.gl.GraphicsGL;
import playn.core.gl.GroupLayerGL;
import playn.core.gl.Scale;
import playn.core.gl.SurfaceGL;

public class AndroidGraphics extends GraphicsGL {

  private final AndroidPlatform platform;
  private final Point touchTemp = new Point();

  private final Map<Pair<String,Font.Style>,Typeface> fonts =
    new HashMap<Pair<String,Font.Style>,Typeface>();
  private final Map<Pair<String,Font.Style>,String[]> ligatureHacks =
    new HashMap<Pair<String,Font.Style>,String[]>();

  private int screenWidth, screenHeight;
  private ScaleFunc canvasScaleFunc = new ScaleFunc() {
    public Scale computeScale (float width, float height, Scale gfxScale) {
      return gfxScale;
    }
  };

  final AndroidGLContext ctx;
  final Bitmap.Config preferredBitmapConfig;
  final GroupLayerGL rootLayer;

  public AndroidGraphics(AndroidPlatform platform, AndroidGL20 gfx, Bitmap.Config bitmapConfig) {
    this.platform = platform;
    this.preferredBitmapConfig = bitmapConfig;
    ctx = new AndroidGLContext(platform, gfx);
    rootLayer = new GroupLayerGL(ctx);
  }

  void onSizeChanged(int viewWidth, int viewHeight) {
    screenWidth = MathUtil.iceil(viewWidth / ctx.scale.factor);
    screenHeight = MathUtil.iceil(viewHeight / ctx.scale.factor);
    platform.log().info("Updating size " + viewWidth + "x" + viewHeight + " / " + ctx.scale.factor +
                        " -> " + screenWidth + "x" + screenHeight);
    ctx.setSize(screenWidth, screenHeight);
  }

  /**
   * Registers a font with the graphics system.
   *
   * @param path the path to the font resource (relative to the asset manager's path prefix).
   * @param name the name under which to register the font.
   * @param style the style variant of the specified name provided by the font file. For example
   * one might {@code registerFont("myfont.ttf", "My Font", Font.Style.PLAIN)} and
   * {@code registerFont("myfontb.ttf", "My Font", Font.Style.BOLD)} to provide both the plain and
   * bold variants of a particular font.
   * @param ligatureGlyphs any known text sequences that are converted into a single ligature
   * character in this font. This works around an Android bug where measuring text for wrapping
   * that contains character sequences that are converted into ligatures (e.g. "fi" or "ae")
   * incorrectly reports the number of characters "consumed" from the to-be-wrapped string.
   */
  public void registerFont(String path, String name, Font.Style style, String... ligatureGlyphs) {
    try {
      registerFont(platform.assets().getTypeface(path), name, style, ligatureGlyphs);
    } catch (Exception e) {
      platform.log().warn("Failed to load font [name=" + name + ", path=" + path + "]", e);
    }
  }

  /**
   * Registers a font with the graphics system.
   *
   * @param face the typeface to be registered.
   * @param name the name under which to register the font.
   * @param style the style variant of the specified name provided by the font file. For example
   * one might {@code registerFont("myfont.ttf", "My Font", Font.Style.PLAIN)} and
   * {@code registerFont("myfontb.ttf", "My Font", Font.Style.BOLD)} to provide both the plain and
   * bold variants of a particular font.
   * @param ligatureGlyphs any known text sequences that are converted into a single ligature
   * character in this font. This works around an Android bug where measuring text for wrapping
   * that contains character sequences that are converted into ligatures (e.g. "fi" or "ae")
   * incorrectly reports the number of characters "consumed" from the to-be-wrapped string.
   */
  public void registerFont(Typeface face, String name, Font.Style style, String... ligatureGlyphs) {
    Pair<String,Font.Style> key = Pair.create(name, style);
    fonts.put(key, face);
    ligatureHacks.put(key, ligatureGlyphs);
  }

  /**
   * Configures the default bitmap filtering (smoothing) setting used when rendering images to a
   * canvas. The default is not to smooth the bitmaps, pass true to make smoothing the default.
   */
  public void setCanvasFilterBitmaps(boolean filterBitmaps) {
    if (filterBitmaps) AndroidCanvasState.PAINT_FLAGS |= Paint.FILTER_BITMAP_FLAG;
    else AndroidCanvasState.PAINT_FLAGS &= ~Paint.FILTER_BITMAP_FLAG;
  }

  /** See {@link #setCanvasScaleFunc}. */
  public interface ScaleFunc {
    /** Returns the scale to be used by the canvas with the supplied dimensions.
     * @param width the width of the to-be-created canvas, in logical pixels.
     * @param height the height of the to-be-created canvas, in logical pixels.
     * @param gfxScale the default scale factor (defines the scale of the logical pixels). */
    Scale computeScale (float width, float height, Scale gfxScale);
  }

  /**
   * Configures the scale factor function to use for {@link CanvasImage}. By default we use the
   * current graphics scale factor, which provides maximum resolution. Apps running on memory
   * constrained devices may wish to lower to lower this scale factor to reduce memory usage for
   * especially large canvases.
   */
  public void setCanvasScaleFunc(ScaleFunc scaleFunc) {
    canvasScaleFunc = Asserts.checkNotNull(scaleFunc, "Scale func must not be null");
  }

  @Override
  public CanvasImage createImage(float width, float height) {
    Scale scale = canvasScaleFunc.computeScale(width, height, ctx.scale);
    return new AndroidCanvasImage(this, width, height, scale);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1, int[] colors,
      float[] positions) {
    LinearGradient gradient = new LinearGradient(x0, y0, x1, y1, colors, positions, TileMode.CLAMP);
    return new AndroidGradient(gradient);
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors, float[] positions) {
    RadialGradient gradient = new RadialGradient(x, y, r, colors, positions, TileMode.CLAMP);
    return new AndroidGradient(gradient);
  }

  @Override
  public Font createFont(String name, Font.Style style, float size) {
    Pair<String,Font.Style> key = Pair.create(name, style);
    return new AndroidFont(this, name, style, size, fonts.get(key), ligatureHacks.get(key));
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    return new AndroidTextLayout(text, format);
  }

  @Override
  public int screenHeight() {
    return screenHeight;
  }

  @Override
  public int screenWidth() {
    return screenWidth;
  }

  @Override
  public int height() {
    return ctx.viewHeight;
  }

  @Override
  public int width() {
    return ctx.viewWidth;
  }

  @Override
  public GroupLayer rootLayer() {
    return rootLayer;
  }

  @Override
  public GL20 gl20() {
    return ctx.gl;
  }

  @Override
  public GLContext ctx() {
    return ctx;
  }

  @Override
  protected SurfaceGL createSurfaceGL(float width, float height) {
    return new AndroidSurfaceGL(platform.activity.getCacheDir(), ctx, width, height);
  }

  void paint() {
    ctx.paint(rootLayer);
  }

  IPoint transformTouch(float x, float y) {
    return ctx.rootTransform().inverseTransform(touchTemp.set(x, y), touchTemp);
  }
}
