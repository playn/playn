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
package forplay.android;

import forplay.core.Keyboard;

public class AndroidKeyboard implements Keyboard {

  private Listener listener;

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  void onKeyDown(int keyCode) {
    if (listener != null) {
      listener.onKeyDown(keyCode);
    }
  }

  void onKeyUp(int keyCode) {
    if (listener != null) {
      listener.onKeyUp(keyCode);
    }
  }
}
