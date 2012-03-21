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
 * Input-device interface for mouse events. This interface is for mice and
 * supports buttons and the scroll wheel.
 */
// TODO(pdr): make the (x,y) coordinates relative to a {@link Layer}, if
// specified, or the {@link Graphics#rootLayer()} otherwise.
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

      @Override
      public int button() {
        return button;
      }

      public Impl(double time, float x, float y, int button) {
        super(time, x, y);
        this.button = button;
      }

      /** Creates a copy of this event with local x and y in the supplied layer's coord system. */
      public ButtonEvent.Impl localize(Layer layer) {
        Point local = Layer.Util.screenToLayer(layer, x(), y());
        return new ButtonEvent.Impl(time(), x(), y(), local.x, local.y, button);
      }

      protected Impl(double time, float x, float y, float localX, float localY, int button) {
        super(time, x, y, localX, localY);
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
    // nothing currently here, for future compatibility

    class Impl extends Events.Position.Impl implements MotionEvent {
      public Impl(double time, float x, float y) {
        super(time, x, y);
      }

      /** Creates a copy of this event with local x and y in the supplied layer's coord system. */
      public MotionEvent.Impl localize(Layer layer) {
        Point local = Layer.Util.screenToLayer(layer, x(), y());
        return new MotionEvent.Impl(time(), x(), y(), local.x, local.y);
      }

      protected Impl(double time, float x, float y, float localX, float localY) {
        super(time, x, y, localX, localY);
      }

      @Override
      protected String name() {
        return "MotionEvent";
      }
    }
  }

  /** An event dispatched when the mouse wheel is scrolled. */
  interface WheelEvent extends Events.Input {
    /**
     * The velocity of the scroll wheel. Negative velocity corresponds to scrolling north/up. Each
     * scroll 'click' is 1 velocity.
     */
    float velocity();

    class Impl extends Events.Input.Impl implements WheelEvent {
      private float velocity;

      @Override
      public float velocity() {
        return velocity;
      }

      public Impl(double time, float velocity) {
        super(time);
        this.velocity = velocity;
      }

      @Override
      protected String name() {
        return "WheelEvent";
      }

      @Override
      protected void addFields(StringBuilder builder) {
        super.addFields(builder);
        builder.append(", velocity=").append(velocity);
      }
    }
  }

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
     * Called when the mouse is dragged with a button pressed.
     *
     * @param event provides mouse position and other metadata.
     */
    // Commented out to avoid bloating this API with unused features
    //void onMouseDrag(MotionEvent event);

    /**
     * Called when the mouse is moved.
     *
     * @param event provides mouse position and other metadata.
     */
    void onMouseMove(MotionEvent event);

    /**
     * Called when the mouse is double clicked.
     *
     * @param event provides mouse position, button and other metadata.
     */
    // Commented out to avoid bloating this API with unused features
    //void onMouseDoubleClick(ButtonEvent event);

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

  /**
   * Sets the listener that will receive mouse events. Setting the listener to
   * <code>null</code> will cause mouse events to stop being fired.
   */
  void setListener(Listener listener);
}
