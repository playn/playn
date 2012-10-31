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

import cli.MonoTouch.Foundation.NSSet;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UIViewController;

/**
 * Handles dispatching events on the root view, and other view controller things. The entry point
 * to many stock iOS things is the root view controller, so we need to provide one even though it's
 * not strictly necessary to meet basic PlayN needs.
 */
public class IOSRootViewController extends UIViewController
{
  private final IOSPlatform platform;

  public IOSRootViewController(IOSPlatform platform) {
    this.platform = platform;
  }

  @Override
  public void TouchesBegan(NSSet touches, UIEvent event) {
    super.TouchesBegan(touches, event);
    platform.touch().onTouchesBegan(touches, event);
    platform.pointer().onTouchesBegan(touches, event);
  }

  @Override
  public void TouchesMoved(NSSet touches, UIEvent event) {
    super.TouchesMoved(touches, event);
    platform.touch().onTouchesMoved(touches, event);
    platform.pointer().onTouchesMoved(touches, event);
  }

  @Override
  public void TouchesEnded(NSSet touches, UIEvent event) {
    super.TouchesEnded(touches, event);
    platform.touch().onTouchesEnded(touches, event);
    platform.pointer().onTouchesEnded(touches, event);
  }

  @Override
  public void TouchesCancelled(NSSet touches, UIEvent event) {
    super.TouchesCancelled(touches, event);
    platform.touch().onTouchesCancelled(touches, event);
    platform.pointer().onTouchesCancelled(touches, event);
  }
}
