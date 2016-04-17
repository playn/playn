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
package playn.bugvm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bugvm.apple.coregraphics.CGRect;
import com.bugvm.apple.foundation.NSObject;
import com.bugvm.apple.foundation.NSTimer;
import com.bugvm.apple.foundation.NSURL;
import com.bugvm.apple.glkit.GLKViewDrawableColorFormat;
import com.bugvm.apple.opengles.EAGLContext;
import com.bugvm.apple.uikit.UIApplication;
import com.bugvm.apple.uikit.UIDevice;
import com.bugvm.apple.uikit.UIInterfaceOrientationMask;
import com.bugvm.apple.uikit.UIWindow;
import com.bugvm.objc.block.VoidBlock1;
import com.bugvm.rt.bro.annotation.Callback;

import playn.core.*;
import playn.core.json.JsonImpl;
import react.Signal;

public class BugPlatform extends Platform {

  /** Used to configure the BugVM platform. */
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

    /** Configures the format of the GL framebuffer. The default is RGBA8888, but one can use
      * RGB565 for higher performance at the cost of lower color fidelity. */
    public GLKViewDrawableColorFormat glBufferFormat = GLKViewDrawableColorFormat.RGBA8888;

    /** The desired frames per second for the app. This controls the frequency of {@code update}
      * and {@code paint} calls to your app. Safe values are {@code 30} or {@code 0}. I'm not sure
      * that iOS will honor other arbitrary frame rates. */
    public int targetFPS = 60;

    /** Dictates the name of the temporary file used by {@link BugStorage}. Configure this if you
      * want to embed multiple games into your application. */
    public String storageFileName = "playn.db";
  }

  /**
   * Creates a BugVM platform for operation in {@code window}.
   *
   * <p>This basically just sets the root view controller of the supplied window and that view
   * controller manages everything else. If you wish to embed a PlayN game in a larger iOS app, or
   * to customize PlayN more deeply, create a {@link BugViewController} yourself and include it in
   * your app wherever you like.
   */
  public static BugPlatform create (UIWindow window, Config config) {
    BugViewController ctrl = new BugViewController(window.getBounds(), config);
    window.setRootViewController(ctrl);
    return ctrl.plat;
  }

  /** A signal emitted when the device rotates. */
  public Signal<BugOrientEvent> orient = Signal.create();

  final int osVersion = getOSVersion();
  final Config config;

  /** Used as a guard flag to avoid duplicated entries caused by the double dispatches of
    * GLKViewControllerDelegate.willPause in one cycle. That could be a bug of BugVM.
    * TODO: remove this after we figure out a better solution. **/
  private boolean paused = false;
  private final long gameStart = System.nanoTime();
  private final ExecutorService pool = Executors.newFixedThreadPool(3);

  // create log early because other services use it in their ctor
  private final BugLog log = new BugLog();
  private final Json json = new JsonImpl();
  private final Exec exec = new Exec.Default(this) {
    @Override public boolean isAsyncSupported () { return true; }
    @Override public void invokeAsync (Runnable action) { pool.execute(action); }
  };

  private BugAudio audio; // lazily initialized
  private final BugAssets assets;
  private final BugGraphics graphics;
  private final BugInput input;
  private final BugNet net;
  private final BugStorage storage;

  protected BugPlatform(Config config, CGRect initBounds) {
    this.config = config;
    assets = new BugAssets(this);
    graphics = new BugGraphics(this, config, initBounds);
    input = new BugInput(this);
    net = new BugNet(exec);
    storage = new BugStorage(this);
  }

  @Override public Type type() { return Type.IOS; }
  @Override public double time() { return System.currentTimeMillis(); }
  @Override public int tick() { return (int)((System.nanoTime() - gameStart) / 1000000); }

  @Override public void openURL(String url) {
    if (!UIApplication.getSharedApplication().openURL(new NSURL(url))) {
      log().warn("Failed to open URL: " + url);
    }
  }

  @Override public BugAssets assets() { return assets; }
  @Override public BugAudio audio() {
    if (audio == null) audio = new BugAudio(this, config.openALSources);
    return audio;
  }
  @Override public Exec exec() { return exec; }
  @Override public Json json() { return json; }
  @Override public Log log() { return log; }
  @Override public Net net() { return net; }
  @Override public BugGraphics graphics() { return graphics; }
  @Override public BugInput input() { return input; }
  @Override public Storage storage() { return storage; }

  // NOTE: all of the below callbacks are called by BugViewController which handles interfacing
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
    // shutdown the GL and AL systems after our configured delay
    new NSTimer(config.timeForTermination, new VoidBlock1<NSTimer>() {
      public void invoke (NSTimer timer) {
        // shutdown the GL view completely
        EAGLContext.setCurrentContext(null);
        // stop and release the AL resources (if audio was ever initialized)
        if (audio != null) audio.terminate();
      }
    }, null, false);
    // let the app know that we're terminating
    dispatchEvent(lifecycle, Lifecycle.EXIT);
  }

  private int getOSVersion () {
    String systemVersion = UIDevice.getCurrentDevice().getSystemVersion();
    int version = Integer.parseInt(systemVersion.split("\\.")[0]);
    return version;
  }
}
