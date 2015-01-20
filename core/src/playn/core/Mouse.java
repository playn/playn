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

import react.Slot;

/**
 * Defines and dispatches mouse events.
 */
public class Mouse {

  /** The base class for all mouse events. */
  public static class Event extends playn.core.Event.XY {

    protected Event (int flags, double time, float x, float y) {
      super(flags, time, x, y);
    }
  }

  /** The event dispatched for mouse input. */
  public static class ButtonEvent extends Event {

    /** Enumerates the supported mouse buttons. */
    public static enum Id { LEFT, RIGHT, MIDDLE, X1, X2 }

    /** The id of the button associated with this event. */
    public final Id button;

    /** True if the button was just pressed, false if it was just released. */
    public boolean down;

    public ButtonEvent (int flags, double time, float x, float y, Id button, boolean down) {
      super(flags, time, x, y);
      this.button = button;
      this.down = down;
    }

    @Override protected String name () {
      return "Button";
    }

    @Override protected void addFields (StringBuilder builder) {
      super.addFields(builder);
      builder.append(", id=").append(button).append(", down=").append(down);
    }
  }

  /** An event dispatched when the mouse is moved. */
  public static class MotionEvent extends Event {

    /** The amount by which the mouse moved on the x axis. */
    public final float dx;

    /** The amount by which the mouse moved on the y axis. */
    public final float dy;

    public MotionEvent (int flags, double time, float x, float y, float dx, float dy) {
      super(flags, time, x, y);
      this.dx = dx;
      this.dy = dy;
    }

    @Override protected String name () {
      return "MotionEvent";
    }

    @Override protected void addFields (StringBuilder builder) {
      super.addFields(builder);
      builder.append(", dx=").append(dx).append(", dy=").append(dy);
    }
  }

  /** An event dispatched when the mouse wheel is scrolled. */
  public static class WheelEvent extends Event {

    /** The velocity of the scroll wheel. Negative velocity corresponds to scrolling north/up. Each
      * scroll 'click' is 1 velocity. */
    public final float velocity;

    public WheelEvent (int flags, double time, float x, float y, float velocity) {
      super(flags, time, x, y);
      this.velocity = velocity;
    }

    @Override protected String name () {
      return "Wheel";
    }

    @Override protected void addFields (StringBuilder builder) {
      super.addFields(builder);
      builder.append(", velocity=").append(velocity);
    }
  }

  /** A slot which only dispatches on {@link ButtonEvent}s. */
  public static abstract class ButtonSlot extends Slot<Event> {
    public void onEmit (Event event) {
      if (event instanceof ButtonEvent) onEmit((ButtonEvent)event);
    }
    public abstract void onEmit (ButtonEvent event);
  }

  /** A slot which only dispatches on {@link MotionEvent}s. */
  public static abstract class MotionSlot extends Slot<Event> {
    public void onEmit (Event event) {
      if (event instanceof MotionEvent) onEmit((MotionEvent)event);
    }
    public abstract void onEmit (MotionEvent event);
  }

  /** A slot which only dispatches on {@link WheelEvent}s. */
  public static abstract class WheelSlot extends Slot<Event> {
    public void onEmit (Event event) {
      if (event instanceof WheelEvent) onEmit((WheelEvent)event);
    }
    public abstract void onEmit (WheelEvent event);
  }
}
