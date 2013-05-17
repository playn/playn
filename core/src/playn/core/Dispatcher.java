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

import playn.core.AbstractLayer.Interaction;
import playn.core.Events.Flags;
import playn.core.Events.Input;

/** Some internal utilities for dispatching events. */
abstract class Dispatcher {

  static class EventSource extends Flags.Capturable {
    AbstractLayer layer;
    AbstractLayer captured;
    Object capturedListener;
    boolean didCapture;

    void clear () {
      layer = null;
      captured = null;
      capturedListener = null;
      didCapture = false;
    }

    boolean check (boolean wasCaptured, AbstractLayer interacted) {
      if (!wasCaptured && didCapture) {
        captured = interacted;
      }
      return didCapture;
    }

    @Override Flags.Capturable capture () {
      if (!didCapture) {
        didCapture = true;
      } else {
        // TODO: warn about already being captured?
      }
      return this;
    }
  }

  /** Dispatches events to a single layer. */
  static final Dispatcher SINGLE = new Dispatcher() {
    @Override
    public <L, E extends Input.Impl> void dispatch(
        AbstractLayer layer, Class<L> listenerType, E event, Interaction<L, E> interaction,
        Interaction<L, E> cancel) {
      tryInteract(layer, listenerType, interaction, localize(event, layer));
    }
  };

  /** Dispatches events to a layer and all its parents. */
  static final Dispatcher PROPAGATING = new Dispatcher() {
    <L, E extends Input.Impl> void dispatch(
        AbstractLayer inLayer, Class<L> listenerType, E event, Interaction<L, E> interaction,
        Interaction<L, E> cancel) {
      E localized = localize(event, inLayer);
      EventSource esrc = getSource(event);
      if (esrc == null) {
        // make no attempts to capture, etc; just dispatch to layer + parents
        for (AbstractLayer ll = inLayer; ll != null; ll = (AbstractLayer)ll.parent()) {
          tryInteract(ll, listenerType, interaction, localized);
        }
      } else {
        Interaction<L, E> delegator = DELEGATOR.cast();
        if (esrc.captured == null) {
          // no capture yet, dispatch to layer + parents and check for capturings
          DELEGATOR.prepare(interaction).mode = DelegatingInteraction.RECORD_CAPTURE;
          boolean captured = false;
          for (AbstractLayer ll = inLayer; ll != null; ll = (AbstractLayer)ll.parent()) {
            tryInteract(ll, listenerType, delegator, localized);
            captured = esrc.check(captured, ll);
          }

          if (captured) {
            if (cancel != null) {
              // someone captured on this dispatch, cancel everything else
              DELEGATOR.prepare(cancel).mode = DelegatingInteraction.EXCEPT_CAPTURED;
              for (AbstractLayer ll = inLayer; ll != null; ll = (AbstractLayer)ll.parent()) {
                tryInteract(ll, listenerType, delegator, localized);
              }
            } else {
              // TODO: warn that capture is occurring for an unsupported event?
            }
          }
        } else {
          // someone captured on a previous dispatch, divert all events to captured layer
          // TODO: should we update the hit layer?
          DELEGATOR.prepare(interaction).mode = DelegatingInteraction.ONLY_CAPTURED;
          tryInteract(esrc.captured, listenerType, delegator, localized);
        }
      }
    }
  };

  static class DelegatingInteraction
      implements Interaction<Object, Input.Impl> {

    static final int ONLY_CAPTURED = 0, EXCEPT_CAPTURED = 1, RECORD_CAPTURE = 2;

    int mode;
    Interaction<Object, Input.Impl> delegate;

    @SuppressWarnings("unchecked")
    <L, E extends Input.Impl> DelegatingInteraction prepare(
        Interaction<L, E> delegate) {
      this.delegate = (Interaction<Object, Input.Impl>)delegate;
      return this;
    }

    @SuppressWarnings("unchecked")
    <L, E extends Input.Impl> Interaction<L, E> cast () {
      return (Interaction<L, E>)this;
    }

    public void interact(Object listener, Input.Impl event) {
      EventSource esrc = getSource(event);
      switch (mode) {
      case ONLY_CAPTURED:
        if (listener == esrc.capturedListener) {
          delegate.interact(listener, event);
        }
        break;
      case EXCEPT_CAPTURED:
        if (listener != esrc.capturedListener) {
          delegate.interact(listener, event);
        }
        break;
      case RECORD_CAPTURE:
        boolean prevCapture = esrc.didCapture;
        delegate.interact(listener, event);
        if (esrc.didCapture && !prevCapture) {
          esrc.capturedListener = listener;
        }
        break;
      }
    }
  }

  static final DelegatingInteraction DELEGATOR = new DelegatingInteraction();

  static Dispatcher select(boolean propagating) {
    return propagating ? PROPAGATING : SINGLE;
  }

  static <L, E extends Input.Impl> void tryInteract (AbstractLayer layer,
      Class<L> listenerType, Interaction<L, E> interaction, E event) {
    try {
      layer.interact(listenerType, interaction, event);
    } catch (Throwable t) {
      PlayN.log().warn("Interaction failure [layer=" + layer + ", iact=" + interaction +
        ", event=" + event + "]", t);
    }
  }

  /** Sets the event source of the event. This is an implementation detail that allows the
   * dispatcher to track the progress of start/move/cancel interactions of the caller vis-a-vis
   * capturing. Called by control implementations that support capturing. */
  static void setSource (Input.Impl event, EventSource source) {
    ((Flags.Impl)event.flags()).captureState = source;
  }

  static EventSource getSource (Input.Impl event) {
    return (EventSource)((Flags.Impl)event.flags()).captureState;
  }

  @SuppressWarnings("unchecked")
  static <E extends Input.Impl> E localize (E event, AbstractLayer layer) {
    return (E)event.localize(layer);
  }

  /** Issues an interact call to a layer and listener with a localized copy of the
   * given event, using the given cancel interaction if one of the layer listeners calls
   * {@link Flags#capture()} and this is a multi-layer dispatcher */
  abstract <L, E extends Input.Impl> void dispatch(AbstractLayer layer,
      Class<L> listenerType, E event, Interaction<L, E> interaction, Interaction<L, E> cancel);

  /** Issues an interact call to a layer and listener with a localized copy of the
   * given event, using the given cancel interaction if one of the layer listeners calls
   * {@link Flags#capture()} and this is a multi-layer dispatcher */
  <L, E extends Input.Impl> void dispatch(
      Class<L> listenerType, E event, Interaction<L, E> interaction, Interaction<L, E> cancel) {
    dispatch(getSource(event).layer, listenerType, event, interaction, cancel);
  }

  /** Issues an interact call to a layer and listener with a localized copy of the
   * given event. */
  <L, E extends Input.Impl> void dispatch(AbstractLayer layer,
    Class<L> listenerType, E event, Interaction<L, E> interaction) {
    dispatch(layer, listenerType, event, interaction, null);
  }
}
