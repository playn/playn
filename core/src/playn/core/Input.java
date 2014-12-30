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

/** Provides information about user input: mouse, touch, and keyboard. */
public abstract class Input {

  /** Used by {@link ButtonEvent} to indicate that the left button is pressed. */
  public static final int BUTTON_LEFT = 0;
  /** Used by {@link ButtonEvent} to indicate that the middle button is pressed. */
  public static final int BUTTON_MIDDLE = 1;
  /** Used by {@link ButtonEvent} to indicate that the right button is pressed. */
  public static final int BUTTON_RIGHT = 2;

  /** A flag indicating that the default OS behavior for an event should be prevented. */
  public static final int F_PREVENT_DEFAULT = 1 << 0;

  /** The base for all input events. */
  public static class Event {
    private int flags;

    /**
     * The time at which this event was generated, in milliseconds. This time's magnitude is not
     * portable (i.e. may not be the same across backends), clients must interpret it as only a
     * monotonically increasing value.
     */
    public final double time;

    /** Returns whether the {@code flag} bit is set. */
    public boolean isSet (int flag) {
      return (flags & flag) != 0;
    }

    /** Sets the {@code flag} bit. */
    public void setFlag (int flag) {
      flags |= flag;
    }

    /** Clears the {@code flag} bit. */
    public void clearFlag (int flag) {
      flags &= ~flag;
    }

    // TODO(mdb): a mechanism to determine which modifier keys are pressed, if any

    @Override public String toString () {
      StringBuilder builder = new StringBuilder(name()).append('[');
      addFields(builder);
      return builder.append(']').toString();
    }

    protected Event (int flags, double time) {
      this.flags = flags;
      this.time = time;
    }

    protected String name () {
      return "Event";
    }

    protected void addFields (StringBuilder builder) {
      builder.append("time=").append(time).append(", flags=").append(flags);
    }
  }

  /** The base for all events with a screen position. */
  public static class EventXY extends Event {

    /** The screen x-coordinate associated with this event. */
    public final float x;

    /** The screen y-coordinate associated with this event. */
    public final float y;

    protected EventXY (int flags, double time, float x, float y) {
      super(flags, time);
      this.x = x;
      this.y = y;
    }

    @Override protected String name () {
      return "EventXY";
    }

    @Override protected void addFields (StringBuilder builder) {
      super.addFields(builder);
      builder.append(", x=").append(x).append(", y=").append(y);
    }
  }
}
