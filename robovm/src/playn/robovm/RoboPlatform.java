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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSThread;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.foundation.NSOperationQueue;
import org.robovm.apple.glkit.GLKViewDrawableColorFormat;
import org.robovm.apple.opengles.EAGLContext;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIInterfaceOrientationMask;
import org.robovm.apple.uikit.UIWindow;
import org.robovm.objc.block.VoidBlock1;

import playn.core.*;
import playn.core.json.JsonImpl;
import react.Signal;

public class RoboPlatform extends Platform {

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

    /** Configures the format of the GL framebuffer. The default is RGBA8888, but one can use
      * RGB565 for higher performance at the cost of lower color fidelity. */
    public GLKViewDrawableColorFormat glBufferFormat = GLKViewDrawableColorFormat.RGBA8888;

    /** The desired frames per second for the app. This controls the frequency of {@code update}
      * and {@code paint} calls to your app. Safe values are {@code 30} or {@code 0}. I'm not sure
      * that iOS will honor other arbitrary frame rates. */
    public int targetFPS = 60;

    /** Dictates the name of the temporary file used by {@link RoboStorage}. Configure this if you
      * want to embed multiple games into your application. */
    public String storageFileName = "playn.db";
  }

  /**
   * Creates a RoboVM platform for operation in {@code window}.
   *
   * <p>This basically just sets the root view controller of the supplied window and that view
   * controller manages everything else. If you wish to embed a PlayN game in a larger iOS app, or
   * to customize PlayN more deeply, create a {@link RoboViewController} yourself and include it in
   * your app wherever you like.
   */
  public static RoboPlatform create (UIWindow window, Config config) {
    RoboViewController ctrl = new RoboViewController(window.getBounds(), config);
    window.setRootViewController(ctrl);
    return ctrl.plat;
  }

  /** A signal emitted when the device rotates. */
  public Signal<RoboOrientEvent> orient = Signal.create();

  final int osVersion = getOSVersion();
  final Config config;

  /** Used as a guard flag to avoid duplicated entries caused by the double dispatches of
    * GLKViewControllerDelegate.willPause in one cycle. That could be a bug of RoboVM.
    * TODO: remove this after we figure out a better solution. **/
  private boolean paused = false;
  private final long gameStart = System.nanoTime();
  private final ExecutorService pool = Executors.newFixedThreadPool(3);

  // create log early because other services use it in their ctor
  private final RoboLog log = new RoboLog();
  private final Json json = new JsonImpl();
  private final Exec exec = new Exec.Default(this) {
    @Override public boolean isMainThread () { return NSThread.getCurrentThread().isMainThread(); }
    @Override public void invokeLater (Runnable action) {
      if (paused) NSOperationQueue.getMainQueue().addOperation(action);
      else super.invokeLater(action);
    }
    @Override public boolean isAsyncSupported () { return true; }
    @Override public void invokeAsync (Runnable action) { pool.execute(action); }
  };

  private RoboAudio audio; // lazily initialized
  private final RoboAssets assets;
  private final RoboGraphics graphics;
  private final RoboInput input;
  private final RoboNet net;
  private final RoboStorage storage;

  protected RoboPlatform(Config config, CGRect initBounds) {
    this.config = config;
    assets = new RoboAssets(this);
    graphics = new RoboGraphics(this, config, initBounds);
    input = new RoboInput(this);
    net = new RoboNet(this);
    storage = new RoboStorage(this);
  }

  @Override public Type type() { return Type.IOS; }
  @Override public double time() { return System.currentTimeMillis(); }
  @Override public int tick() { return (int)((System.nanoTime() - gameStart) / 1000000); }

  @Override public void openURL(String url) {
    if (!UIApplication.getSharedApplication().openURL(new NSURL(url))) {
      log().warn("Failed to open URL: " + url);
    }
  }

  @Override public RoboAssets assets() { return assets; }
  @Override public RoboAudio audio() {
    if (audio == null) audio = new RoboAudio(this, config.openALSources);
    return audio;
  }
  @Override public Exec exec() { return exec; }
  @Override public Json json() { return json; }
  @Override public Log log() { return log; }
  @Override public Net net() { return net; }
  @Override public RoboGraphics graphics() { return graphics; }
  @Override public RoboInput input() { return input; }
  @Override public Storage storage() { return storage; }

  // NOTE: all of the below callbacks are called by RoboViewController which handles interfacing
  // with iOS for rotation notifications, game loop callbacks, and app lifecycle events

  void processFrame() { emitFrame(); }

  void willEnterForeground () {
    if (!paused) return;
    paused = false;
    exec.invokeLater(new Runnable() {
      public void run() { dispatchEvent(lifecycle, Lifecycle.RESUME); }
    });
  }

  void didEnterBackground () {
    if (paused) return;
    paused = true;
    // we call this directly rather than via invokeLater() because the PlayN thread is already
    // stopped at this point so a) there's no point in worrying about racing with that thread,
    // and b) onPause would never get called, since the PlayN thread is not processing events
    dispatchEvent(lifecycle, Lifecycle.PAUSE);
  }

  void willTerminate () {
    // stop and release the AL resources (if audio was ever initialized)
    // TODO this does not do anything at the moment.
    // if (audio != null) audio.terminate();

    // let the app know that we're terminating
    dispatchEvent(lifecycle, Lifecycle.EXIT);
  }

  private int getOSVersion () {
    String systemVersion = UIDevice.getCurrentDevice().getSystemVersion();
    int version = Integer.parseInt(systemVersion.split("\\.")[0]);
    return version;
  }
}
