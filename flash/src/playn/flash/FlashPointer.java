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
package playn.flash;

import flash.events.MouseEvent;
import flash.display.Sprite;

import playn.core.PlayN;
import playn.core.Pointer;

class FlashPointer implements Pointer {

  private Listener listener;
  private boolean mouseDown;

  FlashPointer() {
    // Mouse handlers.
    FlashPlatform.captureEvent(Sprite.MOUSEDOWN, new EventHandler<MouseEvent>() {
      public void handleEvent(MouseEvent nativeEvent) {
        mouseDown = true;
        if (listener != null) {
          Event.Impl event = new Event.Impl(PlayN.currentTime(), nativeEvent.getStageX(),
              nativeEvent.getStageY());
          listener.onPointerStart(event);
          if (event.getPreventDefault()) {
            nativeEvent.preventDefault();
          }
        }
      }
    });
    FlashPlatform.captureEvent(Sprite.MOUSEUP, new EventHandler<MouseEvent>() {
      public void handleEvent(MouseEvent nativeEvent) {
        mouseDown = false;
        if (listener != null) {
          Event.Impl event = new Event.Impl(PlayN.currentTime(), nativeEvent.getStageX(),
              nativeEvent.getStageY());
          listener.onPointerEnd(event);
          if (event.getPreventDefault()) {
            nativeEvent.preventDefault();
          }
        }
      }
    });
    FlashPlatform.captureEvent(Sprite.MOUSEMOVE, new EventHandler<MouseEvent>() {
      public void handleEvent(MouseEvent nativeEvent) {
        if (listener != null) {
          if (mouseDown) {
            Event.Impl event = new Event.Impl(PlayN.currentTime(), nativeEvent.getStageX(),
                nativeEvent.getStageY());
            listener.onPointerDrag(event);
            if (event.getPreventDefault()) {
              nativeEvent.preventDefault();
            }
          }
        }
      }
    });
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }
}
