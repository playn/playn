/**
 * Copyright 2011 The PlayN Authors
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
package playn.android;

import android.content.Intent;
import android.net.Uri;

import playn.core.Game;
import playn.core.Json;
import playn.core.Mouse;
import playn.core.Platform;
import playn.core.PlayN;
import playn.core.json.JsonImpl;

public class AndroidPlatform implements Platform {
  public static final boolean DEBUG_LOGS = true;

  public static AndroidPlatform register(AndroidGL20 gl20, GameActivity activity) {
    AndroidPlatform platform = new AndroidPlatform(activity, gl20);
    PlayN.setPlatform(platform);
    return platform;
  }

  Game game;
  GameActivity activity;
  private final AndroidGL20 gl20;

  private AndroidAudio audio;
  private AndroidGraphics graphics;
  private Json json;
  private AndroidKeyboard keyboard;
  private AndroidLog log;
  private AndroidNet net;
  private AndroidPointer pointer;
  private AndroidStorage storage;
  private AndroidTouch touch;
  private AndroidTouchEventHandler touchHandler;
  private AndroidAssets assets;
  private AndroidAnalytics analytics;

  protected AndroidPlatform(GameActivity activity, AndroidGL20 gl20) {
    this.activity = activity;
    this.gl20 = gl20;

    audio = new AndroidAudio(activity);
    touchHandler = new AndroidTouchEventHandler(activity.gameView());
    graphics = new AndroidGraphics(activity, gl20, touchHandler);
    json = new JsonImpl();
    keyboard = new AndroidKeyboard();
    log = new AndroidLog();
    net = new AndroidNet();
    pointer = new AndroidPointer();
    touch = new AndroidTouch();
    assets = new AndroidAssets(activity.getAssets(), graphics, audio);
    analytics = new AndroidAnalytics();
    storage = new AndroidStorage(activity);
  }

  @Override
  public AndroidAssets assets() {
    return assets;
  }

  @Override
  public AndroidAnalytics analytics() {
    return analytics;
  }

  @Override
  public AndroidAudio audio() {
    return audio;
  }

  @Override
  public AndroidGraphics graphics() {
    return graphics;
  }

  @Override
  public Json json() {
    return json;
  }

  @Override
  public AndroidKeyboard keyboard() {
    return keyboard;
  }

  @Override
  public AndroidLog log() {
    return log;
  }

  @Override
  public AndroidNet net() {
    return net;
  }

  @Override
  public void openURL(String url) {
    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    activity.startActivity(browserIntent);
  }

  @Override
  public Mouse mouse() {
    return new Mouse() {
      public void setListener(Listener listener) {
        log().warn("Mouse not supported on Android.");
      }
    };
  }

  @Override
  public AndroidTouch touch() {
    return touch;
  }

  public AndroidTouchEventHandler touchEventHandler() {
    return touchHandler;
  }

  @Override
  public AndroidPointer pointer() {
    return pointer;
  }

  @Override
  public float random() {
    return (float) Math.random();
  }

  @Override
  public AndroidRegularExpression regularExpression() {
    return new AndroidRegularExpression();
  }

  @Override
  public void run(Game game) {
    this.game = game;
    game.init();
  }

  @Override
  public AndroidStorage storage() {
    return storage;
  }

  @Override
  public double time() {
    return System.currentTimeMillis();
  }

  @Override
  public Type type() {
    return Type.ANDROID;
  }

  void update(float delta) {
    if (game != null) {
      game.update(delta);
    }
  }
}
