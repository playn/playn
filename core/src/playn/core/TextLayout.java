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
package playn.core;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.IRectangle;

/**
 * Contains metrics and metadata for a laid out body of text. The text may subsequently be rendered
 * to a canvas.
 */
public abstract class TextLayout {

  /** The text that was laid out. */
  public final String text;

  /** The {@link TextFormat} used to lay out this text. */
  public final TextFormat format;

  /** Returns the precise bounds of the text rendered by this layout. The x and y position may be
    * non-zero if the text is rendered somewhat offset from the "natural" origin of the text line.
    * Unfortunately x may even be negative in some cases which makes rendering the text into a
    * bespoke image troublesome. {@link TextBlock} provides methods to help with this. */
  public final IRectangle bounds;

  /** The size of the bounding box that contains all of the rendered text. Note: the height is the
    * height of a line of text. It is not a tight bounding box, but rather the ascent plus the
    * descent and is thus consistent regardless of what text is rendered. Use {@link #bounds} if
    * you want the precise height of just the rendered text. */
  public final IDimension size;

  /** The number of pixels from the top of a line of text to the baseline. */
  public abstract float ascent ();

  /** The number of pixels from the baseline to the bottom of a line of text. */
  public abstract float descent ();

  /** The number of pixels between the bottom of one line of text and the top of the next. */
  public abstract float leading ();

  protected TextLayout (String text, TextFormat format, IRectangle bounds, float height) {
    this.text = text;
    this.format = format;
    this.bounds = bounds;
    // if the x position is positive, we need to include extra space in our full-width for it
    this.size = new Dimension(Math.max(bounds.x(), 0) + bounds.width(), height);
  }

  /** A helper function for normalizing EOL prior to processing. */
  public static String normalizeEOL(String text) {
    // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
    return text.replace("\r\n", "\n").replace('\r', '\n');
  }
}
