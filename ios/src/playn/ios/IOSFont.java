/**
 * Copyright 2012 The PlayN Authors
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
package playn.ios;

import java.util.HashMap;
import java.util.Map;

import cli.MonoTouch.CoreText.CTFont;

import playn.core.AbstractFont;

public class IOSFont extends AbstractFont
{
  /**
   * Registers a font for use when a bold, italic or bold italic variant is requested. iOS does not
   * programmatically generate bold, italic and bold italic variants of fonts. Instead it uses the
   * actual bold, italic or bold italic variant of the font provided by the original designer.
   *
   * <p> The built-in iOS fonts (Helvetica, Courier) have already had their variants mapped, but if
   * you add custom fonts to your game, you will need to register variants for the bold, italic or
   * bold italic versions if you intend to make use of them. </p>
   *
   * <p> Alternatively, you can simply request a font variant by name (e.g. {@code
   * graphics().createFont("Arial Bold Italic", Font.Style.PLAIN, 16)}) to use a specific font
   * variant directly. This variant mapping process exists only to simplify cross-platform
   * development. </p>
   */
  public static final void registerVariant(String name, Style style, String variantName) {
    Map<String,String> styleVariants = _variants.get(style);
    if (styleVariants == null) {
      _variants.put(style, styleVariants = new HashMap<String,String>());
    }
    styleVariants.put(name, variantName);
  }

  final CTFont ctFont;

  public IOSFont(String name, Style style, float size) {
    super(name, style, size);
    ctFont = new CTFont(iosName(), size);
  }

  String iosName() {
    return getVariant(name, style);
  }

  private static String getVariant(String name, Style style) {
    Map<String,String> styleVariants = _variants.get(style);
    String variant = (styleVariants == null) ? null : styleVariants.get(name);
    if (variant != null)
      return variant;
    else if (style == Style.BOLD_ITALIC)
      // fall back to bold if we have no bold+italic variant
      return getVariant(name, Style.BOLD);
    else return name;
  }

  private static Map<Style,Map<String,String>> _variants = new HashMap<Style,Map<String,String>>();
  static {
    // this is a selection of moderately well-known fonts that are available on iOS;
    // see http://www.bluecrowbar.com/blog/2010/12/ios-fonts.html for a complete list
    registerVariant("American Typewriter", Style.BOLD, "American Typewriter Bold");
    registerVariant("Arial", Style.ITALIC, "Arial Italic");
    registerVariant("Arial", Style.BOLD, "Arial Bold");
    registerVariant("Arial", Style.BOLD_ITALIC, "Arial Bold Italic");
    registerVariant("Arial Hebrew", Style.BOLD, "Arial Hebrew Bold");
    registerVariant("Baskerville", Style.BOLD, "Baskerville Bold");
    registerVariant("Baskerville", Style.ITALIC, "Baskerville Italic");
    registerVariant("Baskerville", Style.BOLD_ITALIC, "Baskerville Bold Italic");
    registerVariant("Chalkboard SE", Style.PLAIN, "Chalkboard SE Regular");
    registerVariant("Chalkboard SE", Style.BOLD, "Chalkboard SE Bold");
    registerVariant("Chalkboard SE Regular", Style.BOLD_ITALIC, "Chalkboard SE Bold");
    registerVariant("Cochin", Style.BOLD, "Cochin Bold");
    registerVariant("Cochin", Style.ITALIC, "Cochin Italic");
    registerVariant("Cochin", Style.BOLD_ITALIC, "Cochin Bold Italic");
    registerVariant("Courier", Style.BOLD, "Courier Bold");
    registerVariant("Courier", Style.ITALIC, "Courier Oblique");
    registerVariant("Courier", Style.BOLD_ITALIC, "Courier Bold Oblique");
    registerVariant("Courier New", Style.BOLD, "Courier New Bold");
    registerVariant("Courier New", Style.ITALIC, "Courier New Italic");
    registerVariant("Courier New", Style.BOLD_ITALIC, "Courier New Bold Italic");
    registerVariant("Georgia", Style.ITALIC, "Georgia Italic");
    registerVariant("Georgia", Style.BOLD, "Georgia Bold");
    registerVariant("Georgia", Style.BOLD_ITALIC, "Georgia Bold Italic");
    registerVariant("Georgia", Style.ITALIC, "Georgia Italic");
    registerVariant("Georgia", Style.BOLD, "Georgia Bold");
    registerVariant("Georgia", Style.BOLD_ITALIC, "Georgia Bold Italic");
    registerVariant("Helvetica", Style.BOLD, "Helvetica Bold");
    registerVariant("Helvetica", Style.ITALIC, "Helvetica Oblique");
    registerVariant("Helvetica", Style.BOLD_ITALIC, "Helvetica Bold Oblique");
    registerVariant("Helvetica Neue", Style.BOLD, "Helvetica Neue Bold");
    registerVariant("Helvetica Neue", Style.ITALIC, "Helvetica Neue Italic");
    registerVariant("Helvetica Neue", Style.BOLD_ITALIC, "Helvetica Neue Bold Italic");
    registerVariant("Palatino", Style.ITALIC, "Palatino Italic");
    registerVariant("Palatino", Style.BOLD, "Palatino Bold");
    registerVariant("Palatino", Style.BOLD_ITALIC, "Palatino Bold Italic");
    registerVariant("Times New Roman", Style.ITALIC, "Times New Roman Italic");
    registerVariant("Times New Roman", Style.BOLD, "Times New Roman Bold");
    registerVariant("Times New Roman", Style.BOLD_ITALIC, "Times New Roman Bold Italic");
    registerVariant("Trebuchet MS", Style.ITALIC, "Trebuchet MS Italic");
    registerVariant("Trebuchet MS", Style.BOLD, "Trebuchet MS Bold");
    registerVariant("Trebuchet MS", Style.BOLD_ITALIC, "Trebuchet MS Bold Italic");
    registerVariant("Verdana", Style.ITALIC, "Verdana Italic");
    registerVariant("Verdana", Style.BOLD, "Verdana Bold");
    registerVariant("Verdana", Style.BOLD_ITALIC, "Verdana Bold Italic");
    // map 'Times New Roman' as 'Times' for compatibility as well
    registerVariant("Times", Style.PLAIN, "Times New Roman");
    registerVariant("Times", Style.ITALIC, "Times New Roman Italic");
    registerVariant("Times", Style.BOLD, "Times New Roman Bold");
    registerVariant("Times", Style.BOLD_ITALIC, "Times New Roman Bold Italic");
  }
}
