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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import static org.lwjgl.glfw.GLFW.*;

import pythagoras.f.Dimension;
import pythagoras.f.Point;
import react.RFuture;

import playn.core.Key;
import static playn.core.Keyboard.*; 
import static playn.core.Mouse.*;

public class LWJGLInput extends JavaInput {

  // TODO: set this from somewhere?
  private JFrame frame;
  private final LWJGLWindow window;
  
  // We have to keep strong references to GLFW callback.
  private GLFWCharCallback charCallback;
  private GLFWKeyCallback keyCallback;
  private GLFWMouseButtonCallback mouseCallback;
  private GLFWScrollCallback scrollCallback;
  
  public LWJGLInput (LWJGLPlatform plat) {
    super(plat);
    this.window = ((LWJGLGraphics) plat.graphics()).window;
  }

  @Override public RFuture<String> getText(TextType textType, String label, String initVal) {
    Object result = JOptionPane.showInputDialog(
      frame, label, "", JOptionPane.QUESTION_MESSAGE, null, null, initVal);
    return RFuture.success((String)result);
  }

  @Override public RFuture<Boolean> sysDialog (String title, String text,
                                               String ok, String cancel) {
    int optType = JOptionPane.OK_CANCEL_OPTION;
    int msgType = cancel == null ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.QUESTION_MESSAGE;
    Object[] options = (cancel == null) ? new Object[] { ok } : new Object[] { ok, cancel };
    Object defOption = (cancel == null) ? ok : cancel;
    int result = JOptionPane.showOptionDialog(
      frame, text, title, optType, msgType, null, options, defOption);
    return RFuture.success(result == 0);
  }

  @Override public boolean hasMouseLock () { return true; }
  
