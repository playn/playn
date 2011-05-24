/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.html;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;

import forplay.core.Image;
import forplay.core.ImageLayer;
import forplay.core.ResourceCallback;

class HtmlImageLayerDom extends HtmlLayerDom implements ImageLayer {

  private float width, height;
  private boolean widthSet, heightSet;
  private float sx, sy, sw, sh;
  private boolean sourceRectSet;
  private boolean repeatX, repeatY;

  private HtmlImage htmlImage;

  public HtmlImageLayerDom() {
    super(Document.get().createDivElement());

    setRepeatX(false);
    setRepeatY(false);
  }

  HtmlImageLayerDom(final Image img) {
    this();
    setImage(img);
  }

  @Override
  public void clearHeight() {
    heightSet = true;
    applySize();
  }

  @Override
  public void clearSourceRect() {
    this.sourceRectSet = false;
    applyBackgroundSize();
  }

  @Override
  public void clearWidth() {
    widthSet = true;
    applySize();
  }

  @Override
  public Image image() {
    return htmlImage;
  }

  @Override
  public void setHeight(float height) {
    assert height > 0;
    heightSet = true;

    this.height = height;
    applySize();
  }

  @Override
  public void setImage(final Image img) {
    assert img instanceof HtmlImage;

    // Make sure redundant setImage() calls don't cost much.
    if (htmlImage == img) {
      return;
    }

    htmlImage = (HtmlImage) img;
    ImageElement imgElem = htmlImage.img.cast();
    element().getStyle().setBackgroundImage("url(" + imgElem.getSrc() + ")");
    element().getStyle().setOverflow(Overflow.HIDDEN);

    img.addCallback(new ResourceCallback<Image>() {
      @Override
      public void done(Image resource) {
        applySize();
        applyBackgroundSize();
      }

      @Override
      public void error(Throwable err) {
        // Nothing to be done about errors.
      }
    });
  }

  @Override
  public void setRepeatX(boolean repeat) {
    if (repeat) {
      assert !sourceRectSet;
    }

    repeatX = repeat;
    applyBackgroundSize();
  }

  @Override
  public void setRepeatY(boolean repeat) {
    if (repeat) {
      assert !sourceRectSet;
    }

    repeatY = repeat;
    applyBackgroundSize();
  }

  @Override
  public void setSourceRect(float sx, float sy, float sw, float sh) {
    assert !repeatX && !repeatY;

    // Will cause div-by-zero
    assert sw != 0 && sh != 0;

    // Early out if there's no change. applyBackgroundSize() isn't free.
    if (sourceRectSet &&
        (this.sx == sx) && (this.sy == sy) &&
        (this.sw == sw) && (this.sh == sh)) {
      return;
    }

    this.sourceRectSet = true;
    this.sx = sx;
    this.sy = sy;
    this.sw = sw;
    this.sh = sh;
    applyBackgroundSize();
  }

  @Override
  public void setWidth(float width) {
    assert width > 0;
    widthSet = true;

    this.width = width;
    applySize();
  }

  @Override
  public void setSize(float width, float height) {
    assert width > 0;
    widthSet = true;

    assert height > 0;
    heightSet = true;

    this.width = width;
    this.height = height;
    applySize();
  }

  private void applyBackgroundSize() {
    Style style = element().getStyle();

    // Set background-repeat to get the right repeating behavior.
    String repeat = repeatX ? "repeat-x " : "";
    repeat += repeatY ? "repeat-y" : "";
    style.setProperty("backgroundRepeat", repeat);

    // Set background-size to get the right pinning behavior.
    if (sourceRectSet) {
      float wratio = widthSet ? (width / sw) : (image().width() / sw);
      float hratio = heightSet ? (height / sh) : (image().height() / sh);
      if (wratio == 0) {
        wratio = 1;
      }
      if (hratio == 0) {
        hratio = 1;
      }
      float backWidth = image().width() * wratio;
      float backHeight = image().height() * hratio;

      style.setProperty("backgroundSize", backWidth + "px " + backHeight + "px");
      style.setProperty("backgroundPosition", (-sx * wratio) + "px " + (-sy * hratio) + "px");
    } else {
      String size = repeatX ? image().width() + "px " : "100% ";
      size += repeatY ? image().height() + "px" : "100%";
      style.setProperty("backgroundSize", size);
      style.clearProperty("backgroundPosition");
    }
  }

  private void applySize() {
    Style style = element().getStyle();
    style.setWidth(widthSet ? width : htmlImage.img.getWidth(), Unit.PX);
    style.setHeight(heightSet ? height : htmlImage.img.getHeight(), Unit.PX);
    if (sourceRectSet) {
      applyBackgroundSize();
    }
  }
}
