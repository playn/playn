/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.flash;

import flash.events.MouseEvent;
import flash.display.Sprite;

import forplay.core.Pointer;

class FlashPointer implements Pointer {

  private Listener listener;
  private boolean mouseDown;

  FlashPointer() {
    // Mouse handlers.
    FlashPlatform.captureEvent(Sprite.MOUSEDOWN, new EventHandler<MouseEvent>() {
      public void handleEvent(MouseEvent evt) {
        evt.preventDefault();  
        mouseDown = true;
        if (listener != null) {
          listener.onPointerStart(evt.getStageX(), evt.getStageY());
        }
      }
    });
    FlashPlatform.captureEvent(Sprite.MOUSEUP, new EventHandler<MouseEvent>() {
      public void handleEvent(MouseEvent evt) {
        mouseDown = false;
        if (listener != null) {
          listener.onPointerEnd(evt.getStageX(), evt.getStageY());
        }
      }
    });
    FlashPlatform.captureEvent(Sprite.MOUSEMOVE, new EventHandler<MouseEvent>() {
      public void handleEvent(MouseEvent evt) {
        if (listener != null) {
          if (mouseDown) {
            listener.onPointerDrag(evt.getStageX(), evt.getStageY());
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
