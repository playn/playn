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

  
  private final Image image;

  private BitmapData bitmapData;

  private Rectangle sourceRect;

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
    bitmapData = resource.bitmapData();
    applySourceRect();
  }

  private void applySourceRect() {
    BitmapData clippedSource;
    if (sourceRect == null || bitmapData == null) {
      clippedSource = bitmapData;
    } else {
      clippedSource = BitmapData.create(sourceRect.getWidth(), sourceRect.getHeight(), true, 0x000000FF);
      clippedSource.copyPixels(bitmapData, sourceRect, Point.create(0,0), null, null, false);
    }
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
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#clearSourceRect()
   */
  @Override
  public void clearSourceRect() {
    sourceRect = null;
    applySourceRect();
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#clearWidth()
   */
  @Override
  public void clearWidth() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#image()
   */
  @Override
  public Image image() {
    // TODO Auto-generated method stub
    return image;
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setHeight(float)
   */
  @Override
  public void setHeight(float height) {
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setRepeatY(boolean)
   */
  @Override
  public void setRepeatY(boolean repeat) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setSourceRect(float, float, float, float)
   */
  @Override
  public void setSourceRect(float sx, float sy, float sw, float sh) {
    sourceRect = Rectangle.create(sx, sy, sw, sh);
    applySourceRect();
  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setWidth(float)
   */
  @Override
  public void setWidth(float width) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see playn.core.ImageLayer#setSize(float, float)
   */
  @Override
  public void setSize(float width, float height) {
    // TODO Auto-generated method stub

  }


  @Override
  public float width() {
    Asserts.checkNotNull(image, "Image must not be null");
    return image.width();
  }

  @Override
  public float height() {
    Asserts.checkNotNull(image, "Image must not be null");
    return image.height();
  }

  @Override
  public float scaledWidth() {
    return transform().scaleX() * width();
  }

  @Override
  public float scaledHeight() {
    return transform().scaleY() * height();
  }
}
