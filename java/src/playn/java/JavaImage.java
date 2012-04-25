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
import playn.core.Image;
import playn.core.Pattern;
import playn.core.gl.GLContext;
import playn.core.gl.ImageGL;

abstract class JavaImage extends ImageGL implements JavaCanvas.Drawable {

  protected final JavaGLContext ctx;
  protected BufferedImage img;

  JavaImage(JavaGLContext ctx, BufferedImage img) {
    this.ctx = ctx;
    this.img = img;
  }

  @Override
  public int width() {
    return ctx.invScaledCeil(img.getWidth());
  }

  @Override
  public int height() {
    return ctx.invScaledCeil(img.getHeight());
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
  public Image transform(BitmapTransformer xform) {
    return new JavaStaticImage(ctx, ((JavaBitmapTransformer) xform).transform(img));
  }

  @Override
  public void draw(Graphics2D gfx, float x, float y, float w, float h) {
    // using img.getWidth/Height here accounts for ctx.scaleFactor
    AffineTransform tx = new AffineTransform(w / img.getWidth(), 0f, 0f,
                                             h / img.getHeight(), x, y);
    gfx.drawImage(img, tx, null);
  }

  @Override
  public void draw(Graphics2D gfx, float dx, float dy, float dw, float dh,
                   float sx, float sy, float sw, float sh) {
    // adjust our source rect to account for the scale factor
    sx *= ctx.scaleFactor;
    sy *= ctx.scaleFactor;
    sw *= ctx.scaleFactor;
    sh *= ctx.scaleFactor;
    // now render the image through a clip and with a scaling transform, so that only the desired
    // source rect is rendered, and is rendered into the desired target region
    float scaleX = dw/sw, scaleY = dh/sh;
    gfx.setClip(new Rectangle2D.Float(dx, dy, dw, dh));
    gfx.drawImage(img, new AffineTransform(scaleX, 0f, 0f, scaleY, dx-sx*scaleX, dy-sy*scaleY), null);
    gfx.setClip(null);
  }

  @Override
  protected void updateTexture(GLContext ctx, Object tex) {
    Asserts.checkState(img != null);
    ((JavaGLContext) ctx).updateTexture((Integer) tex, img);
  }
}
