/**
 * Copyright 2010 The PlayN Authors
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
package playn.core;

import react.Signal;
import react.Slot;

/**
 * Defines a simple game API. It's not necessary to use this abstraction for your PlayN games, but
 * it takes care of some standard stuff that most games are likely to want.
 *
 * <p>This implementation separates game processing into two phases: simulation and render. The
 * simulation phase takes place via {@link #update} and is called with a monotonoically increasing
 * timer at a fixed rate. The interpolation phase takes place via {@link #paint} and is called
 * every time the game is rendered to the display (which may be more frequently than the simulation
 * is updated). The render phase will generally interpolate the values computed in {@link #update}
 * to provide smooth rendering based on lower-frequency simulation updates.
 *
 * @see <a href="http://code.google.com/p/playn/wiki/GameLoop">Understanding the Game Loop</a>
 */
public abstract class Game {

  /** The platform on which this game is running. */
  public final Platform plat;

  /** A signal emitted on every simulation update. */
  public final Signal<Clock> update = Signal.create();

  /** A signal emitted on every frame. */
  public final Signal<Clock> paint = Signal.create();

  private final Clock updateClock = new Clock();
  private final Clock paintClock = new Clock();
  private final int updateRate;
  private int nextUpdate;

  /** Creates a clocked game with the desired simulation update rate, in ms. */
  public Game (Platform plat, int updateRate) {
    assert updateRate > 0 : "updateRate must be greater than zero.";
    this.plat = plat;
    this.updateRate = updateRate;
    plat.frame.connect(new Slot<Platform>() {
      public void onEmit (Platform plat) { onFrame(); }
    });
  }

  /** Called on every simulation update. The default implementation emits the clock to the {@link
    * #updateClock} signal, but you can override this method to change or augment this behavior.
    * @param clock a clock configured with the update timing information.
    */
  public void update (Clock clock) {
    update.emit(clock);
  }

  /** Called on every frame. The default implementation emits the clock to the {@link #paintClock}
    * signal, but you can override this method to change or augment this behavior.
    * @param clock a clock configured with the frame timing information.
    */
  public void paint (Clock clock) {
    paint.emit(paintClock);
  }

  private void onFrame () {
    int nextUpdate = this.nextUpdate;
    int updateTick = plat.tick();
    if (updateTick >= nextUpdate) {
      int updateRate = this.updateRate;
      int updates = 0;
      while (updateTick >= nextUpdate) {
        nextUpdate += updateRate;
        updates++;
      }
      this.nextUpdate = nextUpdate;
      int updateDt = updates*updateRate;
      updateClock.tick += updateDt;
      updateClock.dt = updateDt;
      update(updateClock);
    }

    int paintTick = plat.tick();
    paintClock.dt = paintTick - paintClock.tick;
    paintClock.tick = paintTick;
    paintClock.alpha = 1 - (nextUpdate - paintTick) / (float)updateRate;
    paint(paintClock);
  }
}
