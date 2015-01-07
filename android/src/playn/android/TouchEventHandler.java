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

import playn.core.Pointer;
import playn.core.Touch;

class TouchEventHandler {

  private static Touch.Event.Kind[] TO_KIND = new Touch.Event.Kind[16];
  static {
    TO_KIND[MotionEvent.ACTION_DOWN]         = Touch.Event.Kind.START;
    TO_KIND[MotionEvent.ACTION_UP]           = Touch.Event.Kind.END;
    TO_KIND[MotionEvent.ACTION_POINTER_DOWN] = Touch.Event.Kind.START;
    TO_KIND[MotionEvent.ACTION_POINTER_UP]   = Touch.Event.Kind.END;
    TO_KIND[MotionEvent.ACTION_MOVE]         = Touch.Event.Kind.MOVE;
    TO_KIND[MotionEvent.ACTION_CANCEL]       = Touch.Event.Kind.CANCEL;
  }

  private final AndroidPlatform plat;

  TouchEventHandler(AndroidPlatform plat) {
    this.plat = plat;
  }

  public boolean onMotionEvent(MotionEvent nativeEvent) {
    int actionType = nativeEvent.getActionMasked();
    Touch.Event.Kind kind = (actionType < TO_KIND.length) ? TO_KIND[actionType] : null;
    if (kind != null) {
      // extract the native event data while we're on the UI thread
      final Touch.Event[] touches = parseMotionEvent(nativeEvent, kind);
      // process it (issuing game callbacks) on the GL/Game thread
      plat.invokeLater(new Runnable() {
        public void run() { plat.input().touchEvents.emit(touches); }
      });
    }

    // let our caller know whether we will be handling this event
    return kind != null;
  }

  private Touch.Event[] parseMotionEvent (MotionEvent event, Touch.Event.Kind kind) {
    int actionType = event.getActionMasked();
    boolean isChanged = (actionType == MotionEvent.ACTION_POINTER_UP ||
                         actionType == MotionEvent.ACTION_POINTER_DOWN);
    int changedIdx = isChanged ? event.getActionIndex() : 0;
    int count = event.getPointerCount();
    Touch.Event[] touches = new Touch.Event[isChanged ? 1 : count];
    double time = event.getEventTime();
    int tidx = 0;
    for (int tt = 0; tt < count; tt++) {
      // if this is a pointer up/down, we only want the changed touch
      if (isChanged && tt != changedIdx) continue;
      IPoint xy = plat.graphics().transformTouch(event.getX(tt), event.getY(tt));
      float pressure = event.getPressure(tt);
      float size = event.getSize(tt);
      int id = event.getPointerId(tt);
      touches[tidx++] = new Touch.Event(0, time, xy.x(), xy.y(), kind, id, pressure, size);
    }
    return touches;
  }
}
