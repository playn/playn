/**
 * Copyright 2010 The ForPlay Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package forplay.core;

/**
 * Input-device interface for keyboard events.
 */
public interface Keyboard {

  interface Listener {
    /**
     * Called when a key is depressed.
     */
    void onKeyDown(int keyCode);

    /**
     * Called when a key is released.
     */
    void onKeyUp(int keyCode);
  }

  public static final int KEY_ESC = 27;
  public static final int KEY_SPACE = 32;

  public static final int KEY_LEFT = 37;
  public static final int KEY_UP = 38;
  public static final int KEY_RIGHT = 39;
  public static final int KEY_DOWN = 40;

  // TODO(jgw): Lots more keyboard definitions. These values only work on the desktop at the moment
  // and are completely untested on Android.

  /**
   * Sets the listener that will receive keyboard events. Setting the listener to
   * <code>null</code> will cause keyboard events to stop being fired.
   */
  void setListener(Listener listener);
}
