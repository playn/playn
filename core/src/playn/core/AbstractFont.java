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
 * Base {@link Font} implementation shared among platforms.
 */
public abstract class AbstractFont implements Font
{
  protected final String name;
  protected final Style style;
  protected final float size;

  @Override
  public String name() {
    return name;
  }

  @Override
  public Style style() {
    return style;
  }

  @Override
  public float size() {
    return size;
  }

  @Override
  public int hashCode() {
    return name.hashCode() ^ style.hashCode() ^ (int)size;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof AbstractFont) {
      AbstractFont ofont = (AbstractFont)other;
      return name.equals(ofont.name) && style == ofont.style && size == ofont.size;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return name + " " + style + " " + size + "pt";
  }

  protected AbstractFont(String name, Style style, float size) {
    this.name = name;
    this.style = style;
    this.size = size;
  }
}
