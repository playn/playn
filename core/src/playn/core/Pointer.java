/**
 * Copyright 2010 The PlayN Authors
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

import playn.core.Events.Flags;

/**
 * Input-device interface for pointer events. This is a generic interface that
 * works for both mouse and touch events, but only handles the basic
 * touch-drag-release case.
 */
public interface Pointer {

  /** The event dispatched to pointer listeners. */
  interface Event extends Events.Position {
    /** Returns true if this event originated from touch input, false otherwise. */
    boolean isTouch();

    class Impl extends Events.Position.Impl implements Event {
      private boolean isTouch;

      public Impl(Flags flags, double time, float x, float y, boolean isTouch) {
        this(null, flags, time, x, y, isTouch);
      }

      @Override
      public Event.Impl localize(Layer hit) {
        Event.Impl impl = new Event.Impl(hit, flags(), time(), x(), y(), isTouch());
        impl.captureState = captureState;
        return impl;
      }

      @Override
      public boolean isTouch() {
        return isTouch;
      }

      protected Impl(Layer hit, Flags flags, double time, float x, float y, boolean isTouch) {
        super(hit, flags, time, x, y);
        this.isTouch = isTouch;
      }

      @Override
      protected String name() {
        return "Pointer.Event";
      }
    }
  }

  interface Listener {
    /**
     * Called when the pointer event starts.
     */
    void onPointerStart(Event event);

    /**
     * Called when the pointer event ends.
     */
    void onPointerEnd(Event event);

    /**
     * Called when the pointer drags (always between start/end events).
     */
    void onPointerDrag(Event event);

    /**
     * Called when a drag is canceled (always after start and takes the place of end). This happens
     * when {@link #cancelLayerDrags} is called, or it can be initiated by the OS. <em>Note:</em>
     * the coordinates of this event will be (0, 0) if initiated by {@link #cancelLayerDrags}.
     */
    void onPointerCancel(Event event);
  }

  /** A {@link Listener} implementation with NOOP stubs provided for each method. */
  class Adapter implements Listener {
    @Override
    public void onPointerStart(Event event) { /* NOOP! */ }
    @Override
    public void onPointerEnd(Event event) { /* NOOP! */ }
    @Override
    public void onPointerDrag(Event event) { /* NOOP! */ }
    @Override
    public void onPointerCancel(Event event) { /* NOOP! */ }
  }

  /**
   * Returns true if pointer interaction is enabled, false if not. Interaction is enabled by default.
   * See {@link #setEnabled}.
   */
  boolean isEnabled();

  /**
   * Allows pointer interaction to be temporarily disabled. No pointer events will be dispatched
   * whilst this big switch is in the off position.
   */
  void setEnabled(boolean enabled);

  /**
   * Returns the currently configured global pointer listener, or null.
   */
  Listener listener ();

  /**
   * Sets the listener that will receive pointer events. Setting the listener to {@code null} will
   * cause pointer events to stop being fired.
   */
  void setListener(Listener listener);

  /**
   * Cancels any currently active drag interaction <em>on a layer</em>. This does not cancel a drag
   * interaction being managed by the global pointer listener. This is useful if you are
   * implementing "overlapping" interactions, like the ability to click on buttons and also the
   * ability to flick scroll an entire interface. You might start an interaction by clicking a
   * button, but then turn that interaction into a flick scroll by flicking your finger. Your code
   * should then cancel the button interaction so that the user does not accidentally end up
   * clicking the button if they happen to move finger in just the right way.
   */
  void cancelLayerDrags();
}
