/**
 * Copyright 2011 The PlayN Authors
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

import playn.core.PlayN;
import playn.core.MouseImpl;

class HtmlMouse extends MouseImpl {
  private Listener listener;
  // true when we are in a drag sequence (after mouse down but before mouse up)
  boolean inDragSequence = false;

  HtmlMouse(final Element rootElement) {
    // capture mouse down on the root element, only.
    HtmlInput.captureEvent(rootElement, "mousedown", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent ev) {
        inDragSequence = true;
        float x = HtmlInput.getRelativeX(ev, rootElement);
        float y = HtmlInput.getRelativeY(ev, rootElement);
        if (onMouseDown(new ButtonEvent.Impl(PlayN.currentTime(), x, y, getMouseButton(ev))))
          ev.preventDefault();
      }
    });

    // capture mouse up anywhere on the page as long as we are in a drag sequence
    HtmlInput.capturePageEvent("mouseup", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent ev) {
        if (inDragSequence) {
          inDragSequence = false;
          float x = HtmlInput.getRelativeX(ev, rootElement);
          float y = HtmlInput.getRelativeY(ev, rootElement);
          if (onMouseUp(new ButtonEvent.Impl(PlayN.currentTime(), x, y, getMouseButton(ev))))
            ev.preventDefault();
        }
      }
    });

    // capture mouse move anywhere on the page that fires only if we are in a drag sequence
    HtmlInput.capturePageEvent("mousemove", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent ev) {
        if (inDragSequence) {
          float x = HtmlInput.getRelativeX(ev, rootElement);
          float y = HtmlInput.getRelativeY(ev, rootElement);
          if (onMouseMove(new MotionEvent.Impl(PlayN.currentTime(), x, y)))
            ev.preventDefault();
        }
      }
    });

    // capture mouse move on the root element that fires only if we are not in a drag sequence
    // (the page-level event listener will handle the firing when we are in a drag sequence)
    HtmlInput.captureEvent(rootElement, "mousemove", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent ev) {
        if (!inDragSequence) {
          float x = HtmlInput.getRelativeX(ev, rootElement);
          float y = HtmlInput.getRelativeY(ev, rootElement);
          if (onMouseMove(new MotionEvent.Impl(PlayN.currentTime(), x, y)))
            ev.preventDefault();
        }
      }
    });

    HtmlInput.captureEvent(rootElement, getMouseWheelEvent(), new EventHandler() {
      @Override
      public void handleEvent(NativeEvent ev) {
        if (onMouseWheelScroll(new WheelEvent.Impl(PlayN.currentTime(),
                                                   getMouseWheelVelocity(ev))))
          ev.preventDefault();
      }
    });
  }

  /**
   * Return the mouse wheel velocity for the event
   */

  private static native float getMouseWheelVelocity(NativeEvent evt) /*-{
    var delta = 0.0;
    var agentInfo = @playn.html.HtmlPlatform::agentInfo()();

    if (agentInfo.isFirefox) {
      if (agentInfo.isMacOS) {
        delta = 1.0 * evt.detail;
      } else {
        delta = 1.0 * evt.detail/3;
      }
    } else if (agentInfo.isOpera) {
      if (agentInfo.isLinux) {
        delta = -1.0 * evt.wheelDelta/80;
      } else {
        // on mac
        delta = -1.0 * evt.wheelDelta/40;
      }
    } else if (agentInfo.isChrome || agentInfo.isSafari) {
      delta = -1.0 * evt.wheelDelta/120;
      // handle touchpad for chrome
      if (Math.abs(delta) < 1) {
        if (agentInfo.isWindows) {
          delta = -1.0 * evt.wheelDelta;
        } else if (agentInfo.isMacOS) {
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

  protected static int getMouseButton(NativeEvent evt) {
    switch (evt.getButton()) {
    case (NativeEvent.BUTTON_LEFT):   return BUTTON_LEFT;
    case (NativeEvent.BUTTON_MIDDLE): return BUTTON_MIDDLE;
    case (NativeEvent.BUTTON_RIGHT):  return BUTTON_RIGHT;
    default:                          return evt.getButton();
    }
  }
}
