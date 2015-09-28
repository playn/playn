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
import android.util.Log;

import playn.core.*;
import playn.core.json.JsonImpl;

public class AndroidPlatform extends Platform {

  public static final boolean DEBUG_LOGS = false;

  Game game;
  GameActivity activity;
  private enum State { RUNNING, PAUSED, EXITED };
  private State state = State.RUNNING;

  private final AndroidAssets assets;
  private final AndroidAudio audio;
  private final AndroidExec exec;
  private final AndroidGraphics graphics;
  private final AndroidInput input;
  private final AndroidLog log;
  private final AndroidNet net;
  private final AndroidStorage storage;
  private final Json json;
  private final long start = System.nanoTime();

  public AndroidPlatform (GameActivity activity) {
    this.activity = activity;

    log = new AndroidLog(activity.logIdent());
    exec = new AndroidExec(this, activity) {
      @Override protected boolean isPaused () { return state == State.PAUSED; }
    };
    audio = new AndroidAudio(this);
    graphics = new AndroidGraphics(this, activity.preferredBitmapConfig(), activity.scaleFactor());
    assets = new AndroidAssets(this);
    json = new JsonImpl();
    input = new AndroidInput(this);
    net = new AndroidNet(exec);
    storage = new AndroidStorage(this);
  }

  static void debugLog(String message) {
    if (DEBUG_LOGS) Log.d("playn", message);
  }

  @Override public Type type() { return Type.ANDROID; }
  @Override public double time() { return System.currentTimeMillis(); }
  @Override public int tick() { return (int)((System.nanoTime() - start) / 1000000L); }

  @Override public void openURL(String url) {
    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    activity.startActivity(browserIntent);
  }

  @Override public AndroidAssets assets() { return assets; }
  @Override public AndroidAudio audio() { return audio; }
  @Override public AndroidGraphics graphics() { return graphics; }
  @Override public AndroidInput input() { return input; }
  @Override public AndroidLog log() { return log; }
  @Override public AndroidNet net() { return net; }
  @Override public AndroidStorage storage() { return storage; }
  @Override public Exec exec() { return exec; }
  @Override public Json json() { return json; }

  // note: these are called by GameActivity
  void onPause() {
    state = State.PAUSED;
    dispatchEvent(lifecycle, Lifecycle.PAUSE);
  }
  void onResume() {
    state = State.RUNNING;
    dispatchEvent(lifecycle, Lifecycle.RESUME);
  }
  void onExit() {
    state = State.EXITED;
    dispatchEvent(lifecycle, Lifecycle.EXIT);
  }

  void processFrame() { emitFrame(); }
}
