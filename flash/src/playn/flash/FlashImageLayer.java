/*
 * Copyright 2011 Google Inc.
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

package playn.flash;

import flash.display.BitmapData;
import flash.display.Graphics;
import flash.display.Shape;
import flash.geom.Matrix;
import playn.core.Asserts;

import flash.display.Bitmap;
import flash.display.Sprite;

import playn.core.PlayN;
import playn.core.ResourceCallback;
import playn.core.Image;
import playn.core.ImageLayer;

public class FlashImageLayer extends FlashLayer implements ImageLayer {

  private FlashImage image;
  private BitmapData bitmapData;
  private static int NOT_SET = -1;

  float width = NOT_SET;
  float height = NOT_SET;

  private boolean dirty = true;
  private boolean repeatX;
  private boolean repeatY;

  public FlashImageLayer(Image image) {
    super((Sprite) (Bitmap.create(null).cast()));
    setImage(image);
  }

  public FlashImageLayer() {
    super(Bitmap.create(null));
    image = null;
  }

  @Override
  public void clearHeight() {
    height = NOT_SET;
    dirty = true;
  }

  @Override @Deprecated
  public void clearSourceRect() {
    if (image instanceof Image.Region) {
      setImage(((Image.Region) image).parent());
    }
  }

  @Override
  public void clearWidth() {
    width = NOT_SET;
    dirty = true;
  }

  @Override
  public Image image() {
    return image;
  }

  @Override
  public void setHeight(float height) {
    this.height = height;
    dirty = true;
  }

  @Override
  public void setImage(Image image) {
    this.image = (FlashImage) image;
    image.addCallback(new ResourceCallback<Image>() {
      @Override
      public void error(Throwable err) {
        PlayN.log().error(err.toString());
      }
      @Override
      public void done(Image resource) {
        bitmapData = ((FlashImage) resource).bitmapData();
      }
    });
  }

  @Override
  public void setRepeatX(boolean repeat) {
    repeatX = repeat;
    dirty = true;
  }

  @Override
  public void setRepeatY(boolean repeat) {
    repeatY = repeat;
    dirty = true;
  }

  @Override @Deprecated
  public void setSourceRect(float sx, float sy, float sw, float sh) {
    Image source = (image instanceof Image.Region) ? ((Image.Region)image).parent() : image;
    setImage(source.subImage(sx, sy, sw, sh));
  }

  @Override
  public void setWidth(float width) {
    this.width = width;
    dirty = true;
  }

  @Override
  public void setSize(float width, float height) {
    setWidth(width);
    setHeight(height);
  }

  @Override
  public float width() {
    Asserts.checkNotNull(image, "Image must not be null");
    return width != NOT_SET ? width : image.width();
  }

  @Override
  public float height() {
    Asserts.checkNotNull(image, "Image must not be null");
    return height != NOT_SET ? height : image().height();
  }

  @Override
  public float scaledWidth() {
    return transform().scaleX() * width();
  }

  @Override
  public float scaledHeight() {
    return transform().scaleY() * height();
  }

  @Override
  public void update() {
    if (dirty && image != null) {
      float dw = width();
      float dh = height();

      if (repeatX || repeatY) {
        float anchorWidth = repeatX ? dw : image.width();
        float anchorHeight = repeatY ? dh : image.height();

        Shape shape = Shape.create((int) anchorWidth, (int) anchorHeight);
        Graphics g = shape.getGraphics();
        g.beginBitmapFill(bitmapData, Matrix.create(), true, true);
        g.drawRect(0, 0, anchorWidth, anchorHeight);
        g.endFill();
        BitmapData data = BitmapData.create((int) dw, (int) dh, true, 0x00000000);
        data.draw(shape);
        ((Bitmap) display()).setBitmapData(data);
      } else {
        ((Bitmap) display()).setBitmapData(bitmapData);
      }
      display().setWidth((int) dw);
      display().setHeight((int) dh);
      dirty = false;
    }

    super.update();
  }
}
