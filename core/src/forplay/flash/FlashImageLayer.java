// Copyright 2011 Google Inc. All Rights Reserved.

package forplay.flash;

import forplay.core.ForPlay;

import forplay.core.ResourceCallback;

import forplay.core.ResourceCallback;

import flash.display.Bitmap;

import flash.display.Sprite;

import forplay.core.AbstractLayer;

import forplay.core.GroupLayer;
import forplay.core.Image;
import forplay.core.Transform;

import forplay.core.ImageLayer;

/**
 * @author cromwellian@google.com 
 */
public class FlashImageLayer extends FlashLayer implements ImageLayer {

  
  private final Image image;

  /**
   * @param image
   */
  public FlashImageLayer(Image image) {
    super((Sprite) (Bitmap.create(((FlashImage) image).bitmapData()).cast()));
    this.image = image;
    image.addCallback(new ResourceCallback<Image>() {
      
      @Override
      public void error(Throwable err) {
       ForPlay.log().error(err.toString());
      }
    
      @Override
      public void done(Image resource) {
        ((Bitmap) display().cast()).setBitmapData(((FlashImage) resource).bitmapData());
      }
    });
  }

  /**
   * 
   */
  public FlashImageLayer() {
    super(Bitmap.create(null));
    image = null;
  }

  /* (non-Javadoc)
   * @see forplay.core.ImageLayer#clearHeight()
   */
  @Override
  public void clearHeight() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see forplay.core.ImageLayer#clearSourceRect()
   */
  @Override
  public void clearSourceRect() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see forplay.core.ImageLayer#clearWidth()
   */
  @Override
  public void clearWidth() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see forplay.core.ImageLayer#image()
   */
  @Override
  public Image image() {
    // TODO Auto-generated method stub
    return image;
  }

  /* (non-Javadoc)
   * @see forplay.core.ImageLayer#setHeight(float)
   */
  @Override
  public void setHeight(float height) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see forplay.core.ImageLayer#setImage(forplay.core.Image)
   */
  @Override
  public void setImage(Image image) {
    ((Bitmap) display().cast()).setBitmapData(((FlashImage) image).bitmapData());


  }

  /* (non-Javadoc)
   * @see forplay.core.ImageLayer#setRepeatX(boolean)
   */
  @Override
  public void setRepeatX(boolean repeat) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see forplay.core.ImageLayer#setRepeatY(boolean)
   */
  @Override
  public void setRepeatY(boolean repeat) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see forplay.core.ImageLayer#setSourceRect(float, float, float, float)
   */
  @Override
  public void setSourceRect(float sx, float sy, float sw, float sh) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see forplay.core.ImageLayer#setWidth(float)
   */
  @Override
  public void setWidth(float width) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see forplay.core.ImageLayer#setSize(float, float)
   */
  @Override
  public void setSize(float width, float height) {
    // TODO Auto-generated method stub

  }
}
