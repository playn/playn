/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.html;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;

import playn.core.Key;
import playn.core.Keyboard;
import playn.core.PlayN;
import playn.core.util.Callback;

class HtmlKeyboard implements Keyboard {

  private Listener listener;

  public void init() {
    // Key handlers.
    HtmlPlatform.captureEvent("keydown", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent nativeEvent) {
        if (listener != null) {
          Event.Impl event = new Event.Impl(
            PlayN.currentTime(), keyForCode(nativeEvent.getKeyCode()));
          listener.onKeyDown(event);
          if (event.getPreventDefault()) {
            nativeEvent.preventDefault();
          }
        }
      }
    });

    HtmlPlatform.captureEvent("keypress", new EventHandler() {
      public void handleEvent(NativeEvent nativeEvent) {
        if (listener != null) {
          TypedEvent.Impl event = new TypedEvent.Impl(
            PlayN.currentTime(), (char)nativeEvent.getCharCode());
          listener.onKeyTyped(event);
          if (event.getPreventDefault()) {
            nativeEvent.preventDefault();
          }
        }
      }
    });

    HtmlPlatform.captureEvent("keyup", new EventHandler() {
      @Override
      public void handleEvent(NativeEvent nativeEvent) {
        if (listener != null) {
          Event.Impl event = new Event.Impl(
            PlayN.currentTime(), keyForCode(nativeEvent.getKeyCode()));
          listener.onKeyUp(event);
          if (event.getPreventDefault()) {
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

  @Override
  public boolean hasHardwareKeyboard() {
    return true; // TODO: check whether we're on a mobile device or not
  }

  @Override
  public void getText(TextType textType, String label, String initVal, Callback<String> callback) {
    callback.onFailure(new UnsupportedOperationException("Not yet implemented."));
  }

  private static Key keyForCode(int keyCode) {
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
  private static final int KEY_SELECT_KEY = 93;
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
