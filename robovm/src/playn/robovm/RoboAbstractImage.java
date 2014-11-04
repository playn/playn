/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import org.robovm.apple.coregraphics.CGBitmapContext;
import org.robovm.apple.coregraphics.CGBitmapInfo;
import org.robovm.apple.coregraphics.CGColorSpace;
import org.robovm.apple.coregraphics.CGImage;
import org.robovm.apple.coregraphics.CGImageAlphaInfo;
import org.robovm.apple.coregraphics.CGInterpolationQuality;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.uikit.UIColor;
import org.robovm.apple.uikit.UIImage;

import playn.core.Image;
import playn.core.Pattern;
import playn.core.gl.AbstractImageGL;
import playn.core.gl.GLContext;
import playn.core.gl.ImageGL;
import playn.core.gl.Scale;
import playn.core.util.Callback;

/**
 * Provides some shared bits for {@link RoboImage} and {@link RoboCanvasImage}.
 */
public abstract class RoboAbstractImage extends ImageGL<CGBitmapContext> implements Image {

  /**
   * Creates a {@code UIImage} based on our underlying image data. This is useful when you need to
   * pass PlayN images to iOS APIs.
   */
  public UIImage toUIImage() {
    return new UIImage(cgImage());
  }

  /**
   * Returns the {@link BufferedImage} that underlies this image. This is public so that games that
   * need to write custom backend code to do special stuff can access it. No promises are made,
   * caveat coder.
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
    return new RoboPattern(this, UIColor.fromPatternImage(new UIImage(cgImage())).getCGColor(),
                          repeatX, repeatY);
  }

  @Override
  public void getRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    int bytesPerRow = 4 * width;
    CGBitmapContext context = CGBitmapContext.create(
      width, height, 8, bytesPerRow, CGColorSpace.createDeviceRGB(),
      // PremultipliedFirst for ARGB, same as BufferedImage in Java.
      new CGBitmapInfo(CGImageAlphaInfo.PremultipliedFirst.value()));
    // since we're fishing for authentic RGB data, never allow interpolation.
    context.setInterpolationQuality(CGInterpolationQuality.None);
    draw(context, 0, 0, width, height, startX, startY, width, height);

    // TODO: extract data from context.getData()
    // int x = 0;
    // int y = height - 1; // inverted Y
    // for (int px = 0; px < regionBytes.length; px += 4) {
    //   int a = (int)regionBytes[px    ] & 0xFF;
    //   int r = (int)regionBytes[px + 1] & 0xFF;
    //   int g = (int)regionBytes[px + 2] & 0xFF;
    //   int b = (int)regionBytes[px + 3] & 0xFF;
    //   rgbArray[offset + y * scanSize + x] = a << 24 | r << 16 | g << 8 | b;

    //   x++;
    //   if (x == width) {
    //     x = 0;
    //     y--;
    //   }
    // }
  }

  @Override
  public Image transform(BitmapTransformer xform) {
    UIImage ximage = new UIImage(((RoboBitmapTransformer) xform).transform(cgImage()));
    return new RoboImage(ctx, ximage.getCGImage(), scale);
  }

  @Override
  public void draw(CGBitmapContext bctx, float x, float y, float width, float height) {
    CGImage cgImage = cgImage();
    // pesky fiddling to cope with the fact that UIImages are flipped; TODO: make sure drawing a
    // canvas image on a canvas image does the right thing
    y += height;
    bctx.saveGState();
    bctx.translateCTM(x, y);
    bctx.scaleCTM(1, -1);
    bctx.drawImage(new CGRect(0, 0, width, height), cgImage);
    bctx.restoreGState();
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
    float iw = cgImage.getWidth(), ih = cgImage.getHeight();
    float scaleX = dw/sw, scaleY = dh/sh;

    // pesky fiddling to cope with the fact that UIImages are flipped
    bctx.saveGState();
    bctx.translateCTM(dx, dy+dh);
    bctx.scaleCTM(1, -1);
    bctx.clipToRect(new CGRect(0, 0, dw, dh));
    bctx.translateCTM(-sx*scaleX, -(ih-(sy+sh))*scaleY);
    bctx.drawImage(new CGRect(0, 0, iw*scaleX, ih*scaleY), cgImage);
    bctx.restoreGState();
  }

  @Override
  protected Pattern toSubPattern(AbstractImageGL<?> image, boolean repeatX, boolean repeatY,
                                 float x, float y, float width, float height) {
    // this is a circuitous route, but I'm not savvy enough to find a more direct one
    CGImage subImage = CGImage.createWithImageInRect(cgImage(), new CGRect(x, y, width, height));
    return new RoboPattern(image, UIColor.fromPatternImage(new UIImage(subImage)).getCGColor(),
                          repeatX, repeatY);
  }

  protected RoboAbstractImage(GLContext ctx, Scale scale) {
    super(ctx, scale);
  }
}
