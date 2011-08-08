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
package playn.java;

import java.util.HashMap;
import java.util.Map;

import playn.core.AbstractFont;

class JavaFont extends AbstractFont {

  public final java.awt.Font jfont;

  public JavaFont(String name, Style style, float size) {
    super(name, style, size);
    // the Font constructor takes only integer size, so we instantiate it at an arbitrary size and
    // derive a font of the desired (floating point) size
    jfont = new java.awt.Font(name, TO_JAVA_STYLE.get(style), 12).deriveFont(size);
  }

  protected static final Map<Style,Integer> TO_JAVA_STYLE = new HashMap<Style,Integer>();
  static {
    TO_JAVA_STYLE.put(Style.PLAIN,       java.awt.Font.PLAIN);
    TO_JAVA_STYLE.put(Style.BOLD,        java.awt.Font.BOLD);
    TO_JAVA_STYLE.put(Style.ITALIC,      java.awt.Font.ITALIC);
    TO_JAVA_STYLE.put(Style.BOLD_ITALIC, java.awt.Font.BOLD|java.awt.Font.ITALIC);
  }
}
