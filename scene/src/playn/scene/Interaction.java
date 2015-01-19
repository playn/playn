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

import playn.core.Event;
import pythagoras.f.Point;

/**
 * Contains information about the interaction of which an event is a part.
 */
public class Interaction<E extends Event.XY> {

  private Layer dispatchLayer;
  private Layer capturingLayer;

  /** The layer that was hit at the start of this interaction. */
  public final Layer hitLayer;

  /** The current event's location, translated into the hit layer's coordinate space. */
  public final Point local = new Point();

  /** The event currently being dispatched in this interaction. */
  public E event;

  /** Returns {@link #event}'s x coordinate, for convenience. */
  public float x () {
    return event.x;
  }

  /** Returns {@link #event}'s y coordinate, for convenience. */
  public float y () {
    return event.y;
  }

  /** Returns whether this interaction is captured. */
  public boolean captured () {
    return capturingLayer != null;
  }

  /** Captures this interaction. See {@link Dispatcher}. */
  public void capture () {
    assert dispatchLayer != null;
    if (capturingLayer != dispatchLayer && captured()) throw new IllegalStateException(
      "Interaction already captured by " + capturingLayer);
    capturingLayer = dispatchLayer;
    // TODO: notify all non-capturing layers of cancel
  }

  public Interaction (Layer hitLayer) {
    assert hitLayer != null;
    this.hitLayer = hitLayer;
  }

  void dispatch (E event, boolean bubble) {
    assert event != null;
    LayerUtil.screenToLayer(hitLayer, local.set(event.x, event.y), local);
    this.event = event;
    try {
      if (bubble) {
        for (Layer target = hitLayer; target != null; target = target.parent()) {
          if (capturingLayer != null && target != capturingLayer) continue;
          if (target.hasEventListeners()) dispatch(target);
        }
      } else {
        if (hitLayer.hasEventListeners()) dispatch(hitLayer);
      }
    } finally { this.event = null; }
  }

  void dispatch (Layer layer) {
    dispatchLayer = layer;
    try { layer.events().emit(this); }
    finally { dispatchLayer = null; }
  }
}
