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
package playn.android;

import playn.core.Graphics;
import playn.core.Pointer;
import playn.core.Touch;
import android.view.MotionEvent;

/**
 * Class for taking MotionEvents from GameActivity.onMotionEvent() and parsing
 * them into an array of Touch.Events for the Listener.
 */
class AndroidTouchEventHandler {
  private final GameViewGL gameView;
  private float xScreenOffset = 0;
  private float yScreenOffset = 0;

  AndroidTouchEventHandler(GameViewGL gameView) {
    this.gameView = gameView;
  }

  /**
   * Default Android touch behavior. Parses the immediate MotionEvent and passes
   * it to the correct methods in {@GameViewGL} for processing
   * on the GL render thread. Ignores historical values.
   */
  public boolean onMotionEvent(MotionEvent event) {
    double time = event.getEventTime();
    int action = event.getAction();
    Touch.Event[] touches = parseMotionEvent(event);
    Touch.Event pointerEvent = touches[0];
    switch (action) {
      case (MotionEvent.ACTION_DOWN):
        gameView.onTouchStart(touches);
        gameView.onPointerStart(new Pointer.Event.Impl(time, pointerEvent.x(), pointerEvent.y()));
        break;
      case (MotionEvent.ACTION_UP):
        gameView.onTouchEnd(touches);
      gameView.onPointerEnd(new Pointer.Event.Impl(time, pointerEvent.x(), pointerEvent.y()));
        break;
      case (MotionEvent.ACTION_MOVE):
        gameView.onTouchMove(touches);
        gameView.onPointerDrag(new Pointer.Event.Impl(time, pointerEvent.x(), pointerEvent.y()));
        break;
      case (MotionEvent.ACTION_CANCEL):
        break;
      case (MotionEvent.ACTION_OUTSIDE):
        break;
    }
    return true;
  }

  /**
   * Performs the actual parsing of the MotionEvent event.
   *
   * @param event The MotionEvent to process
   * @param historical Whether or not to parse historical touches (currently
   *          never called as true, but the functionality is still here in case
   *          this feature is ever added to other platforms)
   * @return The processed array of individual AndroidTouchEvents.
   */
  private Touch.Event[] parseMotionEvent(MotionEvent event) {
    calculateOffsets();
    int eventPointerCount = event.getPointerCount();
    Touch.Event[] touches = new Touch.Event[eventPointerCount];
    double time = event.getEventTime();
    float x, y, pressure, size;
    int id;
    for (int t = 0; t < eventPointerCount; t++) {
      int pointerIndex = t;
      x = event.getX(pointerIndex) + xScreenOffset;
      y = event.getY(pointerIndex) + yScreenOffset;
      pressure = event.getPressure(pointerIndex);
      size = event.getSize(pointerIndex);
      id = event.getPointerId(pointerIndex);
      touches[t] = new Touch.Event.Impl(time, x, y, id, pressure, size);
    }
    return touches;
  }

  public void calculateOffsets() {
    Graphics graphics = AndroidPlatform.instance.graphics();
    xScreenOffset = -(graphics.screenWidth() - graphics.width()) / 2;
    yScreenOffset = -(graphics.screenHeight() - graphics.height()) / 2;
  }

}
