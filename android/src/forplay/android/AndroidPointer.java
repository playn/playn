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
package forplay.android;

import forplay.core.Pointer;

class AndroidPointer implements Pointer {

  // true when we are in a drag sequence (after pointer start but before pointer end)
  private boolean inDragSequence = false;
  private Listener listener;

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  void onPointerEnd(Event event) {
    if (listener != null) {
      inDragSequence = false;
      listener.onPointerEnd(event);
    }
  }

  void onPointerMove(Event event) {
    if (listener != null) {
      if (inDragSequence) {
        listener.onPointerDrag(event);
      }
    }
  }

  void onPointerStart(Event event) {
    if (listener != null) {
      inDragSequence = true;
      listener.onPointerStart(event);
    }
  }
}
