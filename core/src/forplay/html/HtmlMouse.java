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

import forplay.core.ForPlay;
import forplay.core.Mouse;

class HtmlMouse extends HtmlInput implements Mouse {
  private Listener listener;
  boolean inDragSequence = false; // true when we are in a drag sequence (after mouse down but before mouse up)

  HtmlMouse(final Element rootElement) {
    // capture mouse down on the root element, only.
    captureEvent(rootElement, "mousedown", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          // Prevent the default so that the target element doesn't highlight.
          evt.preventDefault();

          inDragSequence = true;

          listener.onMouseDown(
            new ButtonEvent.Impl(ForPlay.currentTime(), getRelativeX(evt, rootElement),
                                 getRelativeY(evt, rootElement), getMouseButton(evt)));
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

          listener.onMouseUp(
            new ButtonEvent.Impl(ForPlay.currentTime(), getRelativeX(evt, rootElement),
                                 getRelativeY(evt, rootElement), getMouseButton(evt)));
        }
      }
    });

    // capture mouse move anywhere on the page that fires only if we are in a drag sequence
    capturePageEvent("mousemove", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null && inDragSequence) {
          evt.preventDefault();
          listener.onMouseMove(
            new MotionEvent.Impl(ForPlay.currentTime(), getRelativeX(evt, rootElement),
                                 getRelativeY(evt, rootElement)));
        }
      }
    });

    // capture mouse move on the root element that fires only if we are not in a drag sequence
    // (the page-level event listener will handle the firing when we are in a drag sequence)
    captureEvent(rootElement, "mousemove", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null && !inDragSequence) {
          listener.onMouseMove(
            new MotionEvent.Impl(ForPlay.currentTime(), getRelativeX(evt, rootElement),
                                 getRelativeY(evt, rootElement)));
        }
      }
    });

    captureEvent(rootElement, getMouseWheelEvent(), new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          // We need to prevent the default so that the page doesn't scroll.
          // The user can still scroll if the mouse isn't over the root element.
          evt.preventDefault();

          listener.onMouseWheelScroll(
            new WheelEvent.Impl(ForPlay.currentTime(), getMouseWheelVelocity(evt)));
        }
      }
    });
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  /**
   * Return the mouse wheel velocity for the event
   */

  private static native float getMouseWheelVelocity(NativeEvent evt) /*-{
    var delta = 0.0; 
    var useragent = navigator.userAgent.toLowerCase(); 
    
    if (useragent.indexOf('firefox') != -1) {
      if (useragent.indexOf('mac') != -1) {
        delta = 1.0 * evt.detail;
      } else {
        delta = 1.0 * evt.detail/3;
      }
    } else if (useragent.indexOf('opera') != -1) {
      if (useragent.indexOf('linux') != -1) {
        delta = -1.0 * evt.wheelDelta/80;
      } else {
        // on mac
        delta = -1.0 * evt.wheelDelta/40;
      }
    } else if (useragent.indexOf('chrome') != -1 || 
        useragent.indexOf('safari') != -1) {
      delta = -1.0 * evt.wheelDelta/120;
      // handle touchpad for chrome
      if (Math.abs(delta) < 1) {
        if (useragent.indexOf('win') != -1) {
          delta = -1.0 * evt.wheelDelta;
        } else if (useragent.indexOf('mac') != -1) {
          delta = -1.0 * evt.wheelDelta/3;
        }
      }
    }
    return delta;
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
