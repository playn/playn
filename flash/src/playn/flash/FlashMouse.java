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
import playn.core.Mouse;

class FlashMouse implements Mouse {

  private Listener listener;

  FlashMouse() {
    // Mouse handlers.
    FlashPlatform.captureEvent(Sprite.MOUSEDOWN, new EventHandler<MouseEvent>() {
      @Override
      public void handleEvent(MouseEvent nativeEvent) {
        if (listener != null) {
          ButtonEvent.Impl event = new ButtonEvent.Impl(PlayN.currentTime(),
              nativeEvent.getStageX(), nativeEvent.getStageY(), getMouseButton(nativeEvent));
          listener.onMouseDown(event);
          if (event.getPreventDefault()) {
            nativeEvent.preventDefault();
          }
        }
      }
    });
    FlashPlatform.captureEvent(Sprite.MOUSEUP, new EventHandler<MouseEvent>() {
      @Override
      public void handleEvent(MouseEvent nativeEvent) {
        if (listener != null) {
          ButtonEvent.Impl event = new ButtonEvent.Impl(PlayN.currentTime(),
              nativeEvent.getStageX(), nativeEvent.getStageY(), getMouseButton(nativeEvent));
          listener.onMouseUp(event);
          if (event.getPreventDefault()) {
            nativeEvent.preventDefault();
          }
        }
      }
    });
    FlashPlatform.captureEvent(Sprite.MOUSEMOVE, new EventHandler<MouseEvent>() {
      @Override
      public void handleEvent(MouseEvent nativeEvent) {
        if (listener != null) {
          MotionEvent.Impl event = new MotionEvent.Impl(PlayN.currentTime(),
              nativeEvent.getStageX(), nativeEvent.getStageY());
          listener.onMouseMove(event);
          if (event.getPreventDefault()) {
            nativeEvent.preventDefault();
          }
        }
      }
    });
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  /**
   * Return the {@link Mouse} button given a {@link MouseEvent}
   *
   * @param e MouseEvent
   * @return {@link Mouse} button corresponding to the event
   */
  protected static int getMouseButton(MouseEvent e) {
//    if (e.isButtonDown()) {
      return Mouse.BUTTON_LEFT;
//    } else {
//      return Mouse.BUTTON_RIGHT;
//    }
  }
}
