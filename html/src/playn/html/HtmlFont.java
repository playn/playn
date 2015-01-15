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

import playn.core.Font;

class HtmlFont {

  /** For use when no font is specified. */
  public static final Font DEFAULT = new Font("sans-serif", Font.Style.PLAIN, 12);

  public static String toCSS (Font font) {
    String name = font.name;
    if (!name.startsWith("\"") && name.contains(" ")) name = '"' + name + '"';

    String style = "";
    switch (font.style) {
    case BOLD:        style = "bold";   break;
    case ITALIC:      style = "italic"; break;
    case BOLD_ITALIC: style = "bold italic"; break;
    default: break; // nada
    }

    return style + " " + font.size + "px " + name;
  }
}
