/**
 * Copyright 2011 The PlayN Authors
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
package playn.android;

import playn.core.Asserts;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.InternalTransform;

class AndroidImageLayer extends AndroidLayer implements ImageLayer {

  private float width, height;
  private boolean widthSet, heightSet;
  private float sx, sy, sw, sh;
  private boolean sourceRectSet;
  private boolean repeatX, repeatY;

  private AndroidImage image;

  AndroidImageLayer(AndroidGraphics gfx) {
    super(gfx);
  }

  AndroidImageLayer(AndroidGraphics gfx, AndroidImage image) {
    this(gfx);
    this.image = image;
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
    return image;
  }

  @Override
  public void setHeight(float height) {
    Asserts.checkArgument(height > 0, "Height must be > 0");

    heightSet = true;
    this.height = height;
  }

  @Override
  public void setImage(Image image) {
    Asserts.checkArgument(image instanceof AndroidImage);
    this.image = (AndroidImage) image;
  }

  @Override
  public void setRepeatX(boolean repeat) {
    Asserts.checkArgument(!repeat || !sourceRectSet, "Cannot repeat when source rect is used");

    if (repeatX != repeat) {
      repeatX = repeat;
    }
  }

  @Override
  public void setRepeatY(boolean repeat) {
    Asserts.checkArgument(!repeat || !sourceRectSet, "Cannot repeat when source rect is used");

    if (repeatY != repeat) {
      repeatY = repeat;
    }
  }

  @Override
  public void setSize(float width, float height) {
    setWidth(width);
    setHeight(height);
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
  public void paint(InternalTransform parentTransform, float parentAlpha) {
    if (!visible())
      return;

    gfx.checkGlError("AndroidImageLayer.paint start");
    if (sourceRectSet) Asserts.check(repeatX == false && repeatY == false);

    int tex = image.ensureTexture(gfx, repeatX, repeatY);
    if (tex != -1) {
      InternalTransform xform = localTransform(parentTransform);
      float childAlpha = parentAlpha * alpha;

      float width = widthSet ? this.width : image.width();
      float height = heightSet ? this.height : image.height();

      if (sourceRectSet) {
        gfx.drawTexture(tex, image.width(), image.height(), xform, 0, 0, width, height, sx,
            sy, sw, sh, childAlpha);
      } else {
        gfx.drawTexture(tex, image.width(), image.height(), xform, width, height, repeatX,
            repeatY, childAlpha);
      }
    }
    gfx.checkGlError("AndroidImageLayer.paint end");
  }

  @Override
  public float width() {
    Asserts.checkNotNull(image, "Image must not be null");
    if (widthSet) {
      return width;
    } else {
      return image.width();
    }
  }

  @Override
  public float height() {
    Asserts.checkNotNull(image, "Image must not be null");
    if (heightSet) {
      return height;
    } else {
      return image.height();
    }
  }

  @Override
  public float scaledWidth() {
    return transform.scaleX() * width();
  }

  @Override
  public float scaledHeight() {
    return transform.scaleY() * height();
  }
}
