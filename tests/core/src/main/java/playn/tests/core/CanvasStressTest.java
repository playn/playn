/**
 * Copyright 2013 The PlayN Authors
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
import react.Connection;
import react.Slot;

import playn.core.*;
import playn.scene.*;

public class CanvasStressTest extends Test {

  public CanvasStressTest (TestsGame game) {
    super(game);
  }

  @Override public String getName() {
    return "Canvas Stress Test";
  }

  @Override public String getDescription() {
    return "Animates a full-screen sized canvas, forcing a massive reupload of image data to " +
      "the GPU on every frame.";
  }

  @Override public void init() {
    final Canvas canvas = game.graphics.createCanvas(game.graphics.viewSize);
    final Texture canvasTex = game.graphics.createTexture(canvas.image);
    game.rootLayer.add(new ImageLayer(canvasTex));

    conns.add(game.update.connect(new Slot<TestsGame>() {
      private int noSegs = 30;
      private int direction = 1;

      public void onEmit (TestsGame game) {
        canvas.clear();
        canvas.setStrokeWidth(3);
        canvas.setStrokeColor(0x88ff0000);

        noSegs += direction;
        if (noSegs > 50) direction = -1;
        if (noSegs < 20) direction = 1;

        final float r = 100;
        for (int ii = 0; ii < noSegs; ii++) {
          float angle = 2*FloatMath.PI * ii / noSegs;
          float x = (r * FloatMath.cos(angle)) + game.graphics.viewSize.width() / 2;
          float y = (r * FloatMath.sin(angle)) + game.graphics.viewSize.height() /2;
          canvas.strokeCircle(x, y, 100);
        }

        // TODO: update canvasTex with canvas.image
      }
    }));
  }
}
