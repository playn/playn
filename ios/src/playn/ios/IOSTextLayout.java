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

import playn.core.TextFormat;
import playn.core.TextLayout;

public abstract class IOSTextLayout implements TextLayout {

  protected final IOSGraphics gfx;
  protected final TextFormat format;
  protected final IOSFont font;

  public static IOSTextLayout create(IOSGraphics gfx, String text, TextFormat format) {
    // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
    text = text.replace("\r\n", "\n").replace('\r', '\n');

    IOSFont font = (format.font == null) ? gfx.defaultFont : (IOSFont) format.font;
    CTStringAttributes attribs = new CTStringAttributes();
    attribs.set_Font(font.ctFont);
    attribs.set_ForegroundColorFromContext(true);

    if (format.effect instanceof TextFormat.Effect.Outline) {
      attribs.set_StrokeColor(IOSCanvas.toCGColor(format.effect.getAltColor()));
      // negative stroke width means stroke and fill, rather than just stroke
      attribs.set_StrokeWidth(new cli.System.Nullable$$00601_$$$_F_$$$$_(-3));
    }

    CTParagraphStyleSettings pstyle = new CTParagraphStyleSettings();
    // the "view C# as Java" abstraction is suffering a bit here; please avert your eyes
    pstyle.set_Alignment(new cli.System.Nullable$$00601_$$$_Lcli__MonoTouch__CoreText__CTTextAlignment_$$$$_(toCT(format.align)));
    attribs.set_ParagraphStyle(new CTParagraphStyle(pstyle));
    // TODO: add underline here; foreground/stroke color also?
    NSAttributedString atext = new NSAttributedString(text, attribs);

    if (format.shouldWrap() || text.contains("\\n")) {
      return new Wrapped(gfx, format, font, atext);
    } else {
      return new Single(gfx, format, font, atext);
    }
  }

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
  // MAX_HEIGHT (a large value used instead of Float.MAX_VALUE to avoid overflow problems) and we
  // translate the graphics context by -MAX_HEIGHT when we render. Hacky, but it works.
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

  private static class Wrapped extends IOSTextLayout {
    protected static final float MAX_HEIGHT = 10000f;

    private final CTFrame frame;
    private final float width, height;
    private final float lineHeight;
    private final float adjustX;

    Wrapped(IOSGraphics gfx, TextFormat format, IOSFont font, NSAttributedString atext) {
      super(gfx, format, font);

      float fontLineHeight = font.ctFont.get_AscentMetric() + font.ctFont.get_DescentMetric() +
        font.ctFont.get_LeadingMetric();

      CTFramesetter fs = new CTFramesetter(atext);
      try {
        CGPath path = new CGPath();
        path.AddRect(new RectangleF(0, 0, format.wrapWidth, MAX_HEIGHT));
        this.frame = fs.GetFrame(new NSRange(0, 0), path, null);

        PointF[] origins = new PointF[frame.GetLines().length];
        frame.GetLineOrigins(new NSRange(0, 0), origins);
        float minX = Float.MAX_VALUE;
        for (PointF origin : origins) {
          minX = Math.min(origin.get_X(), minX);
        }
        adjustX = -minX;

        float maxX = 0;
        int idx = 0;
        for (CTLine line : frame.GetLines()) {
          RectangleF bounds = line.GetImageBounds(gfx.scratchCtx);
          float lineX = origins[idx++].get_X() + bounds.get_X() + bounds.get_Width();
          maxX = Math.max(maxX, lineX);
        }
        this.width = format.effect.adjustWidth(maxX - minX);

        this.lineHeight = (origins.length < 2) ? fontLineHeight :
          (origins[0].get_Y() - origins[1].get_Y());
        float gap = lineHeight - fontLineHeight;
        this.height = format.effect.adjustHeight(lineHeight * frame.GetLines().length - gap);

      } finally {
        fs.Dispose();
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
      return frame.GetLines().length;
    }

    @Override
    void drawOnce(CGBitmapContext bctx, float x, float y) {
      float dx = x + adjustX;
      float dy = y + MAX_HEIGHT - font.ctFont.get_DescentMetric();
      bctx.TranslateCTM(dx, dy);
      bctx.ScaleCTM(1, -1);
      frame.Draw(bctx);
      bctx.ScaleCTM(1, -1);
      bctx.TranslateCTM(-dx, -dy);
    }
  }

  // Single line text is much simpler and works without too much fuss.

  private static class Single extends IOSTextLayout {
    private final CTLine line;
    private final RectangleF bounds;

    Single(IOSGraphics gfx, TextFormat format, IOSFont font, NSAttributedString atext) {
      super(gfx, format, font);
      line = new CTLine(atext);
      bounds = line.GetImageBounds(gfx.scratchCtx);
    }

    @Override
    public int lineCount() {
      return 1;
    }

    @Override
    public float width() {
      return format.effect.adjustWidth(bounds.get_X() + bounds.get_Width());
    }

    @Override
    public float height() {
      return format.effect.adjustHeight(
        font.ctFont.get_AscentMetric() + font.ctFont.get_DescentMetric());
    }

    @Override
    void drawOnce(CGBitmapContext bctx, float x, float y) {
      float dy = y + font.ctFont.get_AscentMetric();
      bctx.TranslateCTM(x, dy);
      bctx.ScaleCTM(1, -1);
      bctx.set_TextPosition(new PointF(0, 0));
      line.Draw(bctx);
      bctx.ScaleCTM(1, -1);
      bctx.TranslateCTM(-x, -dy);
    }
  }

  protected IOSTextLayout(IOSGraphics gfx, TextFormat format, IOSFont font) {
    this.gfx = gfx;
    this.format = format;
    this.font = font;
  }

  @Override
  public TextFormat format() {
    return format;
  }

  void draw(CGBitmapContext bctx, float x, float y) {
    float tx = 0, ty = 0;
    if (format.effect instanceof TextFormat.Effect.Shadow) {
      TextFormat.Effect.Shadow seffect = (TextFormat.Effect.Shadow)format.effect;
      // if the shadow is negative, we need to move the real text down/right to keep everything
      // within our bounds
      float sx, sy;
      if (seffect.shadowOffsetX > 0) {
        sx = seffect.shadowOffsetX;
      } else {
        tx = -seffect.shadowOffsetX;
        sx = 0;
      }
      if (seffect.shadowOffsetY > 0) {
        sy = seffect.shadowOffsetY;
      } else {
        ty = -seffect.shadowOffsetY;
        sy = 0;
      }
      bctx.SetFillColor(IOSCanvas.toCGColor(format.effect.getAltColor()));
      drawOnce(bctx, x+sx, y+sy);
    }
    bctx.SetFillColor(IOSCanvas.toCGColor(format.textColor));
    drawOnce(bctx, x+tx, y+ty);
  }

  abstract void drawOnce(CGBitmapContext bctx, float x, float y);

  protected static CTTextAlignment toCT(TextFormat.Alignment align) {
    switch (align) {
    default:
    case LEFT: return CTTextAlignment.wrap(CTTextAlignment.Left);
    case CENTER: return CTTextAlignment.wrap(CTTextAlignment.Center);
    case RIGHT: return CTTextAlignment.wrap(CTTextAlignment.Right);
    }
  }
}
