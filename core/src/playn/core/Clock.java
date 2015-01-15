/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.core;

/**
 * Encapsulates an absolute and delta time. Used by {@link Game} to emit simulation update and
 * frame paint signals. <em>Note:</em> these values are exposed as public mutable fields for
 * efficiency. Clients should naturally not mutate these values, only the clock provider.
 */
public class Clock {

  /** The number of milliseconds that have elapsed since time 0. */
  public int tick;

  /** The number of milliseconds that have elapsed since the last signal. */
  public int dt;

  /** If this clock is used by a game with separate simulation and paint schedules, this value
    * represents the fraction of time between the last simulation update and the next scheduled
    * update. This value is only provided for the paint clock.
    *
    * <p>For example if the previous update was scheduled to happen at T=500ms and the next update
    * at T=530ms and the actual time at which we are being rendered is T=517ms then alpha will be
    * (517-500)/(530-500) or 17/30. This is usually between 0 and 1, but if your game is running
    * slowly, it can exceed 1. For example, if an update is scheduled to happen at T=500ms and the
    * update actually happens at T=517ms, and the update call itself takes 20ms, the alpha value
    * passed to paint will be (537-500)/(530-500) or 37/30.
    */
  public float alpha;
}
