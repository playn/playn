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
import cli.MonoTouch.CoreText.CTParagraphStyle;
import cli.MonoTouch.CoreText.CTParagraphStyleSettings;
import cli.MonoTouch.CoreText.CTStringAttributes;
import cli.MonoTouch.CoreText.CTTextAlignment;
import cli.MonoTouch.Foundation.NSAttributedString;
import cli.MonoTouch.Foundation.NSRange;
import cli.System.Drawing.PointF;
import cli.System.Drawing.RectangleF;

import playn.core.PaddedTextLayout;
import playn.core.TextFormat;
import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

class OldIOSTextLayout extends PaddedTextLayout implements IOSCanvas.Drawable {

  // There are numerous impedance mismatches between how PlayN wants to layout text and how iOS
  // allows text to be laid out. Fortunately, with some hackery, we can make things work (quite
  // nicely, in fact: iOS does not lie about its text measurements, unlike every other platform for
  // which I've ever written text rendering code).
  //
  // The first problem is the inverted coordinate system: CoreGraphics uses OpenGL coordinates, and
  // PlayN uses "Imperial" origin-in-upper-left coordinates. This impacts the actual rendering
  // (where we have to do the same unflipping that we do when rendering images), but it also
  // impacts text wrapping. When wrapping text in iOS you give it a rectangle (an arbitrary "clip"
  // path, actually) into which to constrain the text, and the coordinates at which the text is
  // laid out are retained and used later when rendering. This is problematic because we want to
  // lay out text into an infinitely tall rectangle. That's not a problem when lines start at 0,0
  // and y increases for each line, until you get to the last. But in iOS/OpenGL coordinates, that
  // means lines starts at 0,MAX_FLOAT and y decreases line by line until the last line, which is
  // still at some exceedingly large y value. To accommodate that, we use a rectangle of height
  // MAX_HEIGHT (a large value used instead of Float.MAX_VALUE to avoid overflow problems). We now
  // render lines ourselves instead of having the CTFrame do the rendering, so we only need
  // MAX_HEIGHT during layout. We ignore the assigned y positions during rendering.
  //
  // The next problem is that when wrapping text in said region, the x position of each line is
  // recorded and used when rendering the text. This x coordinate accounts for justification, so if
  // I say I want a maximum line width of 200, the lines will be laid out such that they are flush
  // to the right of a rectangle that is 200 pixels wide. The widest line may only be 180 pixels,
  // and that line will start at x = 20. In PlayN, we want to "trim" the bounds of the wrapped text
  // to the width of the widest line and report the width of the entire TextLayout as that widest
  // line's width. The aligned text is then aligned based on the bounds established by that widest
  // line. To achieve that behavior, we record the smallest x offset of any of the lines and store
  // that in adjustX, then translate by -adjustX when rendering the text. More hackery!
  //
  // We have to be careful about measuring the width of text layout as well. GetImageBounds()
  // returns the precise pixel bounds of the rendered text, but some glyphs have extra whitespace
  // to their right, so we can't use the width of the widest line (as computed by GetImageBounds())
  // to compute the width of the entire TextLayout, rather we have to measure the maximum X pixel
  // of each line and subtract minX from maxX to ensure that we accommodate the rightmost rendered
  // pixel, which may be on a line other than the widest.
  //
  // Finally, we have to use the line origins reported by CTFrame to calculate the inter-line
  // spacing, because that does not seem to be reported by CTFont.LeadingMetric (which is where I
  // would expect it to be reported), and it is non-zero.

  private abstract class IOSTextStamp {
    public float width, height, ascent, descent, leading;

    public abstract int lineCount();
    public abstract Rectangle lineBounds(int line);
    public abstract void paint(CGBitmapContext bctx, float x, float y, boolean antialias);
  }

  private class Wrapped extends IOSTextStamp {
    protected static final float MAX_HEIGHT = 10000f;
    private final CTLine[] lines;
    private final PointF[] origins;
    private final float adjustX;
    private final float lineHeight;

    Wrapped(IOSFont font, CTStringAttributes attribs, String text) {
      this.ascent = font.ctFont.get_AscentMetric();
      this.descent = font.ctFont.get_DescentMetric();
      this.leading = font.ctFont.get_LeadingMetric();
      this.lineHeight = ascent + descent + leading;

      NSAttributedString atext = new NSAttributedString(text, attribs);
      CTFramesetter fs = new CTFramesetter(atext);
      try {
        CGPath path = new CGPath();
        path.AddRect(new RectangleF(0, 0, format.wrapWidth, MAX_HEIGHT));
        CTFrame frame = fs.GetFrame(new NSRange(0, 0), path, null);
        this.lines = frame.GetLines();
        this.origins = new PointF[lines.length];
        frame.GetLineOrigins(new NSRange(0, 0), origins);
        float minX = Float.MAX_VALUE, maxX = 0;
        for (int ii = 0; ii < lines.length; ii++) {
          RectangleF bounds = lines[ii].GetImageBounds(gfx.scratchCtx);
          float ox = origins[ii].get_X(), lineX = ox + bounds.get_X() + bounds.get_Width();
          minX = Math.min(ox, minX);
          maxX = Math.max(maxX, lineX);
        }
        this.adjustX = -minX;
        this.width = maxX - minX;
        this.height = lineHeight * lines.length - leading;

      } finally {
        fs.Dispose();
      }
    }

    @Override
    public int lineCount() {
      return lines.length;
    }

