/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import playn.core.*;
import pythagoras.f.Point;
import react.Slot;

/** Implements touches using a mouse and keyboard events (for testing), if so configured.
  * TODO: show multitouch points on screen
  * TODO: allow pivot slide */
public class JavaTouch extends Touch {

  private final JavaPlatform plat;
  private boolean mouseDown;
  private Point pivot;
  private float x, y;
  private int currentId;

  public JavaTouch (JavaPlatform plat, Keyboard keyboard, Mouse mouse) {
    this.plat = plat;
    // if touch emulation is not configured, stop now and do nothing
    if (!plat.config.emulateTouch) return;

    final Key pivotKey = plat.config.pivotKey;
    keyboard.events.connect(new Slot<Keyboard.Event>() {
      public void onEmit (Keyboard.Event event) {
        if (event instanceof Keyboard.KeyEvent) {
          Keyboard.KeyEvent kevent = (Keyboard.KeyEvent)event;
          if (kevent.key == pivotKey && kevent.down) {
            pivot = new Point(x, y);
          }
        }
      }
    });

    mouse.events.connect(new Slot<Mouse.Event>() {
      public void onEmit (Mouse.Event event) {
        if (event instanceof Mouse.ButtonEvent) {
          Mouse.ButtonEvent bevent = (Mouse.ButtonEvent)event;
          if (bevent.button == Mouse.ButtonEvent.Id.LEFT) {
            if (mouseDown = bevent.down) {
              currentId += 2; // skip an id in case of pivot
              dispatch(event, Event.Kind.START);
            } else {
              pivot = null;
              dispatch(event, Event.Kind.END);
            }
          }
        } else if (event instanceof Mouse.MotionEvent) {
          if (mouseDown) dispatch(event, Event.Kind.MOVE);
          // keep track of the current mouse position for pivot
          x = event.x; y = event.y;
        }
      }
    });
  }

  @Override public boolean isSupported() {
    return true;
  }

  private void dispatch (Mouse.Event event, Event.Kind kind) {
    Event main = toTouch(event.time, event.x, event.y, kind, 0);
    Event[] evs = (pivot == null) ?
      new Event[] { main } :
      new Event[] { main, toTouch(event.time, 2*pivot.x-event.x, 2*pivot.y-event.y, kind, 1) };
    events.emit(evs);
  }

  private Event toTouch (double time, float x, float y, Event.Kind kind, int idoff) {
    return new Event(0, time, x, y, kind, currentId+idoff);
  }
}
