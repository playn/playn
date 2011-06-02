/**
 * Copyright 2010 The ForPlay Authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.android;

import forplay.core.Touch;

import android.graphics.Canvas;
import forplay.core.Analytics;
import forplay.core.AssetManager;
import forplay.core.Audio;
import forplay.core.ForPlay;
import forplay.core.Game;
import forplay.core.Graphics;
import forplay.core.Json;
import forplay.core.Keyboard;
import forplay.core.Log;
import forplay.core.Mouse;
import forplay.core.Net;
import forplay.core.Platform;
import forplay.core.Pointer;
import forplay.core.Touch;
import forplay.core.RegularExpression;
import forplay.core.Storage;
import forplay.java.JavaJson;

public class AndroidPlatform implements Platform {

  static AndroidPlatform instance;

  public static void register(GameActivity activity) {
    ForPlay.setPlatform(instance = new AndroidPlatform(activity));
  }

  private final GameActivity activity;
  private Game game;
  private AndroidAudio audio;
  private AndroidGraphics graphics;
  private JavaJson json;
  private AndroidKeyboard keyboard;
  private AndroidLog log;
  private AndroidNet net;
  private AndroidPointer pointer;
  private Canvas currentCanvas;
  private AndroidAssetManager assetManager;
  private AndroidAnalytics analytics;

  private AndroidPlatform(GameActivity activity) {
    this.activity = activity;
  }

  @Override
  public AssetManager assetManager() {
    return assetManager;
  }

  @Override
  public Analytics analytics() {
    return analytics;
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
  public void openURL(String url) {
    // TODO(jgw): wtf is this doing here?
  }

  @Override
  public Mouse mouse() {
    return null;
  }

  @Override
  public Touch touch() {
    // TODO(pdr): need to implement this.
    return null;
  }

  @Override
  public Pointer pointer() {
    return pointer;
  }

  @Override
  public float random() {
    return (float) Math.random();
  }

  @Override
  public RegularExpression regularExpression() {
    return new AndroidRegularExpression();
  }

  @Override
  public void run(Game game) {
    audio = new AndroidAudio();
    graphics = new AndroidGraphics(activity);
    json = new JavaJson();
    keyboard = new AndroidKeyboard();
    log = new AndroidLog();
    net = new AndroidNet();
    pointer = new AndroidPointer();
    assetManager = new AndroidAssetManager();
    analytics = new AndroidAnalytics();

    this.game = game;
    game.init();
  }

  @Override
  public Storage storage() {
    // TODO(jgw): Implement this on something android-ish.
    return null;
  }

  @Override
  public double time() {
    return System.currentTimeMillis();
  }

  void draw(float delta) {
    // TODO(jgw): This isn't really the right form for the paint/update loop.
    if (game != null) {
      game.paint(delta);
      graphics.rootLayer.paint(new AndroidCanvas(currentCanvas));
    }
  }

  void onKeyDown(final int keyCode) {
    activity.getGameThread().post(new Runnable() {

      @Override
      public void run() {
        keyboard.onKeyDown(keyCode);
      }
    });
  }

  void onKeyUp(final int keyCode) {
    activity.getGameThread().post(new Runnable() {

      @Override
      public void run() {
        keyboard.onKeyUp(keyCode);
      }
    });
  }

  void onPointerEnd(final float x, final float y) {
    activity.getGameThread().post(new Runnable() {

      @Override
      public void run() {
        pointer.onPointerEnd(x, y);
      }
    });
  }

  void onPointerMove(final float x, final float y) {
    activity.getGameThread().post(new Runnable() {

      @Override
      public void run() {
        pointer.onPointerMove(x, y);
      }
    });
  }

  void onPointerStart(final float x, final float y) {
    activity.getGameThread().post(new Runnable() {

      @Override
      public void run() {
        pointer.onPointerStart(x, y);
      }
    });
  }

  void setCurrentCanvas(Canvas c) {
    this.currentCanvas = c;
  }

  void update(float delta) {
    if (game != null) {
      game.update(delta);
    }
  }
}
