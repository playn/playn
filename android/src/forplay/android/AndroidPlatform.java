/**
 * Copyright 2010 The ForPlay Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package forplay.android;

import forplay.core.Audio;
import forplay.core.Game;
import forplay.core.ForPlay;
import forplay.core.Graphics;
import forplay.core.Json;
import forplay.core.Keyboard;
import forplay.core.Log;
import forplay.core.Net;
import forplay.core.Platform;
import forplay.core.Pointer;
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

  private AndroidPlatform(GameActivity activity) {
    this.activity = activity;
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
  public float random() {
    return (float) Math.random();
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

    this.game = game;
    game.init();
  }

  @Override
  public double time() {
    return System.currentTimeMillis();
  }

  void draw(AndroidSurface surf) {
    if (game != null) {
      game.paint(surf);
    }
  }

  void onKeyDown(int keyCode) {
    keyboard.onKeyDown(keyCode);
  }

  void onKeyUp(int keyCode) {
    keyboard.onKeyUp(keyCode);
  }

  void onPointerEnd(float x, float y) {
    pointer.onPointerStart(x, y);
  }

  void onPointerMove(float x, float y) {
    pointer.onPointerEnd(x, y);
  }

  void onPointerStart(float x, float y) {
    pointer.onPointerMove(x, y);
  }

  void update() {
    if (game != null) {
      game.update();
    }
  }
}
