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

import java.util.ArrayList;
import java.util.List;

import cli.MonoTouch.Foundation.NSUrl;
import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UIScreen;
import cli.MonoTouch.UIKit.UIViewController;
import cli.MonoTouch.UIKit.UIWindow;
import cli.System.DateTime;
import cli.System.Drawing.RectangleF;

import playn.core.Game;
import playn.core.Json;
import playn.core.Mouse;
import playn.core.Platform;
import playn.core.PlayN;
import playn.core.RegularExpression;
import playn.core.json.JsonImpl;

/**
 * Provides access to all the PlayN services on iOS.
 */
public class IOSPlatform implements Platform {

  public static IOSPlatform register(UIApplication app) {
    return register(app, null);
  }

  public static IOSPlatform register(UIApplication app, UIViewController ctrl) {
    IOSPlatform platform = new IOSPlatform(app, ctrl);
    PlayN.setPlatform(platform);
    return platform;
  }

  static IOSPlatform instance;

  private IOSAudio audio;
  private IOSGraphics graphics;
  private Json json;
  private IOSKeyboard keyboard;
  private IOSLog log;
  private IOSNet net;
  private IOSPointer pointer;
  private IOSStorage storage;
  private IOSTouch touch;
  private IOSAssets assets;
  private IOSAnalytics analytics;

  private Game game;
  private float accum, alpha;

  private final UIApplication app;
  private final UIWindow mainWindow;
  private final IOSGameView gameView;

  private final List<Runnable> pendingActions = new ArrayList<Runnable>();

  private IOSPlatform(UIApplication app, UIViewController ctrl) {
    this.app = app;
    RectangleF bounds = UIScreen.get_MainScreen().get_Bounds();
    float scale = 1f; // TODO: UIScreen.get_MainScreen().get_Scale();

    // create log first so that other services can use it during initialization
    log = new IOSLog();

    instance = this;
    audio = new IOSAudio();
    graphics = new IOSGraphics(bounds, scale);
    json = new JsonImpl();
    keyboard = new IOSKeyboard();
    net = new IOSNet();
    pointer = new IOSPointer();
    touch = new IOSTouch();
    assets = new IOSAssets();
    analytics = new IOSAnalytics();
    storage = new IOSStorage();

    mainWindow = new UIWindow(bounds);
    if (ctrl != null)
      mainWindow.set_RootViewController(ctrl);
    mainWindow.Add(gameView = new IOSGameView(bounds, scale));
  }

  /**
   * Configures the orientations supported by your game.
   */
  public void setSupportedOrientations(boolean portrait, boolean landscapeRight,
                                       boolean upsideDown, boolean landscapeLeft) {
    graphics.setSupportedOrientations(portrait, landscapeRight, upsideDown, landscapeLeft);
  }

  @Override
  public IOSAssets assets() {
    return assets;
  }

  @Override
  public IOSAnalytics analytics() {
    return analytics;
  }

  @Override
  public IOSAudio audio() {
    return audio;
  }

  @Override
  public IOSGraphics graphics() {
    return graphics;
  }

  @Override
  public Json json() {
    return json;
  }

  @Override
  public IOSKeyboard keyboard() {
    return keyboard;
  }

  @Override
  public IOSLog log() {
    return log;
  }

  @Override
  public IOSNet net() {
    return net;
  }

  @Override
  public void openURL(String url) {
    if (!app.OpenUrl(new NSUrl(url))) {
      log().warn("Failed to open URL: " + url);
    }
  }

  @Override
  public Mouse mouse() {
    return null;
  }

  @Override
  public IOSTouch touch() {
    return touch;
  }

  @Override
  public IOSPointer pointer() {
    return pointer;
  }

  @Override
  public float random() {
    return (float) Math.random();
  }

  @Override
  public RegularExpression regularExpression() {
    return null; // new IOSRegularExpression();
  }

  @Override
  public void run(Game game) {
    this.game = game;
    // start the main game loop (TODO: support 0 update rate)
    gameView.Run(1000d / game.updateRate());
    // make our main window visible
    mainWindow.MakeKeyAndVisible();
    // initialize the game and start things off
    game.init();
  }

  @Override
  public IOSStorage storage() {
    return storage;
  }

  @Override
  public double time() {
    return System.currentTimeMillis();
  }

  @Override
  public Type type() {
    return Type.IOS;
  }

  void update(float delta) {
    // log.debug("Update " + delta);

    // process any pending actions
    List<Runnable> actions = null;
    synchronized (pendingActions) {
      if (!pendingActions.isEmpty()) {
        actions = new ArrayList<Runnable>(pendingActions);
        pendingActions.clear();
      }
    }
    if (actions != null) {
      for (Runnable action : actions) {
        try {
          action.run();
        } catch (Exception e) {
          log().warn("Pending action failed", e);
        }
      }
    }

    // perform the game updates
    float updateRate = game.updateRate();
    if (updateRate == 0) {
      game.update(delta);
      accum = 0;
    } else {
      accum += delta;
      while (accum >= updateRate) {
        game.update(updateRate);
        accum -= updateRate;
      }
    }

    // save the alpha, we'll get a call to paint later
    alpha = (updateRate == 0) ? 0 : accum / updateRate;
  }

  void paint() {
    // log.debug("Paint " + alpha);
    graphics.paint(game, alpha);
  }

  /** Queues an action to be executed before the next {@link #update}. */
  void queueAction(Runnable r) {
    synchronized (pendingActions) {
      pendingActions.add(r);
    }
  }
}
