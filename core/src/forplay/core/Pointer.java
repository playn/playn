/**
 * Copyright 2010 The ForPlay Authors
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
 * Input-device interface for pointer events. This is a generic interface that
 * works for both mouse and touch events, but only handles the basic
 * touch-drag-release case.
 */
public interface Pointer {

  interface Listener {
    /**
     * Called when the pointer event starts.
     */
    void onPointerStart(float x, float y);

    /**
     * Called when the pointer event ends.
     */
    void onPointerEnd(float x, float y);

    /**
     * Called when the pointer drags (always between start/end events).
     */
    void onPointerDrag(float x, float y);
  }

  /**
   * Sets the listener that will receive pointer events. Setting the listener to
   * <code>null</code> will cause pointer events to stop being fired.
   */
  void setListener(Listener listener);
}
