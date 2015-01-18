/**
 * Copyright 2011 The PlayN Authors
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
import react.Slot;

public class AlphaLayerTest extends Test {

  static float width = 100, height = 100;
  static int offset = 5;

  public AlphaLayerTest (TestsGame game) {
    super(game, "AlphaLayer",
          "Test that alpha works the same on all layer types and that alpha is 'additive'.");
  }

  @Override public void init() {
    final GroupLayer rootLayer = game.rootLayer;
    final float fullWidth = 6*width, fullHeight = 3*height;

    // add a half white, half blue background
    TextureSurface bg = game.createSurface(fullWidth, fullHeight);
    bg.begin().
      setFillColor(Color.rgb(255, 255, 255)).fillRect(0, 0, fullWidth, fullHeight).
      setFillColor(Color.rgb(0, 0, 255)).fillRect(0, 2*height, fullWidth, height).
      end().close();
    rootLayer.add(new ImageLayer(bg.texture));

    addDescrip("all layers contained in group layer with a=0.5\n" +
               "thus, fully composited a=0.25", offset, fullHeight+5, fullWidth);

    // add a 50% transparent group layer
    final GroupLayer groupLayer = new GroupLayer();
    groupLayer.setAlpha(0.5f);
    rootLayer.add(groupLayer);

    game.assets.getImage("images/alphalayertest.png").state.onSuccess(new Slot<Image>() {
      public void onEmit(Image image) {
        Texture imtex = image.texture();
        float x = offset, y0 = offset, y1 = offset+height, y2 = offset+2*height;

        // add the layers over a white background, then again over blue
        groupLayer.addAt(new ImageLayer(imtex).setAlpha(0.5f), x, y0);
        addDescrip("image\nimg layer a=0.5", x, y1, width);
        groupLayer.addAt(new ImageLayer(imtex).setAlpha(0.5f), x, y2);
        x += width;

        TextureSurface surf1 = game.createSurface(image.width(), image.height());
        surf1.begin().clear().setAlpha(0.5f).draw(imtex, 0, 0).end().close();
        groupLayer.addAt(new ImageLayer(surf1.texture), x, y0);
        addDescrip("surface a=0.5\nimg layer a=1", x, y1, width);
        groupLayer.addAt(new ImageLayer(surf1.texture), x, y2);
        x += width;

        TextureSurface surf2 = game.createSurface(image.width(), image.height());
        surf2.begin().clear().draw(imtex, 0, 0).end().close();
        groupLayer.addAt(new ImageLayer(surf2.texture).setAlpha(0.5f), x, y0);
        addDescrip("surface a=1\nimg layer a=0.5", x, y1, width);
        groupLayer.addAt(new ImageLayer(surf2.texture).setAlpha(0.5f), x, y2);
        x += width;

        Canvas canvas1 = game.graphics.createCanvas(image.width(), image.height());
        canvas1.draw(image, 0, 0);
        Texture cantex1 = canvas1.toTexture();
        groupLayer.addAt(new ImageLayer(cantex1).setAlpha(0.5f), x, y0);
        addDescrip("canvas a=1\nimg layer a=0.5", x, y1, width);
        groupLayer.addAt(new ImageLayer(cantex1).setAlpha(0.5f), x, y2);
        x += width;

        Canvas canvas2 = game.graphics.createCanvas(image.width(), image.height());
        canvas2.setAlpha(0.5f).draw(image, 0, 0);
        Texture cantex2 = canvas2.toTexture();
        groupLayer.addAt(new ImageLayer(cantex2), x, y0);
        addDescrip("canvas a=0.5\nimg layer a=1", x, y1, width);
        groupLayer.addAt(new ImageLayer(cantex2), x, y2);
        x += width;

        // add some copies of the image at 1, 0.5, 0.25 and 0.125 alpha
        x = offset + width;
        for (float alpha : new float[] { 1, 1/2f, 1/4f, 1/8f }) {
          float y = fullHeight+50;
          rootLayer.addAt(new ImageLayer(imtex).setAlpha(alpha), x, y);
          addDescrip("image a=" + alpha, x, y+height/2, width/2);
          x += width;
        }
      }
    });

    // add ground truth of 25% opaque image
    Image truth = game.assets.getImage("images/alphalayertest_expected.png");
    rootLayer.addAt(new ImageLayer(truth), 5*width, 0);
    addDescrip("ground truth", 5*width, offset+height, width);
  }
}
