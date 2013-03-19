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

/**
 * Main game interface. To start a new game, implement this interface and call {@link PlayN#run}.
 *
 * @see <a href="http://code.google.com/p/playn/wiki/GameLoop">Understanding the Game Loop</a>
 */
public interface Game {

  /**
   * Called once on initialization. Most setup work should be performed in this method, as all
   * PlayN subsystems are guaranteed to be available when it is called.
   */
  void init();

  /**
   * Called once per game logic tick. Input-handling, physics, and game logic should be performed
   * in this method. It is also appropriate to update the structure of the scene graph here
   * (adding, removing layers). You should not animate scene graph properties in update; such
   * actions should be taken in {@link #paint}.
   *
   * @param delta time, in ms, passed since the previous {@link #update} (if {@link #updateRate} is
   * zero, this will be real time, otherwise it will always be {@link #updateRate}).
   */
  void update(float delta);

  /**
   * Called as frequently as the backend refreshes the display. Any manual painting (e.g. {@link
   * Surface#drawImage}) should be performed in this method. Animating of scene graph elements
   * (smoothly updating an element's transform or alpha, for example) should also take place here.
   *
   * <p> You should not run game logic in this method, as it's not always guaranteed to be called
   * frequently (for example, when the game is not visible this method may not be called). </p>
   *
   * @param alpha a value between 0 and 1, this represents how far we have elapsed along the time
   * line between the previous update tick and the next. For example if the previous update was
   * scheduled to happen at T=500ms and the next update at T=530ms (i.e. {@link #updateRate}
   * returns 30) and the actual time at which we are being rendered is T=517ms then alpha will be
   * (517-500)/(530-500) or 17/30. Note: if {@link #updateRate} is zero, this value will always be
   * zero.
   */
  void paint(float alpha);

  /**
   * Return the update rate of the main game loop, in ms.
   *
   * <p> Using an update rate of zero will cause {@link #update} to be called precisely once per
   * rendered frame, regardless of the frame-rate. </p>
   *
   * @return update rate of the main game loop, in ms.
   */
  int updateRate();
}
