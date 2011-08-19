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

/**
 * Implementation of <a href="http://livedocs.adobe.com/flash/9.0/ActionScriptLangRefV3/flash/events/KeyboardEvent.html">
 * flash.events.KeyboardEvent</a>
 */
public class KeyboardEvent extends Event {
  protected KeyboardEvent() {}
  
  /**
   * Indicates whether the Alt key is active (true) or inactive (false).
   * @return
   */
  final public native boolean isAltKey() /*-{
    return this.altKey;
  }-*/;
  
  /**
   * Contains the character code value of the key pressed or released.
   * @return
   */
  final public native int charCode() /*-{
    return this.charCode;
  }-*/;
  
  
  /**
   * The key code value of the key pressed or released.
   * @return
   */
  final public native int keyCode() /*-{
    return this.keyCode;
  }-*/;
  
  /**
   * Indicates the location of the key on the keyboard.
   * @return
   */
  final public native int keyLocation() /*-{
    return this.keyLocation;
  }-*/;
  
  
  /**
   * On Windows, indicates whether the Ctrl key is active (true) or inactive (false).
   * @return
   */
  final public native boolean isCtrlKey() /*-{
    return this.ctrlKey; 
  }-*/;
  
  /**
   * Indicates whether the Shift key is active (true) or inactive (false).
   * @return
   */
  final public native boolean isShiftKey() /*-{
    return this.shiftKey;
  }-*/;
}
