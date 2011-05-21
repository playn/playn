/**
 * Copyright 2011 The ForPlay Authors
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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;

import forplay.core.Mouse;

class JavaMouse implements Mouse {

  private Listener listener;

  JavaMouse(JComponent frame) {
    frame.addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {
        // mouseMoved(MouseEvent) does not fire when dragged
        if (listener != null) {
          listener.onMouseMove(e.getX(), e.getY());
        }
      }

      public void mouseMoved(MouseEvent e) {
        if (listener != null) {
          listener.onMouseMove(e.getX(), e.getY());
        }
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
          listener.onMouseDown(e.getX(), e.getY(), getMouseButton(e));
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (listener != null) {
          listener.onMouseUp(e.getX(), e.getY(), getMouseButton(e));
        }
      }
    });

    frame.addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        if (listener != null) {
          listener.onMouseWheelScroll(e.getWheelRotation());
        }
      }
    });
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  /**
   * Return the {@link Mouse} button given a {@link MouseEvent}
   * 
   * @param e MouseEvent
   * @return {@link Mouse} button corresponding to the event
   */
  protected static int getMouseButton(MouseEvent e) {
    switch (e.getButton()) {
      case (MouseEvent.BUTTON1):
        return Mouse.BUTTON_LEFT;
      case (MouseEvent.BUTTON2):
        return Mouse.BUTTON_MIDDLE;
      case (MouseEvent.BUTTON3):
        return Mouse.BUTTON_RIGHT;
      default:
        return e.getButton();
    }
  }
}
