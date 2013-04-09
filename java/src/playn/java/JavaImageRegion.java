/**
 * Copyright 2012 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import java.awt.Graphics2D;

import playn.core.gl.ImageRegionGL;

public class JavaImageRegion extends ImageRegionGL implements JavaCanvas.Drawable {

  @Override
  public void draw(Graphics2D gfx, float x, float y, float w, float h) {
    draw(gfx, x, y, w, h, 0, 0, width, height);
  }

  @Override
  public void draw(Graphics2D gfx, float dx, float dy, float dw, float dh,
                   float x, float y, float w, float h) {
    ((JavaImage)parent).draw(gfx, dx, dy, dw, dh, this.x+x, this.y+y, w, h);
  }

  JavaImageRegion(JavaImage parent, float sx, float sy, float swidth, float sheight) {
    super(parent, sx, sy, swidth, sheight);
  }
}
