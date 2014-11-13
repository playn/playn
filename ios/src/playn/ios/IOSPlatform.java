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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cli.MonoTouch.CoreAnimation.CAAnimation;
import cli.MonoTouch.Foundation.NSAction;
import cli.MonoTouch.Foundation.NSNotification;
import cli.MonoTouch.Foundation.NSNotificationCenter;
import cli.MonoTouch.Foundation.NSObject;
import cli.MonoTouch.Foundation.NSString;
import cli.MonoTouch.Foundation.NSTimer;
import cli.MonoTouch.Foundation.NSUrl;
import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UIDeviceOrientation;
import cli.MonoTouch.UIKit.UIInterfaceOrientation;
import cli.MonoTouch.UIKit.UIScreen;
import cli.MonoTouch.UIKit.UIView;
import cli.MonoTouch.UIKit.UIViewController;
import cli.MonoTouch.UIKit.UIWindow;
import cli.System.Drawing.RectangleF;
import cli.System.Threading.ThreadPool;
import cli.System.Threading.WaitCallback;

import playn.core.AbstractPlatform;
import playn.core.Game;
import playn.core.Json;
import playn.core.Mouse;
import playn.core.MouseStub;
import playn.core.PlayN;
import playn.core.json.JsonImpl;

/**
 * Provides access to all the PlayN services on iOS.
 */
public class IOSPlatform extends AbstractPlatform {

  /** Defines the orientations supported by your app. */
  public enum SupportedOrients {
    /** Supports portrait and portrait upside down orients. */
    PORTRAITS(UIDeviceOrientation.Portrait) {
      @Override
      public boolean isSupported(UIDeviceOrientation orient) {
        return ((orient.Value == UIDeviceOrientation.Portrait) ||
                (orient.Value == UIDeviceOrientation.PortraitUpsideDown));
      }
    },

    /** Supports landscape left and right orients. */
    LANDSCAPES(UIDeviceOrientation.LandscapeRight) {
      @Override
      public boolean isSupported(UIDeviceOrientation orient) {
        return ((orient.Value == UIDeviceOrientation.LandscapeLeft) ||
                (orient.Value == UIDeviceOrientation.LandscapeRight));
      }
    },

    /** Supports both portrait and landscape orients. */
    ALL(UIDeviceOrientation.Portrait) {
      @Override
      public boolean isSupported(UIDeviceOrientation orient) {
        return ((orient.Value == UIDeviceOrientation.Portrait) ||
                (orient.Value == UIDeviceOrientation.PortraitUpsideDown) ||
                (orient.Value == UIDeviceOrientation.LandscapeLeft) ||
                (orient.Value == UIDeviceOrientation.LandscapeRight));
      }
    };

    public final UIDeviceOrientation defaultOrient;

    public abstract boolean isSupported(UIDeviceOrientation orient);

    SupportedOrients(int defaultOrient) {
      this.defaultOrient = UIDeviceOrientation.wrap(defaultOrient);
    }
  }

  // TODO: this should be generalized and shared among platforms that do orientation
  public interface OrientationChangeListener {
    void orientationChanged(int orientationValue);
  }

  /** Used to configure the iOS platform. */
  public static class Config {
    /** Indicates which orients are supported by your app. You should also configure this
     * information in your {@code Info.plist} file. */
    public SupportedOrients orients = SupportedOrients.PORTRAITS;

    /** If true, an iPad will be treated like a 2x Retina device with resolution 384x512 and which
     * will use {@code @2x} images. A Retina iPad will also have resolution 384x512 and will use
     * {@code @4x} images if they exist, then fall back to {@code @2x} (and default (1x) if
     * necessary). If false, iPad will be treated as a non-Retina device with resolution 768x1024
     * and will use default (1x) images, and a Retina iPad will be treated as a Retina device with
     * resolution 768x1024 and will use {@code @2x} images. */
    public boolean iPadLikePhone = false;

    /** Indicates the frequency at which the game should be rendered (and updated). Defaults to
     * one, which means one render per device screen refresh (maximum FPS). Higher values (like 2)
     * can be used to reduce the update rate to half or third FPS for games that can't run at full
     * FPS. As the iOS docs say: a game that runs at a consistent but slow frame rate is better
     * than a game that runs at an erratic frame rate. */
    public int frameInterval = 1;

    /** If true, calls to CanvasImage.draw() on a retina device using a non-retina image as the
     * source will use the default interpolation defined for CGBitmapContext. This will potentially
     * make scaled non-retina images look better, but has performance and pixel accuracy
     * implications. */
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
    public double timeForTermination = 0.5;

