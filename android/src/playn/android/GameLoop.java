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

import static playn.core.PlayN.log;

import java.util.concurrent.atomic.AtomicBoolean;

public class GameLoop implements Runnable {
  private static final boolean LOG_FPS = false;
  private static final int MAX_DELTA = 100;

  private AtomicBoolean running = new AtomicBoolean();
  private final AndroidPlatform platform;

  private long timeOffset = System.currentTimeMillis();

  private int updateRate;
  private float accum;
  private int lastTime;

  private float totalTime;
  private int framesPainted;

  public GameLoop(AndroidPlatform platform) {
    this.platform = platform;
  }

  public void start() {
    if (!running.get()) {
      if (AndroidPlatform.DEBUG_LOGS)
        log().debug("Starting game loop");
      this.updateRate = platform.game.updateRate();
      running.set(true);
    }
  }

  public void pause() {
    if (AndroidPlatform.DEBUG_LOGS)
      log().debug("Pausing game loop");
    running.set(false);
  }

  @Override
  public void run() {
    // The thread can be stopped between runs.
    if (!running.get())
      return;

    int now = time();
    float delta = now - lastTime;
    if (delta > MAX_DELTA)
      delta = MAX_DELTA;
    lastTime = now;

    if (updateRate == 0) {
      platform.update(delta);
      accum = 0;
    } else {
      accum += delta;
      while (accum >= updateRate) {
        platform.update(updateRate);
        accum -= updateRate;
      }
    }

    paint((updateRate == 0) ? 0 : accum / updateRate);

    if (LOG_FPS) {
      totalTime += delta / 1000;
      framesPainted++;
      if (totalTime > 1) {
        log().info("FPS: " + framesPainted / totalTime);
        totalTime = framesPainted = 0;
      }
    }
  }

  private int time() {
    // System.nanoTime() would be better here, but it's busted on the HTC EVO
    // 2.3 update. Instead we use an offset from a known time to keep it within
    // int range.
    return (int) (System.currentTimeMillis() - timeOffset);
  }

  public boolean running() {
    return running.get();
  }

  protected void paint(float paintAlpha) {
    platform.graphics().preparePaint();
    platform.game.paint(paintAlpha); // Run the game's custom painting code
    platform.graphics().paintLayers(); // Paint the scene graph
  }
}
