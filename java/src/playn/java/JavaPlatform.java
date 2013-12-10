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

import playn.core.AbstractPlatform;
import playn.core.Analytics;
import playn.core.Game;
import playn.core.Json;
import playn.core.Key;
import playn.core.Keyboard;
import playn.core.Mouse;
import playn.core.Net;
import playn.core.PlayN;
import playn.core.RegularExpression;
import playn.core.Storage;
import playn.core.Touch;
import playn.core.TouchImpl;
import playn.core.TouchStub;
import playn.core.json.JsonImpl;

public class JavaPlatform extends AbstractPlatform {

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

    /** If set, emulates Touch and disables Mouse. For testing. */
    public boolean emulateTouch;

    /** If {link #emulateTouch} is set, sets the pivot for a two-finger touch when pressed. */
    public Key multiTouchKey = Key.F11;

    /** If set, converts images into a format for fast GPU uploads when initially loaded versus
     * doing it on demand when displayed. Assuming asynchronous image loads, this keeps that effort
     * off the main thread so it doesn't cause slow frames.
     */
    public boolean convertImagesOnLoad = true;

    /** If supported by the backend and platform, configures the application's name and initial
     * window title. Currently only supported for SWT backend. */
    public String appName = "Game";
  }

  /**
   * Registers the Java platform with a default configuration.
   */
  public static JavaPlatform register() {
    return register(new Config());
  }

  /**
   * Registers the Java platform with the specified configuration.
   */
  public static JavaPlatform register(Config config) {
    // guard against multiple-registration (only in headless mode because this can happen when
    // running tests in Maven; in non-headless mode, we want to fail rather than silently ignore
    // erroneous repeated registration)
    if (config.headless && testInstance != null) {
      return testInstance;
    }
    JavaPlatform instance = new JavaPlatform(config);
    if (config.headless) {
      testInstance = instance;
    }
    PlayN.setPlatform(instance);
    return instance;
  }

  private static JavaPlatform testInstance;

  private static float getDefaultScaleFactor() {
    String sfprop = System.getProperty("playn.scaleFactor", "1");
    try {
      return Float.parseFloat(sfprop);
    } catch (Exception e) {
      System.err.println("Invalid scaleFactor supplied '" + sfprop + "': " + e);
      return 1;
    }
  }

  public final boolean convertImagesOnLoad;

  private final Config config;
  private final JavaAnalytics analytics = new JavaAnalytics();
  private final JavaAudio audio = new JavaAudio(this);
  private final JavaNet net = new JavaNet(this);
  private final JavaRegularExpression regex = new JavaRegularExpression();
  private final JavaStorage storage;
  private final JsonImpl json = new JsonImpl();
  private final JavaKeyboard keyboard;
  private final JavaPointer pointer = new JavaPointer();
  private final TouchImpl touch;
  private final JavaGraphics graphics;
  private final JavaMouse mouse;
  private final JavaAssets assets = new JavaAssets(this);

  private final ExecutorService _exec = Executors.newFixedThreadPool(4);
  private final long start = System.nanoTime();

  public JavaPlatform(Config config) {
    super(new JavaLog());
    this.config = config;
    unpackNatives();
    graphics = createGraphics(config);
    keyboard = createKeyboard();
    storage = new JavaStorage(this, config);
    touch = createTouch(config);
    if (touch instanceof JavaEmulatedTouch) {
      mouse = ((JavaEmulatedTouch)touch).createMouse(this);
    } else {
      mouse = createMouse();
    }

    if (!config.headless) {
      setTitle(config.appName);
    }
    convertImagesOnLoad = config.convertImagesOnLoad;
  }

  /**
   * Sets the title of the window.
   *
   * @param title the window title
   */
  public void setTitle(String title) {
    Display.setTitle(title);
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
  public Keyboard keyboard() {
    return keyboard;
  }

  @Override
  public Net net() {
    return net;
  }

  @Override
  public JavaPointer pointer() {
    return pointer;
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
  public Analytics analytics() {
    return analytics;
  }

  @Override
  public JavaAssets assets() {
    return assets;
  }

  @Override
  public RegularExpression regularExpression() {
    return regex;
  }

  @Override
  public float random() {
    return (float) Math.random();
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
      log.warn("Failed to open URL [url=" + url + ", error=" + e + "]");
    }
  }

  @Override
  public void setPropagateEvents(boolean propagate) {
    mouse.setPropagateEvents(propagate);
    touch.setPropagateEvents(propagate);
    pointer.setPropagateEvents(propagate);
  }

  @Override
  public void run(final Game game) {
    if (!config.headless) {
      try {
        Display.create();
      } catch (LWJGLException e) {
        throw new RuntimeException(e);
      }
    }
    init(game);

    boolean wasActive = Display.isActive();
    while (!Display.isCloseRequested()) {
      // Notify the app if lose or regain focus (treat said as pause/resume).
      boolean newActive = Display.isActive();
      if (wasActive != newActive) {
        if (wasActive)
          onPause();
        else
          onResume();
        wasActive = newActive;
      }
      processFrame(game);
      Display.update();
      // Sleep until it's time for the next frame.
      Display.sync(60);
    }

    shutdown();
  }

  protected JavaGraphics createGraphics(Config config) {
    return new JavaGraphics(this, config);
  }
  protected TouchImpl createTouch(Config config) {
    if (config.emulateTouch) {
      return new JavaEmulatedTouch(config.multiTouchKey);
    } else {
      return new TouchStub();
    }
  }
  protected JavaMouse createMouse() {
    return new JavaLWJGLMouse(this);
  }
  protected JavaKeyboard createKeyboard() {
    return new JavaLWJGLKeyboard();
  }

  protected void init(Game game) {
    graphics.init();
    mouse.init();
    keyboard.init(touch);
    game.init();
  }

  protected void shutdown() {
    // let the game run any of its exit hooks
    onExit();

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

  protected void processFrame(Game game) {
    // Event handling.
    mouse.update();
    keyboard.update();
    pointer.update();

    // Execute any pending runnables.
    runQueue.execute();

    // Run the game loop, render the scene graph, and update the display.
    game.tick(tick());
    graphics.paint();
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
