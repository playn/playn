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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

import forplay.core.Mouse;

class HtmlMouse implements Mouse {

  private Listener listener;
  private Element capturingElement;

  HtmlMouse(final Element rootElement) {
    captureEvent(rootElement, "mousedown", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        // Prevent the default so that the target element doesn't highlight.
        evt.preventDefault();
        // Set so we catch future events (mouse up) anywhere on the page
        setCaptureAnywhere(rootElement);
        if (listener != null) {
          listener.onMouseDown(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement), getMouseButton(evt));
        }
      }
    });
    captureEvent(rootElement, "mouseup", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        // Prevent the default so that the target element doesn't highlight.
        evt.preventDefault();
        // Remove so we stop catching future events anywhere on the page
        if (releaseCaptureAnywhere(rootElement)) {
          if (listener != null) {
            listener.onMouseUp(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement), getMouseButton(evt));
          }
        }
      }
    });
    captureEvent(rootElement, "mousemove", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          listener.onMouseMove(getRelativeX(evt, rootElement), getRelativeY(evt, rootElement));
        }
      }
    });
    String eventName = getMouseWheelEvent();
    captureEvent(rootElement, eventName, new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        // We need to prevent the default so that the page doesn't scroll.
        // The user can still scroll if the mouse isn't over the root element.
        evt.preventDefault();
        if (listener != null) {
          listener.onMouseWheelScroll(getMouseWheelVelocity(evt));
        }
      }
    });
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  /**
   * Helper method which allows element to 'capture' mouse/touch event which occur anywhere on the
   * page.
   */
  protected void captureEvent(final Element elem, String eventName, final EventHandler handler) {
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

  protected boolean releaseCaptureAnywhere(Element capturingElement) {
    if (this.capturingElement == capturingElement) {
      this.capturingElement = null;
      return true;
    } else {
      return false;
    }
  }

  protected void setCaptureAnywhere(Element capturingElement) {
    this.capturingElement = capturingElement;
  }

  /**
   * Gets the event's x-position relative to a given element.
   * 
   * @param e native event
   * @param target the element whose coordinate system is to be used
   * @return the relative x-position
   */
  protected static float getRelativeX(NativeEvent e, Element target) {
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
  protected static float getRelativeY(NativeEvent e, Element target) {
    return e.getClientY() - target.getAbsoluteTop() + target.getScrollTop()
        + target.getOwnerDocument().getScrollTop();
  }

  /**
   * Return the mouse wheel velocity for the event
   */
  private static native float getMouseWheelVelocity(NativeEvent evt) /*-{
    return -(evt.detail ? evt.detail * -1 : evt.wheelDelta / 40);
  }-*/;

  /**
   * Return the appropriate mouse wheel event name for the current browser
   * 
   * @return return the mouse wheel event name for the current browser
   */
  protected static native String getMouseWheelEvent() /*-{
    if (navigator.userAgent.toLowerCase().indexOf('firefox') != -1) {
      return "DOMMouseScroll";
    } else {
      return "mousewheel";
    }
  }-*/;

  /**
   * Return the {@link Mouse} button given a {@link NativeEvent}
   * 
   * @param evt Native event
   * @return {@link Mouse} button corresponding to the event
   */
  protected static int getMouseButton(NativeEvent evt) {
    switch (evt.getButton()) {
      case (NativeEvent.BUTTON_LEFT):
        return Mouse.BUTTON_LEFT;
      case (NativeEvent.BUTTON_MIDDLE):
        return Mouse.BUTTON_MIDDLE;
      case (NativeEvent.BUTTON_RIGHT):
        return Mouse.BUTTON_RIGHT;
      default:
        return evt.getButton();
    }
  }
}
