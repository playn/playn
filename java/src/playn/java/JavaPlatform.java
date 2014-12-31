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
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import playn.core.*;
import playn.core.json.JsonImpl;
import react.Slot;

/**
 * Implements the PlayN platform for Java, based on LWJGL. Due to the way LWJGL works, a game must
 * call {@link #init}, then perform any of its own initialization that requires access to GL
 * resources, and then call {@link #start} to start the game loop. The {@link #start} call does not
 * return until the game exits.
 */
public class JavaPlatform extends Platform {

  /** Defines JavaPlatform configurable parameters. */
  public static class Config {
    /** The graphics scale factor. Allows simulating HiDPI mode during testing. */
    public float scaleFactor = getDefaultScaleFactor(); // default scale factor is 1

    /** Configures platform in headless mode; useful for unit testing. */
    public boolean headless = false;

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

  private static float getDefaultScaleFactor() {
    String sfprop = System.getProperty("playn.scaleFactor", "1");
    try {
      return Float.parseFloat(sfprop);
    } catch (Exception e) {
      System.err.println("Invalid scaleFactor supplied '" + sfprop + "': " + e);
      return 1;
    }
  }

  final Config config;

  private final JavaLog log = new JavaLog();
  private final JavaAudio audio = new JavaAudio(this);
  private final JavaNet net = new JavaNet(this);
  private final JavaStorage storage;
  private final JsonImpl json = new JsonImpl();
  private final JavaKeyboard keyboard;
  private final Touch touch;
  private final JavaGraphics graphics;
  private final JavaMouse mouse;
  private final JavaAssets assets = new JavaAssets(this);
  private boolean active = true;

  private final ExecutorService _exec = Executors.newFixedThreadPool(4);
  private final long start = System.nanoTime();

  public JavaPlatform(final Config config) {
    this.config = config;
    if (!config.headless) {
      unpackNatives();
    }
    graphics = createGraphics();
    keyboard = createKeyboard();
    mouse = createMouse();
    touch = createTouch(keyboard, mouse);
    storage = new JavaStorage(this);

    if (config.activationKey != null) {
      keyboard.events.connect(new Slot<Keyboard.Event>() {
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
    if (!config.headless) {
      setTitle(config.appName);
    }
  }

  /**
   * Sets the title of the window.
   *
   * @param title the window title
   */
  public void setTitle(String title) {
    Display.setTitle(title);
  }

  /**
   * Initializes the LWJGL subsystems. This must be called before calling {@link #start}.
   */
  public void init () {
    // set our starting display mode before we create our display
    graphics.preInit();

    if (!config.headless) {
      try {
        Display.create();
      } catch (LWJGLException e) {
        throw new RuntimeException(e);
      }
    }

    keyboard.init();
    mouse.init();
  }

  @Override
  public void invokeAsync(Runnable action) {
    _exec.execute(action);
  }

  @Override
  public Type type() {
    return Type.JAVA;
  }

  @Override
  public JavaAudio audio() {
    return audio;
  }

  @Override
  public JavaGraphics graphics() {
    return graphics;
  }

  @Override
  public Json json() {
    return json;
  }

  @Override
  public Log log() {
    return log;
  }

  @Override
  public Keyboard keyboard() {
    return keyboard;
  }

  @Override
  public Net net() {
    return net;
  }

  @Override
  public Mouse mouse() {
    return mouse;
  }

  @Override
  public Touch touch() {
    return touch;
  }

  @Override
  public Storage storage() {
    return storage;
  }

  @Override
  public JavaAssets assets() {
    return assets;
  }

  @Override
  public double time() {
    return System.currentTimeMillis();
  }

  @Override
  public int tick() {
    return (int)((System.nanoTime() - start) / 1000000L);
  }

  @Override
  public void openURL(String url) {
    try {
      Desktop.getDesktop().browse(URI.create(url));
    } catch (Exception e) {
      reportError("Failed to open URL [url=" + url + "]", e);
    }
  }

  @Override
  public void start() {
    boolean wasActive = Display.isActive();
    while (!Display.isCloseRequested()) {
      // notify the app if lose or regain focus (treat said as pause/resume)
      boolean newActive = Display.isActive();
      if (wasActive != newActive) {
        lifecycle.emit(wasActive ? Lifecycle.PAUSE : Lifecycle.RESUME);
        wasActive = newActive;
      }
      // process frame, if we don't need to provide true pausing
      if (newActive || !config.truePause) processFrame();
      Display.update();
      // sleep until it's time for the next frame
      Display.sync(60);
    }

    shutdown();
  }

  protected JavaGraphics createGraphics() {
    return new JavaGraphics(this);
  }
  protected JavaTouch createTouch(Keyboard keyboard, Mouse mouse) {
    return new JavaTouch(this, keyboard, mouse);
  }
  protected JavaMouse createMouse() {
    return new JavaLWJGLMouse(this);
  }
  protected JavaKeyboard createKeyboard() {
    return new JavaLWJGLKeyboard(this);
  }

  protected void shutdown() {
    // let the game run any of its exit hooks
    lifecycle.emit(Lifecycle.EXIT);

    // shutdown our thread pool
    try {
      _exec.shutdown();
      _exec.awaitTermination(1, TimeUnit.SECONDS);
    } catch (InterruptedException ie) {
      // nothing to do here except go ahead and exit
    }

    // and finally stick a fork in the JVM
    System.exit(0);
  }

  protected void processFrame() {
    // event handling
    mouse.update();
    keyboard.update();
    // emit a frame signal
    frame.emit(this);
  }

  protected void toggleActivation () {
    active = !active;
  }

  protected void unpackNatives() {
    // avoid native library unpacking if we're running in Java Web Start
    if (isInJavaWebStart())
      return;

    SharedLibraryExtractor extractor = new SharedLibraryExtractor();
    File nativesDir = null;
    try {
      nativesDir = extractor.extractLibrary("lwjgl", null).getParentFile();
    } catch (Throwable ex) {
      throw new RuntimeException("Unable to extract LWJGL native libraries.", ex);
    }
    System.setProperty("org.lwjgl.librarypath", nativesDir.getAbsolutePath());
  }

  protected boolean isInJavaWebStart() {
    try {
      Method method = Class.forName("javax.jnlp.ServiceManager").
        getDeclaredMethod("lookup", new Class<?>[] { String.class });
      method.invoke(null, "javax.jnlp.PersistenceService");
      return true;
    } catch (Throwable ignored) {
      return false;
    }
  }
}
