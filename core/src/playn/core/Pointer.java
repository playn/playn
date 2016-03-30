/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.core;

import react.Signal;
import react.Slot;

/**
 * Abstracts over {@link Mouse} and {@link Touch} input, providing a least-common-denominator API
 * which tracks a single "pointer" with simple interactions. If you want global pointer events,
 * you have to create an instance of this class yourself.
 */
public class Pointer {

  /** Contains information on a pointer event. */
  public static class Event extends playn.core.Event.XY {

    /** Enumerates the different kinds of pointer event. */
    public static enum Kind {
      START(true, false), DRAG(false, false), END(false, true), CANCEL(false, true);

      /** Whether this kind starts or ends an interaction. */
      public final boolean isStart, isEnd;

      Kind (boolean isStart, boolean isEnd) {
        this.isStart = isStart;
        this.isEnd = isEnd;
      }
    };
    // NOTE: this enum must match Touch.Event.Kind exactly

    /** Whether this event represents a start, move, etc. */
    public final Kind kind;

    /** Whether this event originated from a touch event. */
    public boolean isTouch;

    public Event (int flags, double time, float x, float y, Kind kind, boolean isTouch) {
      super(flags, time, x, y);
      this.kind = kind;
      this.isTouch = isTouch;
    }

    @Override protected String name () {
      return "Pointer";
    }

    @Override protected void addFields (StringBuilder builder) {
      super.addFields(builder);
      builder.append(", kind=").append(kind);
      builder.append(", touch=").append(isTouch);
    }
  }

  /** Allows pointer interaction to be temporarily disabled.
    * No pointer events will be dispatched whilst this big switch is in the off position. */
  public boolean enabled = true;

  /** A signal which emits pointer events. */
  public Signal<Event> events = Signal.create();

  public Pointer (Platform plat) {
    this.plat = plat;

    // if this platform supports touch events, use those
    if (plat.input().hasTouch()) {
      plat.input().touchEvents.connect(new Slot<Touch.Event[]>() {
        private int active = -1;
        @Override public void onEmit (Touch.Event[] events) {
          for (Touch.Event event : events) {
            if (active == -1 && event.kind.isStart) active = event.id;
            if (event.id == active) {
              forward(Event.Kind.values()[event.kind.ordinal()], true, event);
              if (event.kind.isEnd) active = -1;
            }
          }
        }
      });
    }
    // otherwise use mouse events if it has those
    else if (plat.input().hasMouse()) {
      plat.input().mouseEvents.connect(new Slot<Mouse.Event>() {
        private boolean dragging;
        @Override public void onEmit (Mouse.Event event) {
          if (event instanceof Mouse.MotionEvent) {
            if (dragging) forward(Event.Kind.DRAG, false, event);
          } else if (event instanceof Mouse.ButtonEvent) {
            Mouse.ButtonEvent bevent = (Mouse.ButtonEvent)event;
            if (bevent.button == Mouse.ButtonEvent.Id.LEFT) {
              dragging = bevent.down;
              forward(bevent.down ? Event.Kind.START : Event.Kind.END, false, bevent);
            }
          }
        }
      });
    }
    // otherwise complain because what's going on?
    else plat.log().warn("Platform has neither mouse nor touch events?", "type", plat.type());
  }

  protected void forward (Event.Kind kind, boolean isTouch, playn.core.Event.XY source) {
    if (!enabled || !events.hasConnections()) return;
    Event event = new Event(source.flags, source.time, source.x, source.y, kind, isTouch);
    plat.dispatchEvent(events, event);
    // TODO: propagate prevent default back to original event
  }

  private Platform plat;
}
