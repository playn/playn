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

/**
 * Contains metadata for a font.
 */
public interface Font {

  /** The styles that may be requested for a given font. */
  public enum Style { PLAIN, BOLD, ITALIC, BOLD_ITALIC }

  /** Returns the name of this font. */
  String name();

  /** Returns the style of this font. */
  Style style();

  /** Returns the point size of this font. */
  float size();

  /** Creates a new font with the same name and style as this font, with the specified size. */
  Font derive(float size);
}
