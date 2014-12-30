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
package playn.core;

import react.Signal;

/**
 * Input-device interface for touch and multi-touch events.
 */
public class Touch {

  /** Contains information on a touch event. */
  public static class Event extends playn.core.Event.XY {

    /** Enumerates the different kinds of touch event. */
    public static enum Kind { START, MOVE, END, CANCEL };
    // NOTE: this enum must match Pointer.Event.Kind exactly

    /** Whether this event represents a start, move, etc. */
    public final Kind kind;

    /** The id of the touch associated with this event. */
    public final int id;

    /** The pressure of the touch. */
    public final float pressure;
    // TODO(mdb): provide guidance as to range in the docs? 0 to 1?

    /** The size of the touch. */
    public final float size;
    // TODO(mdb): provide more details in the docs? size in pixels?

    // TODO: Implement pressure and size across all platforms that support touch.
    public Event (int flags, double time, float x, float y, Kind kind, int id) {
      this(flags, time, x, y, kind, id, -1, -1);
    }

    public Event (int flags, double time, float x, float y, Kind kind, int id,
                  float pressure, float size) {
      super(flags, time, x, y);
      this.kind = kind;
      this.id = id;
      this.pressure = pressure;
      this.size = size;
    }

    @Override protected String name () {
      return "Touch";
    }

    @Override protected void addFields (StringBuilder builder) {
      super.addFields(builder);
      builder.append(", kind=").append(kind).append(", kind=").append(id).
        append(", pressure=").append(pressure).append(", size=").append(size);
    }
  }

  /** A signal via which touch events are emitted. */
  public Signal<Event[]> events = Signal.create();

  /**
   * Returns true if the underlying platform supports touch interaction. If this method returns
   * false, listeners may still be registered with this service but they will never be notified.
   */
  public boolean isSupported () {
    return false;
  }

  /**
   * Returns true if touch interaction is enabled, false if not. Interaction is enabled by default.
   * See {@link #setEnabled}.
   */
  public boolean isEnabled () {
    return enabled;
  }

  /**
   * Allows touch interaction to be temporarily disabled. No touch events will be dispatched whilst
   * this big switch is in the off position.
   */
  public void setEnabled (boolean enabled) {
    this.enabled = enabled;
  }

  private boolean enabled = true;
}
