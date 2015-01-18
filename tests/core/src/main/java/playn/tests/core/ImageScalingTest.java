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

import java.util.Arrays;

import pythagoras.f.FloatMath;
import react.RFuture;
import react.Slot;
import react.UnitSlot;

import playn.core.*;
import playn.core.Pointer;
import playn.scene.*;

public class ImageScalingTest extends Test {

  private static final Texture.Config MIPMAPPED = new Texture.Config(
    true, false, false, GL20.GL_LINEAR, GL20.GL_LINEAR, true);

  private boolean paused = false;

  public ImageScalingTest (TestsGame game) {
    super(game, "ImageScaling",
          "Tests use of min/mag filters and mipmapping when scaling images.");
  }

  @Override public void init () {
    final Image princess = game.assets.getImage("images/princess.png");
    final Image star     = game.assets.getImage("images/star.png");

    RFuture.collect(Arrays.asList(princess.state, star.state)).onSuccess(new UnitSlot() {
      public void onEmit () {
        // the second princess and (64x64) star images are mipmapped
        float phwidth = princess.width()/2f, phheight = princess.height()/2f;
        final ImageLayer player1 = new ImageLayer(princess);
        player1.setOrigin(phwidth, phheight);
        game.rootLayer.addAt(player1, 100, 100);
        final ImageLayer player2 = new ImageLayer(princess.createTexture(MIPMAPPED));
        player2.setOrigin(phwidth, phheight);
        game.rootLayer.addAt(player2, 250, 100);

        float shwidth = star.width()/2, shheight = star.height()/2;
        final ImageLayer slayer1 = new ImageLayer(star);
        slayer1.setOrigin(shwidth, shheight);
        game.rootLayer.addAt(slayer1, 100, 250);
        final ImageLayer slayer2 = new ImageLayer(star.createTexture(MIPMAPPED));
        slayer2.setOrigin(shwidth, shheight);
        game.rootLayer.addAt(slayer2, 250, 250);

        conns.add(game.pointer.events.connect(new Slot<Pointer.Event>() {
          public void onEmit (Pointer.Event event) {
            switch (event.kind) {
              case START: paused = true; break;
              case END:
              case CANCEL: paused = false; break;
            }
          }
        }));

        conns.add(game.paint.connect(new Slot<Clock>() {
          private float elapsed;
          public void onEmit (Clock clock) {
            if (!paused) {
              elapsed += clock.dt/1000f;
              float scale = Math.abs(FloatMath.sin(elapsed));
              player1.setScale(scale);
              player2.setScale(scale);
              slayer1.setScale(scale);
              slayer2.setScale(scale);
            }
          }
        }));
      }
    });
  }
}
