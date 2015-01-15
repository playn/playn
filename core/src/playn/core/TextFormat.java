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
 * Contains info for laying out and drawing single- or multi-line text to a {@link Canvas}.
 */
public class TextFormat {

  /** The font in which to render the text (null indicates that the default font is used). */
  public final Font font;

  /** Whether or not the text should be antialiased. Defaults to true.
    * NOTE: this is not supported by the HTML5 backend. */
  public final boolean antialias;

  /** Creates a default text format instance. */
  public TextFormat() {
    this(null);
  }

  /** Creates a text format instance with the specified font. */
  public TextFormat(Font font) {
    this(font, true);
  }

  /** Creates a configured text format instance. */
  public TextFormat(Font font, boolean antialias) {
    this.font = font;
    this.antialias = antialias;
  }

  /** Returns a clone of this text format with the font configured as specified. */
  public TextFormat withFont(Font font) {
    return new TextFormat(font, this.antialias);
  }
  /** Returns a clone of this text format with the font configured as specified. */
  public TextFormat withFont(String name, Font.Style style, float size) {
    return withFont(new Font(name, style, size));
  }
  /** Returns a clone of this text format with the font configured as specified. */
  public TextFormat withFont(String name, float size) {
    return withFont(new Font(name, size));
  }

  /** Returns a clone of this text format with {@link #antialias} configured as specified. */
  public TextFormat withAntialias(boolean antialias) {
    return new TextFormat(this.font, antialias);
  }

  @Override public String toString() {
    return "[font=" + font + ", antialias=" + antialias + "]";
  }

  @Override public boolean equals(Object other) {
    if (other instanceof TextFormat) {
      TextFormat ofmt = (TextFormat)other;
      return (font == ofmt.font || (font != null && font.equals(ofmt.font))) &&
        antialias == ofmt.antialias;
    } else {
      return false;
    }
  }

  @Override public int hashCode() {
    int hash = (antialias ? 1 : 0);
    if (font != null) hash ^= font.hashCode();
    return hash;
  }
}
