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

/**
 * Base {@link TextLayout} implementation shared among platforms.
 */
public abstract class AbstractTextLayout implements TextLayout {

  protected final String text;
  protected final TextFormat format;

  protected float width, height;

  @Override
  public String text() {
    return text;
  }

  @Override
  public float width() {
    return width;
  }

  @Override
  public float height() {
    return height;
  }

  @Override
  public TextFormat format() {
    return format;
  }

  protected AbstractTextLayout (String text, TextFormat format) {
    this.text = text;
    this.format = format;
  }
}
