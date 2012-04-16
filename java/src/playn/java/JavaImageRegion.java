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

import pythagoras.f.MathUtil;

import playn.core.Image;
import playn.core.ResourceCallback;

public class JavaImageRegion extends JavaImage implements Image.Region {

  private final JavaImage parent;
  private final float sx, sy;
  private final int swidth, sheight;

  @Override
  public float x() {
    return sx;
  }

  @Override
  public float y() {
    return sy;
  }

  @Override
  public int width() {
    return swidth;
  }

  @Override
  public int height() {
    return sheight;
  }

  @Override
  public Image parent() {
    return parent;
  }

  @Override
  public boolean isReady() {
    return parent.isReady();
  }

  @Override
  public void addCallback(final ResourceCallback<? super Image> callback) {
    parent.addCallback(new ResourceCallback<Image>() {
      public void done(Image image) {
        callback.done(JavaImageRegion.this);
      }
      public void error(Throwable err) {
        callback.error(err);
      }
    });
  }

  @Override
  public Region subImage(float x, float y, float width, float height) {
    // TODO: clamp swidth, sheight to our bounds?
    return parent.subImage(sx+x, sy+y, width, height);
  }

  JavaImageRegion(JavaImage parent, float sx, float sy, float swidth, float sheight) {
    super(null);
    this.parent = parent;
    this.sx = sx;
    this.sy = sy;
    this.swidth = MathUtil.iceil(swidth);
    this.sheight = MathUtil.iceil(sheight);
  }

  @Override
  TexturePaint createTexture(float width, float height) {
    return new TexturePaint(parent.img.getSubimage((int)sx, (int)sy, swidth, sheight),
                            new Rectangle2D.Float(0, 0, width, height));
  }

  @Override
  void draw(Graphics2D gfx, float x, float y, float w, float h) {
    draw(gfx, x, y, w, h, 0, 0, swidth, sheight);
  }

  @Override
  void draw(Graphics2D gfx, float dx, float dy, float dw, float dh,
            float x, float y, float w, float h) {
    parent.draw(gfx, dx, dy, dw, dh, sx+x, sy+y, w, h);
  }
}
