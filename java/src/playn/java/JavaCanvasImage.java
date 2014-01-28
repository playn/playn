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
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Image;
import playn.core.gl.GLContext;
import playn.core.util.Callback;

class JavaCanvasImage extends JavaImage implements CanvasImage {

  private final JavaCanvas canvas;

  JavaCanvasImage(GLContext ctx, float width, float height) {
    super(ctx, new BufferedImage(ctx.scale.scaledCeil(width), ctx.scale.scaledCeil(height),
                                 BufferedImage.TYPE_INT_ARGB_PRE), ctx.scale);
    Graphics2D gfx = img.createGraphics();
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    gfx.scale(ctx.scale.factor, ctx.scale.factor);
    canvas = new JavaCanvas(gfx, width(), height());
  }

  @Override
  public Canvas canvas() {
    return canvas;
  }

  @Override
  public Image snapshot() {
    ColorModel cm = img.getColorModel();
    boolean isAlphaPremultiplied = img.isAlphaPremultiplied();
    WritableRaster raster = img.copyData(null);
    BufferedImage snap = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    return new JavaStaticImage(ctx, snap, scale);
  }

  @Override
  public void addCallback(Callback<? super Image> callback) {
    callback.onSuccess(this);
  }

  @Override
  public int ensureTexture() {
    // if we have a canvas, and it's dirty, force the recreation of our texture which will obtain
    // the latest canvas data
    if (canvas.dirty()) {
      canvas.clearDirty();
      refreshTexture();
    }
    return super.ensureTexture();
  }

  @Override
  public void setRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    img.setRGB(startX, startY, width, height, rgbArray, offset, scanSize);
  }
}
