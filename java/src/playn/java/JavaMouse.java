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

import playn.core.Asserts;
import playn.core.Events;
import playn.core.MouseImpl;

abstract class JavaMouse extends MouseImpl {

  protected final JavaPlatform platform;
  private final JavaPointer pointer;

  public JavaMouse(JavaPlatform platform) {
    this.platform = platform;
    this.pointer = Asserts.checkNotNull(platform.pointer());
  }

  abstract void init();
  abstract void update();

  protected void onMouseDown(double time, float x, float y, int btn) {
    onMouseDown(new ButtonEvent.Impl(new Events.Flags.Impl(), time, x, y, btn));
    if (btn == 0)
      pointer.onMouseDown(time, x, y);
  }

  protected void onMouseUp(double time, float x, float y, int btn) {
    onMouseUp(new ButtonEvent.Impl(new Events.Flags.Impl(), time, x, y, btn));
    if (btn == 0)
      pointer.onMouseUp(time, x, y);
  }

  protected void onMouseMove(double time, float x, float y, float dx, float dy) {
    onMouseMove(new MotionEvent.Impl(new Events.Flags.Impl(), time, x, y, dx, dy));
    pointer.onMouseMove(time, x, y);
  }

  protected void onMouseWheelScroll(double time, float x, float y, int delta) {
    onMouseWheelScroll(new WheelEvent.Impl(new Events.Flags.Impl(), time, x, y, delta));
  }
}

