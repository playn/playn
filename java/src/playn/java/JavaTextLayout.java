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

import pythagoras.f.Rectangle;

import playn.core.AbstractTextLayout;
import playn.core.TextFormat;
import playn.core.TextWrap;

class JavaTextLayout extends AbstractTextLayout {

  public static JavaTextLayout layoutText(JavaGraphics gfx, String text, TextFormat format) {
    // we do some fiddling to work around the fact that TextLayout chokes on the empty string
    AttributedString astring = new AttributedString(text.length() == 0 ? " " : text);
    if (format.font != null) {
      astring.addAttribute(TextAttribute.FONT, ((JavaFont)format.font).jfont);
    }
    FontRenderContext frc = format.antialias ? gfx.aaFontContext : gfx.aFontContext;
    return new JavaTextLayout(text, format, new TextLayout(astring.getIterator(), frc));
  }

  public static JavaTextLayout[] layoutText(JavaGraphics gfx, String text, TextFormat format,
                                            TextWrap wrap) {
    // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
    text = normalizeEOL(text);

    // we do some fiddling to work around the fact that TextLayout chokes on the empty string
    String ltext = text.length() == 0 ? " " : text;

    // set up an attributed character iterator so that we can measure the text
    AttributedString astring = new AttributedString(ltext);
    if (format.font != null) {
      astring.addAttribute(TextAttribute.FONT, ((JavaFont)format.font).jfont);
    }

    List<JavaTextLayout> layouts = new ArrayList<JavaTextLayout>();
    FontRenderContext frc = format.antialias ? gfx.aaFontContext : gfx.aFontContext;
    LineBreakMeasurer measurer = new LineBreakMeasurer(astring.getIterator(), frc);
    int lastPos = ltext.length(), curPos = 0;
    char eol = '\n';
    while (curPos < lastPos) {
      int nextRet = ltext.indexOf(eol, measurer.getPosition()+1);
      if (nextRet == -1) {
        nextRet = lastPos;
      }
      TextLayout layout = measurer.nextLayout(wrap.width, nextRet, false);
      int endPos = measurer.getPosition();
      while (curPos < endPos && ltext.charAt(curPos) == eol)
        curPos += 1; // skip over EOLs
      layouts.add(new JavaTextLayout(ltext.substring(curPos, endPos), format, layout));
      curPos = endPos;
    }
    return layouts.toArray(new JavaTextLayout[layouts.size()]);
  }

  private final TextLayout layout;

  JavaTextLayout(String text, TextFormat format, TextLayout layout) {
    super(text, format, computeBounds(layout));
    this.layout = layout;
  }

  @Override
  public float ascent () {
    return layout.getAscent();
  }

  @Override
  public float descent () {
    return layout.getDescent();
  }

  @Override
  public float leading () {
    return layout.getLeading();
  }

  void stroke(Graphics2D gfx, float x, float y) {
    paint(gfx, x, y, true);
  }

  void fill(Graphics2D gfx, float x, float y) {
    paint(gfx, x, y, false);
  }

  void paint(Graphics2D gfx, float x, float y, boolean stroke) {
    Object ohint = gfx.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    try {
      gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, format.antialias ?
                           RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

      float yoff = y + layout.getAscent();
      if (stroke) {
        gfx.translate(x, yoff);
        gfx.draw(layout.getOutline(null));
        gfx.translate(-x, -yoff);
      } else {
        layout.draw(gfx, x, yoff);
      }

    } finally {
      gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, ohint);
    }
  }

  private static Rectangle computeBounds (TextLayout layout) {
    Rectangle2D bounds = layout.getBounds();
    // the y position of the bounds includes a negative ascent, but we don't want that showing up
    // in our bounds since we render from 0 rather than from the baseline
    return new Rectangle((float)bounds.getX(), (float)bounds.getY() + layout.getAscent(),
                         (float)bounds.getWidth(), (float)bounds.getHeight());
  }
}
