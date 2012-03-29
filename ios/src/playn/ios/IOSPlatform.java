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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cli.System.Drawing.RectangleF;

import cli.MonoTouch.Foundation.NSSet;
import cli.MonoTouch.Foundation.NSUrl;
import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UIDeviceOrientation;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UIInterfaceOrientation;
import cli.MonoTouch.UIKit.UIScreen;
import cli.MonoTouch.UIKit.UIViewController;
import cli.MonoTouch.UIKit.UIWindow;

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

  private Set<Integer> supportedOrients = new HashSet<Integer>();
  private final List<Runnable> pendingActions = new ArrayList<Runnable>();

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
    IOSViewController ctrl = new IOSViewController();
    ctrl.Add(gameView = new IOSGameView(bounds, scale));
    mainWindow.set_RootViewController(ctrl);
  }

  /**
   * Configures the orientations supported by your game.
   */
  public void setSupportedOrientations(boolean portrait, boolean landscapeRight,
                                       boolean upsideDown, boolean landscapeLeft) {
    supportedOrients.clear();
    if (portrait)
      supportedOrients.add(UIDeviceOrientation.Portrait);
    if (landscapeRight)
      supportedOrients.add(UIDeviceOrientation.LandscapeRight);
    if (landscapeLeft)
      supportedOrients.add(UIDeviceOrientation.LandscapeLeft);
    if (upsideDown)
      supportedOrients.add(UIDeviceOrientation.PortraitUpsideDown);
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
    return new Mouse() {
      public void setListener(Listener listener) {
        log().warn("Mouse not supported on iOS.");
      }
    };
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

  void onOrientationChange(UIDeviceOrientation orientation) {
    if (!supportedOrients.contains(orientation.Value))
      return; // ignore unsupported (or Unknown) orientations
    graphics.setOrientation(orientation);
    app.SetStatusBarOrientation(ORIENT_MAP.get(orientation), false);
    // TODO: notify the game of the orientation change
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

  protected class IOSViewController extends UIViewController {
    @Override
    public void TouchesBegan(NSSet touches, UIEvent event) {
      super.TouchesBegan(touches, event);
      touch().onTouchesBegan(touches, event);
      pointer().onTouchesBegan(touches, event);
    }

    @Override
    public void TouchesMoved(NSSet touches, UIEvent event) {
      super.TouchesMoved(touches, event);
      touch().onTouchesMoved(touches, event);
      pointer().onTouchesMoved(touches, event);
    }

    @Override
    public void TouchesEnded(NSSet touches, UIEvent event) {
      super.TouchesEnded(touches, event);
      touch().onTouchesEnded(touches, event);
      pointer().onTouchesEnded(touches, event);
    }

    @Override
    public void TouchesCancelled(NSSet touches, UIEvent event) {
      super.TouchesCancelled(touches, event);
      touch().onTouchesCancelled(touches, event);
      pointer().onTouchesCancelled(touches, event);
    }
  }

  protected static final Map<UIDeviceOrientation,UIInterfaceOrientation> ORIENT_MAP =
    new HashMap<UIDeviceOrientation,UIInterfaceOrientation>();
  static {
    ORIENT_MAP.put(UIDeviceOrientation.wrap(UIDeviceOrientation.Portrait),
                   UIInterfaceOrientation.wrap(UIInterfaceOrientation.Portrait));
    ORIENT_MAP.put(UIDeviceOrientation.wrap(UIDeviceOrientation.PortraitUpsideDown),
                   UIInterfaceOrientation.wrap(UIInterfaceOrientation.PortraitUpsideDown));
    // nb: these are swapped, because of some cracksmoking at Apple
    ORIENT_MAP.put(UIDeviceOrientation.wrap(UIDeviceOrientation.LandscapeLeft),
                   UIInterfaceOrientation.wrap(UIInterfaceOrientation.LandscapeRight));
    ORIENT_MAP.put(UIDeviceOrientation.wrap(UIDeviceOrientation.LandscapeRight),
                   UIInterfaceOrientation.wrap(UIInterfaceOrientation.LandscapeLeft));
  }
}
