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
import playn.core.Mouse;
import playn.core.Mouse.ButtonEvent.Impl;
import playn.core.TouchImpl;

/** Implements touches using a special customized JavaMouse mouse. For testing.
 * TODO: flesh out using keypresses for multitouch emulation */
public class JavaEmulatedTouch extends TouchImpl
{
  private boolean mouseDown;
  private int currentId;

  @Override public boolean hasTouch() {
    return true;
  }

  JavaMouse createMouse (JavaPlatform platform) {
    return new JavaMouse(platform) {

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
    currentId++;
    onTouchStart(new Event.Impl[] {toTouch(time, x, y)});
    mouseDown = true;
  }

  void onMouseUp(double time, float x, float y) {
    onTouchEnd(new Event.Impl[] {toTouch(time, x, y)});
    mouseDown = false;
  }

  void onMouseMove(double time, float x, float y) {
    if (mouseDown) {
      onTouchMove(new Event.Impl[] {toTouch(time, x, y)});
    }
  }

  Event.Impl toTouch (double time, float x, float y) {
    return new Event.Impl(new Events.Flags.Impl(), time, x, y, currentId);
  }
}
