/**
 * Copyright 2010 The ForPlay Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package forplay.java;

import java.awt.image.BufferedImage;

import forplay.core.AssetLoadException;
import forplay.core.Image;
import forplay.core.Canvas;
import forplay.core.CanvasImage;
import forplay.core.ResourceCallback;

class JavaImage implements CanvasImage {

  BufferedImage img;
  private JavaCanvas canvas;
  private Exception exception;

  JavaImage(BufferedImage img) {
    this.img = img;
  }

  public JavaImage(AssetLoadException assetLoadException) {
    this.exception = assetLoadException;
  }

  @Override
  public Canvas canvas() {
    if (canvas == null) {
      canvas = new JavaCanvas(img.createGraphics(), width(), height());
    }
    return canvas;
  }

  @Override
  public void replaceWith(Image img) {
    assert img instanceof JavaImage;
    this.img = ((JavaImage) img).img;
  }

  @Override
  public void addCallback(ResourceCallback<Image> callback) {
    if (img == null) {
      callback.error(exception != null ? exception : new RuntimeException());
    } else {
      callback.done(this);
    }
  }

  @Override
  public int width() {
    return img.getWidth();
  }

  @Override
  public int height() {
    return img.getHeight();
  }

  @Override
  public boolean isReady() {
    return (img != null && exception == null);
  }
}
