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

package flash.display;

import flash.geom.Rectangle;

import flash.gwt.FlashImport;

@FlashImport({"flash.display.Sprite", "flash.geom.Matrix"})
final public class Sprite extends DisplayObjectContainer {
   protected Sprite() {}
   
   public static native Sprite getRootSprite() /*-{
      return $root; 
   }-*/;
   
   public static native Sprite create() /*-{
     return new flash.display.Sprite();
   }-*/;

   /**
    * Specifies the button mode of this sprite.
    * @return
    */
   public native boolean isButtonMode() /*-{
     return this.buttonMode;
   }-*/;
   
   public native void setButtonMode(boolean mode) /*-{
      this.buttonMode = mode;
   }-*/;
   
   /**
    * Specifies the display object over which the sprite is being dragged, or 
    * on which the sprite was dropped.
    * @return
    */
   public native DisplayObject getDropTarget() /*-{
     return this.dropTarget;
   }-*/;
   
   /**
    * Specifies the Graphics object that belongs to this sprite where vector 
    * drawing commands can occur.
    * @return
    */
   public native Graphics getGraphics() /*-{
     return this.graphics;
   }-*/;
   
   /**
    * Designates another sprite to serve as the hit area for a sprite.
    * @return
    */
   public native Sprite getHitArea() /*-{
     return this.hitArea;
   }-*/;
   
   public native void setHitArea(Sprite hitArea) /*-{
     this.hitArea = hitArea;
   }-*/;
   
   
//   /**
//    * Controls sound within this sprite.
//    */
//   public native SoundTransform getSoundTransform() /*-{
//     return this.soundTransform;
//   }-*/;
   
//   public native void setSoundTransform(SoundTransform transform) /*-{
//     this.soundTransform = transform;
//   }-*/;
   
   public native boolean isUseHandCursor() /*-{
     return this.useHandCursor;
   }-*/;
   
   public native void setUseHandCursor(boolean b) /*-{
     this.useHandCursor = b;
   }-*/;
  
  /**
   * Lets the user drag the specified sprite.
   * @param lockCenter
   * @param bounds
   */
  public native void startDrag(boolean lockCenter, Rectangle bounds) /*-{
    this.startDrag(lockCenter, bounds);
  }-*/;
  
    
  /**
   * Ends the startDrag() method.
   */
  public native void stopDrag() /*-{
    this.stopDrag();
  }-*/;
}
