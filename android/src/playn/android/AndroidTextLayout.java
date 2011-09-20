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

import playn.core.TextFormat;
import playn.core.TextLayout;

class AndroidTextLayout implements TextLayout {

  private float width, height;
  private TextFormat format;
  private Paint paint;
  private Paint.FontMetrics metrics;
  private List<Line> lines = new ArrayList<Line>();

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
    this.format = format;

    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    if (format.font != null) {
      paint.setTypeface(((AndroidFont)format.font).typeface);
      // TODO
      // float density = getContext().getResources().getDisplayMetrics().density;
      // float scaledPx = format.font.size() * density;
      paint.setTextSize(format.font.size());
    }
    metrics = paint.getFontMetrics();

    // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
    text = text.replace("\r\n", "\n").replace('\r', '\n');

    // break the text and compute metrics
    if (!format.shouldWrap() && text.indexOf('\n') == -1) {
      lines.add(new Line(text, paint.measureText(text)));
    } else {
      for (String line : text.split("\\n")) {
        breakLine(line);
      }
    }

    // compute the text height based on the metrics
    float twidth = 0;
    for (Line line : lines) {
      twidth = Math.max(twidth, line.width);
    }
    float theight = lines.size() * (-metrics.ascent + metrics.descent) +
      (lines.size()-1) * metrics.leading; // leading only applies to lines after 0

    // finalize our our width and height calculations
    width = format.effect.adjustWidth(twidth);
    height = format.effect.adjustHeight(theight);
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
          lines.add(new Line(text.substring(start, lineEnd), measuredWidth[0]));
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

  void draw(Canvas canvas, float x, float y) {
    if (format.effect instanceof TextFormat.Effect.Shadow) {
      // TODO: look into Android built-in support for drawing text with shadows
      TextFormat.Effect.Shadow seffect = (TextFormat.Effect.Shadow)format.effect;
      // if the shadow is negative, we need to move the real text down/right to keep everything
      // within our bounds
      float tx, sx, ty, sy;
      if (seffect.shadowOffsetX > 0) {
        tx = 0;
        sx = seffect.shadowOffsetX;
      } else {
        tx = -seffect.shadowOffsetX;
        sx = 0;
      }
      if (seffect.shadowOffsetY > 0) {
        ty = 0;
        sy = seffect.shadowOffsetY;
      } else {
        ty = -seffect.shadowOffsetY;
        sy = 0;
      }
      paint.setColor(format.effect.getAltColor());
      drawOnce(canvas, x + sx, y + sy);
      paint.setColor(format.textColor);
      drawOnce(canvas, x + tx, y + ty);

    } else if (format.effect instanceof TextFormat.Effect.Outline) {
      // TODO: check whether Android's outline text drawing sucks less than Java's
      paint.setColor(format.effect.getAltColor());
      drawOnce(canvas, x+0, y+0);
      drawOnce(canvas, x+0, y+1);
      drawOnce(canvas, x+0, y+2);
      drawOnce(canvas, x+1, y+0);
      drawOnce(canvas, x+1, y+2);
      drawOnce(canvas, x+2, y+0);
      drawOnce(canvas, x+2, y+1);
      drawOnce(canvas, x+2, y+2);

      paint.setColor(format.textColor);
      drawOnce(canvas, x+1, y+1);

    } else {
      paint.setColor(format.textColor);
      drawOnce(canvas, x, y);
    }
  }

  void drawOnce(Canvas canvas, float x, float y) {
    float yoff = 0;
    for (Line line : lines) {
      float rx = format.align.getX(line.width, width);
      yoff += -metrics.ascent;
      canvas.drawText(line.text, x + rx, y + yoff, paint);
      if (line != lines.get(0)) {
        yoff += metrics.leading; // add interline spacing
      }
      yoff += metrics.descent;
    }
  }
}
