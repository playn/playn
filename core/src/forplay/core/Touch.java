/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.core;

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

      @Override
      public int id() {
        return id;
      }

      @Override public float pressure() {
        return pressure;
      }

      @Override public float size() {
        return size;
      }

      public Impl(double time, float x, float y, int id, float pressure, float size) {
        super(time, x, y);
        this.id = id;
        this.pressure = pressure;
        this.size = size;
      }

      // TODO: Implement pressure and size across all platforms that support touch.
      public Impl(double time, float x, float y, int id) {
        this(time, x, y, id, -1, -1);
      }
    }
  }

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

  /**
   * A {@link Listener} implementation with NOOP stubs provided for each method.
   */
  class Adapter implements Listener {
    public void onTouchStart(Event[] touches) { /* NOOP! */ }
    public void onTouchMove(Event[] touches) { /* NOOP! */ }
    public void onTouchEnd(Event[] touches) { /* NOOP! */ }
  }

  /**
   * Sets the listener that will receive touch events. Setting the listener to
   * <code>null</code> will cause touch events to stop being fired.
   */
  void setListener(Listener listener);
}
