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

import playn.core.Events;
import playn.core.TouchImpl;
import pythagoras.f.IPoint;

public class RoboTouch extends TouchImpl {

  private final RoboPlatform platform;

  public RoboTouch(RoboPlatform platform) {
    this.platform = platform;
  }

  void onTouchesBegan(NSSet<UITouch> touches, UIEvent event) {
    onTouchStart(toTouchEvents(touches, event));
  }

  void onTouchesMoved(NSSet<UITouch> touches, UIEvent event) {
    onTouchMove(toTouchEvents(touches, event));
  }

  void onTouchesEnded(NSSet<UITouch> touches, UIEvent event) {
    onTouchEnd(toTouchEvents(touches, event));
  }

  void onTouchesCancelled(NSSet<UITouch> touches, UIEvent event) {
    onTouchCancel(toTouchEvents(touches, event));
  }

  private Event.Impl[] toTouchEvents(NSSet<UITouch> touches, UIEvent event) {
    final Event.Impl[] events = new Event.Impl[touches.size()];
    int idx = 0;
    for (UITouch touch : touches) {
      CGPoint loc = touch.getLocation(touch.getView());
      // transform the point based on our current scale
      IPoint xloc = platform.graphics().transformTouch((float)loc.x(), (float)loc.y());
      // on iOS the memory address of the UITouch object is the unique id
      int id = (int)touch.getHandle();
      events[idx++] = new Event.Impl(
        new Events.Flags.Impl(), touch.getTimestamp() * 1000, xloc.x(), xloc.y(), id);
    }
    return events;
  }
}
