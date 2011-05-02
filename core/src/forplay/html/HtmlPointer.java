/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.html;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

import forplay.core.Pointer;

class HtmlPointer implements Pointer {

  private Listener listener;
  private boolean mouseDown;

  HtmlPointer(final Element rootElement) {
    // Mouse handlers.
    HtmlPlatform.captureEvent(rootElement, "mousedown", new EventHandler() {
      public void handleEvent(NativeEvent evt) {
        // We need to prevent the default so that the target element doesn't
        // highlight.
    	  evt.preventDefault();
        mouseDown = true;
        if (listener != null) {
          listener.onPointerStart(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement));
        }
      }
    });
    HtmlPlatform.captureEvent(rootElement, "mouseup", new EventHandler() {
      public void handleEvent(NativeEvent evt) {
        mouseDown = false;
        if (listener != null) {
          listener.onPointerEnd(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement));
        }
      }
    });
    HtmlPlatform.captureEvent(rootElement, "mousemove", new EventHandler() {
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          if (mouseDown) {
            listener.onPointerDrag(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement));
          } else {
            listener.onPointerMove(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement));
          }
        }
      }
    });

    // Touch handlers.
    HtmlPlatform.captureEvent(rootElement, "touchstart", new EventHandler() {
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          if (evt.getTouches().length() == 0) {
            return;
          }
          // TODO(pdr): these may need to call getRelativeX/getRelativeY.
          int x = evt.getTouches().get(0).getClientX();
          int y = evt.getTouches().get(0).getClientY();
          listener.onPointerStart(x, y);
        }
      }
    });
    HtmlPlatform.captureEvent(rootElement, "touchend", new EventHandler() {
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          if (evt.getTouches().length() == 0) {
            return;
          }
          // TODO(pdr): these may need to call getRelativeX/getRelativeY.
          int x = evt.getTouches().get(0).getClientX();
          int y = evt.getTouches().get(0).getClientY();
          listener.onPointerEnd(x, y);
        }
      }
    });
    HtmlPlatform.captureEvent(rootElement, "touchmove", new EventHandler() {
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          if (evt.getTouches().length() == 0) {
            return;
          }
          // TODO(pdr): these may need to call getRelativeX/getRelativeY.
          int x = evt.getTouches().get(0).getClientX();
          int y = evt.getTouches().get(0).getClientY();
          listener.onPointerDrag(x, y);
        }
      }
    });

    // Scroll handlers
    HtmlPlatform.captureEvent(rootElement, "mousewheel", new EventHandler() {
      public void handleEvent(NativeEvent evt) {
        // We need to prevent the default so that the page doesn't scroll.
        // The user can still scroll if the mouse isn't over the root element.
        evt.preventDefault();
        if (listener != null) {
          listener.onPointerScroll(evt.getMouseWheelVelocityY());
        }
      }
    });
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  /**
   * Gets the event's x-position relative to a given element.
   * 
   * @param e native event
   * @param target the element whose coordinate system is to be used
   * @return the relative x-position
   */
  static int getRelativeX(NativeEvent e, Element target) {
    return e.getClientX() - target.getAbsoluteLeft() + target.getScrollLeft() +
      target.getOwnerDocument().getScrollLeft();
  }

  /**
   * Gets the event's y-position relative to a given element.
   * 
   * @param e native event
   * @param target the element whose coordinate system is to be used
   * @return the relative y-position
   */
  static int getRelativeY(NativeEvent e, Element target) {
    return e.getClientY() - target.getAbsoluteTop() + target.getScrollTop() +
      target.getOwnerDocument().getScrollTop();
  }
}
