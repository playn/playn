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
package playn.tests.robovm;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationDelegateAdapter;
import org.robovm.apple.uikit.UIApplicationLaunchOptions;
import org.robovm.apple.uikit.UIInterfaceOrientationMask;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIWindow;

import playn.robovm.RoboPlatform;
import playn.tests.core.TestsGame;

public class TestsGameRoboVM extends UIApplicationDelegateAdapter {

  @Override
  public boolean didFinishLaunching (UIApplication app, UIApplicationLaunchOptions launchOpts) {
    // create a full-screen window
    CGRect bounds = UIScreen.getMainScreen().getBounds();
    UIWindow window = new UIWindow(bounds);

    // configure and register the PlayN platform
    RoboPlatform.Config config = new RoboPlatform.Config();
    config.orients = UIInterfaceOrientationMask.All;
    RoboPlatform pf = RoboPlatform.create(window, config);

    // create and initialize our game
    TestsGame game = new TestsGame(pf, new String[0]);

    // make our main window visible
    window.makeKeyAndVisible();
    addStrongRef(window);
    return true;
  }

  public static void main (String[] args) {
    NSAutoreleasePool pool = new NSAutoreleasePool();
    UIApplication.main(args, null, TestsGameRoboVM.class);
    pool.close();
  }
}
