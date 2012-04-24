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

import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import playn.core.Analytics;
import playn.core.Audio;
import playn.core.Game;
import playn.core.Json;
import playn.core.Keyboard;
import playn.core.Log;
import playn.core.Mouse;
import playn.core.Net;
import playn.core.Platform;
import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.RegularExpression;
import playn.core.Storage;
import playn.core.Touch;
import playn.core.json.JsonImpl;

public class JavaPlatform implements Platform {
  // Maximum delta time to consider between update() calls (in milliseconds). If the delta between
  // two update()s is greater than MAX_DELTA, we clamp to MAX_DELTA.
  private static final float MAX_DELTA = 100;

  // Minimum time between any two paint() calls (in milliseconds). We will paint every
  // FRAME_TIME ms, which is equivalent to (1000 * 1 / FRAME_TIME) frames per second.
  // TODO(pdr): this is set ridiculously low because we're using Java's software renderer which
  // causes the paint loop to be quite slow. Setting this to 10 prevents hitching that occurs when
  // we try to squeeze a paint() near max bound of FRAME_TIME.
  private static final float FRAME_TIME = 10;

  private static JavaPlatform instance;

  public static JavaPlatform register() {
    String sfprop = System.getProperty("playn.scaleFactor");
    if (sfprop != null) {
      try {
        return register(Float.parseFloat(sfprop));
      } catch (Exception e) {
        System.err.println("Invalid scaleFactor supplied '" + sfprop + "': " + e);
        // fall through and register with scale factor 1
      }
    }
    return register(1);
  }

  public static JavaPlatform register(float scaleFactor) {
    // Guard against multiple-registration. This can happen when running tests in maven.
    if (instance != null) {
      return instance;
    }

    instance = new JavaPlatform(scaleFactor);
    PlayN.setPlatform(instance);
    instance.init();
    return instance;
  }

  private JavaAnalytics analytics = new JavaAnalytics();
  private JavaAudio audio = new JavaAudio();
  private JavaLog log = new JavaLog();
  private JavaNet net = new JavaNet();
  private JavaRegularExpression regex = new JavaRegularExpression();
  private JavaStorage storage = new JavaStorage();
  private JsonImpl json = new JsonImpl();
  private JavaKeyboard keyboard = new JavaKeyboard();
  private JavaPointer pointer = new JavaPointer();
  private JavaGraphics graphics;
  private JavaMouse mouse;
  private JavaAssets assets;

  private int updateRate = 0;
  private float accum = updateRate;
  private double lastUpdateTime;
  private double lastPaintTime;

  public JavaPlatform(float scaleFactor) {
    graphics = new JavaGraphics(scaleFactor);
    mouse = new JavaMouse(graphics);
    assets = new JavaAssets(graphics, audio);
  }

  protected void init() {
    storage.init();
  }

  @Override
  public Audio audio() {
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
  public Log log() {
    return log;
  }

  @Override
  public Net net() {
    return net;
  }

  @Override
  public Pointer pointer() {
    return pointer;
  }

  @Override
  public Mouse mouse() {
    return mouse;
  }

  @Override
  public Touch touch() {
    // TODO(pdr): need to implement this.
    throw new UnsupportedOperationException("Touch is not yet supported on the Java platform");
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
  public float random() {
    return (float) Math.random();
  }

  @Override
  public void run(final Game game) {
    this.updateRate = game.updateRate();

    try {
      // initialize LWJGL (and show the display) now that the game has been initialized
      graphics.init();
      // now that the display is initialized we can init our mouse and keyboard
      mouse.init();
      keyboard.init();
    } catch (LWJGLException e) {
      throw new RuntimeException("Unrecoverable initialization error", e);
    }

    game.init();

    while (!Display.isCloseRequested()) {
      // Event handling.
      mouse.update();
      keyboard.update();
      pointer.update();
      net.update();

      // Game loop.
      double now = time();
      float updateDelta = (float) (now - lastUpdateTime);
      if (updateDelta > 1) {
        updateDelta = updateDelta > MAX_DELTA ? MAX_DELTA : updateDelta;
        lastUpdateTime = now;

        if (updateRate == 0) {
          game.update(updateDelta);
          accum = 0;
        } else {
          accum += updateDelta;
          while (accum > updateRate) {
            game.update(updateRate);
            accum -= updateRate;
          }
        }
      }

      float paintDelta = (float) (now - lastPaintTime);
      if (paintDelta > FRAME_TIME) {
        if (updateRate == 0) {
          game.paint(0);
        } else {
          game.paint(accum / updateRate);
        }

        graphics.paintLayers();

        lastPaintTime = now;
      }

      Display.sync(60);
      Display.update();
    }

    System.exit(0);
  }

  @Override
  public double time() {
    return System.currentTimeMillis();
  }

  @Override
  public Type type() {
    return Type.JAVA;
  }

  @Override
  public RegularExpression regularExpression() {
    return regex;
  }

  @Override
  public void openURL(String url) {
    System.out.println("Opening url: " + url);
    String browser = "chrome ";
    if (System.getProperty("os.name", "-").contains("indows"))
      browser = "rundll32 url.dll,FileProtocolHandler ";
    try {
      Runtime.getRuntime().exec(browser + url);
    } catch (IOException e) {
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
}
