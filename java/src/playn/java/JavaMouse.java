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

import playn.core.MouseImpl;

class JavaMouse extends MouseImpl {

  JavaMouse(JComponent frame) {
    frame.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent ev) {
        if (onMouseMove(new MotionEvent.Impl(ev.getWhen(), ev.getX(), ev.getY())))
          ev.consume();
      }

      @Override
      public void mouseMoved(MouseEvent ev) {
        if (onMouseMove(new MotionEvent.Impl(ev.getWhen(), ev.getX(), ev.getY())))
          ev.consume();
      }
    });

    frame.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent ev) {
      }

      @Override
      public void mouseEntered(MouseEvent ev) {
      }

      @Override
      public void mouseExited(MouseEvent ev) {
      }

      @Override
      public void mousePressed(MouseEvent ev) {
        if (onMouseDown(new ButtonEvent.Impl(ev.getWhen(), ev.getX(), ev.getY(), getButton(ev))))
          ev.consume();
      }

      @Override
      public void mouseReleased(MouseEvent ev) {
        if (onMouseUp(new ButtonEvent.Impl(ev.getWhen(), ev.getX(), ev.getY(), getButton(ev))))
          ev.consume();
      }
    });

    frame.addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseWheelMoved(MouseWheelEvent ev) {
        if (onMouseWheelScroll(new WheelEvent.Impl(ev.getWhen(), ev.getWheelRotation())))
          ev.consume();
      }
    });
  }

  protected static int getButton(MouseEvent ev) {
    switch (ev.getButton()) {
    case (MouseEvent.BUTTON1): return BUTTON_LEFT;
    case (MouseEvent.BUTTON2): return BUTTON_MIDDLE;
    case (MouseEvent.BUTTON3): return BUTTON_RIGHT;
    default:                   return ev.getButton();
    }
  }
}
