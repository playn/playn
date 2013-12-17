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

import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

import playn.core.util.TextBlock;

/**
 * Contains metrics and metadata for a laid out body of text. The text may subsequently be rendered
 * to a canvas.
 */
public interface TextLayout {

  /** The text that was laid out. */
  String text();

  /** The {@link TextFormat} used to lay out this text. */
  TextFormat format();

  /** The width of the bounding box that contains all of the rendered text. */
  float width();

  /** The height of a line of text. This is not a tight bounding box, but rather the ascent plus
   * the descent and is thus consistent regardless of what text is rendered. Use {@link #bounds} if
   * you want the precise height of just the rendered text. */
  float height();

  /** Returns the precise bounds of the text rendered by this layout. The x and y position may be
   * non-zero if the text is rendered somewhat offset from the "natural" origin of the text line.
   * Unfortunately x may even be negative in some cases which makes rendering the text into a
   * bespoke image troublesome. {@link TextBlock} provides methods to help with this. */
  IRectangle bounds();

  /** The number of pixels from the top of a line of text to the baseline. */
  float ascent ();

  /** The number of pixels from the baseline to the bottom of a line of text. */
  float descent ();

  /** The number of pixels between the bottom of one line of text and the top of the next. */
  float leading ();

  /** @deprecated Use {@link Graphics#layoutText(String,TextFormat,float)} and handle multiple
   * lines separately. */
  @Deprecated int lineCount();

  /** @deprecated Use {@link Graphics#layoutText(String,TextFormat,float)} and handle multiple
   * lines separately. */
  @Deprecated Rectangle lineBounds(int line);
}
