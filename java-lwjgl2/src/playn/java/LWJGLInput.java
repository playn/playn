/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import javax.swing.JOptionPane;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import pythagoras.f.Point;
import react.RFuture;

import playn.core.Key;
import static playn.core.Keyboard.*; // to avoid clash with LWJGL Keyboard; meh
import static playn.core.Mouse.*;    // to avoid clash with LWJGL Mouse; double meh

public class LWJGLInput extends JavaInput {

  public LWJGLInput (LWJGLPlatform plat) {
    super(plat);
    try {
      Keyboard.create();
      Mouse.create();
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public RFuture<String> getText(TextType textType, String label, String initVal) {
    Object result = JOptionPane.showInputDialog(
      null, label, "", JOptionPane.QUESTION_MESSAGE, null, null, initVal);
    return RFuture.success((String)result);
  }

  @Override public RFuture<Boolean> sysDialog (String title, String text,
                                               String ok, String cancel) {
    int optType = JOptionPane.OK_CANCEL_OPTION;
    int msgType = cancel == null ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.QUESTION_MESSAGE;
    Object[] options = (cancel == null) ? new Object[] { ok } : new Object[] { ok, cancel };
    Object defOption = (cancel == null) ? ok : cancel;
    int result = JOptionPane.showOptionDialog(
      null, text, title, optType, msgType, null, options, defOption);
    return RFuture.success(result == 0);
  }

  @Override public boolean hasMouseLock () { return true; }
  @Override public boolean isMouseLocked() { return Mouse.isGrabbed(); }
  @Override public void setMouseLocked (boolean locked) { Mouse.setGrabbed(locked); }

  @Override void update () {
    super.update();

    // determine the current state of the modifier keys (note: the code assumes the current state
    // of the modifier keys is "correct" for all events that have arrived since the last call to
    // update; since that happens pretty frequently, 60fps, that's probably good enough)
    Keyboard.poll();
    int flags = modifierFlags(
      Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU),
      Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL),
      Keyboard.isKeyDown(Keyboard.KEY_LMETA) || Keyboard.isKeyDown(Keyboard.KEY_RMETA),
      Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));

    // process keyboard events
    while (Keyboard.next()) {
      double time = (double) (Keyboard.getEventNanoseconds() / 1000000);
      int keyCode = Keyboard.getEventKey();

      if (Keyboard.getEventKeyState()) {
        Key key = translateKey(keyCode);
        if (key != null) emitKeyPress(time, key, true, flags);
        char keyChar = Keyboard.getEventCharacter();
        if (!Character.isISOControl(keyChar)) emitKeyTyped(time, keyChar);
      } else {
        Key key = translateKey(keyCode);
        if (key != null) emitKeyPress(time, key, false, flags);
      }
    }

