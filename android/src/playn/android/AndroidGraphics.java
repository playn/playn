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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Pair;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.IPoint;
import pythagoras.f.Point;

import playn.core.*;

public class AndroidGraphics extends Graphics {

  /** An interface implemented by entities that need to store things when our GL context is lost
   * and restore them when we are given a new context. */
  public interface Refreshable {
    /** Called when our GL context is about to go away. */
    void onSurfaceLost();
    /** Called when we have been given a new GL context. */
    void onSurfaceCreated();
  }

  private final Point touchTemp = new Point();

  private Map<Refreshable, Void> refreshables =
    Collections.synchronizedMap(new WeakHashMap<Refreshable, Void>());

  private final Map<Pair<String,Font.Style>,Typeface> fonts =
    new HashMap<Pair<String,Font.Style>,Typeface>();
  private final Map<Pair<String,Font.Style>,String[]> ligatureHacks =
    new HashMap<Pair<String,Font.Style>,String[]>();

  private Dimension screenSize = new Dimension();
  private ScaleFunc canvasScaleFunc = new ScaleFunc() {
    public Scale computeScale (float width, float height, Scale gfxScale) {
      return gfxScale;
    }
  };

  final Bitmap.Config preferredBitmapConfig;

  public AndroidGraphics(Platform plat, Bitmap.Config bitmapConfig, float scaleFactor) {
    super(plat, new AndroidGL20(), new Scale(scaleFactor));
    this.preferredBitmapConfig = bitmapConfig;
  }

  /**
   * Registers a font with the graphics system.
   *
   * @param face the typeface to be registered. It can be loaded via
   * {@link AndroidAssets#getTypeface}.
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
   * Configures the scale factor function to use for {@link Canvas}. By default we use the current
   * graphics scale factor, which provides maximum resolution. Apps running on memory constrained
   * devices may wish to lower to lower this scale factor to reduce memory usage for especially
   * large canvases.
   */
  public void setCanvasScaleFunc(ScaleFunc scaleFunc) {
    if (scaleFunc == null) throw new NullPointerException("Scale func must not be null");
    canvasScaleFunc = scaleFunc;
  }

  /**
   * Informs the graphics system that the surface into which it is rendering was created.
   */
  public void onSurfaceCreated () {
    // TODO: incrementEpoch(); // increment our GL context epoch
    // TODO: init(); // reinitialize GL
    for (Refreshable ref : refreshables.keySet()) ref.onSurfaceCreated();
  }

  /**
   * Informs the graphics system that the surface into which it is rendering has changed size. The
   * supplied width and height are in pixels, not display units.
   */
  public void onSurfaceChanged (int pixelWidth, int pixelHeight) {
    viewportChanged(pixelWidth, pixelHeight);
    screenSize.setSize(viewSize);
  }

  /**
   * Informs the graphics system that the surface into which it is rendering was lost.
   */
  public void onSurfaceLost () {
    for (Refreshable ref : refreshables.keySet()) ref.onSurfaceLost();
  }

  @Override public IDimension screenSize () { return screenSize; }

  @Override public Canvas createCanvas (float width, float height) {
    Scale scale = canvasScaleFunc.computeScale(width, height, scale());
    return createCanvasImpl(scale, scale.scaledCeil(width), scale.scaledCeil(height));
  }

  @Override public TextLayout layoutText(String text, TextFormat format) {
    return AndroidTextLayout.layoutText(this, text, format);
  }

  @Override public TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
    return AndroidTextLayout.layoutText(this, text, format, wrap);
  }

  @Override protected Canvas createCanvasImpl(Scale scale, int pixelWidth, int pixelHeight) {
    Bitmap bitmap = Bitmap.createBitmap(pixelWidth, pixelHeight, preferredBitmapConfig);
    return new AndroidCanvas(this, new AndroidImage(this, scale, bitmap, "<canvas>"));
  }

  AndroidFont resolveFont(Font font) {
    if (font == null) return AndroidFont.DEFAULT;
    Pair<String,Font.Style> key = Pair.create(font.name, font.style);
    Typeface face = fonts.get(key);
    if (face == null) fonts.put(key, face = AndroidFont.create(font));
    return new AndroidFont(face, font.size, ligatureHacks.get(key));
  }

  void addRefreshable(Refreshable ref) {
    assert ref != null;
    refreshables.put(ref, null);
  }

  void removeRefreshable(Refreshable ref) {
    assert ref != null;
    refreshables.remove(ref);
  }

  IPoint transformTouch(float x, float y) {
    // TODO: return ctx.rootTransform().inverseTransform(touchTemp.set(x, y), touchTemp);
    float factor = scale().factor;
    return touchTemp.set(x / factor, y / factor);
  }
}
