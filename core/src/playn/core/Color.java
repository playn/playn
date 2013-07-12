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
 * Utility methods for working with packed-integer colors.
 */
public class Color {

  /**
   * Creates a packed integer color from four ARGB values in the range [0, 255].
   */
  public static int argb(int a, int r, int g, int b) {
    return (a << 24) | (r << 16) | (g << 8) | b;
  }

  /**
   * Creates a packed integer color from three RGB values in the range [0, 255].
   */
  public static int rgb(int r, int g, int b) {
    return argb(0xff, r, g, b);
  }

  /**
   * Extracts the alpha, in range [0, 255], from the given packed color.
   */
  public static final int alpha (int color) {
    return (color >> 24) & 0xFF;
  }

  /**
   * Extracts the red component, in range [0, 255], from the given packed color.
   */
  public static final int red (int color) {
    return (color >> 16) & 0xFF;
  }

  /**
   * Extracts the green component, in range [0, 255], from the given packed color.
   */
  public static final int green (int color) {
    return (color >> 8) & 0xFF;
  }

  /**
   * Extracts the blue component, in range [0, 255], from the given packed color.
   */
  public static final int blue (int color) {
    return color & 0xFF;
  }

  /**
   * Returns a new color that's a copy of the given color, but with the new alpha value, in
   * range [0, 255].
   */
  public static int withAlpha (int color, int alpha) {
    return (color & 0x00ffffff) | (alpha << 24);
  }

  /**
   * Encodes two [0..1] color values into the format used by the standard shader program. The
   * standard shader program delivers tinting information as two floats per vertex (AR and GB).
   */
  public static float encode (float upper, float lower) {
    int upquant = (int)(upper * 255), lowquant = (int)(lower * 255);
    return (float)(upquant * 256 + lowquant);
  }

  /**
   * Decodes and returns the upper color value in a two color value encoded by {@link #encode}.
   */
  public static float decodeUpper (float encoded) {
    float lower = encoded % 256;
    return (encoded - lower) / 255;
  }

  /**
   * Decodes and returns the lower color value in a two color value encoded by {@link #encode}.
   */
  public static float decodeLower (float encoded) {
    return (encoded % 256) / 255;
  }
}
