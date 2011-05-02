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
package forplay.java;

import forplay.core.Keyboard;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

class JavaKeyboard implements Keyboard {

  private Listener listener;

  JavaKeyboard(JFrame frame) {
    frame.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (listener != null) {
          listener.onKeyDown(e.getKeyCode());
        }
      }

      public void keyReleased(KeyEvent e) {
        if (listener != null) {
          listener.onKeyUp(e.getKeyCode());
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
