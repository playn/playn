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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;

import pythagoras.f.Point;
import react.RFuture;

import playn.core.*;

public class HtmlInput extends Input {

  private final HtmlPlatform plat;
  private final Element rootElement;

  private final Point lastMousePt = new Point();
  // true when we are in a drag sequence (after mouse down but before mouse up)
  private boolean inDragSequence = false;
  private boolean isRequestingMouseLock;

  // true when we are in a touch sequence (after touch start but before touch end)
  private boolean inTouchSequence = false;

  public HtmlInput (HtmlPlatform hplat, Element root) {
    super(hplat);
    this.plat = hplat;
    this.rootElement = root;

    // wire up keyboard handlers
    capturePageEvent("keydown", new EventHandler() {
      @Override public void handleEvent(NativeEvent nevent) {
        Key key = keyForCode(nevent.getKeyCode());
        dispatch(new Keyboard.KeyEvent(0, plat.time(), key, true), nevent);
      }
    });
    capturePageEvent("keypress", new EventHandler() {
      @Override public void handleEvent(NativeEvent nevent) {
        dispatch(new Keyboard.TypedEvent(0, plat.time(), (char)nevent.getCharCode()), nevent);
      }
    });
    capturePageEvent("keyup", new EventHandler() {
      @Override public void handleEvent(NativeEvent nevent) {
        Key key = keyForCode(nevent.getKeyCode());
        dispatch(new Keyboard.KeyEvent(0, plat.time(), key, false), nevent);
      }
    });

    // wire up mouse handlers
    abstract class XYEventHandler implements EventHandler {
      public void handleEvent(NativeEvent ev) {
        Point xy = plat.graphics().transformMouse(
          getRelativeX(ev, rootElement), getRelativeY(ev, rootElement));
        handleEvent(ev, xy.x, xy.y);
      }
      public abstract void handleEvent(NativeEvent ev, float x, float y);
    }

    abstract class MoveEventHandler extends XYEventHandler {
      private float lastX = -1, lastY = -1;

      @Override public void handleEvent(NativeEvent ev, float x, float y) {
        if (lastX == -1) {
          lastX = x;
          lastY = y;
        }
        if (inDragSequence == wantDragSequence()) {
          float dx;
          float dy;
          if (isMouseLocked()) {
            dx = getMovementX(ev);
            dy = getMovementY(ev);
          } else {
            dx = x - lastX;
            dy = y - lastY;
          }
          dispatch(new Mouse.MotionEvent(0, plat.time(), x, y, dx, dy), ev);
        }
        lastX = x;
        lastY = y;
        lastMousePt.set(x, y);
      }

      protected abstract boolean wantDragSequence();
    }

    // needed so the right mouse button becomes available
    addEventListener(Document.get(), "contextmenu", new EventHandler() {
      @Override public void handleEvent(NativeEvent evt) {
        evt.preventDefault();
        evt.stopPropagation();
      }
    }, false);

    // capture mouse down on the root element, only.
    captureEvent(rootElement, "mousedown", new XYEventHandler() {
      @Override public void handleEvent(NativeEvent ev, float x, float y) {
        inDragSequence = true;
        Mouse.ButtonEvent.Id btn = getMouseButton(ev);
        if (btn != null) dispatch(new Mouse.ButtonEvent(0, plat.time(), x, y, btn, true), ev);
      }
    });

    // capture mouse up anywhere on the page as long as we are in a drag sequence
    capturePageEvent("mouseup", new XYEventHandler() {
      @Override public void handleEvent(NativeEvent ev, float x, float y) {
        if (inDragSequence) {
          inDragSequence = false;
          Mouse.ButtonEvent.Id btn = getMouseButton(ev);
          if (btn != null) dispatch(new Mouse.ButtonEvent(0, plat.time(), x, y, btn, false), ev);
        }
        handleRequestsInUserEventContext();
      }
    });

    // capture mouse move anywhere on the page that fires only if we are in a drag sequence
    capturePageEvent("mousemove", new MoveEventHandler() {
      @Override protected boolean wantDragSequence() { return true; }
    });

    // capture mouse move on the root element that fires only if we are not in a drag sequence
    // (the page-level event listener will handle the firing when we are in a drag sequence)
    captureEvent(rootElement, "mousemove", new MoveEventHandler() {
      @Override protected boolean wantDragSequence() { return false; }
    });

    captureEvent(rootElement, getMouseWheelEvent(), new EventHandler() {
      @Override public void handleEvent(NativeEvent ev) {
        float vel = getMouseWheelVelocity(ev);
        dispatch(new Mouse.WheelEvent(0, plat.time(), lastMousePt.x, lastMousePt.y, vel), ev);
      }
    });

    // capture touch start on the root element, only.
    captureEvent(rootElement, "touchstart", new EventHandler() {
      @Override public void handleEvent(NativeEvent nevent) {
        inTouchSequence = true;
        dispatch(toTouchEvents(Touch.Event.Kind.START, nevent), nevent);
      }
    });

    // capture touch move anywhere on the page as long as we are in a touch sequence
    capturePageEvent("touchmove", new EventHandler() {
      @Override public void handleEvent(NativeEvent nevent) {
        if (inTouchSequence) dispatch(toTouchEvents(Touch.Event.Kind.MOVE, nevent), nevent);
      }
    });

    // capture touch end anywhere on the page as long as we are in a touch sequence
    capturePageEvent("touchend", new EventHandler() {
      @Override public void handleEvent(NativeEvent nevent) {
        if (inTouchSequence) {
          dispatch(toTouchEvents(Touch.Event.Kind.END, nevent), nevent);
          // if there are no remaining active touches, note that this touch sequence has ended
          if (nevent.getTouches().length() == 0) inTouchSequence = false;
        }
      }
    });
  }

