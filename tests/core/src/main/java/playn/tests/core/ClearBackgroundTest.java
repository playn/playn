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

import pythagoras.f.IDimension;
import pythagoras.f.FloatMath;

import playn.core.*;
import playn.scene.*;
import react.Slot;

public class ClearBackgroundTest extends Test {

  static int width = 100;
  static int height = 100;

  public ClearBackgroundTest (TestsGame game) {
    super(game, "ClearBackground",
          "Test that the platform correctly clears the background to black between frames, " +
          "even if nothing is painted.");
  }

  @Override public void init () {
    // remove the background layer added by default
    game.rootLayer.disposeAll();

    // add a grey square
    TextureSurface surf = game.createSurface(width, height);
    surf.begin().setFillColor(Color.rgb(200, 200, 200)).fillRect(0, 0, width, height).end().close();
    final ImageLayer square = new ImageLayer(surf.texture);
    game.rootLayer.add(square);

    conns.add(game.paint.connect(new Slot<Clock>() {
      public void onEmit (Clock clock) {
        float t = clock.tick / 1000f;
        IDimension vsize = game.plat.graphics().viewSize;
        square.setTranslation((FloatMath.cos(t) + 1) * (vsize.width() - width)/2,
                              (FloatMath.sin(t) + 1) * (vsize.height() - height)/2);
      }
    }));
  }
}
