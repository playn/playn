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
public abstract class Font {

  /** The styles that may be requested for a given font. */
  public static enum Style { PLAIN, BOLD, ITALIC, BOLD_ITALIC }

  /** Defines the configuration for a font. */
  public static class Config {
    /** The name of this font. */
    public final String name;
    /** The style of this font. */
    public final Style style;
    /** The point size of this font. */
    public final float size;

    /** Creates a config as specified. */
    public Config (String name, Style style, float size) {
      this.name = name;
      this.style = style;
      this.size = size;
    }

    /** Creates a config as specified with {@link Style#PLAIN}.. */
    public Config (String name, float size) {
      this(name, Style.PLAIN, size);
    }

    /** Creates a config with the same name and style as this one, with the specified size. */
    public Font.Config derive (float size) {
      return new Config(name, style, size);
    }

    @Override public int hashCode () {
      return name.hashCode() ^ style.hashCode() ^ (int)size;
    }

    @Override public boolean equals (Object other) {
      if (!(other instanceof Config)) return false;
      Config ofont = (Config)other;
      return name.equals(ofont.name) && style == ofont.style && size == ofont.size;
    }

    @Override public String toString () {
      return name + " " + style + " " + size + "pt";
    }
  }

  /** Describes this font. */
  public final Config config;

  public Font (Config config) {
    this.config = config;
  }

  public String name () {
    return config.name;
  }

  public Style style () {
    return config.style;
  }

  public float size () {
    return config.size;
  }

  /** Creates font with the same name and style as this font, at size {@code size}. */
  public Font derive (Graphics graphics, float size) {
    return graphics.createFont(config.derive(size));
  }
}
