/**
 * Copyright 2010 The PlayN Authors
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
package playn.html;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.TouchEvent;

import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.PointerImpl;

class HtmlPointer extends PointerImpl implements Pointer {
  // true when we are in a drag sequence (after pointer start but before pointer end)
  private boolean inDragSequence = false;

  HtmlPointer(final Element rootElement) {
    // if touch events are supported, we want to use touch handlers; otherwise we use mouse event
    // handlers; we cannot use both because some browsers (mobile webkit) emit both touch and mouse
    // events for the same user action
    if (TouchEvent.isSupported()) {
      // capture touch start on the root element, only.
      HtmlInput.captureEvent(rootElement, "touchstart", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          JsArray<Touch> touches = nativeEvent.getChangedTouches();
          if (touches.length() > 0) {
            inDragSequence = true;
            // cancel touch events by default to prevent browser scrolling on iOS
            if (onPointerStart(eventFromTouch(rootElement, touches.get(0)), true))
              nativeEvent.preventDefault();
          }
        }
      });

      // capture touch end anywhere on the page as long as we are in a drag sequence
      HtmlInput.capturePageEvent("touchend", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          JsArray<Touch> touches = nativeEvent.getChangedTouches();
          if (inDragSequence && touches.length() > 0) {
            inDragSequence = false;
            // cancel touch events by default to prevent browser scrolling on iOS
            if (onPointerEnd(eventFromTouch(rootElement, touches.get(0)), true))
              nativeEvent.preventDefault();
          } else {
            nativeEvent.preventDefault();
          }
        }
      });

      // capture touch move anywhere on the page as long as we are in a drag sequence
      HtmlInput.capturePageEvent("touchmove", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          JsArray<Touch> touches = nativeEvent.getChangedTouches();
          if (inDragSequence && touches.length() > 0) {
            // cancel touch events by default to prevent browser scrolling on iOS
            if (onPointerDrag(eventFromTouch(rootElement, touches.get(0)), true))
              nativeEvent.preventDefault();
          } else {
            nativeEvent.preventDefault();
          }
        }
      });

    } else {
      // capture mouse down on the root element, only.
      HtmlInput.captureEvent(rootElement, "mousedown", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          inDragSequence = true;
          if (onPointerStart(eventFromMouse(rootElement, nativeEvent), false))
            nativeEvent.preventDefault();
        }
      });

      // capture mouse up anywhere on the page as long as we are in a drag sequence
      HtmlInput.capturePageEvent("mouseup", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          if (inDragSequence) {
            inDragSequence = false;
            if (onPointerEnd(eventFromMouse(rootElement, nativeEvent), false))
              nativeEvent.preventDefault();
          }
        }
      });

      // capture mouse move anywhere on the page that fires only if we are in a drag sequence
      HtmlInput.capturePageEvent("mousemove", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          if (inDragSequence) {
            if (onPointerDrag(eventFromMouse(rootElement, nativeEvent), false))
              nativeEvent.preventDefault();
          }
        }
      });
    }
  }

  private static Event.Impl eventFromMouse(final Element rootElement, NativeEvent nativeEvent) {
    float x = HtmlInput.getRelativeX(nativeEvent, rootElement);
    float y = HtmlInput.getRelativeY(nativeEvent, rootElement);
    return new Event.Impl(PlayN.currentTime(), x, y, false);
  }

  private static Event.Impl eventFromTouch(final Element rootElement, Touch touch) {
    float x = touch.getRelativeX(rootElement), y = touch.getRelativeY(rootElement);
    return new Event.Impl(PlayN.currentTime(), x, y, true);
  }
}
