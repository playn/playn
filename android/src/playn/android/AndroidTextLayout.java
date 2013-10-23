/**
 * Copyright 2011 The PlayN Authors
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

import android.graphics.Canvas;
import android.graphics.Paint;

import playn.core.AbstractTextLayout;
import playn.core.TextFormat;
import pythagoras.f.Rectangle;

class AndroidTextLayout extends AbstractTextLayout {

  private final AndroidFont font;
  private final Paint paint;
  private final Paint.FontMetrics metrics;
  private final List<Line> lines = new ArrayList<Line>();

  private static class Line {
    public final String text;
    public final float width;
    public Line(String text, float width) {
      this.text = text;
      this.width = width;
    }
  }

  @Override
  public int lineCount() {
    return lines.size();
  }

  @Override
  public Rectangle lineBounds(int lineIdx) {
    Line line = lines.get(lineIdx);
    float x = LEFT_FUDGE + format.align.getX(line.width, width-LEFT_FUDGE-RIGHT_FUDGE);
    float y = TOP_FUDGE + lineIdx * (ascent() + descent() + leading());
    return new Rectangle(x, y, line.width, ascent() + descent());
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

  AndroidTextLayout(String text, TextFormat format) {
    super(text, format);
    this.font = (format.font == null) ? AndroidFont.DEFAULT : (AndroidFont)format.font;

    paint = new Paint(format.antialias ? Paint.ANTI_ALIAS_FLAG : 0);
    paint.setTypeface(font.typeface);
    paint.setTextSize(font.size());
    paint.setSubpixelText(true);
    metrics = paint.getFontMetrics();

    // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
    text = text.replace("\r\n", "\n").replace('\r', '\n');

    // we always break lines on newlines
    for (String line : text.split("\\n")) {
      // we may break lines between newlines if we have a wrap width
      if (format.shouldWrap()) {
        breakLine(line);
      } else {
        lines.add(new Line(line, paint.measureText(line)));
      }
    }

    // compute the text height based on the metrics
    float twidth = 0;
    for (Line line : lines) {
      twidth = Math.max(twidth, line.width);
    }
    width = twidth + LEFT_FUDGE + RIGHT_FUDGE;
    height = lines.size() * (-metrics.ascent + metrics.descent) +
      // leading only applies to lines after 0
      (lines.size()-1) * metrics.leading + TOP_FUDGE;
  }

  void breakLine(String text) {
    float[] measuredWidth = new float[1];
    int start = 0, end = text.length();
    while (start < end) {
      // breakText only breaks on characters; we want to break on word boundaries
      int count = paint.breakText(text, start, end, true, format.wrapWidth, measuredWidth);

      // breakText exhibits a bug where ligaturized text sequences (e.g. "fi") are counted as a
      // single character in the returned count when in reality they consume multiple characters of
      // the source text; so we use a hacky table of known ligatures for the font in question to
      // adjust the count if the text passed to breakText contains any known ligatures
      int lineEnd = start+count;
      if (lineEnd < end && font.ligatureHacks.length > 0) {
        int adjust = accountForLigatures(text, start, count, font.ligatureHacks);
        count += adjust;
        lineEnd += adjust;
      }

      // if we matched the rest of the line, things are simple
      if (lineEnd == end) {
        lines.add(new Line(text.substring(start, lineEnd), measuredWidth[0]));
        start += count;

      } else {
        // if we ended in the middle of a word, back up until we hit whitespace
        if (!Character.isWhitespace(text.charAt(lineEnd-1)) &&
            !Character.isWhitespace(text.charAt(lineEnd))) {
          do {
            --lineEnd;
          } while (lineEnd > start && !Character.isWhitespace(text.charAt(lineEnd)));
        }

        // if there is no whitespace on the line, then we hard-break in the middle of the word
        if (lineEnd == start) {
          lines.add(new Line(text.substring(start, start+count), measuredWidth[0]));
          start += count;

        } else {
          // otherwise we're now positioned on some sort of whitespace; trim it
          while (Character.isWhitespace(text.charAt(lineEnd-1))) {
            --lineEnd;
          }
          String line = text.substring(start, lineEnd);
          float size = paint.measureText(line);
          lines.add(new Line(line, size));
          start = lineEnd;
        }

        // now trim any whitespace from start to the first non-whitespace character
        while (start < end && Character.isWhitespace(text.charAt(start))) {
          start++;
        }
      }
    }
  }

  void draw(Canvas canvas, float x, float y, Paint paint) {
    boolean oldAA = paint.isAntiAlias();
    paint.setAntiAlias(format.antialias);
    try {
      paint.setTypeface(font.typeface);
      paint.setTextSize(font.size());
      paint.setSubpixelText(true);

      float yoff = TOP_FUDGE;
      for (Line line : lines) {
        float rx = format.align.getX(line.width, width-LEFT_FUDGE-RIGHT_FUDGE);
        yoff -= metrics.ascent;
        canvas.drawText(line.text, x + rx + LEFT_FUDGE, y + yoff, paint);
        yoff += metrics.descent + metrics.leading;
      }

    } finally {
      paint.setAntiAlias(oldAA);
    }
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

  protected static final float TOP_FUDGE = 1;
  protected static final float LEFT_FUDGE = 1;
  protected static final float RIGHT_FUDGE = 2;
}
