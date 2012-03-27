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
package playn.android;

import playn.core.Keyboard;
import playn.core.util.Callback;

public class AndroidKeyboard implements Keyboard {
  private Listener listener;

  @Override
  public synchronized void setListener(Listener listener) {
    this.listener = listener;
  }

  @Override
  public boolean hasHardwareKeyboard() {
    return false; // TODO: return true for devices that have a hardware keyboard
  }

  @Override
  public void getText(TextType textType, String label, String initVal, Callback<String> callback) {
    callback.onFailure(new UnsupportedOperationException("Not yet implemented."));
  }

  /*
   * The methods below are called from the GL render thread
   */
  boolean onKeyDown(Event event) {
    if (listener != null) {
      listener.onKeyDown(event);
      return event.getPreventDefault();
    }
    return false;
  }

  boolean onKeyTyped(TypedEvent event) {
    if (listener != null) {
      listener.onKeyTyped(event);
      return event.getPreventDefault();
    }
    return false;
  }

  boolean onKeyUp(Event event) {
    if (listener != null) {
      listener.onKeyUp(event);
      return event.getPreventDefault();
    }
    return false;
  }
}
