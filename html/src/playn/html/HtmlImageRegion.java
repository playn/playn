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
package playn.html;

import com.google.gwt.canvas.dom.client.Context2d;

import playn.core.Pattern;
import playn.core.gl.ImageRegionGL;

class HtmlImageRegion extends ImageRegionGL implements HtmlCanvas.Drawable {

  public HtmlImageRegion(HtmlImage parent, float x, float y, float width, float height) {
    super(parent, x, y, width, height);
  }

  @Override
  public Pattern toPattern() {
    return new HtmlPattern(this, ((HtmlImage) parent).subImageElement(x, y, width, height));
  }

  @Override
  public void getRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    parent.getRgb(startX + (int) this.x, startY + (int) this.y, width, height, rgbArray, offset,
                  scanSize);
  }

  @Override
  public void draw(Context2d ctx, float x, float y, float width, float height) {
    draw(ctx, 0, 0, this.width, this.height, x, y, width, height);
  }

  @Override
  public void draw(Context2d ctx, float sx, float sy, float sw, float sh,
                   float dx, float dy, float dw, float dh) {
    ((HtmlImage) parent).draw(ctx, this.x+sx, this.y+sy, sw, sh, dx, dy, dw, dh);
  }
}
