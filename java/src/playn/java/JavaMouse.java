/**
 * Copyright 2011 The PlayN Authors
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
package playn.java;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;

import playn.core.Mouse;

class JavaMouse implements Mouse {

  private Listener listener;

  JavaMouse(JComponent frame) {
    frame.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent nativeEvent) {
        // mouseMoved(MouseEvent) does not fire when dragged
        if (listener != null) {
          MotionEvent.Impl event = new MotionEvent.Impl(nativeEvent.getWhen(), nativeEvent.getX(),
              nativeEvent.getY());
          listener.onMouseMove(event);
          if (event.getPreventDefault()) {
            nativeEvent.consume();
          }
        }
      }

      @Override
      public void mouseMoved(MouseEvent nativeEvent) {
        if (listener != null) {
          MotionEvent.Impl event = new MotionEvent.Impl(nativeEvent.getWhen(), nativeEvent.getX(),
              nativeEvent.getY());
          listener.onMouseMove(event);
          if (event.getPreventDefault()) {
            nativeEvent.consume();
          }
        }
      }
    });

    frame.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
      }

      @Override
      public void mouseEntered(MouseEvent e) {
      }

      @Override
      public void mouseExited(MouseEvent e) {
      }

      @Override
      public void mousePressed(MouseEvent nativeEvent) {
        if (listener != null) {
          ButtonEvent.Impl event = new ButtonEvent.Impl(nativeEvent.getWhen(), nativeEvent.getX(),
              nativeEvent.getY(), getMouseButton(nativeEvent));
          listener.onMouseDown(event);
          if (event.getPreventDefault()) {
            nativeEvent.consume();
          }
        }
      }

      @Override
      public void mouseReleased(MouseEvent nativeEvent) {
        if (listener != null) {
          ButtonEvent.Impl event = new ButtonEvent.Impl(nativeEvent.getWhen(), nativeEvent.getX(),
              nativeEvent.getY(), getMouseButton(nativeEvent));
          listener.onMouseUp(event);
          if (event.getPreventDefault()) {
            nativeEvent.consume();
          }
        }
      }
    });

    frame.addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseWheelMoved(MouseWheelEvent nativeEvent) {
        if (listener != null) {
          WheelEvent.Impl event = new WheelEvent.Impl(nativeEvent.getWhen(),
              nativeEvent.getWheelRotation());
          listener.onMouseWheelScroll(event);
          if (event.getPreventDefault()) {
            nativeEvent.consume();
          }
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
