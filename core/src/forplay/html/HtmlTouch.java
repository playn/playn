/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.html;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

import forplay.core.Touch;

// TODO(pdr): Need to implement a JSO overlay type for TouchEvent
// so that we don't do a bunch of work copying all the native event
// stuff into the TouchEvent[] array.
class HtmlTouch extends HtmlInput implements Touch {
  private Listener listener;
  boolean inTouchSequence = false; // true when we are in a touch sequence (after touch start but before touch end)

  HtmlTouch(final Element rootElement) {
    // capture touch start on the root element, only.
    captureEvent(rootElement, "touchstart", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          // Prevent the default so that the target element doesn't highlight.
          evt.preventDefault();

          JsArray<com.google.gwt.dom.client.Touch> nativeTouches = evt.getTouches();
          int nativeTouchesLen = nativeTouches.length();

          if (nativeTouchesLen == 0) {
            listener.onTouchStart(new TouchEvent[0]);
            return;
          }

          inTouchSequence = true;

          // Convert the JsArray<Native Touch> to an array of TouchEvents
          // TODO(pdr): replace TouchEvent with a JSO overlay type to avoid so much work here
          TouchEvent[] touches = new TouchEvent[nativeTouchesLen];
          for (int t = 0; t < nativeTouchesLen; t++) {
            com.google.gwt.dom.client.Touch touch = nativeTouches.get(t);
            float x = touch.getRelativeX(rootElement);
            float y = touch.getRelativeY(rootElement);
            int id = getTouchIdentifier(evt, t);
            touches[t] = new TouchEvent(x, y, id);
          }
          listener.onTouchStart(touches);
        }
      }
    });

    // capture touch end anywhere on the page as long as we are in a touch sequence
    capturePageEvent("touchend", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null && inTouchSequence) {
          // Prevent the default so that the target element doesn't highlight.
          evt.preventDefault();

          JsArray<com.google.gwt.dom.client.Touch> nativeTouches = evt.getTouches();
          int nativeTouchesLen = nativeTouches.length();

          // Convert the JsArray<Native Touch> to an array of TouchEvents
          // TODO(pdr): replace TouchEvent with a JSO overlay type to avoid so much work here
          TouchEvent[] touches = new TouchEvent[nativeTouchesLen];
          for (int t = 0; t < nativeTouchesLen; t++) {
            com.google.gwt.dom.client.Touch touch = nativeTouches.get(t);
            float x = touch.getRelativeX(rootElement);
            float y = touch.getRelativeY(rootElement);
            int id = getTouchIdentifier(evt, t);
            touches[t] = new TouchEvent(x, y, id);
          }
          listener.onTouchEnd(touches);

          // ending a touch sequence
          inTouchSequence = false;
        }
      }
    });

    // capture touch move anywhere on the page as long as we are in a touch sequence
    capturePageEvent("touchmove", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null && inTouchSequence) {
          // Prevent the default so that the target element doesn't highlight.
          evt.preventDefault();

          JsArray<com.google.gwt.dom.client.Touch> nativeTouches = evt.getTouches();
          int nativeTouchesLen = nativeTouches.length();

          // Convert the JsArray<Native Touch> to an array of TouchEvents
          // TODO(pdr): replace TouchEvent with a JSO overlay type to avoid so much work here
          TouchEvent[] touches = new TouchEvent[nativeTouchesLen];
          for (int t = 0; t < nativeTouchesLen; t++) {
            com.google.gwt.dom.client.Touch touch = nativeTouches.get(t);
            float x = touch.getRelativeX(rootElement);
            float y = touch.getRelativeY(rootElement);
            int id = getTouchIdentifier(evt, t);
            touches[t] = new TouchEvent(x, y, id);
          }
          listener.onTouchMove(touches);
        }
      }
    });
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  /**
   * Return the unique identifier of a touch, or 0
   * 
   * @return return the unique identifier of a touch, or 0
   */
  private static native int getTouchIdentifier(NativeEvent evt, int index) /*-{
    return evt.touches[index].identifier || 0;
  }-*/;
}
