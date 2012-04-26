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

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.view.View;

import playn.core.Asserts;
import playn.core.CanvasImage;
import playn.core.Font;
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

class AndroidGraphics extends GraphicsGL {

  private static int startingScreenWidth;
  private static int startingScreenHeight;

  public final AndroidGLContext ctx;
  public final Bitmap.Config preferredBitmapConfig;

  final GroupLayerGL rootLayer;
  private final GameActivity activity;
  private final GameViewGL gameView;
  private final AndroidTouchEventHandler touchHandler;

  private int screenWidth, screenHeight;
  private boolean sizeSetManually = false;

  public AndroidGraphics(GameActivity activity, AndroidGL20 gfx,
                         AndroidTouchEventHandler touchHandler) {
    this.activity = activity;
    this.touchHandler = touchHandler;
    this.preferredBitmapConfig = mapDisplayPixelFormat();
    if (startingScreenWidth != 0)
      screenWidth = startingScreenWidth;
    if (startingScreenHeight != 0)
      screenHeight = startingScreenHeight;
    // TODO: determine scale factor automatically?
    ctx = new AndroidGLContext(1, gfx, screenWidth, screenHeight);
    gameView = activity.gameView();
    rootLayer = new GroupLayerGL(ctx);
  }

  @Override
  public CanvasImage createImage(int w, int h) {
    return new AndroidCanvasImage(this, w, h, true);
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
    return new AndroidFont(name, style, size);
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
    View viewLayout = activity.viewLayout();
    int oldWidth = screenWidth;
    int oldHeight = screenHeight;
    screenWidth = viewLayout.getWidth();
    screenHeight = viewLayout.getHeight();
    touchHandler.calculateOffsets(this);
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
  public float scaleFactor() {
    return ctx.scaleFactor;
  }

  @Override
  public GL20 gl20() {
    return ctx.gl20;
  }

  @Override
  protected SurfaceGL createSurface(int width, int height) {
    return new AndroidSurfaceGL(activity.getCacheDir(), ctx, width, height);
  }

  @Override
  protected GLContext ctx() {
    return ctx;
  }

  /** Used to create bitmaps for canvas images. */
  Bitmap createBitmap(int width, int height, boolean alpha) {
    // TODO: Why not always use the preferredBitmapConfig?  (Preserved from pre-GL code)
    return Bitmap.createBitmap(
      width, height, alpha ? preferredBitmapConfig : Bitmap.Config.ARGB_8888);
  }

  void preparePaint() {
    ctx.processPending();
    ctx.preparePaint();
  }

  void paintLayers() {
    ctx.paintLayers(rootLayer);
  }

  private void setSize(int width, int height, boolean manual) {
    if (manual)
      sizeSetManually = true;
    gameView.gameSizeSet();
    touchHandler.calculateOffsets(this);
    // Layout the views again to change the surface size
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        View viewLayout = activity.viewLayout();
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
    int format = activity.getWindowManager().getDefaultDisplay().getPixelFormat();
    ActivityManager activityManager = (ActivityManager)
      activity.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
    int memoryClass = activityManager.getMemoryClass();

    // For low memory devices (like the HTC Magic), prefer 16-bit bitmaps
    // FIXME: The memoryClass check is from the Canvas-only implementation and may function incorrectly with OpenGL
    if (format == PixelFormat.RGBA_4444 ||  memoryClass <= 16)
      return Bitmap.Config.ARGB_4444;
    else return Bitmap.Config.ARGB_8888;
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
