/**
 * Copyright 2010 The PlayN Authors
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

import com.google.gwt.canvas.dom.client.CanvasPattern;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.Repetition;

import playn.core.Asserts;
import playn.core.Image;
import playn.core.ImageLayer;

class HtmlImageLayerCanvas extends HtmlLayerCanvas implements ImageLayer {

  private float width, height;
  private boolean widthSet, heightSet;
  private float sx, sy, sw, sh;
  private boolean sourceRectSet;
  private boolean repeatX, repeatY;

  private HtmlImage img;
  private CanvasPattern pattern;
  private boolean patternRepeatX;
  private boolean patternRepeatY;

  public HtmlImageLayerCanvas() {
  }

  HtmlImageLayerCanvas(Image img) {
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
    Asserts.checkArgument(height > 0, "Height must be > 0");

    heightSet = true;
    this.height = height;
  }

  @Override
  public void setImage(Image img) {
    Asserts.checkArgument(img instanceof HtmlImage);

    this.img = (HtmlImage) img;
  }

  @Override
  public void setRepeatX(boolean repeat) {
    Asserts.checkArgument(!repeat || !sourceRectSet, "Cannot repeat when source rect is used");

    repeatX = repeat;
  }

  @Override
  public void setRepeatY(boolean repeat) {
    Asserts.checkArgument(!repeat || !sourceRectSet, "Cannot repeat when source rect is used");

    repeatY = repeat;
  }

  @Override
  public void setSourceRect(float sx, float sy, float sw, float sh) {
    Asserts.checkState(!repeatX && !repeatY, "Cannot use source rect when repeating x or y");

    sourceRectSet = true;
    this.sx = sx;
    this.sy = sy;
    this.sw = sw;
    this.sh = sh;
  }

  @Override
  public void setWidth(float width) {
    Asserts.checkArgument(width > 0, "Width must be > 0");

    widthSet = true;
    this.width = width;
  }

  @Override
  public void setSize(float width, float height) {
    Asserts.checkArgument(width > 0 && height > 0,
                          "Width and height must be > 0 (got %dx%d)", width, height);

    widthSet = true;
    this.width = width;
    heightSet = true;
    this.height = height;
  }

  @Override
  public void paint(Context2d ctx, float parentAlpha) {
    if (!visible()) return;

    float width = widthSet ? this.width : img.img.getWidth();
    float height = heightSet ? this.height : img.img.getHeight();

    ctx.save();
    transform(ctx);

    ctx.setGlobalAlpha(parentAlpha * alpha);
    if (sourceRectSet) {
      ctx.drawImage(img.img, sx, sy, sw, sh, 0, 0, width, height);
    } else {
      if (repeatX || repeatY) {
        updatePattern(ctx);

        ctx.setFillStyle(pattern);
        ctx.beginPath();
        ctx.rect(0, 0, width, height);
        ctx.scale(repeatX ? 1 : width / img.width(), repeatY ? 1 : height / img.height());
        ctx.fill();
      } else {
        ctx.drawImage(img.img, 0, 0, width, height);
      }
    }

    ctx.restore();
  }

  @Override
  public float width() {
    Asserts.checkNotNull(img, "Image must not be null");
    if (widthSet) {
      return width;
    } else {
      return img.width();
    }
  }

  @Override
  public float height() {
    Asserts.checkNotNull(img, "Image must not be null");
    if (heightSet) {
      return height;
    } else {
      return img.height();
    }
  }

  @Override
  public float scaledWidth() {
    return transform.scaleX() * width();
  }

  @Override
  public float scaledHeight() {
    return transform().scaleY() * height();
  }

  private void updatePattern(Context2d ctx) {
    if ((repeatX == patternRepeatX) && (repeatY == patternRepeatY)) {
      return;
    }

    Repetition repeat;
    if (repeatX) {
      if (repeatY) {
        repeat = Repetition.REPEAT;
      } else {
        repeat = Repetition.REPEAT_X;
      }
    } else if (repeatY) {
      repeat = Repetition.REPEAT_Y;
    } else {
      pattern = null;
      return;
    }

    pattern = ctx.createPattern(img.img, repeat);
    patternRepeatX = repeatX;
    patternRepeatY = repeatY;
  }
}
