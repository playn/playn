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

  private boolean enabled = true;
  private Dispatcher dispatcher = Dispatcher.SINGLE;
  private Listener listener;
  private final Dispatcher.CaptureState active = new Dispatcher.CaptureState();

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
  public void cancelLayerDrags() {
    if (active.layer != null) {
      Event.Impl event = new Event.Impl(new Events.Flags.Impl(), PlayN.currentTime(), 0, 0, false);
      event.captureState = active;
      dispatcher.dispatch(Listener.class, event, CANCEL, null);
      active.clear();
    }
  }

  public void setPropagateEvents(boolean propagate) {
    dispatcher = Dispatcher.select(propagate);
  }

  protected boolean onPointerStart(Event.Impl event, boolean preventDefault) {
    if (!enabled)
      return preventDefault;

    event.flags().setPreventDefault(preventDefault);
    if (listener != null) {
      listener.onPointerStart(event);
    }

    GroupLayer root = PlayN.graphics().rootLayer();
    if (root.interactive()) {
      Point p = new Point(event.x(), event.y());
      root.transform().inverseTransform(p, p);
      p.x += root.originX();
      p.y += root.originY();
      active.layer = (AbstractLayer)root.hitTest(p);
      if (active.layer != null) {
        event.captureState = active;
        dispatcher.dispatch(Listener.class, event, START, CANCEL);
      }
    }
    return event.flags().getPreventDefault();
  }

  protected boolean onPointerDrag(Event.Impl event, boolean preventDefault) {
    if (!enabled)
      return preventDefault;

    event.flags().setPreventDefault(preventDefault);
    if (listener != null) {
      listener.onPointerDrag(event);
    }

    if (active.layer != null) {
      event.captureState = active;
      dispatcher.dispatch(Listener.class, event, DRAG, CANCEL);
    }
    return event.flags().getPreventDefault();
  }

  protected boolean onPointerEnd(Event.Impl event, boolean preventDefault) {
    if (!enabled)
      return preventDefault;

    event.flags().setPreventDefault(preventDefault);
    if (listener != null) {
      listener.onPointerEnd(event);
    }

    if (active.layer != null) {
      event.captureState = active;
      dispatcher.dispatch(Listener.class, event, END, null);
      active.clear();
    }
    return event.flags().getPreventDefault();
  }

  protected boolean onPointerCancel(Event.Impl event, boolean preventDefault) {
    if (!enabled)
      return preventDefault;

    event.flags().setPreventDefault(preventDefault);
    if (listener != null) {
      listener.onPointerCancel(event);
    }

    if (active.layer != null) {
      event.captureState = active;
      dispatcher.dispatch(Listener.class, event, CANCEL, null);
      active.clear();
    }
    return event.flags().getPreventDefault();
  }

  protected AbstractLayer.Interaction<Listener, Event.Impl> START =
      new AbstractLayer.Interaction<Listener, Event.Impl>() {
    public void interact(Listener l, Event.Impl ev) {
      l.onPointerStart(ev);
    }
  };

  protected AbstractLayer.Interaction<Listener, Event.Impl> DRAG =
      new AbstractLayer.Interaction<Listener, Event.Impl>() {
    public void interact(Listener l, Event.Impl ev) {
      l.onPointerDrag(ev);
    }
  };

  protected AbstractLayer.Interaction<Listener, Event.Impl> END =
      new AbstractLayer.Interaction<Listener, Event.Impl>() {
    public void interact(Listener l, Event.Impl ev) {
      l.onPointerEnd(ev);
    }
  };

  protected AbstractLayer.Interaction<Listener, Event.Impl> CANCEL =
      new AbstractLayer.Interaction<Listener, Event.Impl>() {
    public void interact(Listener l, Event.Impl ev) {
      l.onPointerCancel(ev);
    }
  };
}
