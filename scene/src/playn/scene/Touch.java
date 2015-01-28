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

import java.util.HashMap;
import java.util.Map;

import pythagoras.f.Point;
import react.Slot;

/**
 * Integrates the layer system with touch interactions. To receive touch events on layers, connect
 * the touch event dispatcher as a global touch listener like so:
 * {@code platform.input().touchEvents.connect(new Touch.Dispatcher(...))}.
 */
public class Touch extends playn.core.Touch {

  /** A listener for touch events with layer info. */
  public static abstract class Listener extends Slot<Object> {

    /** Notifies listener of a touch start event. */
    public void onStart (Interaction iact) {}

    /** Notifies listener of a touch move event. */
    public void onMove (Interaction iact) {}

    /** Notifies listener of a touch end event. */
    public void onEnd (Interaction iact) {}

    /** Notifies listener of a touch cancel event. */
    public void onCancel (Interaction iact) {}

    @Override public final void onEmit (Object event) {
      if (event instanceof Interaction) {
        Interaction iact = (Interaction)event;
        switch (iact.event.kind) {
          case  START: onStart(iact);  break;
          case   MOVE: onMove(iact);   break;
          case    END: onEnd(iact);    break;
          case CANCEL: onCancel(iact); break;
          default:     break;
        }
      }
    }
  }

  /** Used to dispatch touch interactions to layers. */
  public static class Interaction extends playn.scene.Interaction<Event> {
    Interaction (Layer hitLayer, boolean bubble) { super(hitLayer, bubble); }

    @Override protected Event newCancelEvent (Event source) {
      return (source == null) ?
        new Event(0, 0, 0, 0, Event.Kind.CANCEL, 0) :
        new Event(0, source.time, source.x, source.y, Event.Kind.CANCEL,
                  source.id, source.pressure, source.size);
    }
  }

  /** Handles the dispatching of touch events to layers. */
  public static class Dispatcher extends Slot<Event[]> {

    private final Layer root;
    private final boolean bubble;
    private final Point scratch = new Point();
    private final Map<Integer,Interaction> activeIacts = new HashMap<>();

    public Dispatcher (Layer root, boolean bubble) {
      this.root = root;
      this.bubble = bubble;
    }

    @Override public void onEmit (Event[] events) {
      // each event has an id which defines the interaction of which it is a part
      for (Event event : events) {
        // start a new interaction for this id if START and we don't already have one
        Interaction iact = activeIacts.get(event.id);
        if (iact == null && event.kind.isStart) {
          Layer hitLayer = LayerUtil.getHitLayer(root, scratch.set(event.x, event.y));
          if (hitLayer != null) activeIacts.put(event.id, iact = new Interaction(hitLayer, bubble));
        }

        // dispatch the event to the interaction
        if (iact != null) iact.dispatch(event);

        // if this is END or CANCEL, clear out the interaction for this id
        if (event.kind.isEnd) activeIacts.remove(event.id);
      }
    }
  }
}
