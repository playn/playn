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
 * Input-device interface for touch and multi-touch events if they are supported.
 */
// TODO(pdr): make the (x,y) coordinates relative to a {@link Layer}, if
// specified, or the {@link Graphics#rootLayer()} otherwise.
public interface Touch {
  /**
   * Class for a {@link Touch} that encapsulates the location of a touch (i.e., finger).
   */
  public class TouchEvent {
    private final float x;
    private final float y;
    private final int id;

    public TouchEvent(float x, float y, int id) {
      this.x = x;
      this.y = y;
      this.id = id;
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
   * Sets the listener that will receive touch events. Setting the listener to
   * <code>null</code> will cause touch events to stop being fired.
   */
  void setListener(Listener listener);
}
