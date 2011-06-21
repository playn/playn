/**
 * Copyright 2010 The ForPlay Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.html;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.Window;

import forplay.core.Storage;
import forplay.core.Analytics;
import forplay.core.Audio;
import forplay.core.ForPlay;
import forplay.core.Game;
import forplay.core.Graphics;
import forplay.core.Json;
import forplay.core.Keyboard;
import forplay.core.Log;
import forplay.core.Net;
import forplay.core.Platform;
import forplay.core.Pointer;
import forplay.core.Mouse;
import forplay.core.Touch;
import forplay.core.RegularExpression;
import forplay.html.HtmlUrlParameters.Renderer;

public class HtmlPlatform implements Platform {

  /** Used by {@link #register(Mode)}. */
  public static enum Mode {
    WEBGL {
      public boolean useGL() {
        return true;
      }
    },
    CANVAS {
      public boolean useGL() {
        return false;
      }
    },
    AUTODETECT {
      public boolean useGL() {
        return shouldUseGL();
      }
    };

    public abstract boolean useGL ();
  }

  static final int DEFAULT_WIDTH = 640;
  static final int DEFAULT_HEIGHT = 480;

  private static final int LOG_FREQ = 2500;
  private static final float MAX_DELTA = 100;

  /**
   * Prepares the HTML platform for operation.
   */
  public static HtmlPlatform register() {
    return register(Mode.AUTODETECT);
  }

  /**
   * Prepares the HTML platform for operation.
   *
   * @param mode indicates whether to force the use of WebGL, force the use of Canvas, or to
   * autodetect whether the browser supports WebGL and use it if possible.
   */
  public static HtmlPlatform register(Mode mode) {
    HtmlPlatform platform = new HtmlPlatform(mode);
    ForPlay.setPlatform(platform);
    platform.init();
    return platform;
  }

  static native void addEventListener(JavaScriptObject target, String name, EventHandler handler,
      boolean capture) /*-{
    target.addEventListener(name, function(e) {
    handler.@forplay.html.EventHandler::handleEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);
    }, capture);
  }-*/;

  static void captureEvent(String name, EventHandler handler) {
    captureEvent(null, name, handler);
  }

  static void captureEvent(Element target, String name, EventHandler handler) {
    addEventListener((target == null ? Document.get() : target), name, handler, true);
  }

  private HtmlAssetManager assetManager = new HtmlAssetManager();
  private HtmlAudio audio = new HtmlAudio();
  private HtmlRegularExpression regularExpression = new HtmlRegularExpression();
  private Game game;
  private HtmlGraphics graphics;
  private HtmlJson json = new HtmlJson();
  private HtmlKeyboard keyboard = new HtmlKeyboard();
  private HtmlLog log;
  private HtmlNet net = new HtmlNet();
  private HtmlPointer pointer;
  private HtmlMouse mouse;
  private HtmlTouch touch;
  private HtmlStorage storage = new HtmlStorage();
  private HtmlAnalytics analytics = new HtmlAnalytics();

  private TimerCallback paintCallback;
  private TimerCallback updateCallback;

  // Non-instantiable.
  private HtmlPlatform(Mode mode) {
    // Setup logging first, so it can be used by other subsystems
    log = GWT.create(HtmlLog.class);
    if (!GWT.isProdMode()) {
      log.info("You are running in GWT Development Mode. "
          + "For optimal performance you may want to use an alternative method. "
          + "See http://code.google.com/p/forplay/wiki/GameDebuggingOptions");
    }

    /*
     * Wrap remaining calls in try-catch, since the UncaughtExceptionHandler installed by HtmlLog
     * above won't take effect until we yield to the browser event loop. That means we have to catch
     * our own exceptions here.
     */
    try {
      try {
        graphics = mode.useGL() ? new HtmlGraphicsGL() : new HtmlGraphicsDom();
      } catch (RuntimeException e) {
        // HtmlGraphicsGL ctor throws a runtime exception if the context creation fails.
        log().info("Failed to create GL context. Falling back.");
        graphics = new HtmlGraphicsDom();
      }
      pointer = new HtmlPointer(graphics.getRootElement());
      mouse = new HtmlMouse(graphics.getRootElement());
      touch = new HtmlTouch(graphics.getRootElement());
    } catch (Throwable e) {
      log.error("init()", e);
      Window.alert("failed to init(): " + e.getMessage());
    }
  }

  public void init() {
    analytics.init();
    audio.init();
    keyboard.init();
  }

  @Override
  public HtmlAssetManager assetManager() {
    return assetManager;
  }

  @Override
  public Audio audio() {
    return audio;
  }

  @Override
  public Graphics graphics() {
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
  public float random() {
    return (float) Math.random();
  }

  @Override
  public RegularExpression regularExpression() {
    return regularExpression;
  }

  @Override
  public void run(final Game game) {
    final int updateRate = game.updateRate();

    this.game = game;
    game.init();

    // Game loop.
    paintCallback = new TimerCallback() {
      private float accum = updateRate;
      private double lastTime;

      @Override
      public void fire() {
        requestAnimationFrame(paintCallback);
        double now = time();
        float delta = (float) (now - lastTime);
        if (delta > MAX_DELTA) {
          delta = MAX_DELTA;
        }
        lastTime = now;

        if (updateRate == 0) {
          game.update(delta);
          accum = 0;
        } else {
          accum += delta;
          while (accum > updateRate) {
            game.update(updateRate);
            accum -= updateRate;
          }
        }

        game.paint(accum / updateRate);
        graphics.updateLayers();
      }
    };
    requestAnimationFrame(paintCallback);
  }

  @Override
  public double time() {
    return Duration.currentTimeMillis();
  }

  private native JavaScriptObject getWindow() /*-{
    return $wnd;
  }-*/;

  private native void requestAnimationFrame(TimerCallback callback) /*-{
    var fn = function() {
      callback.@forplay.html.TimerCallback::fire()();
    };
    if ($wnd.requestAnimationFrame) {
      $wnd.requestAnimationFrame(fn);
    } else if ($wnd.mozRequestAnimationFrame) {
      $wnd.mozRequestAnimationFrame(fn);
    } else if ($wnd.webkitRequestAnimationFrame) {
      $wnd.webkitRequestAnimationFrame(fn);
    } else {
      // 20ms => 50fps
      $wnd.setTimeout(fn, 20);
    }
  }-*/;

  private native int setInterval(TimerCallback callback, int ms) /*-{
    return $wnd.setInterval(function() {
      callback.@forplay.html.TimerCallback::fire()();
    }, ms);
  }-*/;

  private native int setTimeout(TimerCallback callback, int ms) /*-{
    return $wnd.setTimeout(function() {
      callback.@forplay.html.TimerCallback::fire()();
    }, ms);
  }-*/;

  /**
   * Gets the URL's parameter of the specified name. Note that if multiple parameters have been
   * specified with the same name, the last one will be returned.
   *
   * @param name the name of the URL's parameter
   * @return the value of the URL's parameter
   */
  public String getUrlParameter(String name) {
    return Window.Location.getParameter(name);
  }

  /**
   * @see forplay.core.Platform#openURL(java.lang.String)
   */
  @Override
  public void openURL(String url) {
    Window.open(url, "_blank", "");
  }

  /**
   * Sets the title of the browser's window or tab.
   *
   * @param title the window title
   */
  public void setTitle(String title) {
    Window.setTitle(title);
  }

  /**
   * Sets the {@code cursor} CSS property.
   * 
   * @param cursor the {@link Cursor} to use, or null to hide the cursor.
   */
  public static void setCursor(Cursor cursor) {
    Element rootElement = ((HtmlGraphics) ForPlay.graphics()).getRootElement();
    if (cursor == null) {
      rootElement.getStyle().setProperty("cursor", "none");
    } else {
      rootElement.getStyle().setCursor(cursor);
    }
  }

  /**
   * Disable the right-click context menu.
   */
  public static void disableRightClickContextMenu() {
    Element rootElement = ((HtmlGraphics) ForPlay.graphics()).getRootElement();
    disableRightClickImpl(rootElement);
  }

  /**
   * Return true if renderer query parameter equals {@link Renderer#GL} or is not set, and the
   * browser supports WebGL
   *
   * @return true if renderer query parameter equals {@link Renderer#GL} or is not set, and the
   *         browser supports WebGL
   */
  private static boolean shouldUseGL() {
    boolean useGlFromFlag = Renderer.shouldUseGL();
    return (useGlFromFlag && hasGLSupport());
  }

  /**
   * Return true if the browser supports WebGL
   *
   * Note: This test can have false positives depending on the graphics hardware.
   *
   * @return true if the browser supports WebGL
   */
  private static native boolean hasGLSupport() /*-{
    return !!$wnd.WebGLRenderingContext &&
      // WebGL is slow on Chrome OSX 10.5
      (!/Chrome/.test(navigator.userAgent) || !/OS X 10_5/.test(navigator.userAgent));
  }-*/;

  private static native void disableRightClickImpl(JavaScriptObject target) /*-{
    target.oncontextmenu = function() {
      return false;
    };
  }-*/;
}
