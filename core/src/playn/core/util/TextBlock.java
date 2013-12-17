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
package playn.core.util;

import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.TextLayout;
import static playn.core.PlayN.graphics;

/**
 * Encapsulates a block of multi-line text. This code handles all the fiddly "fonts sometimes
 * extend outside their reported bounds" hackery that was once embedded into the various backend
 * code. It also handles text alignment.
 */
public class TextBlock {

  /** Used to align a block of text. */
  public static enum Align {
    LEFT {
      @Override
      public float getX(float lineWidth, float blockWidth) {
        return 0;
      }
    },
    CENTER {
      @Override
      public float getX(float lineWidth, float blockWidth) {
        return (blockWidth - lineWidth)/2;
      }
    },
    RIGHT {
      @Override
      public float getX(float lineWidth, float blockWidth) {
        return (blockWidth - lineWidth);
      }
    };

    /** Returns the x offset for a line of text with width {@code lineWidth} rendered as part of a
     * block of width {@code blockWidth}. */
    public abstract float getX(float lineWidth, float blockWidth);
  }

  /** The individual lines of text in this block. Obtained by a call to {@link
   * Graphics#layoutText(String,playn.core.TextFormat,playn.core.TextWrap}}. */
  public final TextLayout[] lines;

  /** The bounds of this block of text. The {@code x} component of the bounds may be positive,
   * indicating that the text should be rendered at that offset. This is to account for the fact
   * that some text renders to the left of its reported origin due to font extravagance. The {@link
   * #stroke} and {@link #fill} methods automatically take into account this x coordinate, the
   * caller need only account for it if they choose to render {@link #lines} manually. */
  public final IRectangle bounds;

  /**
   * Returns the padding used by {@link #toImage} to ensure that anti-aliasing has room to do its
   * work.
   */
  public static float pad() {
    return 1/graphics().scaleFactor();
  }

  /** Computes the bounds of a block of text. The {@code x} component of the bounds may be
   * positive, indicating that the text should be rendered at that offset. This is to account for
   * the fact that some text renders to the left of its reported origin due to font
   * extravagance. */
  public static Rectangle getBounds(TextLayout[] lines, Rectangle into) {
    // some font glyphs start rendering at a negative inset, blowing outside their bounding box
    // (naughty!); in such cases, we use xAdjust to shift everything to the right to ensure that we
    // don't paint outside our reported bounding box (so that someone can create a single canvas of
    // bounding box size and render this text layout into it at (0,0) and nothing will get cut off)
    float xAdjust = 0, twidth = 0, theight = 0;
    for (TextLayout layout : lines) {
      IRectangle bounds = layout.bounds();
      xAdjust = Math.max(xAdjust, -Math.min(0, bounds.x()));
      // we use layout.width() here not bounds width because layout width includes extra space
      // needed for lines that start rendering at a positive x offset whereas bounds.width() is
      // only the precise width of the rendered text
      twidth = Math.max(twidth, layout.width());
      if (layout != lines[0])
        theight += layout.leading(); // leading only applied to lines after 0
      theight += layout.ascent() + layout.descent();
    }
    into.setBounds(xAdjust, 0, xAdjust+twidth, theight);
    return into;
  }

  /**
   * Creates a text block with the supplied {@code lines}.
   */
  public TextBlock (TextLayout[] lines) {
    this.lines = lines;
    this.bounds = getBounds(lines, new Rectangle());
  }

  /**
   * Returns the width of the rendered text. This is the width that should be used when computing
   * alignment for text in this block.
   */
  public float textWidth() {
    return bounds.width() - bounds.x();
  }

  /**
   * Fills {@code lines} into {@code canvas} at the specified coordinates, using the specified
   * alignment.
   */
  public void fill(Canvas canvas, Align align, float x, float y) {
    float sy = y + bounds.y();
    for (TextLayout line : lines) {
      float sx = x + bounds.x() + align.getX(line.width(), textWidth());
      canvas.fillText(line, sx, sy);
      sy += line.ascent() + line.descent() + line.leading();
    }
  }

  /**
   * Strokes {@code lines} into {@code canvas} at the specified coordinates, using the specified
   * alignment.
   */
  public void stroke(Canvas canvas, Align align, float x, float y) {
    float sy = y + bounds.y();
    for (TextLayout line : lines) {
      float sx = x + bounds.x() + align.getX(line.width(), textWidth());
      canvas.strokeText(line, sx, sy);
      sy += line.ascent() + line.descent() + line.leading();
    }
  }

  /**
   * Creates a canvas image large enough to accommodate this text block and renders the lines into
   * it. The image will include padding around the edge to ensure that antialiasing has a bit of
   * extra space to do its work.
   */
  public CanvasImage toImage(Align align, int fillColor) {
    float pad = pad();
    CanvasImage image = graphics().createImage(bounds.width()+2*pad, bounds.height()+2*pad);
    image.canvas().setFillColor(fillColor);
    fill(image.canvas(), align, pad, pad);
    return image;
  }
}
