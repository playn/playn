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
import java.util.HashMap;
import java.util.Map;

import cli.System.DateTime;
import cli.System.Drawing.RectangleF;
import cli.System.Threading.ThreadPool;
import cli.System.Threading.WaitCallback;

import cli.MonoTouch.Foundation.NSUrl;
import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UIDeviceOrientation;
import cli.MonoTouch.UIKit.UIInterfaceOrientation;
import cli.MonoTouch.UIKit.UIScreen;
import cli.MonoTouch.UIKit.UIView;
import cli.MonoTouch.UIKit.UIViewController;
import cli.MonoTouch.UIKit.UIWindow;

import playn.core.AbstractPlatform;
import playn.core.Game;
import playn.core.Json;
import playn.core.Mouse;
import playn.core.MouseStub;
import playn.core.PlayN;
import playn.core.RegularExpression;
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
     * will use @2x images. A Retina iPad will also have resolution 384x512 and will use @4x images
     * if they exist, then fall back to @2x (and default (1x) if necessary). If false, iPad will be
     * treated as a non-Retina device with resolution 768x1024 and will use default (1x) images,
     * and a Retina iPad will be treated as a Retina device with resolution 768x1024 and will use
     * @2x images. */
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
     * implications.
     */
    public boolean interpolateCanvasDrawing = true;
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
    IOSPlatform platform = new IOSPlatform(app, config);
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

  private final IOSAudio audio;
  private final IOSGraphics graphics;
  private final Json json;
  private final IOSKeyboard keyboard;
  private final IOSNet net;
  private final IOSPointer pointer;
  private final IOSStorage storage;
  private final IOSTouch touch;
  private final IOSAssets assets;
  private final IOSAnalytics analytics;

  private Game game;

  private final SupportedOrients orients;
  private final int frameInterval;
  private final UIApplication app;
  private final UIWindow mainWindow;
  private final IOSRootViewController rootViewController;
  private final IOSGameView gameView;
  private final long start = DateTime.get_Now().get_Ticks();

  private int currentOrientation;

  private OrientationChangeListener orientationChangeListener;

  /** Returns the top-level UIWindow. */
  public UIWindow window () {
    return mainWindow;
  }

  /** Returns the controller for the root view. */
  public UIViewController rootViewController() {
    return rootViewController;
  }

  protected IOSPlatform(UIApplication app, Config config) {
    super(new IOSLog());
    this.app = app;
    this.orients = config.orients;
    this.frameInterval = config.frameInterval;

    float deviceScale = UIScreen.get_MainScreen().get_Scale();
    RectangleF bounds = UIScreen.get_MainScreen().get_Bounds();
    int screenWidth = (int)bounds.get_Width(), screenHeight = (int)bounds.get_Height();
    boolean useHalfSize = (screenWidth >= 768) && config.iPadLikePhone;
    float viewScale = (useHalfSize ? 2 : 1) * deviceScale;
    if (useHalfSize) {
      screenWidth /= 2;
      screenHeight /= 2;
    }

    audio = new IOSAudio(this);
    graphics = new IOSGraphics(this, screenWidth, screenHeight, viewScale, deviceScale,
      config.interpolateCanvasDrawing);
    json = new JsonImpl();
    keyboard = new IOSKeyboard(this);
    net = new IOSNet(this);
    pointer = new IOSPointer(graphics);
    touch = new IOSTouch(graphics);
    assets = new IOSAssets(this);
    analytics = new IOSAnalytics();
    storage = new IOSStorage();

    mainWindow = new UIWindow(bounds);
    gameView = new IOSGameView(this, bounds, deviceScale);
    rootViewController = new IOSRootViewController(this, gameView);
    mainWindow.set_RootViewController(rootViewController);

    // if the game supplied a proper delegate, configure it (for lifecycle notifications)
    if (app.get_Delegate() instanceof IOSApplicationDelegate)
      ((IOSApplicationDelegate) app.get_Delegate()).setPlatform(this);

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
    if (dorient == null || !orients.isSupported(dorient)) {
      dorient = orients.defaultOrient;
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
          log.warn("Async task failure [task=" + action + "]", t);
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
  public RegularExpression regularExpression() {
    return null; // new IOSRegularExpression();
  }

  @Override
  public IOSStorage storage() {
    return storage;
  }

  /** Returns the orientations we're configured to support. */
  public SupportedOrients supportedOrients() {
    return orients;
  }

  @Override
  public double time() {
    return System.currentTimeMillis();
  }

  @Override
  public int tick() {
    return (int)((DateTime.get_Now().get_Ticks() - start) / 10000);
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
    gameView.RunWithFrameInterval(frameInterval);
    // make our main window visible
    mainWindow.MakeKeyAndVisible();
  }

  public UIView gameView () {
    return gameView;
  }

  public void setOrientationChangeListener (OrientationChangeListener listener) {
    orientationChangeListener = listener;
    dispatchOrientationChange(currentOrientation);
  }

  // make these accessible to IOSApplicationDelegate
  @Override
  protected void onPause() {
    super.onPause();
    gameView.onPause();
  }
  @Override
  protected void onResume() {
    super.onResume();
    gameView.onResume();
  }
  @Override
  protected void onExit() {
    super.onExit();
  }

  void viewDidInit(int defaultFrameBuffer) {
    graphics.ctx.viewDidInit(defaultFrameBuffer);
  }

  void onOrientationChange(UIDeviceOrientation orientation) {
    if (orientation.Value == currentOrientation) return; // NOOP
    if (!orients.isSupported(orientation))
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
