/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import java.util.HashMap;
import java.util.Map;

import org.robovm.apple.coregraphics.CGAffineTransform;
import org.robovm.apple.coretext.CTFont;

import playn.core.AbstractFont;

public class RoboFont extends AbstractFont {

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
  public static void registerVariant(String name, Style style, String variantName) {
    Map<String,String> styleVariants = _variants.get(style);
    if (styleVariants == null) {
      _variants.put(style, styleVariants = new HashMap<String,String>());
    }
    styleVariants.put(name, variantName);
  }

  /**
   * Returns the font used when no font is configured.
   */
  public static RoboFont defaultFont () {
    return RoboGraphics.defaultFont;
  }

  final CTFont ctFont;

  public RoboFont(RoboGraphics graphics, String name, Style style, float size) {
    super(graphics, name, style, size);
    ctFont = CTFont.create(iosName(), size, CGAffineTransform.Identity());
  }

  public String iosName() {
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
    registerVariant("American Typewriter", Style.PLAIN, "AmericanTypewriter");
    registerVariant("American Typewriter", Style.BOLD, "AmericanTypewriter-Bold");
    registerVariant("Arial", Style.PLAIN, "ArialMT");
    registerVariant("Arial", Style.ITALIC, "Arial-ItalicMT");
    registerVariant("Arial", Style.BOLD, "Arial-BoldMT");
    registerVariant("Arial", Style.BOLD_ITALIC, "Arial-BoldItalicMT");
    registerVariant("Arial Hebrew", Style.PLAIN, "ArialHebrew");
    registerVariant("Arial Hebrew", Style.BOLD, "ArialHebrew-Bold");
    registerVariant("Baskerville", Style.BOLD, "Baskerville-Bold");
    registerVariant("Baskerville", Style.ITALIC, "Baskerville-Italic");
    registerVariant("Baskerville", Style.BOLD_ITALIC, "Baskerville-BoldItalic");
    registerVariant("Chalkboard SE", Style.PLAIN, "ChalkboardSE-Regular");
    registerVariant("Chalkboard SE", Style.BOLD, "ChalkboardSE-Bold");
    registerVariant("Cochin", Style.BOLD, "Cochin-Bold");
    registerVariant("Cochin", Style.ITALIC, "Cochin-Italic");
    registerVariant("Cochin", Style.BOLD_ITALIC, "Cochin-BoldItalic");
    registerVariant("Courier", Style.BOLD, "Courier-Bold");
    registerVariant("Courier", Style.ITALIC, "Courier-Oblique");
    registerVariant("Courier", Style.BOLD_ITALIC, "Courier-BoldOblique");
    registerVariant("Courier New", Style.PLAIN, "CourierNewPSMT");
    registerVariant("Courier New", Style.BOLD, "CourierNewPS-BoldMT");
    registerVariant("Courier New", Style.ITALIC, "CourierNewPS-ItalicMT");
    registerVariant("Courier New", Style.BOLD_ITALIC, "CourierNewPS-BoldItalicMT");
    registerVariant("Georgia", Style.ITALIC, "Georgia-Italic");
    registerVariant("Georgia", Style.BOLD, "Georgia-Bold");
    registerVariant("Georgia", Style.BOLD_ITALIC, "Georgia-BoldItalic");
    registerVariant("Helvetica", Style.BOLD, "Helvetica-Bold");
    registerVariant("Helvetica", Style.ITALIC, "Helvetica-Oblique");
    registerVariant("Helvetica", Style.BOLD_ITALIC, "Helvetica-Bold-Oblique");
    registerVariant("Helvetica Neue", Style.PLAIN, "HelveticaNeue");
    registerVariant("Helvetica Neue", Style.BOLD, "HelveticaNeue-Bold");
    registerVariant("Helvetica Neue", Style.ITALIC, "HelveticaNeue-Italic");
    registerVariant("Helvetica Neue", Style.BOLD_ITALIC, "HelveticaNeue-BoldItalic");
    registerVariant("Palatino", Style.PLAIN, "Palatino-Romain");
    registerVariant("Palatino", Style.ITALIC, "Palatino-Italic");
    registerVariant("Palatino", Style.BOLD, "Palatino-Bold");
    registerVariant("Palatino", Style.BOLD_ITALIC, "Palatino-BoldItalic");
    registerVariant("Times New Roman", Style.PLAIN, "TimesNewRomanPSMT");
    registerVariant("Times New Roman", Style.ITALIC, "TimesNewRomanPS-ItalicMT");
    registerVariant("Times New Roman", Style.BOLD, "TimesNewRomanPS-BoldMT");
    registerVariant("Times New Roman", Style.BOLD_ITALIC, "TimesNewRomanPS-BoldItalicMT");
    registerVariant("Trebuchet MS", Style.PLAIN, "TrebuchetMS");
    registerVariant("Trebuchet MS", Style.ITALIC, "TrebuchetMS-Italic");
    registerVariant("Trebuchet MS", Style.BOLD, "TrebuchetMS-Bold");
    registerVariant("Trebuchet MS", Style.BOLD_ITALIC, "Trebuchet-BoldItalic"); // omits MS
    registerVariant("Verdana", Style.ITALIC, "Verdana-Italic");
    registerVariant("Verdana", Style.BOLD, "Verdana-Bold");
    registerVariant("Verdana", Style.BOLD_ITALIC, "Verdana-BoldItalic");
    // map 'Times New Roman' as 'Times' for compatibility as well
    registerVariant("Times", Style.PLAIN, "TimesNewRomanPSMT");
    registerVariant("Times", Style.ITALIC, "TimesNewRomanPS-ItalicMT");
    registerVariant("Times", Style.BOLD, "TimesNewRomanPS-BoldMT");
    registerVariant("Times", Style.BOLD_ITALIC, "TimesNewRomanPS-BoldItalicMT");
  }
}
