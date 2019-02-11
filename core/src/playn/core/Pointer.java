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
      class TouchConverter {
        private int active = -1;
        public Slot<Touch.Event[]> onTouch = new Slot<Touch.Event[]>() {
          @Override public void onEmit (Touch.Event[] events) {
            for (Touch.Event event : events) {
              if (active == -1 && event.kind.isStart) active = event.id;
              if (event.id == active) {
                forward(event, Event.Kind.values()[event.kind.ordinal()], true);
                if (event.kind.isEnd) active = -1;
              }
            }
          }
        };
        // if the app loses focus, cancel any in progress pointer interaction
        public Slot<Boolean> onFocus = new Slot<Boolean>() {
          @Override public void onEmit (Boolean focus) {
            if (!focus && active != -1) {
              forward(0, 0L, 0, 0, Event.Kind.CANCEL, true);
              active = -1;
            }
          }
        };
      }
      TouchConverter tc = new TouchConverter();
      plat.input().touchEvents.connect(tc.onTouch);
      plat.input().focus.connect(tc.onFocus);
    }
    // otherwise use mouse events if it has those
    else if (plat.input().hasMouse()) {
      class MouseConverter {
        private boolean dragging;
        public Slot<Mouse.Event> onMouse = new Slot<Mouse.Event>() {
          @Override public void onEmit (Mouse.Event event) {
            if (event instanceof Mouse.MotionEvent) {
              if (dragging) forward(event, Event.Kind.DRAG, false);
            } else if (event instanceof Mouse.ButtonEvent) {
              Mouse.ButtonEvent bevent = (Mouse.ButtonEvent)event;
              if (bevent.button == Mouse.ButtonEvent.Id.LEFT) {
                dragging = bevent.down;
                forward(bevent, bevent.down ? Event.Kind.START : Event.Kind.END, false);
              }
            }
          }
        };
        // if the app loses focus, cancel any in progress pointer interaction
        public Slot<Boolean> onFocus = new Slot<Boolean>() {
          @Override public void onEmit (Boolean focus) {
            if (!focus && dragging) {
              forward(0, 0L, 0, 0, Event.Kind.CANCEL, true);
              dragging = false;
            }
          }
        };
      }
      MouseConverter mc = new MouseConverter();
      plat.input().mouseEvents.connect(mc.onMouse);
      plat.input().focus.connect(mc.onFocus);
    }
    // otherwise complain because what's going on?
    else plat.log().warn("Platform has neither mouse nor touch events?", "type", plat.type());
  }

  protected void forward (playn.core.Event.XY source, Event.Kind kind, boolean isTouch) {
    forward(source.flags, source.time, source.x, source.y, kind, isTouch);
    // TODO: propagate prevent default back to original event?
  }

  protected void forward (int flags, double time, float x, float y,
                          Event.Kind kind, boolean isTouch) {
    if (enabled && events.hasConnections()) {
      Event event = new Event(flags, time, x, y, kind, isTouch);
      plat.dispatchEvent(events, event);
    }
  }

  private Platform plat;
}
