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
package playn.ios;

import cli.MonoTouch.CoreGraphics.CGBitmapContext;
import cli.MonoTouch.CoreGraphics.CGImage;
import cli.MonoTouch.UIKit.UIColor;
import cli.MonoTouch.UIKit.UIImage;
import cli.System.Drawing.RectangleF;

import playn.core.Image;
import playn.core.Pattern;
import playn.core.ResourceCallback;
import playn.core.gl.ImageGL;

/**
 * Provides some shared bits for {@link IOSImage} and {@link IOSCanvasImage}.
 */
public abstract class IOSAbstractImage extends ImageGL implements Image, IOSCanvas.Drawable
{
  protected final IOSGLContext ctx;

  /**
   * Returns a core graphics image that can be used to paint this image into a canvas.
   */
  protected abstract CGImage cgImage();

  @Override
  public boolean isReady() {
    return true; // we're always ready
  }

  @Override
  public void addCallback(ResourceCallback<? super Image> callback) {
    callback.done(this); // we're always ready
  }

  @Override
  public Region subImage(float x, float y, float width, float height) {
    return new IOSImageRegion(this, x, y, width, height);
  }

  @Override
  public Pattern toPattern() {
    // this is a circuitous route, but I'm not savvy enough to find a more direct one
    return new IOSPattern(this, UIColor.FromPatternImage(new UIImage(cgImage())).get_CGColor());
  }

  @Override
  public void getRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    throw new UnsupportedOperationException("getRgb() not yet supported on iOS");
  }

  @Override
  public void draw(CGBitmapContext bctx, float x, float y, float width, float height) {
    CGImage cgImage = cgImage();
    // pesky fiddling to cope with the fact that UIImages are flipped; TODO: make sure drawing a
    // canvas image on a canvas image does the right thing
    y += height;
    bctx.TranslateCTM(x, y);
    bctx.ScaleCTM(1, -1);
    bctx.DrawImage(new RectangleF(0, 0, width, height), cgImage);
    bctx.ScaleCTM(1, -1);
    bctx.TranslateCTM(-x, -y);
  }

  @Override
  public void draw(CGBitmapContext bctx, float dx, float dy, float dw, float dh,
                   float sx, float sy, float sw, float sh) {
    // adjust our source rect to account for the scale factor
    sx *= ctx.scaleFactor;
    sy *= ctx.scaleFactor;
    sw *= ctx.scaleFactor;
    sh *= ctx.scaleFactor;

    CGImage cgImage = cgImage();
    float iw = cgImage.get_Width(), ih = cgImage.get_Height();
    float scaleX = dw/sw, scaleY = dh/sh;

    // pesky fiddling to cope with the fact that UIImages are flipped
    bctx.SaveState();
    bctx.TranslateCTM(dx, dy+dh);
    bctx.ScaleCTM(1, -1);
    bctx.ClipToRect(new RectangleF(0, 0, dw, dh));
    bctx.TranslateCTM(-sx*scaleX, -(ih-(sy+sh))*scaleY);
    bctx.DrawImage(new RectangleF(0, 0, iw*scaleX, ih*scaleY), cgImage);
    bctx.RestoreState();
  }

  @Override
  public Image transform(BitmapTransformer xform) {
    return new IOSImage(ctx, new UIImage(((IOSBitmapTransformer) xform).transform(cgImage())));
  }

  @Override
  protected void finalize() {
    if (tex != null)
      ctx.queueDestroyTexture(tex);
    if (reptex != null)
      ctx.queueDeleteFramebuffer(reptex);
  }

  protected IOSAbstractImage(IOSGLContext ctx) {
    this.ctx = ctx;
  }
}
