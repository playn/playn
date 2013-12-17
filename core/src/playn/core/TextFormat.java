/**
 * Copyright 2010 The PlayN Authors
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
package playn.core;

/**
 * Contains configuration for laying out and drawing single- or multi-line text to a {@link
 * Canvas}.
 */
public class TextFormat {

  /** Used to align multiline text. */
  public static enum Alignment {
    LEFT {
      @Override
      public float getX(float textWidth, float lineWidth) {
        return 0;
      }
    },
    CENTER {
      @Override
      public float getX(float textWidth, float lineWidth) {
        return (lineWidth - textWidth)/2;
      }
    },
    RIGHT {
      @Override
      public float getX(float textWidth, float lineWidth) {
        return (lineWidth - textWidth);
      }
    };

    /** Returns the x offset for text with the specified width rendered on a line of the specified
     * width. */
    public abstract float getX(float textWidth, float lineWidth);
  }

  /** The font in which to render the text (or null which indicates that the default font should be
   * used). */
  public final Font font;

  /** The width at which to wrap lines of text (or {@link Float#MAX_VALUE} if the text should not
   * be wrapped. */
  public final float wrapWidth;

  /** The alignment to use for multiline text. */
  public final Alignment align;

  /** Whether or not the text should be antialiased. Defaults to true. NOTE: this is not supported
   * by the HTML5 and Flash backends. */
  public final boolean antialias;

  /** Creates a default text format instance. */
  public TextFormat() {
    this(null, true);
  }

  /** @deprecated Use {@link TextFormat()} and configuration methods. */
  @Deprecated public TextFormat(Font font, float wrapWidth, Alignment align) {
    this(font, wrapWidth, align, true);
  }

  /** Creates a configured text format instance. */
  public TextFormat(Font font, boolean antialias) {
    this.font = font;
    this.wrapWidth = Float.MAX_VALUE;
    this.align = Alignment.LEFT;
    this.antialias = antialias;
  }

  /** @deprecated {@code wrapWidth} and {@code align} are no longer supported. */
  @Deprecated public TextFormat(Font font, float wrapWidth, Alignment align, boolean antialias) {
    this.font = font;
    this.wrapWidth = wrapWidth;
    this.align = align;
    this.antialias = antialias;
  }

  /** Returns true if line wrapping is desired. */
  public boolean shouldWrap() {
    return wrapWidth != Float.MAX_VALUE;
  }

  /** Returns a clone of this text format with the font configured as specified. */
  public TextFormat withFont(Font font) {
    return new TextFormat(font, this.wrapWidth, this.align, this.antialias);
  }

  /** @deprecated Use {@link Graphics#layoutText(String,TextFormat,float)} and render the wrapped
   * lines individually. */
  @Deprecated public TextFormat withWrapping(float wrapWidth, Alignment align) {
    return new TextFormat(this.font, wrapWidth, align, this.antialias);
  }

  /** @deprecated Use {@link Graphics#layoutText(String,TextFormat,float)} and render the wrapped
   * lines individually. */
  @Deprecated public TextFormat withWrapWidth(float wrapWidth) {
    return new TextFormat(this.font, wrapWidth, this.align, this.antialias);
  }

  /** @deprecated Use {@link Graphics#layoutText(String,TextFormat,float)} and render the wrapped
   * lines individually (aligning them as desired). */
  @Deprecated public TextFormat withAlignment(Alignment align) {
    return new TextFormat(this.font, this.wrapWidth, align, this.antialias);
  }

  /** Returns a clone of this text format with {@link #antialias} configured as specified. */
  public TextFormat withAntialias(boolean antialias) {
    return new TextFormat(this.font, this.wrapWidth, this.align, antialias);
  }

  @Override
  public String toString() {
    return "[font=" + font + ", antialias=" + antialias + "]";
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof TextFormat) {
      TextFormat ofmt = (TextFormat)other;
      return (font == ofmt.font || (font != null && font.equals(ofmt.font))) &&
        wrapWidth == ofmt.wrapWidth && align == ofmt.align && antialias == ofmt.antialias;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = align.hashCode() ^ (int)wrapWidth ^ (antialias ? 1 : 0);
    if (font != null) hash ^= font.hashCode();
    return hash;
  }
}