  @Override public boolean hasHardwareKeyboard() {
    return true; // TODO: check whether we're on a mobile device or not
  }

  @Override public native boolean hasTouch() /*-{
    return ('ontouchstart' in $doc.documentElement) ||
      ($wnd.navigator.userAgent.match(/ipad|iphone|android/i) != null);
  }-*/;

  @Override public native boolean hasMouse() /*-{
    return ('onmousedown' in $doc.documentElement) &&
      ($wnd.navigator.userAgent.match(/ipad|iphone|android/i) == null);
  }-*/;

  @Override public native boolean hasMouseLock() /*-{
    return !!($doc.body.requestPointerLock || $doc.body.webkitRequestPointerLock ||
              $doc.body.mozRequestPointerLock);
  }-*/;

  @Override public RFuture<String> getText(Keyboard.TextType textType, String label,
                                           String initVal) {
    String result = Window.prompt(label, initVal);
    emitFakeMouseUp();
    return RFuture.success(result);
  }

  @Override public RFuture<Boolean> sysDialog(String title, String message,
                                              String ok, String cancel) {
    boolean result;
    if (cancel != null) result = Window.confirm(message);
    else {
      Window.alert(message);
      result = true;
    }
    emitFakeMouseUp();
    return RFuture.success(result);
  }

  // HACK HACK HACK HACK!
  // Chrome and Firefox on Mac OS (at least) fail to deliver the MOUSE UP event that should be
  // delivered after a system dialog completes, assuming the dialog was triggered on MOUSE DOWN; so
  // we emit a fake mouse up event here just to avoid causing the Pointer system to fail to
  // terminate the current pointer interaction if it happens to have triggered a system dialog; if
  // the dialog was not shown on mouse down, a spurious mouse up is not likely to do much damage; a
  // better devil for sure than failing to deliver a needed mouse up
  private void emitFakeMouseUp () {
    mouseEvents.emit(new Mouse.ButtonEvent(0, plat.time(), 0, 0, Mouse.ButtonEvent.Id.LEFT, false));
  }

  @Override public native boolean isMouseLocked() /*-{
    return !!($doc.pointerLockElement || $doc.webkitPointerLockElement ||
              $doc.mozPointerLockElement);
  }-*/;

  @Override public void setMouseLocked(boolean locked) {
    if (locked) {
      if (hasMouseLock()) {
        isRequestingMouseLock = true;
        plat.log().debug("Requesting mouse lock (supported)");
      } else {
        plat.log().debug("Requesting mouse lock -- but unsupported");
      }
    } else {
      plat.log().debug("Requesting mouse unlock");
      if (hasMouseLock()) {
        isRequestingMouseLock = false;
        unlockImpl();
      }
    }
  }

