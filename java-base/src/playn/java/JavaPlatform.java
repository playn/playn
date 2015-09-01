/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import playn.core.*;
import playn.core.json.JsonImpl;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import react.Slot;

/**
 * Implements the base Java platform which is then shared by LWJGL, LWJGL+SWT, and JOGL
 * implementations.
 */
public abstract class JavaPlatform extends Platform {

  /** Defines JavaPlatform configurable parameters. */
  public static class Config {

    /** Dictates the name of the temporary file used by {@link JavaStorage}. Configure this if you
     * want to run multiple sessions without overwriting one another's storage. */
    public String storageFileName = "playn";

    /** The width of the PlayN window, in pixels. */
    public int width = 640;

    /** The height of the PlayN window, in pixels. */
    public int height = 480;

    /** Whether or not to run the game in fullscreen mode. <em>Note:</em> this is not well tested,
     * so you may discover issues. Consider yourself warned. */
    public boolean fullscreen;

    /** If set, emulates Touch and disables Mouse. For testing. */
    public boolean emulateTouch;

    /** If {link #emulateTouch} is set, sets the pivot for a two-finger touch when pressed. */
    public Key pivotKey = Key.F11;

    /** If set, toggles the activation mode when pressed. This is for emulating the active
     * state found in {@code IOSGameView}. */
    public Key activationKey;

    /** If set, converts images into a format for fast GPU uploads when initially loaded versus
     * doing it on demand when displayed. Assuming asynchronous image loads, this keeps that effort
     * off the main thread so it doesn't cause slow frames.
     */
    public boolean convertImagesOnLoad = true;

    /** If supported by the backend and platform, configures the application's name and initial
     * window title. Currently only supported for SWT backend. */
    public String appName = "Game";

    /** Stop processing frames while the app is "inactive", to better emulate iOS. */
    public boolean truePause;
  }

  final Config config;
  private boolean active = true;
  private final long start = System.nanoTime();
  private final ExecutorService pool = Executors.newFixedThreadPool(4);

  private final JavaLog log = new JavaLog();
  private final Exec exec = new Exec.Default(log, frame) {
    @Override public boolean isAsyncSupported () { return true; }
    @Override public void invokeAsync (Runnable action) { pool.execute(action); }
  };
  private final JavaAudio audio = new JavaAudio(exec);
  private final JavaNet net = new JavaNet(exec);
  private final JavaStorage storage;
  private final JsonImpl json = new JsonImpl();
  private final JavaGraphics graphics;
  private final JavaInput input;
  private final JavaAssets assets = new JavaAssets(this);

  public static class Headless extends JavaPlatform {
    public Headless (Config config) { super(config); }
    @Override public void start () {} // noop!
    @Override public void setTitle (String title) {} // noop!
    @Override protected void preInit () {}
    @Override protected JavaGraphics createGraphics () {
      return new JavaGraphics(this,config, null, Scale.ONE) {
        /*ctor*/ { setSize(config.width, config.height, config.fullscreen); }
        @Override public void setSize (int width, int height, boolean fullscreen) {
          updateViewport(Scale.ONE, width, height);
        }
        @Override public IDimension screenSize () {
          return new Dimension(config.width, config.height);
        }
        @Override protected void init () {} // noop!
        @Override protected void upload (BufferedImage img, Texture tex) {} // noop!
      };
    }
    @Override protected JavaInput createInput () { return new JavaInput(this); }
  }

  public JavaPlatform(final Config config) {
    this.config = config;

    // give platform a chance to do things before initializing services
    preInit();

    // create and initialize our various services
    graphics = createGraphics();
    input = createInput();
    storage = new JavaStorage(log, config.storageFileName);

    if (config.activationKey != null) {
      input.keyboardEvents.connect(new Slot<Keyboard.Event>() {
        public void onEmit (Keyboard.Event event) {
          if (event instanceof Keyboard.KeyEvent) {
            Keyboard.KeyEvent kevent = (Keyboard.KeyEvent)event;
            if (kevent.key == config.activationKey && kevent.down) {
              toggleActivation();
            }
          }
        }
      });
    }

    setTitle(config.appName);
    graphics.init();
    input.init();
  }

  /** Sets the title of the window to {@code title}. */
  public abstract void setTitle(String title);

  /**
   * Starts the game loop. This method will not return until the game exits.
   */
  public abstract void start ();

  @Override public double time () { return System.currentTimeMillis(); }
  @Override public Type type () { return Type.JAVA; }
  @Override public int tick () { return (int)((System.nanoTime() - start) / 1000000L); }

  @Override public JavaAssets assets () { return assets; }
  @Override public JavaAudio audio () { return audio; }
  @Override public Exec exec () { return exec; }
  @Override public JavaGraphics graphics () { return graphics; }
  @Override public JavaInput input () { return input; }
  @Override public Json json () { return json; }
  @Override public Log log () { return log; }
  @Override public Net net () { return net; }
  @Override public Storage storage () { return storage; }

  @Override public void openURL(String url) {
    try {
      Desktop.getDesktop().browse(URI.create(url));
    } catch (Exception e) {
      reportError("Failed to open URL [url=" + url + "]", e);
    }
  }

  protected abstract void preInit ();
  protected abstract JavaGraphics createGraphics ();
  protected abstract JavaInput createInput ();

  protected void shutdown () {
    // let the game run any of its exit hooks
    dispatchEvent(lifecycle, Lifecycle.EXIT);

    // shutdown our thread pool
    try {
      pool.shutdown();
      pool.awaitTermination(1, TimeUnit.SECONDS);
    } catch (InterruptedException ie) {
      // nothing to do here except go ahead and exit
    }

    // and finally stick a fork in the JVM
    System.exit(0);
  }

  protected void processFrame () {
    input.update(); // event handling
    emitFrame();
  }

  protected void toggleActivation () {
    active = !active;
  }
}
