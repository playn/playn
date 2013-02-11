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
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import playn.core.AbstractTextLayout;
import playn.core.TextFormat;
import pythagoras.f.Rectangle;

class JavaTextLayout extends AbstractTextLayout {

  private static FontRenderContext dummyFontContext = createDummyFRC();

  private List<TextLayout> layouts = new ArrayList<TextLayout>();

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

    if (format.shouldWrap() || ltext.indexOf('\n') != -1) {
      LineBreakMeasurer measurer = new LineBreakMeasurer(astring.getIterator(), dummyFontContext);
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
      layouts.add(new TextLayout(astring.getIterator(), dummyFontContext));
    }

    // compute our width and height
    float twidth = 0, theight = 0;
    for (TextLayout layout : layouts) {
      Rectangle2D bounds = layout.getBounds();
      twidth = Math.max(twidth, getWidth(bounds));
      if (layout != layouts.get(0)) {
        theight += layout.getLeading(); // leading only applied to lines after 0
      }
      theight += (layout.getAscent() + layout.getDescent());
    }
    width = isEmptyString ? 0 : twidth;
    height = theight;
  }

  @Override
  public int lineCount() {
    return layouts.size();
  }

  @Override
  public Rectangle lineBounds(int line) {
    if (line < 0 || layouts.size() <= line) return null;
    Rectangle2D bounds = layouts.get(line).getBounds();
    float lineWidth = getWidth(bounds);
    float x = (float)-bounds.getX() + format.align.getX(lineWidth, width);
    float y = line == 0 ? 0 : line * (ascent() + descent() + leading());
    return new Rectangle(x, y, lineWidth, (float)bounds.getHeight());
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
    for (TextLayout layout : layouts) {
      Rectangle2D bounds = layout.getBounds();
      // some fonts starting rendering inset to the right, and others start rendering at a negative
      // inset, blowing outside their bounding box (naughty!); for the former, we trim off that
      // inset and for the latter we shift everything to the right to ensure that we don't paint
      // outside our reported bounding box (so that someone can create a single canvas of bounding
      // box size and render this text layout into it at (0,0) and nothing will get cut off)
      float sx = x + (float)-bounds.getX() + format.align.getX(getWidth(bounds), width);
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
  }

  private static float getWidth(Rectangle2D bounds) {
    // if our text includes a negative inset, that needs to be tacked onto the width
    return (float)(Math.max(-bounds.getX(), 0) + bounds.getWidth());
  }

  private static FontRenderContext createDummyFRC () {
    Graphics2D gfx = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    return gfx.getFontRenderContext();
  }
}
