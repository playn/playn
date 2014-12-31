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

import playn.core.Mouse;

abstract class JavaMouse extends Mouse {

  protected final JavaPlatform plat;

  public JavaMouse(JavaPlatform plat) {
    this.plat = plat;
  }

  abstract void init();
  abstract void update();

  protected void onMouseDown (double time, float x, float y, ButtonEvent.Id btn) {
    events.emit(new ButtonEvent(0, time, x, y, btn, true));
  }

  protected void onMouseUp (double time, float x, float y, ButtonEvent.Id btn) {
    events.emit(new ButtonEvent(0, time, x, y, btn, false));
  }

  protected void onMouseMove (double time, float x, float y, float dx, float dy) {
    events.emit(new MotionEvent(0, time, x, y, dx, dy));
  }

  protected void onMouseWheelScroll (double time, float x, float y, int delta) {
    events.emit(new WheelEvent(0, time, x, y, delta));
  }
}

