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
import static playn.tests.core.TestsGame.game;

public class CanvasTest extends Test {

  private final static float GAP = 10;
  private float nextX, nextY, maxY;

  private CanvasLayer time;
  private int lastSecs;

  public CanvasTest (TestsGame game) {
    super(game, "Canvas", "Tests various Canvas rendering features.");
  }

  @Override public void init() {
    nextX = nextY = GAP;
    lastSecs = -1;

    addTestCanvas("radial fill gradient", 100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        Gradient.Config cfg = new Gradient.Radial(
          0, 0, 50, new int[] { 0xFFFF0000, 0xFF00FF00 }, new float[] { 0, 1 });
        canvas.setFillGradient(canvas.createGradient(cfg));
        canvas.fillRect(0, 0, 100, 100);
      }
    });

    addTestCanvas("linear fill gradient", 100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        Gradient.Config cfg = new Gradient.Linear(
          0, 0, 100, 100, new int[] { 0xFF0000FF, 0xFF00FF00 }, new float[] { 0, 1 });
        canvas.setFillGradient(canvas.createGradient(cfg));
        canvas.fillRect(0, 0, 100, 100);
      }
    });

    addTestCanvas("image fill pattern", 100, 100, "images/tile.png", new ImageDrawer() {
      public void draw(Canvas canvas, Image tile) {
        canvas.setFillPattern(tile.createPattern(true, true));
        canvas.fillRect(0, 0, 100, 100);
      }
    });

    addTestCanvas("lines and circles", 100, 100, new Drawer() {
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

    addTestCanvas("image, subimage", 100, 100, "images/orange.png", new ImageDrawer() {
      public void draw(Canvas canvas, Image orange) {
        canvas.setFillColor(0xFF99CCFF);
        canvas.fillRect(0, 0, 100, 100);

        // draw an image normally, scaled, cropped, cropped and scaled, etc.
        float half = 37/2f;
        canvas.draw(orange, 10, 10);
        canvas.draw(orange, 55, 10, 37, 37, half, half, half, half);
        canvas.draw(orange, 10, 55, 37, 37, half, 0, half, half);
        canvas.draw(orange, 55, 55, 37, 37, half, half/2, half, half);
      }
    });

    Canvas repcan = createCanvas(30, 30, new Drawer() {
      public void draw (Canvas canvas) {
        canvas.setFillColor(0xFF99CCFF).fillCircle(15, 15, 15);
        canvas.setStrokeColor(0xFF000000).strokeRect(0, 0, 30, 30);
      }
    });
    Texture.Config repeat = Texture.Config.DEFAULT.repeat(true, true);
    Texture reptex = repcan.toTexture(repeat);
    addTestLayer("ImageLayer repeat x/y", 100, 100, new ImageLayer(reptex).setSize(100, 100));

    time = new CanvasLayer(game.graphics, 100, 100);
    addTestLayer("updated canvas", 100, 100, time);

    final Gradient linear = repcan.createGradient(new Gradient.Linear(
      0, 0, 100, 100, new int[] { 0xFF0000FF, 0xFF00FF00 }, new float[] { 0, 1 }));
    final float dotRadius = 40;
    final Gradient radial = repcan.createGradient(new Gradient.Radial(
      100/3f, 100/2.5f, dotRadius, new int[] { 0xFFFFFFFF, 0xFFCC66FF }, new float[] { 0, 1 }));

    addTestCanvas("filled bezier path", 100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        // draw a rounded rect with bezier curves
        Path path = canvas.createPath();
        path.moveTo(10, 0);
        path.lineTo(90, 0);
        path.bezierTo(95, 0, 100, 5, 100, 10);
        path.lineTo(100, 90);
        path.bezierTo(100, 95, 95, 100, 90, 100);
        path.lineTo(10, 100);
        path.bezierTo(5, 100, 0, 95, 0, 90);
        path.lineTo(0, 10);
        path.bezierTo(0, 5, 5, 0, 10, 0);
        path.close();
        canvas.setFillGradient(linear).fillPath(path);
      }
    });

    addTestCanvas("gradient round rect", 100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        // draw a rounded rect directly
        canvas.setFillGradient(linear).fillRoundRect(0, 0, 100, 100, 10);
      }
    });

    addTestCanvas("gradient filled text", 100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        // draw a rounded rect directly
        canvas.setFillGradient(linear);
        TextLayout capF = game.graphics.layoutText("F", new TextFormat(F_FONT.derive(96)));
        canvas.fillText(capF, 15, 5);
      }
    });

    addTestCanvas("nested round rect", 100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        // demonstrates a bug (now worked around) in Android round-rect drawing
        canvas.setFillColor(0xFFFFCC99).fillRoundRect(0, 0, 98.32f, 29.5f, 12f);
        canvas.setFillColor(0xFF99CCFF).fillRoundRect(3, 3, 92.32f, 23.5f, 9.5f);
      }
    });

    addTestCanvas("android fill/stroke bug", 100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        canvas.save();
        canvas.setFillGradient(radial).fillCircle(50, 50, dotRadius);
        canvas.restore();
        canvas.setStrokeColor(0xFF000000).setStrokeWidth(1.5f).strokeCircle(50, 50, dotRadius);
      }
    });

    addTestCanvas("transform test", 100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        canvas.setFillColor(0xFFCCCCCC).fillRect(0, 0, 50, 50);
        canvas.setFillColor(0xFFCCCCCC).fillRect(50, 50, 50, 50);
        TextLayout capF = game.graphics.layoutText("F", new TextFormat(F_FONT));
        float theta = -FloatMath.PI/4, tsin = FloatMath.sin(theta), tcos = FloatMath.cos(theta);
        canvas.setFillColor(0xFF000000).fillText(capF, 0, 0);
        canvas.transform(tcos, -tsin, tsin, tcos, 50, 50);
        canvas.setFillColor(0xFF000000).fillText(capF, 0, 0);
      }
    });

    addTestCanvas("round rect precision", 100, 100, new Drawer() {
      float bwid = 4;
      void outer (Canvas canvas, float y) {
        canvas.setFillColor(0xFF000000);
        canvas.fillRect(2, y, 94, 30);
      }
      void inner (Canvas canvas, float y) {
        canvas.setFillColor(0xFF555555);
        canvas.fillRect(2 + bwid, y + bwid, 94 - bwid * 2, 30 - bwid * 2);
      }
      void stroke (Canvas canvas, float y) {
        canvas.setStrokeColor(0xFF99CCFF);
        canvas.setStrokeWidth(bwid);
        canvas.strokeRoundRect(2 + bwid / 2, y + bwid / 2, 94 - bwid, 30 - bwid, 10);
      }
      public void draw(Canvas canvas) {
        float y = 1;
        outer(canvas, y);
        inner(canvas, y);
        stroke(canvas, y);

        y += 34;
        outer(canvas, y);
        stroke(canvas, y);
        inner(canvas, y);

        y += 34;
        stroke(canvas, y);
        outer(canvas, y);
        inner(canvas, y);
      }
    });

    final ImageLayer tileLayer = new ImageLayer(
      game.assets.getImage("images/tile.png").setConfig(repeat));
    addTestLayer("img layer anim setWidth", 100, 100, tileLayer.setSize(0, 100));

    conns.add(game.paint.connect(new Slot<Clock>() {
      public void onEmit (Clock clock) {
        int curSecs = clock.tick/1000;
        if (curSecs != lastSecs) {
          Canvas tcanvas = time.begin();
          tcanvas.clear();
          tcanvas.setStrokeColor(0xFF000000).strokeRect(0, 0, 99, 99);
          tcanvas.drawText(""+curSecs, 40, 55);
          lastSecs = curSecs;
          time.end();
        }

        // round the width so that it goes to zero sometimes (which should be fine)
        if (tileLayer != null) tileLayer.forceWidth = Math.round(
          Math.abs(FloatMath.sin(clock.tick/2000f)) * 100);
      }
    }));

    final Canvas cancan = createCanvas(50, 50, new Drawer() {
      public void draw (Canvas canvas) {
        canvas.setFillGradient(radial).fillRect(0, 0, canvas.width, canvas.height);
      }
    });
    addTestCanvas("canvas drawn on canvas", 100, 100, new Drawer() {
      public void draw(Canvas canvas) {
        canvas.translate(50, 25);
        canvas.rotate(FloatMath.PI/4);
        canvas.draw(cancan.image, 0, 0);
      }
    });
  }

  private interface Drawer {
    void draw(Canvas canvas);
  }

  private void addTestCanvas(String descrip, int width, int height, Drawer drawer) {
    Canvas canvas = createCanvas(width, height, drawer);
    addTestLayer(descrip, width, height, new ImageLayer(canvas.toTexture()));
  }

  private Canvas createCanvas(int width, int height, final Drawer drawer) {
    final Canvas canvas = game.graphics.createCanvas(width, height);
    drawer.draw(canvas);
    return canvas;
  }

  private void addTestLayer(String descrip, int width, int height, Layer layer) {
    // if this layer won't fit in this row, wrap down to the next
    if (nextX + width > game.graphics.viewSize.width()) {
      nextY += (maxY + GAP);
      nextX = GAP;
      maxY = 0;
    }

    // add the layer and its description below
    game.rootLayer.addAt(layer, nextX, nextY);
    ImageLayer dlayer = createDescripLayer(descrip, width);
    game.rootLayer.addAt(dlayer, nextX + Math.round((width-dlayer.width())/2),
                         nextY + height + 2);

    // update our positioning info
    nextX += (width + GAP);
    maxY = Math.max(maxY, height+dlayer.height()+2);
  }

  private interface ImageDrawer {
    void draw(Canvas canvas, Image image);
  }

  private void addTestCanvas(String descrip, int width, int height, String imagePath,
                             final ImageDrawer drawer) {
    final Canvas target = game.graphics.createCanvas(width, height);
    final ImageLayer layer = new ImageLayer().setSize(width, height);
    game.assets.getImage(imagePath).state.onSuccess(new Slot<Image>() {
      public void onEmit (Image image) {
        drawer.draw(target, image);
        layer.setTile(target.toTexture());
      }
    });
    addTestLayer(descrip, width, height, layer);
  }

  private Font F_FONT = new Font("Helvetica", Font.Style.BOLD, 48);
}
