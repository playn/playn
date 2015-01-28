/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.scene;

import playn.core.Platform;
import pythagoras.f.Point;
import react.Slot;

/**
 * Integrates the layer system with pointer interactions. This supercedes the
 * {@link playn.core.Pointer} service. So you simply create a scene {@code Pointer} instead of a
 * core {@code Pointer}, and it will dispatch both global and layer-local pointer interactions.
 */
public class Pointer extends playn.core.Pointer {

  /** A listener for pointer events with layer info. */
  public static abstract class Listener extends Slot<Object> {

    /** Notifies listener of a pointer start event. */
    public void onStart (Interaction iact) {}

    /** Notifies listener of a pointer drag (move) event. */
    public void onDrag (Interaction iact) {}

    /** Notifies listener of a pointer end event. */
    public void onEnd (Interaction iact) {}

    /** Notifies listener of a pointer cancel event. */
    public void onCancel (Interaction iact) {}

    @Override public final void onEmit (Object event) {
      if (event instanceof Interaction) {
        Interaction iact = (Interaction)event;
        switch (iact.event.kind) {
          case  START: onStart(iact);  break;
          case   DRAG: onDrag(iact);   break;
          case    END: onEnd(iact);    break;
          case CANCEL: onCancel(iact); break;
          default:     break;
        }
      }
    }
  }

  /** Used to dispatch pointer interactions to layers. */
  public static class Interaction extends playn.scene.Interaction<Event> {
    Interaction (Layer hitLayer, boolean bubble) { super(hitLayer, bubble); }

    @Override protected Event newCancelEvent (Event source) {
      return (source == null) ?
        new Event(0, 0, 0, 0, Event.Kind.CANCEL, false) :
        new Event(0, source.time, source.x, source.y, Event.Kind.CANCEL, source.isTouch);
    }
  }

  /** Handles the dispatching of pointer events to layers. */
  public static class Dispatcher extends Slot<Event> {

    private final Layer root;
    private final boolean bubble;
    private final Point scratch = new Point();
    private Interaction currentIact;

    public Dispatcher (Layer root, boolean bubble) {
      this.root = root;
      this.bubble = bubble;
    }

    @Override public void onEmit (Event event) {
      // start a new interaction on START, if we don't already have one
      if (currentIact == null && event.kind.isStart) {
        Layer hitLayer = LayerUtil.getHitLayer(root, scratch.set(event.x, event.y));
        if (hitLayer != null) currentIact = new Interaction(hitLayer, bubble);
      }
      // dispatch the event to the interaction
      if (currentIact != null) currentIact.dispatch(event);
      // if this is END or CANCEL, clear out the current interaction
      if (event.kind.isEnd) currentIact = null;
    }
  }

  /**
   * Creates a pointer event system which dispatches both global pointer events and per-layer
   * pointer events.
   *
   * @param bubble if true, events are "bubbled" up the layer hierarchy, if false they are
   * delivered only to the hit layer. See {@link Dispatcher} for details.
   */
  public Pointer (Platform plat, Layer root, boolean bubble) {
    super(plat);
    events.connect(new Dispatcher(root, bubble));
  }
}
