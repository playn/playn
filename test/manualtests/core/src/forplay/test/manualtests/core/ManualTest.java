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

package forplay.test.manualtests.core;

import forplay.core.Game;
import forplay.core.Mouse;

public abstract class ManualTest implements Game, Mouse.Listener {
  public abstract String getName();

  public abstract String getDescription();

  @Override
  public void update(float delta) {

  }

  @Override
  public void paint(float alpha) {

  }

  @Override
  public int updateRate() {
    return 25;
  }

  @Override
  public void onMouseDown(float x, float y, int button) {

  }

  @Override
  public void onMouseUp(float x, float y, int button) {

  }

  @Override
  public void onMouseMove(float x, float y) {

  }

  @Override
  public void onMouseWheelScroll(float velocity) {

  }
}
