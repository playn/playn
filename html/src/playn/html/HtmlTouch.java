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

import pythagoras.f.Point;

import playn.core.Events;
import playn.core.PlayN;
import playn.core.TouchImpl;

class HtmlTouch extends TouchImpl {

  private final HtmlPlatform platform;
  private final Element rootElement;
  // true when we are in a touch sequence (after touch start but before touch end)
  private boolean inTouchSequence = false;

  HtmlTouch(HtmlPlatform platform, Element rootElement) {
    this.platform = platform;
    this.rootElement = rootElement;

    // capture touch start on the root element, only.
    HtmlInput.captureEvent(rootElement, "touchstart", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent nativeEvent) {
        inTouchSequence = true;
        Events.Flags flags = new Events.Flags.Impl();
        onTouchStart(toEvents(nativeEvent, flags));
        if (flags.getPreventDefault())
          nativeEvent.preventDefault();
      }
    });

    // capture touch move anywhere on the page as long as we are in a touch sequence
    HtmlInput.capturePageEvent("touchmove", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent nativeEvent) {
        if (inTouchSequence) {
          Events.Flags flags = new Events.Flags.Impl();
          onTouchMove(toEvents(nativeEvent, flags));
          if (flags.getPreventDefault())
            nativeEvent.preventDefault();
        }
      }
    });

    // capture touch end anywhere on the page as long as we are in a touch sequence
    HtmlInput.capturePageEvent("touchend", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent nativeEvent) {
        if (inTouchSequence) {
          Events.Flags flags = new Events.Flags.Impl();
          onTouchEnd(toEvents(nativeEvent, flags));
          if (flags.getPreventDefault())
            nativeEvent.preventDefault();

          // if there are no remaining active touches, note that this touch sequence has ended
          if (nativeEvent.getTouches().length() == 0)
            inTouchSequence = false;
        }
      }
    });
  }

  @Override
  public native boolean hasTouch() /*-{
    return ('ontouchstart' in $doc.documentElement) ||
      ($wnd.navigator.userAgent.match(/ipad|iphone|android/i) != null);
  }-*/;

  private Event.Impl[] toEvents(NativeEvent nativeEvent, Events.Flags flags) {
    // Convert the JsArray<Native Touch> to an array of Touch.Events
    JsArray<com.google.gwt.dom.client.Touch> nativeTouches = nativeEvent.getChangedTouches();
    int nativeTouchesLen = nativeTouches.length();
    Event.Impl[] touches = new Event.Impl[nativeTouchesLen];
    for (int t = 0; t < nativeTouchesLen; t++) {
      com.google.gwt.dom.client.Touch touch = nativeTouches.get(t);
      float x = touch.getRelativeX(rootElement);
      float y = touch.getRelativeY(rootElement);
      Point xy = platform.graphics().transformMouse(x, y);
      int id = getTouchIdentifier(nativeEvent, t);
      touches[t] = new Event.Impl(flags, PlayN.currentTime(), xy.x, xy.y, id);
    }
    return touches;
  }

  /** Returns the unique identifier of a touch, or 0. */
  private static native int getTouchIdentifier(NativeEvent evt, int index) /*-{
    return evt.changedTouches[index].identifier || 0;
  }-*/;
}
