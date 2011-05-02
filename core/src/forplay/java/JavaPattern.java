/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.java;

import forplay.core.Pattern;

import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

class JavaPattern implements Pattern {

  TexturePaint paint;

  static Pattern create(JavaImage img) {
    // Apparently we need to know the size of the image to specify the anchor
    // rectangle properly. We have to recreate the TexturePaint() when the image
    // size is known.
    return new JavaPattern(createTexture(img.img, 1, 1));
  }

  private static TexturePaint createTexture(BufferedImage img, int width,
      int height) {
    return new TexturePaint(img, new Rectangle2D.Double(0, 0, width, height));
  }

  JavaPattern(TexturePaint paint) {
    this.paint = paint;
  }

  void updateSize() {
    BufferedImage img = paint.getImage();
    int imageWidth = img.getWidth();
    int imageHeight = img.getHeight();
    double anchorWidth = paint.getAnchorRect().getWidth();
    double anchorHeight = paint.getAnchorRect().getHeight();
    if ((imageWidth != anchorWidth) || (imageHeight != anchorHeight)) {
      paint = createTexture(img, imageWidth, imageHeight);
    }
  }
}
