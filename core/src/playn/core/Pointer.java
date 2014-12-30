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
 * which tracks a single "pointer" with simple interactions.
 */
public class Pointer {

  /** Contains information on a pointer event. */
  public static class Event extends playn.core.Event.XY {

    /** Enumerates the different kinds of pointer event. */
    public static enum Kind { START, MOVE, END, CANCEL };
    // NOTE: this enum must match Touch.Event.Kind exactly

    /** Whether this event represents a start, move, etc. */
    public final Kind kind;

    public Event (int flags, double time, float x, float y, Kind kind) {
      super(flags, time, x, y);
      this.kind = kind;
    }

    @Override protected String name () {
      return "Pointer";
    }

    @Override protected void addFields (StringBuilder builder) {
      super.addFields(builder);
      builder.append(", kind=").append(kind);
    }
  }

  private final Platform plat;
  private boolean enabled = true;

  public Pointer (Platform plat) {
    this.plat = plat;

    // listen for mouse events and convert them to pointer events
    plat.mouse().events.connect(new Slot<Mouse.Event>() {
      private boolean dragging;
      @Override public void onEmit (Mouse.Event event) {
        if (event instanceof Mouse.MotionEvent) {
          if (dragging) forward(Event.Kind.MOVE, event);
        } else if (event instanceof Mouse.ButtonEvent) {
          Mouse.ButtonEvent bevent = (Mouse.ButtonEvent)event;
          if (bevent.button == Mouse.ButtonEvent.Id.LEFT) {
            dragging = bevent.down;
            forward(bevent.down ? Event.Kind.START : Event.Kind.END, bevent);
          }
        }
      }
    });

    // listen for touch events and convert them to pointer events
    plat.touch().events.connect(new Slot<Touch.Event[]>() {
      private int active = -1;
      @Override public void onEmit (Touch.Event[] events) {
        for (Touch.Event event : events) {
          if (active == -1 || event.id == active) {
            active = event.id;
            forward(Event.Kind.values()[event.kind.ordinal()], event);
          }
        }
      }
    });
  }

  /** A signal which emits pointer events. */
  public Signal<Event> events = Signal.create();

  /**
   * Returns true if pointer interaction is enabled, false if not. Interaction is enabled by
   * default. See {@link #setEnabled}.
   */
  public boolean isEnabled () {
    return enabled;
  }

  /**
   * Allows pointer interaction to be temporarily disabled. No pointer events will be dispatched
   * whilst this big switch is in the off position.
   */
  public void setEnabled (boolean enabled) {
    this.enabled = enabled;
  }

  protected void forward (Event.Kind kind, playn.core.Event.XY source) {
    if (!enabled || !events.hasConnections()) return;
    events.emit(new Event(source.flags, source.time, source.x, source.y, kind));
    // TODO: propagate prevent default back to original event
  }
}
