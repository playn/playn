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

  private boolean enabled = true;
  private Dispatcher dispatcher = Dispatcher.SINGLE;
  private Listener listener;
  private AbstractLayer activeLayer;
  private AbstractLayer hoverLayer;

  @Override
  public boolean hasMouse() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public Listener listener () {
    return listener;
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

  public void setPropagateEvents(boolean propagate) {
    dispatcher = Dispatcher.select(propagate);
  }

  protected boolean onMouseDown(ButtonEvent.Impl event) {
    if (!enabled)
      return false;

    event.flags().setPreventDefault(false);
    if (listener != null) {
      listener.onMouseDown(event);
    }

    GroupLayer root = PlayN.graphics().rootLayer();
    if (root.interactive()) {
      Point p = new Point(event.x(), event.y());
      root.transform().inverseTransform(p, p);
      p.x += root.originX();
      p.y += root.originY();
      activeLayer = (AbstractLayer)root.hitTest(p);
      if (activeLayer != null) {
        dispatcher.dispatch(activeLayer, LayerListener.class, event, DOWN);
      }
    }
    return event.flags().getPreventDefault();
  }

  protected boolean onMouseMove(MotionEvent.Impl event) {
    if (!enabled)
      return false;

    event.flags().setPreventDefault(false);
    if (listener != null) {
      listener.onMouseMove(event);
    }

    GroupLayer root = PlayN.graphics().rootLayer();
    if (root.interactive()) {
      Point p = new Point(event.x(), event.y());
      root.transform().inverseTransform(p, p);
      p.x += root.originX();
      p.y += root.originY();
      AbstractLayer lastHoverLayer = hoverLayer;
      hoverLayer = (AbstractLayer)root.hitTest(p);

      // handle onMouseDrag if we have an active layer, onMouseMove otherwise
      if (activeLayer != null) {
        dispatcher.dispatch(activeLayer, LayerListener.class, event, DRAG);
      } else if (hoverLayer != null) {
        dispatcher.dispatch(hoverLayer, LayerListener.class, event, MOVE);
      }

      // handle onMouseOut
      if (lastHoverLayer != hoverLayer && lastHoverLayer != null) {
        dispatcher.dispatch(lastHoverLayer, LayerListener.class, event, OUT);
      }

      // handle onMouseOver
      if (hoverLayer != lastHoverLayer && hoverLayer != null) {
        dispatcher.dispatch(hoverLayer, LayerListener.class, event, OVER);
      }
    }

    return event.flags().getPreventDefault();
  }

  protected boolean onMouseUp(ButtonEvent.Impl event) {
    if (!enabled)
      return false;

    event.flags().setPreventDefault(false);
    if (listener != null) {
      listener.onMouseUp(event);
    }

    if (activeLayer != null) {
      dispatcher.dispatch(activeLayer, LayerListener.class, event, UP);
      activeLayer = null;
    }

    return event.flags().getPreventDefault();
  }

  protected boolean onMouseWheelScroll(final WheelEvent.Impl event) {
    if (!enabled)
      return false;

    if (listener != null)
      listener.onMouseWheelScroll(event);

    AbstractLayer target = (activeLayer != null) ? activeLayer : hoverLayer;
    if (target != null)
      dispatcher.dispatch(target, LayerListener.class, event, WHEEL_SCROLL);
    return event.flags().getPreventDefault();
  }

  protected AbstractLayer.Interaction<LayerListener, ButtonEvent.Impl> DOWN =
      new AbstractLayer.Interaction<LayerListener, ButtonEvent.Impl>() {
    @Override public void interact (LayerListener l, ButtonEvent.Impl ev) {
      l.onMouseDown(ev);
    }
  };

  protected AbstractLayer.Interaction<LayerListener, ButtonEvent.Impl> UP =
      new AbstractLayer.Interaction<LayerListener, ButtonEvent.Impl>() {
    @Override public void interact (LayerListener l, ButtonEvent.Impl ev) {
      l.onMouseUp(ev);
    }
  };

  protected AbstractLayer.Interaction<LayerListener, MotionEvent.Impl> DRAG =
      new AbstractLayer.Interaction<LayerListener, MotionEvent.Impl>() {
    @Override public void interact (LayerListener l, MotionEvent.Impl ev) {
      l.onMouseDrag(ev);
    }
  };

  protected AbstractLayer.Interaction<LayerListener, MotionEvent.Impl> MOVE =
      new AbstractLayer.Interaction<LayerListener, MotionEvent.Impl>() {
    @Override public void interact (LayerListener l, MotionEvent.Impl ev) {
      l.onMouseMove(ev);
    }
  };

  protected AbstractLayer.Interaction<LayerListener, MotionEvent.Impl> OVER =
      new AbstractLayer.Interaction<LayerListener, MotionEvent.Impl>() {
    @Override public void interact (LayerListener l, MotionEvent.Impl ev) {
      l.onMouseOver(ev);
    }
  };

  protected AbstractLayer.Interaction<LayerListener, MotionEvent.Impl> OUT =
      new AbstractLayer.Interaction<LayerListener, MotionEvent.Impl>() {
    @Override public void interact (LayerListener l, MotionEvent.Impl ev) {
      l.onMouseOut(ev);
    }
  };

  protected AbstractLayer.Interaction<LayerListener, WheelEvent.Impl> WHEEL_SCROLL =
      new AbstractLayer.Interaction<LayerListener, WheelEvent.Impl>() {
    @Override public void interact (LayerListener l, WheelEvent.Impl ev) {
      l.onMouseWheelScroll(ev);
    }
  };
}
