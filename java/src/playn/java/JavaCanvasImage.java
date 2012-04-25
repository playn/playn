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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Image;
import playn.core.ResourceCallback;
import playn.core.gl.GLContext;

class JavaCanvasImage extends JavaImage implements CanvasImage {

  private final JavaCanvas canvas;

  JavaCanvasImage(JavaGLContext ctx, int width, int height) {
    super(ctx, new BufferedImage(ctx.scaledCeil(width), ctx.scaledCeil(height),
                                 BufferedImage.TYPE_INT_ARGB));
    Graphics2D gfx = img.createGraphics();
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    gfx.scale(ctx.scaleFactor, ctx.scaleFactor);
    canvas = new JavaCanvas(gfx, width(), height());
  }

  @Override
  public Canvas canvas() {
    return canvas;
  }

  @Override
  public void addCallback(ResourceCallback<? super Image> callback) {
    callback.done(this);
  }

  @Override
  public Object ensureTexture(GLContext ctx, boolean repeatX, boolean repeatY) {
    // if we have a canvas, and it's dirty, force the recreation of our texture which will obtain
    // the latest canvas data
    if (canvas.dirty()) {
      canvas.clearDirty();
      clearTexture(ctx);
    }
    return super.ensureTexture(ctx, repeatX, repeatY);
  }
}
