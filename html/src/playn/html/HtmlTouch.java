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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

import playn.core.PlayN;
import playn.core.Touch;

class HtmlTouch extends HtmlInput implements Touch {
  private Listener listener;
  boolean inTouchSequence = false; // true when we are in a touch sequence (after touch start but before touch end)

  /**
   * Special implementation of Event.Impl for keeping track of changes to preventDefault
   */
  static class HtmlTouchEventImpl extends Event.Impl {
    final boolean[] preventDefault;

    public HtmlTouchEventImpl(double time, float x, float y, int id, boolean[] preventDefault) {
      super(time, x, y, id);
      this.preventDefault = preventDefault;
    }

    @Override
    public void setPreventDefault(boolean preventDefault) {
      this.preventDefault[0] = preventDefault;
    }

    @Override
    public boolean getPreventDefault() {
      return preventDefault[0];
    }
  }

  HtmlTouch(final Element rootElement) {
    // capture touch start on the root element, only.
    captureEvent(rootElement, "touchstart", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent nativeEvent) {
        if (listener != null) {
          JsArray<com.google.gwt.dom.client.Touch> nativeTouches = nativeEvent.getChangedTouches();
          int nativeTouchesLen = nativeTouches.length();

          if (nativeTouchesLen == 0) {
            listener.onTouchStart(new Event[0]);
            return;
          }

          inTouchSequence = true;
          boolean[] preventDefault = {false};

          // Convert the JsArray<Native Touch> to an array of Touch.Events
          Event[] touches = new Event[nativeTouchesLen];
          for (int t = 0; t < nativeTouchesLen; t++) {
            com.google.gwt.dom.client.Touch touch = nativeTouches.get(t);
            float x = touch.getRelativeX(rootElement);
            float y = touch.getRelativeY(rootElement);
            int id = getTouchIdentifier(nativeEvent, t);
            touches[t] = new HtmlTouchEventImpl(PlayN.currentTime(), x, y, id, preventDefault);
          }
          listener.onTouchStart(touches);
          if (preventDefault[0]) {
            nativeEvent.preventDefault();
          }
        }
      }
    });

    // capture touch end anywhere on the page as long as we are in a touch sequence
    capturePageEvent("touchend", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent nativeEvent) {
        if (listener != null && inTouchSequence) {
          JsArray<com.google.gwt.dom.client.Touch> nativeTouches = nativeEvent.getChangedTouches();
          int nativeTouchesLen = nativeTouches.length();

          boolean[] preventDefault = {false};

          // Convert the JsArray<Native Touch> to an array of Touch.Events
          Event[] touches = new Event[nativeTouchesLen];
          for (int t = 0; t < nativeTouchesLen; t++) {
            com.google.gwt.dom.client.Touch touch = nativeTouches.get(t);
            float x = touch.getRelativeX(rootElement);
            float y = touch.getRelativeY(rootElement);
            int id = getTouchIdentifier(nativeEvent, t);
            touches[t] = new HtmlTouchEventImpl(PlayN.currentTime(), x, y, id, preventDefault);
          }
          listener.onTouchEnd(touches);
          if (preventDefault[0]) {
            nativeEvent.preventDefault();
          }

          // if there are no remaining active touches, note that this touch sequence has ended
          if (nativeEvent.getTouches().length() == 0)
            inTouchSequence = false;
        }
      }
    });

    // capture touch move anywhere on the page as long as we are in a touch sequence
    capturePageEvent("touchmove", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent nativeEvent) {
        if (listener != null && inTouchSequence) {
          JsArray<com.google.gwt.dom.client.Touch> nativeTouches = nativeEvent.getChangedTouches();
          int nativeTouchesLen = nativeTouches.length();

          boolean[] preventDefault = {false};

          // Convert the JsArray<Native Touch> to an array of Touch.Events
          Event[] touches = new Event[nativeTouchesLen];
          for (int t = 0; t < nativeTouchesLen; t++) {
            com.google.gwt.dom.client.Touch touch = nativeTouches.get(t);
            float x = touch.getRelativeX(rootElement);
            float y = touch.getRelativeY(rootElement);
            int id = getTouchIdentifier(nativeEvent, t);
            touches[t] = new HtmlTouchEventImpl(PlayN.currentTime(), x, y, id, preventDefault);
          }
          listener.onTouchMove(touches);
          if (preventDefault[0]) {
            nativeEvent.preventDefault();
          }
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
    return evt.changedTouches[index].identifier || 0;
  }-*/;
}
