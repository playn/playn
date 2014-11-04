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

import java.util.ArrayList;
import java.util.List;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSInvocation;
import org.robovm.apple.foundation.NSNotificationCenter;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.foundation.NSTimer;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.glkit.GLKView;
import org.robovm.apple.opengles.EAGLContext;
import org.robovm.apple.opengles.EAGLRenderingAPI;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIInterfaceOrientation;
import org.robovm.apple.uikit.UIInterfaceOrientationMask;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIUserInterfaceIdiom;
import org.robovm.apple.uikit.UIView;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.apple.uikit.UIWindow;

import playn.core.*;
import playn.core.json.JsonImpl;

public class RoboPlatform extends AbstractPlatform {

  /** Used to configure the RoboVM platform. */
  public static class Config {
    /** Indicates which orients are supported by your app. You should also configure this
      * information in your {@code Info.plist} file. */
    public UIInterfaceOrientationMask orients = UIInterfaceOrientationMask.Portrait;

    /** If true, an iPad will be treated like a 2x Retina device with resolution 384x512 and which
      * will use @2x images. A Retina iPad will also have resolution 384x512 and will use @4x
      * images if they exist, then fall back to @2x (and default (1x) if necessary). If false, iPad
      * will be treated as a non-Retina device with resolution 768x1024 and will use default (1x)
      * images, and a Retina iPad will be treated as a Retina device with resolution 768x1024 and
      * will use @2x images. */
    public boolean iPadLikePhone = false;

    /** Indicates the frequency at which the game should be rendered (and updated). Defaults to
      * one, which means one render per device screen refresh (maximum FPS). Higher values (like 2)
      * can be used to reduce the update rate to half or third FPS for games that can't run at full
      * FPS. As the iOS docs say: a game that runs at a consistent but slow frame rate is better
      * than a game that runs at an erratic frame rate. */
    public int frameInterval = 1;

    /** If true, calls to CanvasImage.draw() on a retina device using a non-retina image as the
      * source will use the default interpolation defined for CGBitmapContext. This will
      * potentially make scaled non-retina images look better, but has performance and pixel
      * accuracy implications. */
    public boolean interpolateCanvasDrawing = true;

    /** The number of audio channels to reserve for OpenAL. This dictates the number of
      * simultaneous sounds that can be played via OpenAL. It can't be higher than 32, and can be
      * reduced from the default of 24 if you plan to play a lot of compressed sound effects
      * simultaneously (those don't go through OpenAL, they go through AVAudioPlayer, and I presume
      * AVAudioPlayer competes with OpenAL for sound channels). */
    public int openALSources = 24;

    /** Seconds to wait for the game loop to terminate before terminating GL and AL services. This
      * is only used if PlayN is integrated into a larger iOS application and does not control the
      * application lifecycle. */
    public float timeForTermination = 0.5f;

    /** Indicates that PlayN is to be embedded in a larger iOS app. This disables the default
      * lifecycle listeners. The main app must call {@link RoboPlatform#activate} when the view
      * containing the PlayN app is about to be shown, and {@link RoboPlatform#terminate} when the
      * view goes away. Note that while PlayN is activated, it will automatically listen for and
      * handle background and foreground notifications, so those need not be performed manually. */
    public boolean embedded = false;

    /** Dictates the name of the temporary file used by {@link RoboStorage}. Configure this if you
      * want to embed multiple games into your application. */
    public String storageFileName = "playn.db";
  }

  /**
   * Registers your application using the default configuration.
   */
  public static RoboPlatform register(UIApplication app) {
    return register(app, new Config());
  }

  /**
   * Registers your application using the supplied configuration.
   */
  public static RoboPlatform register(UIApplication app, Config config) {
    return register(app, null, config);
  }

  /**
   * Registers your application using the supplied configuration and window.
   *
   * The window is used for a game integrated as a part of application. An iOS application
   * typically just works on one screen so that the game has to share the window created by other
   * controllers (typically created by the story board). If no window is specified, the platform
   * will create one taking over the whole application.
   *
   * Note that PlayN will still install a RootViewController on the supplied UIWindow. If a custom
   * root view controller is needed, your application should subclass {@link RoboRootViewController}
   * or replicate its functionality in your root view controller.
   */
  public static RoboPlatform register(UIApplication app, UIWindow window, Config config) {
    RoboPlatform platform = new RoboPlatform(app, window, config);
    PlayN.setPlatform(platform);
    return platform;
  }

