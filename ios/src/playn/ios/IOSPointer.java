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
package playn.ios;

import cli.System.Drawing.PointF;

import cli.MonoTouch.Foundation.NSObject;
import cli.MonoTouch.Foundation.NSSet;
import cli.MonoTouch.Foundation.NSSetEnumerator;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UITouch;

import pythagoras.f.IPoint;

import playn.core.Events;
import playn.core.PointerImpl;

public class IOSPointer extends PointerImpl {

  private final IOSGraphics graphics;

  public IOSPointer(IOSGraphics graphics) {
    this.graphics = graphics;
  }

  void onTouchesBegan(NSSet touches, UIEvent event) {
    Event.Impl ev = toPointerEvent(touches, event);
    if (ev != null)
      onPointerStart(ev, false);
  }

  void onTouchesMoved(NSSet touches, UIEvent event) {
    Event.Impl ev = toPointerEvent(touches, event);
    if (ev != null)
      onPointerDrag(ev, false);
  }

  void onTouchesEnded(NSSet touches, UIEvent event) {
    Event.Impl ev = toPointerEvent(touches, event);
    if (ev != null) {
      onPointerEnd(ev, false);
      _active = null;
    }
  }

  void onTouchesCancelled(NSSet touches, UIEvent event) {
    Event.Impl ev = toPointerEvent(touches, event);
    if (ev != null) {
      _active = null;
    }
  }

  private Event.Impl toPointerEvent(NSSet touches, UIEvent event) {
    final Event.Impl[] eventw = new Event.Impl[1];
    touches.Enumerate(new NSSetEnumerator(new NSSetEnumerator.Method() {
      public void Invoke (NSObject obj, boolean[] stop) {
        UITouch touch = (UITouch) obj;
        // if we have an active touch, we only care about that touch
        if (_active != null && touch != _active) {
          stop[0] = false;
        } else {
          _active = touch;
          PointF loc = touch.LocationInView(touch.get_View());
          // transform the point based on our current orientation and scale
          IPoint xloc = graphics.transformTouch(loc.get_X(), loc.get_Y());
          eventw[0] = new Event.Impl(
            new Events.Flags.Impl(), touch.get_Timestamp() * 1000, xloc.x(), xloc.y(), true);
          stop[0] = true;
        }
      }
    }));
    return eventw[0];
  }

  private UITouch _active;
}
