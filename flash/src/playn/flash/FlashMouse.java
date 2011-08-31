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
      public void handleEvent(MouseEvent evt) {
        evt.preventDefault();
        if (listener != null) {
          listener.onMouseDown(
            new ButtonEvent.Impl(PlayN.currentTime(), evt.getStageX(), evt.getStageY(),
                                 getMouseButton(evt)));
        }
      }
    });
    FlashPlatform.captureEvent(Sprite.MOUSEUP, new EventHandler<MouseEvent>() {
      public void handleEvent(MouseEvent evt) {
        if (listener != null) {
          listener.onMouseUp(
            new ButtonEvent.Impl(PlayN.currentTime(), evt.getStageX(), evt.getStageY(),
                // TODO: fix to handle release of right button / middle button
                                 Mouse.BUTTON_LEFT));
        }
      }
    });
    FlashPlatform.captureEvent(Sprite.MOUSEMOVE, new EventHandler<MouseEvent>() {
      public void handleEvent(MouseEvent evt) {
        if (listener != null) {
          listener.onMouseMove(
            new MotionEvent.Impl(PlayN.currentTime(), evt.getStageX(), evt.getStageY()));
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
    if (e.isButtonDown()) {
      return Mouse.BUTTON_LEFT;
    } else {
      return Mouse.BUTTON_RIGHT;
    }
  }
}
