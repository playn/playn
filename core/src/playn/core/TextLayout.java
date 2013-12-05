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

import pythagoras.f.Rectangle;

/**
 * Contains metrics and metadata for a laid out body of text. The text may subsequently be rendered
 * to a canvas.
 */
public interface TextLayout {

  /** The text that was laid out. */
  String text();

  /** The width of the bounding box that contains all of the rendered text. */
  float width();

  /** The height of the bounding box that contains all of the rendered text. */
  float height();

  /** The number of lines of text contained in this layout (after wrapping). */
  int lineCount();

  /** The bounds of the text on the given line. */
  Rectangle lineBounds(int line);

  /** The number of pixels from the top of a line of text to the baseline. */
  float ascent ();

  /** The number of pixels from the baseline to the bottom of a line of text. */
  float descent ();

  /** The number of pixels between the bottom of one line of text and the top of the next. */
  float leading ();

  /** The {@link TextFormat} used to lay out this text. */
  TextFormat format();
}
