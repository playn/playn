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

import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.TouchEvent;

import playn.core.PlayN;
import playn.core.Pointer;

class HtmlPointer extends HtmlInput implements Pointer {
  private Listener listener;
  // true when we are in a drag sequence (after pointer start but before pointer end)
  private boolean inDragSequence = false;

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  HtmlPointer(final Element rootElement) {
    // if touch events are supported, we want to use touch handlers; otherwise we use mouse event
    // handlers; we cannot use both because some browsers (mobile webkit) emit both touch and mouse
    // events for the same user action
    if (TouchEvent.isSupported()) {
      // capture touch start on the root element, only.
      captureEvent(rootElement, "touchstart", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          if (listener != null) {
            Event.Impl event = null;
            if (nativeEvent.getChangedTouches().length() > 0) {
              inDragSequence = true;
              event = eventFromTouch(rootElement, nativeEvent.getChangedTouches().get(0));
              listener.onPointerStart(event);
            }
            // cancel touch events by default to prevent browser scrolling on iOS
            if (event == null || event.getPreventDefault()) {
              nativeEvent.preventDefault();
            }
          }
        }
      });

      // capture touch end anywhere on the page as long as we are in a drag sequence
      capturePageEvent("touchend", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          if (listener != null) {
            Event.Impl event = null;
            if (inDragSequence) {
              if (nativeEvent.getChangedTouches().length() > 0) {
                inDragSequence = false;
                event = eventFromTouch(rootElement, nativeEvent.getChangedTouches().get(0));
                listener.onPointerEnd(event);
              }
            }
            // cancel touch events by default to prevent browser scrolling on iOS
            if (event == null || event.getPreventDefault()) {
              nativeEvent.preventDefault();
            }
          }
        }
      });

      // capture touch move anywhere on the page as long as we are in a drag sequence
      capturePageEvent("touchmove", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          if (listener != null) {
            Event.Impl event = null;
            if (inDragSequence) {
              if (nativeEvent.getChangedTouches().length() > 0) {
                event = eventFromTouch(rootElement, nativeEvent.getChangedTouches().get(0));
                listener.onPointerDrag(event);
              }
            }
            // cancel touch events by default to prevent browser scrolling on iOS
            if (event == null || event.getPreventDefault()) {
              nativeEvent.preventDefault();
            }
          }
        }
      });

    } else {
      // capture mouse down on the root element, only.
      captureEvent(rootElement, "mousedown", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          if (listener != null) {
            inDragSequence = true;

            Event.Impl event = new Event.Impl(PlayN.currentTime(), getRelativeX(nativeEvent,
                rootElement), getRelativeY(nativeEvent, rootElement), false);
            listener.onPointerStart(event);
            if (event.getPreventDefault()) {
              nativeEvent.preventDefault();
            }
          }
        }
      });

      // capture mouse up anywhere on the page as long as we are in a drag sequence
      capturePageEvent("mouseup", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          if (listener != null && inDragSequence) {
            inDragSequence = false;

            Event.Impl event = new Event.Impl(PlayN.currentTime(), getRelativeX(nativeEvent,
                rootElement), getRelativeY(nativeEvent, rootElement), false);
            listener.onPointerEnd(event);
            if (event.getPreventDefault()) {
              nativeEvent.preventDefault();
            }
          }
        }
      });

      // capture mouse move anywhere on the page that fires only if we are in a drag sequence
      capturePageEvent("mousemove", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent nativeEvent) {
          if (listener != null && inDragSequence) {
            Event.Impl event = new Event.Impl(PlayN.currentTime(), getRelativeX(nativeEvent,
                rootElement), getRelativeY(nativeEvent, rootElement), false);
            listener.onPointerDrag(event);
            if (event.getPreventDefault()) {
              nativeEvent.preventDefault();
            }
          }
        }
      });
    }
  }

  /**
   * Converts a {@link Touch} to a pointer {@link Event}.
   */
  private static Event.Impl eventFromTouch(final Element rootElement, Touch touch) {
    Event.Impl event;
    float x = touch.getRelativeX(rootElement);
    float y = touch.getRelativeY(rootElement);
    event = new Event.Impl(PlayN.currentTime(), x, y, true);
    event.setPreventDefault(true);
    return event;
  }

}
