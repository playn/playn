/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import org.robovm.apple.coregraphics.CGBitmapContext;
import org.robovm.apple.coregraphics.CGBitmapInfo;
import org.robovm.apple.coregraphics.CGColorSpace;
import org.robovm.apple.coregraphics.CGImageAlphaInfo;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIUserInterfaceIdiom;
import org.robovm.apple.uikit.UIWindow;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Gradient;
import playn.core.GroupLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.TextWrap;
import playn.core.gl.GL20;
import playn.core.gl.GLContext;
import playn.core.gl.GraphicsGL;
import playn.core.gl.GroupLayerGL;
import pythagoras.f.IPoint;
import pythagoras.f.Point;

/**
 * Provides graphics implementation on iOS.
 */
public class RoboGraphics extends GraphicsGL {

  // a shared colorspace instance for use all over the place
  static final CGColorSpace colorSpace = CGColorSpace.createDeviceRGB();
  static final RoboFont defaultFont = new RoboFont(null, "Helvetica", Font.Style.PLAIN, 12);

  private final RoboPlatform platform;
  private final GroupLayerGL rootLayer;
  private final float touchScale;
  private final Point touchTemp = new Point();
  private int screenWidth, screenHeight;

  // a scratch bitmap context used for measuring text
  private static final int S_SIZE = 10;
  final CGBitmapContext scratchCtx = createCGBitmap(S_SIZE, S_SIZE);

  final RoboGLContext ctx;

  public RoboGraphics(RoboPlatform platform, UIWindow window) {
    this.platform = platform;

    float deviceScale = (float)(/*(platform.osVersion >= 8) ?
      UIScreen.getMainScreen().getNativeScale() :*/ // TODO: enable when RoboVM supports API
      UIScreen.getMainScreen().getScale());

    boolean isPad = UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad;
    boolean useHalfSize = isPad && platform.config.iPadLikePhone;
    float viewScale = (useHalfSize ? 2 : 1) * deviceScale;

    CGRect bounds = window.getBounds();
    int screenWidth = (int)bounds.getWidth(), screenHeight = (int)bounds.getHeight();
    if (useHalfSize) {
      screenWidth /= 2;
      screenHeight /= 2;
    }

    this.touchScale = deviceScale;
    ctx = new RoboGLContext(platform, new RoboGL20(), viewScale);
    setSize(screenWidth, screenHeight);
    rootLayer = new GroupLayerGL(ctx);
  }

  @Override
  public CanvasImage createImage(float width, float height) {
    return new RoboCanvasImage(ctx, width, height, platform.config.interpolateCanvasDrawing);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1,
                                       int[] colors, float[] positions) {
    return new RoboGradient.Linear(x0, y0, x1, y1, colors, positions);
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors, float[] positions) {
    return new RoboGradient.Radial(x, y, r, colors, positions);
  }

  @Override
  public Font createFont(String name, Font.Style style, float size) {
    return new RoboFont(this, name, style, size);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    return RoboTextLayout.layoutText(this, text, format);
  }

  @Override
  public TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
    return RoboTextLayout.layoutText(this, text, format, wrap);
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

  static CGBitmapContext createCGBitmap(int width, int height) {
    return CGBitmapContext.create(width, height, 8, 4 * width, colorSpace, new CGBitmapInfo(
      CGImageAlphaInfo.PremultipliedLast.value()));
  }

  void setSize(int screenWidth, int screenHeight) {
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    ctx.setSize(screenWidth, screenHeight);
  }

  IPoint transformTouch(float x, float y) {
    return ctx.rootTransform().inverseTransform(
      touchTemp.set(x*touchScale, y*touchScale), touchTemp);
  }

  void paint() {
    ctx.paint(rootLayer);
  }
}