    /** Indicates that PlayN is to be embedded in a larger iOS app. This disables the default
     * lifecycle listeners. The main app must call {@link IOSPlatform#activate} when the view
     * containing the PlayN app is about to be shown, and {@link IOSPlatform#terminate} when the
     * view goes away. Note that while PlayN is activated, it will automatically listen for and
     * handle background and foreground notifications, so those need not be performed manually. */
    public boolean embedded = false;

    /** Dictates the name of the temporary file used by {@link IOSStorage}. Configure this if you
     * want to embed multiple games into your application. */
    public String storageFileName = "playn.db";
  }

  /**
   * Registers your application using the default configuration.
   */
  public static IOSPlatform register(UIApplication app) {
    return register(app, new Config());
  }

  /**
   * Registers your application using the supplied configuration.
   */
  public static IOSPlatform register(UIApplication app, Config config) {
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
   * root view controller is needed, your application should subclass {@link IOSRootViewController}
   * or replicate its functionality in your root view controller.
   */
  public static IOSPlatform register(UIApplication app, UIWindow window, Config config) {
    IOSPlatform platform = new IOSPlatform(app, window, config);
    PlayN.setPlatform(platform);
    return platform;
  }

  static {
    // disable output to System.out/err as that will result in a crash due to iOS disallowing
    // writes to stdout/stderr
    OutputStream noop = new OutputStream() {
      @Override
      public void write(int b) throws IOException {} // noop!
      @Override
      public void write(byte b[], int off, int len) throws IOException {} // noop!
    };
    System.setOut(new PrintStream(noop));
    System.setErr(new PrintStream(noop));
  }

  private IOSAudio audio; // lazily initialized
  private final IOSGraphics graphics;
  private final Json json;
  private final IOSKeyboard keyboard;
  private final IOSNet net;
  private final IOSPointer pointer;
  private final IOSStorage storage;
  private final IOSTouch touch;
  private final IOSAssets assets;

  private Game game;

  private final Config config;
  private final UIApplication app;
  private final UIWindow mainWindow;
  private final IOSRootViewController rootViewController;
  private final IOSGameView gameView;
  private final double start = CAAnimation.CurrentMediaTime();
  private final List<NSObject> lifecycleObservers = new ArrayList<NSObject>();

  private int currentOrientation;

  private OrientationChangeListener orientationChangeListener;

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
  public UIView gameView() {
    return gameView;
  }

  /** Returns the orientations we're configured to support. */
  public SupportedOrients supportedOrients() {
    return config.orients;
  }

  /** Configures a listener that is notified when the game orientation changes. */
  public void setOrientationChangeListener(OrientationChangeListener listener) {
    orientationChangeListener = listener;
    dispatchOrientationChange(currentOrientation);
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

  protected IOSPlatform(UIApplication app, UIWindow window, Config config) {
    super(new IOSLog());
    this.app = app;
    this.config = config;

    float deviceScale = UIScreen.get_MainScreen().get_Scale();
    RectangleF bounds = UIScreen.get_MainScreen().get_Bounds();
    int screenWidth = (int)bounds.get_Width(), screenHeight = (int)bounds.get_Height();
    boolean useHalfSize = (screenWidth >= 768) && config.iPadLikePhone;
    float viewScale = (useHalfSize ? 2 : 1) * deviceScale;
    if (useHalfSize) {
      screenWidth /= 2;
      screenHeight /= 2;
    }

    graphics = new IOSGraphics(this, screenWidth, screenHeight, viewScale, deviceScale,
      config.interpolateCanvasDrawing);
    json = new JsonImpl();
    keyboard = new IOSKeyboard(this);
    net = new IOSNet(this);
    pointer = new IOSPointer(graphics);
    touch = new IOSTouch(graphics);
    assets = new IOSAssets(this);
    storage = new IOSStorage(config.storageFileName);

    mainWindow = (window == null) ? new UIWindow(bounds) : window;
    gameView = new IOSGameView(this, bounds, deviceScale);
    rootViewController = new IOSRootViewController(this, gameView);
    mainWindow.set_RootViewController(rootViewController);

    // if we're not in embedded mode, register our lifecycle observers
    if (!config.embedded) registerLifecycleObservers();

    // use the status bar orientation during startup. The device orientation will not be known
    // for some time and most games will want to show a "right side up" loading screen, i.e.
    // matching the iOS "default" splash
    int sorient = UIApplication.get_SharedApplication().get_StatusBarOrientation().Value;
    UIDeviceOrientation dorient = null;
    for (Map.Entry<UIDeviceOrientation, UIInterfaceOrientation> e : ORIENT_MAP.entrySet()) {
      if (e.getValue().Value == sorient) {
        dorient = e.getKey();
        break;
      }
    }
    // if it isn't supported, use the game's default
    if (dorient == null || !config.orients.isSupported(dorient)) {
      dorient = config.orients.defaultOrient;
    }
    onOrientationChange(dorient);
  }

  @Override
  public void invokeAsync(final Runnable action) {
    ThreadPool.QueueUserWorkItem(new WaitCallback(new WaitCallback.Method() {
      public void Invoke(Object unused) {
        try {
          action.run();
        } catch (Throwable t) {
          reportError("Async task failure [task=" + action + "]", t);
        }
      }
    }));
  }

  @Override
  public Type type() {
    return Type.IOS;
  }

  @Override
  public IOSAssets assets() {
    return assets;
  }

  @Override
  public IOSAudio audio() {
    if (audio == null) audio = new IOSAudio(this, config.openALSources);
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
  public IOSNet net() {
    return net;
  }

  @Override
  public Mouse mouse() {
    return new MouseStub();
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
  public IOSStorage storage() {
    return storage;
  }

  @Override
  public double time() {
    return System.currentTimeMillis();
  }

  @Override
  public int tick() {
    return (int)((CAAnimation.CurrentMediaTime() - start) * 1000);
  }

  @Override
  public void openURL(String url) {
    if (!app.OpenUrl(new NSUrl(url))) {
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
    // start the main game loop
    gameView.RunWithFrameInterval(config.frameInterval);
    // make our main window visible
    mainWindow.MakeKeyAndVisible();
  }

  void viewDidInit(int defaultFramebuffer) {
    graphics.ctx.viewDidInit(defaultFramebuffer);
  }

  void onOrientationChange(UIDeviceOrientation orientation) {
    if (orientation.Value == currentOrientation) return; // NOOP
    if (!config.orients.isSupported(orientation))
      return; // ignore unsupported (or Unknown) orientations

    currentOrientation = orientation.Value;
    graphics.setOrientation(orientation);

    UIInterfaceOrientation sorient = ORIENT_MAP.get(orientation);
    if (!sorient.equals(app.get_StatusBarOrientation())) {
      app.SetStatusBarOrientation(sorient, !app.get_StatusBarHidden());
    }
    dispatchOrientationChange(orientation.Value);
  }

  void dispatchOrientationChange (int orientationValue) {
    if (orientationChangeListener == null) {
        return;
    }
    orientationChangeListener.orientationChanged(orientationValue);
  }

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
    gameView.onActivated();
  }
  private void willEnterForeground() {
    invokeLater(new Runnable() {
      public void run() {
        onResume();
      }
    });
  }
  private void willResignActive () {
    gameView.onResignActivation();
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
      NSNotificationCenter.get_DefaultCenter().RemoveObserver(obs);
    }
    lifecycleObservers.clear();

    // wait for the desired interval and then terminate the GL and AL systems
    NSTimer.CreateScheduledTimer(config.timeForTermination, new NSAction(new NSAction.Method() {
      @Override public void Invoke() {
        // stop the GL view
        gameView.Stop();
        // stop and release the AL resources (if audio was ever initialized)
        if (audio != null) audio.terminate();
        // clear out the platform in order to make sure the game creation flow can be repeated when
        // it is used as a part of a larger application
        PlayN.setPlatform(null);
      }
    }));
  }

  private void registerLifecycleObservers() {
    // observe lifecycle events (we deviate from "standard code style" here to make it easier to
    // ignore the repeated boilerplate and see the actual important bits)
    observeLifecycle(UIApplication.get_DidBecomeActiveNotification(),
                     new Runnable() { public void run () { didBecomeActive(); }});
    observeLifecycle(UIApplication.get_WillEnterForegroundNotification(),
                     new Runnable() { public void run () { willEnterForeground(); }});
    observeLifecycle(UIApplication.get_WillResignActiveNotification(),
                     new Runnable() { public void run () { willResignActive(); }});
    observeLifecycle(UIApplication.get_DidEnterBackgroundNotification(),
                     new Runnable() { public void run () { didEnterBackground(); }});
    observeLifecycle(UIApplication.get_WillTerminateNotification(),
                     new Runnable() { public void run () { willTerminate(); }});
  }
  private void observeLifecycle (NSString event, final Runnable action) {
    // avert your eyes from this horrible abomination; I'm not even going to try to wrap the code
    // in any sort of sensible way
    lifecycleObservers.add(NSNotificationCenter.get_DefaultCenter().AddObserver(event, new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_(new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_.Method() {
      @Override public void Invoke(NSNotification n) {
        action.run();
      }
    })));
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
