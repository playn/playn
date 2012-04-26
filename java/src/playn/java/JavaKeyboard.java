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
package playn.java;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;

import playn.core.Key;
import playn.core.util.Callback;

class JavaKeyboard implements playn.core.Keyboard {

  private Listener listener;
  private JFrame frame;

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  @Override
  public boolean hasHardwareKeyboard() {
    return true;
  }

  @Override
  public void getText(TextType textType, String label, String initVal, Callback<String> callback) {
    Object result = JOptionPane.showInputDialog(
      frame, label, "", JOptionPane.QUESTION_MESSAGE, null, null, initVal);
    callback.onSuccess((String) result);
  }

  void init() throws LWJGLException {
    Keyboard.create();
  }

  void update() {
    while (Keyboard.next()) {
      if (listener == null) {
        continue;
      }

      double time = (double) (Keyboard.getEventNanoseconds() / 1000);
      int keyCode = Keyboard.getEventKey();

      if (Keyboard.getEventKeyState()) {
        Key key = translateKey(keyCode);
        if (key != null)
          listener.onKeyDown(new playn.core.Keyboard.Event.Impl(time, key));
        char keyChar = Keyboard.getEventCharacter();
        if (!Character.isISOControl(keyChar))
          listener.onKeyTyped(new playn.core.Keyboard.TypedEvent.Impl(time, keyChar));
      } else {
        Key key = translateKey(keyCode);
        if (key != null)
          listener.onKeyUp(new playn.core.Keyboard.Event.Impl(time, key));
      }
    }
  }

  private Key translateKey(int keyCode) {
    // TODO(jgw): Confirm that these mappings are correct.
    switch (keyCode) {
      case 0x01 : return Key.ESCAPE          ;
      case 0x02 : return Key.K1              ;
      case 0x03 : return Key.K2              ;
      case 0x04 : return Key.K3              ;
      case 0x05 : return Key.K4              ;
      case 0x06 : return Key.K5              ;
      case 0x07 : return Key.K6              ;
      case 0x08 : return Key.K7              ;
      case 0x09 : return Key.K8              ;
      case 0x0A : return Key.K9              ;
      case 0x0B : return Key.K0              ;
      case 0x0C : return Key.MINUS           ;
      case 0x0D : return Key.EQUALS          ;
      case 0x0E : return Key.BACKSPACE       ;
      case 0x0F : return Key.TAB             ;
      case 0x10 : return Key.Q               ;
      case 0x11 : return Key.W               ;
      case 0x12 : return Key.E               ;
      case 0x13 : return Key.R               ;
      case 0x14 : return Key.T               ;
      case 0x15 : return Key.Y               ;
      case 0x16 : return Key.U               ;
      case 0x17 : return Key.I               ;
      case 0x18 : return Key.O               ;
      case 0x19 : return Key.P               ;
      case 0x1A : return Key.LEFT_BRACKET    ;
      case 0x1B : return Key.RIGHT_BRACKET   ;
      case 0x1C : return Key.ENTER           ;
      case 0x1D : return Key.CONTROL         ;
      case 0x1E : return Key.A               ;
      case 0x1F : return Key.S               ;
      case 0x20 : return Key.D               ;
      case 0x21 : return Key.F               ;
      case 0x22 : return Key.G               ;
      case 0x23 : return Key.H               ;
      case 0x24 : return Key.J               ;
      case 0x25 : return Key.K               ;
      case 0x26 : return Key.L               ;
      case 0x27 : return Key.SEMICOLON       ;
      case 0x28 : return Key.QUOTE           ;
      case 0x2A : return Key.SHIFT           ;
      case 0x2B : return Key.BACKSLASH       ;
      case 0x2C : return Key.Z               ;
      case 0x2D : return Key.X               ;
      case 0x2E : return Key.C               ;
      case 0x2F : return Key.V               ;
      case 0x30 : return Key.B               ;
      case 0x31 : return Key.N               ;
      case 0x32 : return Key.M               ;
      case 0x33 : return Key.COMMA           ;
      case 0x34 : return Key.PERIOD          ;
      case 0x35 : return Key.SLASH           ;
      case 0x36 : return Key.SHIFT           ;
      case 0x37 : return Key.MULTIPLY        ;
      case 0x38 : return Key.MENU            ;
      case 0x39 : return Key.SPACE           ;
      case 0x3A : return Key.CAPS_LOCK       ;
      case 0x3B : return Key.F1              ;
      case 0x3C : return Key.F2              ;
      case 0x3D : return Key.F3              ;
      case 0x3E : return Key.F4              ;
      case 0x3F : return Key.F5              ;
      case 0x40 : return Key.F6              ;
      case 0x41 : return Key.F7              ;
      case 0x42 : return Key.F8              ;
      case 0x43 : return Key.F9              ;
      case 0x44 : return Key.F10             ;
      case 0x45 : return Key.NP_NUM_LOCK     ;
      case 0x46 : return Key.SCROLL_LOCK     ;
      case 0x47 : return Key.NP7             ;
      case 0x48 : return Key.NP8             ;
      case 0x49 : return Key.NP9             ;
      case 0x4A : return Key.NP_SUBTRACT     ;
      case 0x4B : return Key.NP4             ;
      case 0x4C : return Key.NP5             ;
      case 0x4D : return Key.NP6             ;
      case 0x4E : return Key.NP_ADD          ;
      case 0x4F : return Key.NP1             ;
      case 0x50 : return Key.NP2             ;
      case 0x51 : return Key.NP3             ;
      case 0x52 : return Key.NP0             ;
      case 0x53 : return Key.NP_DECIMAL      ;
      case 0x57 : return Key.F11             ;
      case 0x58 : return Key.F12             ;
      case 0x90 : return Key.CIRCUMFLEX      ;
      case 0x91 : return Key.AT              ;
      case 0x92 : return Key.COLON           ;
      case 0x93 : return Key.UNDERSCORE      ;
      case 0xB7 : return Key.SYSRQ           ;
      case 0xC5 : return Key.PAUSE           ;
      case 0xC7 : return Key.HOME            ;
      case 0xC8 : return Key.UP              ;
      case 0xC9 : return Key.PAGE_UP         ;
      case 0xCB : return Key.LEFT            ;
      case 0xCD : return Key.RIGHT           ;
      case 0xCF : return Key.END             ;
      case 0xD0 : return Key.DOWN            ;
      case 0xD1 : return Key.PAGE_DOWN       ;
      case 0xD2 : return Key.INSERT          ;
      case 0xD3 : return Key.DELETE          ;
      case 0xDB : return Key.META            ;
      case 0xDE : return Key.POWER           ;
    }

    return null;
  }
}
