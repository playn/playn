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
package playn.bugvm;

import com.bugvm.apple.coregraphics.CGBitmapContext;
import com.bugvm.apple.coregraphics.CGBitmapInfo;
import com.bugvm.apple.coregraphics.CGColorSpace;
import com.bugvm.apple.coregraphics.CGImage;
import com.bugvm.apple.coregraphics.CGImageAlphaInfo;
import com.bugvm.apple.coregraphics.CGInterpolationQuality;
import com.bugvm.apple.coregraphics.CGRect;
import com.bugvm.apple.uikit.UIColor;
import com.bugvm.apple.uikit.UIImage;
import com.bugvm.rt.bro.ptr.IntPtr;

import playn.core.*;
import playn.bugvm.OpenGLES;

public class BugImage extends ImageImpl {

  // note: this is not used for the image that backs a BugCanvas because of the way
  // CGBitmapContext and CGImage don't completely place nicely together
  private CGImage image;

  public BugImage (Graphics gfx, Scale scale, CGImage img, String source) {
    super(gfx, scale, (int)img.getWidth(), (int)img.getHeight(), source, img);
  }

  public BugImage (BugPlatform plat, boolean async, int preWidth, int preHeight, String source) {
    super(plat, async, Scale.ONE, preWidth, preHeight, source);
  }

  /** Returns the {@link CGImage} that underlies this image. This is public so that games that need
    * to write custom backend code to do special stuff can access it. No promises are made, caveat
    * coder. Note: it's not initialized immediately for async loaded images. */
  public CGImage cgImage() {
    return image;
  }

  /** Creates a {@code UIImage} based on our underlying image data. This is useful when you need to
    * pass PlayN images to iOS APIs. */
  public UIImage toUIImage() {
    return new UIImage(cgImage());
  }

  @Override public Pattern createPattern(boolean repeatX, boolean repeatY) {
    if (image == null) throw new IllegalStateException("Can't create pattern from un-ready image.");
    // this is a circuitous route, but I'm not savvy enough to find a more direct one
    return new BugPattern(UIColor.fromPatternImage(toUIImage()).getCGColor(), repeatX, repeatY);
  }

  @Override public void getRgb(int startX, int startY, int width, int height,
                               int[] rgbArray, int offset, int scanSize) {
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

  @Override public void setRgb(int startX, int startY, int width, int height,
                               int[] rgbArray, int offset, int scanSize) {
    throw new UnsupportedOperationException("TODO!");
  }

  @Override public Image transform(BitmapTransformer xform) {
    UIImage ximage = new UIImage(((BugBitmapTransformer) xform).transform(cgImage()));
    return new BugImage(gfx, scale, ximage.getCGImage(), source);
  }

  @Override public void draw(Object ctx, float x, float y, float width, float height) {
    CGBitmapContext bctx = (CGBitmapContext)ctx;
    // pesky fiddling to cope with the fact that UIImages are flipped; TODO: make sure drawing a
    // canvas image on a canvas image does the right thing
    y += height;
    bctx.saveGState();
    bctx.translateCTM(x, y);
    bctx.scaleCTM(1, -1);
    bctx.drawImage(new CGRect(0, 0, width, height), cgImage());
    bctx.restoreGState();
  }

  @Override public void draw(Object ctx, float dx, float dy, float dw, float dh,
                             float sx, float sy, float sw, float sh) {
    // adjust our source rect to account for the scale factor
    sx *= scale.factor;
    sy *= scale.factor;
    sw *= scale.factor;
    sh *= scale.factor;

    CGImage image = cgImage();
    CGBitmapContext bctx = (CGBitmapContext)ctx;
    float iw = image.getWidth(), ih = image.getHeight();
    float scaleX = dw/sw, scaleY = dh/sh;

    // pesky fiddling to cope with the fact that UIImages are flipped
    bctx.saveGState();
    bctx.translateCTM(dx, dy+dh);
    bctx.scaleCTM(1, -1);
    bctx.clipToRect(new CGRect(0, 0, dw, dh));
    bctx.translateCTM(-sx*scaleX, -(ih-(sy+sh))*scaleY);
    bctx.drawImage(new CGRect(0, 0, iw*scaleX, ih*scaleY), image);
    bctx.restoreGState();
  }

  @Override public String toString () { return "Image[src=" + source + ", cgimg=" + image + "]"; }

  public void dispose () {
    if (image != null) {
      image.dispose();
      image = null;
    }
  }

  protected BugImage (Graphics gfx, Scale scale, int pixelWidth, int pixelHeight, String source) {
    super(gfx, scale, pixelWidth, pixelHeight, source, null);
  }

  @Override protected void finalize () {
    dispose(); // meh
  }

  @Override protected void upload (Graphics gfx, Texture tex) {
    int width = pixelWidth, height = pixelHeight;
    if (width == 0 || height == 0) {
      ((BugGraphics)gfx).plat.log().info("Ignoring texture update for empty image (" +
                                          width + "x" + height + ").");
      return;
    }

    CGBitmapContext bctx = BugGraphics.createCGBitmap(width, height);
    CGRect rect = new CGRect(0, 0, width, height);
    bctx.clearRect(rect);
    bctx.drawImage(rect, image);
    upload(gfx, tex.id, width, height, bctx.getData());
    bctx.dispose();
  }

  protected void upload (Graphics gfx, int tex, int width, int height, IntPtr data) {
    gfx.gl.glBindTexture(GL20.GL_TEXTURE_2D, tex);
    gfx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
    OpenGLES.glTexImage2Dp(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, width, height, 0,
                           GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, data);
  }

  @Override protected void setBitmap (Object bitmap) {
    image = (CGImage)bitmap;
  }

  @Override protected Object createErrorBitmap (int pixelWidth, int pixelHeight) {
    // TODO: draw something into the image, or fill it with red or something
    return BugGraphics.createCGBitmap(pixelWidth, pixelHeight).toImage();
  }
}
