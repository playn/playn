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

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import react.RFuture;
import react.RPromise;

import playn.core.Key;
import playn.core.Keyboard;
import playn.core.Mouse;

public class SWTInput extends JavaInput {

  private final SWTPlatform plat;

  public SWTInput(SWTPlatform splat) {
    super(splat);
    this.plat = splat;

    // wire up mouse events
    plat.display().addFilter(SWT.MouseDown, new org.eclipse.swt.widgets.Listener() {
      public void handleEvent (Event event) {
        if (event.widget == plat.graphics().canvas) {
          Mouse.ButtonEvent.Id btn = mapButton(event.button);
          if (btn != null) emitMouseButton(event.time, event.x, event.y, btn, true, mods(event));
        }
      }
    });
    plat.display().addFilter(SWT.MouseUp, new org.eclipse.swt.widgets.Listener() {
      public void handleEvent (Event event) {
        if (event.widget == plat.graphics().canvas) {
          Mouse.ButtonEvent.Id btn = mapButton(event.button);
          if (btn != null) emitMouseButton(event.time, event.x, event.y, btn, false, mods(event));
        }
      }
    });
    plat.display().addFilter(SWT.MouseMove, new org.eclipse.swt.widgets.Listener() {
      public void handleEvent (Event event) {
        if (event.widget == plat.graphics().canvas) {
          float dx = event.x - lastX, dy = event.y - lastY;
          emitMouseMotion(event.time, event.x, event.y, dx, dy, mods(event));
        }
      }
      private float lastX, lastY;
    });
    plat.display().addFilter(SWT.MouseWheel, new org.eclipse.swt.widgets.Listener() {
      public void handleEvent (Event event) {
        if (event.widget == plat.graphics().canvas) {
          emitMouseWheel(event.time, event.x, event.y, -event.count, mods(event));
        }
      }
    });

    // wire up keyboard events
    plat.display().addFilter(SWT.KeyDown, new org.eclipse.swt.widgets.Listener() {
      public void handleEvent (Event event) {
        Key key = translateKey(event.keyCode);
        if (key != null) emitKeyPress(event.time, key, true, mods(event));
        else System.err.println("KEY? " + event.keyCode + " / " + event.character);

        char keyChar = event.character;
        if (Character.isISOControl(keyChar)) emitKeyTyped(event.time, keyChar);
      }
    });
    plat.display().addFilter(SWT.KeyUp, new org.eclipse.swt.widgets.Listener() {
      public void handleEvent (Event event) {
        Key key = translateKey(event.keyCode);
        if (key != null) emitKeyPress(event.time, key, false, mods(event));
      }
    });
  }

