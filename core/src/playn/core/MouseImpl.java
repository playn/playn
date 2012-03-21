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
package playn.core;

import pythagoras.f.Point;

/**
 * Handles the common logic for all platform {@link Mouse} implementations.
 */
public class MouseImpl implements Mouse {

  private Listener listener;
  private AbstractLayer activeLayer;

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  protected boolean onMouseDown(ButtonEvent.Impl event) {
    boolean preventDefault = false;
    if (listener != null) {
      event.setPreventDefault(preventDefault);
      listener.onMouseDown(event);
      preventDefault = event.getPreventDefault();
    }

    GroupLayer root = PlayN.graphics().rootLayer();
    if (root.interactive()) {
      Point p = new Point(event.x(), event.y());
      root.transform().inverseTransform(p, p);
      p.x += root.originX();
      p.y += root.originY();
      activeLayer = (AbstractLayer)root.hitTest(p);
      if (activeLayer != null) {
        final ButtonEvent.Impl localEvent = event.localize(activeLayer);
        localEvent.setPreventDefault(preventDefault);
        activeLayer.interact(Listener.class, new AbstractLayer.Interaction<Listener>() {
          public void interact(Listener l) {
            l.onMouseDown(localEvent);
          }
        });
        preventDefault = localEvent.getPreventDefault();
      }
    }
    return preventDefault;
  }

  protected boolean onMouseMove(MotionEvent.Impl event) {
    boolean preventDefault = false;
    if (listener != null) {
      event.setPreventDefault(preventDefault);
      listener.onMouseMove(event);
      preventDefault = event.getPreventDefault();
    }

    if (activeLayer != null) {
      final MotionEvent.Impl localEvent = event.localize(activeLayer);
      localEvent.setPreventDefault(preventDefault);
      activeLayer.interact(Listener.class, new AbstractLayer.Interaction<Listener>() {
        public void interact(Listener l) {
          l.onMouseMove(localEvent);
        }
      });
      preventDefault = localEvent.getPreventDefault();
    }
    return preventDefault;
  }

  protected boolean onMouseUp(ButtonEvent.Impl event) {
    boolean preventDefault = false;
    if (listener != null) {
      event.setPreventDefault(preventDefault);
      listener.onMouseUp(event);
      preventDefault = event.getPreventDefault();
    }

    if (activeLayer != null) {
      final ButtonEvent.Impl localEvent = event.localize(activeLayer);
      localEvent.setPreventDefault(preventDefault);
      activeLayer.interact(Listener.class, new AbstractLayer.Interaction<Listener>() {
        public void interact(Listener l) {
          l.onMouseUp(localEvent);
        }
      });
      preventDefault = localEvent.getPreventDefault();
      activeLayer = null;
    }
    return preventDefault;
  }

  protected boolean onMouseWheelScroll(WheelEvent.Impl event) {
    boolean preventDefault = false;
    if (listener != null) {
      event.setPreventDefault(preventDefault);
      listener.onMouseWheelScroll(event);
      preventDefault = event.getPreventDefault();
    }

    // TODO: if we ever want to dispatch wheel events directly to layers, we'll need this
    // if (activeLayer != null) {
    //   final WheelEvent.Impl localEvent = event.localize(activeLayer);
    //   localEvent.setPreventDefault(preventDefault);
    //   activeLayer.interact(Listener.class, new AbstractLayer.Interaction<Listener>() {
    //     public void interact(Listener l) {
    //       l.onMouseWheelScroll(localEvent);
    //     }
    //   });
    //   preventDefault = localEvent.getPreventDefault();
    //   activeLayer = null;
    // }
    return preventDefault;
  }
}
