/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.flash;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.JavaScriptObject;

import forplay.core.Touch;

import flash.events.Event;

import forplay.core.Storage;

import flash.display.Sprite;

import flash.events.EventType;

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
import forplay.core.RegularExpression;
import forplay.html.HtmlRegularExpression;

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
    ForPlay.setPlatform(platform);
    platform.init();
    return platform;
  }

  static native void addEventListener(JavaScriptObject target, String name, EventHandler<?> handler, boolean capture) /*-{
    target.addEventListener(name, function(e) {
      handler.@forplay.flash.EventHandler::handleEvent(Lflash/events/Event;)(e);
    }, capture);
  }-*/;


  private FlashAssetManager assetManager = new FlashAssetManager();
  private FlashAudio audio;
  private HtmlRegularExpression regularExpression;
  private Game game;
  private FlashGraphics graphics;
  private FlashJson json;
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
    json = new FlashJson();
    graphics = new FlashGraphics();
    storage = new FlashStorage();
    analytics = new FlashAnalytics();
  }

  @Override
  public FlashAssetManager assetManager() {
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
//          log().info("Update took " + (time() - now));
          accum = 0;
        } else {
          accum += delta;
          while (accum > updateRate) {
            game.update(updateRate);
//            log().info("Update took " + (time() - now));
            accum -= updateRate;
          }
        }

//        double now2 = time();
        game.paint(accum / updateRate);
//        log().info("Paint took " + (time() - now2));

        graphics.updateLayers();
        frameCounter++;
        if (frameCounter == FPS_COUNTER_MAX) {
          double frameRate = frameCounter / 
            ((time() - frameCounterStart) / 1000.0);
          ForPlay.log().info("FPS: " + frameRate);
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

    private void requestAnimationFrame(final TimerCallback callback) {
    //  http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/flash/display/DisplayObject.html#event:enterFrame
    FlashPlatform.captureEvent(Sprite.ENTERFRAME, new EventHandler<Event>() {
      public void handleEvent(Event evt) {
        evt.preventDefault();  
        callback.fire();
      }
    });

    }

  private native int setInterval(TimerCallback callback, int ms) /*-{
    return $wnd.setInterval(function() { callback.@forplay.flash.TimerCallback::fire()(); }, ms);
  }-*/;

  private native int setTimeout(TimerCallback callback, int ms) /*-{
    return $wnd.setTimeout(function() { callback.@forplay.flash.TimerCallback::fire()(); }, ms);
  }-*/;

  public static native void captureEvent(EventType eventType, EventHandler<?> eventHandler) /*-{
    $root.addEventListener(eventType, function(arg) {
      eventHandler.@forplay.flash.EventHandler::handleEvent(Lflash/events/Event;)(arg);
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
