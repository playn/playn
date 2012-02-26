/**
 * Copyright 2010 The PlayN Authors
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
package playn.flash;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.JavaScriptObject;

import playn.core.Touch;

import flash.events.Event;

import playn.core.Storage;

import flash.display.Sprite;

import flash.events.EventType;

import playn.core.Analytics;
import playn.core.Audio;
import playn.core.PlayN;
import playn.core.Game;
import playn.core.Graphics;
import playn.core.Json;
import playn.core.Keyboard;
import playn.core.Log;
import playn.core.Net;
import playn.core.Platform;
import playn.core.Pointer;
import playn.core.Mouse;
import playn.core.RegularExpression;
import playn.core.json.JsonImpl;
import playn.html.HtmlRegularExpression;

public class FlashPlatform implements Platform {

  static final int DEFAULT_WIDTH = 640;
  static final int DEFAULT_HEIGHT = 480;

  private static final int LOG_FREQ = 2500;
  private static final float MAX_DELTA = 100;
  private static FlashPlatform platform;

  static {
    platform = new FlashPlatform();
  }

  public static FlashPlatform register() {
    PlayN.setPlatform(platform);
    platform.init();
    return platform;
  }

  static native void addEventListener(JavaScriptObject target, String name, EventHandler<?> handler, boolean capture) /*-{
    target.addEventListener(name, function(e) {
      handler.@playn.flash.EventHandler::handleEvent(Lflash/events/Event;)(e);
    }, capture);
  }-*/;


  private FlashAssets assets = new FlashAssets();
  private FlashAudio audio;
  private HtmlRegularExpression regularExpression;
  private Game game;
  private FlashGraphics graphics;
  private Json json;
  private FlashKeyboard keyboard;
  private FlashLog log;
  private FlashNet net;
  private FlashPointer pointer;
  private FlashMouse mouse;

  private TimerCallback paintCallback;
  private TimerCallback updateCallback;
  private Storage storage;
  private Analytics analytics;

  // Non-instantiable.
  public FlashPlatform() {
  }

  public void init() {
    log = new FlashLog();
    regularExpression = new HtmlRegularExpression();
    net = new FlashNet();
    audio = new FlashAudio();
    keyboard = new FlashKeyboard();
    pointer = new FlashPointer();
    mouse = new FlashMouse();
    json = new JsonImpl();
    graphics = new FlashGraphics();
    storage = new FlashStorage();
    analytics = new FlashAnalytics();
  }

  @Override
  public FlashAssets assets() {
    return assets;
  }

  /** @deprecated Use {@link #assets}. */
  @Deprecated
  public FlashAssets assetManager() {
    return assets;
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
    // TODO(pdr): need to implement this.
    throw new UnsupportedOperationException("Touch is not yet supported on the Flash platform");
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
  public void openURL(String url) {
      //TODO: implement
  }

  private static int FPS_COUNTER_MAX = 300;
  @Override
  public void run(final Game game) {


    final int updateRate = game.updateRate();

    this.game = game;

    // Game loop.
    paintCallback = new TimerCallback() {
      private float accum = updateRate;
      private double lastTime;
      int frameCounter = 0;
      private double frameCounterStart = 0;

      @Override
      public void fire() {
        double now = time();
        if (frameCounter == 0) {
          frameCounterStart = now;
        }

        float delta = (float)(now - lastTime);
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
        frameCounter++;
        if (frameCounter == FPS_COUNTER_MAX) {
          double frameRate = frameCounter /
            ((time() - frameCounterStart) / 1000.0);
          PlayN.log().info("FPS: " + frameRate);
          frameCounter = 0;
        }
      }
    };
    game.init();
    requestAnimationFrame(paintCallback);
  }

  @Override
  public double time() {
    return Duration.currentTimeMillis();
  }

  @Override
  public Type type() {
    return Type.FLASH;
  }

    private void requestAnimationFrame(final TimerCallback callback) {
    //  http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/flash/display/DisplayObject.html#event:enterFrame
    FlashPlatform.captureEvent(Sprite.ENTERFRAME, new EventHandler<Event>() {
      @Override
      public void handleEvent(Event evt) {
        evt.preventDefault();
        callback.fire();
      }
    });

    }

  private native int setInterval(TimerCallback callback, int ms) /*-{
    return $wnd.setInterval(function() { callback.@playn.flash.TimerCallback::fire()(); }, ms);
  }-*/;

  private native int setTimeout(TimerCallback callback, int ms) /*-{
    return $wnd.setTimeout(function() { callback.@playn.flash.TimerCallback::fire()(); }, ms);
  }-*/;

  public static native void captureEvent(EventType eventType, EventHandler<?> eventHandler) /*-{
    $root.stage.addEventListener(eventType, function(arg) {
      eventHandler.@playn.flash.EventHandler::handleEvent(Lflash/events/Event;)(arg);
    });
  }-*/;

  @Override
  public Storage storage() {
    return storage;
  }

  @Override
  public Analytics analytics() {
    return analytics;
  }
}
