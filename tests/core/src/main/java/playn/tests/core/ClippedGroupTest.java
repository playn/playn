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

import playn.core.CanvasImage;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.Layer;
import playn.core.Surface;
import playn.core.SurfaceImage;
import static playn.core.PlayN.graphics;

public class ClippedGroupTest extends Test {

  private float elapsed;
  private GroupLayer.Clipped g1, g2, g3, g4, g5;
  private ImageLayer i1;
  private GroupLayer inner, g5Inner;
  private ImageLayer s1;

  @Override
  public String getName() {
    return "ClippedGroupTest";
  }

  @Override
  public String getDescription() {
    return "Tests clipping of children in group layers.";
  }

  @Override
  public void init() {
    GroupLayer rootLayer = graphics().rootLayer();

    final CanvasImage img = graphics().createImage(100, 50);
    img.canvas().setFillGradient(graphics().createLinearGradient(
                                   0, 0, 100, 100, new int[] { 0xFF0000FF, 0xFF00FF00 },
                                   new float[] { 0, 1 }));
    img.canvas().fillRoundRect(0, 0, 100, 50, 10);

    // create an immediate layer that draws the boundaries of our clipped group layers
    rootLayer.add(graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
      public void render(Surface surf) {
        // draw the border of our various clipped groups
        surf.setFillColor(0xFF000000);
        outline(surf, g1);
        outline(surf, g2);
        outline(surf, g3);
        outline(surf, g4);
        outline(surf, g5);
      }
      protected void outline (Surface surf, Layer.HasSize ly) {
        drawRect(surf, ly.tx() - ly.originX(), ly.ty() - ly.originY(), ly.width(), ly.height());
      }
      protected void drawRect(Surface surf, float x, float y, float w, float h) {
        float left = x-1, top = y-1, right = x+w+2, bot = y+h+2;
        surf.drawLine(left, top, right, top, 1);
        surf.drawLine(right, top, right, bot, 1);
        surf.drawLine(left, top, left, bot, 1);
        surf.drawLine(left, bot, right, bot, 1);
      }
    }));

    // create a group layer with a static clip, and a rotating image inside
    g1 = graphics().createGroupLayer(100, 100);
    // test the origin not being at zero/zero
    g1.setOrigin(50, 0);
    i1 = graphics().createImageLayer(img);
    i1.setOrigin(i1.width()/2, i1.height()/2);
    g1.addAt(i1, 50, 50);
    rootLayer.addAt(g1, 75, 25);

    // static image inside and animated clipped width
    g2 = graphics().createGroupLayer(100, 100);
    g2.setOrigin(50, 50);
    g2.addAt(graphics().createImageLayer(img), (100 - img.width())/2, (100 - img.height())/2);
    rootLayer.addAt(g2, 200, 75);

    // nest a group layer inside with an animated origin
    inner = graphics().createGroupLayer();
    inner.addAt(graphics().createImageLayer(img), (100 - img.width())/2, (100 - img.height())/2);
    g3 = graphics().createGroupLayer(100, 100);
    g3.add(inner);
    rootLayer.addAt(g3, 275, 25);

    // create a group layer with a static clip, and a rotating surface image inside
    g4 = graphics().createGroupLayer(100, 100);
    SurfaceImage si = graphics().createSurface(100, 50);
    si.surface().setFillColor(0xFF99CCFF).fillRect(0, 0, 100, 50);
    s1 = graphics().createImageLayer(si);
    s1.setOrigin(s1.width()/2, s1.height()/2);
    g4.addAt(s1, 50, 50);
    rootLayer.addAt(g4, 400, 25);

    // put a large clipped group inside a small one
    g5Inner = graphics().createGroupLayer(150, 150);
    g5Inner.addAt(graphics().createImageLayer(img).setScale(2), -img.width(), -img.height());
    g5Inner.addAt(graphics().createImageLayer(img).setScale(2), -img.width(), img.height());
    g5Inner.addAt(graphics().createImageLayer(img).setScale(2), img.width(), -img.height());
    g5Inner.addAt(graphics().createImageLayer(img).setScale(2), img.width(), img.height());
    g5 = graphics().createGroupLayer(100, 100);
    g5.addAt(g5Inner, -25, -25);
    rootLayer.addAt(g5, 525, 25);
  }

  @Override
  public void update(int delta) {
    elapsed += delta/1000f;
    i1.setRotation(elapsed * FloatMath.PI/2);
    s1.setRotation(elapsed * FloatMath.PI/2);
    g2.setWidth(Math.round(Math.abs(100 * FloatMath.sin(elapsed))));
    inner.setOrigin(FloatMath.sin(elapsed * 2f) * 50, FloatMath.cos(elapsed * 2f) * 50);
    float cycle = elapsed / (FloatMath.PI * 2);
    if (FloatMath.ifloor(cycle) % 2 == 0) {
      // go in a circle without going out of bounds
      g5Inner.setTranslation(-25 + 50 * FloatMath.cos(elapsed), -25 + 50 * FloatMath.sin(elapsed));
    } else {
      // go out of bounds on right and left
      g5Inner.setTranslation(25 + 250 * FloatMath.cos(elapsed + FloatMath.PI/2), -25);
    }
  }
}