  static class EventCloseHandler implements HandlerRegistration {
    private final JavaScriptObject target;
    private final String name;
    private final boolean capture;
    private JavaScriptObject listener;

    EventCloseHandler (JavaScriptObject target, String name,
                       EventHandler eventHandler, boolean capture) {
      this.target = target;
      this.name = name;
      this.capture = capture;
      addEventListener(this, target, name, eventHandler, capture);
    }

    void setListener (JavaScriptObject listener) {
      this.listener = listener;
    }

    @Override public void removeHandler () {
      removeEventListener(target, name, listener, capture);
    }

    private native void addEventListener (EventCloseHandler closeHandler,
                                          JavaScriptObject target, String name,
                                          EventHandler handler, boolean capture) /*-{
      var listener = function(e) {
        handler.@playn.html.EventHandler::handleEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);
      };
      target.addEventListener(name, listener, capture);
      closeHandler.@playn.html.HtmlInput.EventCloseHandler::setListener(Lcom/google/gwt/core/client/JavaScriptObject;)(listener);
    }-*/;

    private native void removeEventListener (JavaScriptObject target, String name,
                                             JavaScriptObject listener, boolean capture)/*-{
      target.removeEventListener(name, listener, capture);
    }-*/;
  }

  static HandlerRegistration addEventListener (JavaScriptObject target, String name,
                                               EventHandler handler, boolean capture) {
    return new EventCloseHandler(target, name, handler, capture);
  };

  /** Capture events that occur anywhere on the page. Event values will be relative to the page
    * (not the rootElement) {@see #getRelativeX(NativeEvent, Element)} and
    * {@see #getRelativeY(NativeEvent, Element)}. */
  static HandlerRegistration capturePageEvent (String name, EventHandler handler) {
    return addEventListener(Document.get(), name, handler, true);
  }

  static HandlerRegistration captureEvent (Element target, String name, EventHandler handler) {
    return addEventListener(target, name, handler, true);
  }

  /**
   * Gets the event's x-position relative to a given element.
   *
   * @param e native event
   * @param target the element whose coordinate system is to be used
   * @return the relative x-position
   */
  static float getRelativeX (NativeEvent e, Element target) {
    return (e.getClientX() - target.getAbsoluteLeft() + target.getScrollLeft() +
            target.getOwnerDocument().getScrollLeft()) / HtmlGraphics.experimentalScale;
  }

  /**
   * Gets the event's y-position relative to a given element.
   *
   * @param e native event
   * @param target the element whose coordinate system is to be used
   * @return the relative y-position
   */
  static float getRelativeY (NativeEvent e, Element target) {
    return (e.getClientY() - target.getAbsoluteTop() + target.getScrollTop() +
            target.getOwnerDocument().getScrollTop()) / HtmlGraphics.experimentalScale;
  }

  void handleRequestsInUserEventContext () {
    // hack to allow requesting mouse lock from non-mouse/key handler event
    if (isRequestingMouseLock && !isMouseLocked()) {
      requestMouseLockImpl(rootElement);
    }
  }

  private int mods (NativeEvent event) {
    return modifierFlags(event.getAltKey(), event.getCtrlKey(), event.getMetaKey(),
                         event.getShiftKey());
  }

  private void dispatch (Keyboard.Event event, NativeEvent nevent) {
    try {
      event.setFlag(mods(nevent));
      plat.dispatchEvent(keyboardEvents, event);
    } finally {
      if (event.isSet(Event.F_PREVENT_DEFAULT)) nevent.preventDefault();
    }
  }

  private void dispatch (Mouse.Event event, NativeEvent nevent) {
    try {
      event.setFlag(mods(nevent));
      plat.dispatchEvent(mouseEvents, event);
    } finally {
      if (event.isSet(Event.F_PREVENT_DEFAULT)) nevent.preventDefault();
    }
  }

  private void dispatch (Touch.Event[] events, NativeEvent nevent) {
    try {
      plat.dispatchEvent(touchEvents, events);
    } finally {
      // TODO: is there a better alternative to being so extravagant? I don't want to go back to
      // having all touch events share a mutable Flags instance
      for (Touch.Event event : events) {
        if (event.isSet(Event.F_PREVENT_DEFAULT)) nevent.preventDefault();
      }
    }
  }

