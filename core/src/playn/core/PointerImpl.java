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
 * Handles the common logic for all platform {@link Pointer} implementations.
 */
public abstract class PointerImpl implements Pointer {

  private Listener listener;
  private AbstractLayer activeLayer;

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  protected boolean onPointerStart(Event.Impl event, boolean preventDefault) {
    if (listener != null) {
      event.setPreventDefault(preventDefault);
      listener.onPointerStart(event);
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
        final Event.Impl localEvent = event.localize(activeLayer);
        localEvent.setPreventDefault(preventDefault);
        activeLayer.interact(Listener.class, new AbstractLayer.Interaction<Listener>() {
          public void interact(Listener l) {
            l.onPointerStart(localEvent);
          }
        });
        preventDefault = localEvent.getPreventDefault();
      }
    }
    return preventDefault;
  }

  protected boolean onPointerDrag(Event.Impl event, boolean preventDefault) {
    if (listener != null) {
      event.setPreventDefault(preventDefault);
      listener.onPointerDrag(event);
      preventDefault = event.getPreventDefault();
    }

    if (activeLayer != null) {
      final Event.Impl localEvent = event.localize(activeLayer);
      localEvent.setPreventDefault(preventDefault);
      activeLayer.interact(Listener.class, new AbstractLayer.Interaction<Listener>() {
        public void interact(Listener l) {
          l.onPointerDrag(localEvent);
        }
      });
      preventDefault = localEvent.getPreventDefault();
    }
    return preventDefault;
  }

  protected boolean onPointerEnd(Event.Impl event, boolean preventDefault) {
    if (listener != null) {
      event.setPreventDefault(preventDefault);
      listener.onPointerEnd(event);
      preventDefault = event.getPreventDefault();
    }

    if (activeLayer != null) {
      final Event.Impl localEvent = event.localize(activeLayer);
      localEvent.setPreventDefault(preventDefault);
      activeLayer.interact(Listener.class, new AbstractLayer.Interaction<Listener>() {
        public void interact(Listener l) {
          l.onPointerEnd(localEvent);
        }
      });
      preventDefault = localEvent.getPreventDefault();
      activeLayer = null;
    }
    return preventDefault;
  }
}
