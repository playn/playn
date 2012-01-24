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

import cli.MonoTouch.CoreGraphics.CGColorSpace;
import cli.System.Drawing.RectangleF;

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
import playn.core.gl.GLContext;
import playn.core.gl.GraphicsGL;
import playn.core.gl.GroupLayerGL;

/**
 * Provides graphics implementation on iOS.
 */
public class IOSGraphics extends GraphicsGL {

  // a shared colorspace instance for use all over the place
  static final CGColorSpace colorSpace = CGColorSpace.CreateDeviceRGB();

  private final GroupLayerGL rootLayer;
  private final int screenWidth, screenHeight;

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

  @Override
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
    throw new UnsupportedOperationException();
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    throw new UnsupportedOperationException();
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
  public void setSize(int width, int height) {
    // setSize(width, height, true);
  }

  // @Override
  // protected SurfaceGL createSurface(int width, int height) {
  //   return new AndroidSurfaceGL(ctx, width, height);
  // }

  @Override
  protected GLContext ctx() {
    return ctx;
  }

  void paint(Game game, float alpha) {
    ctx.processPending();
    ctx.bindFramebuffer();
    game.paint(alpha); // run the game's custom painting code
    ctx.paintLayers(rootLayer);
  }
}
