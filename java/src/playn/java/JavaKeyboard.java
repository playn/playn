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

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import playn.core.Event;
import playn.core.Key;

public abstract class JavaKeyboard extends playn.core.Keyboard {

  private final Deque<Event> queue = new ConcurrentLinkedDeque<>();
  private final JavaPlatform plat;

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
  public void post (Key key, boolean pressed, char typedCh) {
    queue.add(key == null ? new TypedEvent(0, 0, typedCh) : new KeyEvent(0, 0, key, pressed));
  }

  public JavaKeyboard(JavaPlatform plat) {
    this.plat = plat;
  }

  @Override public boolean hasHardwareKeyboard() {
    return true;
  }

  abstract void init ();

  void update() {
    Event event;
    while ((event = queue.poll()) != null) events.emit(event);
  }
}
