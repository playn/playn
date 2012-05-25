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
public abstract class MouseImpl implements Mouse {

  private Listener listener;
  private AbstractLayer activeLayer;
  private AbstractLayer hoverLayer;

  @Override
  public boolean hasMouse() {
    return true;
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  @Override
  public void lock() {
    // noop
  }

  @Override
  public void unlock() {
    // noop
  }

  @Override
  public boolean isLocked() {
    return false;
  }

  @Override
  public boolean isLockSupported() {
    return false;
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
        activeLayer.interact(LayerListener.class, new AbstractLayer.Interaction<LayerListener>() {
          public void interact(LayerListener l) {
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

    GroupLayer root = PlayN.graphics().rootLayer();
    if (root.interactive()) {
      Point p = new Point(event.x(), event.y());
      root.transform().inverseTransform(p, p);
      p.x += root.originX();
      p.y += root.originY();
      AbstractLayer lastHoverLayer = hoverLayer;
      hoverLayer = (AbstractLayer)root.hitTest(p);

      // handle onMouseDrag if we have an active layer
      if (activeLayer != null) {
        final MotionEvent.Impl localEvent = event.localize(activeLayer);
        localEvent.setPreventDefault(preventDefault);
        activeLayer.interact(LayerListener.class, new AbstractLayer.Interaction<LayerListener>() {
          public void interact(LayerListener l) {
            l.onMouseDrag(localEvent);
          }
        });
        preventDefault = localEvent.getPreventDefault();
      }

      // handle onMouseOut
      if (lastHoverLayer != hoverLayer && lastHoverLayer != null) {
        final MotionEvent.Impl localEvent = event.localize(lastHoverLayer);
        localEvent.setPreventDefault(preventDefault);
        lastHoverLayer.interact(LayerListener.class, new AbstractLayer.Interaction<LayerListener>() {
          public void interact(LayerListener l) {
            l.onMouseOut(localEvent);
          }
        });
        preventDefault = localEvent.getPreventDefault();
      }

      // handle onMouseOver
      if (hoverLayer != lastHoverLayer && hoverLayer != null) {
        final MotionEvent.Impl localEvent = event.localize(hoverLayer);
        localEvent.setPreventDefault(preventDefault);
        hoverLayer.interact(LayerListener.class, new AbstractLayer.Interaction<LayerListener>() {
          public void interact(LayerListener l) {
            l.onMouseOver(localEvent);
          }
        });
        preventDefault = localEvent.getPreventDefault();
      }
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
      activeLayer.interact(LayerListener.class, new AbstractLayer.Interaction<LayerListener>() {
        public void interact(LayerListener l) {
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

    return preventDefault;
  }
}
