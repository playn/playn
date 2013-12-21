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

import cli.System.Runtime.InteropServices.Marshal;

import cli.MonoTouch.CoreGraphics.CGBitmapContext;
import cli.MonoTouch.CoreGraphics.CGColorSpace;
import cli.MonoTouch.CoreGraphics.CGImageAlphaInfo;
import cli.MonoTouch.UIKit.UIDeviceOrientation;

import pythagoras.f.IPoint;
import pythagoras.f.Point;

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

/**
 * Provides graphics implementation on iOS.
 */
public class IOSGraphics extends GraphicsGL {

  // a shared colorspace instance for use all over the place
  static final CGColorSpace colorSpace = CGColorSpace.CreateDeviceRGB();
  static final IOSFont defaultFont = new IOSFont(null, "Helvetica", Font.Style.PLAIN, 12);

  private final GroupLayerGL rootLayer;
  private final int screenWidth, screenHeight;
  private final float touchScale;
  private final Point touchTemp = new Point();
  private boolean invertSizes;
  private boolean interpolateCanvasDrawing;

  // a scratch bitmap context used for measuring text
  private static final int S_SIZE = 10;
  final CGBitmapContext scratchCtx = new CGBitmapContext(
    Marshal.AllocHGlobal(S_SIZE * S_SIZE * 4), S_SIZE, S_SIZE, 8, 4 * S_SIZE, colorSpace,
    CGImageAlphaInfo.wrap(CGImageAlphaInfo.PremultipliedLast));

  final IOSGLContext ctx;

  public IOSGraphics(IOSPlatform platform, int screenWidth, int screenHeight,
                     float viewScale, float touchScale, boolean interpolateCanvasDrawing) {
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.touchScale = touchScale;
    this.interpolateCanvasDrawing = interpolateCanvasDrawing;
    ctx = new IOSGLContext(platform, new IOSGL20(), viewScale, screenWidth, screenHeight);
    rootLayer = new GroupLayerGL(ctx);
  }

  @Override
  public CanvasImage createImage(float width, float height) {
    return new IOSCanvasImage(ctx, width, height, interpolateCanvasDrawing);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1,
                                       int[] colors, float[] positions) {
    return new IOSGradient.Linear(x0, y0, x1, y1, colors, positions);
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors, float[] positions) {
    return new IOSGradient.Radial(x, y, r, colors, positions);
  }

  @Override
  public Font createFont(String name, Font.Style style, float size) {
    return new IOSFont(this, name, style, size);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    // TEMP: handle multiline in TextFormat until that's removed
    if (format.shouldWrap() || text.indexOf('\n') != -1 ||  text.indexOf('\r') != -1)
      return new OldIOSTextLayout(this, text, format);
    else
      return IOSTextLayout.layoutText(this, text, format);
  }

  @Override
  public TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
    return IOSTextLayout.layoutText(this, text, format, wrap);
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
  public GL20 gl20() {
    return ctx.gl;
  }

  @Override
  public GLContext ctx() {
    return ctx;
  }

  void setOrientation(UIDeviceOrientation orientation) {
    invertSizes = ctx.setOrientation(orientation);
  }

  IPoint transformTouch(float x, float y) {
    return ctx.rootTransform().inverseTransform(
      touchTemp.set(x*touchScale, y*touchScale), touchTemp);
  }

  void paint() {
    ctx.paint(rootLayer);
  }
}
