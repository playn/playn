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
package forplay.html;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

import forplay.core.Pointer;

class HtmlPointer extends HtmlInput implements Pointer {
  private Listener listener;
  boolean inDragSequence = false; // true when we are in a drag sequence (after pointer start but before pointer end)

  HtmlPointer(final Element rootElement) {
    // capture mouse down on the root element, only.
    captureEvent(rootElement, "mousedown", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          // Prevent the default so that the target element doesn't highlight.
          evt.preventDefault();

          inDragSequence = true;

          listener.onPointerStart(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement));
        }
      }
    });

    // capture mouse up anywhere on the page as long as we are in a drag sequence
    capturePageEvent("mouseup", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null && inDragSequence) {
          // Prevent the default so that the target element doesn't highlight.
          evt.preventDefault();

          inDragSequence = false;

          listener.onPointerEnd(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement));
        }
      }
    });

    // capture mouse move anywhere on the page that fires only if we are in a drag sequence
    capturePageEvent("mousemove", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null && inDragSequence) {
          evt.preventDefault();
          listener.onPointerDrag(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement));
        }
      }
    });

    // capture touch start on the root element, only.
    captureEvent(rootElement, "touchstart", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          // Prevent the default so that the target element doesn't highlight.
          evt.preventDefault();

          if (evt.getTouches().length() > 0) {
            inDragSequence = true;
            com.google.gwt.dom.client.Touch touch = evt.getTouches().get(0);
            float x = touch.getRelativeX(rootElement);
            float y = touch.getRelativeY(rootElement);
            listener.onPointerStart(x, y);
          }
        }
      }
    });

    // capture touch end anywhere on the page as long as we are in a drag sequence
    capturePageEvent("touchend", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null && inDragSequence) {
          // Prevent the default so that the target element doesn't highlight.
          evt.preventDefault();

          if (evt.getTouches().length() > 0) {
            inDragSequence = false;
            com.google.gwt.dom.client.Touch touch = evt.getTouches().get(0);
            float x = touch.getRelativeX(rootElement);
            float y = touch.getRelativeY(rootElement);
            listener.onPointerEnd(x, y);
          }
        }
      }
    });

    // capture touch move anywhere on the page as long as we are in a drag sequence
    capturePageEvent("touchmove", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null && inDragSequence) {
          // Prevent the default so that the target element doesn't highlight.
          evt.preventDefault();

          if (evt.getTouches().length() > 0) {
            com.google.gwt.dom.client.Touch touch = evt.getTouches().get(0);
            float x = touch.getRelativeX(rootElement);
            float y = touch.getRelativeY(rootElement);
            listener.onPointerDrag(x, y);
          }
        }
      }
    });
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }
}
