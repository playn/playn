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

public class ClippedLayerTest extends Test {

  private float elapsed, rotation;

  public ClippedLayerTest (TestsGame game) {
    super(game, "ClippedLayer",
          "Tests rendering of layers with and without clipping. Clipped layers " +
          "should not overdraw one pixel black lines that circumscribes them.");
  }

  @Override public void init() {
    addClippedLayers();
    addClippedGroupLayers();
  }

  protected void addClippedLayers () {
    Canvas circle = game.graphics.createCanvas(100, 100);
    circle.setFillColor(0xFFCC99FF).fillCircle(50, 50, 50);
    final Texture cirtex = circle.toTexture();

    Canvas sausage = game.graphics.createCanvas(100, 50);
    Gradient linear = sausage.createGradient(new Gradient.Linear(
      0, 0, 100, 100, new int[] { 0xFF0000FF, 0xFF00FF00 }, new float[] { 0, 1 }));
    sausage.setFillGradient(linear).fillRoundRect(0, 0, 100, 50, 10);
    final Texture saustex = sausage.toTexture();

    // add an unclipped layer which will draw our background and outlines
    game.rootLayer.add(new Layer() {
      @Override protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFFFFCC99).fillRect(
          0, 0, game.graphics.viewSize.width(), game.graphics.viewSize.height());

        // fill a rect that will be covered except for one pixel by the clipped immediate layers
        surf.setFillColor(0xFF000000);
        surf.fillRect(29, 29, 152, 152);
        surf.fillRect(259, 29, 102, 102);
        surf.fillRect(389, 29, 102, 102);
      }
    });

    // add a clipped layer that will clip a fill and image draw
    ClippedLayer clayer = new ClippedLayer(150, 150) {
      protected void paintClipped (Surface surf) {
        // this fill should be clipped to our bounds
        surf.setFillColor(0xFF99CCFF);
        surf.fillRect(-50, -50, 200, 200);
        // and this image should be clipped to our bounds
        surf.draw(cirtex, 80, -25);
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
    }, 390, 30);

    conns.add(game.paint.connect(new Slot<Clock>() {
      public void onEmit (Clock clock) {
        elapsed = clock.tick/1000f;
        rotation = elapsed * FloatMath.PI/2;
      }
    }));
  }

  protected void addClippedGroupLayers () {
    final float iwidth = 100, iheight = 50;
    final Canvas img = game.graphics.createCanvas(iwidth, iheight);
    Gradient linear = img.createGradient(new Gradient.Linear(
      0, 0, 100, 100, new int[] { 0xFF0000FF, 0xFF00FF00 }, new float[] { 0, 1 }));
    img.setFillGradient(linear).fillRoundRect(0, 0, 100, 50, 10);
    final Texture tex = img.toTexture();

    // create a group layer with a static clip, and a rotating image inside
    final GroupLayer g1 = new GroupLayer(100, 100);
    // test the origin not being at zero/zero
    g1.setOrigin(50, 0);
    final ImageLayer i1 = new ImageLayer(tex);
    i1.setOrigin(i1.width()/2, i1.height()/2);
    g1.addAt(i1, 50, 50);

    // static image inside and animated clipped width
    final GroupLayer g2 = new GroupLayer(100, 100);
    g2.setOrigin(50, 50);
    g2.addAt(new ImageLayer(tex), (100 - iwidth)/2, (100 - iheight)/2);

    // nest a group layer inside with an animated origin
    final GroupLayer inner = new GroupLayer();
    inner.addAt(new ImageLayer(tex), (100 - iwidth)/2, (100 - iheight)/2);
    final GroupLayer g3 = new GroupLayer(100, 100);
    g3.add(inner);

    // create a group layer with a static clip, and a rotating surface image inside
    final GroupLayer g4 = new GroupLayer(100, 100);
    TextureSurface si = game.createSurface(100, 50);
    si.begin().setFillColor(0xFF99CCFF).fillRect(0, 0, 100, 50).end().close();
    final ImageLayer s1 = new ImageLayer(si.texture);
    s1.setOrigin(s1.width()/2, s1.height()/2);
    g4.addAt(s1, 50, 50);

    // put a large clipped group inside a small one
    final GroupLayer g5Inner = new GroupLayer(150, 150);
    g5Inner.addAt(new ImageLayer(tex).setScale(2), -iwidth, -iheight);
    g5Inner.addAt(new ImageLayer(tex).setScale(2), -iwidth, iheight);
    g5Inner.addAt(new ImageLayer(tex).setScale(2), iwidth, -iheight);
    g5Inner.addAt(new ImageLayer(tex).setScale(2), iwidth, iheight);
    final GroupLayer g5 = new GroupLayer(100, 100);
    g5.addAt(g5Inner, -25, -25);

    // create a layer that draws the boundaries of our clipped group layers
    game.rootLayer.add(new Layer() {
      @Override protected void paintImpl (Surface surf) {
        // draw the border of our various clipped groups
        surf.setFillColor(0xFF000000);
        outline(surf, g1);
        outline(surf, g2);
        outline(surf, g3);
        outline(surf, g4);
        outline(surf, g5);
      }
      protected void outline (Surface surf, GroupLayer gl) {
        drawRect(surf, gl.tx() - gl.originX(), gl.ty() - gl.originY(), gl.width(), gl.height());
      }
      protected void drawRect(Surface surf, float x, float y, float w, float h) {
        float left = x-1, top = y-1, right = x+w+2, bot = y+h+2;
        surf.drawLine(left, top, right, top, 1);
        surf.drawLine(right, top, right, bot, 1);
        surf.drawLine(left, top, left, bot, 1);
        surf.drawLine(left, bot, right, bot, 1);
      }
    });
    game.rootLayer.addAt(g1, 75, 225);
    game.rootLayer.addAt(g2, 200, 275);
    game.rootLayer.addAt(g3, 275, 225);
    game.rootLayer.addAt(g4, 400, 225);
    game.rootLayer.addAt(g5, 525, 225);

    conns.add(game.paint.connect(new Slot<Clock>() {
      public void onEmit (Clock clock) {
        float elapsed = clock.tick/1000f;
        i1.setRotation(elapsed * FloatMath.PI/2);
        s1.setRotation(elapsed * FloatMath.PI/2);
        g2.setWidth(Math.round(Math.abs(100 * FloatMath.sin(elapsed))));
        inner.setOrigin(FloatMath.sin(elapsed * 2f) * 50, FloatMath.cos(elapsed * 2f) * 50);
        float cycle = elapsed / (FloatMath.PI * 2);
        if (FloatMath.ifloor(cycle) % 2 == 0) {
          // go in a circle without going out of bounds
          g5Inner.setTranslation(-25 + 50 * FloatMath.cos(elapsed),
                                 -25 + 50 * FloatMath.sin(elapsed));
        } else {
          // go out of bounds on right and left
          g5Inner.setTranslation(25 + 250 * FloatMath.cos(elapsed + FloatMath.PI/2), -25);
        }
      }
    }));
  }
}
