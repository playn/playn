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

import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UIApplicationDelegate;

/**
 * PlayN iOS games must extend this class for their AppDelegate. It wires up the appropriate
 * lifecycle events.
 */
public class IOSApplicationDelegate extends UIApplicationDelegate {

  private IOSPlatform platform;

  void setPlatform(IOSPlatform platform) {
    this.platform = platform;
  }

  @Override
  public void WillEnterForeground(UIApplication app) {
    if (platform != null) {
      platform.willEnterForeground();
    }
  }

  @Override
  public void OnActivated(UIApplication app) {
    if (platform != null) {
      platform.onActivated();
    }
  }

  @Override
  public void OnResignActivation(UIApplication app) {
    if (platform != null) {
      platform.onResignActivation();
    }
  }

  @Override
  public void DidEnterBackground(UIApplication app) {
    if (platform != null) {
      platform.didEnterBackground();
    }
  }

  @Override
  public void WillTerminate(UIApplication app) {
    if (platform != null) {
      platform.willTerminate();
    }
  }
}
