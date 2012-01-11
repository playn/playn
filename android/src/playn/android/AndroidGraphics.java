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

import android.graphics.LinearGradient;
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
import playn.core.gl.GLContext;
import playn.core.gl.GraphicsGL;
import playn.core.gl.GroupLayerGL;
import playn.core.gl.SurfaceGL;

class AndroidGraphics extends GraphicsGL {

  private static int startingScreenWidth;
  private static int startingScreenHeight;

  public final AndroidGLContext ctx;

  final GroupLayerGL rootLayer;
  private final GameViewGL gameView;
  private int screenWidth, screenHeight;
  private boolean sizeSetManually = false;

  public AndroidGraphics(AndroidGL20 gfx) {
    if (startingScreenWidth != 0)
      screenWidth = startingScreenWidth;
    if (startingScreenHeight != 0)
      screenHeight = startingScreenHeight;
    ctx = new AndroidGLContext(gfx, screenWidth, screenHeight);
    gameView = AndroidPlatform.instance.activity.gameView();
    rootLayer = new GroupLayerGL(ctx);
  }

  @Override
  public CanvasImage createImage(int w, int h) {
    return new AndroidImage(ctx, w, h, true);
  }

  public CanvasImage createImage(int w, int h, boolean alpha) {
    return new AndroidImage(ctx, w, h, alpha);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1, int[] colors,
      float[] positions) {
    LinearGradient gradient = new LinearGradient(x0, y0, x1, y1, colors, positions, TileMode.CLAMP);
    return new AndroidGradient(gradient);
  }

  @Override
  public Path createPath() {
    return new AndroidPath();
  }

  @Override
  public Pattern createPattern(Image img) {
    Asserts.checkArgument(img instanceof AndroidImage);
    return new AndroidPattern(img);
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
    View viewLayout = AndroidPlatform.instance.activity.viewLayout();
    int oldWidth = screenWidth;
    int oldHeight = screenHeight;
    screenWidth = viewLayout.getWidth();
    screenHeight = viewLayout.getHeight();
    AndroidPlatform.instance.touchEventHandler().calculateOffsets();
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
  protected SurfaceGL createSurface(int width, int height) {
    return new AndroidSurfaceGL(ctx, width, height);
  }

  @Override
  protected GLContext ctx() {
    return ctx;
  }

  void preparePaint() {
    ctx.processPending();
    ctx.bindFramebuffer();
  }

  void paintLayers() {
    ctx.paintLayers(rootLayer);
  }

  private void setSize(int width, int height, boolean manual) {
    if (manual)
      sizeSetManually = true;
    gameView.gameSizeSet();
    AndroidPlatform.instance.touchEventHandler().calculateOffsets();
    // Layout the views again to change the surface size
    AndroidPlatform.instance.activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        View viewLayout = AndroidPlatform.instance.activity.viewLayout();
        viewLayout.measure(viewLayout.getMeasuredWidth(), viewLayout.getMeasuredHeight());
        viewLayout.requestLayout();
      }
    });
    ctx.setSize(width, height);
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
