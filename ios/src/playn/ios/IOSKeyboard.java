/**
 * Copyright 2012 The PlayN Authors
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
package playn.ios;

import playn.core.Keyboard;
import playn.core.PlayN;
import playn.core.util.Callback;

class IOSKeyboard implements Keyboard
{
  @Override
  public void setListener(Listener listener) {
    PlayN.log().warn("iOS cannot generate keyboard events. Use Keyboard.getText() instead");
  }

  @Override
  public boolean hasHardwareKeyboard() {
    return false;
  }

  @Override
  public void getText(TextType textType, String label, String initVal, Callback<String> callback) {
    callback.onFailure(new UnsupportedOperationException("Not yet implemented."));
  }
}
