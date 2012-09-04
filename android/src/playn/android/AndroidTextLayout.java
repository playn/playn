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

import playn.core.Asserts;
import playn.core.TextFormat;
import playn.core.TextLayout;

class AndroidTextLayout implements TextLayout {

  private final TextFormat format;
  private final AndroidFont font;
  private final Paint paint;
  private final float width, height;
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
  public float width() {
    return width;
  }

  @Override
  public float height() {
    return height;
  }

  @Override
  public int lineCount() {
    return lines.size();
  }

  @Override
  public TextFormat format() {
    return format;
  }

  AndroidTextLayout(String text, TextFormat format) {
    Asserts.checkArgument(text.length() > 0, "Cannot layout the empty string.");

    this.format = format;
    this.font = (format.font == null) ? AndroidFont.DEFAULT : (AndroidFont)format.font;

    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setTypeface(font.typeface);
    paint.setTextSize(font.size());
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
    width = twidth;
    height = lines.size() * (-metrics.ascent + metrics.descent) +
      (lines.size()-1) * metrics.leading; // leading only applies to lines after 0
  }

  void breakLine(String text) {
    float[] measuredWidth = new float[1];
    int start = 0, end = text.length();
    while (start < end) {
      int count = paint.breakText(text, start, end, true, format.wrapWidth, measuredWidth);
      // breakText only breaks on characters; we want to break on word boundaries
      int lineEnd = start+count;
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
          lines.add(new Line(line, paint.measureText(line)));
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
    paint.setTypeface(font.typeface);
    paint.setTextSize(font.size());

    float yoff = 0;
    for (Line line : lines) {
      float rx = format.align.getX(line.width, width);
      yoff -= metrics.ascent;
      canvas.drawText(line.text, x + rx, y + yoff, paint);
      yoff += metrics.descent + metrics.leading;
    }
  }
}
