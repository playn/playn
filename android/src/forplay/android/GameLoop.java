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

import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

public abstract class GameLoop implements Runnable {
  private static final int MAX_DELTA = 100;

  private AtomicBoolean running = new AtomicBoolean();

  private long timeOffset = System.currentTimeMillis();

  private int updateRate;
  private int accum;
  private int lastTime;
  private final View view;

  private int lastStats;
  private int updateCount;
  private int updateTime;
  private int paintCount;
  private int paintQueueStart;
  private int runQueueStart;
  private int paintTime, paintLagTime;
  private int runCount;
  private int runQueueTime;

  private float paintAlpha;

  public GameLoop(View view) {
    this.view = view;
  }

  public void start() {
    lastStats = time();

    if (!running.get()) {
      Log.i("forplay", "Starting game loop");
      this.updateRate = AndroidPlatform.instance.game.updateRate();
      running.set(true);
      runQueueStart = lastTime = time();
      view.post(this);
    }
  }

  public void end() {
    Log.i("forplay", "Halting game loop");
    running.set(false);
  }

  public void run() {
    // The thread can be stopped between runs.
    if (!running.get())
      return;

    runCount++;

    int now = time();
    float delta = now - lastTime;
    if (delta > MAX_DELTA)
      delta = MAX_DELTA;

    lastTime = now;
    runQueueTime += now - runQueueStart;

    if (now - lastStats > 10000) {
      if (paintCount > 0)
        Log.i("forplay", "Stats: paints = " + paintCount + " " + ((float) paintTime / paintCount)
            + "ms (" + ((float) paintCount / (now - lastStats) * 1000) + "/s) lag = "
            + ((float) paintLagTime / paintCount) + "ms");
      if (updateCount > 0)
        Log.i("forplay", "Stats: updates = " + updateCount + " "
            + ((float) updateTime / updateCount) + "ms ("
            + ((float) updateCount / (now - lastStats) * 1000) + "/s)");
      Log.i("forplay", "Stats: runs = " + runCount + " run lag = "
          + ((float) runQueueTime / runCount) + "ms (" + runCount / ((float) now - lastStats)
          * 1000 + "/s)");
      paintCount = updateCount = runCount = 0;
      paintTime = paintLagTime = updateTime = 0;
      lastStats = now;
    }

    boolean isPaintDirty = false;

    if (updateRate == 0) {
      AndroidPlatform.instance.update(delta);
      accum = 0;
      isPaintDirty = true;
    } else {
      accum += delta;
      while (accum >= updateRate) {
        updateCount++;
        double start = time();
        AndroidPlatform.instance.update(updateRate);
        updateTime += time() - start;
        accum -= updateRate;
        isPaintDirty = true;
      }
    }

    if (isPaintDirty) {
      paintAlpha = (updateRate == 0) ? 0 : accum / updateRate;
      paintQueueStart = time();
      paint();
    }

    // A stop request can occur during this run, in which case we must not
    // try to schedule a new loop.
    if (running.get()) {
      runQueueStart = time();

      // Did we spend so much time in this method that we're due for another
      // update?
      if (updateRate > 0 && runQueueStart - now > updateRate)
        view.post(this);
      else
        view.postDelayed(this, 1);
    }
  }

  private int time() {
    // System.nanoTime() would be better here, but it's busted on the HTC EVO
    // 2.3 update. Instead we use an offset from a known time to keep it within
    // int range.
    return (int) (System.currentTimeMillis() - timeOffset);
  }

  protected abstract void paint();

  void paint(Canvas c) {
    double start = time();
    paintLagTime += start - paintQueueStart;
    AndroidPlatform.instance.draw(c, paintAlpha);
    paintTime += time() - start;
    paintCount++;
  }
}
