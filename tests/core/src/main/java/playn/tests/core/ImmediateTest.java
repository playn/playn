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
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.ImmediateLayer;
import playn.core.Surface;
import static playn.core.PlayN.*;

public class ImmediateTest extends Test {
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

    final CanvasImage image = graphics().createImage(100, 100);
    Canvas canvas = image.canvas();
    canvas.setFillColor(0xFFCC99FF);
    canvas.fillCircle(50, 50, 50);

    ImmediateLayer unclipped = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFFFFCC99);
        surf.fillRect(0, 0, graphics().width(), graphics().height());

        // fill a rect that will be covered except for one pixel by the clipped immediate layer
        surf.setFillColor(0xFF000000);
        surf.fillRect(99, 99, 202, 202);
      }
    });
    rootLayer.add(unclipped);

    ImmediateLayer clipped = graphics().createImmediateLayer(200, 200, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFF99CCFF);
        // this fill should be clipped to our bounds
        surf.fillRect(-50, -50, 300, 300);
        surf.drawImage(image, 125, -25);
      }
    });
    clipped.setTranslation(100, 100);
    rootLayer.add(clipped);
  }
}
