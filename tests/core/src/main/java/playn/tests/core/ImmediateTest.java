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

import playn.core.*;
import playn.scene.*;
import react.Slot;

public class ImmediateTest extends Test {

  private float elapsed, rotation;

  public ImmediateTest (TestsGame game) {
    super(game, "ClippedLayerTest",
          "Tests rendering of simple layers with and without clipping. Clipped blue layers " +
          "should not overdraw one pixel black lines that circumscribes them.");
  }

  @Override public void init() {
    Canvas circle = game.graphics.createCanvas(100, 100);
    circle.setFillColor(0xFFCC99FF).fillCircle(50, 50, 50);
    final Texture cirtex = game.graphics.createTexture(circle.bitmap);

    Canvas sausage = game.graphics.createCanvas(100, 50);
    Gradient linear = sausage.createGradient(new Gradient.Linear(
      0, 0, 100, 100, new int[] { 0xFF0000FF, 0xFF00FF00 }, new float[] { 0, 1 }));
    sausage.setFillGradient(linear).fillRoundRect(0, 0, 100, 50, 10);
    final Texture saustex = game.graphics.createTexture(sausage.bitmap);

    // add an unclipped layer which will draw our background and outlines
    game.rootLayer.add(new Layer() {
      @Override protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFFFFCC99).fillRect(
          0, 0, game.graphics.viewSize.width(), game.graphics.viewSize.height());

        // fill a rect that will be covered except for one pixel by the clipped immediate layers
        surf.setFillColor(0xFF000000);
        surf.fillRect(29, 29, 202, 202);
        surf.fillRect(259, 29, 102, 102);
        surf.fillRect(259, 159, 102, 102);
      }
    });

    // add a clipped layer that will clip a fill and image draw
    ClippedLayer clayer = new ClippedLayer(200, 200) {
      protected void paintClipped (Surface surf) {
        // this fill should be clipped to our bounds
        surf.setFillColor(0xFF99CCFF);
        surf.fillRect(-50, -50, 300, 300);
        // and this image should be clipped to our bounds
        surf.draw(cirtex, 125, -25);
      }
    };
    // adjust the origin to ensure that is accounted for in the clipping
    game.rootLayer.addAt(clayer.setOrigin(100, 100), 130, 130);

    // add a clipped layer that draws an image through a rotation transform
    game.rootLayer.addAt(new ClippedLayer(100, 100) {
      protected void paintClipped (Surface surf) {
        surf.setFillColor(0xFF99CCFF).fillRect(0, 0, 100, 100);
        surf.translate(50, 50).rotate(rotation).translate(-50, -50);
        surf.draw(saustex, 0, 25);
      }
    }, 260, 30);

    // add a clipped layer that draws an image through a translation transform
    game.rootLayer.addAt(new ClippedLayer(100, 100) {
      protected void paintClipped (Surface surf) {
        surf.setFillColor(0xFF99CCFF).fillRect(0, 0, 100, 100);
        surf.translate(FloatMath.sin(elapsed) * 50, FloatMath.cos(elapsed) * 50 + 25);
        surf.draw(saustex, 0, 0);
      }
    }, 260, 160);

    conns.add(game.paint.connect(new Slot<Clock>() {
      public void onEmit (Clock clock) {
        elapsed = clock.tick/1000f;
        rotation = elapsed * FloatMath.PI/2;
      }
    }));
  }
}
