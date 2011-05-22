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
 * Input-device interface for mouse events. This interface is for mice and
 * supports buttons and the scroll wheel.
 */
// TODO(pdr): make the (x,y) coordinates relative to a {@link Layer}, if
// specified, or the {@link Graphics#rootLayer()} otherwise.
public interface Mouse {
  public static final int BUTTON_LEFT = 0;
  public static final int BUTTON_MIDDLE = 1;
  public static final int BUTTON_RIGHT = 2;

  interface Listener {
    /**
     * Called when the mouse is pressed.
     * <p>
     * The button will be one of {@link Mouse#BUTTON_LEFT},
     * {@link Mouse#BUTTON_MIDDLE}, or {@link Mouse#BUTTON_RIGHT}.
     * 
     * @param x x location
     * @param y y location
     * @param button button that was pressed
     */
    void onMouseDown(float x, float y, int button);

    /**
     * Called when the mouse is released.
     * <p>
     * The button will be one of {@link Mouse#BUTTON_LEFT},
     * {@link Mouse#BUTTON_MIDDLE}, or {@link Mouse#BUTTON_RIGHT}.
     * 
     * @param x x location
     * @param y y location
     * @param button button that was pressed
     */
    void onMouseUp(float x, float y, int button);

    /**
     * Called when the mouse is dragged with a button pressed.
     * 
     * @param x x location
     * @param y y location
     */
    // Commented out to avoid bloating this API with unused features
    //void onMouseDrag(float x, float y);

    /**
     * Called when the mouse is moved.
     * 
     * @param x x location
     * @param y y location
     */
    void onMouseMove(float x, float y);

    /**
     * Called when the mouse is double clicked.
     * <p>
     * The button will be one of {@link Mouse#BUTTON_LEFT},
     * {@link Mouse#BUTTON_MIDDLE}, or {@link Mouse#BUTTON_RIGHT}.
     * 
     * @param x x location
     * @param y y location
     * @param button button that was pressed
     */
    // Commented out to avoid bloating this API with unused features
    //void onMouseDoubleClick(float x, float y, int button);

    /**
     * Called when mouse wheel scroll occurs.
     * <p>
     * Negative velocity corresponds to scrolling north/up. Each scroll 'click'
     * is 1 velocity.
     * 
     * @param velocity velocity of the scroll wheel
     */
    void onMouseWheelScroll(float velocity);
  }

  /**
   * Sets the listener that will receive mouse events. Setting the listener to
   * <code>null</code> will cause mouse events to stop being fired.
   */
  void setListener(Listener listener);
}
