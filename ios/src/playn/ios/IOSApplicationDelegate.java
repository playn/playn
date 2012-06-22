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
  public void OnActivated(UIApplication app) {
    // UIApplicationDelegate specifically disallows calling super here
    if (platform != null) {
      platform.invokeLater(new Runnable() {
        public void run() {
          platform.onResume();
        }
      });
    }
  }

  @Override
  public void OnResignActivation(UIApplication app) {
    // UIApplicationDelegate specifically disallows calling super here
    if (platform != null) {
      // we call this directly because routing it through the GL thread results in iOS thinking
      // that we're done and can be suspended immediately; the GL thread will already have been
      // suspended at this point, so race conditions should not be an issue (modulo invisible
      // changes sitting in another CPU's cache which we can do nothing about)
      platform.onPause();
    }
  }

  @Override
  public void WillTerminate(UIApplication app) {
    // UIApplicationDelegate specifically disallows calling super here
    if (platform != null) {
      // we call this directly for the same reason as onPause
      platform.onExit();
    }
  }
}
