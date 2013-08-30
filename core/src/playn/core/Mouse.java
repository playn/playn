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
 * Input-device interface for mouse events. This interface is for mice and
 * supports buttons and the scroll wheel.
 */
public interface Mouse {
  /** Used by {@link ButtonEvent} to indicate that the left button is pressed. */
  int BUTTON_LEFT = 0;
  /** Used by {@link ButtonEvent} to indicate that the middle button is pressed. */
  int BUTTON_MIDDLE = 1;
  /** Used by {@link ButtonEvent} to indicate that the right button is pressed. */
  int BUTTON_RIGHT = 2;

  /** An event dispatched when a button is pressed. */
  interface ButtonEvent extends Events.Position {
    /**
     * The id of the button associated with this event, one of {@link #BUTTON_LEFT}, {@link
     * #BUTTON_MIDDLE}, or {@link #BUTTON_RIGHT}.
     */
    int button();

    class Impl extends Events.Position.Impl implements ButtonEvent {
      private int button;

      public Impl(Events.Flags flags, double time, float x, float y, int button) {
        super(null, flags, time, x, y);
        this.button = button;
      }

      @Override
      public int button() {
        return button;
      }

      @Override
      public ButtonEvent.Impl localize(Layer hit) {
        return new ButtonEvent.Impl(hit, flags(), time(), x(), y(), button);
      }

      protected Impl(Layer hit, Events.Flags flags, double time, float x, float y, int button) {
        super(hit, flags, time, x, y);
        this.button = button;
      }

      @Override
      protected String name() {
        return "ButtonEvent";
      }

      @Override
      protected void addFields(StringBuilder builder) {
        super.addFields(builder);
        builder.append(", button=").append(button);
      }
    }
  }

  /** An event dispatched when the mouse is moved. */
  interface MotionEvent extends Events.Position {
    /**
     * The x-coordinate associated with this event.
     */
    float dx();

    /**
     * The y-coordinate associated with this event.
     */
    float dy();

    class Impl extends Events.Position.Impl implements MotionEvent {
      private final float dx, dy;

      public Impl(Events.Flags flags, double time, float x, float y, float dx, float dy) {
        super(flags, time, x, y);
        this.dx = dx;
        this.dy = dy;
      }

      @Override
      public float dx() {
        return dx;
      }

      @Override
      public float dy() {
        return dy;
      }

      @Override
      public MotionEvent.Impl localize(Layer hit) {
        return new MotionEvent.Impl(hit, flags(), time(), x(), y(), dx(), dy());
      }

      protected Impl(Layer hit, Events.Flags flags, double time, float x, float y,
                     float dx, float dy) {
        super(hit, flags, time, x, y);
        this.dx = dx;
        this.dy = dy;
      }

      @Override
      protected String name() {
        return "MotionEvent";
      }
    }
  }

  /** An event dispatched when the mouse wheel is scrolled. */
  interface WheelEvent extends Events.Position {
    /**
     * The velocity of the scroll wheel. Negative velocity corresponds to scrolling north/up. Each
     * scroll 'click' is 1 velocity.
     */
    float velocity();

    class Impl extends Events.Position.Impl implements WheelEvent {
      private float velocity;

      public Impl(Events.Flags flags, double time, float x, float y, float velocity) {
        super(flags, time, x, y);
        this.velocity = velocity;
      }

      @Override
      public float velocity() {
        return velocity;
      }

      @Override
      protected String name() {
        return "WheelEvent";
      }

      protected Impl(Layer hit, Events.Flags flags, double time, float x, float y, float velocity) {
        super(hit, flags, time, x, y);
        this.velocity = velocity;
      }

      @Override
      public WheelEvent.Impl localize (Layer hit) {
        return new WheelEvent.Impl(hit, flags(), time(), x(), y(), velocity);
      }

      @Override
      protected void addFields(StringBuilder builder) {
        super.addFields(builder);
        builder.append(", velocity=").append(velocity);
      }
    }
  }

  /** An interface for listening to all mouse events. */
  interface Listener {
    /**
     * Called when the mouse is pressed.
     *
     * @param event provides mouse position, button and other metadata.
     */
    void onMouseDown(ButtonEvent event);

    /**
     * Called when the mouse is released.
     *
     * @param event provides mouse position, button and other metadata.
     */
    void onMouseUp(ButtonEvent event);

    /**
     * Called when the mouse is moved.
     *
     * @param event provides mouse position and other metadata.
     */
    void onMouseMove(MotionEvent event);

    /**
     * Called when mouse wheel scroll occurs.
     * <p>
     * Negative velocity corresponds to scrolling north/up.
     * Positive velocity corresponds to scrolling south/down.
     * Each scroll 'click' is 1 velocity.
     *
     * @param event provides wheel velocity and other metadata.
     */
    void onMouseWheelScroll(WheelEvent event);
  }

