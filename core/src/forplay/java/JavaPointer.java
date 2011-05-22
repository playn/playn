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
package forplay.java;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

import forplay.core.Pointer;

// TODO(pdr): add touch support.
class JavaPointer implements Pointer {

  private Listener listener;

  JavaPointer(JComponent frame) {
    frame.addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {
        if (listener != null) {
          listener.onPointerDrag(e.getX(), e.getY());
        }
      }

      public void mouseMoved(MouseEvent e) {
      }
    });

    frame.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }

      public void mousePressed(MouseEvent e) {
        if (listener != null) {
          listener.onPointerStart(e.getX(), e.getY());
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (listener != null) {
          listener.onPointerEnd(e.getX(), e.getY());
        }
      }
    });
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }
}
