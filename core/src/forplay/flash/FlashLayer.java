// Copyright 2011 Google Inc. All Rights Reserved.

package forplay.flash;


import forplay.core.Transform;

import flash.display.DisplayObject;

import forplay.core.ForPlay;

import forplay.core.AbstractLayer;

import flash.display.Sprite;

import flash.gwt.FlashImport;

/**
 * @author cromwellian@google.com (Your Name Here)
 *
 */
@FlashImport({"flash.display.Sprite"})
public class FlashLayer extends AbstractLayer {

  
  protected final DisplayObject displayObject;

  /**
   * @param sprite
   */
  public FlashLayer(DisplayObject displayObject) {
    this.displayObject = displayObject;
  }

  DisplayObject display() {
    return displayObject;
  }

  public void update() {
    updateDisplay();
    updateChildren();
  }

  /**
   * 
   */
  protected void updateChildren() {
    // TODO Auto-generated method stub
    
  }

   /**
   * 
   */
  private void updateDisplay() {
//    display().setX((int) originX);
//    display().setY((int) originY);
    Transform x = new Transform(Transform.IDENTITY);
    x.setTranslation(originX, originY);
    x.transform(transform.m00(), transform.m01(), transform.m10(), 
        transform.m11(), transform.tx() - originX, transform.ty() - originY);
    x.translate(-originX, -originY);
    display().setTransform(x.m00(), x.m01(), x.m10(),
        x.m11(), x.tx(), x.ty());
  }
  
  
}
