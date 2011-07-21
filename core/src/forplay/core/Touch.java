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
  /**
   * Class for a {@link Touch} that encapsulates the location, pressure, and
   * size of a touch (i.e., finger).
   */
  public class TouchEvent {
    private final float x;
    private final float y;
    private final float pressure;
    private final float size;
    private final int id;

    // TODO: Implement pressure and size across all platforms that support
    // touch.
    public TouchEvent(float x, float y, float pressure, float size, int id) {
      this.x = x;
      this.y = y;
      this.pressure = pressure;
      this.size = size;
      this.id = id;
    }

    public TouchEvent(float x, float y, int id) {
      this(x, y, -1, -1, id);
    }

    /**
     * Return the x location.
     * 
     * @return the x location
     */
    public float x() {
      return x;
    }

    /**
     * Return the y location.
     * 
     * @return the y location
     */
    public float y() {
      return y;
    }

    /**
     * Return the pressure.
     * 
     * @return the pressure
     */
    public float pressure() {
      return pressure;
    }

    /**
     * Return the size.
     * 
     * @return the size
     */
    public float size() {
      return size;
    }

    /**
     * Return the touch's unique identifier.
     * 
     * @return the touch's unique identifier
     */
    public float id() {
      return id;
    }
  }

  interface Listener {
    /**
     * Called when a touch starts.
     * 
     * @param touches the array of {@link Touch.TouchEvent}s.
     */
    void onTouchStart(TouchEvent[] touches);

    /**
     * Called when a touch moves (always between start/end events).
     * 
     * @param touches the array of {@link Touch.TouchEvent}s.
     */
    void onTouchMove(TouchEvent[] touches);

    /**
     * Called when a touch ends.
     * 
     * @param touches the array of {@link Touch.TouchEvent}s.
     */
    void onTouchEnd(TouchEvent[] touches);
  }

  /**
   * A {@link Listener} implementation with NOOP stubs provided for each method.
   */
  public static class Adapter implements Listener {
    public void onTouchStart(TouchEvent[] touches) { /* NOOP! */ }

    public void onTouchMove(TouchEvent[] touches) { /* NOOP! */ }

    public void onTouchEnd(TouchEvent[] touches) { /* NOOP! */ }
  }

  /**
   * Sets the listener that will receive touch events. Setting the listener to
   * <code>null</code> will cause touch events to stop being fired.
   */
  void setListener(Listener listener);
}
