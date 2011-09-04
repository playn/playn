/**
 * Copyright 2010 The PlayN Authors
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
package playn.java;

import playn.core.Keyboard;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

class JavaKeyboard implements Keyboard {

  private Listener listener;

  JavaKeyboard(JFrame frame) {
    frame.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent nativeEvent) {
        if (listener != null) {
          Event.Impl event = new Event.Impl(nativeEvent.getWhen(), nativeEvent.getKeyCode());
          listener.onKeyDown(event);
          if (event.getPreventDefault()) {
            nativeEvent.consume();
          }
        }
      }

      public void keyReleased(KeyEvent nativeEvent) {
        if (listener != null) {
          Event.Impl event = new Event.Impl(nativeEvent.getWhen(), nativeEvent.getKeyCode());
          listener.onKeyUp(event);
          if (event.getPreventDefault()) {
            nativeEvent.consume();
          }
        }
      }

      public void keyTyped(KeyEvent e) {
      }
    });
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }
}
