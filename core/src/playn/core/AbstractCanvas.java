/**
 * Copyright 2013 The PlayN Authors
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
 * A base {@link Canvas} implementation shared by all platforms.
 */
public abstract class AbstractCanvas implements Canvas {

  protected final float width, height;

  @Override
  public float width() {
    return width;
  }

  @Override
  public float height() {
    return height;
  }

  @Override
  public Canvas outlineText(TextLayout text, int textColor, int outlineColor, boolean underlined,
    float x, float y) {
    save();
    if (underlined) {
      for (int ii = 0; ii < text.lineCount(); ii++) {
        Rectangle bounds = text.lineBounds(ii);
        float sx = x + bounds.x + 1;
        float sy = y + bounds.y + bounds.height();
        setFillColor(outlineColor);
        fillRect(sx - 1, sy - 1, bounds.width() + 3, 3);
        setFillColor(textColor);
        fillRect(sx, sy, bounds.width(), 1);
      }
    }
    fillOutline(text, outlineColor, x, y);
    setFillColor(textColor);
    fillText(text, x+1, y+1);
    restore();
    return this;
  }

  protected AbstractCanvas(float width, float height) {
    this.width = width;
    this.height = height;
  }

  protected void fillOutline(TextLayout text, int outlineColor, float x, float y) {
    setFillColor(outlineColor);
    fillText(text, x+0, y+0);
    fillText(text, x+0, y+1);
    fillText(text, x+0, y+2);
    fillText(text, x+1, y+0);
    fillText(text, x+1, y+2);
    fillText(text, x+2, y+0);
    fillText(text, x+2, y+1);
    fillText(text, x+2, y+2);
  }
}
