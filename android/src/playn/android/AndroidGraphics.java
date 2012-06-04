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

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.util.Pair;
import android.view.View;

import pythagoras.f.IPoint;
import pythagoras.f.MathUtil;
import pythagoras.f.Point;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Game;
import playn.core.Gradient;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GL20;
import playn.core.gl.GLContext;
import playn.core.gl.GraphicsGL;
import playn.core.gl.GroupLayerGL;
import playn.core.gl.SurfaceGL;

public class AndroidGraphics extends GraphicsGL {

  private static int startingScreenWidth, startingScreenHeight;

  public final AndroidGLContext ctx;
  public final Bitmap.Config preferredBitmapConfig;

  final GroupLayerGL rootLayer;
  private final AndroidPlatform platform;
  private final Point touchTemp = new Point();

  private int screenWidth, screenHeight;
  private boolean sizeSetManually = false;
  private Map<Pair<String,Font.Style>,Typeface> fonts =
    new HashMap<Pair<String,Font.Style>,Typeface>();

  public AndroidGraphics(AndroidPlatform platform, AndroidGL20 gfx, float scaleFactor) {
    this.platform = platform;
    this.preferredBitmapConfig = mapDisplayPixelFormat();
    if (startingScreenWidth != 0)
      screenWidth = MathUtil.iceil(startingScreenWidth / scaleFactor);
    if (startingScreenHeight != 0)
      screenHeight = MathUtil.iceil(startingScreenHeight / scaleFactor);
    ctx = new AndroidGLContext(platform, gfx, scaleFactor, screenWidth, screenHeight);
    rootLayer = new GroupLayerGL(ctx);
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
   */
  public void registerFont(String path, String name, Font.Style style) {
    try {
      // Android has no way to load a font from an input stream so we have to first copy the data
      // into a file and then load from there; awesome!
      Typeface face = Typeface.createFromFile(
        platform.assets().cacheAsset(path, name + path.substring(path.lastIndexOf('.'))));
      fonts.put(Pair.create(name, style), face);
    } catch (Exception e) {
        platform.log().warn("Failed to load font [name=" + name + ", path=" + path + "]", e);
    }
  }

  @Override
  public CanvasImage createImage(float width, float height) {
    return new AndroidCanvasImage(this, width, height);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1, int[] colors,
      float[] positions) {
    LinearGradient gradient = new LinearGradient(x0, y0, x1, y1, colors, positions, TileMode.CLAMP);
    return new AndroidGradient(gradient);
  }

  @Override @Deprecated
  public Path createPath() {
    return new AndroidPath();
  }

  @Override @Deprecated
  public Pattern createPattern(Image img) {
    return img.toPattern();
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors, float[] positions) {
    RadialGradient gradient = new RadialGradient(x, y, r, colors, positions, TileMode.CLAMP);
    return new AndroidGradient(gradient);
  }

  @Override
  public Font createFont(String name, Font.Style style, float size) {
    Typeface face = fonts.get(Pair.create(name, style));
    return (face == null) ? new AndroidFont(name, style, size) :
      new AndroidFont(name, style, size, face);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    return new AndroidTextLayout(text, format);
  }

  /**
   * @return The height of the AndroidLayoutView containing the GameView (generally the
   *         entire display height) in pixels.
   */
  @Override
  public int screenHeight() {
    return screenHeight;
  }

  /**
   * @return The width of the AndroidLayoutView containing the GameView (generally the
   *         entire display width) in pixels.
   */
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

  public void refreshScreenSize() {
    refreshScreenSize(true);
  }

  void refreshScreenSize(boolean resize) {
    View viewLayout = platform.activity.viewLayout();
    int oldWidth = screenWidth;
    int oldHeight = screenHeight;
    screenWidth = viewLayout.getWidth();
    screenHeight = viewLayout.getHeight();
    // Change game size to fill the screen if it has never been set manually.
    if (resize && !sizeSetManually && (screenWidth != oldWidth || screenHeight != oldHeight))
      setSize(screenWidth, screenHeight, false);
  }

  /*
   * Public manual setSize function. Once this is called, automatic calls to
   * refreshScreenSize() when something changes the size of the gameView will
   * not force a call to setSize.
   */
  @Override
  public void setSize(int width, int height) {
    setSize(width, height, true);
  }

  @Override
  public GL20 gl20() {
    return ctx.gl;
  }

  @Override
  protected SurfaceGL createSurface(float width, float height) {
    return new AndroidSurfaceGL(platform.activity.getCacheDir(), ctx, width, height);
  }

  @Override
  protected GLContext ctx() {
    return ctx;
  }

  void paint(Game game, float paintAlpha) {
    ctx.preparePaint(rootLayer);
    game.paint(paintAlpha);     // run the game's custom painting code
    ctx.paintLayers(rootLayer); // paint the scene graph
  }

  IPoint transformTouch(float x, float y) {
    // TODO: nix these adjustments when we nix support for setting screen size
    x -= (screenWidth() - width()) / 2;
    y -= (screenHeight() - height()) / 2;
    return ctx.rootTransform().inverseTransform(touchTemp.set(x, y), touchTemp);
  }

  private void setSize(int width, int height, boolean manual) {
    if (manual)
      sizeSetManually = true;
    platform.activity.gameView().gameSizeSet();
    // Layout the views again to change the surface size
    platform.activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        View viewLayout = platform.activity.viewLayout();
        viewLayout.measure(viewLayout.getMeasuredWidth(), viewLayout.getMeasuredHeight());
        viewLayout.requestLayout();
      }
    });
    ctx.setSize(width, height);
  }

  /**
   * Determines the most performant pixel format for the active display.
   */
  private Bitmap.Config mapDisplayPixelFormat() {
    // TODO:  This method will require testing over a variety of devices.
    int format = platform.activity.getWindowManager().getDefaultDisplay().getPixelFormat();
    ActivityManager activityManager = (ActivityManager)
      platform.activity.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
    int memoryClass = activityManager.getMemoryClass();

    // For low memory devices (like the HTC Magic), prefer 16-bit bitmaps
    // FIXME: The memoryClass check is from the Canvas-only implementation and may function
    // incorrectly with OpenGL
    return (format == PixelFormat.RGBA_4444 || memoryClass <= 16) ?
      Bitmap.Config.ARGB_4444 : Bitmap.Config.ARGB_8888;
  }

  /**
   * Called by AndroidViewLayout to make sure that AndroidGraphics is
   * initialized with non-zero screen dimensions.
   *
   * @param width
   * @param height
   */
  static void setStartingScreenSize(int width, int height) {
    startingScreenWidth = width;
    startingScreenHeight = height;
  }
}
