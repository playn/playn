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

import pythagoras.f.Point;

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

      public Impl(double time, float x, float y, boolean isTouch) {
        this(time, x, y, x, y, isTouch);
      }

      /** Creates a copy of this event with local x and y in the supplied layer's coord system. */
      public Event.Impl localize(Layer layer) {
        Point local = Layer.Util.screenToLayer(layer, x(), y());
        return new Event.Impl(time(), x(), y(), local.x, local.y, isTouch());
      }

      @Override
      public boolean isTouch() {
        return isTouch;
      }

      protected Impl(double time, float x, float y, float localX, float localY, boolean isTouch) {
        super(time, x, y, localX, localY);
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
  }

  /** A {@link Listener} implementation with NOOP stubs provided for each method. */
  class Adapter implements Listener {
    @Override
    public void onPointerStart(Event event) { /* NOOP! */ }
    @Override
    public void onPointerEnd(Event event) { /* NOOP! */ }
    @Override
    public void onPointerDrag(Event event) { /* NOOP! */ }
  }

  /**
   * Sets the listener that will receive pointer events. Setting the listener to {@code null} will
   * cause pointer events to stop being fired.
   */
  void setListener(Listener listener);
}
