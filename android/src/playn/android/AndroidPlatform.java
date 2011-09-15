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

import playn.core.Game;
import playn.core.Json;
import playn.core.Mouse;
import playn.core.Platform;
import playn.core.PlayN;
import playn.java.JavaJson;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PixelFormat;
import android.net.Uri;

public class AndroidPlatform implements Platform {
  public static final boolean DEBUG_LOGS = true;

  static AndroidPlatform instance;
  private static AndroidGL20 gl20;

  public static void register(AndroidGL20 _gl20, GameActivity activity) {
    gl20 = _gl20;
    PlayN.setPlatform(new AndroidPlatform(activity));
  }

  Game game;
  GameActivity activity;

  private AndroidAudio audio;
  private AndroidGraphics graphics;
  private JavaJson json;
  private AndroidKeyboard keyboard;
  private AndroidLog log;
  private AndroidNet net;
  private AndroidPointer pointer;
  private AndroidStorage storage;
  private AndroidTouch touch;
  private AndroidTouchEventHandler touchHandler;
  private AndroidAssetManager assetManager;
  private AndroidAnalytics analytics;

  public Bitmap.Config preferredBitmapConfig;

  private AndroidPlatform(GameActivity activity) {
    instance = this;
    this.activity = activity;
    audio = new AndroidAudio();
    graphics = new AndroidGraphics(gl20);
    json = new JavaJson();
    keyboard = new AndroidKeyboard();
    log = new AndroidLog();
    net = new AndroidNet();
    pointer = new AndroidPointer();
    touch = new AndroidTouch();
    touchHandler = new AndroidTouchEventHandler(activity.gameView());
    assetManager = new AndroidAssetManager();
    analytics = new AndroidAnalytics();
    storage = new AndroidStorage(activity);

    assetManager.assets = activity.getAssets();

    preferredBitmapConfig = mapDisplayPixelFormat();
  }

  /**
   * Determines the most performant pixel format for the active display.
   */
  // TODO:  This method will require testing over a variety of devices.
  private Config mapDisplayPixelFormat() {
    int format = activity.getWindowManager().getDefaultDisplay().getPixelFormat();
    ActivityManager activityManager = (ActivityManager) activity.getApplication().getSystemService(
        Context.ACTIVITY_SERVICE);
    int memoryClass = activityManager.getMemoryClass();

    // For low memory devices (like the HTC Magic), prefer 16-bit bitmaps
    // FIXME: The memoryClass check is from the Canvas-only implementation and may function incorrectly with OpenGL
    if (format == PixelFormat.RGBA_4444 ||  memoryClass <= 16)
      return Bitmap.Config.ARGB_4444;
    else return Bitmap.Config.ARGB_8888;
  }

  @Override
  public AndroidAssetManager assetManager() {
    return assetManager;
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
    return null;
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
