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
package playn.java;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import playn.core.PaddedTextLayout;
import playn.core.TextFormat;
import pythagoras.f.Rectangle;

class JavaTextLayout extends PaddedTextLayout {

  private List<TextLayout> layouts = new ArrayList<TextLayout>();
  private final float xAdjust;

  public JavaTextLayout(JavaGraphics gfx, String text, TextFormat format) {
    super(gfx, text, format);

    // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
    text = text.replace("\r\n", "\n").replace('\r', '\n');

    // we do some fiddling to work around the fact that TextLayout chokes on the empty string
    boolean isEmptyString = text.length() == 0;
    String ltext = isEmptyString ? " " : text;

    // set up an attributed character iterator so that we can measure the text
    AttributedString astring = new AttributedString(ltext);
    if (format.font != null) {
      astring.addAttribute(TextAttribute.FONT, ((JavaFont)format.font).jfont);
    }

    FontRenderContext frc = format.antialias ? gfx.aaFontContext : gfx.aFontContext;
    if (format.shouldWrap() || ltext.indexOf('\n') != -1) {
      LineBreakMeasurer measurer = new LineBreakMeasurer(astring.getIterator(), frc);
      char eol = '\n'; // TODO: platform line endings?
      int lastPos = ltext.length();
      while (measurer.getPosition() < lastPos) {
        int nextRet = ltext.indexOf(eol, measurer.getPosition()+1);
        if (nextRet == -1) {
          nextRet = lastPos;
        }
        layouts.add(measurer.nextLayout(format.wrapWidth, nextRet, false));
      }
    } else {
      layouts.add(new TextLayout(astring.getIterator(), frc));
    }

    // some font glyphs start rendering at a negative inset, blowing outside their bounding box
    // (naughty!); in such cases, we shift everything to the right to ensure that we don't paint
    // outside our reported bounding box (so that someone can create a single canvas of bounding
    // box size and render this text layout into it at (0,0) and nothing will get cut off)
    float maxXAdjust = 0;
    // compute our total width and height
    float twidth = 0, theight = 0;
    for (TextLayout layout : layouts) {
      Rectangle2D bounds = layout.getBounds();
      maxXAdjust = Math.max(maxXAdjust, -Math.min(0, (float)bounds.getX()));
      twidth = Math.max(twidth, getWidth(bounds));
      if (layout != layouts.get(0)) {
        theight += layout.getLeading(); // leading only applied to lines after 0
      }
      theight += (layout.getAscent() + layout.getDescent());
    }
    width = isEmptyString ? 0 : twidth;
    height = theight;
    xAdjust = maxXAdjust;
  }

  @Override
  public float width() {
    return super.width() + xAdjust;
  }

  @Override
  public int lineCount() {
    return layouts.size();
  }

  @Override
  public Rectangle lineBounds(int line) {
    Rectangle2D bounds = layouts.get(line).getBounds();
    float lineWidth = getWidth(bounds);
    float x = xAdjust + format.align.getX(lineWidth, width);
    float y = line == 0 ? 0 : line * (ascent() + descent() + leading());
    return new Rectangle(x+pad, y+pad, lineWidth, ascent()+descent());
  }

  @Override
  public float ascent() {
    return layouts.size() == 0 ? 0 : layouts.get(0).getAscent();
  }

  @Override
  public float descent() {
    return layouts.size() == 0 ? 0 : layouts.get(0).getDescent();
  }

  @Override
  public float leading() {
    return layouts.size() == 0 ? 0 : layouts.get(0).getLeading();
  }

  void stroke(Graphics2D gfx, float x, float y) {
    paint(gfx, x+pad, y+pad, true);
  }

  void fill(Graphics2D gfx, float x, float y) {
    paint(gfx, x+pad, y+pad, false);
  }

  void paint(Graphics2D gfx, float x, float y, boolean stroke) {
    float yoff = y;
    Object ohint = gfx.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    try {
      gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, format.antialias ?
                           RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

      for (TextLayout layout : layouts) {
        Rectangle2D bounds = layout.getBounds();
        float sx = x + xAdjust + format.align.getX(getWidth(bounds), width);
        yoff += layout.getAscent();
        if (stroke) {
          gfx.translate(sx, yoff);
          gfx.draw(layout.getOutline(null));
          gfx.translate(-sx, -yoff);
        } else {
          layout.draw(gfx, sx, yoff);
        }
        yoff += layout.getDescent() + layout.getLeading();
      }

    } finally {
      gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, ohint);
    }
  }

  private static float getWidth(Rectangle2D bounds) {
    // if the x position is positive, we need to account for the fact that getWidth doesn't include
    // this leading whitespace, but we need to include it in our bounds; we don't need to worry
    // about xAdjust here because that's accounted elsewhere
    return (float)(Math.max(0, bounds.getX()) + bounds.getWidth());
  }
}
