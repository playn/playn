/**
 * Copyright 2012 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.Display;

import pythagoras.f.Point;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Game;
import playn.core.Gradient;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GL20;
import playn.core.gl.GL20Context;
import playn.core.gl.GLContext;
import playn.core.gl.GraphicsGL;
import playn.core.gl.GroupLayerGL;
import playn.core.gl.Scale;
import static playn.core.PlayN.*;

public class JavaGraphics extends GraphicsGL {

  private final GroupLayerGL rootLayer;
  private final GL20Context ctx;

  public JavaGraphics(JavaPlatform platform, JavaPlatform.Config config) {
    // if we're being run in headless mode, create a stub GL context which does not trigger the
    // initialization of LWJGL; this allows tests to run against non-graphics services without
    // needing to configure LWJGL native libraries
    this.ctx = config.headless ? new GL20Context(platform, null, config.scaleFactor, false) {
      @Override
      protected void viewWasResized () {}
    } : new JavaGLContext(platform, config.scaleFactor, config.width, config.height);
    this.rootLayer = new GroupLayerGL(ctx);
  }

  /**
   * Registers a font with the graphics system.
   *
   * @param name the name under which to register the font.
   * @param path the path to the font resource (relative to the asset manager's path prefix).
   * Currently only TrueType ({@code .ttf}) fonts are supported.
   */
  public void registerFont(String name, String path) {
    try {
      java.awt.Font font = java.awt.Font.createFont(
        java.awt.Font.TRUETYPE_FONT, ((JavaAssets) assets()).getAssetStream(path));
      _fonts.put(name, font);
    } catch (Exception e) {
      log().warn("Failed to load font [name=" + name + ", path=" + path + "]", e);
    }
  }

  /**
   * Changes the size of the PlayN window.
   */
  public void setSize(int width, int height) {
    ctx.setSize(width, height);
  }

  @Override
  public GroupLayerGL rootLayer() {
    return rootLayer;
  }

  @Override
  public CanvasImage createImage(float width, float height) {
    return new JavaCanvasImage(ctx, width, height);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1,
      int[] colors, float[] positions) {
    return JavaGradient.createLinear(x0, y0, x1, y1, positions, colors);
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors, float[] positions) {
    return JavaGradient.createRadial(x, y, r, positions, colors);
  }

  @Override
  public Font createFont(String name, Font.Style style, float size) {
    java.awt.Font jfont = _fonts.get(name);
    // if we don't have a custom font registered for this name, assume it's a platform font
    if (jfont == null) {
      jfont = new java.awt.Font(name, java.awt.Font.PLAIN, 12);
    }
    return new JavaFont(name, style, size, jfont);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    return new JavaTextLayout(this, text, format);
  }

  @Override
  public int screenWidth() {
    return Display.getDesktopDisplayMode().getWidth();
  }

  @Override
  public int screenHeight() {
    return Display.getDesktopDisplayMode().getHeight();
  }

  @Override
  public GL20 gl20() {
    return ctx.gl;
  }

  @Override
  public GL20Context ctx() {
    return ctx;
  }

  protected JavaImage createStaticImage(BufferedImage source, Scale scale) {
    return new JavaStaticImage(ctx, source, scale);
  }

  protected JavaAsyncImage createAsyncImage(float width, float height) {
    return new JavaAsyncImage(ctx, width, height);
  }

  void init() {
    ctx.init();
  }

  void transformMouse(Point point) {
    point.x /= ctx.scale.factor;
    point.y /= ctx.scale.factor;
  }

  void paint() {
    ctx.paint(rootLayer);
  }

  protected Map<String,java.awt.Font> _fonts = new HashMap<String,java.awt.Font>();
}
