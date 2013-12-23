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
package playn.core;

import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

/**
 * Base {@link TextLayout} implementation shared among platforms.
 */
public abstract class AbstractTextLayout implements TextLayout {

  public static String normalizeEOL(String text) {
    // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
    return text.replace("\r\n", "\n").replace('\r', '\n');
  }

  protected final String text;
  protected final TextFormat format;
  protected final Rectangle bounds;

  @Override
  public String text() {
    return text;
  }

  @Override
  public IRectangle bounds() {
    return bounds;
  }

  @Override
  public float width() {
    // if the x position is positive, we need to include extra space in our full-width for it
    return Math.max(bounds.x, 0) + bounds.width;
  }

  @Override
  public float height() {
    return ascent() + descent();
  }

  @Override
  public TextFormat format() {
    return format;
  }

  protected AbstractTextLayout (String text, TextFormat format, Rectangle bounds) {
    this.text = text;
    this.format = format;
    this.bounds = bounds;
  }
}
