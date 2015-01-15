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
 * Contains metadata for a font.
 */
public class Font {

  /** The styles that may be requested for a given font. */
  public static enum Style { PLAIN, BOLD, ITALIC, BOLD_ITALIC }

  /** The name of this font. */
  public final String name;
  /** The style of this font. */
  public final Style style;
  /** The point size of this font. */
  public final float size;

  /** Creates a font as specified. */
  public Font (String name, Style style, float size) {
    this.name = name;
    this.style = style;
    this.size = size;
  }

  /** Creates a font as specified with {@link Style#PLAIN}.. */
  public Font (String name, float size) {
    this(name, Style.PLAIN, size);
  }

  /** Derives a font with the same name and style as this one, at the specified size. */
  public Font derive (float size) {
    return new Font(name, style, size);
  }

  @Override public int hashCode () {
    return name.hashCode() ^ style.hashCode() ^ (int)size;
  }

  @Override public boolean equals (Object other) {
    if (!(other instanceof Font)) return false;
    Font ofont = (Font)other;
    return name.equals(ofont.name) && style == ofont.style && size == ofont.size;
  }

  @Override public String toString () {
    return name + " " + style + " " + size + "pt";
  }
}
