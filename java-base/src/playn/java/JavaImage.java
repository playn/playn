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
import playn.core.Image;
import playn.core.ImageImpl;
import playn.core.Graphics;
import playn.core.Pattern;
import playn.core.Scale;
import playn.core.Texture;
import pythagoras.f.MathUtil;

public class JavaImage extends ImageImpl {

  protected BufferedImage img;

  public JavaImage (Graphics gfx, Scale scale, BufferedImage img, String source) {
    super(gfx, scale, img.getWidth(), img.getHeight(), source, img);
  }

  public JavaImage (JavaPlatform plat, boolean async, int preWidth, int preHeight, String source) {
    super(plat, async, Scale.ONE, preWidth, preHeight, source);
  }

  /**
   * Returns the {@link BufferedImage} that underlies this image. This is for games that need to
   * write custom backend code to do special stuff. No promises are made, caveat coder.
   */
  public BufferedImage bufferedImage() {
    return img;
  }

  @Override public Pattern createPattern (boolean repeatX, boolean repeatY) {
    assert img != null : "Cannot generate a pattern from unready image.";
    Rectangle2D rect = new Rectangle2D.Float(0, 0, width(), height());
    return new JavaPattern(repeatX, repeatY, new TexturePaint(img, rect));
  }

  @Override public void getRgb(int startX, int startY, int width, int height,
                               int[] rgbArray, int offset, int scanSize) {
    img.getRGB(startX, startY, width, height, rgbArray, offset, scanSize);
  }

  @Override public void setRgb(int startX, int startY, int width, int height,
                               int[] rgbArray, int offset, int scanSize) {
    img.setRGB(startX, startY, width, height, rgbArray, offset, scanSize);
  }

  @Override public Image transform(BitmapTransformer xform) {
    return new JavaImage(gfx, scale, ((JavaBitmapTransformer) xform).transform(img), source);
  }

  @Override public void draw (Object ctx, float x, float y, float w, float h) {
    // using img.getWidth/Height here accounts for ctx.scale.factor
    AffineTransform tx = new AffineTransform(w / img.getWidth(), 0f, 0f,
                                             h / img.getHeight(), x, y);
    ((Graphics2D)ctx).drawImage(img, tx, null);
  }

  @Override public void draw (Object ctx, float dx, float dy, float dw, float dh,
                              float sx, float sy, float sw, float sh) {
    // adjust our source rect to account for the scale factor
    float f = scale().factor;
    sx *= f; sy *= f; sw *= f; sh *= f;
    // now render the image through a clip and with a scaling transform, so that only the desired
    // source rect is rendered, and is rendered into the desired target region
    float scaleX = dw/sw, scaleY = dh/sh;
    Graphics2D gfx = (Graphics2D)ctx;
    Shape oclip = gfx.getClip();
    gfx.clipRect(MathUtil.ifloor(dx), MathUtil.ifloor(dy), MathUtil.iceil(dw), MathUtil.iceil(dh));
    gfx.drawImage(img, new AffineTransform(scaleX, 0f, 0f, scaleY, dx-sx*scaleX, dy-sy*scaleY),
                  null);
    gfx.setClip(oclip);
  }

  @Override public String toString () { return "Image[src=" + source + ", img=" + img + "]"; }

  @Override protected void upload (Graphics gfx, Texture tex) {
    ((JavaGraphics)gfx).upload(img, tex);
  }

  @Override protected void setBitmap (Object bitmap) {
    img = (BufferedImage)bitmap;
  }

  @Override protected Object createErrorBitmap (int rawWidth, int rawHeight) {
    BufferedImage img = new BufferedImage(rawWidth, rawHeight, BufferedImage.TYPE_INT_ARGB_PRE);
    Graphics2D g = img.createGraphics();
    try {
      g.setColor(java.awt.Color.red);
      for (int yy = 0; yy <= rawHeight/15; yy++) {
        for (int xx = 0; xx <= rawWidth/45; xx++) {
          g.drawString("ERROR", xx*45, yy*15);
        }
      }
    } finally {
      g.dispose();
    }
    return img;
  }
}
