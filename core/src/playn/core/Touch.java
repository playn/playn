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

/**
 * Input-device interface for touch and multi-touch events if they are supported.
 */
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
      public Impl(Events.Flags flags, double time, float x, float y, int id) {
        this(flags, time, x, y, id, -1, -1);
      }

      public Impl(Events.Flags flags, double time, float x, float y, int id,
                  float pressure, float size) {
        this(null, flags, time, x, y, id, pressure, size);
      }

      @Override
      public Event.Impl localize(Layer hit) {
        return new Event.Impl(hit, flags(), time(), x(), y(), id(), pressure(), size());
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

      protected Impl(Layer hit, Events.Flags flags, double time, float x, float y,
                     int id, float pressure, float size) {
        super(hit, flags, time, x, y);
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

    /**
     * Called when a touch, that started out hitting the listening layer, is canceled. This happens
     * when the OS cancels one or more active touches.
     */
    void onTouchCancel(Event[] touches);
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

    /**
     * Called when a touch, that started out hitting the listening layer, is canceled. This happens
     * when {@link #cancelLayerTouches} is called, or it can be initiated by the OS. <em>Note:</em>
     * the coordinates of this event will be (0, 0) if initiated by {@link #cancelLayerTouches}.
     */
    void onTouchCancel(Event touch);
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
    @Override
    public void onTouchCancel(Event[] touch) { /* NOOP! */ }
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
    @Override
    public void onTouchCancel(Event touch) { /* NOOP! */ }
  }

  /**
   * Returns true if the underlying platform supports touch interaction. If this method returns
   * false, listeners may still be registered with this service but they will never be notified.
   */
  boolean hasTouch();

  /**
   * Returns true if touch interaction is enabled, false if not. Interaction is enabled by default.
   * See {@link #setEnabled}.
   */
  boolean isEnabled();

  /**
   * Allows touch interaction to be temporarily disabled. No touch events will be dispatched whilst
   * this big switch is in the off position.
   */
  void setEnabled(boolean enabled);

  /**
   * Returns the currently configured global touch listener, or null.
   */
  Listener listener ();

  /**
   * Sets the listener that will receive touch events. Setting the listener to
   * {@code null} will cause touch events to stop being fired.
   */
  void setListener(Listener listener);

  /**
   * Cancels all currently active touch interactions <em>on layers</em>. This does not cancel any
   * touch interactions being managed by the global touch listener. This is useful if you are
   * implementing "overlapping" interactions, like the ability to click on buttons and also the
   * ability to flick scroll an entire interface. You might start an interaction by clicking a
   * button, but then turn that interaction into a flick scroll by flicking your finger. Your code
   * should then cancel the button interaction so that the user does not accidentally end up
   * clicking the button if they happen to move finger in just the right way.
   *
   * @param except a layer to be exempted from the cancelation (this would be the layer that "wins"
   * in the conflict of who gets to process the event sequence), or null.
   */
  void cancelLayerTouches(Layer except);
}
