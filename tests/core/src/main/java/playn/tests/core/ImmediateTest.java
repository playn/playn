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

import playn.core.GroupLayer;
import playn.core.CanvasImage;
import playn.core.ImmediateLayer;
import playn.core.Surface;
import static playn.core.PlayN.*;

import pythagoras.f.FloatMath;

public class ImmediateTest extends Test {

  private float elapsed, rotation;

  @Override
  public String getName() {
    return "ImmediateTest";
  }

  @Override
  public String getDescription() {
    return "Tests rendering of immediate layers with and without clipping. Clipped blue layer " +
      "should not overdraw one pixel black line that circumscribes it.";
  }

  @Override
  public void init() {
    GroupLayer rootLayer = graphics().rootLayer();

    final CanvasImage circle = graphics().createImage(100, 100);
    circle.canvas().setFillColor(0xFFCC99FF);
    circle.canvas().fillCircle(50, 50, 50);

    final CanvasImage sausage = graphics().createImage(100, 50);
    sausage.canvas().setFillGradient(graphics().createLinearGradient(
                                       0, 0, 100, 100, new int[] { 0xFF0000FF, 0xFF00FF00 },
                                       new float[] { 0, 1 }));
    sausage.canvas().fillRoundRect(0, 0, 100, 50, 10);

    // add an unclipped layer which will draw our background and outlines
    rootLayer.add(graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFFFFCC99);
        surf.fillRect(0, 0, graphics().width(), graphics().height());

        // fill a rect that will be covered except for one pixel by the clipped immediate layers
        surf.setFillColor(0xFF000000);
        surf.fillRect(29, 29, 202, 202);
        surf.fillRect(259, 29, 102, 102);
        surf.fillRect(259, 159, 102, 102);
      }
    }));

    // add a clipped layer that will clip a fill and image draw
    ImmediateLayer ilayer = graphics().createImmediateLayer(200, 200, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        // this fill should be clipped to our bounds
        surf.setFillColor(0xFF99CCFF);
        surf.fillRect(-50, -50, 300, 300);
        // and this image should be clipped to our bounds
        surf.drawImage(circle, 125, -25);
      }
    });
    // adjust the origin to ensure that is accounted for in the clipping
    ilayer.setOrigin(100, 100);
    rootLayer.addAt(ilayer, 130, 130);

    // add a clipped layer that draws an image through a rotation transform
    rootLayer.addAt(graphics().createImmediateLayer(100, 100, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFF99CCFF);
        surf.fillRect(0, 0, 100, 100);
        surf.translate(50, 50);
        surf.rotate(rotation);
        surf.translate(-50, -50);
        surf.drawImage(sausage, 0, 25);
      }
    }), 260, 30);

    // add a clipped layer that draws an image through a translation transform
    rootLayer.addAt(graphics().createImmediateLayer(100, 100, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFF99CCFF);
        surf.fillRect(0, 0, 100, 100);
        surf.translate(FloatMath.sin(elapsed) * 50, FloatMath.cos(elapsed) * 50 + 25);
        surf.drawImage(sausage, 0, 0);
      }
    }), 260, 160);
  }

  @Override
  public void update(int delta) {
    elapsed += delta/1000f;
    rotation = elapsed * FloatMath.PI/2;
  }
}
