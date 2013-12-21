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
package playn.html;

/**
 * Contains metrics measured for an HTML font at a particular size and style.
 */
class HtmlFontMetrics {

  /** The font in question. */
  public final HtmlFont font;

  /** The full height of a line of text rendered with this font. */
  public final float height;

  /** The width of a lower-case 'm'. */
  public final float emwidth;

  public HtmlFontMetrics(HtmlFont font, float height, float emwidth) {
    this.font = font;
    this.height = height;
    this.emwidth = emwidth;
  }

  /**
   * Returns the "ascent" for this font. We fake it because there's no way to know in HTML5. Yay!
   */
  public float ascent() {
    return 0.7f * height; // hey, we'll just assume the top 70% is ascent
  }

  /**
   * Returns the "descent" for this font. We fake it because there's no way to know in HTML5. Yay!
   */
  public float descent() {
    return height - ascent();
  }

  /**
   * Returns the "leading" for this font. We fake it because there's no way to know in HTML5. Yay!
   */
  public float leading() {
    return 0.1f * height; // 10% space between lines sounds good, woo!
  }

  /**
   * Adjusts a measured width to account for italic and bold italic text. We have to handle this
   * hackily because there's no way to measure exact text extent in HTML5.
   */
  public float adjustWidth(float width) {
    // Canvas.measureText does not account for the extra width consumed by italic characters, so we
    // fudge in a fraction of an em and hope the font isn't too slanted
    switch (font.style()) {
    case ITALIC:      return width + emwidth/8;
    case BOLD_ITALIC: return width + emwidth/6;
    default:          return width; // nada
    }
  }
}
