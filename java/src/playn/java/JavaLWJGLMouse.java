/**
 * Copyright 2013 The PlayN Authors
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

import pythagoras.f.Point;

class JavaLWJGLMouse extends JavaMouse {

  public JavaLWJGLMouse(JavaPlatform plat) {
    super(plat);
  }

  @Override
  public void lock() {
    Mouse.setGrabbed(true);
  }

  @Override
  public void unlock() {
    Mouse.setGrabbed(false);
  }

  @Override
  public boolean isLocked() {
    return Mouse.isGrabbed();
  }

  @Override
  public boolean isLockSupported() {
    return true;
  }

  @Override
  void init() {
    try {
      Mouse.create();
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  void update() {
    while (Mouse.next()) {
      double time = (double) (Mouse.getEventNanoseconds() / 1000000);
      ButtonEvent.Id btn = getButton(Mouse.getEventButton());
      Point m = new Point(Mouse.getEventX(), Display.getHeight() - Mouse.getEventY() - 1);
      plat.graphics().transformMouse(m);

      int dx = Mouse.getEventDX(), dy = -Mouse.getEventDY();
      if (btn != null) {
        if (Mouse.getEventButtonState()) {
          onMouseDown(time, m.x, m.y, btn);
        } else {
          onMouseUp(time, m.x, m.y, btn);
        }
      } else if (Mouse.getEventDWheel() != 0) {
        int delta = Mouse.getEventDWheel() > 0 ? -1 : 1;
        onMouseWheelScroll(time, m.x, m.y, delta);
      } else {
        onMouseMove(time, m.x, m.y, dx, dy);
      }
    }
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