  private native int getMovementX (NativeEvent nevent) /*-{
    return nevent.webkitMovementX;
  }-*/;

  private native int getMovementY (NativeEvent nevent) /*-{
      return nevent.webkitMovementY;
  }-*/;

  native void requestMouseLockImpl (Element element) /*-{
     element.requestPointerLock = (element.requestPointerLock || element.webkitRequestPointerLock ||
                                   element.mozRequestPointerLock);
    if (element.requestPointerLock) element.requestPointerLock();
  }-*/;

  /**
   * Return the mouse wheel velocity for the event
   */
  private static native float getMouseWheelVelocity (NativeEvent evt) /*-{
    var delta = 0.0;
    var agentInfo = @playn.html.HtmlPlatform::agentInfo;

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
    } else if (agentInfo.isChrome || agentInfo.isSafari || agentInfo.isIE) {
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
  protected static native String getMouseWheelEvent () /*-{
    if (navigator.userAgent.toLowerCase().indexOf('firefox') != -1) {
      return "DOMMouseScroll";
    } else {
      return "mousewheel";
    }
  }-*/;

  protected static Mouse.ButtonEvent.Id getMouseButton (NativeEvent evt) {
    switch (evt.getButton()) {
    case (NativeEvent.BUTTON_LEFT):   return Mouse.ButtonEvent.Id.LEFT;
    case (NativeEvent.BUTTON_MIDDLE): return Mouse.ButtonEvent.Id.MIDDLE;
    case (NativeEvent.BUTTON_RIGHT):  return Mouse.ButtonEvent.Id.RIGHT;
    default:                          return null;
    }
  }

  private native void unlockImpl () /*-{
    $doc.exitPointerLock = $doc.exitPointerLock || $doc.webkitExitPointerLock || $doc.mozExitPointerLock;
    $doc.exitPointerLock && $doc.exitPointerLock();
  }-*/;

  private Touch.Event[] toTouchEvents (Touch.Event.Kind kind, NativeEvent nevent) {
    // Convert the JsArray<Native Touch> to an array of Touch.Events
    JsArray<com.google.gwt.dom.client.Touch> nativeTouches = nevent.getChangedTouches();
    int nativeTouchesLen = nativeTouches.length();
    Touch.Event[] touches = new Touch.Event[nativeTouchesLen];
    double time = plat.time();
    for (int t = 0; t < nativeTouchesLen; t++) {
      com.google.gwt.dom.client.Touch touch = nativeTouches.get(t);
      float x = touch.getRelativeX(rootElement);
      float y = touch.getRelativeY(rootElement);
      Point xy = plat.graphics().transformMouse(x, y);
      int id = getTouchIdentifier(nevent, t);
      touches[t] = new Touch.Event(0, time, xy.x, xy.y, kind, id);
    }
    return touches;
  }

  /** Returns the unique identifier of a touch, or 0. */
  private static native int getTouchIdentifier (NativeEvent evt, int index) /*-{
    return evt.changedTouches[index].identifier || 0;
  }-*/;

