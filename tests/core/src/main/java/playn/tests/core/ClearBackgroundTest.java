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

package playn.tests.core;

import pythagoras.f.FloatMath;

import playn.core.Color;
import playn.core.SurfaceLayer;
import static playn.core.PlayN.*;

public class ClearBackgroundTest extends Test {

  private int time;
  private SurfaceLayer square;
  static int width = 100;
  static int height = 100;

  @Override
  public String getName() {
    return "ClearBackgroundTest";
  }

  @Override
  public String getDescription() {
    return "Test that the platform correctly clears the background to black between frames, " +
      "even if nothing is painted.";
  }

  @Override
  public void init() {
    // remove the background layer added by default
    graphics().rootLayer().clear();

    // add a grey square
    square = graphics().createSurfaceLayer(width, height);
    square.surface().setFillColor(Color.rgb(200, 200, 200));
    square.surface().fillRect(0, 0, square.surface().width(), square.surface().height());
    graphics().rootLayer().add(square);
  }

  @Override
  public void update(int delta) {
    time += delta;
  }

  @Override
  public void paint(float alpha) {
    float t = (time + alpha*UPDATE_RATE) / 1000;
    square.setTranslation((FloatMath.cos(t) + 1) * (graphics().width() - width) / 2,
                          (FloatMath.sin(t) + 1) * (graphics().height() - height) / 2);
  }
}
