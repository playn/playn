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

import playn.core.PlayN;
import playn.core.Pointer;

class IOSPointer implements Pointer
{
  private Listener listener;

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  void onTouchesBegan(NSSet touches, UIEvent event) {
    if (listener != null) {
      listener.onPointerStart(toPointerEvent(touches, event));
    }
  }

  void onTouchesMoved(NSSet touches, UIEvent event) {
    if (listener != null) {
      listener.onPointerDrag(toPointerEvent(touches, event));
    }
  }

  void onTouchesEnded(NSSet touches, UIEvent event) {
    if (listener != null) {
      listener.onPointerEnd(toPointerEvent(touches, event));
    }
  }

  void onTouchesCancelled(NSSet touches, UIEvent event) {
    if (listener != null) {
      // TODO: ???
    }
  }

  private Pointer.Event toPointerEvent(NSSet touches, UIEvent event) {
    final Pointer.Event[] eventw = new Pointer.Event[1];
    touches.Enumerate(new NSSetEnumerator(new NSSetEnumerator.Method() {
      public void Invoke (NSObject obj, boolean[] stop) {
        UITouch touch = (UITouch) obj;
        PointF loc = touch.LocationInView(touch.get_View());
        eventw[0] = new Pointer.Event.Impl(touch.get_Timestamp(), loc.get_X(), loc.get_Y(), true);
        stop[0] = true;
      }
    }));
    return eventw[0];
  }
}
