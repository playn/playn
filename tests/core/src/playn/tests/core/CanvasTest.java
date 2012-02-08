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

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.ImageLayer;
import playn.core.Image;
import static playn.core.PlayN.*;

public class CanvasTest extends Test {

  private final static float GAP = 10;
  private float nextX, nextY, maxY;

  @Override
  public String getName() {
    return "CanvasTest";
  }

  @Override
  public String getDescription() {
    return "Tests various Canvas rendering features.";
  }

  @Override
  public void init() {
    nextX = nextY = GAP;

    addTestCanvas(100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        canvas.setFillGradient(graphics().createRadialGradient(
                                 0, 0, 50, new int[] { 0xFFFF0000, 0xFF00FF00 },
                                 new float[] { 0, 1 }));
        canvas.fillRect(0, 0, 100, 100);
      }
    });

    addTestCanvas(100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        canvas.setFillGradient(graphics().createLinearGradient(
                                 0, 0, 100, 100, new int[] { 0xFF0000FF, 0xFF00FF00 },
                                 new float[] { 0, 1 }));
        canvas.fillRect(0, 0, 100, 100);
      }
    });

    addTestCanvas(100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        canvas.setFillGradient(graphics().createLinearGradient(
                                 0, 0, 100, 100, new int[] { 0xFF0000FF, 0xFF00FF00 },
                                 new float[] { 0, 1 }));
        canvas.setFillPattern(graphics().createPattern(assets().getImage("images/tile.png")));
        canvas.fillRect(0, 0, 100, 100);
      }
    });

    addTestCanvas(100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        canvas.setFillColor(0xFF99CCFF);
        canvas.fillRect(0, 0, 100, 100);
        // draw a point and some lines
        canvas.setStrokeColor(0xFFFF0000);
        canvas.drawPoint(50, 50);
        canvas.drawLine(0, 25, 100, 25);
        canvas.drawLine(0, 75, 100, 75);
        canvas.drawLine(25, 0, 25, 100);
        canvas.drawLine(75, 0, 75, 100);
        // stroke and fill a circle
        canvas.strokeCircle(25, 75, 10);
        canvas.setFillColor(0xFF0000FF);
        canvas.fillCircle(75, 75, 10);
      }
    });

    addTestCanvas(100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        canvas.setFillColor(0xFF99CCFF);
        canvas.fillRect(0, 0, 100, 100);

        // draw an image normally, scaled, cropped, cropped and scaled, etc.
        Image pea = assets().getImage("images/pea.png");
        float half = 37/2f;
        canvas.drawImage(pea, 10, 10);
        canvas.drawImage(pea, 55, 10, 37, 37, half, half, half, half);
        canvas.drawImage(pea, 10, 55, 37, 37, half, 0, half, half);
        canvas.drawImage(pea, 55, 55, 37, 37, half, half/2, half, half);
      }
    });
  }

  private void addTestCanvas(int width, int height, Drawer drawer) {
    // if this canvas won't fit in this row, wrap down to the next
    if (nextX + width > graphics().width()) {
      nextY += (maxY + GAP);
      nextX = GAP;
      maxY = 0;
    }
    // create the canvas, render it and add it to the scene graph
    CanvasImage image = graphics().createImage(width, height);
    drawer.draw(image.canvas());
    ImageLayer layer = graphics().createImageLayer(image);
    layer.setTranslation(nextX, nextY);
    graphics().rootLayer().add(layer);
    // update our positioning info
    nextX += (width + GAP);
    maxY = Math.max(maxY, height);
  }

  private interface Drawer {
    void draw(Canvas canvas);
  }
}
