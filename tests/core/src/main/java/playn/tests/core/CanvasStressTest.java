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

import playn.core.Canvas;
import playn.core.CanvasImage;
import static playn.core.PlayN.*;

public class CanvasStressTest extends Test {

  private Canvas canvas;
  private int noSegs = 30;
  private int direction = 1;

  @Override
  public String getName() {
    return "Canvas Stress Test";
  }

  @Override
  public String getDescription() {
    return "Animates a full-screen sized canvas, forcing a massive reupload of image data to " +
      "the GPU on every frame.";
  }

  @Override
  public void init() {
    CanvasImage canvasImage = graphics().createImage(graphics().width(), graphics().height());
    canvas = canvasImage.canvas();
    graphics().rootLayer().add(graphics().createImageLayer(canvasImage));
  }

  @Override
  public void update(int delta) {
    canvas.clear();
    canvas.setStrokeWidth(3);
    canvas.setStrokeColor(0x88ff0000);

    noSegs += direction;
    if (noSegs > 50) direction = -1;
    if (noSegs < 20) direction = 1;

    final float r = 100;
    for (int ii = 0; ii < noSegs; ii++) {
      float angle = 2*FloatMath.PI * ii / noSegs;
      float x = (r * FloatMath.cos(angle)) + graphics().width() / 2;
      float y = (r * FloatMath.sin(angle)) + graphics().height() /2;
      canvas.strokeCircle(x, y, 100);
    }
  }
}
