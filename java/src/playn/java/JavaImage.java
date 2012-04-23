/**
 * Copyright 2010-2012 The PlayN Authors
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import playn.core.Asserts;
import playn.core.Pattern;
import playn.core.gl.GLContext;
import playn.core.gl.ImageGL;

abstract class JavaImage extends ImageGL implements JavaCanvas.Drawable {

  protected BufferedImage img;

  JavaImage(BufferedImage img) {
    this.img = img;
  }

  @Override
  public int width() {
    return img.getWidth();
  }

  @Override
  public int height() {
    return img.getHeight();
  }

  @Override
  public boolean isReady() {
    return (img != null);
  }

  @Override
  public Region subImage(float sx, float sy, float swidth, float sheight) {
    Asserts.checkArgument(sx >= 0 && sy >= 0 && swidth > 0 && sheight > 0 &&
                          (sx + swidth) <= width() && (sy + sheight) <= height(),
                          "Invalid bounds for subimage [image=" + width() + "x" + height() +
                          ", subImage=" + swidth + "x" + sheight + "+" + sx + "+" + sy + "]");
    return new JavaImageRegion(this, sx, sy, swidth, sheight);
  }

  @Override
  public Pattern toPattern() {
    Asserts.checkState(isReady(), "Cannot generate a pattern from unready image.");
    Rectangle2D rect = new Rectangle2D.Float(0, 0, width(), height());
    return new JavaPattern(this, new TexturePaint(img, rect));
  }

  @Override
  public void getRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    img.getRGB(startX, startY, width, height, rgbArray, offset, scanSize);
  }

  @Override
  public void draw(Graphics2D gfx, float x, float y, float w, float h) {
    // For non-integer scaling, we have to use AffineTransform.
    AffineTransform tx = new AffineTransform(w / width(), 0f, 0f, h / height(), x, y);
    gfx.drawImage(img, tx, null);
  }

  @Override
  public void draw(Graphics2D gfx, float dx, float dy, float dw, float dh,
                   float sx, float sy, float sw, float sh) {
    // TODO: use AffineTransform here as well?
    gfx.drawImage(img, (int)dx, (int)dy, (int)(dx + dw), (int)(dy + dh),
                  (int)sx, (int)sy, (int)(sx + sw), (int)(sy + sh), null);
  }

  @Override
  protected void updateTexture(GLContext ctx, Object tex) {
    Asserts.checkState(img != null);
    ((JavaGLContext) ctx).updateTexture((Integer) tex, img);
  }
}
