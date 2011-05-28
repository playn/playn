/**
 * Copyright 2010 The ForPlay Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.html;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLTexture;

import forplay.core.Image;
import forplay.core.ImageLayer;
import forplay.core.Transform;

class HtmlImageLayerGL extends HtmlLayerGL implements ImageLayer {

  private float width, height;
  private boolean widthSet, heightSet;
  private float sx, sy, sw, sh;
  private boolean sourceRectSet;
  private boolean repeatX, repeatY;

  private HtmlImage img;

  public HtmlImageLayerGL(HtmlGraphicsGL gfx) {
    super(gfx);
  }

  HtmlImageLayerGL(HtmlGraphicsGL gfx, Image img) {
    this(gfx);
    setImage(img);
  }

  @Override
  public void clearHeight() {
    heightSet = false;
  }

  @Override
  public void clearSourceRect() {
    sourceRectSet = false;
  }

  @Override
  public void clearWidth() {
    widthSet = false;
  }

  @Override
  public Image image() {
    return img;
  }

  @Override
  public void setHeight(float height) {
    assert height > 0;

    heightSet = true;
    this.height = height;
  }

  @Override
  public void setImage(Image img) {
    assert img instanceof HtmlImage;
    this.img = (HtmlImage) img;
  }

  @Override
  public void setRepeatX(boolean repeat) {
    if (repeat) {
      assert !sourceRectSet;
    }

    repeatX = repeat;
  }

  @Override
  public void setRepeatY(boolean repeat) {
    if (repeat) {
      assert !sourceRectSet;
    }

    repeatY = repeat;
  }

  @Override
  public void setSourceRect(float sx, float sy, float sw, float sh) {
    assert !repeatX && !repeatY;

    sourceRectSet = true;
    this.sx = sx;
    this.sy = sy;
    this.sw = sw;
    this.sh = sh;
  }

  @Override
  public void setWidth(float width) {
    assert width > 0;

    widthSet = true;
    this.width = width;
  }

  @Override
  public void setSize(float width, float height) {
    assert width > 0;
    assert height > 0;

    widthSet = true;
    this.width = width;
    heightSet = true;
    this.height = height;
  }

  @Override
  void paint(WebGLRenderingContext gl, Transform parentTransform) {
    // TODO(jgw): Assert exclusive source-rect vs. repeat.

    WebGLTexture tex = img.ensureTexture(gfx, repeatX, repeatY);
    if (tex != null) {
      ImageElement elem = img.img;

      Transform xform = localTransform(parentTransform);

      float width = widthSet ? this.width : elem.getWidth();
      float height = heightSet ? this.height : elem.getHeight();

      if (sourceRectSet) {
        gfx.drawTexture(tex, img.width(), img.height(), xform, 0, 0, width, height, sx, sy, sw, sh);
      } else {
        gfx.drawTexture(tex, img.width(), img.height(), xform, width, height, repeatX, repeatY);
      }
    }
  }

}
