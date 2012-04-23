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

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Gradient;
import playn.core.Image;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GL20;
import playn.core.gl.GLContext;
import playn.core.gl.GraphicsGL;
import playn.core.gl.GroupLayerGL;
import static playn.core.PlayN.*;

public class JavaGraphics extends GraphicsGL {

  private final int DEFAULT_WIDTH = 640;
  private final int DEFAULT_HEIGHT = 480;

  private final GroupLayerGL rootLayer;
  private int width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT;
  private final JavaGLContext ctx;

  JavaGraphics() throws LWJGLException {
    // set the display mode to the default width/height before creating the display, otherwise
    // LWJGL will create a full-screen window and then we'll resize it to our small window which
    // causes annoying flashing
    Display.setDisplayMode(new DisplayMode(DEFAULT_WIDTH, DEFAULT_HEIGHT));
    Display.create();
    this.ctx = new JavaGLContext(DEFAULT_WIDTH, DEFAULT_HEIGHT);
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

  @Override
  public GroupLayerGL rootLayer() {
    return rootLayer;
  }

  @Override
  public CanvasImage createImage(int w, int h) {
    return new JavaCanvasImage(w, h);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1,
      int[] colors, float[] positions) {
    return JavaGradient.createLinear(x0, y0, x1, y1, positions, colors);
  }

  @Override @Deprecated
  public Path createPath() {
    return new JavaPath();
  }

  @Override @Deprecated
  public Pattern createPattern(Image img) {
    return img.toPattern();
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
    return new JavaTextLayout(text, format);
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
  public int width() {
    return width;
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public void setSize(int width, int height) {
    try {
      this.width = width; this.height = height;
      Display.setDisplayMode(new DisplayMode(width, height));
      ctx.setSize(width, height);
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected GLContext ctx() {
    return ctx;
  }

  void paintLayers() {
    ctx.paintLayers(rootLayer);
  }

  @Override
  public GL20 gl20() {
    throw new UnsupportedOperationException();
  }

  protected Map<String,java.awt.Font> _fonts = new HashMap<String,java.awt.Font>();
}
