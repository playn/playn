/**
 * Copyright 2012 The PlayN Authors
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
package playn.ios;

import cli.MonoTouch.CoreGraphics.CGBitmapContext;
import cli.MonoTouch.CoreGraphics.CGColorSpace;
import cli.MonoTouch.CoreGraphics.CGImageAlphaInfo;
import cli.MonoTouch.UIKit.UIDeviceOrientation;
import cli.System.Drawing.RectangleF;
import cli.System.Runtime.InteropServices.Marshal;

import pythagoras.f.FloatMath;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Game;
import playn.core.Gradient;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.InternalTransform;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.StockInternalTransform;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GLContext;
import playn.core.gl.GraphicsGL;
import playn.core.gl.GroupLayerGL;

/**
 * Provides graphics implementation on iOS.
 */
public class IOSGraphics extends GraphicsGL {

  // a shared colorspace instance for use all over the place
  static final CGColorSpace colorSpace = CGColorSpace.CreateDeviceRGB();
  static final IOSFont defaultFont = new IOSFont("Helvetica", Font.Style.PLAIN, 12);

  private final GroupLayerGL rootLayer;
  private final int screenWidth, screenHeight;

  InternalTransform rootTransform = StockInternalTransform.IDENTITY;
  private boolean invertSizes;

  // a scratch bitmap context used for measuring text
  private static final int S_SIZE = 10;
  final CGBitmapContext scratchCtx = new CGBitmapContext(
    Marshal.AllocHGlobal(S_SIZE * S_SIZE * 4), S_SIZE, S_SIZE, 8, 4 * S_SIZE, colorSpace,
    CGImageAlphaInfo.wrap(CGImageAlphaInfo.PremultipliedLast));

  final IOSGLContext ctx;

  public IOSGraphics(RectangleF bounds, float scale) {
    screenWidth = (int)(bounds.get_Width() * scale);
    screenHeight = (int)(bounds.get_Height() * scale);
    ctx = new IOSGLContext(screenWidth, screenHeight);
    rootLayer = new GroupLayerGL(ctx);
  }

  @Override
  public CanvasImage createImage(int width, int height) {
    return new IOSCanvasImage(ctx, width, height);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1,
                                       int[] colors, float[] positions) {
    return new IOSGradient.Linear(x0, y0, x1, y1, colors, positions);
  }

  @Override @Deprecated
  public Path createPath() {
    return new IOSPath();
  }

  @Override
  public Pattern createPattern(Image image) {
    return new IOSPattern((IOSAbstractImage) image);
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors, float[] positions) {
    return new IOSGradient.Radial(x, y, r, colors, positions);
  }

  @Override
  public Font createFont(String name, Font.Style style, float size) {
    return new IOSFont(name, style, size);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    return IOSTextLayout.create(this, text, format);
  }

  @Override
  public int screenHeight() {
    return invertSizes ? screenWidth : screenHeight;
  }

  @Override
  public int screenWidth() {
    return invertSizes ? screenHeight : screenWidth;
  }

  @Override
  public int height() {
    return invertSizes ? ctx.viewWidth : ctx.viewHeight;
  }

  @Override
  public int width() {
    return invertSizes ? ctx.viewHeight : ctx.viewWidth;
  }

  @Override
  public GroupLayer rootLayer() {
    return rootLayer;
  }

  @Override
  public void setSize(int width, int height) {
    // setSize(width, height, true);
  }

  @Override
  protected GLContext ctx() {
    return ctx;
  }

  void setOrientation(UIDeviceOrientation orientation) {
    switch (orientation.Value) {
    case UIDeviceOrientation.Portrait:
      rootTransform = StockInternalTransform.IDENTITY;
      invertSizes = false;
      break;
    case UIDeviceOrientation.PortraitUpsideDown:
      rootTransform = new StockInternalTransform();
      rootTransform.translate(-ctx.viewWidth, -ctx.viewHeight);
      rootTransform.scale(-1, -1);
      invertSizes = false;
      break;
    case UIDeviceOrientation.LandscapeLeft:
      rootTransform = new StockInternalTransform();
      rootTransform.rotate(FloatMath.PI/2);
      rootTransform.translate(0, -ctx.viewWidth);
      invertSizes = true;
      break;
    case UIDeviceOrientation.LandscapeRight:
      rootTransform = new StockInternalTransform();
      rootTransform.rotate(-FloatMath.PI/2);
      rootTransform.translate(-ctx.viewHeight, 0);
      invertSizes = true;
      break;
    }
  }

  void paint(Game game, float alpha) {
    ctx.processPending();
    ctx.bindFramebuffer();
    game.paint(alpha); // run the game's custom painting code
    ctx.paintLayers(rootTransform, rootLayer);
  }
}
