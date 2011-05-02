/**
 * Copyright 2010 The ForPlay Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.bench.core;

import static forplay.core.ForPlay.log;
import static forplay.core.ForPlay.currentTime;
import forplay.core.GroupLayer;

abstract class TimeTest {

  private static final int FREQ_SAMPLES = 10;
  private static final double TARGET_FREQ = 33;
  private static final double EPSILON = 4;
  private static final int SETTLE_FRAMES = 100;

  private double[] freqs = new double[FREQ_SAMPLES];
  private int freqOffset;
  private double lastTime = currentTime();
  private int doneFrames;
  private boolean done;
  private int logCountdown = 30;

  final void paint() {
    if (--logCountdown == 0) {
      logCountdown = 30;
      log().info("count: " + count());
    }

    double now = currentTime();
    freqs[freqOffset++] = now - lastTime;
    if (freqOffset == freqs.length) {
      freqOffset = 0;
    }
    lastTime = now;

    if (averageFreq() < TARGET_FREQ - EPSILON) {
      doneFrames = 0;
      advance();
    } else if (averageFreq() > TARGET_FREQ + EPSILON) {
      doneFrames = 0;
      retreat();
    } else {
      ++doneFrames;
      if (doneFrames >= SETTLE_FRAMES) {
        done = true;
      }
    }

    doPaint();
  }

  abstract String name();

  abstract void init(GroupLayer root);

  abstract void cleanup();

  abstract double score();

  boolean done() {
    return done;
  }

  protected abstract void doPaint();

  protected abstract void advance();

  protected abstract void retreat();

  private double averageFreq() {
    double total = 0;
    for (int i = 0; i < freqs.length; ++i) {
      total += freqs[i];
    }
    return total / FREQ_SAMPLES;
  }

  protected int count() {
    // TODO Auto-generated method stub
    return 0;
  }
}
