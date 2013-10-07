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

import pythagoras.f.Point;

/**
 * Defines some shared events.
 */
public class Events {

  private static Point scratchPoint = new Point();

  /** Defines some information for how event processing may be controlled. One instance may
   * be shared among multiple events.
   * TODO: better name than flags? ProcessControls is more accurate but too long.
   */
  public interface Flags {
    /**
     * Return whether the default action normally taken by the platform will be prevented.
     */
    boolean getPreventDefault();

    /**
     * Set whether the default action normally taken by the platform as a result of the event should
     * be performed. By default, the default action is not prevented.
     * <p>
     * For example, pressing the down key in a browser typically scrolls the window. Calling
     * {@code setPreventDefault(true)} prevents this action.
     * <p>
     * Note: this must be set from inside the event handler callback (e.g., onKeyUp()). If it is
     * called after the callback has returned, setPreventDefault will have no effect.
     */
    void setPreventDefault(boolean preventDefault);

    /**
     * Returns true if the event is eligible to also dispatch to parents of the current layer. If
     * this method returns true and the platform has propagation enabled, then the layer's parent's
     * listeners will also be notified. Otherwise, the event processing will exit after the current
     * layer's listeners have all been notified.
     * @see Platform#setPropagateEvents(boolean)
     */
    boolean getPropagationStopped();

    /**
     * Sets whether the event is eligible to also dispatch to parents of the current layer.
     * @see #getPropagationStopped()
     */
    void setPropagationStopped(boolean stopped);

    public static class Impl implements Flags {
      private boolean preventDefault, stopped;

      @Override
      public boolean getPreventDefault () {
        return preventDefault;
      }

      @Override
      public void setPreventDefault (boolean preventDefault) {
        this.preventDefault = preventDefault;
      }

      @Override
      public boolean getPropagationStopped() {
        return stopped;
      }

      @Override
      public void setPropagationStopped(boolean stopped) {
        this.stopped = stopped;
      }

      @Override
      public String toString() {
        return preventDefault ? "preventDefault" : "normal";
      }
    }
  }

  /** The base for all input events. */
  public interface Input {
    /** The flags that control the processing of this event. */
    Flags flags();

    /**
     * The time at which this event was generated, in milliseconds. This time's magnitude is not
     * portable (i.e. may not be the same across backends), clients must interpret it as only a
     * monotonically increasing value.
     */
    double time();

    /**
     * Where appropriate, causes all subsequent events in the current touch, point or mousedown to
     * be sent to the current listener. Any other layer listeners that have an outstanding "start"
     * will be cancelled.
     *
     * <p>This is only valid during dispatch of {@link Pointer.Listener} start and drag events,
     * {@link Mouse.LayerListener} down and drag events, and {@link Touch.Listener} start and move
     * events. Calls at other times will be ignored.</p>
     *
     * <p>The classic use case for this is if some game element is scrollable and yet contains an
     * element that is pressable. After measuring user intent, the scrollable listener can capture
     * the event stream and thereby cancel the pressable's listener.</p>
     *
     * <p>NOTE: this is only meaningful if event propagation is enabled for the platform ({@link
     * Platform#setPropagateEvents}). If event propagation is not enabled, there will only ever be
     * a single layer listener involved in an interaction.</p>
     */
    void capture();

    // TODO(mdb): a mechanism to determine which modifier keys are pressed, if any

    class Impl implements Input {
      private final Flags flags;
      private final double time;
      Dispatcher.CaptureState captureState;

      /** Creates a copy of this event with local x and y in the supplied layer's coord system and
       * flags inherited from this event. */
      Input.Impl localize(Layer hit) {
        return this;
      }

      @Override
      public double time() {
        return time;
      }

      @Override
      public Flags flags () {
        return flags;
      }

      protected Impl(Flags flags, double time) {
        this.flags = flags;
        this.time = time;
      }

      protected String name() {
        return "Events.Input";
      }

      @Override
      public String toString() {
        StringBuilder builder = new StringBuilder(name()).append('[');
        addFields(builder);
        return builder.append(']').toString();
      }

      protected void addFields(StringBuilder builder) {
        builder.append("time=").append(time).append(", flags=").append(flags);
      }

      public void capture () {
        if (captureState != null)
          captureState.capture();
      }
    }
  }

  /** The base for all events with a screen position. */
  public interface Position extends Input {
    /**
     * The screen x-coordinate associated with this event.
     */
    float x();

    /**
     * The screen y-coordinate associated with this event.
     */
    float y();

    /**
     * The x-coordinate associated with this event transformed into the receiving layer's
     * coordinate system. See {@link Layer#addListener}, etc.
     */
    float localX();

    /**
     * The y-coordinate associated with this event transformed into the receiving layer's
     * coordinate system. See {@link Layer#addListener}, etc.
     */
    float localY();

    /**
     * The layer that was hit when generating this event, or null if the event is currently
     * being processed by the global listener.
     */
    Layer hit();

    abstract class Impl extends Input.Impl implements Position {
      private final Layer hit;
      private final float x, y, localX, localY;

      @Override
      abstract Input.Impl localize(Layer hit);

      @Override
      public float x() {
        return x;
      }

      @Override
      public float y() {
        return y;
      }

      @Override
      public float localX() {
        return localX;
      }

      @Override
      public float localY() {
        return localY;
      }

      @Override
      public Layer hit() {
        return hit;
      }

      protected Impl(Flags flags, double time, float x, float y) {
        this(null, flags, time, x, y);
      }

      protected Impl(Layer hit, Flags flags, double time, float x, float y) {
        super(flags, time);
        this.hit = hit;
        this.x = x;
        this.y = y;
        if (hit == null) {
          this.localX = x;
          this.localY = y;
        } else {
          Layer.Util.screenToLayer(hit, scratchPoint.set(x, y), scratchPoint);
          this.localX = scratchPoint.x;
          this.localY = scratchPoint.y;
        }
      }

      @Override
      protected String name() {
        return "Events.Position";
      }

      @Override
      protected void addFields(StringBuilder builder) {
        super.addFields(builder);
        builder.append(", x=").append(x).append(", y=").append(y).append(", hit=").append(hit);
      }
    }
  }

  public static class Util {

    public static Point screenPos (Position ev) {
      return screenPos(ev, new Point());
    }

    public static Point screenPos (Position ev, Point dest) {
      dest.set(ev.x(),  ev.y());
      return dest;
    }

    public static Point localPos (Position ev) {
      return localPos(ev, new Point());
    }

    public static Point localPos (Position ev, Point dest) {
      dest.set(ev.localX(),  ev.localY());
      return dest;
    }
  }
}