  @Override public boolean isMouseLocked() { 
    return glfwGetInputMode(window.handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED; 
  }
  
  @Override public void setMouseLocked (boolean locked) { 
    glfwSetInputMode(window.handle, GLFW_CURSOR, locked ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL); 
  }

  @Override void init() {
    glfwSetCharCallback(window.handle, charCallback = new GLFWCharCallback(){
      @Override public void invoke(long window, int codepoint) {
        emitKeyTyped(System.currentTimeMillis(), (char) codepoint);
      }
    });
    
    glfwSetKeyCallback(window.handle, keyCallback = new GLFWKeyCallback(){
      @Override public void invoke(long window, int keyCode, int scancode, int action, int mods) {
        int flags = calModifierFlags();
        double time = System.currentTimeMillis();
        Key key = translateKey(keyCode);
        boolean pressed = action == GLFW_PRESS || action == GLFW_REPEAT;
        if (key != null) emitKeyPress(time, key, pressed, flags);
        else plat.log().warn("Unknown keyCode:" + keyCode);
      }
    });
    
    glfwSetMouseButtonCallback(window.handle, mouseCallback = new GLFWMouseButtonCallback(){
      @Override public void invoke(long handle, int btnIdx, int action, int mods) {
        double time = System.currentTimeMillis();
        Point m = queryCursorPosition();
        ButtonEvent.Id btn = getButton(btnIdx);
        if (btn == null) return;
        emitMouseButton(time, m.x, m.y, btn, action == GLFW_PRESS, mods);
      }
    });
    
    glfwSetScrollCallback(window.handle, scrollCallback = new GLFWScrollCallback(){
      @Override public void invoke(long handle, double xoffset, double yoffset) {
        Point m = queryCursorPosition();
        double time = System.currentTimeMillis();
        //TODO: is it correct that just simply sets the flag as 0?
        if (GLFW_CURSOR_DISABLED == glfwGetInputMode(window.handle, GLFW_CURSOR))
          emitMouseMotion(time, m.x, m.y, (float) xoffset, -(float) yoffset, 0);
        else emitMouseWheel(time, m.x, m.y, yoffset > 0 ? -1 : 1, 0);
      }
    });
  }
  
  private Point queryCursorPosition() {
    ByteBuffer xpos = ByteBuffer.allocate(1);
    ByteBuffer ypos = ByteBuffer.allocate(1);
    glfwGetCursorPos(window.handle, xpos, ypos);
    Dimension wsize = window.size();
    Point m = new Point(xpos.getFloat(), wsize.height - ypos.getFloat() -1);
    return m;
  }
  
  /* Determine the current state of the modifier keys (note: the code assumes the current state 
   * of the modifier keys is "correct" for all events that have arrived since the last call to 
   * update; since that happens pretty frequently, 60fps, that's probably good enough) */
  private int calModifierFlags() {
    int flags = modifierFlags(
        isKeyDown(GLFW_KEY_LEFT_ALT) || isKeyDown(GLFW_KEY_LEFT_ALT),
        isKeyDown(GLFW_KEY_LEFT_CONTROL) || isKeyDown(GLFW_KEY_RIGHT_CONTROL),
        isKeyDown(GLFW_KEY_LEFT_SUPER) || isKeyDown(GLFW_KEY_RIGHT_SUPER),
        isKeyDown(GLFW_KEY_LEFT_SHIFT) || isKeyDown(GLFW_KEY_RIGHT_SHIFT));
    return flags;
  }

  private boolean isKeyDown(int key){
    return glfwGetKey(window.handle, key) == GLFW_PRESS;
  }
  
  private static ButtonEvent.Id getButton(int lwjglButton) {
    switch (lwjglButton) {
    case GLFW_MOUSE_BUTTON_LEFT:  return ButtonEvent.Id.LEFT;
    case GLFW_MOUSE_BUTTON_MIDDLE:  return ButtonEvent.Id.MIDDLE;
    case GLFW_MOUSE_BUTTON_RIGHT:  return ButtonEvent.Id.RIGHT;
    default: return null;
    }
  }

  private Key translateKey(int keyCode) {
    switch (keyCode) {
      case GLFW_KEY_ESCAPE       : return Key.ESCAPE;
      case GLFW_KEY_1            : return Key.K1;
      case GLFW_KEY_2            : return Key.K2;
      case GLFW_KEY_3            : return Key.K3;
      case GLFW_KEY_4            : return Key.K4;
      case GLFW_KEY_5            : return Key.K5;
      case GLFW_KEY_6            : return Key.K6;
      case GLFW_KEY_7            : return Key.K7;
      case GLFW_KEY_8            : return Key.K8;
      case GLFW_KEY_9            : return Key.K9;
      case GLFW_KEY_0            : return Key.K0;
      case GLFW_KEY_MINUS        : return Key.MINUS;
      case GLFW_KEY_EQUAL        : return Key.EQUALS;
      case GLFW_KEY_BACKSPACE    : return Key.BACK;
      case GLFW_KEY_TAB          : return Key.TAB;
      case GLFW_KEY_Q            : return Key.Q;
      case GLFW_KEY_W            : return Key.W;
      case GLFW_KEY_E            : return Key.E;
      case GLFW_KEY_R            : return Key.R;
      case GLFW_KEY_T            : return Key.T;
      case GLFW_KEY_Y            : return Key.Y;
      case GLFW_KEY_U            : return Key.U;
      case GLFW_KEY_I            : return Key.I;
      case GLFW_KEY_O            : return Key.O;
      case GLFW_KEY_P            : return Key.P;
      case GLFW_KEY_LEFT_BRACKET : return Key.LEFT_BRACKET;
      case GLFW_KEY_RIGHT_BRACKET: return Key.RIGHT_BRACKET;
      case GLFW_KEY_ENTER        : return Key.ENTER;
      case GLFW_KEY_RIGHT_CONTROL: return Key.CONTROL; 
      case GLFW_KEY_LEFT_CONTROL : return Key.CONTROL;
      case GLFW_KEY_A            : return Key.A;
      case GLFW_KEY_S            : return Key.S;
      case GLFW_KEY_D            : return Key.D;
      case GLFW_KEY_F            : return Key.F;
      case GLFW_KEY_G            : return Key.G;
      case GLFW_KEY_H            : return Key.H;
      case GLFW_KEY_J            : return Key.J;
      case GLFW_KEY_K            : return Key.K;
      case GLFW_KEY_L            : return Key.L;
      case GLFW_KEY_SEMICOLON    : return Key.SEMICOLON;
      case GLFW_KEY_APOSTROPHE   : return Key.QUOTE;
      case GLFW_KEY_GRAVE_ACCENT : return Key.BACKQUOTE;
      case GLFW_KEY_LEFT_SHIFT   : return Key.SHIFT; // PlayN doesn't know left v. right
      case GLFW_KEY_BACKSLASH    : return Key.BACKSLASH;
      case GLFW_KEY_Z            : return Key.Z;
      case GLFW_KEY_X            : return Key.X;
      case GLFW_KEY_C            : return Key.C;
      case GLFW_KEY_V            : return Key.V;
      case GLFW_KEY_B            : return Key.B;
      case GLFW_KEY_N            : return Key.N;
      case GLFW_KEY_M            : return Key.M;
      case GLFW_KEY_COMMA        : return Key.COMMA;
      case GLFW_KEY_PERIOD       : return Key.PERIOD;
      case GLFW_KEY_SLASH        : return Key.SLASH;
      case GLFW_KEY_RIGHT_SHIFT  : return Key.SHIFT; // PlayN doesn't know left v. right
      case GLFW_KEY_KP_MULTIPLY  : return Key.MULTIPLY;
      case GLFW_KEY_SPACE        : return Key.SPACE;
      case GLFW_KEY_CAPS_LOCK    : return Key.CAPS_LOCK;
      case GLFW_KEY_F1           : return Key.F1;
      case GLFW_KEY_F2           : return Key.F2;
      case GLFW_KEY_F3           : return Key.F3;
      case GLFW_KEY_F4           : return Key.F4;
      case GLFW_KEY_F5           : return Key.F5;
      case GLFW_KEY_F6           : return Key.F6;
      case GLFW_KEY_F7           : return Key.F7;
      case GLFW_KEY_F8           : return Key.F8;
      case GLFW_KEY_F9           : return Key.F9;
      case GLFW_KEY_F10          : return Key.F10;
      case GLFW_KEY_NUM_LOCK     : return Key.NP_NUM_LOCK;
      case GLFW_KEY_SCROLL_LOCK  : return Key.SCROLL_LOCK;
      case GLFW_KEY_KP_7         : return Key.NP7;
      case GLFW_KEY_KP_8         : return Key.NP8;
      case GLFW_KEY_KP_9         : return Key.NP9;
      case GLFW_KEY_KP_SUBTRACT  : return Key.NP_SUBTRACT;
      case GLFW_KEY_KP_4         : return Key.NP4;
      case GLFW_KEY_KP_5         : return Key.NP5;
      case GLFW_KEY_KP_6         : return Key.NP6;
      case GLFW_KEY_KP_ADD       : return Key.NP_ADD;
      case GLFW_KEY_KP_1         : return Key.NP1;
      case GLFW_KEY_KP_2         : return Key.NP2;
      case GLFW_KEY_KP_3         : return Key.NP3;
      case GLFW_KEY_KP_0         : return Key.NP0;
      case GLFW_KEY_KP_DECIMAL   : return Key.NP_DECIMAL;
      case GLFW_KEY_F11          : return Key.F11;
      case GLFW_KEY_F12          : return Key.F12;
      //case GLFW_KEY_F13          : return Key.F13;
      //case GLFW_KEY_F14          : return Key.F14;
      //case GLFW_KEY_F15          : return Key.F15;
      //case GLFW_KEY_F16          : return Key.F16;
      //case GLFW_KEY_F17          : return Key.F17;
      //case GLFW_KEY_F18          : return Key.F18;
      //case GLFW_KEY_KANA         : return Key.
      //case GLFW_KEY_F19          : return Key.F19;
      //case GLFW_KEY_CONVERT      : return Key.
      //case GLFW_KEY_NOCONVERT    : return Key.
      //case GLFW_KEY_YEN          : return Key.
      //case GLFW_KEY_NUMPADEQUALS : return Key.
      //TODO: case GLFW_KEY_CIRCUMFLEX   : return Key.CIRCUMFLEX;
      //TODO: case GLFW_KEY_AT           : return Key.AT;
      //TODO: case GLFW_KEY_COLON        : return Key.COLON;
      //TODO: case GLFW_KEY_UNDERLINE    : return Key.UNDERSCORE;
      //case GLFW_KEY_KANJI        : return Key.
      //case GLFW_KEY_STOP         : return Key.
      //case GLFW_KEY_AX           : return Key.
      //case GLFW_KEY_UNLABELED    : return Key.
      //case GLFW_KEY_NUMPADENTER  : return Key.
      //case GLFW_KEY_SECTION      : return Key.
      //case GLFW_KEY_NUMPADCOMMA  : return Key.
      //case GLFW_KEY_DIVIDE       :
      //TODO: case GLFW_KEY_SYSRQ        : return Key.SYSRQ;
      case GLFW_KEY_RIGHT_ALT    : return Key.ALT; // PlayN doesn't know left v. right
      case GLFW_KEY_LEFT_ALT     : return Key.ALT; // PlayN doesn't know left v. right
      case GLFW_KEY_MENU         : return Key.FUNCTION;
      case GLFW_KEY_PAUSE        : return Key.PAUSE;
      case GLFW_KEY_HOME         : return Key.HOME;
      case GLFW_KEY_UP           : return Key.UP;
      case GLFW_KEY_PAGE_UP      : return Key.PAGE_UP;
      case GLFW_KEY_LEFT         : return Key.LEFT;
      case GLFW_KEY_RIGHT        : return Key.RIGHT;
      case GLFW_KEY_END          : return Key.END;
      case GLFW_KEY_DOWN         : return Key.DOWN;
      case GLFW_KEY_PAGE_DOWN    : return Key.PAGE_DOWN;
      case GLFW_KEY_INSERT       : return Key.INSERT;
      case GLFW_KEY_DELETE       : return Key.DELETE;
      //TODO: case GLFW_KEY_CLEAR        : return Key.CLEAR;
      case GLFW_KEY_RIGHT_SUPER  : return Key.META; // PlayN doesn't know left v. right
      case GLFW_KEY_LEFT_SUPER   : return Key.META; // PlayN doesn't know left v. right
      //case GLFW_KEY_LWIN         : return Key.WINDOWS; // Duplicate with KEY_LMETA
      //case GLFW_KEY_RWIN         : return Key.WINDOWS; // Duplicate with KEY_RMETA
      //case GLFW_KEY_APPS         : return Key.
      //TODO: case GLFW_KEY_POWER  : return Key.POWER;
      //case Keyboard.KEY_SLEEP    : return Key.
    }

    return null;
  }

  public void shutdown() {
    if (charCallback != null) glfwSetCharCallback(window.handle, charCallback = null);
    if (keyCallback != null) glfwSetKeyCallback(window.handle, keyCallback = null);
    if (mouseCallback != null ) glfwSetMouseButtonCallback(window.handle, mouseCallback = null);
    if (scrollCallback != null ) glfwSetScrollCallback(window.handle, scrollCallback = null);
  }
}
