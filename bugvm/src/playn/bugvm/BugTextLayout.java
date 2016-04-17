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
package playn.bugvm;

import java.util.List;

import com.bugvm.apple.corefoundation.CFRange;
import com.bugvm.apple.coregraphics.CGAffineTransform;
import com.bugvm.apple.coregraphics.CGBitmapContext;
import com.bugvm.apple.coregraphics.CGPath;
import com.bugvm.apple.coregraphics.CGRect;
import com.bugvm.apple.coretext.CTFont;
import com.bugvm.apple.coretext.CTFrame;
import com.bugvm.apple.coretext.CTFramesetter;
import com.bugvm.apple.coretext.CTLine;
import com.bugvm.apple.foundation.NSAttributedString;
import com.bugvm.apple.uikit.NSAttributedStringAttributes;
import com.bugvm.apple.uikit.UIColor;
import com.bugvm.apple.uikit.UIFont;

import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.TextWrap;
import pythagoras.f.Rectangle;

class BugTextLayout extends TextLayout {

  public static BugTextLayout layoutText(BugGraphics gfx, final String text, TextFormat format) {
    final CTFont font = BugFont.resolveFont(format.font);
    NSAttributedStringAttributes attribs = createAttribs(font);
    CTLine line = CTLine.create(new NSAttributedString(text, attribs));
    return new BugTextLayout(gfx, text, format, font, line);
  }

  public static BugTextLayout[] layoutText(BugGraphics gfx, String text, TextFormat format,
                                            TextWrap wrap) {
    text = normalizeEOL(text);

    final CTFont font = BugFont.resolveFont(format.font);
    NSAttributedStringAttributes attribs = createAttribs(font);
    List<CTLine> lines = wrapLines(new NSAttributedString(text, attribs), wrap.width);

    BugTextLayout[] layouts = new BugTextLayout[lines.size()];
    for (int ii = 0; ii < layouts.length; ii++) {
      CTLine line = lines.get(ii);
      CFRange range = line.getStringRange();
      String ltext = text.substring((int)range.getLocation(),
                                    (int)(range.getLocation()+range.getLength()));
      layouts[ii] = new BugTextLayout(gfx, ltext, format, font, line);
    }
    return layouts;
  }

  private static NSAttributedStringAttributes createAttribs(CTFont font) {
    NSAttributedStringAttributes attribs = new NSAttributedStringAttributes();
    attribs.setFont(font.as(UIFont.class));
    // attribs.setForegroundColorFromContext(true); // TODO
    return attribs;
  }

  private static void addStroke(NSAttributedStringAttributes attribs, CTFont font,
                                float strokeWidth, int strokeColor) {
    // stroke width is expressed as a percentage of the font size in iOS
    double strokePct = 100 * strokeWidth / font.getSize();
    attribs.setStrokeWidth(strokePct);
    // unfortunately we have to set the stroke color here, we cannot inherit it from the context
    attribs.setStrokeColor(toUIColor(strokeColor));
  }

  private static List<CTLine> wrapLines(NSAttributedString astring, float wrapWidth) {
    CTFramesetter fs = CTFramesetter.create(astring);
    try {
      // iOS lays things out from max-y up to zero (inverted coordinate system); so we need to
      // provide a large height for our rectangle to ensure that all lines "fit"
      CGPath path = CGPath.createWithRect(
        new CGRect(0, 0, wrapWidth, Float.MAX_VALUE/2), CGAffineTransform.Identity());
      CTFrame frame = fs.createFrame(new CFRange(0, 0), path, null);
      return frame.getLines();
    } finally {
      fs.dispose();
    }
  }

  private final CTFont font;
  private CTLine fillLine;
  private int fillColor;
  private CTLine strokeLine; // initialized lazily
  private float strokeWidth;
  private int strokeColor;

  private BugTextLayout(BugGraphics gfx, String text, TextFormat format, CTFont font,
                         CTLine fillLine) {
    super(text, format, computeBounds(font, fillLine.getImageBounds(gfx.scratchCtx)),
          (float)(font.getAscent()+font.getDescent()));
    this.font = font;
    this.fillLine = fillLine;
  }

  @Override
  public float ascent() {
    return (float)font.getAscent();
  }

  @Override
  public float descent() {
    return (float)font.getDescent();
  }

  @Override
  public float leading() {
    return (float)font.getLeading();
  }

  void stroke(CGBitmapContext bctx, float x, float y, float strokeWidth, int strokeColor) {
    if (strokeLine == null || strokeWidth != this.strokeWidth || strokeColor != this.strokeColor) {
      this.strokeWidth = strokeWidth;
      this.strokeColor = strokeColor;
      NSAttributedStringAttributes attribs = createAttribs(font);
      addStroke(attribs, font, strokeWidth, strokeColor);
      strokeLine = CTLine.create(new NSAttributedString(text, attribs));
    }
    paint(bctx, strokeLine, x, y);
  }

  void fill(CGBitmapContext bctx, float x, float y, int fillColor) {
    if (this.fillColor != fillColor){
      this.fillColor = fillColor;
      NSAttributedStringAttributes attribs = createAttribs(font);
      attribs.setForegroundColor(toUIColor(fillColor));
      fillLine = CTLine.create(new NSAttributedString(text, attribs));
    }
    paint(bctx, fillLine, x, y);
  }

  private void paint(CGBitmapContext bctx, CTLine line, float x, float y) {
    bctx.saveGState();
    bctx.translateCTM(x, y + ascent());
    bctx.scaleCTM(1, -1);
    bctx.setShouldAntialias(format.antialias);
    bctx.setTextPosition(0, 0);
    line.draw(bctx);
    bctx.restoreGState();
  }

  private static UIColor toUIColor(int color) {
    float blue = (color & 0xFF) / 255f;
    color >>= 8;
    float green = (color & 0xFF) / 255f;
    color >>= 8;
    float red = (color & 0xFF) / 255f;
    color >>= 8;
    float alpha = (color & 0xFF) / 255f;
    return new UIColor(red, green, blue, alpha);
  }

  private static Rectangle computeBounds(CTFont font, CGRect bounds) {
    // the y coordinate of bounds is a little tricky: iOS reports y as the number of pixels to
    // below the baseline that the text extends (the descent, but precisely for this text, not the
    // font's "maximum" descent) and the value is negative (due to the inverted coordinate system);
    // so we have to do some math to recover the desired y value which is the number of pixels
    // below the top-left of the line bounding box
    float ascent = (float)font.getAscent();
    return new Rectangle((float)bounds.getMinX(),
                         ascent - (float)(bounds.getHeight() + bounds.getMinY()),
                         (float)bounds.getWidth(), (float)bounds.getHeight());
  }
}
