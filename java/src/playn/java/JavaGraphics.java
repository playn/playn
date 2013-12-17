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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import pythagoras.f.Point;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Gradient;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.TextWrap;
import playn.core.gl.GL20;
import playn.core.gl.GL20Context;
import playn.core.gl.GraphicsGL;
import playn.core.gl.GroupLayerGL;
import playn.core.gl.Scale;
import static playn.core.PlayN.*;

public class JavaGraphics extends GraphicsGL {

  protected final JavaPlatform platform;
  protected final GL20Context ctx;
  protected final GroupLayerGL rootLayer;
  // antialiased font context and aliased font context
  final FontRenderContext aaFontContext, aFontContext;

  public JavaGraphics(JavaPlatform platform, JavaPlatform.Config config) {
    this.platform = platform;
    // if we're being run in headless mode, create a stub GL context which does not trigger the
    // initialization of LWJGL; this allows tests to run against non-graphics services without
    // needing to configure LWJGL native libraries
    this.ctx = config.headless ? new GL20Context(platform, null, config.scaleFactor, false) :
      new JavaGLContext(platform, config.scaleFactor);
    this.rootLayer = new GroupLayerGL(ctx);

    // set up the dummy font contexts
    Graphics2D aaGfx = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
    aaGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    aaFontContext = aaGfx.getFontRenderContext();
    Graphics2D aGfx = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
    aGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    aFontContext = aGfx.getFontRenderContext();

    if (!config.headless) {
      setDisplayMode(ctx.scale.scaledCeil(config.width), ctx.scale.scaledCeil(config.height), false);
    }
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
  public void setSize(int width, int height, boolean fullscreen) {
    int swidth = ctx.scale.scaledCeil(width), sheight = ctx.scale.scaledCeil(height);
    setDisplayMode(swidth, sheight, fullscreen);
    ctx.setSize(width, height);
  }

  protected void setDisplayMode(int width, int height, boolean fullscreen) {
    try {
      // check if current mode is suitable
      DisplayMode mode = Display.getDisplayMode();
      if (fullscreen == Display.isFullscreen() &&
          mode.getWidth() == width && mode.getHeight() == height)
        return;

      if (fullscreen) {
        // try and find a mode matching width and height
        DisplayMode matching = null;
        for (DisplayMode test : Display.getAvailableDisplayModes()) {
          if (test.getWidth() == width && test.getHeight() == height && test.isFullscreenCapable()) {
            matching = test;
          }
        }

        if (matching == null) {
          platform.log().info("Could not find a matching fullscreen mode, available: " +
                              Arrays.asList(Display.getAvailableDisplayModes()));
        } else {
          mode = matching;
        }

      } else {
        mode = new DisplayMode(width, height);
      }

      platform.log().debug("Updating display mode: " + mode + ", fullscreen: " + fullscreen);
      // TODO: fix crashes when fullscreen is toggled repeatedly
      if (fullscreen) {
        Display.setDisplayModeAndFullscreen(mode);
        // TODO: fix alt-tab, maybe add a key listener or something?
      } else {
        Display.setDisplayMode(mode);
      }

    } catch (LWJGLException ex) {
      throw new RuntimeException(ex);
    }
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
    return new JavaFont(this, name, style, size, jfont);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    // TEMP: handle multiline in TextFormat until that's removed
    if (format.shouldWrap() || text.indexOf('\n') != -1 ||  text.indexOf('\r') != -1)
      return new OldJavaTextLayout(this, text, format);
    else
      return JavaTextLayout.layoutText(this, text, format);
  }

  @Override
  public TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
    return JavaTextLayout.layoutText(this, text, format, wrap);
  }

  @Override
  public int screenWidth() {
    return ctx.scale.invScaledFloor(Display.getDesktopDisplayMode().getWidth());
  }

  @Override
  public int screenHeight() {
    return ctx.scale.invScaledFloor(Display.getDesktopDisplayMode().getHeight());
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

  protected void init() {
    DisplayMode mode = Display.getDisplayMode();
    ctx.setSize(ctx.scale.invScaledFloor(mode.getWidth()),
                ctx.scale.invScaledFloor(mode.getHeight()));
    ctx.init();
  }

  protected void paint() {
    ctx.paint(rootLayer);
  }

  Point transformMouse(Point point) {
    point.x /= ctx.scale.factor;
    point.y /= ctx.scale.factor;
    return point;
  }

  protected Map<String,java.awt.Font> _fonts = new HashMap<String,java.awt.Font>();
}
