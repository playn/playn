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
import java.util.HashMap;
import java.util.Map;

import playn.core.*;

public abstract class JavaGraphics extends Graphics {

  private final Map<String,java.awt.Font> fonts = new HashMap<String,java.awt.Font>();
  private ByteBuffer imgBuf;
  // antialiased font context and aliased font context
  private FontRenderContext aaFontContext, aFontContext;

  protected JavaGraphics(Platform plat, GL20 gl20, Scale scale) {
    super(plat, gl20, scale);
  }

  /** Sets the title of the window. */
  abstract void setTitle (String title);

  /** Uploads the image data in {@code img} into {@code tex}. */
  abstract void upload (BufferedImage img, Texture tex);

  // these are initialized lazily to avoid doing any AWT stuff during startup
  FontRenderContext aaFontContext() {
    if (aaFontContext == null) {
      // set up the dummy font contexts
      Graphics2D aaGfx = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
      aaGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      aaFontContext = aaGfx.getFontRenderContext();
    }
    return aaFontContext;
  }

  FontRenderContext aFontContext() {
    if (aFontContext == null) {
      Graphics2D aGfx = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
      aGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
      aFontContext = aGfx.getFontRenderContext();
    }
    return aFontContext;
  }

  /**
   * Registers a font with the graphics system.
   *
   * @param name the name under which to register the font.
   * @param font the Java font, which can be loaded from a path via {@link JavaAssets#getFont}.
   */
  public void registerFont (String name, java.awt.Font font) {
    if (font == null) throw new NullPointerException();
    fonts.put(name, font);
  }

  /**
   * Changes the size of the PlayN window. The supplied size is in display units, it will be
   * converted to pixels based on the display scale factor.
   */
  public abstract void setSize (int width, int height, boolean fullscreen);

  @Override public TextLayout layoutText(String text, TextFormat format) {
    return JavaTextLayout.layoutText(this, text, format);
  }

  @Override public TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
    return JavaTextLayout.layoutText(this, text, format, wrap);
  }

  @Override protected Canvas createCanvasImpl (Scale scale, int pixelWidth, int pixelHeight) {
    BufferedImage bitmap = new BufferedImage(
      pixelWidth, pixelHeight, BufferedImage.TYPE_INT_ARGB_PRE);
    return new JavaCanvas(this, new JavaImage(this, scale, bitmap, "<canvas>"));
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

  ByteBuffer checkGetImageBuffer (int byteSize) {
    if (imgBuf == null) imgBuf = createImageBuffer(Math.max(1024, byteSize));
    if (imgBuf.capacity() >= byteSize) imgBuf.clear(); // reuse it!
    else imgBuf = createImageBuffer(byteSize);
    return imgBuf;
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
