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
import android.os.AsyncTask;
import android.util.Log;

import playn.core.AbstractPlatform;
import playn.core.Game;
import playn.core.Json;
import playn.core.Mouse;
import playn.core.MouseStub;
import playn.core.TouchImpl;
import playn.core.json.JsonImpl;

public class AndroidPlatform extends AbstractPlatform {

  public static final boolean DEBUG_LOGS = false;

  Game game;
  GameActivity activity;
  private boolean paused;

  private final AndroidAnalytics analytics;
  private final AndroidAssets assets;
  private final AndroidAudio audio;
  private final AndroidGraphics graphics;
  private final AndroidKeyboard keyboard;
  private final AndroidNet net;
  private final AndroidPointer pointer;
  private final AndroidStorage storage;
  private final TouchImpl touch;
  private final Json json;
  private final long start = System.nanoTime();

  protected AndroidPlatform(GameActivity activity, AndroidGL20 gl20) {
    super(new AndroidLog(activity));
    this.activity = activity;

    audio = new AndroidAudio(this);
    graphics = new AndroidGraphics(this, gl20, activity.preferredBitmapConfig());
    analytics = new AndroidAnalytics();
    assets = new AndroidAssets(this);
    json = new JsonImpl();
    keyboard = new AndroidKeyboard(this);
    net = new AndroidNet(this);
    pointer = new AndroidPointer();
    storage = new AndroidStorage(this);
    touch = new TouchImpl();
  }

  static void debugLog(String message) {
    if (DEBUG_LOGS) Log.d("playn", message);
  }

  @Override
  public void invokeLater(Runnable runnable) {
    // if we're paused, we need to run these on the main app thread instead of queueing them up for
    // processing on the run queue, because the run queue isn't processed while we're paused; the
    // main thread will ensure they're run serially, but also that they don't linger until the next
    // time the app is resumed (if that happens at all)
    if (paused)
      activity.runOnUiThread(runnable);
    else
      super.invokeLater(runnable);
  }

  @Override
  public void invokeAsync(final Runnable action) {
    activity.runOnUiThread(new Runnable() {
      public void run () {
        new AsyncTask<Void,Void,Void>() {
          @Override public Void doInBackground(Void... params) {
            try {
              action.run();
            } catch (Exception e) {
              log.warn("Async task failure [task=" + action + "]", e);
            }
            return null;
          }
        }.execute();
      }
    });
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
    return new MouseStub();
  }

  @Override
  public TouchImpl touch() {
    return touch;
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
  public int tick() {
    return (int)((System.nanoTime() - start) / 1000000L);
  }

  @Override
  public void setPropagateEvents(boolean propagate) {
    touch.setPropagateEvents(propagate);
    pointer.setPropagateEvents(propagate);
  }

  @Override
  public Type type() {
    return Type.ANDROID;
  }

  // allow these to be called by GameActivity
  @Override
  protected void onPause() {
    super.onPause();
    paused = true;
  }
  @Override
  protected void onResume() {
    super.onResume();
    paused = false;
  }

  boolean paused() {
    return paused;
  }
  @Override
  protected void onExit() {
    super.onExit();
  }

  void update() {
    runQueue.execute();
    if (game != null) {
      game.tick(tick());
    }
  }
}
