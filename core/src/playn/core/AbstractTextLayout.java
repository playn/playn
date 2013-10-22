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

import pythagoras.f.Rectangle;

/**
 * Base {@link TextLayout} implementation shared among platforms.
 */
public abstract class AbstractTextLayout implements TextLayout {

  protected final TextFormat format;

  protected float width, height;

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

  @Override
  public void outline (Canvas canvas, int textColor, int outlineColor, boolean underlined, float x,
    float y) {
    canvas.save();
    if (underlined) {
      for (int ii = 0; ii < lineCount(); ii++) {
        Rectangle bounds = lineBounds(ii);
        float sx = x + bounds.x + 1;
        float sy = y + bounds.y + bounds.height();
        canvas.setFillColor(outlineColor);
        canvas.fillRect(sx - 1, sy - 1, bounds.width() + 3, 3);
        canvas.setFillColor(textColor);
        canvas.fillRect(sx, sy, bounds.width(), 1);
      }
    }
    fillOutline(canvas, outlineColor, x, y);
    canvas.setFillColor(textColor);
    canvas.fillText(this, x+1, y+1);
    canvas.restore();
  }

  protected AbstractTextLayout (String text, TextFormat format) {
    this.format = format;
  }

  protected void fillOutline (Canvas canvas, int outlineColor, float x, float y) {
    canvas.setFillColor(outlineColor);
    canvas.fillText(this, x+0, y+0);
    canvas.fillText(this, x+0, y+1);
    canvas.fillText(this, x+0, y+2);
    canvas.fillText(this, x+1, y+0);
    canvas.fillText(this, x+1, y+2);
    canvas.fillText(this, x+2, y+0);
    canvas.fillText(this, x+2, y+1);
    canvas.fillText(this, x+2, y+2);
  }
}
