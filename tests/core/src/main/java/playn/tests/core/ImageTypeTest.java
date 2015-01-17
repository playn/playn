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

public class ImageTypeTest extends Test {

  static float width = 100;
  static float height = 100;
  static int offset = 5;
  static String imageSrc = "images/imagetypetest.png";
  static String imageGroundTruthSrc = "images/imagetypetest_expected.png";

  public ImageTypeTest (TestsGame game) {
    super(game, "ImageTypeTest",
          "Test that image types display the same. Left-to-right: TextureLayer, SurfaceImage, " +
          "CanvasImage, ground truth (expected).");
  }

  @Override public void init() {
    // add a half white, half blue background
    float bwidth = 4*width, bheight = 4*height;
    TextureSurface bg = game.createSurface(bwidth, bheight);
    bg.begin().
      setFillColor(Color.rgb(255, 255, 255)).fillRect(0, 0, bwidth, bheight).
      setFillColor(Color.rgb(0, 0, 255)).fillRect(0, bwidth/2, bwidth, bheight/2).
      end().close();
    game.rootLayer.add(new TextureLayer(bg.texture));

    game.assets.getBitmap(imageSrc).state.onSuccess(new Slot<Bitmap>() {
      public void onEmit (Bitmap image) {
        // once the image loads, create our layers
        Texture imtex = game.graphics.createTexture(image);
        game.rootLayer.addAt(new TextureLayer(imtex), offset, offset);
        game.rootLayer.addAt(new TextureLayer(imtex), offset, offset + 2*height);

        TextureSurface surf = game.createSurface(image.width(), image.height());
        surf.begin().clear().draw(imtex, 0, 0).end().close();
        game.rootLayer.addAt(new TextureLayer(surf.texture), offset + width, offset);
        game.rootLayer.addAt(new TextureLayer(surf.texture), offset + width, offset + 2*height);

        Canvas canvas = game.graphics.createCanvas(image.width(), image.height());
        canvas.draw(image, 0, 0);
        Texture cantex = game.graphics.createTexture(canvas.bitmap);
        game.rootLayer.addAt(new TextureLayer(cantex), offset + 2*width, offset);
        game.rootLayer.addAt(new TextureLayer(cantex), offset + 2*width, offset + 2*height);
      }
    });

    // add ground truth image
    game.assets.getBitmap(imageGroundTruthSrc).state.onSuccess(new Slot<Bitmap>() {
      public void onEmit (Bitmap image) {
        game.rootLayer.addAt(new TextureLayer(game.graphics, image), 3 * width, 0);
      }
    });
  }
}