  private static Key keyForCode (int keyCode) {
    switch (keyCode) {
    case KeyCodes.KEY_ALT: return Key.ALT;
    case KeyCodes.KEY_BACKSPACE: return Key.BACKSPACE;
    case KeyCodes.KEY_CTRL: return Key.CONTROL;
    case KeyCodes.KEY_DELETE: return Key.DELETE;
    case KeyCodes.KEY_DOWN: return Key.DOWN;
    case KeyCodes.KEY_END: return Key.END;
    case KeyCodes.KEY_ENTER: return Key.ENTER;
    case KeyCodes.KEY_ESCAPE: return Key.ESCAPE;
    case KeyCodes.KEY_HOME: return Key.HOME;
    case KeyCodes.KEY_LEFT: return Key.LEFT;
    case KeyCodes.KEY_PAGEDOWN: return Key.PAGE_DOWN;
    case KeyCodes.KEY_PAGEUP: return Key.PAGE_UP;
    case KeyCodes.KEY_RIGHT: return Key.RIGHT;
    case KeyCodes.KEY_SHIFT: return Key.SHIFT;
    case KeyCodes.KEY_TAB: return Key.TAB;
    case KeyCodes.KEY_UP: return Key.UP;

    case KEY_PAUSE: return Key.PAUSE;
    case KEY_CAPS_LOCK: return Key.CAPS_LOCK;
    case KEY_SPACE: return Key.SPACE;
    case KEY_INSERT: return Key.INSERT;
    case KEY_0: return Key.K0;
    case KEY_1: return Key.K1;
    case KEY_2: return Key.K2;
    case KEY_3: return Key.K3;
    case KEY_4: return Key.K4;
    case KEY_5: return Key.K5;
    case KEY_6: return Key.K6;
    case KEY_7: return Key.K7;
    case KEY_8: return Key.K8;
    case KEY_9: return Key.K9;
    case KEY_A: return Key.A;
    case KEY_B: return Key.B;
    case KEY_C: return Key.C;
    case KEY_D: return Key.D;
    case KEY_E: return Key.E;
    case KEY_F: return Key.F;
    case KEY_G: return Key.G;
    case KEY_H: return Key.H;
    case KEY_I: return Key.I;
    case KEY_J: return Key.J;
    case KEY_K: return Key.K;
    case KEY_L: return Key.L;
    case KEY_M: return Key.M;
    case KEY_N: return Key.N;
    case KEY_O: return Key.O;
    case KEY_P: return Key.P;
    case KEY_Q: return Key.Q;
    case KEY_R: return Key.R;
    case KEY_S: return Key.S;
    case KEY_T: return Key.T;
    case KEY_U: return Key.U;
    case KEY_V: return Key.V;
    case KEY_W: return Key.W;
    case KEY_X: return Key.X;
    case KEY_Y: return Key.Y;
    case KEY_Z: return Key.Z;
    case KEY_LEFT_WINDOW_KEY: return Key.WINDOWS;
    case KEY_RIGHT_WINDOW_KEY: return Key.WINDOWS;
    // case KEY_SELECT_KEY: return Key.SELECT_KEY;
    case KEY_NUMPAD0: return Key.NP0;
    case KEY_NUMPAD1: return Key.NP1;
    case KEY_NUMPAD2: return Key.NP2;
    case KEY_NUMPAD3: return Key.NP3;
    case KEY_NUMPAD4: return Key.NP4;
    case KEY_NUMPAD5: return Key.NP5;
    case KEY_NUMPAD6: return Key.NP6;
    case KEY_NUMPAD7: return Key.NP7;
    case KEY_NUMPAD8: return Key.NP8;
    case KEY_NUMPAD9: return Key.NP9;
    case KEY_MULTIPLY: return Key.NP_MULTIPLY;
    case KEY_ADD: return Key.NP_ADD;
    case KEY_SUBTRACT: return Key.NP_SUBTRACT;
    case KEY_DECIMAL_POINT_KEY: return Key.NP_DECIMAL;
    case KEY_DIVIDE: return Key.NP_DIVIDE;
    case KEY_F1: return Key.F1;
    case KEY_F2: return Key.F2;
    case KEY_F3: return Key.F3;
    case KEY_F4: return Key.F4;
    case KEY_F5: return Key.F5;
    case KEY_F6: return Key.F6;
    case KEY_F7: return Key.F7;
    case KEY_F8: return Key.F8;
    case KEY_F9: return Key.F9;
    case KEY_F10: return Key.F10;
    case KEY_F11: return Key.F11;
    case KEY_F12: return Key.F12;
    case KEY_NUM_LOCK: return Key.NP_NUM_LOCK;
    case KEY_SCROLL_LOCK: return Key.SCROLL_LOCK;
    case KEY_SEMICOLON: return Key.SEMICOLON;
    case KEY_EQUALS: return Key.EQUALS;
    case KEY_COMMA: return Key.COMMA;
    case KEY_DASH: return Key.MINUS;
    case KEY_PERIOD: return Key.PERIOD;
    case KEY_FORWARD_SLASH: return Key.SLASH;
    case KEY_GRAVE_ACCENT: return Key.BACKQUOTE;
    case KEY_OPEN_BRACKET: return Key.LEFT_BRACKET;
    case KEY_BACKSLASH: return Key.BACKSLASH;
    case KEY_CLOSE_BRACKET: return Key.RIGHT_BRACKET;
    case KEY_SINGLE_QUOTE: return Key.QUOTE;
    default: return Key.UNKNOWN;
    }
  }

