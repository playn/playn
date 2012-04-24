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
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import playn.core.Pattern;
import playn.core.gl.ImageRegionGL;

public class JavaImageRegion extends ImageRegionGL implements JavaCanvas.Drawable {

  @Override
  public Pattern toPattern() {
    JavaImage jparent = (JavaImage)parent;
    // we have to account for the scale factor when extracting our subimage
    BufferedImage subImage = jparent.img.getSubimage(
      jparent.ctx.scaledFloor(x), jparent.ctx.scaledFloor(y),
      jparent.ctx.scaledCeil(width()), jparent.ctx.scaledCeil(height()));
    Rectangle2D rect = new Rectangle2D.Float(0, 0, width, height);
    return new JavaPattern(this, new TexturePaint(subImage, rect));
  }

  @Override
  public void getRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    parent.getRgb(startX + (int) x, startY + (int) y, width, height, rgbArray, offset, scanSize);
  }

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
