/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.core;

/**
 * Main game interface. To start a new game, implement this interface and call
 * {@link ForPlay#run(Game)}.
 * 
 * TODO(jgw): Callbacks for resize, background, foreground, shutdown.
 * 
 * TODO(jgw): Link to game loop wiki page.
 */
public interface Game {

  /**
   * Called once on initialization. Most setup work should be performed in this
   * method, as all ForPlay subsystems are guaranteed to be available when it is
   * called.
   */
  void init();

  /**
   * Called once per game logic tick. Input-handling, physics, and game logic
   * should be performed in this method.
   * 
   * @param delta time, in ms, passed since the previous {@link #update(float)}
   *          (if {@link #updateRate()} is zero, this will be wall time, otherwise
   *          it will always be {@link #updateRate()})
   */
  void update(float delta);

  /**
   * Paint callback. All painting should be performed in this method, including
   * upates to the layer hierarchy.
   * 
   * <p>
   * You should not run game logic in this method, as it's not always guaranteed
   * to be called frequently (for example, when the game is not visible this
   * method may not be called.
   * </p>
   * 
   * @param alpha a value between 0 and 1, representing the proportion of time
   *          passed between the last two physics updates (if {@link
   *          #updateRate()} is zero, this value will always be zero.
   */
  void paint(float alpha);

  /**
   * Return the update rate of the main game loop.
   * 
   * <p>
   * Using an update rate of zero will cause {@link #update()} to be called
   * precisely once per rendered frame, regardless of the frame-rate.
   * </p>
   * 
   * @return update rate of the main game loop, in ms.
   */
  int updateRate();
}
