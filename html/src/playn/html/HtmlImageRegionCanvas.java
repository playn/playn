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

import pythagoras.f.MathUtil;

import playn.core.Image;
import playn.core.ResourceCallback;

class HtmlImageRegionCanvas extends HtmlImage implements Image.Region {

  private final HtmlImage parent;
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
        callback.done(HtmlImageRegionCanvas.this);
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

  HtmlImageRegionCanvas(HtmlImage parent, float sx, float sy, float swidth, float sheight) {
    super(parent.img);
    this.parent = parent;
    this.sx = sx;
    this.sy = sy;
    this.swidth = MathUtil.iceil(swidth);
    this.sheight = MathUtil.iceil(sheight);
  }

  @Override
  void draw(Context2d ctx, float sx, float sy, float sw, float sh,
            float dx, float dy, float dw, float dh) {
    ctx.drawImage(parent.img, this.sx+sx, this.sy+sy, sw, sh, dx, dy, dw, dh);
  }
}
