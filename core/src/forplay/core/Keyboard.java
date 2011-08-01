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

  /** An event dispatched when a key is pressed or released. */
  interface Event extends Events.Input {
    /**
     * The code of the key that triggered this event, e.g. {@link #KEY_ESC}, etc.
     */
    int keyCode();

    class Impl extends Events.Input.Impl implements Event {
      private int keyCode;

      @Override public int keyCode() {
        return keyCode;
      }

      public Impl(double time, int keyCode) {
        super(time);
        this.keyCode = keyCode;
      }
    }
  }

  interface Listener {
    /**
     * Called when a key is depressed.
     */
    void onKeyDown(Event event);

    /**
     * Called when a key is released.
     */
    void onKeyUp(Event event);
  }

  /** A {@link Listener} implementation with NOOP stubs provided for each method. */
  class Adapter implements Listener {
    public void onKeyDown(Event event) { /* NOOP! */ }
    public void onKeyUp(Event event) { /* NOOP! */ }
  }

  int KEY_ESC = 27;
  int KEY_SPACE = 32;

  int KEY_LEFT = 37;
  int KEY_UP = 38;
  int KEY_RIGHT = 39;
  int KEY_DOWN = 40;

  // TODO(jgw): Lots more keyboard definitions. These values only work on the desktop at the moment
  // and are completely untested on Android.

  /**
   * Sets the listener that will receive keyboard events. Setting the listener to
   * <code>null</code> will cause keyboard events to stop being fired.
   */
  void setListener(Listener listener);
}
