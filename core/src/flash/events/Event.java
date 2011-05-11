/*
 * Copyright 2010 Google Inc.
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

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Implements <a href="http://livedocs.adobe.com/flash/9.0/ActionScriptLangRefV3/flash/events/Event.html">
 * flash.events.Event</a>
 */
public class Event extends JavaScriptObject {

  protected Event() {}
  
  /**
   * Indicates whether an event is a bubbling event.
   * @return
   */
  final public native boolean getBubbles() /*-{
    return this.bubbles;  
  }-*/;
  
  /**
   * Indicates whether an event is a bubbling event.
   * @return
   */
  final public native boolean getCancelable() /*-{
    return this.cancelable;
  }-*/;
  
  /**
   * The object that is actively processing the Event object with an event listener.
   * @return
   */
  final public native JavaScriptObject getCurrentTarget() /*-{
    return this.currentTarget;
  }-*/;
  
  /**
   * The current phase in the event flow.
   * @return
   */
  final public native int getEventPhase() /*-{
    return this.eventPhase;
  }-*/;
  
  /**
   * The event target.
   * @return
   */
  final public native JavaScriptObject getTarget() /*-{
    return this.target;  
  }-*/;
  
  /**
   * The type of event. 
   * @return
   */
  final public native EventType getType() /*-{
    return this.type;
  }-*/;
  
  /**
   * Checks whether the preventDefault() method has been called on the event.
   */
  final public native boolean isDefaultPrevented() /*-{
    return this.isDefaultPrevented();
  }-*/;
  
  /**
   * Cancels an event's default behavior if that behavior can be canceled.
   */
  final public native void preventDefault() /*-{
    this.preventDefault();
  }-*/;

  /**
   * Prevents processing of any event listeners in the current node and any subsequent nodes in the event flow.
   */
  final public native void stopImmediatePropagation() /*-{
    this.stopImmediatePropagation();  
  }-*/;
  
  /**
   * Prevents processing of any event listeners in nodes subsequent to the current node in the event flow.
   */
  final public native void stopPropagation() /*-{
    this.stopPropagation();  
  }-*/;
}
