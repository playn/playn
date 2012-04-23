/**
 * Copyright 2011 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.java;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import playn.core.MouseImpl;
import playn.core.PlayN;

class JavaMouse extends MouseImpl {

  JavaMouse() throws LWJGLException {
    Mouse.create();
  }

  void update() {
    JavaPointer pointer = (JavaPointer) PlayN.pointer();

    while (Mouse.next()) {
      double time = (double) (Mouse.getEventNanoseconds() / 1000);
      int btn = getButton(Mouse.getEventButton());
      int x = Mouse.getEventX();
      int y = Display.getHeight() - Mouse.getEventY() - 1;

      if (btn != -1) {
        if (Mouse.getEventButtonState()) {
          onMouseDown(new ButtonEvent.Impl(time, x, y, btn));
          pointer.onMouseDown(time, x, y);
        } else {
          onMouseUp(new ButtonEvent.Impl(time, x, y, btn));
          pointer.onMouseUp(time, x, y);
        }
      } else if (Mouse.getEventDWheel() != 0) {
        onMouseWheelScroll(new WheelEvent.Impl(time, Mouse.getEventDWheel()));
      } else {
        onMouseMove(new MotionEvent.Impl(time, x, y));
        pointer.onMouseMove(time, x, y);
      }
    }
  }

  protected static int getButton(int lwjglButton) {
    switch (lwjglButton) {
    case 0:  return BUTTON_LEFT;
    case 2:  return BUTTON_MIDDLE;
    case 1:  return BUTTON_RIGHT;
    default: return lwjglButton;
    }
  }
}

