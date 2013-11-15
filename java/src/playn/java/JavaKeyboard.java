/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import playn.core.Events;
import playn.core.Key;
import playn.core.Touch;

public abstract class JavaKeyboard implements playn.core.Keyboard {

  private Listener[] listeners = {null};
  private final List<Queued<?>> queue = Collections.synchronizedList(new ArrayList<Queued<?>>());

  protected final Dispatcher<Event> down = new Dispatcher<Event>() {
    public void send (Listener l, Event e) { l.onKeyDown(e); }
  };
  protected final Dispatcher<Event> up = new Dispatcher<Event>() {
    public void send (Listener l, Event e) { l.onKeyUp(e); }
  };
  protected final Dispatcher<TypedEvent> typed = new Dispatcher<TypedEvent>() {
    public void send (Listener l, TypedEvent e) { l.onKeyTyped(e); }
  };

  /** Posts a key event received from elsewhere (i.e. a native UI component). This is useful for
   * applications that are using GL in Canvas mode and sharing keyboard focus with other (non-GL)
   * components. The event will be queued and dispatched on the next frame, after GL keyboard
   * events.
   *
   * <p><em>Note</em>: the resulting event will be sent with time = 0, since the GL event time is
   * inaccessible and platform dependent.</p>
   *
   * @param key the key that was pressed or released, or null for a char typed event
   * @param pressed whether the key was pressed or released, ignored if key is null
   * @param typedCh the character that was typed, ignored if key is not null
   */
  public void post(Key key, boolean pressed, char typedCh) {
    queue.add(
      key == null ?
      new Queued<TypedEvent>(new TypedEvent.Impl(new Events.Flags.Impl(), 0, typedCh), typed) :
      new Queued<Event>(new Event.Impl(new Events.Flags.Impl(), 0, key), pressed ? down : up));
  }

  @Override
  public void setListener(Listener listener) {
    listeners[0] = listener;
  }

  @Override
  public boolean hasHardwareKeyboard() {
    return true;
  }

  void init(Touch touch) {
    // let our friend the touch emulator have key messages too
    if (touch instanceof JavaEmulatedTouch)
      listeners = new Listener[] { listeners[0], ((JavaEmulatedTouch)touch).keyListener };
  }

  void update() {
    while (!queue.isEmpty())
      queue.remove(0).dispatch();
  }

  protected <E extends Events.Input> void dispatch (E e, Dispatcher<E> d) {
    for (Listener l : listeners) if (l != null) d.send(l, e);
  }

  protected interface Dispatcher<E extends Events.Input> {
    void send (Listener l, E e);
  }

  protected class Queued<E extends Events.Input> {
    final E event;
    final Dispatcher<E> dispatcher;

    Queued (E event, Dispatcher<E> dispatcher) {
      this.event = event;
      this.dispatcher = dispatcher;
    }

    void dispatch () {
      JavaKeyboard.this.dispatch(event, dispatcher);
    }
  }
}
