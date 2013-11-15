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

import playn.core.Events;
import playn.core.Key;
import playn.core.Keyboard;
import playn.core.Mouse;
import playn.core.Mouse.ButtonEvent.Impl;
import playn.core.TouchImpl;
import pythagoras.f.Point;

/** Implements touches using a special customized JavaMouse mouse. For testing.
 * TODO: show multitouch points on screen
 * TODO: allow pivot slide */
public class JavaEmulatedTouch extends TouchImpl
{
  private boolean mouseDown;
  private Point pivot;
  private float x, y;
  private int currentId;
  private final Key multiTouchKey;

  Keyboard.Listener keyListener = new Keyboard.Adapter() {
    @Override public void onKeyUp (playn.core.Keyboard.Event event) {
      if (event.key() == multiTouchKey)
        pivot = new Point(x, y);
    }
  };

  public JavaEmulatedTouch (Key multiTouchKey) {
    this.multiTouchKey = multiTouchKey;
  }

  @Override public boolean hasTouch() {
    return true;
  }

  JavaMouse createMouse (JavaPlatform platform) {
    return new JavaLWJGLMouse(platform) {

      @Override public boolean hasMouse() {
        return false;
      }

      @Override protected boolean onMouseDown(Mouse.ButtonEvent.Impl event) {
        JavaEmulatedTouch.this.onMouseDown(event.time(), event.x(), event.y());
        return false;
      }

      @Override protected boolean onMouseMove(Mouse.MotionEvent.Impl event) {
        JavaEmulatedTouch.this.onMouseMove(event.time(), event.x(), event.y());
        return false;
      }

      @Override protected boolean onMouseUp(Impl event) {
        JavaEmulatedTouch.this.onMouseUp(event.time(), event.x(), event.y());
        return false;
      }

      @Override protected boolean onMouseWheelScroll(playn.core.Mouse.WheelEvent.Impl event) {
        return false;
      }
    };
  }

  void onMouseDown(double time, float x, float y) {
    currentId+=2; // skip an id in case of pivot
    onTouchStart(toTouches(time, x, y));
    mouseDown = true;
  }

  void onMouseUp(double time, float x, float y) {
    onTouchEnd(toTouches(time, x, y));
    mouseDown = false;
    pivot = null;
  }

  void onMouseMove(double time, float x, float y) {
    this.x = x;
    this.y = y;
    if (mouseDown) {
      onTouchMove(toTouches(time, x, y));
    }
  }

  Event.Impl toTouch (double time, float x, float y, int idoff) {
    return new Event.Impl(new Events.Flags.Impl(), time, x, y, currentId+idoff);
  }

  Event.Impl[] toTouches (double time, float x, float y) {
    Event.Impl t = toTouch(time, x, y, 0);
    return pivot == null ?
      new Event.Impl[] {t} :
      new Event.Impl[] {t, toTouch(time, 2*pivot.x-x, 2*pivot.y-y, 1)};
  }
}
