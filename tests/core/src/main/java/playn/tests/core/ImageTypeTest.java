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

public class ImageTypeTest extends Test {
  GroupLayer rootLayer;

  static float width = 100;
  static float height = 100;
  static int offset = 5;
  static String imageSrc = "images/imagetypetest.png";
  static String imageGroundTruthSrc = "images/imagetypetest_expected.png";

  Image image1;

  ImageLayer imageLayer1;
  SurfaceLayer surfaceLayer1;
  ImageLayer canvasLayer1;

  ImageLayer imageLayer2;
  SurfaceLayer surfaceLayer2;
  ImageLayer canvasLayer2;

  Image imageGroundTruth;
  ImageLayer groundTruthLayer;

  @Override
  public String getName() {
    return "ImageTypeTest";
  }

  @Override
  public String getDescription() {
    return "Test that image types display the same. Left-to-right: ImageLayer, SurfaceLayer, CanvasImage, ground truth (expected).";
  }

  @Override
  public void init() {
    rootLayer = graphics().rootLayer();

    // add a half white, half blue background
    SurfaceLayer bg = graphics().createSurfaceLayer((int) (4 * width), (int) (4 * height));
    bg.surface().setFillColor(Color.rgb(255, 255, 255));
    bg.surface().fillRect(0, 0, bg.surface().width(), bg.surface().height());
    bg.surface().setFillColor(Color.rgb(0, 0, 255));
    bg.surface().fillRect(0, bg.surface().width() / 2, bg.surface().width(),
        bg.surface().height() / 2);
    rootLayer.add(bg);

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

        // add layers to the rootLayer
        rootLayer.addAt(imageLayer1, offset, offset);
        rootLayer.addAt(surfaceLayer1, offset + width, offset);
        rootLayer.addAt(canvasLayer1, offset + 2 * width, offset);

        rootLayer.addAt(imageLayer2, offset, offset + 2 * height);
        rootLayer.addAt(surfaceLayer2, offset + width, offset + 2 * height);
        rootLayer.addAt(canvasLayer2, offset + 2 * width, offset + 2 * height);
      }

      @Override
      public void error(Throwable err) {
        log().error("Error loading image", err);
      }
    });

    // add ground truth image
    imageGroundTruth = assets().getImage(imageGroundTruthSrc);
    imageGroundTruth.addCallback(new ResourceCallback<Image>() {
      @Override
      public void done(Image image) {
        groundTruthLayer = graphics().createImageLayer(image);
        rootLayer.addAt(groundTruthLayer, 3 * width, 0);
      }

      @Override
      public void error(Throwable err) {
        log().error("Error loading image", err);
      }
    });
  }
}
