/**
 * Copyright 2010 The ForPlay Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package flash.events;

import com.google.gwt.core.client.JavaScriptObject;

import forplay.flash.EventHandler;

/**
 * Implementation of 
 * <a href="http://livedocs.adobe.com/flash/9.0/ActionScriptLangRefV3/flash/events/EventDispatcher.html">
 * flash.events.EventDispatcher</a>
 */
public class EventDispatcher extends JavaScriptObject {
  
  /**
   * Dispatched when Flash Player or an AIR application gains operating system 
   * focus and becomes active.
   */
  final public static EventType ACTIVATE = EventType.make("activate");
  
  /**
   * Dispatched when Flash Player or an AIR application gains operating system 
   * focus and becomes active.
   */
  final public static EventType DEACTIVATE = EventType.make("deactivate");
  
  protected EventDispatcher() {}
  
  public static EventDispatcher create() {
    return createObject().cast();
  }
  
  /**
   * Registers an event listener object with an EventDispatcher object so that the listener receives notification of an event.
   */
  final public native void addEventListener(EventType type, EventHandler<?> listener,  boolean useCapture, int priority, boolean useWeakReference) /*-{
    this.addEventListener(type, function(evt) {
      return listener.@forplay.flash.EventHandler::handleEvent(Lflash/events/Event;)(evt);
    }, useCapture, priority, useWeakReference);
  }-*/;
          
  /**
   * Dispatches an event into the event flow.
   **/
  final public native boolean dispatchEvent(Event event) /*-{
     return this.dispatchEvent(event); 
  }-*/;
    
  /**
   *   Checks whether the EventDispatcher object has any listeners registered for a specific type of event.
   * @param type
   * @return
   */
  final public native boolean hasEventListener(EventType type) /*-{
     return this.hasEventListener(type);
  }-*/;
       
  /**
   * Removes a listener from the EventDispatcher object.
   * @param type
   */
  final public native void removeEventListener(EventType type, EventHandler<?> listener, boolean useCapture) /*-{
    this.removeEventListener(type, listener, useCapture);
  }-*/;
          
  /**
   * Checks whether an event listener is registered with this EventDispatcher
   * object or any of its ancestors for the specified event type.
   * @return
   */
  final public native boolean willTrigger(EventType type) /*-{
    return this.willTrigger(type);
  }-*/;
}
