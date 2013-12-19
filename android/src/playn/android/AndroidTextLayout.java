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
package playn.android;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.Rectangle;
import pythagoras.f.IRectangle;

import playn.core.AbstractTextLayout;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.TextWrap;

import android.graphics.Canvas;
import android.graphics.Paint;

class AndroidTextLayout implements TextLayout, AndroidCanvas.Drawable {

  private final String text;
  private final TextFormat format;
  private final AndroidFont font;

  private final Paint.FontMetrics metrics;
  private final Rectangle bounds;

  public static TextLayout layoutText(String text, TextFormat format) {
    AndroidFont font = (format.font == null) ? AndroidFont.DEFAULT : (AndroidFont)format.font;
    Paint paint = new Paint(format.antialias ? Paint.ANTI_ALIAS_FLAG : 0);
    paint.setTypeface(font.typeface);
    paint.setTextSize(font.size());
    paint.setSubpixelText(true);
    Paint.FontMetrics metrics = paint.getFontMetrics();
    return new AndroidTextLayout(text, format, font, metrics, paint.measureText(text));
  }

  public static TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
    AndroidFont font = (format.font == null) ? AndroidFont.DEFAULT : (AndroidFont)format.font;
    Paint paint = new Paint(format.antialias ? Paint.ANTI_ALIAS_FLAG : 0);
    paint.setTypeface(font.typeface);
    paint.setTextSize(font.size());
    paint.setSubpixelText(true);
    Paint.FontMetrics metrics = paint.getFontMetrics();

    List<TextLayout> layouts = new ArrayList<TextLayout>();
    float[] measuredWidth = new float[1];
    for (String ltext : AbstractTextLayout.normalizeEOL(text).split("\\n")) {
      // if we're only wrapping on newlines, then just add the whole line now
      if (wrap.width <= 0 || wrap.width == Float.MAX_VALUE) {
        layouts.add(new AndroidTextLayout(ltext, format, font, metrics, paint.measureText(ltext)));

      } else {
        int start = 0, end = ltext.length();
        while (start < end) {
          // breakText only breaks on characters; we want to break on word boundaries
          int count = paint.breakText(ltext, start, end, true, wrap.width, measuredWidth);

          // breakText exhibits a bug where ligaturized text sequences (e.g. "fi") are counted as a
          // single character in the returned count when in reality they consume multiple
          // characters of the source text; so we use a hacky table of known ligatures for the font
          // in question to adjust the count if the text passed to breakText contains any known
          // ligatures
          int lineEnd = start+count;
          if (lineEnd < end && font.ligatureHacks.length > 0) {
            int adjust = accountForLigatures(ltext, start, count, font.ligatureHacks);
            count += adjust;
            lineEnd += adjust;
          }

          // if we matched the rest of the line, things are simple
          if (lineEnd == end) {
            layouts.add(new AndroidTextLayout(ltext.substring(start, lineEnd), format, font, metrics,
                                              measuredWidth[0]));
            start += count;

          } else {
            // if we ended in the middle of a word, back up until we hit whitespace
            if (!Character.isWhitespace(ltext.charAt(lineEnd-1)) &&
                !Character.isWhitespace(ltext.charAt(lineEnd))) {
              do {
                --lineEnd;
              } while (lineEnd > start && !Character.isWhitespace(ltext.charAt(lineEnd)));
            }

            // if there is no whitespace on the line, then we hard-break in the middle of the word
            if (lineEnd == start) {
              layouts.add(new AndroidTextLayout(ltext.substring(start, start+count), format, font,
                                                metrics, measuredWidth[0]));
              start += count;

            } else {
              // otherwise we're now positioned on some sort of whitespace; trim it
              while (Character.isWhitespace(ltext.charAt(lineEnd-1))) {
                --lineEnd;
              }
              String line = ltext.substring(start, lineEnd);
              float size = paint.measureText(line);
              layouts.add(new AndroidTextLayout(line, format, font, metrics, size));
              start = lineEnd;
            }

            // now trim any whitespace from start to the first non-whitespace character
            while (start < end && Character.isWhitespace(ltext.charAt(start))) {
              start++;
            }
          }
        }
      }
    }
    return layouts.toArray(new TextLayout[layouts.size()]);
  }

  @Override
  public String text() {
    return text;
  }

  @Override
  public TextFormat format() {
    return format;
  }

  @Override
  public float width() {
    return bounds.width;
  }

  @Override
  public float height() {
    return ascent() + descent();
  }

  @Override
  public IRectangle bounds() {
    return bounds;
  }

  @Override
  public float ascent() {
    return -metrics.ascent;
  }

  @Override
  public float descent() {
    return metrics.descent;
  }

  @Override
  public float leading() {
    return metrics.leading;
  }

  @Override @Deprecated
  public int lineCount() {
    return 1;
  }

  @Override @Deprecated
  public Rectangle lineBounds(int line) {
    return new Rectangle(bounds);
  }

  public void draw(Canvas canvas, float x, float y, Paint paint) {
    boolean oldAA = paint.isAntiAlias();
    paint.setAntiAlias(format.antialias);
    try {
      paint.setTypeface(font.typeface);
      paint.setTextSize(font.size());
      paint.setSubpixelText(true);
      canvas.drawText(text, x, y-metrics.ascent, paint);

    } finally {
      paint.setAntiAlias(oldAA);
    }
  }

  AndroidTextLayout(String text, TextFormat format, AndroidFont font, Paint.FontMetrics metrics,
                    float width) {
    this.text = text;
    this.format = format;
    this.font = font;
    this.metrics = metrics;
    // Android doesn't provide a way to get precise text bounds, so we half-ass it, woo!
    this.bounds = new Rectangle(0, 0, width, -metrics.ascent+metrics.descent);
  }

  static int accountForLigatures (String text, int start, int count, String[] ligatures) {
    int adjust = 0;
    for (String lig : ligatures) {
      // for every instance of this ligature, add its extra characters to the adjustment
      int llen = lig.length(), idx = start;
      while ((idx = text.indexOf(lig, idx)) != -1) {
        if (idx+1 > start+count) break;
        int extra = llen-1;
        adjust += extra;
        count += extra;
        idx += llen;
      }
    }
    return adjust;
  }
}
