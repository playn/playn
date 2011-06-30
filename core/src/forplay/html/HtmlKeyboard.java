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
package forplay.html;

import com.google.gwt.dom.client.NativeEvent;

import forplay.core.Keyboard;

class HtmlKeyboard implements Keyboard {

  private Listener listener;

  public void init() {
    // Key handlers.
    HtmlPlatform.captureEvent("keydown", new EventHandler() {
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          listener.onKeyDown(evt.getKeyCode());
          evt.preventDefault();
        }
      }
    });

    HtmlPlatform.captureEvent("keyup", new EventHandler() {
      public void handleEvent(NativeEvent evt) {
        if (listener != null) {
          listener.onKeyUp(evt.getKeyCode());
          evt.preventDefault();
        }
      }
    });
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }
}
