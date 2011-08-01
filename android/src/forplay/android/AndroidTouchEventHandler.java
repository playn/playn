/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.android;

import android.view.MotionEvent;
import forplay.core.Pointer;
import forplay.core.Touch;

/**
 * Class for taking MotionEvents from GameActivity.onMotionEvent() and parsing
 * them into an array of Touch.Events for the Listener.
 */
class AndroidTouchEventHandler {
  boolean inTouchSequence = false;

  /**
   * Default Android touch behavior. Parses the immediate MotionEvent and passes
   * it to listener and the appropriate method in
   * AndroidPlatform.instance().pointer(). Ignores historical values.
   */
  public boolean onMotionEvent(MotionEvent event) {
    AndroidPointer pointer = AndroidPlatform.instance.pointer();
    AndroidTouch touch = (AndroidTouch) AndroidPlatform.instance.touch();
    double time = event.getEventTime();
    int action = event.getAction();
    Touch.Event[] touches = parseMotionEvent(event);
    Touch.Event pointerEvent = touches[0];
    switch (action) {
      case (MotionEvent.ACTION_DOWN):
        inTouchSequence = true;
        touch.onTouchStart(touches);
        pointer.onPointerStart(new Pointer.Event.Impl(time, pointerEvent.x(), pointerEvent.y()));
        break;
      case (MotionEvent.ACTION_UP):
        inTouchSequence = false;
        touch.onTouchEnd(touches);
        pointer.onPointerEnd(new Pointer.Event.Impl(time, pointerEvent.x(), pointerEvent.y()));
        break;
      case (MotionEvent.ACTION_MOVE):
        touch.onTouchMove(touches);
        pointer.onPointerMove(new Pointer.Event.Impl(time, pointerEvent.x(), pointerEvent.y()));
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
    int eventPointerCount = event.getPointerCount();
    Touch.Event[] touches = new Touch.Event[eventPointerCount];
    double time = event.getEventTime();
    float x, y, pressure, size;
    int id;
    for (int t = 0; t < eventPointerCount; t++) {
      int pointerIndex = t;
      x = event.getX(pointerIndex);
      y = event.getY(pointerIndex);
      pressure = event.getPressure(pointerIndex);
      size = event.getSize(pointerIndex);
      id = event.getPointerId(pointerIndex);
      touches[t] = new Touch.Event.Impl(time, x, y, id, pressure, size);
    }
    return touches;
  }
}
