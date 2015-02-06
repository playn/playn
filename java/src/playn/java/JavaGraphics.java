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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import playn.core.*;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.Point;

public class JavaGraphics extends Graphics {

  private Dimension screenSize = new Dimension();
  private ByteBuffer imgBuf = createImageBuffer(1024);
  private Map<String,java.awt.Font> fonts = new HashMap<String,java.awt.Font>();

  protected final JavaPlatform plat;

  // antialiased font context and aliased font context
  final FontRenderContext aaFontContext, aFontContext;

  public JavaGraphics(JavaPlatform plat) {
    super(plat, new JavaGL20(), Scale.ONE); // real scale factor set in init()
    this.plat = plat;

    // set up the dummy font contexts
    Graphics2D aaGfx = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
    aaGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    aaFontContext = aaGfx.getFontRenderContext();
    Graphics2D aGfx = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
    aGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    aFontContext = aGfx.getFontRenderContext();
  }

  /**
   * Registers a font with the graphics system.
   *
   * @param name the name under which to register the font.
   * @param path the path to the font resource (relative to the asset manager's path prefix).
   * Currently only TrueType ({@code .ttf}) fonts are supported.
   */
  public void registerFont (String name, String path) {
    try {
      fonts.put(name, plat.assets().requireResource(path).createFont());
    } catch (Exception e) {
      plat.reportError("Failed to load font [name=" + name + ", path=" + path + "]", e);
    }
  }

  /**
   * Changes the size of the PlayN window. The supplied size is in display units, it will be
   * converted to pixels based on the display scale factor.
   */
  public void setSize (int width, int height, boolean fullscreen) {
    setDisplayMode(width, height, fullscreen);
  }

  protected void setDisplayMode(int width, int height, boolean fullscreen) {
    try {
      // check if current mode is suitable
      DisplayMode mode = Display.getDisplayMode();
      if (fullscreen == Display.isFullscreen() &&
          mode.getWidth() == width && mode.getHeight() == height) return;

      if (!fullscreen) mode = new DisplayMode(width, height);
      else {
        // try and find a mode matching width and height
        DisplayMode matching = null;
        for (DisplayMode dm : Display.getAvailableDisplayModes()) {
          if (dm.getWidth() == width && dm.getHeight() == height && dm.isFullscreenCapable()) {
            matching = dm;
          }
        }
        if (matching != null) mode = matching;
        else plat.log().info("Could not find a matching fullscreen mode, available: " +
                             Arrays.asList(Display.getAvailableDisplayModes()));
      }

      plat.log().debug("Updating display mode: " + mode + ", fullscreen: " + fullscreen);
      // TODO: fix crashes when fullscreen is toggled repeatedly
      Scale scale;
      if (fullscreen) {
        Display.setDisplayModeAndFullscreen(mode);
        scale = Scale.ONE;
        // TODO: fix alt-tab, maybe add a key listener or something?
      } else {
        Display.setDisplayMode(mode);
        scale = new Scale(Display.getPixelScaleFactor());
      }
      updateViewport(scale, mode.getWidth(), mode.getHeight());

    } catch (LWJGLException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override public IDimension screenSize() {
    DisplayMode mode = Display.getDesktopDisplayMode();
    screenSize.width = scale.invScaled(mode.getWidth());
    screenSize.height = scale.invScaled(mode.getHeight());
    return screenSize;
  }

  @Override public TextLayout layoutText(String text, TextFormat format) {
    return JavaTextLayout.layoutText(this, text, format);
  }

  @Override public TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
    return JavaTextLayout.layoutText(this, text, format, wrap);
  }

  @Override protected Canvas createCanvasImpl (Scale scale, int pixelWidth, int pixelHeight) {
    BufferedImage bitmap = new BufferedImage(
      pixelWidth, pixelHeight, BufferedImage.TYPE_INT_ARGB_PRE);
    return new JavaCanvas(this, new JavaImage(this, scale, bitmap));
  }

  java.awt.Font resolveFont(Font font) {
    java.awt.Font jfont = fonts.get(font.name);
    // if we don't have a custom font registered for this name, assume it's a platform font
    if (jfont == null) {
      fonts.put(font.name, jfont = new java.awt.Font(font.name, java.awt.Font.PLAIN, 12));
    }
    // derive a font instance at the desired style and size
    return jfont.deriveFont(STYLE_TO_JAVA[font.style.ordinal()], font.size);
  }

  /** Converts the given image into a format for quick upload to the GPU. */
  static BufferedImage convertImage (BufferedImage image) {
    switch (image.getType()) {
    case BufferedImage.TYPE_INT_ARGB_PRE:
      return image; // Already good to go
    case BufferedImage.TYPE_4BYTE_ABGR:
      image.coerceData(true); // Just premultiply the alpha and it's fine
      return image;
    }

    // Didn't know an easy thing to do, so create a whole new image in our preferred format
    BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                                                     BufferedImage.TYPE_INT_ARGB_PRE);
    Graphics2D g = convertedImage.createGraphics();
    g.setColor(new java.awt.Color(0f, 0f, 0f, 0f));
    g.fillRect(0, 0, image.getWidth(), image.getHeight());
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return convertedImage;
  }

  void preInit () {
    if (plat.config.headless) updateViewport(Scale.ONE, plat.config.width, plat.config.height);
    else setDisplayMode(scale.scaledCeil(plat.config.width),
                        scale.scaledCeil(plat.config.height), plat.config.fullscreen);
  }

  void checkScaleFactor () {
    float scaleFactor = Display.getPixelScaleFactor();
    if (scaleFactor != scale.factor) updateViewport(
      new Scale(scaleFactor), Display.getWidth(), Display.getHeight());
  }

  ByteBuffer checkGetImageBuffer (int byteSize) {
    if (imgBuf.capacity() >= byteSize) imgBuf.clear(); // reuse it!
    else imgBuf = createImageBuffer(byteSize);
    return imgBuf;
  }

  private void updateViewport (Scale scale, float displayWidth, float displayHeight) {
    viewportChanged(scale, scale.scaledCeil(displayWidth), scale.scaledCeil(displayHeight));
  }

  private static ByteBuffer createImageBuffer (int byteSize) {
    return ByteBuffer.allocateDirect(byteSize).order(ByteOrder.nativeOrder());
  }

  // this matches the order in Font.Style
  private static final int[] STYLE_TO_JAVA = {
    java.awt.Font.PLAIN, java.awt.Font.BOLD, java.awt.Font.ITALIC,
    java.awt.Font.BOLD|java.awt.Font.ITALIC
  };
}
