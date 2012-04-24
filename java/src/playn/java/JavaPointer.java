/**
 * Copyright 2010 The PlayN Authors
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

import playn.core.PointerImpl;

class JavaPointer extends PointerImpl {

  private boolean mouseDown;

  void onMouseDown(double time, float x, float y) {
    onPointerStart(new Event.Impl(time, x, y, false), false);
    mouseDown = true;
  }

  void onMouseUp(double time, float x, float y) {
    onPointerEnd(new Event.Impl(time, x, y, false), false);
    mouseDown = false;
  }

  void onMouseMove(double time, float x, float y) {
    if (mouseDown) {
      onPointerDrag(new Event.Impl(time, x, y, false), false);
    }
  }

  void update() {
    // Do nothing -- JavaMouse takes care of pointer events.
  }
}
