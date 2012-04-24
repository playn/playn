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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import playn.core.Image;
import playn.core.ResourceCallback;

class JavaErrorImage extends JavaImage {

  private final Exception exception;

  public JavaErrorImage(JavaGLContext ctx, Exception assetLoadException) {
    // the caller will be notified that this image failed to load, but we also create an error
    // image so that subsequent attempts to use this image won't result in numerous follow-on
    // errors when the caller attempts to call width/height/etc.
    super(ctx, createErrorImage(100, 100));
    this.exception = assetLoadException != null ? assetLoadException :
      new RuntimeException("Error loading image");
  }

  @Override
  public void addCallback(ResourceCallback<? super Image> callback) {
    callback.error(exception);
  }

  private static BufferedImage createErrorImage(int width, int height) {
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    try {
      g.setColor(Color.red);
      for (int yy = 0; yy <= height/15; yy++) {
        for (int xx = 0; xx <= width/45; xx++) {
          g.drawString("ERROR", xx*45, yy*15);
        }
      }
    } finally {
      g.dispose();
    }
    return img;
  }

  @Override
  public boolean isReady() {
    return false;
  }
}
