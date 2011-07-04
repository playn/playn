/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.net.Uri;
import forplay.core.ForPlay;
import forplay.core.Game;
import forplay.core.Json;
import forplay.core.Mouse;
import forplay.core.Platform;
import forplay.core.Touch;
import forplay.java.JavaJson;

public class AndroidPlatform implements Platform {

  static AndroidPlatform instance;

  public static void register(GameActivity activity) {
    ForPlay.setPlatform(instance = new AndroidPlatform(activity));
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
  private AndroidAssetManager assetManager;
  private AndroidAnalytics analytics;

  public Bitmap.Config preferredBitmapConfig;

  private AndroidPlatform(GameActivity activity) {
    this.activity = activity;
    audio = new AndroidAudio();
    graphics = new AndroidGraphics(activity);
    json = new JavaJson();
    keyboard = new AndroidKeyboard();
    log = new AndroidLog();
    net = new AndroidNet();
    pointer = new AndroidPointer();
    assetManager = new AndroidAssetManager();
    analytics = new AndroidAnalytics();
    storage = new AndroidStorage(activity);

    assetManager.assets = activity.getAssets();
    ActivityManager activityManager = (ActivityManager) activity.getApplication().getSystemService(
        Activity.ACTIVITY_SERVICE);
    int memoryClass = activityManager.getMemoryClass();
    
    // For low memory devices (like the HTC Magic), prefer 16-bit bitmaps
    preferredBitmapConfig = memoryClass <= 16 ? Bitmap.Config.ARGB_4444 : mapDisplayPixelFormat();
  }

  /**
   * Determines the most performant pixel format for the active display.
   */
  private Config mapDisplayPixelFormat() {
    int format = activity.getWindowManager().getDefaultDisplay().getPixelFormat();

    if (format == PixelFormat.RGBA_8888 || format == PixelFormat.RGBX_8888)
      return Bitmap.Config.ARGB_8888;
    return Bitmap.Config.ARGB_4444;
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
  public Touch touch() {
    // TODO(pdr): need to implement this.
    return null;
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

  void draw(Canvas c, float delta) {
    AndroidImage.prevMru = AndroidImage.mru;
    AndroidImage.mru = new ArrayList<Bitmap>();

    if (game != null) {
      game.paint(delta);

      AndroidCanvas surf = new AndroidCanvas(c);
      surf.clear();
      graphics.rootLayer.paint(surf);
    }

    AndroidImage.prevMru = null;
  }

  void update(float delta) {
    if (game != null) {
      game.update(delta);
    }
  }
}
