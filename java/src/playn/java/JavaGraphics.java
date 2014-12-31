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
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

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
    super(plat, new JavaGL20(), new Scale(plat.config.scaleFactor));
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
  public void registerFont(String name, String path) {
    try {
      fonts.put(name, plat.assets().requireResource(path).createFont());
    } catch (Exception e) {
      plat.reportError("Failed to load font [name=" + name + ", path=" + path + "]", e);
    }
  }

  /**
   * Changes the size of the PlayN window. The supplied size is in display units, it will be
   * converted to pixels based on the configured scale factor.
   */
  public void setSize(float width, float height, boolean fullscreen) {
    int pixWidth = scale.scaledCeil(width), pixHeight = scale.scaledCeil(height);
    setDisplayMode(pixWidth, pixHeight, fullscreen);
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
          plat.log().info("Could not find a matching fullscreen mode, available: " +
                          Arrays.asList(Display.getAvailableDisplayModes()));
        } else {
          mode = matching;
        }

      } else {
        mode = new DisplayMode(width, height);
      }

      plat.log().debug("Updating display mode: " + mode + ", fullscreen: " + fullscreen);
      // TODO: fix crashes when fullscreen is toggled repeatedly
      if (fullscreen) {
        Display.setDisplayModeAndFullscreen(mode);
        // TODO: fix alt-tab, maybe add a key listener or something?
      } else {
        Display.setDisplayMode(mode);
      }
      viewSizeChanged(mode.getWidth(), mode.getHeight());

    } catch (LWJGLException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public IDimension screenSize() {
    DisplayMode mode = Display.getDesktopDisplayMode();
    screenSize.width = scale.invScaled(mode.getWidth());
    screenSize.height = scale.invScaled(mode.getHeight());
    return screenSize;
  }

  @Override
  public Canvas createCanvas(float width, float height) {
    BufferedImage bitmap = new BufferedImage(scale.scaledCeil(width), scale.scaledCeil(height),
                                             BufferedImage.TYPE_INT_ARGB_PRE);
    return new JavaCanvas(new JavaImage(scale, bitmap));
  }

  @Override
  public Gradient createGradient(Gradient.Config config) {
    if (config instanceof Gradient.Linear) {
      return JavaGradient.create((Gradient.Linear)config);
    } else {
      return JavaGradient.create((Gradient.Radial)config);
    }
  }

  @Override
  public Font createFont(Font.Config config) {
    java.awt.Font jfont = fonts.get(config.name);
    // if we don't have a custom font registered for this name, assume it's a platform font
    if (jfont == null) jfont = new java.awt.Font(config.name, java.awt.Font.PLAIN, 12);
    return new JavaFont(config, jfont);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    return JavaTextLayout.layoutText(this, text, format);
  }

  @Override
  public TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
    return JavaTextLayout.layoutText(this, text, format, wrap);
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
    int pixWidth = scale.scaledCeil(plat.config.width);
    int pixHeight = scale.scaledCeil(plat.config.height);
    if (plat.config.headless) viewSizeChanged(pixWidth, pixHeight);
    else setDisplayMode(pixWidth, pixHeight, plat.config.fullscreen);
  }

  Point transformMouse (Point point) {
    point.x /= scale.factor;
    point.y /= scale.factor;
    return point;
  }

  @Override protected void upload (Image image, Texture tex) {
    BufferedImage bitmap = ((JavaImage)image).bufferedImage();
    // Convert the bitmap into a format for quick uploading (NOOPs if already optimized)
    bitmap = convertImage(bitmap);

    DataBuffer dbuf = bitmap.getRaster().getDataBuffer();
    ByteBuffer bbuf;
    int format, type;

    if (bitmap.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
      DataBufferInt ibuf = (DataBufferInt)dbuf;
      int iSize = ibuf.getSize()*4;
      bbuf = checkGetImageBuffer(iSize);
      bbuf.asIntBuffer().put(ibuf.getData());
      bbuf.position(bbuf.position()+iSize);
      bbuf.flip();
      format = GL12.GL_BGRA;
      type = GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

    } else if (bitmap.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
      DataBufferByte dbbuf = (DataBufferByte)dbuf;
      bbuf = checkGetImageBuffer(dbbuf.getSize());
      bbuf.put(dbbuf.getData());
      bbuf.flip();
      format = GL11.GL_RGBA;
      type = GL12.GL_UNSIGNED_INT_8_8_8_8;

    } else {
      // Something went awry and convertImage thought this image was in a good form already,
      // except we don't know how to deal with it
      throw new RuntimeException("Image type wasn't converted to usable: " + bitmap.getType());
    }

    gl.glBindTexture(GL11.GL_TEXTURE_2D, tex.id);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(),
                      0, format, type, bbuf);
    gl.checkError("updateTexture");
  }

  private ByteBuffer checkGetImageBuffer (int byteSize) {
    if (imgBuf.capacity() >= byteSize) {
      imgBuf.clear(); // reuse it!
    } else {
      imgBuf = createImageBuffer(byteSize);
    }
    return imgBuf;
  }

  private static ByteBuffer createImageBuffer (int byteSize) {
    return ByteBuffer.allocateDirect(byteSize).order(ByteOrder.nativeOrder());
  }
}