  // these are absent from KeyCodes; we know not why...
  private static final int KEY_PAUSE = 19;
  private static final int KEY_CAPS_LOCK = 20;
  private static final int KEY_SPACE = 32;
  private static final int KEY_INSERT = 45;
  private static final int KEY_0 = 48;
  private static final int KEY_1 = 49;
  private static final int KEY_2 = 50;
  private static final int KEY_3 = 51;
  private static final int KEY_4 = 52;
  private static final int KEY_5 = 53;
  private static final int KEY_6 = 54;
  private static final int KEY_7 = 55;
  private static final int KEY_8 = 56;
  private static final int KEY_9 = 57;
  private static final int KEY_A = 65;
  private static final int KEY_B = 66;
  private static final int KEY_C = 67;
  private static final int KEY_D = 68;
  private static final int KEY_E = 69;
  private static final int KEY_F = 70;
  private static final int KEY_G = 71;
  private static final int KEY_H = 72;
  private static final int KEY_I = 73;
  private static final int KEY_J = 74;
  private static final int KEY_K = 75;
  private static final int KEY_L = 76;
  private static final int KEY_M = 77;
  private static final int KEY_N = 78;
  private static final int KEY_O = 79;
  private static final int KEY_P = 80;
  private static final int KEY_Q = 81;
  private static final int KEY_R = 82;
  private static final int KEY_S = 83;
  private static final int KEY_T = 84;
  private static final int KEY_U = 85;
  private static final int KEY_V = 86;
  private static final int KEY_W = 87;
  private static final int KEY_X = 88;
  private static final int KEY_Y = 89;
  private static final int KEY_Z = 90;
  private static final int KEY_LEFT_WINDOW_KEY = 91;
  private static final int KEY_RIGHT_WINDOW_KEY = 92;
  // private static final int KEY_SELECT_KEY = 93;
  private static final int KEY_NUMPAD0 = 96;
  private static final int KEY_NUMPAD1 = 97;
  private static final int KEY_NUMPAD2 = 98;
  private static final int KEY_NUMPAD3 = 99;
  private static final int KEY_NUMPAD4 = 100;
  private static final int KEY_NUMPAD5 = 101;
  private static final int KEY_NUMPAD6 = 102;
  private static final int KEY_NUMPAD7 = 103;
  private static final int KEY_NUMPAD8 = 104;
  private static final int KEY_NUMPAD9 = 105;
  private static final int KEY_MULTIPLY = 106;
  private static final int KEY_ADD = 107;
  private static final int KEY_SUBTRACT = 109;
  private static final int KEY_DECIMAL_POINT_KEY = 110;
  private static final int KEY_DIVIDE = 111;
  private static final int KEY_F1 = 112;
  private static final int KEY_F2 = 113;
  private static final int KEY_F3 = 114;
  private static final int KEY_F4 = 115;
  private static final int KEY_F5 = 116;
  private static final int KEY_F6 = 117;
  private static final int KEY_F7 = 118;
  private static final int KEY_F8 = 119;
  private static final int KEY_F9 = 120;
  private static final int KEY_F10 = 121;
  private static final int KEY_F11 = 122;
  private static final int KEY_F12 = 123;
  private static final int KEY_NUM_LOCK = 144;
  private static final int KEY_SCROLL_LOCK = 145;
  private static final int KEY_SEMICOLON = 186;
  private static final int KEY_EQUALS = 187;
  private static final int KEY_COMMA = 188;
  private static final int KEY_DASH = 189;
  private static final int KEY_PERIOD = 190;
  private static final int KEY_FORWARD_SLASH = 191;
  private static final int KEY_GRAVE_ACCENT = 192;
  private static final int KEY_OPEN_BRACKET = 219;
  private static final int KEY_BACKSLASH = 220;
  private static final int KEY_CLOSE_BRACKET = 221;
  private static final int KEY_SINGLE_QUOTE = 222;
}
