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
package playn.core;

import react.RFuture;
import react.Signal;

/**
 * Provides information about user input: mouse, touch, and keyboard. This class provides the
 * platform-specific code, and events are dispatched via the platform-independent {@link Mouse},
 * {@link Touch} and {@link Keyboard} classes.
 */
public class Input {

  /** Enables or disables mouse interaction.
    * No mouse events will be dispatched whilst this big switch is in the off position. */
  public boolean mouseEnabled = true;

  /** Enables or disables touch interaction.
    * No touch events will be dispatched whilst this big switch is in the off position. */
  public boolean touchEnabled = true;

  /** Enables or disables keyboard interaction.
    * No keyboard events will be dispatched whilst this big switch is in the off position. */
  public boolean keyboardEnabled = true;

  /** A signal which emits mouse events. */
  public Signal<Mouse.Event> mouseEvents = Signal.create();

  /** A signal via which touch events are emitted. */
  public Signal<Touch.Event[]> touchEvents = Signal.create();

  /** A signal via which keyboard events are emitted. */
  public Signal<Keyboard.Event> keyboardEvents = Signal.create();

  /** Returns true if this platform has mouse input. */
  public boolean hasMouse () { return false; }

  /** Returns true if this platform has touch input. */
  public boolean hasTouch () { return false; }

  /**
   * Returns true if this device has a hardware keyboard, false if not. Devices that lack a
   * hardware keyboard will generally not generate keyboard events. Older Android devices that
   * support four hardware buttons are an exception. Use {@link #getText} for text entry on a
   * non-hardware-keyboard having device.
   */
  public boolean hasHardwareKeyboard () { return false; }

  /**
   * Returns true if this platform supports mouse locking. The user may still block it when it is
   * requested, or detection may be broken for some browsers.
   */
  public boolean hasMouseLock () { return false; }

  /**
   * Returns whether the mouse is currently locked.
   */
  public boolean isMouseLocked () { return false; }

  /**
   * Lock or unlock the mouse. When the mouse is locked, mouse events are still received even when
   * the pointer leaves the game window.
   */
  public void setMouseLocked (boolean locked) {} // noop!

  /**
   * Requests a line of text from the user. On platforms that have only a virtual keyboard, this
   * will display a text entry interface, obtain the line of text, and dismiss the text entry
   * interface when finished.
   *
   * @param textType the expected type of text. On mobile devices this hint may be used to display a
   * keyboard customized to the particular type of text.
   * @param label a label to display over the text entry interface, may be null.
   * @param initialValue the initial value to display in the text input field, may be null.
   * @return a future which provides the text when it becomes available. If the user cancels the
   * text entry process, null is supplied. Otherwise the entered text is supplied.
   */
  public RFuture<String> getText (Keyboard.TextType textType, String label, String initialValue) {
    return RFuture.failure(new Exception("getText not supported"));
  }

  protected int modifierFlags (boolean altP, boolean ctrlP, boolean metaP, boolean shiftP) {
    int flags = 0;
    if (altP)   flags |= Event.F_ALT_PRESSED;
    if (ctrlP)  flags |= Event.F_CTRL_PRESSED;
    if (metaP)  flags |= Event.F_META_PRESSED;
    if (shiftP) flags |= Event.F_SHIFT_PRESSED;
    return flags;
  }

  protected void emitKeyPress (double time, Key key, boolean down, int flags) {
    Keyboard.KeyEvent event = new Keyboard.KeyEvent(0, time, key, down);
    event.setFlag(flags);
    keyboardEvents.emit(event);
  }
  protected void emitKeyTyped (double time, char keyChar) {
    keyboardEvents.emit(new Keyboard.TypedEvent(0, time, keyChar));
  }

  protected void emitMouseButton (double time, float x, float y, Mouse.ButtonEvent.Id btn,
                                  boolean down, int flags) {
    Mouse.ButtonEvent event = new Mouse.ButtonEvent(0, time, x, y, btn, down);
    event.setFlag(flags);
    mouseEvents.emit(event);
  }
  protected void emitMouseMotion (double time, float x, float y, float dx, float dy, int flags) {
    Mouse.MotionEvent event = new Mouse.MotionEvent(0, time, x, y, dx, dy);
    event.setFlag(flags);
    mouseEvents.emit(event);
  }
  protected void emitMouseWheel (double time, float x, float y, int delta, int flags) {
    Mouse.WheelEvent event = new Mouse.WheelEvent(0, time, x, y, delta);
    event.setFlag(flags);
    mouseEvents.emit(event);
  }
}
