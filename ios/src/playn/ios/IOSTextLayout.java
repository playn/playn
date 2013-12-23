/**
 * Copyright 2012 The PlayN Authors
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
package playn.ios;

import cli.MonoTouch.CoreGraphics.CGBitmapContext;
import cli.MonoTouch.CoreGraphics.CGPath;
import cli.MonoTouch.CoreText.CTFrame;
import cli.MonoTouch.CoreText.CTFramesetter;
import cli.MonoTouch.CoreText.CTLine;
import cli.MonoTouch.CoreText.CTStringAttributes;
import cli.MonoTouch.Foundation.NSAttributedString;
import cli.MonoTouch.Foundation.NSRange;
import cli.System.Drawing.PointF;
import cli.System.Drawing.RectangleF;

import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

import playn.core.AbstractTextLayout;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.TextWrap;

class IOSTextLayout implements TextLayout, IOSCanvas.Drawable {

  public static IOSTextLayout layoutText(IOSGraphics gfx, final String text, TextFormat format) {
    final IOSFont font = (format.font == null) ? IOSGraphics.defaultFont : (IOSFont) format.font;
    CTStringAttributes attribs = createAttribs(font);
    CTLine line = new CTLine(new NSAttributedString(text, attribs));
    return new IOSTextLayout(gfx, text, format, font, line);
  }

  public static IOSTextLayout[] layoutText(IOSGraphics gfx, String text, TextFormat format,
                                           TextWrap wrap) {
    text = AbstractTextLayout.normalizeEOL(text);

    final IOSFont font = (format.font == null) ? IOSGraphics.defaultFont : (IOSFont) format.font;
    CTStringAttributes attribs = createAttribs(font);
    CTLine[] lines = wrapLines(gfx, new NSAttributedString(text, attribs), wrap.width);

    IOSTextLayout[] layouts = new IOSTextLayout[lines.length];
    for (int ii = 0; ii < lines.length; ii++) {
      NSRange range = lines[ii].get_StringRange();
      String ltext = text.substring(range.Location, range.Location+range.Length);
      layouts[ii] = new IOSTextLayout(gfx, ltext, format, font, lines[ii]);
    }
    return layouts;
  }

  private static CTStringAttributes createAttribs(IOSFont font) {
    CTStringAttributes attribs = new CTStringAttributes();
    attribs.set_Font(font.ctFont);
    attribs.set_ForegroundColorFromContext(true);
    return attribs;
  }

  private static void addStroke(CTStringAttributes attribs, IOSFont font,
                                float strokeWidth, int strokeColor) {
    // stroke width is expressed as a percentage of the font size in iOS
    float strokePct = 100 * strokeWidth / font.size();
    attribs.set_StrokeWidth(new cli.System.Nullable$$00601_$$$_F_$$$$_(strokePct));
    // unfortunately we have to set the stroke color here, we cannot inherit it from the context
    attribs.set_StrokeColor(IOSCanvas.toCGColor(strokeColor));
  }

  private static CTLine[] wrapLines(IOSGraphics gfx, NSAttributedString astring, float wrapWidth) {
    CTFramesetter fs = new CTFramesetter(astring);
    try {
      CGPath path = new CGPath();
      // iOS lays things out from max-y up to zero (inverted coordinate system); so we need to
      // provide a large height for our rectangle to ensure that all lines "fit"
      path.AddRect(new RectangleF(0, 0, wrapWidth, Float.MAX_VALUE/2));
      CTFrame frame = fs.GetFrame(new NSRange(0, 0), path, null);
      return frame.GetLines();
    } finally {
      fs.Dispose();
    }
  }

  private final String text;
  private final TextFormat format;
  private final IOSFont font;
  private final CTLine fillLine;
  private final Rectangle bounds;
  private CTLine strokeLine; // initialized lazily
  private float strokeWidth;
  private int strokeColor;

  private IOSTextLayout(IOSGraphics gfx, String text, TextFormat format, IOSFont font,
                        CTLine fillLine) {
    this.text = text;
    this.format = format;
    this.font = font;
    this.fillLine = fillLine;
    RectangleF bounds = fillLine.GetImageBounds(gfx.scratchCtx);
    // the y coordinate of bounds is a little tricky: iOS reports y as the number of pixels to
    // below the baseline that the text extends (the descent, but precisely for this text, not the
    // font's "maximum" descent) and the value is negative (due to the inverted coordinate system);
    // so we have to do some math to recover the desired y value which is the number of pixels
    // below the top-left of the line bounding box
    this.bounds = new Rectangle(bounds.get_X(), ascent() - (bounds.get_Height() + bounds.get_Y()),
                                bounds.get_Width(), bounds.get_Height());
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
    return Math.max(bounds.x, 0) + bounds.width;
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
  public int lineCount() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Rectangle lineBounds(int line) {
    throw new UnsupportedOperationException();
  }

  @Override
  public float ascent() {
    return font.ctFont.get_AscentMetric();
  }

  @Override
  public float descent() {
    return font.ctFont.get_DescentMetric();
  }

  @Override
  public float leading() {
    return font.ctFont.get_LeadingMetric();
  }

  public void stroke(CGBitmapContext bctx, float x, float y, float strokeWidth, int strokeColor) {
    if (strokeLine == null || strokeWidth != this.strokeWidth || strokeColor != this.strokeColor) {
      this.strokeWidth = strokeWidth;
      this.strokeColor = strokeColor;
      CTStringAttributes attribs = createAttribs(font);
      addStroke(attribs, font, strokeWidth, strokeColor);
      strokeLine = new CTLine(new NSAttributedString(text, attribs));
    }
    paint(bctx, strokeLine, x, y);
  }

  public void fill(CGBitmapContext bctx, float x, float y) {
    paint(bctx, fillLine, x, y);
  }

  private void paint(CGBitmapContext bctx, CTLine line, float x, float y) {
    bctx.SaveState();
    bctx.TranslateCTM(x, y + ascent());
    bctx.ScaleCTM(1, -1);
    bctx.SetShouldAntialias(format.antialias);
    bctx.set_TextPosition(new PointF(0, 0));
    line.Draw(bctx);
    bctx.RestoreState();
  }
}