  /** An interface for listening to mouse events that interact with a single layer.
   * See {@link Layer#addListener(Mouse.LayerListener)}. */
  interface LayerListener {
    /**
     * Called when the mouse is pressed.
     *
     * @param event provides mouse position, button and other metadata.
     */
    void onMouseDown(ButtonEvent event);

    /**
     * Called when the mouse is released.
     *
     * @param event provides mouse position, button and other metadata.
     */
    void onMouseUp(ButtonEvent event);

    /**
     * Called when the mouse button is pressed on a layer and is subsequently moved (dragged). This
     * event is dispatched to the layer that was "hit" when the mouse button was first pressed.
     *
     * @param event provides mouse position and other metadata.
     */
    void onMouseDrag(MotionEvent event);

    /**
     * Called when the mouse is moved and is not currently engaged in a drag (see {@link
     * #onMouseDrag}. The event is dispatched to the layer which is intersected by the mouse
     * coordinates.
     *
     * @param event provides mouse position and other metadata.
     */
    void onMouseMove(MotionEvent event);

    /**
     * Called when the mouse enters a {@link Layer}.
     *
     * Note: MotionEvent is first dispatched to {@link #onMouseDrag} or {@link #onMouseMove}, then
     *       to {@link #onMouseOut} and finally to {@link #onMouseOver}. These three events share a
     *       single preventDefault state.
     *
     * @param event provides mouse position and other metadata.
     */
    void onMouseOver(MotionEvent event);

    /**
     * Called when the mouse leaves a {@link Layer}.
     *
     * Note: MotionEvent is first dispatched to {@link #onMouseDrag} or {@link #onMouseMove}, then
     * to {@link #onMouseOut} and finally to {@link #onMouseOver}. These three events share a
     * single preventDefault state.
     *
     * @param event provides mouse position and other metadata.
     */
    void onMouseOut(MotionEvent event);

    /**
     * Called when mouse wheel scroll occurs while the mouse is hovered over the listening layer,
     * or while the layer is active due to having been previously hit by a mouse click which has
     * not yet been released.
     *
     * <p> Negative velocity corresponds to scrolling north/up. Positive velocity corresponds to
     * scrolling south/down. Each scroll 'click' is 1 velocity. </p>
     *
     * @param event provides wheel velocity and other metadata.
     */
    void onMouseWheelScroll(WheelEvent event);
  }

  /** A {@link Listener} implementation with NOOP stubs provided for each method. */
  class Adapter implements Listener {
    @Override
    public void onMouseDown(ButtonEvent event) { /* NOOP! */ }
    @Override
    public void onMouseUp(ButtonEvent event) { /* NOOP! */ }
    @Override
    public void onMouseMove(MotionEvent event) { /* NOOP! */ }
    @Override
    public void onMouseWheelScroll(WheelEvent event) { /* NOOP! */ }
  }

  /** A {@link LayerListener} implementation with NOOP stubs provided for each method. */
  class LayerAdapter implements LayerListener {
    @Override
    public void onMouseDown(ButtonEvent event) { /* NOOP! */ }
    @Override
    public void onMouseUp(ButtonEvent event) { /* NOOP! */ }
    @Override
    public void onMouseDrag(MotionEvent event) { /* NOOP! */ }
    @Override
    public void onMouseMove(MotionEvent event) { /* NOOP! */ }
    @Override
    public void onMouseOver(MotionEvent event) { /* NOOP! */ }
    @Override
    public void onMouseOut(MotionEvent event) { /* NOOP! */ }
    @Override
    public void onMouseWheelScroll(WheelEvent event) { /* NOOP! */ }
  }

  /**
   * Returns true if the underlying platform supports mouse interaction. If this method returns
   * false, listeners may still be registered with this service but they will never be notified.
   */
  boolean hasMouse();

  /**
   * Returns true if mouse interaction is enabled, false if not. Interaction is enabled by default.
   * See {@link #setEnabled}.
   */
  boolean isEnabled();

  /**
   * Allows mouse interaction to be temporarily disabled. No mouse events will be dispatched whilst
   * this big switch is in the off position.
   */
  void setEnabled(boolean enabled);

  /**
   * Returns the currently configured global mouse listener, or null.
   */
  Listener listener ();

  /**
   * Sets the listener that will receive mouse events. Setting the listener to
   * {@code null} will cause mouse events to stop being fired.
   */
  void setListener(Listener listener);

  /**
   * Lock the mouse, i.e. receive mouse events even when the mouse pointer leaves the window.
   */
  void lock();

  /**
   * Unlock the mouse.
   */
  void unlock();

  /**
   * True if the mouse is locked.
   */
  boolean isLocked();

  /**
   * True if lock has a chance of success on this platform (the user may still block it, or
   * detection may be broken for some browsers).
   */
  boolean isLockSupported();
}
