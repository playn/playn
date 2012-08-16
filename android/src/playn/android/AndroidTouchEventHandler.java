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

import android.view.MotionEvent;

import pythagoras.f.IPoint;

import playn.core.Events;
import playn.core.Pointer;
import playn.core.Touch;

/**
 * Class for taking MotionEvents from GameActivity.onMotionEvent() and parsing
 * them into an array of Touch.Events for the Listener.
 */
class AndroidTouchEventHandler {

  private final AndroidGraphics graphics;
  private final GameViewGL gameView;

  AndroidTouchEventHandler(AndroidGraphics graphics, GameViewGL gameView) {
    this.graphics = graphics;
    this.gameView = gameView;
  }

  /**
   * Default Android touch behavior. Parses the immediate MotionEvent and passes
   * it to the correct methods in {@GameViewGL} for processing
   * on the GL render thread. Ignores historical values.
   */
  public boolean onMotionEvent(MotionEvent nativeEvent) {
    double time = nativeEvent.getEventTime();
    int action = nativeEvent.getAction();
    Events.Flags flags = new Events.Flags.Impl();

    Touch.Event.Impl[] touches = parseMotionEvent(nativeEvent, flags);
    Touch.Event pointerEvent = touches[0];

    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:
        gameView.onTouchStart(touches);
        gameView.onPointerStart(new Pointer.Event.Impl(
          flags, time, pointerEvent.x(), pointerEvent.y(), true));
        break;
      case MotionEvent.ACTION_UP:
        gameView.onTouchEnd(touches);
        gameView.onPointerEnd(new Pointer.Event.Impl(
          flags, time, pointerEvent.x(), pointerEvent.y(), true));
        break;
      case MotionEvent.ACTION_POINTER_DOWN:
        gameView.onTouchStart(getChangedTouches(action, touches));
        break;
      case MotionEvent.ACTION_POINTER_UP:
        gameView.onTouchEnd(getChangedTouches(action, touches));
        break;
      case MotionEvent.ACTION_MOVE:
        gameView.onTouchMove(touches);
        gameView.onPointerDrag(new Pointer.Event.Impl(
          flags, time, pointerEvent.x(), pointerEvent.y(), true));
        break;
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_OUTSIDE:
        return false;
    }
    return flags.getPreventDefault();
  }

  private Touch.Event.Impl[] getChangedTouches(int action, Touch.Event.Impl[] touches) {
    int changed = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
      >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    return new Touch.Event.Impl[] { touches[changed] };
  }

  /**
   * Performs the actual parsing of the MotionEvent event.
   *
   * @param event The MotionEvent to process
   * @param preventDefault Shared preventDefault state among returned {@link AndroidTouchEventImpl}
   * @return Processed array of {@link AndroidTouchEventImpl}s which share a preventDefault state.
   */
  private Touch.Event.Impl[] parseMotionEvent(MotionEvent event, Events.Flags flags) {
    int eventPointerCount = event.getPointerCount();
    Touch.Event.Impl[] touches = new Touch.Event.Impl[eventPointerCount];
    double time = event.getEventTime();
    float pressure, size;
    int id;
    for (int t = 0; t < eventPointerCount; t++) {
      int pointerIndex = t;
      IPoint xy = graphics.transformTouch(event.getX(pointerIndex), event.getY(pointerIndex));
      pressure = event.getPressure(pointerIndex);
      size = event.getSize(pointerIndex);
      id = event.getPointerId(pointerIndex);
      touches[t] = new Touch.Event.Impl(flags, time, xy.x(), xy.y(), id, pressure, size);
    }
    return touches;
  }
}
