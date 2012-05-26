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
 * Input-device interface for touch and multi-touch events if they are
 * supported.
 */
// TODO(pdr): make the (x,y) coordinates relative to a {@link Layer}, if
// specified, or the {@link Graphics#rootLayer()} otherwise.
public interface Touch {

  /** An event dispatched to report touch information. */
  interface Event extends Events.Position {
    /**
     * The id of the touch associated with this event.
     */
    int id();

    /**
     * The pressure of the touch.
     */
    // TODO(mdb): provide guidance as to range in the docs? 0 to 1?
    float pressure();

    /**
     * The size of the touch.
     */
    // TODO(mdb): provide more details in the docs? size in pixels?
    float size();

    static class Impl extends Events.Position.Impl implements Event {
      private final int id;
      private final float pressure;
      private final float size;

      // TODO: Implement pressure and size across all platforms that support touch.
      public Impl(double time, float x, float y, int id) {
        this(time, x, y, id, -1, -1);
      }

      public Impl(double time, float x, float y, int id, float pressure, float size) {
        this(time, x, y, x, y, id, pressure, size);
      }

      /** Creates a copy of this event with local x and y in the supplied layer's coord system. */
      public Event.Impl localize(Layer layer) {
        Point local = Layer.Util.screenToLayer(layer, x(), y());
        return new Event.Impl(time(), x(), y(), local.x, local.y, id(), pressure(), size());
      }

      @Override
      public int id() {
        return id;
      }

      @Override
      public float pressure() {
        return pressure;
      }

      @Override
      public float size() {
        return size;
      }

      protected Impl(double time, float x, float y, float localX, float localY,
                     int id, float pressure, float size) {
        super(time, x, y, localX, localY);
        this.id = id;
        this.pressure = pressure;
        this.size = size;
      }

      @Override
      protected String name() {
        return "Touch.Event";
      }

      @Override
      protected void addFields(StringBuilder builder) {
        super.addFields(builder);
        builder.append(", id=").append(id).append(", pressure=").append(pressure).
          append(", size=").append(size);
      }
    }
  }

  /** An interface for listening to all touch events. */
  interface Listener {
    /**
     * Called when a touch starts.
     *
     * @param touches one or more {@link Event}s.
     */
    void onTouchStart(Event[] touches);

    /**
     * Called when a touch moves (always between start/end events).
     *
     * @param touches one or more {@link Event}s.
     */
    void onTouchMove(Event[] touches);

    /**
     * Called when a touch ends.
     *
     * @param touches one or more {@link Event}s.
     */
    void onTouchEnd(Event[] touches);
  }

  /** An interface for listening to touch events that interact with a single layer.
   * See {@link Layer#addListener(Touch.LayerListener)}. */
  interface LayerListener {
    /**
     * Called when a touch starts that hits this layer.
     */
    void onTouchStart(Event touch);

    /**
     * Called when a touch, that started out hitting the listening layer, moves.
     */
    void onTouchMove(Event touch);

    /**
     * Called when a touch, that started out hitting the listening layer, ends.
     */
    void onTouchEnd(Event touch);
  }

  /**
   * A {@link Listener} implementation with NOOP stubs provided for each method.
   */
  class Adapter implements Listener {
    @Override
    public void onTouchStart(Event[] touches) { /* NOOP! */ }
    @Override
    public void onTouchMove(Event[] touches) { /* NOOP! */ }
    @Override
    public void onTouchEnd(Event[] touches) { /* NOOP! */ }
  }

  /**
   * A {@link LayerListener} implementation with NOOP stubs provided for each method.
   */
  class LayerAdapter implements LayerListener {
    @Override
    public void onTouchStart(Event touch) { /* NOOP! */ }
    @Override
    public void onTouchMove(Event touch) { /* NOOP! */ }
    @Override
    public void onTouchEnd(Event touch) { /* NOOP! */ }
  }

  /**
   * Returns true if the underlying platform supports touch interaction. If this method returns
   * false, listeners may still be registered with this service but they will never be notified.
   */
  boolean hasTouch();

  /**
   * Sets the listener that will receive touch events. Setting the listener to
   * <code>null</code> will cause touch events to stop being fired.
   */
  void setListener(Listener listener);
}
