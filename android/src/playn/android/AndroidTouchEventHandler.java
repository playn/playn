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
   * Special implementation of Touch.Event.Impl for keeping track of changes to preventDefault
   */
  static class AndroidTouchEventImpl extends Touch.Event.Impl {
    final boolean[] preventDefault;

    public AndroidTouchEventImpl(double time, float x, float y, int id, boolean[] preventDefault) {
      super(time, x, y, id);
      this.preventDefault = preventDefault;
    }

    public AndroidTouchEventImpl(double time, float x, float y, int id, float pressure, float size,
        boolean[] preventDefault) {
      super(time, x, y, id, pressure, size);
      this.preventDefault = preventDefault;
    }

    @Override
    public void setPreventDefault(boolean preventDefault) {
      this.preventDefault[0] = preventDefault;
    }

    @Override
    public boolean getPreventDefault() {
      return preventDefault[0];
    }
  }

  /**
   * Default Android touch behavior. Parses the immediate MotionEvent and passes
   * it to the correct methods in {@GameViewGL} for processing
   * on the GL render thread. Ignores historical values.
   */
  public boolean onMotionEvent(MotionEvent nativeEvent) {
    double time = nativeEvent.getEventTime();
    int action = nativeEvent.getAction();
    boolean[] preventDefault = {false};

    Touch.Event[] touches = parseMotionEvent(nativeEvent, preventDefault);
    Touch.Event pointerEvent = touches[0];
    Pointer.Event.Impl event;

    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:
        gameView.onTouchStart(touches);
        event = new Pointer.Event.Impl(time, pointerEvent.x(), pointerEvent.y(), true);
        gameView.onPointerStart(event);
        return (preventDefault[0] || event.getPreventDefault());
      case MotionEvent.ACTION_UP:
        gameView.onTouchEnd(touches);
        event = new Pointer.Event.Impl(time, pointerEvent.x(), pointerEvent.y(), true);
        gameView.onPointerEnd(event);
        return (preventDefault[0] || event.getPreventDefault());
      case MotionEvent.ACTION_POINTER_DOWN:
        gameView.onTouchStart(getChangedTouches(action, touches));
        return preventDefault[0];
      case MotionEvent.ACTION_POINTER_UP:
        gameView.onTouchEnd(getChangedTouches(action, touches));
        return preventDefault[0];
      case MotionEvent.ACTION_MOVE:
        gameView.onTouchMove(touches);
        event = new Pointer.Event.Impl(time, pointerEvent.x(), pointerEvent.y(), true);
        gameView.onPointerDrag(event);
        return (preventDefault[0] || event.getPreventDefault());
      case MotionEvent.ACTION_CANCEL:
        break;
      case MotionEvent.ACTION_OUTSIDE:
        break;
    }
    return false;
  }

  private Touch.Event[] getChangedTouches(int action, Touch.Event[] touches) {
    int changed = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
      >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    return new Touch.Event[] { touches[changed] };
  }

  /**
   * Performs the actual parsing of the MotionEvent event.
   *
   * @param event The MotionEvent to process
   * @param preventDefault Shared preventDefault state among returned {@link AndroidTouchEventImpl}
   * @return Processed array of {@link AndroidTouchEventImpl}s which share a preventDefault state.
   */
  private Touch.Event[] parseMotionEvent(MotionEvent event, boolean[] preventDefault) {
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
      touches[t] = new AndroidTouchEventImpl(time, x, y, id, pressure, size, preventDefault);
    }
    return touches;
  }

  void calculateOffsets(AndroidGraphics graphics) {
    xScreenOffset = -(graphics.screenWidth() - graphics.width()) / 2;
    yScreenOffset = -(graphics.screenHeight() - graphics.height()) / 2;
  }
}
