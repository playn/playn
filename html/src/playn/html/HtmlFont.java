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
package playn.html;

import playn.core.AbstractFont;

class HtmlFont extends AbstractFont {

  /** For use when no font is specified. */
  public static final HtmlFont DEFAULT = new HtmlFont(null, "sans-serif", Style.PLAIN, 12);

  public HtmlFont(HtmlGraphics graphics, String name, Style style, float size) {
    super(graphics, quoteFontName(name), style, size);
  }

  protected static final String quoteFontName(String name) {
    return (!name.startsWith("\"") && name.contains(" ")) ? ('"' + name + '"') : name;
  }
}
