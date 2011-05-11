// Copyright 2011 Google Inc. All Rights Reserved.

package flash.events;

/**
 * Implementation of <a href="http://livedocs.adobe.com/flash/9.0/ActionScriptLangRefV3/flash/events/KeyboardEvent.html">
 * flash.events.KeyboardEvent</a>
 *
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