  /** Returns the top-level UIWindow. */
  public UIWindow window() {
    return mainWindow;
  }

  /** Returns the controller for the root view. */
  public UIViewController rootViewController() {
    return rootViewController;
  }

  /** Returns the main game view. You can add subviews to this view if you wish to overlay views
   * onto your game. */
  public GLKView gameView() {
    return rootViewController.view;
  }

  /** Manually activates the PlayN platform. This is for use by applications which are embedding
    * PlayN into a larger iOS app. {@link Config#embedded} must also be true in that case. */
  public void activate() {
    if (!config.embedded) throw new IllegalStateException(
      "Config.embedded must be true to enable manual lifecycle control");
    registerLifecycleObservers();
    didBecomeActive();
  }

  /** Manually terminates the PlayN platform. This is for use by applications which are embedding
    * PlayN into a larger iOS app. {@link Config#embedded} must also be true in that case. */
  public void terminate() {
    if (!config.embedded) throw new IllegalStateException(
      "Config.embedded must be true to enable manual lifecycle control");
    willTerminate();
  }

  private RoboAudio audio; // lazily initialized
  private final RoboGraphics graphics;
  private final Json json;
  private final RoboKeyboard keyboard;
  private final RoboNet net;
  private final RoboPointer pointer;
  private final RoboStorage storage;
  private final RoboTouch touch;
  private final RoboAssets assets;

  private Game game;

  private final UIApplication app;
  private final UIWindow mainWindow;
  private final RoboRootViewController rootViewController;
  private final long gameStart = System.nanoTime();
  private final List<NSObject> lifecycleObservers = new ArrayList<NSObject>();

  final int osVersion = getOSVersion();
  final Config config;

  protected RoboPlatform(UIApplication app, UIWindow window, Config config) {
    super(new RoboLog());
    this.app = app;
    this.config = config;

    // create our EAGLContext and set up our GL view
    EAGLContext ctx = new EAGLContext(EAGLRenderingAPI.OpenGLES2);
    CGRect bounds = UIScreen.getMainScreen().getBounds();
    mainWindow = (window == null) ? new UIWindow(bounds) : window;
    rootViewController = new RoboRootViewController(this, ctx, mainWindow);
    mainWindow.setRootViewController(rootViewController);

    graphics = new RoboGraphics(this, mainWindow);
    json = new JsonImpl();
    keyboard = new RoboKeyboard(this);
    net = new RoboNet(this);
    pointer = new RoboPointer(this);
    touch = new RoboTouch(this);
    assets = new RoboAssets(this);
    storage = new RoboStorage(config.storageFileName);

    // if we're not in embedded mode, register our lifecycle observers
    if (!config.embedded) registerLifecycleObservers();

    // // use the status bar orientation during startup. The device orientation will not be known
    // // for some time and most games will want to show a "right side up" loading screen, i.e.
    // // matching the iOS "default" splash
    // UIInterfaceOrientation sorient = app.getStatusBarOrientation();
    // UIDeviceOrientation dorient = null;
    // if (!config.orients.contains(sorient)) {
    //   dorient = 
    // }
    // for (Map.Entry<UIDeviceOrientation, UIInterfaceOrientation> e : ORIENT_MAP.entrySet()) {
    //   if (e.getValue() == sorient) {
    //     dorient = e.getKey();
    //     break;
    //   }
    // }
    // // if it isn't supported, use the game's default
    // if (dorient == null || !config.orients.isSupported(dorient)) {
    //   dorient = config.orients.defaultOrient;
    // }
    // onOrientationChange(dorient);
  }

  @Override
  public Type type() {
    return Type.IOS;
  }

  @Override
  public RoboAssets assets() {
    return assets;
  }

  @Override
  public RoboAudio audio() {
    if (audio == null) audio = new RoboAudio(this, config.openALSources);
    return audio;
  }

  @Override
  public RoboGraphics graphics() {
    return graphics;
  }

  @Override
  public Json json() {
    return json;
  }

  @Override
  public RoboKeyboard keyboard() {
    return keyboard;
  }

  @Override
  public RoboNet net() {
    return net;
  }

  @Override
  public Mouse mouse() {
    return new MouseStub();
  }

  @Override
  public RoboTouch touch() {
    return touch;
  }

