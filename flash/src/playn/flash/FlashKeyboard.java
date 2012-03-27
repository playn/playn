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
package playn.flash;

import flash.events.KeyboardEvent;
import flash.display.InteractiveObject;
import flash.ui.Keyboard;

import playn.core.Key;
import playn.core.PlayN;
import playn.core.util.Callback;

class FlashKeyboard implements playn.core.Keyboard {

  private Listener listener;

  FlashKeyboard() {
    // Key handlers.
    FlashPlatform.captureEvent(InteractiveObject.KEYDOWN, new EventHandler<KeyboardEvent>() {
      @Override
      public void handleEvent(KeyboardEvent nativeEvent) {
        if (listener != null) {
          Event.Impl event = new Event.Impl(PlayN.currentTime(), keyForCode(nativeEvent.keyCode()));
          listener.onKeyDown(event);
          if (event.getPreventDefault()) {
            nativeEvent.preventDefault();
          }

          int charCode = nativeEvent.charCode();
          if (charCode != 0) {
            TypedEvent.Impl typedEvent = new TypedEvent.Impl(PlayN.currentTime(), (char)charCode);
            listener.onKeyTyped(typedEvent);
            if (typedEvent.getPreventDefault()) {
              nativeEvent.preventDefault();
            }
          }
        }
      }
    });

    FlashPlatform.captureEvent(InteractiveObject.KEYUP, new EventHandler<KeyboardEvent>() {
      @Override
      public void handleEvent(KeyboardEvent nativeEvent) {
        if (listener != null) {
          Event.Impl event = new Event.Impl(PlayN.currentTime(), keyForCode(nativeEvent.keyCode()));
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
    case Keyboard.A: return Key.A;
    case Keyboard.ALTERNATE: return Key.ALT;
    // case Keyboard.AUDIO: return Key.AUDIO;
    case Keyboard.B: return Key.B;
    case Keyboard.BACK: return Key.BACK;
    case Keyboard.BACKQUOTE: return Key.BACKQUOTE;
    case Keyboard.BACKSLASH: return Key.BACKSLASH;
    case Keyboard.BACKSPACE: return Key.BACKSPACE;
    case Keyboard.BLUE: return Key.BLUE;
    case Keyboard.C: return Key.C;
    case Keyboard.CAPS_LOCK: return Key.CAPS_LOCK;
    case Keyboard.CHANNEL_DOWN: return Key.CHANNEL_DOWN;
    case Keyboard.CHANNEL_UP: return Key.CHANNEL_UP;
    case Keyboard.COMMA: return Key.COMMA;
    case Keyboard.COMMAND: return Key.META;
    case Keyboard.CONTROL: return Key.CONTROL;
    case Keyboard.D: return Key.D;
    case Keyboard.DELETE: return Key.DELETE;
    case Keyboard.DOWN: return Key.DOWN;
    case Keyboard.DVR: return Key.DVR;
    case Keyboard.E: return Key.E;
    case Keyboard.END: return Key.END;
    case Keyboard.ENTER: return Key.ENTER;
    case Keyboard.EQUAL: return Key.EQUALS;
    case Keyboard.ESCAPE: return Key.ESCAPE;
    case Keyboard.EXIT: return Key.BACK;
    case Keyboard.F: return Key.F;
    case Keyboard.F1: return Key.F1;
    case Keyboard.F2: return Key.F2;
    case Keyboard.F3: return Key.F3;
    case Keyboard.F4: return Key.F4;
    case Keyboard.F5: return Key.F5;
    case Keyboard.F6: return Key.F6;
    case Keyboard.F7: return Key.F7;
    case Keyboard.F8: return Key.F8;
    case Keyboard.F9: return Key.F9;
    case Keyboard.F10: return Key.F10;
    case Keyboard.F11: return Key.F11;
    case Keyboard.F12: return Key.F12;
    // case Keyboard.F13: return Key.F13;
    // case Keyboard.F14: return Key.F14;
    // case Keyboard.F15: return Key.F15;
    case Keyboard.FAST_FORWARD: return Key.MEDIA_FAST_FORWARD;
    case Keyboard.G: return Key.G;
    case Keyboard.GREEN: return Key.GREEN;
    case Keyboard.GUIDE: return Key.GUIDE;
    case Keyboard.H: return Key.H;
    // case Keyboard.HELP: return Key.HELP;
    case Keyboard.HOME: return Key.HOME;
    case Keyboard.I: return Key.I;
    case Keyboard.INFO: return Key.INFO;
    case Keyboard.INPUT: return Key.AVR_INPUT; // ?
    case Keyboard.INSERT: return Key.INSERT;
    case Keyboard.J: return Key.J;
    case Keyboard.K: return Key.K;
    case Keyboard.L: return Key.L;
    // case Keyboard.LAST: return Key.LAST;
    case Keyboard.LEFT: return Key.LEFT;
    case Keyboard.LEFTBRACKET: return Key.LEFT_BRACKET;
    // case Keyboard.LIVE: return Key.LIVE;
    case Keyboard.M: return Key.M;
    case Keyboard.MASTER_SHELL: return Key.MENU;
    case Keyboard.MENU: return Key.MENU;
    case Keyboard.MINUS: return Key.MINUS;
    case Keyboard.N: return Key.N;
    case Keyboard.NEXT: return Key.MEDIA_NEXT;
    case Keyboard.NUMBER_0: return Key.K0;
    case Keyboard.NUMBER_1: return Key.K1;
    case Keyboard.NUMBER_2: return Key.K2;
    case Keyboard.NUMBER_3: return Key.K3;
    case Keyboard.NUMBER_4: return Key.K4;
    case Keyboard.NUMBER_5: return Key.K5;
    case Keyboard.NUMBER_6: return Key.K6;
    case Keyboard.NUMBER_7: return Key.K7;
    case Keyboard.NUMBER_8: return Key.K8;
    case Keyboard.NUMBER_9: return Key.K9;
    case Keyboard.NUMPAD: return Key.NP_NUM_LOCK;
    case Keyboard.NUMPAD_0: return Key.NP0;
    case Keyboard.NUMPAD_1: return Key.NP1;
    case Keyboard.NUMPAD_2: return Key.NP2;
    case Keyboard.NUMPAD_3: return Key.NP3;
    case Keyboard.NUMPAD_4: return Key.NP4;
    case Keyboard.NUMPAD_5: return Key.NP5;
    case Keyboard.NUMPAD_6: return Key.NP6;
    case Keyboard.NUMPAD_7: return Key.NP7;
    case Keyboard.NUMPAD_8: return Key.NP8;
    case Keyboard.NUMPAD_9: return Key.NP9;
    case Keyboard.NUMPAD_ADD: return Key.NP_ADD;
    case Keyboard.NUMPAD_DECIMAL: return Key.NP_DECIMAL;
    case Keyboard.NUMPAD_DIVIDE: return Key.NP_DIVIDE;
    case Keyboard.NUMPAD_ENTER: return Key.ENTER;
    case Keyboard.NUMPAD_MULTIPLY: return Key.NP_MULTIPLY;
    case Keyboard.NUMPAD_SUBTRACT: return Key.NP_SUBTRACT;
    case Keyboard.O: return Key.O;
    case Keyboard.P: return Key.P;
    case Keyboard.PAGE_DOWN: return Key.PAGE_DOWN;
    case Keyboard.PAGE_UP: return Key.PAGE_UP;
    case Keyboard.PAUSE: return Key.PAUSE;
    case Keyboard.PERIOD: return Key.PERIOD;
    case Keyboard.PLAY: return Key.MEDIA_PLAY;
    case Keyboard.PREVIOUS: return Key.MEDIA_PREVIOUS;
    case Keyboard.Q: return Key.Q;
    case Keyboard.QUOTE: return Key.QUOTE;
    case Keyboard.R: return Key.R;
    case Keyboard.RECORD: return Key.MEDIA_RECORD;
    case Keyboard.RED: return Key.RED;
    case Keyboard.REWIND: return Key.MEDIA_REWIND;
    case Keyboard.RIGHT: return Key.RIGHT;
    case Keyboard.RIGHTBRACKET: return Key.RIGHT_BRACKET;
    case Keyboard.S: return Key.S;
    case Keyboard.SEARCH: return Key.SEARCH;
    case Keyboard.SEMICOLON: return Key.SEMICOLON;
    case Keyboard.SETUP: return Key.MENU;
    case Keyboard.SHIFT: return Key.SHIFT;
    case Keyboard.SKIP_BACKWARD: return Key.MEDIA_PREVIOUS;
    case Keyboard.SKIP_FORWARD: return Key.MEDIA_NEXT;
    case Keyboard.SLASH: return Key.SLASH;
    case Keyboard.SPACE: return Key.SPACE;
    case Keyboard.STOP: return Key.MEDIA_STOP;
    // case Keyboard.SUBTITLE: return Key.SUBTITLE;
    case Keyboard.T: return Key.T;
    case Keyboard.TAB: return Key.TAB;
    case Keyboard.U: return Key.U;
    case Keyboard.UP: return Key.UP;
    case Keyboard.V: return Key.V;
    // case Keyboard.VOD: return Key.VOD;
    case Keyboard.W: return Key.W;
    case Keyboard.X: return Key.X;
    case Keyboard.Y: return Key.Y;
    case Keyboard.YELLOW: return Key.YELLOW;
    case Keyboard.Z: return Key.Z;
    default: return Key.UNKNOWN;
    }
  }
}
