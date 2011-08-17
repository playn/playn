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

import static flash.events.EventType.make;

import flash.events.EventType;

/**
 * Implementation of <a href="http://livedocs.adobe.com/flash/9.0/ActionScriptLangRefV3/flash/display/InteractiveObject.html">
 * flash.display.InteractiveObject</a>
 */
public class InteractiveObject extends DisplayObject {
  protected InteractiveObject() {}

  /* final public native NativeMenu getContextMenu : NativeMenu
  Specifies the context menu associated with this object.*/

  /**
   * Specifies whether the object receives doubleClick events.
   */
  final public native boolean getDoubleClickEnabled() /*-{
    return this.doubleClickEnabled;
  }-*/;


  /*       focusRect : Object
  Specifies whether this object displays a focus rectangle.
  InteractiveObject*/

  /**
   * Specifies whether this object receives mouse messages.
   */
  final public native boolean getMouseEnabled() /*-{
    return this.mouseEnabled;
  }-*/;

  /**
   * Specifies whether this object is in the tab order.
   * @return
   */
  final public native boolean getTabEnabled() /*-{
    return this.tabEnabled;
  }-*/;

  /**
   * Specifies the tab ordering of objects in a SWF file.
   * @return
   */
  final public native int getTabIndex() /*-{
    return this.tabIndex;
  }-*/;

  final public native void setMouseEnabled(boolean enabled) /*-{
    this.mouseEnabled = enabled;
  }-*/;


  final public native void setTabEnabled(boolean enabled) /*-{
    this.tabEnabled = enabled;
  }-*/;


  final public native void setTabIndex(int tabIndex) /*-{
    this.tabIndex = tabIndex;
  }-*/;

  /**
   *   Dispatched when a user presses and releases the main button of the 
   *   user's pointing device over the same InteractiveObject. 
   */
  final static public EventType CLICK = make("click");
  /**
  Dispatched when a user presses and releases the main button of a pointing
   device twice in rapid succession over the same InteractiveObject when that
    object's doubleClickEnabled flag is set to true. 
   */
  final static public EventType DOUBLECLICK = make("doubleClick");
  /**
   * Dispatched after a display object gains focus. 
   */
  final static public EventType FOCUSIN = make("focusIn");
  /**
   * Dispatched after a display object loses focus.  
   */
  final static public EventType FOCUSOUT = make("focusOut");
  /**
   * Dispatched when the user presses a key. InteractiveObject
   */
  final static public EventType KEYDOWN = make("keyDown");
  /**
   * Dispatched when the user presses a key. InteractiveObject
   */
  final static public EventType KEYFOCUSCHANGE = make("keyFocusChange");


  /**
   * Dispatched when the user releases a key.    
   */
  final static public EventType KEYUP = make("keyUp");
  /**
  Dispatched when a user presses the pointing device button over an InteractiveObject instance.   
   */
  final static public EventType MOUSEDOWN = make("mouseDown");
  /**
  Dispatched when the user attempts to change focus by using a pointer device.    
   */
  final static public EventType MOUSEFOCUSCHANGE = make("mouseFocusChange");
  /**
   */
  final static public EventType MOUSEMOVE = make("mouseMove");
  /**
   * Dispatched when the user moves a pointing device away from an InteractiveObject instance.   
   */
  final static public EventType MOUSEOUT = make("mouseOut");
  /**
   * Dispatched when the user moves a pointing device over an InteractiveObject instance.   
   */
  final static public EventType MOUSEOVER = make("mouseOver");
  /**
   * Dispatched when a user releases the pointing device button over an InteractiveObject instance. 
   */
  final static public EventType MOUSEUP = make("mouseUp");

  /**
   * Dispatched when a user releases the pointing device button over an InteractiveObject instance. 
   */
  final static public EventType MOUSEWHEEL = make("mouseWheel");

  /**
   * Dispatched when the user moves a pointing device away from an InteractiveObject instance.  
   */
  final static public EventType ROLLOUT = make("rollout");

  /**
   * Dispatched when the user moves a pointing device over an InteractiveObject instance.
   */
  final static public EventType ROLLOVER = make("rollOver");

  /**
   * Dispatched when the value of the object's tabChildren flag changes. 
   */
  final static public EventType TABCHILDRENCHANGE = make("tabChildrenChange");


  /**
   * Dispatched when the object's tabEnabled flag changes.  
   */
  final static public EventType TABCENABLEDCHANGE = make("tabEnabledChange");
  
  /**
   * Dispatched when the value of the object's tabIndex property changes.
   */
  final static public EventType TABINDEXCHANGE = make("tabIndexChange");

}
