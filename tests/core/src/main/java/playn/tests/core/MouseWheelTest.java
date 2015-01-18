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

import playn.core.*;
import playn.scene.*;
import playn.scene.Mouse;

public class MouseWheelTest extends Test
{
  private static final float HEIGHT = 300;
  private static final float WIDTH = 30;
  private static final float HWIDTH = WIDTH / 2;

  public MouseWheelTest (TestsGame game) {
    super(game, "MouseWheel", "Tests mouse wheel movement on layers");
  }

  @Override public void init () {
    Canvas bgcanvas = game.graphics.createCanvas(WIDTH + 10, HEIGHT);
    bgcanvas.setFillColor(0xff808080);
    bgcanvas.fillRect(0, 0, WIDTH + 10, HEIGHT);
    ImageLayer bg = new ImageLayer(bgcanvas.toTexture());

    Canvas knob = game.graphics.createCanvas(WIDTH, HWIDTH);
    knob.setFillColor(0xffffffff).fillRect(0, 0, WIDTH, HWIDTH);
    knob.setStrokeColor(0xff000000).drawLine(0, HWIDTH / 2, WIDTH, HWIDTH / 2);
    knob.setStrokeColor(0xffff0000).strokeRect(0, 0, WIDTH - 1, HWIDTH - 1);

    final ImageLayer il = new ImageLayer(knob.toTexture());
    il.setOrigin(0, HWIDTH / 2).setDepth(1).setTranslation(0, HEIGHT / 2);

    GroupLayer slider = new GroupLayer();
    slider.add(bg);
    slider.add(il);
    game.rootLayer.addAt(slider, 25, 25);

    bg.events().connect(new Mouse.Listener() {
      @Override public void onWheel (Mouse.WheelEvent event, Mouse.Interaction iact) {
        float y = il.ty() + event.velocity;
        y = Math.max(0, Math.min(y, HEIGHT));
        il.setTranslation(0, y);
      }
    });
  }
}
