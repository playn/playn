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

import playn.core.CanvasImage;
import playn.core.Color;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ResourceCallback;
import playn.core.SurfaceLayer;
import static playn.core.PlayN.*;

public class AlphaLayerTest extends Test {
  GroupLayer rootLayer;

  static float width = 100;
  static float height = 100;
  static int offset = 5;
  static String imageSrc = "images/alphalayertest.png";
  static String imageGroundTruthSrc = "images/alphalayertest_expected.png";

  Image image1;
  Image imageGroundTruth;
  GroupLayer groupLayer;
  ImageLayer imageLayer1;
  SurfaceLayer surfaceLayer1;
  ImageLayer canvasLayer1;
  ImageLayer imageLayer2;
  SurfaceLayer surfaceLayer2;
  ImageLayer canvasLayer2;
  ImageLayer canvasLayer3;
  ImageLayer canvasLayer4;
  ImageLayer groundTruthLayer;

  @Override
  public String getName() {
    return "AlphaLayerTest";
  }

  @Override
  public String getDescription() {
    return "Test that the alpha value on layers works the same on all layer types and that alpha is 'additive'. Left-to-right: ImageLayer, SurfaceLayer, CanvasImage (layer alpha), CanvasImage (canvas alpha), ground truth (expected). The first three layers all have alpha 50% and are in a grouplayer with alpha 50% (should result in a 25% opaque image).";
  }

  @Override
  public void init() {
    rootLayer = graphics().rootLayer();

    // add a half white, half blue background
    SurfaceLayer bg = graphics().createSurfaceLayer((int) (5 * width), (int) (4 * height));
    bg.surface().setFillColor(Color.rgb(255, 255, 255));
    bg.surface().fillRect(0, 0, bg.surface().width(), bg.surface().height());
    bg.surface().setFillColor(Color.rgb(0, 0, 255));
    bg.surface().fillRect(0, bg.surface().height() / 2, bg.surface().width(),
        bg.surface().height() / 2);
    rootLayer.add(bg);

    // add a 50% transparent group layer
    groupLayer = graphics().createGroupLayer();
    groupLayer.setAlpha(0.5f);
    rootLayer.add(groupLayer);

    image1 = assets().getImage(imageSrc);
    image1.addCallback(new ResourceCallback<Image>() {
      @Override
      public void done(Image image) {
        // once the image loads, create our layers
        imageLayer1 = graphics().createImageLayer(image);
        surfaceLayer1 = graphics().createSurfaceLayer(image.width(), image.height());
        surfaceLayer1.surface().drawImage(image, 0, 0);
        CanvasImage canvas1 = graphics().createImage(image.width(), image.height());
        canvas1.canvas().drawImage(image, 0, 0);
        canvasLayer1 = graphics().createImageLayer(canvas1);
        imageLayer2 = graphics().createImageLayer(image);
        surfaceLayer2 = graphics().createSurfaceLayer(image.width(), image.height());
        surfaceLayer2.surface().drawImage(image, 0, 0);
        CanvasImage canvas2 = graphics().createImage(image.width(), image.height());
        canvas2.canvas().drawImage(image, 0, 0);
        canvasLayer2 = graphics().createImageLayer(canvas2);
        CanvasImage canvas3 = graphics().createImage(image.width(), image.height());
        canvas3.canvas().drawImage(image, 0, 0);
        canvasLayer3 = graphics().createImageLayer(canvas3);
        canvasLayer3.setAlpha(0.5f);
        CanvasImage canvas4 = graphics().createImage(image.width(), image.height());
        canvas4.canvas().drawImage(image, 0, 0);
        canvasLayer4 = graphics().createImageLayer(canvas4);
        canvasLayer4.setAlpha(0.5f);

        // add layers to the groupLayer
        imageLayer1.transform().translate(offset, offset);
        imageLayer1.setAlpha(0.5f);
        groupLayer.add(imageLayer1);
        surfaceLayer1.transform().translate(offset + width, offset);
        surfaceLayer1.setAlpha(0.5f);
        groupLayer.add(surfaceLayer1);
        canvasLayer1.transform().translate(offset + 2 * width, offset);
        canvasLayer1.setAlpha(0.5f);
        groupLayer.add(canvasLayer1);
        canvasLayer3.transform().translate(offset + 3 * width, offset);
        groupLayer.add(canvasLayer3);

        imageLayer2.transform().translate(offset, offset + 2 * height);
        imageLayer2.setAlpha(0.5f);
        groupLayer.add(imageLayer2);
        surfaceLayer2.transform().translate(offset + width, offset + 2 * height);
        surfaceLayer2.setAlpha(0.5f);
        groupLayer.add(surfaceLayer2);
        canvasLayer2.transform().translate(offset + 2 * width, offset + 2 * height);
        canvasLayer2.setAlpha(0.5f);
        groupLayer.add(canvasLayer2);
        canvasLayer4.transform().translate(offset + 3 * width, offset + 2 * height);
        groupLayer.add(canvasLayer4);
      }

      @Override
      public void error(Throwable err) {
        log().error("Error loading image", err);
      }
    });

    // add ground truth of 25% opaque image
    imageGroundTruth = assets().getImage(imageGroundTruthSrc);
    imageGroundTruth.addCallback(new ResourceCallback<Image>() {
      @Override
      public void done(Image image) {
        groundTruthLayer = graphics().createImageLayer(image);
        groundTruthLayer.transform().translate(4 * width, 0);
        rootLayer.add(groundTruthLayer);
      }

      @Override
      public void error(Throwable err) {
        log().error("Error loading image", err);
      }
    });
  }
}
