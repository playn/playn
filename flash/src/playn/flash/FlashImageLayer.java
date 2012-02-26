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
import flash.geom.Point;
import flash.geom.Rectangle;
import playn.core.Asserts;

import flash.display.Bitmap;
import flash.display.Sprite;

import playn.core.PlayN;
import playn.core.ResourceCallback;
import playn.core.Image;
import playn.core.ImageLayer;

// TODO(pdr): fix setWidth, setHeight, setRepeat*, etc.
public class FlashImageLayer extends FlashLayer implements ImageLayer {

  private Image image;

  private BitmapData bitmapData;

  private Rectangle sourceRect;

  private static int NOT_SET = -1;

  float width = NOT_SET;

  float height = NOT_SET;

  private boolean dirty = true;

  private boolean repeatX;

  private boolean repeatY;

  private BitmapData clippedSource;

  /**
   * @param image
   */
  public FlashImageLayer(Image image) {
    super((Sprite) (Bitmap.create(null).cast()));
    this.image = image;
    setBitmapData(((FlashImage) image));

    image.addCallback(new ResourceCallback<Image>() {

      @Override
      public void error(Throwable err) {
        PlayN.log().error(err.toString());
      }

      @Override
      public void done(Image resource) {
        setBitmapData((FlashImage) resource);
      }
    });
  }

  private void setBitmapData(FlashImage resource) {
    this.image = resource;
    bitmapData = resource.bitmapData();
    applySourceRect();
  }

  private void applySourceRect() {
    if (sourceRect == null || bitmapData == null) {
      clippedSource = bitmapData;
    } else {
      clippedSource = BitmapData
          .create(sourceRect.getWidth(), sourceRect.getHeight(), true,
              0x00000000);
      clippedSource
          .copyPixels(bitmapData, sourceRect, Point.create(0, 0), null, null,
              true);
    }
    dirty = true;
    ((Bitmap) display()).setBitmapData(clippedSource);
  }

  /**
   *
   */
  public FlashImageLayer() {
    super(Bitmap.create(null));
    image = null;
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#clearHeight()
   */
  @Override
  public void clearHeight() {
    height = NOT_SET;
    dirty = true;
  }

  private void applySettingIfDirty() {
    if (dirty && image != null) {
      float dw = width();
      float dh = height();

      if (repeatX || repeatY) {
        float anchorWidth = repeatX ? dw : sourceRect != null ? sourceRect.getWidth() : image.width();
        float anchorHeight = repeatY ? dh : sourceRect != null ? sourceRect.getHeight() : image.height();

        Shape shape = Shape.create((int) anchorWidth, (int) anchorHeight);
        Graphics g = shape.getGraphics();
        g.beginBitmapFill(clippedSource, Matrix.create(), true, true);
        g.drawRect(0, 0, anchorWidth, anchorHeight);
        g.endFill();
        BitmapData data = BitmapData.create((int) dw, (int) dh, true, 0x00000000);
        data.draw(shape);
        ((Bitmap) display()).setBitmapData(data);
      } else {
        ((Bitmap) display()).setBitmapData(clippedSource);
      }
      display().setWidth((int) dw);
      display().setHeight((int) dh);
      dirty = false;
    }
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#clearSourceRect()
   */
  @Override
  public void clearSourceRect() {
    sourceRect = null;
    dirty = true;
    applySourceRect();
    applySettingIfDirty();
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#clearWidth()
   */
  @Override
  public void clearWidth() {
    width = NOT_SET;
    dirty = true;
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#image()
   */
  @Override
  public Image image() {
    return image;
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setHeight(float)
   */
  @Override
  public void setHeight(float height) {
    this.height = height;
    dirty = true;
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setImage(playn.core.Image)
   */
  @Override
  public void setImage(Image image) {
    setBitmapData((FlashImage) image);
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setRepeatX(boolean)
   */
  @Override
  public void setRepeatX(boolean repeat) {
    repeatX = repeat;
    dirty = true;
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setRepeatY(boolean)
   */
  @Override
  public void setRepeatY(boolean repeat) {
    repeatY = repeat;
    dirty = true;
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setSourceRect(float, float, float, float)
   */
  @Override
  public void setSourceRect(float sx, float sy, float sw, float sh) {
    sourceRect = Rectangle.create(sx, sy, sw, sh);
    dirty = true;
    applySourceRect();
    applySettingIfDirty();
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setWidth(float)
   */
  @Override
  public void setWidth(float width) {
    this.width = width;
    dirty = true;
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setSize(float, float)
   */
  @Override
  public void setSize(float width, float height) {
    setWidth(width);
    setHeight(height);
  }


  @Override
  public float width() {
    Asserts.checkNotNull(image, "Image must not be null");
    return width != NOT_SET ? width : sourceRect != null ? sourceRect.getWidth() : image.width();
  }

  @Override
  public float height() {
    Asserts.checkNotNull(image, "Image must not be null");
    return height != NOT_SET ? height : sourceRect != null ? sourceRect.getHeight() : image().height();
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
    applySettingIfDirty();
    super.update();
  }
}