    // process mouse events
    while (Mouse.next()) {
      double time = (double) (Mouse.getEventNanoseconds() / 1000000);
      Point m = new Point(Mouse.getEventX(), Display.getHeight() - Mouse.getEventY() - 1);

      int btnIdx = Mouse.getEventButton();
      if (btnIdx >= 0) {
        ButtonEvent.Id btn = getButton(btnIdx);
        if (btn != null) emitMouseButton(time, m.x, m.y, btn, Mouse.getEventButtonState(), flags);
      }
      else {
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) emitMouseWheel(time, m.x, m.y, wheel > 0 ? -1 : 1, flags);
        else emitMouseMotion(time, m.x, m.y, Mouse.getEventDX(), -Mouse.getEventDY(), flags);
      }
    }
  }

  private Key translateKey(int keyCode) {
    switch (keyCode) {
      case Keyboard.KEY_ESCAPE       : return Key.ESCAPE;
      case Keyboard.KEY_1            : return Key.K1;
      case Keyboard.KEY_2            : return Key.K2;
      case Keyboard.KEY_3            : return Key.K3;
      case Keyboard.KEY_4            : return Key.K4;
      case Keyboard.KEY_5            : return Key.K5;
      case Keyboard.KEY_6            : return Key.K6;
      case Keyboard.KEY_7            : return Key.K7;
      case Keyboard.KEY_8            : return Key.K8;
      case Keyboard.KEY_9            : return Key.K9;
      case Keyboard.KEY_0            : return Key.K0;
      case Keyboard.KEY_MINUS        : return Key.MINUS;
      case Keyboard.KEY_EQUALS       : return Key.EQUALS;
      case Keyboard.KEY_BACK         : return Key.BACK;
      case Keyboard.KEY_TAB          : return Key.TAB;
      case Keyboard.KEY_Q            : return Key.Q;
      case Keyboard.KEY_W            : return Key.W;
      case Keyboard.KEY_E            : return Key.E;
      case Keyboard.KEY_R            : return Key.R;
      case Keyboard.KEY_T            : return Key.T;
      case Keyboard.KEY_Y            : return Key.Y;
      case Keyboard.KEY_U            : return Key.U;
      case Keyboard.KEY_I            : return Key.I;
      case Keyboard.KEY_O            : return Key.O;
      case Keyboard.KEY_P            : return Key.P;
      case Keyboard.KEY_LBRACKET     : return Key.LEFT_BRACKET;
      case Keyboard.KEY_RBRACKET     : return Key.RIGHT_BRACKET;
      case Keyboard.KEY_RETURN       : return Key.ENTER;
      case Keyboard.KEY_LCONTROL     : return Key.CONTROL;
      case Keyboard.KEY_A            : return Key.A;
      case Keyboard.KEY_S            : return Key.S;
      case Keyboard.KEY_D            : return Key.D;
      case Keyboard.KEY_F            : return Key.F;
      case Keyboard.KEY_G            : return Key.G;
      case Keyboard.KEY_H            : return Key.H;
      case Keyboard.KEY_J            : return Key.J;
      case Keyboard.KEY_K            : return Key.K;
      case Keyboard.KEY_L            : return Key.L;
      case Keyboard.KEY_SEMICOLON    : return Key.SEMICOLON;
      case Keyboard.KEY_APOSTROPHE   : return Key.QUOTE;
      case Keyboard.KEY_GRAVE        : return Key.BACKQUOTE;
      case Keyboard.KEY_LSHIFT       : return Key.SHIFT; // PlayN doesn't know left v. right
      case Keyboard.KEY_BACKSLASH    : return Key.BACKSLASH;
      case Keyboard.KEY_Z            : return Key.Z;
      case Keyboard.KEY_X            : return Key.X;
      case Keyboard.KEY_C            : return Key.C;
      case Keyboard.KEY_V            : return Key.V;
      case Keyboard.KEY_B            : return Key.B;
      case Keyboard.KEY_N            : return Key.N;
      case Keyboard.KEY_M            : return Key.M;
      case Keyboard.KEY_COMMA        : return Key.COMMA;
      case Keyboard.KEY_PERIOD       : return Key.PERIOD;
      case Keyboard.KEY_SLASH        : return Key.SLASH;
      case Keyboard.KEY_RSHIFT       : return Key.SHIFT; // PlayN doesn't know left v. right
      case Keyboard.KEY_MULTIPLY     : return Key.MULTIPLY;
      case Keyboard.KEY_LMENU        : return Key.ALT; // PlayN doesn't know left v. right
      case Keyboard.KEY_SPACE        : return Key.SPACE;
      case Keyboard.KEY_CAPITAL      : return Key.CAPS_LOCK;
      case Keyboard.KEY_F1           : return Key.F1;
      case Keyboard.KEY_F2           : return Key.F2;
      case Keyboard.KEY_F3           : return Key.F3;
      case Keyboard.KEY_F4           : return Key.F4;
      case Keyboard.KEY_F5           : return Key.F5;
      case Keyboard.KEY_F6           : return Key.F6;
      case Keyboard.KEY_F7           : return Key.F7;
      case Keyboard.KEY_F8           : return Key.F8;
      case Keyboard.KEY_F9           : return Key.F9;
      case Keyboard.KEY_F10          : return Key.F10;
      case Keyboard.KEY_NUMLOCK      : return Key.NP_NUM_LOCK;
      case Keyboard.KEY_SCROLL       : return Key.SCROLL_LOCK;
      case Keyboard.KEY_NUMPAD7      : return Key.NP7;
      case Keyboard.KEY_NUMPAD8      : return Key.NP8;
      case Keyboard.KEY_NUMPAD9      : return Key.NP9;
      case Keyboard.KEY_SUBTRACT     : return Key.NP_SUBTRACT;
      case Keyboard.KEY_NUMPAD4      : return Key.NP4;
      case Keyboard.KEY_NUMPAD5      : return Key.NP5;
      case Keyboard.KEY_NUMPAD6      : return Key.NP6;
      case Keyboard.KEY_ADD          : return Key.NP_ADD;
      case Keyboard.KEY_NUMPAD1      : return Key.NP1;
      case Keyboard.KEY_NUMPAD2      : return Key.NP2;
      case Keyboard.KEY_NUMPAD3      : return Key.NP3;
      case Keyboard.KEY_NUMPAD0      : return Key.NP0;
      case Keyboard.KEY_DECIMAL      : return Key.NP_DECIMAL;
      case Keyboard.KEY_F11          : return Key.F11;
      case Keyboard.KEY_F12          : return Key.F12;
      //case Keyboard.KEY_F13          : return Key.F13;
      //case Keyboard.KEY_F14          : return Key.F14;
      //case Keyboard.KEY_F15          : return Key.F15;
      //case Keyboard.KEY_F16          : return Key.F16;
      //case Keyboard.KEY_F17          : return Key.F17;
      //case Keyboard.KEY_F18          : return Key.F18;
      //case Keyboard.KEY_KANA         : return Key.
      //case Keyboard.KEY_F19          : return Key.F19;
      //case Keyboard.KEY_CONVERT      : return Key.
      //case Keyboard.KEY_NOCONVERT    : return Key.
      //case Keyboard.KEY_YEN          : return Key.
      //case Keyboard.KEY_NUMPADEQUALS : return Key.
      case Keyboard.KEY_CIRCUMFLEX   : return Key.CIRCUMFLEX;
      case Keyboard.KEY_AT           : return Key.AT;
      case Keyboard.KEY_COLON        : return Key.COLON;
      case Keyboard.KEY_UNDERLINE    : return Key.UNDERSCORE;
      //case Keyboard.KEY_KANJI        : return Key.
      //case Keyboard.KEY_STOP         : return Key.
      //case Keyboard.KEY_AX           : return Key.
      //case Keyboard.KEY_UNLABELED    : return Key.
      //case Keyboard.KEY_NUMPADENTER  : return Key.
      case Keyboard.KEY_RCONTROL     : return Key.CONTROL; // PlayN doesn't know left v. right
      //case Keyboard.KEY_SECTION      : return Key.
      //case Keyboard.KEY_NUMPADCOMMA  : return Key.
      //case Keyboard.KEY_DIVIDE       :
      case Keyboard.KEY_SYSRQ        : return Key.SYSRQ;
      case Keyboard.KEY_RMENU        : return Key.ALT; // PlayN doesn't know left v. right
      case Keyboard.KEY_FUNCTION     : return Key.FUNCTION;
      case Keyboard.KEY_PAUSE        : return Key.PAUSE;
      case Keyboard.KEY_HOME         : return Key.HOME;
      case Keyboard.KEY_UP           : return Key.UP;
      case Keyboard.KEY_PRIOR        : return Key.PAGE_UP;
      case Keyboard.KEY_LEFT         : return Key.LEFT;
      case Keyboard.KEY_RIGHT        : return Key.RIGHT;
      case Keyboard.KEY_END          : return Key.END;
      case Keyboard.KEY_DOWN         : return Key.DOWN;
      case Keyboard.KEY_NEXT         : return Key.PAGE_DOWN;
      case Keyboard.KEY_INSERT       : return Key.INSERT;
      case Keyboard.KEY_DELETE       : return Key.DELETE;
      case Keyboard.KEY_CLEAR        : return Key.CLEAR;
      case Keyboard.KEY_LMETA        : return Key.META; // PlayN doesn't know left v. right
      //case Keyboard.KEY_LWIN         : return Key.WINDOWS; // Duplicate with KEY_LMETA
      case Keyboard.KEY_RMETA        : return Key.META; // PlayN doesn't know left v. right
      //case Keyboard.KEY_RWIN         : return Key.WINDOWS; // Duplicate with KEY_RMETA
      //case Keyboard.KEY_APPS         : return Key.
      case Keyboard.KEY_POWER        : return Key.POWER;
      //case Keyboard.KEY_SLEEP        : return Key.
    }

    return null;
  }

  private static ButtonEvent.Id getButton(int lwjglButton) {
    switch (lwjglButton) {
    case 0:  return ButtonEvent.Id.LEFT;
    case 2:  return ButtonEvent.Id.MIDDLE;
    case 1:  return ButtonEvent.Id.RIGHT;
    default: return null;
    }
  }
}
