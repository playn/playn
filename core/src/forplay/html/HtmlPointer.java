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

class HtmlPointer implements Pointer {

  private Listener listener;
  private boolean mouseDown;
  private Element capturingElement;

  HtmlPointer(final Element rootElement) {
    captureEvent(rootElement, "mousedown", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        // We need to prevent the default so that the target element doesn't
        // highlight.
        evt.preventDefault();
        setCapture(rootElement);
        mouseDown = true;
        if (listener != null) {
          listener.onPointerStart(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement));
        }
      }
    });
    captureEvent(rootElement, "mouseup", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        releaseCapture(rootElement);
        mouseDown = false;
        if (listener != null) {
          listener.onPointerEnd(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement));
        }
      }
    });
    captureEvent(rootElement, "mousemove", new EventHandler() {
      @Override
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
    captureEvent(rootElement, "touchstart", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          if (evt.getTouches().length() == 0) {
            return;
          }
          setCapture(rootElement);
          // TODO(pdr): these may need to call getRelativeX/getRelativeY.
          int x = evt.getTouches().get(0).getClientX();
          int y = evt.getTouches().get(0).getClientY();
          listener.onPointerStart(x, y);
        }
      }
    });
    captureEvent(rootElement, "touchend", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          if (evt.getTouches().length() == 0) {
            return;
          }
          releaseCapture(rootElement);
          // TODO(pdr): these may need to call getRelativeX/getRelativeY.
          int x = evt.getTouches().get(0).getClientX();
          int y = evt.getTouches().get(0).getClientY();
          listener.onPointerEnd(x, y);
        }
      }
    });
    captureEvent(rootElement, "touchmove", new EventHandler() {
      @Override
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
    String eventName = getMouseWheelEvent();
    captureEvent(rootElement, eventName, new EventHandler() {
      @Override
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

  /**
   * Helper method which allows element to 'capture' mouse/touch event which occur anywhere on the
   * page.
   */
  private void captureEvent(final Element elem, String eventName, final EventHandler handler) {

    // register regular event handler on the element
    HtmlPlatform.captureEvent(elem, eventName, handler);

    // register page level handler, which fires when the provided element is the capturing element
    HtmlPlatform.captureEvent(eventName, new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (elem == capturingElement) {
          handler.handleEvent(evt);
        }
      }
    });
  }

  protected void releaseCapture(Element capturingElement) {
    if (this.capturingElement == capturingElement) {
      this.capturingElement = null;
    }
  }

  protected void setCapture(Element capturingElement) {
    this.capturingElement = capturingElement;
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
    return e.getClientX() - target.getAbsoluteLeft() + target.getScrollLeft()
        + target.getOwnerDocument().getScrollLeft();
  }

  /**
   * Gets the event's y-position relative to a given element.
   * 
   * @param e native event
   * @param target the element whose coordinate system is to be used
   * @return the relative y-position
   */
  static int getRelativeY(NativeEvent e, Element target) {
    return e.getClientY() - target.getAbsoluteTop() + target.getScrollTop()
        + target.getOwnerDocument().getScrollTop();
  }

  /**
   * Return the appropriate mouse wheel event name for the current browser
   * 
   * @return return the mouse wheel event name for the current browser
   */
  static native String getMouseWheelEvent() /*-{
    if (navigator.userAgent.toLowerCase().indexOf('firefox') != -1) {
      return "DOMMouseScroll";
    } else {
      return "mousewheel";
    }
  }-*/;
}
