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
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import pythagoras.f.MathUtil;

import playn.core.Image;
import playn.core.Pattern;
import playn.core.gl.AbstractImageGL;
import playn.core.gl.GLContext;
import playn.core.gl.ImageGL;
import playn.core.gl.Scale;

public abstract class JavaImage extends ImageGL<Graphics2D> {

  protected BufferedImage img;

  public JavaImage(GLContext ctx, BufferedImage img, Scale scale) {
    super(ctx, scale);
    this.img = img;
  }

  /**
   * Returns the {@link BufferedImage} that underlies this image. This is for games that need to
   * write custom backend code to do special stuff. No promises are made, caveat coder.
   */
  public BufferedImage bufferedImage() {
    return img;
  }

  @Override
  public float width() {
    return scale.invScaled(img.getWidth());
  }

  @Override
  public float height() {
    return scale.invScaled(img.getHeight());
  }

  @Override
  public boolean isReady() {
    return (img != null);
  }

  @Override
  public Pattern toPattern() {
    assert isReady() : "Cannot generate a pattern from unready image.";
    Rectangle2D rect = new Rectangle2D.Float(0, 0, width(), height());
    return new JavaPattern(this, repeatX, repeatY, new TexturePaint(img, rect));
  }

  @Override
  public void getRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    img.getRGB(startX, startY, width, height, rgbArray, offset, scanSize);
  }

  @Override
  public Image transform(BitmapTransformer xform) {
    return new JavaStaticImage(ctx, ((JavaBitmapTransformer) xform).transform(img), scale);
  }

  @Override
  public void draw(Graphics2D gfx, float x, float y, float w, float h) {
    // using img.getWidth/Height here accounts for ctx.scale.factor
    AffineTransform tx = new AffineTransform(w / img.getWidth(), 0f, 0f,
                                             h / img.getHeight(), x, y);
    gfx.drawImage(img, tx, null);
  }

  @Override
  public void draw(Graphics2D gfx, float dx, float dy, float dw, float dh,
                   float sx, float sy, float sw, float sh) {
    // adjust our source rect to account for the scale factor
    sx *= scale.factor;
    sy *= scale.factor;
    sw *= scale.factor;
    sh *= scale.factor;
    // now render the image through a clip and with a scaling transform, so that only the desired
    // source rect is rendered, and is rendered into the desired target region
    float scaleX = dw/sw, scaleY = dh/sh;
    Shape oclip = gfx.getClip();
    gfx.clipRect(MathUtil.ifloor(dx), MathUtil.ifloor(dy), MathUtil.iceil(dw), MathUtil.iceil(dh));
    gfx.drawImage(img, new AffineTransform(scaleX, 0f, 0f, scaleY,
                                           dx-sx*scaleX, dy-sy*scaleY), null);
    gfx.setClip(oclip);
  }

  @Override
  protected Pattern toSubPattern(AbstractImageGL<?> image, boolean repeatX, boolean repeatY,
                                 float x, float y, float width, float height) {
    assert isReady() : "Cannot generate a pattern from unready image.";
    // we have to account for the scale factor when extracting our subimage
    BufferedImage subImage = img.getSubimage(
      scale.scaledFloor(x), scale.scaledFloor(y),
      scale.scaledCeil(width), scale.scaledCeil(height));
    Rectangle2D rect = new Rectangle2D.Float(0, 0, width, height);
    return new JavaPattern(image, repeatX, repeatY, new TexturePaint(subImage, rect));
  }

  @Override
  protected void updateTexture(int tex) {
    assert img != null;
    ((JavaGLContext) ctx).updateTexture(tex, img);
  }
}