  @Override
  public RoboPointer pointer() {
    return pointer;
  }

  @Override
  public float random() {
    return (float) Math.random();
  }

  @Override
  public RoboStorage storage() {
    return storage;
  }

  @Override
  public double time() {
    return System.currentTimeMillis();
  }

  @Override
  public int tick() {
    return (int)((System.nanoTime() - gameStart) / 1000000);
  }

  @Override
  public void openURL(String url) {
    if (!app.openURL(new NSURL(url))) {
      log().warn("Failed to open URL: " + url);
    }
  }

  @Override
  public void setPropagateEvents(boolean propagate) {
    touch.setPropagateEvents(propagate);
    pointer.setPropagateEvents(propagate);
  }

  @Override
  public void run(Game game) {
    this.game = game;
    // initialize the game and start things off
    game.init();
    // make our main window visible
    mainWindow.makeKeyAndVisible();
  }

  // void onOrientationChange(UIDeviceOrientation orientation) {
  //   if (orientation.Value == currentOrientation) return; // NOOP
  //   if (!config.orients.isSupported(orientation))
  //     return; // ignore unsupported (or Unknown) orientations

  //   currentOrientation = orientation.Value;
  //   graphics.setOrientation(orientation);

  //   UIInterfaceOrientation sorient = ORIENT_MAP.get(orientation);
  //   if (!sorient.equals(app.get_StatusBarOrientation())) {
  //     app.SetStatusBarOrientation(sorient, !app.get_StatusBarHidden());
  //   }
  //   dispatchOrientationChange(orientation.Value);
  // }

  // void dispatchOrientationChange (int orientationValue) {
  //   if (orientationChangeListener == null) {
  //       return;
  //   }
  //   orientationChangeListener.orientationChanged(orientationValue);
  // }

  void update() {
    // process pending actions
    runQueue.execute();
    // perform the game updates
    game.tick(tick());
    // flush any pending draw calls (to surfaces)
    graphics.ctx().flush();
  }

  void paint() {
    graphics.paint();
  }

  // lifecycle callbacks
  private void didBecomeActive() {
    // gameView.onActivated();
  }
  private void willEnterForeground() {
    invokeLater(new Runnable() {
      public void run() {
        onResume();
      }
    });
  }
  private void willResignActive () {
    // gameView.onResignActivation();
  }
  private void didEnterBackground () {
    // we call this directly rather than via invokeLater() because the PlayN thread is already
    // stopped at this point so a) there's no point in worrying about racing with that thread,
    // and b) onPause would never get called, since the PlayN thread is not processing events
    onPause();
  }
  private void willTerminate () {
    // let the app know that we're terminating
    onExit();

    // terminate our lifecycle observers
    for (NSObject obs : lifecycleObservers) {
      NSNotificationCenter.getDefaultCenter().removeObserver(obs);
    }
    lifecycleObservers.clear();

    // wait for the desired interval and then terminate the GL and AL systems
    NSTimer.scheduledTimerWithTimeInterval$invocation$repeats$(
      config.timeForTermination, new NSInvocation() {
        @Override public void invoke() {
          // stop the GL view
          // gameView.Stop();
          // stop and release the AL resources (if audio was ever initialized)
          if (audio != null) audio.terminate();
          // clear out the platform in order to make sure the game creation flow can be repeated when
          // it is used as a part of a larger application
          PlayN.setPlatform(null);
        }
      }, false);
  }

  private int getOSVersion () {
    String systemVersion = UIDevice.getCurrentDevice().getSystemVersion();
    int version = Integer.parseInt(systemVersion.split("\\.")[0]);
    return version;
  }

  private void registerLifecycleObservers() {
    // observe lifecycle events (we deviate from "standard code style" here to make it easier to
    // ignore the repeated boilerplate and see the actual important bits)
    UIApplication.Notifications.observeDidBecomeActive(new Runnable() {
      public void run () { didBecomeActive(); }});
    UIApplication.Notifications.observeWillEnterForeground(new Runnable() {
      public void run () { willEnterForeground(); }});
    UIApplication.Notifications.observeWillResignActive(new Runnable() {
      public void run () { willResignActive(); }});
    UIApplication.Notifications.observeDidEnterBackground(new Runnable() {
      public void run () { didEnterBackground(); }});
    UIApplication.Notifications.observeWillTerminate(new Runnable() {
      public void run () { willTerminate(); }});
  }
}
