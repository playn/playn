/**
 * Copyright 2010 The PlayN Authors
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
package playn.html;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.Window;

import playn.core.AbstractPlatform;
import playn.core.Storage;
import playn.core.Audio;
import playn.core.PlayN;
import playn.core.Game;
import playn.core.Json;
import playn.core.Keyboard;
import playn.core.Net;
import playn.core.Pointer;
import playn.core.Mouse;
import playn.core.Touch;
import playn.html.HtmlUrlParameters.Renderer;

public class HtmlPlatform extends AbstractPlatform {

  /** Configures the PlayN HTML platform. */
  public static class Config {
    /** Whether to use GL or Canvas mode, or to auto-detect. */
    public Mode mode = Renderer.requestedMode();

    /** Whether the canvas that contains the game should be transparent. */
    public boolean transparentCanvas = false;

    /** Whether anti-aliasing should be enabled in the WebGL context. */
    public boolean antiAliasing = true;

    /** The HiDPI scale factor to use. */
    public float scaleFactor = 1;

    /** The id of the {@code <div>} element where the game will be inserted. */
    public String rootId = "playn-root";

    // Scale up the canvas on fullscreen. Highly experimental.
    public boolean experimentalFullscreen = false;
  }

  /** Used for {@link Config#mode}. */
  public static enum Mode {
    WEBGL, CANVAS, AUTODETECT;
  }

  /** Returned by {@link #agentInfo}. */
  public static class AgentInfo extends JavaScriptObject {
    public final native boolean isFirefox() /*-{ return this.isFirefox; }-*/;
    public final native boolean isChrome() /*-{ return this.isChrome; }-*/;
    public final native boolean isSafari() /*-{ return this.isSafari; }-*/;
    public final native boolean isOpera() /*-{ return this.isOpera; }-*/;
    public final native boolean isIE() /*-{ return this.isIE; }-*/;
    public final native boolean isMacOS() /*-{ return this.isMacOS; }-*/;
    public final native boolean isLinux() /*-{ return this.isLinux; }-*/;
    public final native boolean isWindows() /*-{ return this.isWindows; }-*/;
    protected AgentInfo() {}
  }

  /** Indicates whether this browser supports JavaScript typed arrays. */
  static final boolean hasTypedArraySupport = hasTypedArraySupport();

  /**
   * Prepares the HTML platform for operation.
   */
  public static HtmlPlatform register() {
    return register(new Config());
  }

  /**
   * Prepares the HTML platform for operation.
   *
   * @param config platform-specific settings.
   */
  public static HtmlPlatform register(Config config) {
    HtmlPlatform platform = new HtmlPlatform(config);
    PlayN.setPlatform(platform);
    platform.init();
    return platform;
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
    Element rootElement = ((HtmlGraphics) PlayN.graphics()).rootElement();
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
    Element rootElement = ((HtmlGraphics) PlayN.graphics()).rootElement();
    disableRightClickImpl(rootElement);
  }

  static native void addEventListener(JavaScriptObject target, String name, EventHandler handler,
      boolean capture) /*-{
    target.addEventListener(name, function(e) {
    handler.@playn.html.EventHandler::handleEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);
    }, capture);
  }-*/;

  static void captureEvent(String name, EventHandler handler) {
    captureEvent(null, name, handler);
  }

  static void captureEvent(Element target, String name, EventHandler handler) {
    addEventListener((target == null ? Document.get() : target), name, handler, true);
  }

  /** Contains precomputed information on the user-agent. Useful for dealing with browser and OS
   * behavioral differences. */
  static AgentInfo agentInfo() {
    return agentInfo;
  }

  // Provide a static logging instance so that it can be used by other subsystems during
  // initialization, before we have called PlayN.setPlatform
  static final HtmlLog log = GWT.create(HtmlLog.class);

  private final HtmlAssets assets = new HtmlAssets(this);
  private final HtmlAudio audio = new HtmlAudio();
  private final HtmlGraphics graphics;
  private final HtmlJson json = new HtmlJson();
  private final HtmlKeyboard keyboard = new HtmlKeyboard();
  private final HtmlNet net = new HtmlNet(this);
  private final HtmlPointer pointer;
  private final HtmlMouse mouse;
  private final HtmlTouch touch;
  private final HtmlStorage storage = new HtmlStorage(this);

  // installs backwards compat Date.now() if needed and calls it
  private final double start = initNow();

  private TimerCallback paintCallback;

  private static AgentInfo agentInfo = computeAgentInfo();

  protected HtmlPlatform(Config config) {
    super(log);
    if (!GWT.isProdMode()) {
      log.info("You are running in GWT Development Mode. "
          + "For optimal performance you may want to use an alternative method. "
          + "See http://code.google.com/p/playn/wiki/GameDebuggingOptions");
    }

    /*
     * Wrap remaining calls in try-catch, since the UncaughtExceptionHandler installed by HtmlLog
     * above won't take effect until we yield to the browser event loop. That means we have to catch
     * our own exceptions here.
     */
    try {
      graphics = createGraphics(config);
      pointer = new HtmlPointer(this, graphics.rootElement());
      mouse = new HtmlMouse(this, graphics.rootElement());
      touch = new HtmlTouch(this, graphics.rootElement());

    } catch (Throwable e) {
      log.error("init()", e);
      Window.alert("failed to init(): " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public void init() {
    audio.init();
    keyboard.init();
  }

  @Override
  public HtmlAssets assets() {
    return assets;
  }

  @Override
  public Audio audio() {
    return audio;
  }

  @Override
  public HtmlGraphics graphics() {
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
  public float random() {
    return (float) Math.random();
  }

  @Override
  public void run(final Game game) {
    game.init();
    // Game loop.
    paintCallback = new TimerCallback() {
      @Override
      public void fire() {
        requestAnimationFrame(paintCallback);
        runQueue.execute(); // process pending actions
        game.tick(tick());  // update the game
        graphics.paint();   // draw the scene graph
      }
    };
    requestAnimationFrame(paintCallback);
  }

  @Override
  public double time() {
    return now();
  }

  @Override
  public int tick() {
    return (int)(now() - start);
  }

  /**
   * @see playn.core.Platform#openURL(java.lang.String)
   */
  @Override
  public void openURL(String url) {
    Window.open(url, "_blank", "");
  }

  @Override
  public void setPropagateEvents(boolean propagate) {
    mouse.setPropagateEvents(propagate);
    touch.setPropagateEvents(propagate);
    pointer.setPropagateEvents(propagate);
  }

  @Override
  public Type type() {
    return Type.HTML;
  }

  private HtmlGraphics createGraphics(Config config) {
    try {
      switch (config.mode) {
      case CANVAS:
        return new HtmlGraphicsCanvas(config);
      case WEBGL:
        return new HtmlGraphicsGL(this, config);
      default:
      case AUTODETECT:
        return hasGLSupport() ? new HtmlGraphicsGL(this, config) : new HtmlGraphicsCanvas(config);
      }

    // HtmlGraphicsGL ctor throws a runtime exception if the context creation fails.
    } catch (RuntimeException e) {
      log().info("Failed to create GL context (" + e.getMessage() + "). Falling back.");
    } catch (Throwable t) {
      log().info("GL context creation failed with an unknown error." + t);
    }

    return new HtmlGraphicsCanvas(config);
  }

  private native JavaScriptObject getWindow() /*-{
    return $wnd;
  }-*/;

  private native void requestAnimationFrame(TimerCallback callback) /*-{
    var fn = function() {
      callback.@playn.html.TimerCallback::fire()();
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

  private static native AgentInfo computeAgentInfo() /*-{
    var userAgent = navigator.userAgent.toLowerCase();
    return {
      // browser type flags
      isFirefox: userAgent.indexOf("firefox") != -1,
      isChrome: userAgent.indexOf("chrome") != -1,
      isSafari: userAgent.indexOf("safari") != -1,
      isOpera: userAgent.indexOf("opera") != -1,
      isIE: userAgent.indexOf("msie") != -1,
      // OS type flags
      isMacOS: userAgent.indexOf("mac") != -1,
      isLinux: userAgent.indexOf("linux") != -1,
      isWindows: userAgent.indexOf("win") != -1
    };
  }-*/;

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

  private static native boolean hasTypedArraySupport() /*-{
    return typeof(Float32Array) != 'undefined';
  }-*/;

  private static native void disableRightClickImpl(JavaScriptObject target) /*-{
    target.oncontextmenu = function() {
      return false;
    };
  }-*/;

  private static native double initNow () /*-{
    if (!Date.now) {
      Date.now = function now() {
        return +(new Date);
      };
    }
    return Date.now();
  }-*/;

  private static native double now () /*-{
    return Date.now();
  }-*/;
}
