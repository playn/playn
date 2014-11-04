/**
 * Copyright 2014 The PlayN Authors
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
package playn.robovm;

import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.foundation.NSSet;
import org.robovm.apple.uikit.UIEvent;

import org.robovm.apple.uikit.UITouch;
import pythagoras.f.IPoint;

import playn.core.Events;
import playn.core.PointerImpl;

public class RoboPointer extends PointerImpl {

  private final RoboPlatform platform;

  public RoboPointer(RoboPlatform platform) {
    this.platform = platform;
  }

  void onTouchesBegan(NSSet<UITouch> touches, UIEvent event) {
    Event.Impl ev = toPointerEvent(touches, event);
    if (ev != null)
      onPointerStart(ev, false);
  }

  void onTouchesMoved(NSSet<UITouch> touches, UIEvent event) {
    Event.Impl ev = toPointerEvent(touches, event);
    if (ev != null)
      onPointerDrag(ev, false);
  }

  void onTouchesEnded(NSSet<UITouch> touches, UIEvent event) {
    Event.Impl ev = toPointerEvent(touches, event);
    if (ev != null) {
      onPointerEnd(ev, false);
      _active = 0;
    }
  }

  void onTouchesCancelled(NSSet<UITouch> touches, UIEvent event) {
    Event.Impl ev = toPointerEvent(touches, event);
    if (ev != null) {
      onPointerCancel(ev, false);
      _active = 0;
    }
  }

  private Event.Impl toPointerEvent(NSSet<UITouch> touches, UIEvent event) {
    for (UITouch touch : touches) {
      long handle = touch.getHandle();
      // if we have an active touch, we only care about that touch
      if (_active == 0 || handle == _active) {
        _active = handle;
        CGPoint loc = touch.getLocation(touch.getView());
        // transform the point based on our current scale
        IPoint xloc = platform.graphics().transformTouch((float)loc.x(), (float)loc.y());
        return new Event.Impl(
          new Events.Flags.Impl(), touch.getTimestamp() * 1000, xloc.x(), xloc.y(), true);
      }
    }
    return null;
  }

  private long _active;
}
