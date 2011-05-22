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

  private Listener listener;
  private boolean inDragSequence = false; // true when we are in a drag sequence (after pointer start but before pointer end)

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  void onPointerEnd(float x, float y) {
    if (listener != null) {
      inDragSequence = false;
      listener.onPointerEnd(x, y);
    }
  }

  void onPointerMove(float x, float y) {
    if (listener != null) {
      if (inDragSequence) {
        listener.onPointerDrag(x, y);
      }
    }
  }

  void onPointerStart(float x, float y) {
    if (listener != null) {
      inDragSequence = true;
      listener.onPointerStart(x, y);
    }
  }
}
