// Copyright 2011 Google Inc. All Rights Reserved.

package flash.events;

import flash.display.InteractiveObject;

/**
 * Implemnents <a href="http://livedocs.adobe.com/flash/9.0/ActionScriptLangRefV3/flash/events/MouseEvent.html">
 * flash.events.MouseEvent</a>
 */
public class MouseEvent extends Event {
  protected MouseEvent() {}
  
  /**
   * Indicates whether the Alt key is active (true) or inactive (false).
   * @return
   */
  final public native boolean isAltKey() /*-{
    return this.altKey;
  }-*/;
  
  /**
   * Indicates whether the primary mouse button is pressed (true) or not (false).
   * @return
   */
  final public native boolean isButtonDown() /*-{
   return this.isButtonDown;
  }-*/;
  
  /**
   * On Windows, indicates whether the Ctrl key is active (true) or inactive (false).
   * @return
   */
  final public native boolean isCtrlKey() /*-{
    return this.ctrlKey; 
  }-*/;
  
  /**
   * Indicates how many lines should be scrolled for each unit the user rotates the mouse wheel.

   * @return
   */
  final public native int getDelta() /*-{
    return this.delta;
  }-*/;
  
  /**
   * The horizontal coordinate at which the event occurred relative to the containing sprite.
   * @return
   */
  final public native int getLocalX() /*-{
    return this.localX;
  }-*/;
  
  /**
   * The vertical coordinate at which the event occurred relative to the containing sprite.
   * @return
   */
  final public native int getLocalY() /*-{
    return this.localY;
  }-*/;
  
  /**
   * A reference to a display list object that is related to the event.
   * @return
   */
  final public native InteractiveObject getRelatedObject() /*-{
    return this.relatedObject;
  }-*/;
  
  /**
   * Indicates whether the Shift key is active (true) or inactive (false).
   * @return
   */
  final public native boolean isShiftKey() /*-{
    return this.shiftKey;
  }-*/;
  
  /**
   * The horizontal coordinate at which the event occurred in global Stage coordinates.
   * @return
   */
  final public native int getStageX() /*-{
    return this.stageX;
  }-*/;
  
  /**
   * The vertical coordinate at which the event occurred in global Stage coordinates.
   * @return
   */
  final public native int getStageY() /*-{
    return this.stageY;
  }-*/;
}
