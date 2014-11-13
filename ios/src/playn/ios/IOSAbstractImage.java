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
import cli.MonoTouch.CoreGraphics.CGColorSpace;
import cli.MonoTouch.CoreGraphics.CGImage;
import cli.MonoTouch.CoreGraphics.CGImageAlphaInfo;
import cli.MonoTouch.CoreGraphics.CGInterpolationQuality;
import cli.MonoTouch.UIKit.UIColor;
import cli.MonoTouch.UIKit.UIImage;
import cli.System.Drawing.RectangleF;

import playn.core.Image;
import playn.core.Pattern;
import playn.core.gl.AbstractImageGL;
import playn.core.gl.GLContext;
import playn.core.gl.ImageGL;
import playn.core.gl.Scale;
import playn.core.util.Callback;

/**
 * Provides some shared bits for {@link IOSImage} and {@link IOSCanvasImage}.
 */
public abstract class IOSAbstractImage extends ImageGL<CGBitmapContext> implements Image {

  /**
   * Creates a {@code UIImage} based on our underlying image data. This is useful when you need to
   * pass PlayN images to iOS APIs.
   */
  public UIImage toUIImage() {
    return UIImage.FromImage(cgImage());
  }

  /**
   * Returns the {@link CGImage} that underlies this image. This is public so that games that need
   * to write custom backend code to do special stuff can access it. No promises are made, caveat
   * coder.
   */
  public abstract CGImage cgImage();

  @Override
  public boolean isReady() {
    return true; // we're always ready
  }

  @Override
  public void addCallback(Callback<? super Image> callback) {
    callback.onSuccess(this); // we're always ready
  }

  @Override
  public Pattern toPattern() {
    // this is a circuitous route, but I'm not savvy enough to find a more direct one
    return new IOSPattern(this, UIColor.FromPatternImage(new UIImage(cgImage())).get_CGColor(),
                          repeatX, repeatY);
  }

  @Override
  public void getRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    int bytesPerRow = 4 * width;
    byte[] regionBytes = new byte[bytesPerRow * height];
    CGBitmapContext context = new CGBitmapContext(regionBytes, width, height, 8, bytesPerRow,
      // PremultipliedFirst for ARGB, same as BufferedImage in Java.
      CGColorSpace.CreateDeviceRGB(), CGImageAlphaInfo.wrap(CGImageAlphaInfo.PremultipliedFirst));
    // since we're fishing for authentic RGB data, never allow interpolation.
    context.set_InterpolationQuality(CGInterpolationQuality.wrap(CGInterpolationQuality.None));
    draw(context, 0, 0, width, height, startX, startY, width, height);

    int x = 0;
    int y = height - 1; // inverted Y
    for (int px = 0; px < regionBytes.length; px += 4) {
      int a = (int)regionBytes[px    ] & 0xFF;
      int r = (int)regionBytes[px + 1] & 0xFF;
      int g = (int)regionBytes[px + 2] & 0xFF;
      int b = (int)regionBytes[px + 3] & 0xFF;
      rgbArray[offset + y * scanSize + x] = a << 24 | r << 16 | g << 8 | b;

      x++;
      if (x == width) {
        x = 0;
        y--;
      }
    }
  }

  @Override
  public Image transform(BitmapTransformer xform) {
    UIImage ximage = new UIImage(((IOSBitmapTransformer) xform).transform(cgImage()));
    return new IOSImage(ctx, ximage.get_CGImage(), scale);
  }

  @Override
  public void draw(CGBitmapContext bctx, float x, float y, float width, float height) {
    CGImage cgImage = cgImage();
    // pesky fiddling to cope with the fact that UIImages are flipped; TODO: make sure drawing a
    // canvas image on a canvas image does the right thing
    y += height;
    bctx.SaveState();
    bctx.TranslateCTM(x, y);
    bctx.ScaleCTM(1, -1);
    bctx.DrawImage(new RectangleF(0, 0, width, height), cgImage);
    bctx.RestoreState();
  }

  @Override
  public void draw(CGBitmapContext bctx, float dx, float dy, float dw, float dh,
                   float sx, float sy, float sw, float sh) {
    // adjust our source rect to account for the scale factor
    sx *= scale.factor;
    sy *= scale.factor;
    sw *= scale.factor;
    sh *= scale.factor;

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
  protected Pattern toSubPattern(AbstractImageGL<?> image, boolean repeatX, boolean repeatY,
                                 float x, float y, float width, float height) {
    // this is a circuitous route, but I'm not savvy enough to find a more direct one
    CGImage subImage = cgImage().WithImageInRect(new RectangleF(x, y, width, height));
    return new IOSPattern(image, UIColor.FromPatternImage(new UIImage(subImage)).get_CGColor(),
                          repeatX, repeatY);
  }

  protected IOSAbstractImage(GLContext ctx, Scale scale) {
    super(ctx, scale);
  }
}
