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
  public final Signal<Game> update = Signal.create();

  /** A signal emitted on every frame. */
  public final Signal<Game> paint = Signal.create();

  /** The number of millis that have elapsed since the last update. This will be a multiple of
    * the {@code updateRate} supplied to the constructor, usually one. */
  public int updateDelta;

  /** The game tick (monotonically increasing value in millis) immediately prior to emitting
    * {@link #update}. */
  public int updateTick;

  /** The game tick (monotonically increasing value in millis) immediately prior to emitting
    * {@link #paint}. */
  public int paintTick;

  /** the ms between this {@link #painttick} and the previous. some animation apis really just want
    * to know how many ms have elapsed since they last did their interpolation, and this saves them
    * the trouble of having to track it themselves. */
  public int paintDt;

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
      this.updateTick = updateTick;
      this.updateDelta = updates*updateRate;
      update.emit(this);
    }

    int newPaintTick = plat.tick();
    paintDt = newPaintTick - paintTick;
    paintTick = newPaintTick;
    paint.emit(this);
  }
}
