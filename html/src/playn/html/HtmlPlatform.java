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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.Window;

import playn.core.*;
import playn.html.HtmlUrlParameters.Renderer;

public class HtmlPlatform extends Platform {

  /** Configures the PlayN HTML platform. */
  public static class Config {
    /** Whether to use GL or Canvas mode, or to auto-detect. */
    public Mode mode = Renderer.requestedMode();

    /** Whether the canvas that contains the game should be transparent. */
    public boolean transparentCanvas = false;

    /** Whether anti-aliasing should be enabled in the WebGL context. */
    public boolean antiAliasing = true;

    /** The HiDPI scale factor to use. */
    public float scaleFactor = devicePixelRatio();

    /** The number of frame buffer pixels per logical pixel. */
    public float frameBufferPixelRatio = devicePixelRatio();

    /** The id of the {@code <div>} element where the game will be inserted. */
    public String rootId = "playn-root";

    /** If {@code > 0}, the period (in milliseconds) at which to fire frame signals when paused.
      * If {@code 0} (the default) no frame signals will be fired when paused. */
    public int backgroundFrameMillis = 0;

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

  /**
   * Sets the title of the browser's window or tab.
   *
   * @param title the window title
   */
  public void setTitle (String title) {
    Window.setTitle(title);
  }

  /**
   * Sets the {@code cursor} CSS property.
   *
   * @param cursor the {@link Cursor} to use, or null to hide the cursor.
   */
  public void setCursor (Cursor cursor) {
    Element rootElement = graphics.rootElement;
    if (cursor == null) rootElement.getStyle().setProperty("cursor", "none");
    else rootElement.getStyle().setCursor(cursor);
  }

  /**
   * Disable the right-click context menu.
   */
  public void disableRightClickContextMenu () {
    disableRightClickImpl(graphics.rootElement);
  }

  native static float devicePixelRatio () /*-{
    return $wnd.devicePixelRatio || 1;
  }-*/;
  native static float backingStorePixelRatio () /*-{
    return $wnd.webkitBackingStorePixelRatio || 1;
  }-*/;

  /** Contains precomputed information on the user-agent.
    * Useful for dealing with browser and OS behavioral differences. */
  static final AgentInfo agentInfo = computeAgentInfo();

  // installs backwards compat Date.now() if needed and calls it
  private final double start = initNow();

  private int backgroundFrameMillis = 0;

  private final HtmlLog log = GWT.create(HtmlLog.class);
  private final Exec exec = new Exec.Default(this);
  private final HtmlAssets assets;
  private final HtmlAudio audio;
  private final HtmlGraphics graphics;
  private final HtmlInput input;
  private final HtmlJson json = new HtmlJson();
  private final HtmlNet net;
  private final HtmlStorage storage;

  /**
   * Creates an HTML platform instance. Once this is created, a game is free to initialize itself,
   * and it should eventually call {@link #start} when it's ready to run.
   */
  public HtmlPlatform(Config config) {
    GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
      @Override public void onUncaughtException (Throwable e) {
        reportError("Uncaught Exception: ", e);
      }
    });

    log.info("DPR " + devicePixelRatio() + " BSPR " + backingStorePixelRatio());

    // wrap these calls in try-catch, a the UncaughtExceptionHandler installed above won't take
    // effect until we yield to the browser event loop
    try {
      backgroundFrameMillis = config.backgroundFrameMillis;
      graphics = new HtmlGraphics(this, config);
      input = new HtmlInput(this, graphics.rootElement);
      audio = new HtmlAudio(this);
      assets = new HtmlAssets(this);
      net = new HtmlNet();
      storage = new HtmlStorage(this);

    } catch (Throwable e) {
      log.error("init()", e);
      Window.alert("failed to init(): " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /**
   * Starts the frame tick for the platform. A game should call this after it has performed its own
   * initialization.
   */
  public void start () {
    listenForVisibilityChange(this);

    requestAnimationFrame(new Runnable() {
      @Override public void run() {
        requestAnimationFrame(this);
        emitFrame();
      }
    });
  }

  @Override public Type type() { return Type.HTML; }
  @Override public double time() { return now(); }
  @Override public int tick() { return (int)(now() - start); }

  @Override public void openURL(String url) { Window.open(url, "_blank", ""); }

  @Override public HtmlAssets assets() { return assets; }
  @Override public HtmlAudio audio() { return audio; }
  @Override public HtmlGraphics graphics() { return graphics; }
  @Override public Exec exec() { return exec; }
  @Override public Input input() { return input; }
  @Override public Json json() { return json; }
  @Override public Log log() { return log; }
  @Override public Net net() { return net; }
  @Override public Storage storage() { return storage; }

  private native JavaScriptObject getWindow() /*-{
    return $wnd;
  }-*/;

  private void visibilityChanged() {
    boolean isHidden = isHidden();
    dispatchEvent(lifecycle, isHidden ? Lifecycle.PAUSE : Lifecycle.RESUME);

    // if we are configured to update while backgrounded, schedule a background frame
    if (isHidden && backgroundFrameMillis > 0) {
      scheduleBackgroundFrame(backgroundFrameMillis, new Runnable() {
        @Override public void run() {
          // if we're still hidden, emit this background frame and schedule another
          if (isHidden()) {
            scheduleBackgroundFrame(backgroundFrameMillis, this);
            emitFrame();
          }
        }
      });
    }
  }
  private native boolean isHidden() /*-{ return $doc.hidden; }-*/;

  private native void listenForVisibilityChange(HtmlPlatform plat) /*-{
    $doc.addEventListener("visibilitychange", function () {
      plat.@playn.html.HtmlPlatform::visibilityChanged()();
    }, false);
  }-*/;

  private native void requestAnimationFrame(Runnable callback) /*-{
    var fn = function() {
      callback.@java.lang.Runnable::run()();
    };
    if ($wnd.requestAnimationFrame) {
      $wnd.requestAnimationFrame(fn);
    } else if ($wnd.mozRequestAnimationFrame) {
      $wnd.mozRequestAnimationFrame(fn);
    } else if ($wnd.webkitRequestAnimationFrame) {
      $wnd.webkitRequestAnimationFrame(fn);
    } else {
      $wnd.setTimeout(fn, 20); // 20ms => 50fps
    }
  }-*/;

  private native void scheduleBackgroundFrame(int millis, Runnable callback) /*-{
    $wnd.setTimeout(function() {
      callback.@java.lang.Runnable::run()();
    }, millis);
  }-*/;

  private static native AgentInfo computeAgentInfo() /*-{
    var userAgent = navigator.userAgent.toLowerCase();
    return {
      // browser type flags
      isFirefox: userAgent.indexOf("firefox") != -1,
      isChrome: userAgent.indexOf("chrome") != -1,
      isSafari: userAgent.indexOf("safari") != -1,
      isOpera: userAgent.indexOf("opera") != -1,
      isIE: userAgent.indexOf("msie") != -1 || userAgent.indexOf("trident") != -1,
      // OS type flags
      isMacOS: userAgent.indexOf("mac") != -1,
      isLinux: userAgent.indexOf("linux") != -1,
      isWindows: userAgent.indexOf("win") != -1
    };
  }-*/;

  private static native void disableRightClickImpl(JavaScriptObject target) /*-{
    target.oncontextmenu = function() { return false; };
  }-*/;

  private static native double initNow () /*-{
    if (!Date.now) Date.now = function now () { return +(new Date); };
    return Date.now();
  }-*/;

  private static native double now () /*-{ return Date.now(); }-*/;
}
