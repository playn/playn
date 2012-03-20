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
package playn.flash;

import flash.events.MouseEvent;
import flash.display.Sprite;

import playn.core.PlayN;
import playn.core.MouseImpl;

class FlashMouse extends MouseImpl {

  FlashMouse() {
    // Mouse handlers.
    FlashPlatform.captureEvent(Sprite.MOUSEDOWN, new EventHandler<MouseEvent>() {
      @Override
      public void handleEvent(MouseEvent nativeEvent) {
        float x = nativeEvent.getStageX(), y = nativeEvent.getStageY();
        if (onMouseDown(new ButtonEvent.Impl(PlayN.currentTime(), x, y, getMouseButton(nativeEvent))))
          nativeEvent.preventDefault();
      }
    });
    FlashPlatform.captureEvent(Sprite.MOUSEUP, new EventHandler<MouseEvent>() {
      @Override
      public void handleEvent(MouseEvent nativeEvent) {
        float x = nativeEvent.getStageX(), y = nativeEvent.getStageY();
        if (onMouseUp(new ButtonEvent.Impl(PlayN.currentTime(), x, y, getMouseButton(nativeEvent))))
          nativeEvent.preventDefault();
      }
    });
    FlashPlatform.captureEvent(Sprite.MOUSEMOVE, new EventHandler<MouseEvent>() {
      @Override
      public void handleEvent(MouseEvent nativeEvent) {
        float x = nativeEvent.getStageX(), y = nativeEvent.getStageY();
        if (onMouseMove(new MotionEvent.Impl(PlayN.currentTime(), x, y)))
          nativeEvent.preventDefault();
      }
    });
  }

  protected static int getMouseButton(MouseEvent e) {
//    if (e.isButtonDown()) {
      return BUTTON_LEFT;
//    } else {
//      return BUTTON_RIGHT;
//    }
  }
}