    @Override
    public Rectangle lineBounds (int line) {
      // TODO: maybe cache bounds for lines?
      RectangleF bounds = lines[line].GetImageBounds(gfx.scratchCtx);
      return new Rectangle(origins[line].get_X()+pad+adjustX, line*lineHeight+pad,
        bounds.get_Width(), lineHeight);
    }

    @Override
    public void paint(CGBitmapContext bctx, float x, float y, boolean antialias) {
      float dx = x + adjustX, dy = y + ascent;
      bctx.SaveState();
      bctx.TranslateCTM(dx, dy);
      bctx.ScaleCTM(1, -1);
      bctx.SetShouldAntialias(antialias);
      PointF origin = new PointF(0, 0);
      for (int ii = 0; ii < lines.length; ii++) {
        origin.set_X(origins[ii].get_X());
        bctx.set_TextPosition(origin);
        lines[ii].Draw(bctx);
        bctx.TranslateCTM(0, -lineHeight);
      }
      bctx.RestoreState();
    }
  }

  private class Single extends IOSTextStamp {
    private final CTLine line;
    private final RectangleF bounds;

    Single(IOSFont font, CTStringAttributes attribs, String text) {
      this.ascent = font.ctFont.get_AscentMetric();
      this.descent = font.ctFont.get_DescentMetric();
      this.leading = font.ctFont.get_LeadingMetric();
      this.line = new CTLine(new NSAttributedString(text, attribs));
      this.bounds = line.GetImageBounds(gfx.scratchCtx);
      this.width = bounds.get_X() + bounds.get_Width();
      this.height = ascent + descent;
    }

    @Override
    public int lineCount() {
      return 1;
    }

    @Override
    public Rectangle lineBounds(int line) {
      return new Rectangle(pad, pad, width, height);
    }

    @Override
    public void paint(CGBitmapContext bctx, float x, float y, boolean antialias) {
      float dy = y + ascent;
      bctx.SaveState();
      bctx.TranslateCTM(x, dy);
      bctx.ScaleCTM(1, -1);
      bctx.SetShouldAntialias(antialias);
      bctx.set_TextPosition(new PointF(0, 0));
      line.Draw(bctx);
      bctx.RestoreState();
    }
  }

  private final IOSGraphics gfx;
  private final String text;
  private final IOSTextStamp fillStamp;
  private IOSTextStamp strokeStamp; // initialized lazily
  private float strokeWidth;

  public OldIOSTextLayout(IOSGraphics gfx, String text, TextFormat format) {
    super(gfx, text, format);
    this.gfx = gfx;
    // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
    this.text = text.replace("\r\n", "\n").replace('\r', '\n');
    this.fillStamp = createStamp(this.text, null, null);
    this.width = fillStamp.width;
    this.height = fillStamp.height;
  }

  @Override
  public IRectangle bounds() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int lineCount() {
    return fillStamp.lineCount();
  }

  @Override
  public Rectangle lineBounds(int line) {
    return fillStamp.lineBounds(line);
  }

  @Override
  public float ascent() {
    return fillStamp.ascent;
  }

  @Override
  public float descent() {
    return fillStamp.descent;
  }

  @Override
  public float leading() {
    return fillStamp.leading;
  }

  public void stroke(CGBitmapContext bctx, float x, float y, float strokeWidth, int strokeColor) {
    if (strokeStamp == null || strokeWidth != this.strokeWidth) {
      this.strokeWidth = strokeWidth;
      strokeStamp = createStamp(text, strokeWidth, strokeColor);
    }
    strokeStamp.paint(bctx, x+pad, y+pad, format.antialias);
  }

  public void fill(CGBitmapContext bctx, float x, float y) {
    fillStamp.paint(bctx, x+pad, y+pad, format.antialias);
  }

  private IOSTextStamp createStamp(String text, Float strokeWidth, Integer strokeColor) {
    CTStringAttributes attribs = new CTStringAttributes();
    IOSFont font = (format.font == null) ? IOSGraphics.defaultFont : (IOSFont) format.font;
    attribs.set_Font(font.ctFont);
    attribs.set_ForegroundColorFromContext(true);

    if (strokeWidth != null) {
      // stroke width is expressed as a percentage of the font size in iOS
      float strokePct = 100 * strokeWidth / font.size();
      attribs.set_StrokeWidth(new cli.System.Nullable$$00601_$$$_F_$$$$_(strokePct));
      // unfortunately we have to set the stroke color here, we cannot inherit it from the context
      attribs.set_StrokeColor(IOSCanvas.toCGColor(strokeColor));
    }

    CTParagraphStyleSettings pstyle = new CTParagraphStyleSettings();
    // the "view C# as Java" abstraction is suffering a bit here; please avert your eyes
    pstyle.set_Alignment(new cli.System.Nullable$$00601_$$$_Lcli__MonoTouch__CoreText__CTTextAlignment_$$$$_(toCT(format.align)));
    attribs.set_ParagraphStyle(new CTParagraphStyle(pstyle));

    if (format.shouldWrap() || text.indexOf('\n') != -1) {
      return new Wrapped(font, attribs, text);
    } else {
      return new Single(font, attribs, text);
    }
  }

  private static CTTextAlignment toCT(TextFormat.Alignment align) {
    switch (align) {
    default:
    case LEFT: return CTTextAlignment.wrap(CTTextAlignment.Left);
    case CENTER: return CTTextAlignment.wrap(CTTextAlignment.Center);
    case RIGHT: return CTTextAlignment.wrap(CTTextAlignment.Right);
    }
  }
}
