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
