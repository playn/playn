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

import cli.MonoTouch.Foundation.NSUrl;
import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UIScreen;
import cli.MonoTouch.UIKit.UIWindow;
import cli.System.DateTime;
import cli.System.Drawing.RectangleF;

import playn.core.Game;
import playn.core.Json;
import playn.core.Keyboard;
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
    IOSPlatform platform = new IOSPlatform(app);
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

  private IOSPlatform(UIApplication app) {
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
    mainWindow.Add(gameView = new IOSGameView(bounds, scale));
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
    // NOTE: the CLR epoch is "00:00:00, 1/1/0001" not "00:00:00, 1/1/1970"; we should probably
    // change the PlayN.currentTime() interface to return millis since app start rather than millis
    // since epoch; if a game wants to do (limited) date processing, it can create a Date()

    // get_Ticks returns 100 nanosecond ticks; turn that into millis
    return DateTime.get_Now().get_Ticks()/10000d;
  }

  @Override
  public Type type() {
    return Type.IOS;
  }

  void update(float delta) {
    // log.debug("Update " + delta);

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
}