  @Override public RFuture<String> getText (Keyboard.TextType textType,
                                            String label, String initVal) {
    final RPromise<String> result = RPromise.create();

    final Shell shell = new Shell(plat.shell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    GridLayout layout = new GridLayout(2, true);
    layout.marginTop = 10;
    layout.marginLeft = 10;
    layout.marginBottom = 10;
    layout.marginRight = 10;
    layout.verticalSpacing = 10;
    shell.setLayout(layout);
    shell.addListener(SWT.Traverse, new Listener() {
      public void handleEvent(Event event) {
        if (event.detail == SWT.TRAVERSE_ESCAPE) {
          result.succeed(null);
        }
      }
    });

    Label info = new Label(shell, SWT.NULL);
    GridData idata = new GridData();
    idata.horizontalSpan = 2;
    info.setLayoutData(idata);
    info.setText(label);

    final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
    GridData tdata = new GridData(GridData.FILL_HORIZONTAL);
    tdata.horizontalSpan = 2;
    text.setLayoutData(tdata);
    if (initVal != null) text.setText(initVal);

    final Button ok = new Button(shell, SWT.PUSH);
    ok.setText("OK");
    ok.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    ok.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        String r = text.getText();
        shell.dispose();
        result.succeed(r);
      }
    });
    shell.setDefaultButton(ok);

    Button cancel = new Button(shell, SWT.PUSH);
    cancel.setText("Cancel");
    cancel.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        shell.dispose();
        result.succeed(null);
      }
    });

    shell.pack();
    Rectangle psize = plat.shell().getBounds(), ssize = shell.getBounds();
    shell.setLocation(new Point(psize.x + (psize.width - ssize.width)/2,
                                psize.y + (psize.height - ssize.height)/2));
    shell.open();

    // HACK HACK HACK: SWT drops any pending mouse up event when we open this shell (even if we
    // defer it via Display.asyncExec) so we have to fake a mouse up here lest all input processing
    // get totally hosed; sigh
    emitFakeMouseUp();

    return result;
  }

  @Override public RFuture<Boolean> sysDialog (String title, String text,
                                               String ok, String cancel) {
    int style = (cancel == null) ?
      SWT.ICON_INFORMATION|SWT.OK : SWT.ICON_QUESTION|SWT.OK|SWT.CANCEL;
    MessageBox box = new MessageBox(plat.shell(), style);
    box.setText(title);
    box.setMessage(text);
    // HACK HACK HACK: see above hand wringing
    emitFakeMouseUp();
    int button = box.open();
    return RFuture.success(button == SWT.OK);
  }

  private void emitFakeMouseUp () {
    mouseEvents.emit(new Mouse.ButtonEvent(0, plat.time(), 0, 0, Mouse.ButtonEvent.Id.LEFT, false));
  }

  private int mods (org.eclipse.swt.widgets.Event event) {
    return modifierFlags((event.stateMask & SWT.ALT) == SWT.ALT,
                         (event.stateMask & SWT.CTRL) == SWT.CTRL,
                         (event.stateMask & SWT.COMMAND) == SWT.COMMAND,
                         (event.stateMask & SWT.SHIFT) == SWT.SHIFT);
  }

  private Mouse.ButtonEvent.Id mapButton(int swtButton) {
    switch (swtButton) {
    case 1:  return Mouse.ButtonEvent.Id.LEFT;
    case 2:  return Mouse.ButtonEvent.Id.MIDDLE;
    case 3:  return Mouse.ButtonEvent.Id.RIGHT;
    default: return null; // TODO
    }
  }

  public Key translateKey(int keyCode) {
    switch (keyCode) {
    case SWT.ALT             : return Key.ALT;
    case SWT.ARROW_DOWN      : return Key.DOWN;
    case SWT.ARROW_LEFT      : return Key.LEFT;
    case SWT.ARROW_RIGHT     : return Key.RIGHT;
    case SWT.ARROW_UP        : return Key.UP;
    case SWT.BREAK           : return Key.BREAK;
    case SWT.CAPS_LOCK       : return Key.CAPS_LOCK;
    case SWT.COMMAND         : return Key.META;
    case SWT.CONTROL         : return Key.CONTROL;
    case SWT.DEL             : return Key.DELETE;
    case SWT.END             : return Key.END;
    case SWT.ESC             : return Key.ESCAPE;
    case SWT.F1              : return Key.F1;
    case SWT.F10             : return Key.F10;
    case SWT.F11             : return Key.F11;
    case SWT.F12             : return Key.F12;
    case SWT.F2              : return Key.F2;
    case SWT.F3              : return Key.F3;
    case SWT.F4              : return Key.F4;
    case SWT.F5              : return Key.F5;
    case SWT.F6              : return Key.F6;
    case SWT.F7              : return Key.F7;
    case SWT.F8              : return Key.F8;
    case SWT.F9              : return Key.F9;
    case SWT.HOME            : return Key.HOME;
    case SWT.INSERT          : return Key.INSERT;
    case SWT.KEYPAD_0        : return Key.NP0;
    case SWT.KEYPAD_1        : return Key.NP1;
    case SWT.KEYPAD_2        : return Key.NP2;
    case SWT.KEYPAD_3        : return Key.NP3;
    case SWT.KEYPAD_4        : return Key.NP4;
    case SWT.KEYPAD_5        : return Key.NP5;
    case SWT.KEYPAD_6        : return Key.NP6;
    case SWT.KEYPAD_7        : return Key.NP7;
    case SWT.KEYPAD_8        : return Key.NP8;
    case SWT.KEYPAD_9        : return Key.NP9;
    case SWT.KEYPAD_CR       : return Key.ENTER;
    case SWT.KEYPAD_DECIMAL  : return Key.NP_DECIMAL;
    case SWT.KEYPAD_DIVIDE   : return Key.NP_DIVIDE;
    // case SWT.KEYPAD_EQUAL    : return Key.NP_EQUAL;
    case SWT.KEYPAD_MULTIPLY : return Key.NP_MULTIPLY;
    case SWT.KEYPAD_SUBTRACT : return Key.NP_SUBTRACT;
    case SWT.NUM_LOCK        : return Key.NP_NUM_LOCK;
    case SWT.PAGE_DOWN       : return Key.PAGE_DOWN;
    case SWT.PAGE_UP         : return Key.PAGE_UP;
    case SWT.PAUSE           : return Key.PAUSE;
    case SWT.PRINT_SCREEN    : return Key.PRINT_SCREEN;
    case SWT.SCROLL_LOCK     : return Key.SCROLL_LOCK;
    case SWT.SHIFT           : return Key.SHIFT;
    case SWT.TAB             : return Key.TAB;
    // case SWT.KEY_SYSRQ       : return Key.SYSRQ;
    // case SWT.KEY_FUNCTION    : return Key.FUNCTION;
    // case SWT.KEY_DELETE      : return Key.DELETE;
    // case SWT.KEY_CLEAR       : return Key.CLEAR;
    // case SWT.KEY_POWER       : return Key.POWER;

    // printing keys are just their ASCII equivalents
    case 'a': return Key.A;
    case 'b': return Key.B;
    case 'c': return Key.C;
    case 'd': return Key.D;
    case 'e': return Key.E;
    case 'f': return Key.F;
    case 'g': return Key.G;
    case 'h': return Key.H;
    case 'i': return Key.I;
    case 'j': return Key.J;
    case 'k': return Key.K;
    case 'l': return Key.L;
    case 'm': return Key.M;
    case 'n': return Key.N;
    case 'o': return Key.O;
    case 'p': return Key.P;
    case 'q': return Key.Q;
    case 'r': return Key.R;
    case 's': return Key.S;
    case 't': return Key.T;
    case 'u': return Key.U;
    case 'v': return Key.V;
    case 'w': return Key.W;
    case 'x': return Key.X;
    case 'y': return Key.Y;
    case 'z': return Key.Z;

    case '0': return Key.K0;
    case '1': return Key.K1;
    case '2': return Key.K2;
    case '3': return Key.K3;
    case '4': return Key.K4;
    case '5': return Key.K5;
    case '6': return Key.K6;
    case '7': return Key.K7;
    case '8': return Key.K8;
    case '9': return Key.K9;

    case '`': return Key.BACKQUOTE;
    case '-': return Key.MINUS;
    case ' ': return Key.SPACE;
    case '=': return Key.EQUALS;
    case '[': return Key.LEFT_BRACKET;
    case ']': return Key.RIGHT_BRACKET;
    case ';': return Key.SEMICOLON;
    case '/': return Key.SLASH;
    case ',': return Key.COMMA;
    case '.': return Key.PERIOD;
    case '~': return Key.TILDE;
    case   8: return Key.BACKSPACE;

    case '\'': return Key.QUOTE;
    case '\\': return Key.BACKSLASH;
    case '\n': return Key.ENTER;
    case '\r': return Key.ENTER;

    // TODO: other printables that aren't "unshifted" on US QWERTY?
    }

    return null;
  }
}
