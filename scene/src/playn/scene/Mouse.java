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

import pythagoras.f.Point;
import react.Slot;

/**
 * Integrates the layer system with mouse interactions. To receive mouse events on layers, connect
 * the mouse event dispatcher as a global mouse listener like so:
 * {@code platform.input().mouseEvents.connect(new Mouse.Dispatcher(...))}.
 */
public class Mouse extends playn.core.Mouse {

  /** An event dispatched when the mouse enters or exits a layer. */
  public static class HoverEvent extends Event {

    /** Whether the mouse is now inside or outside the layer in question. */
    public final boolean inside;

    public HoverEvent (int flags, double time, float x, float y, boolean inside) {
      super(flags, time, x, y);
      this.inside = inside;
    }

    @Override protected String name () {
      return "Hover";
    }

    @Override protected void addFields (StringBuilder builder) {
      super.addFields(builder);
      builder.append(", inside=").append(inside);
    }
  }

  /** A listener for mouse button, motion and wheel events with layer info. */
  public static abstract class Listener extends Slot<Object> {

    /** Notifies listener of a mouse motion event. A motion event is dispatched when no button is
      * currently pressed, in an isolated "one shot" interaction, and always goes to the layer hit
      * by the event coordinates. */
    public void onMotion (MotionEvent event, Interaction iact) {}

    /** Notifies listener of mouse entry or exit. Hover events are dispatched in an isolated "one
      * shot" interaction, regardless of whether there is currently a button-triggered interaction
      * in progress, and always got to the layer whose hover status changed. */
    public void onHover (HoverEvent event, Interaction iact) {}

    /** Notifies listener of a mouse button event. A button down event will start an interaction if
      * no interaction is already in progress, or will be dispatched to the hit layer of the
      * current interaction if an interaction is in progress. If additional buttons are pressed
      * during an interaction, the interaction does not end until <em>all</em> of the buttons are
      * released. */
    public void onButton (ButtonEvent event, Interaction iact) {}

    /** Notifies listener of a mouse drag event. A drag event is dispatched when a button event has
      * started an interaction, and always goes to the layer hit by the button event that started
      * the interaction, <em>not</em> to the layer intersected by the motion event coordinates. */
    public void onDrag (MotionEvent event, Interaction iact) {}

    /** Notifies listener of a mouse wheel event. If no interaction is in progress, the wheel event
      * is dispatched to the layer intersected by the event coordinates, but if an interaction is
      * in progress, the event goes to the layer hit by the event that started the interaction. */
    public void onWheel (WheelEvent event, Interaction iact) {}

    /** Notifies the listener that the current interaction was canceled. This is dispatched when
      * some other layer that was also privy to this interaction has captured the interaction. */
    public void onCancel () {}

    @Override public final void onEmit (Object event) {
      if (event instanceof Interaction) ((Interaction)event).emit(this);
      else if (event == cancelEvent) onCancel();
    }
  }

  /** Used to dispatch mouse interactions to layers. */
  public static class Interaction extends playn.scene.Interaction<Event> {

    private final boolean solo;
    Interaction (Layer hitLayer, boolean bubble, boolean solo) {
      super(hitLayer, bubble);
      this.solo = solo;
    }

    private int buttons;
    void add (ButtonEvent.Id button) {
      buttons |= (1 << button.ordinal());
    }
    boolean remove (ButtonEvent.Id button) {
      buttons &= ~(1 << button.ordinal());
      return buttons == 0;
    }

    void emit (Listener lner) {
      Event mevent = event;
      if (mevent instanceof ButtonEvent) {
        lner.onButton((ButtonEvent)mevent, this);
      } else if (mevent instanceof MotionEvent) {
        if (solo) lner.onMotion((MotionEvent)mevent, this);
        else lner.onDrag((MotionEvent)mevent, this);
      } else if (mevent instanceof HoverEvent) {
        lner.onHover((HoverEvent)mevent, this);
      } else if (mevent instanceof WheelEvent) {
        lner.onWheel((WheelEvent)mevent, this);
      }
    }

    @Override protected Event newCancelEvent (Event source) {
      return cancelEvent;
    }
  }

  /** Handles the dispatching of mouse events to layers. */
  public static class Dispatcher extends Slot<Event> {

    private final Layer root;
    private final boolean bubble;
    private final Point scratch = new Point();
    private Interaction currentIact;
    private Layer hoverLayer;

    public Dispatcher (Layer root, boolean bubble) {
      this.root = root;
      this.bubble = bubble;
    }

    @Override public void onEmit (Event event) {
      if (event instanceof ButtonEvent) {
        ButtonEvent bevent = (ButtonEvent)event;
        if (bevent.down) {
          // if we have no current interaction, start one
          if (currentIact == null) {
            Layer hitLayer = LayerUtil.getHitLayer(root, scratch.set(event.x, event.y));
            if (hitLayer != null) currentIact = new Interaction(hitLayer, bubble, false);
          }
          if (currentIact != null) {
            currentIact.add(bevent.button);
            currentIact.dispatch(event);
          }
        }
        // if we have no current interaction, that's weird, but maybe the app somehow missed the
        // button down event, so just dispatch this event solo
        else if (currentIact == null) dispatchSolo(event);
        // otherwise dispatch this mouse up to the current interaction and end it if there are no
        // longer any buttons pressed therein
        else {
          boolean done = currentIact.remove(bevent.button);
          currentIact.dispatch(event);
          if (done) currentIact = null;
        }

      } else if (event instanceof MotionEvent) {
        // we always compute the hit layer because we need to hover events
        Layer hitLayer = LayerUtil.getHitLayer(root, scratch.set(event.x, event.y));
        // if we have a current interaction, dispatch a drag event
        if (currentIact != null) currentIact.dispatch(event);
        // otherwise dispatch the mouse motion event solo
        else if (hitLayer != null) new Interaction(hitLayer, bubble, true).dispatch(event);

        // dispatch hover events if the hit layer changed
        if (hitLayer != hoverLayer) {
          if (hoverLayer != null) {
            HoverEvent hevent = new HoverEvent(0, event.time, event.x, event.y, false);
            new Interaction(hoverLayer, bubble, true).dispatch(hevent);
          }
          hoverLayer = hitLayer;
          if (hitLayer != null) {
            HoverEvent hevent = new HoverEvent(0, event.time, event.x, event.y, true);
            new Interaction(hitLayer, bubble, true).dispatch(hevent);
          }
        }

      } else if (event instanceof WheelEvent) {
        // if we have a current interaction, dispatch to that
        if (currentIact != null) currentIact.dispatch(event);
        // otherwise create a one-shot interaction and dispatch it
        else dispatchSolo(event);
      }
    }

    private void dispatchSolo (Event event) {
      Layer hitLayer = LayerUtil.getHitLayer(root, scratch.set(event.x, event.y));
      if (hitLayer != null) new Interaction(hitLayer, bubble, true).dispatch(event);
    }
  }

  protected static class CancelEvent extends Event {
    public CancelEvent () { super(0, 0, 0, 0); }
  }
  protected static final CancelEvent cancelEvent = new CancelEvent();
}
