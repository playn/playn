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

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.Pointer;
import static playn.core.PlayN.*;

public class ImageScalingTest extends Test {

  private int elapsed;
  private boolean pointerPressed;
  private ImageLayer player1, player2;
  private ImageLayer slayer1, slayer2;

  @Override
  public String getName() {
    return "ImageScalingTest";
  }

  @Override
  public String getDescription() {
    return "Tests use of min/mag filters and mipmapping when scaling images.";
  }

  @Override
  public void init() {
    Image princess1 = assets().getImage("images/princess.png");
    Image princess2 = assets().getImage("images/princess.png");
    Image star1     = assets().getImage("images/star.png");
    Image star2     = assets().getImage("images/star.png");

    // configure the second princess and (64x64) star images as mipmapped
    princess2.setMipmapped(true);
    star2.setMipmapped(true);

    float phwidth = 101/2f, phheight = 171/2f; // avoid having to wait for images
    player1 = graphics().createImageLayer(princess1);
    player1.setOrigin(phwidth, phheight);
    graphics().rootLayer().addAt(player1, 100, 100);
    player2 = graphics().createImageLayer(princess2);
    player2.setOrigin(phwidth, phheight);
    graphics().rootLayer().addAt(player2, 250, 100);

    float shwidth = 64/2, shheight = 64/2; // avoid having to wait for images
    slayer1 = graphics().createImageLayer(star1);
    slayer1.setOrigin(shwidth, shheight);
    graphics().rootLayer().addAt(slayer1, 100, 250);
    slayer2 = graphics().createImageLayer(star2);
    slayer2.setOrigin(shwidth, shheight);
    graphics().rootLayer().addAt(slayer2, 250, 250);

    pointer().setListener(new Pointer.Adapter() {
      @Override public void onPointerStart(Pointer.Event event) { pointerPressed = true; }
      @Override public void onPointerEnd(Pointer.Event event) { pointerPressed = false; }
      @Override public void onPointerCancel(Pointer.Event event) { pointerPressed = false; }
    });
  }

  @Override
  public void dispose() {
    pointer().setListener(null);
  }

  @Override
  public void update(int delta) {
    super.update(delta);
    if (!pointerPressed) elapsed += delta;
  }

  @Override
  public void paint(float alpha) {
    super.paint(alpha);

    float now = elapsed + alpha*UPDATE_RATE;
    float scale = Math.abs(FloatMath.sin(now/1000));
    player1.setScale(scale);
    player2.setScale(scale);
    slayer1.setScale(scale);
    slayer2.setScale(scale);
  }
}
