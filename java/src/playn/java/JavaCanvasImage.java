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
package playn.java;

import java.awt.image.BufferedImage;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Image;
import playn.core.ResourceCallback;

class JavaCanvasImage extends JavaImage implements CanvasImage {

  private JavaCanvas canvas;

  JavaCanvasImage(int width, int height) {
    super(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
  }

  @Override
  public Canvas canvas() {
    if (canvas == null) {
      canvas = new JavaCanvas(img.createGraphics(), width(), height());
    }
    return canvas;
  }

  @Override
  public void addCallback(ResourceCallback<? super Image> callback) {
    callback.done(this);
  }
}
