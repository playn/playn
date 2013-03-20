/**
 * Copyright 2012 The PlayN Authors
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
package playn.core.util;

/**
 * Used to provide the current interpolated time. The {@code Source} implementation takes care of
 * tracking and computing the current interpolated time. Use like so:
 *
 * <pre>{@code
 * class MyGame extends Game.Default {
 *   private final static int UPDATE_RATE = 50;
 *   private final Clock.Source clock = new Clock.Source(UPDATE_RATE);
 *   public MyGame () {
 *     super(UPDATE_RATE);
 *   }
 *   @Override public void update (int delta) {
 *     clock.update(delta);
 *   }
 *   @Override public void paint (float alpha) {
 *     clock.paint(alpha);
 *     // pass clock into any code that needs an alpha-adjusted timestamp
 *   }
 * }
 * }</pre>
 */
public interface Clock {

  /** A {@link Clock} implementation that works nicely with {@link playn.core.Game.Default}. */
  public static class Source implements Clock {
    private final int updateRate;
    private int elapsed;
    private float current;

    public Source(int updateRate) {
      this.updateRate = updateRate;
    }

    @Override
    public float time() {
      return current;
    }

    /** Call this from {@link playn.core.Game.Default#update}. */
    public void update(int delta) {
      elapsed += delta;
      current = elapsed;
    }

    /** Call this from {@link playn.core.Game.Default#paint}. */
    public void paint(float alpha) {
      current = elapsed + alpha * updateRate;
    }
  }

  /** Returns the current time, adjusted for the latest paint's alpha. */
  float time();
}
