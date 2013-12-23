/**
 * Copyright 2013 The PlayN Authors
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

import com.google.gwt.canvas.dom.client.Context2d;

import pythagoras.f.Rectangle;

import java.util.ArrayList;
import java.util.List;

import playn.core.AbstractTextLayout;
import playn.core.Font;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.TextWrap;

class HtmlTextLayout extends AbstractTextLayout {

  public static TextLayout layoutText(HtmlGraphics gfx, Context2d ctx, String text,
                                      TextFormat format) {
    HtmlFontMetrics metrics = gfx.getFontMetrics(getFont(format));
    configContext(ctx, format);
    float width = (float)ctx.measureText(text).getWidth();
    return new HtmlTextLayout(text, format, metrics, width);
  }

  public static TextLayout[] layoutText(HtmlGraphics gfx, Context2d ctx, String text,
                                        TextFormat format, TextWrap wrap) {
    HtmlFontMetrics metrics = gfx.getFontMetrics(getFont(format));
    configContext(ctx, format);
    List<TextLayout> layouts = new ArrayList<TextLayout>();

    // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
    text = AbstractTextLayout.normalizeEOL(text);
    for (String line : text.split("\\n")) {
      String[] words = line.split("\\s"); // TODO: preserve intra-line whitespace
      for (int idx = 0; idx < words.length; ) {
        // note: measureLine has the side effect of adding the measured line to this.lines and
        // setting this.width to the maximum of the current width and the measured line width
        idx = measureLine(ctx, format, wrap, metrics, words, idx, layouts);
      }
    }
    return layouts.toArray(new TextLayout[layouts.size()]);
  }

  private final HtmlFontMetrics metrics;

  HtmlTextLayout(String text, TextFormat format, HtmlFontMetrics metrics, float width) {
    super(text, format, new Rectangle(0, 0, metrics.adjustWidth(width), metrics.height));
    this.metrics = metrics;
  }

  @Override
  public float ascent() {
    return metrics.ascent();
  }

  @Override
  public float descent() {
    return metrics.descent();
  }

  @Override
  public float leading() {
    return metrics.leading();
  }

  void stroke(Context2d ctx, float x, float y) {
    configContext(ctx, format);
    ctx.strokeText(text, x, y);
  }

  void fill(Context2d ctx, float x, float y) {
    configContext(ctx, format);
    ctx.fillText(text, x, y);
  }

  static void configContext(Context2d ctx, TextFormat format) {
    Font font = getFont(format);
    String style = "";
    switch (font.style()) {
    case BOLD:        style = "bold";   break;
    case ITALIC:      style = "italic"; break;
    case BOLD_ITALIC: style = "bold italic"; break;
    default: break; // nada
    }

    ctx.setFont(style + " " + font.size() + "px " + font.name());
    ctx.setTextBaseline(Context2d.TextBaseline.TOP);
  }

  static HtmlFont getFont(TextFormat format) {
    return (format.font == null) ? HtmlFont.DEFAULT : (HtmlFont)format.font;
  }

  static int measureLine(Context2d ctx, TextFormat format, TextWrap wrap, HtmlFontMetrics metrics,
                         String[] words, int idx, List<TextLayout> layouts) {
    // we always put at least one word on a line
    String line = words[idx++];
    int startIdx = idx;

    // build a rough estimate line based on character count and emwidth
    for (; idx < words.length; idx++) {
      String nline = line + " " + words[idx];
      if (nline.length() * metrics.emwidth > wrap.width) break;
      line = nline;
    }

    // now, based on exact measurements, either add more words...
    double lineWidth = ctx.measureText(line).getWidth();
    if (lineWidth < wrap.width) {
      for (; idx < words.length; idx++) {
        String nline = line + " " + words[idx];
        double nlineWidth = ctx.measureText(nline).getWidth();
        if (nlineWidth > wrap.width) break;
        line = nline;
        lineWidth = nlineWidth;
      }
    }

    // or pop words off...
    while (lineWidth > wrap.width && idx > (startIdx+1)) { // don't pop off the last word
      line = line.substring(0, line.length() - words[--idx].length() - 1);
      lineWidth = ctx.measureText(line).getWidth();
    }

    // finally, if we're still over the limit (we have a single looong word), hard break
    if (lineWidth > wrap.width) {
      StringBuilder remainder = new StringBuilder();
      while (lineWidth > wrap.width && line.length() > 1) {
        // this could be more efficient, but this edge case should be rare enough not to matter
        int lastIdx = line.length()-1;
        remainder.insert(0, line.charAt(lastIdx));
        line = line.substring(0, lastIdx);
        lineWidth = ctx.measureText(line).getWidth();
      }
      words[--idx] = remainder.toString();
    }

    layouts.add(new HtmlTextLayout(line, format, metrics, (float)lineWidth));
    return idx;
  }
}
