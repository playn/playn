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

import playn.core.Pattern;
import playn.core.gl.ImageRegionGL;

public class IOSImageRegion extends ImageRegionGL implements IOSCanvas.Drawable
{
  public IOSImageRegion(IOSAbstractImage parent, float x, float y, float width, float height) {
    super(parent, x, y, width, height);
  }

  @Override
  public Pattern toPattern() {
    // this is an even more circuitous route, but I'm not savvy enough to find a more direct one
    CGImage subImage = ((IOSAbstractImage) parent).cgImage().WithImageInRect(
      new RectangleF(x, y, width, height));
    return new IOSPattern(this, UIColor.FromPatternImage(new UIImage(subImage)).get_CGColor());
  }

  @Override
  public void getRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    throw new UnsupportedOperationException("getRgb() not yet supported on iOS");
  }

  @Override
  public void draw(CGBitmapContext bctx, float dx, float dy, float dw, float dh) {
    ((IOSAbstractImage) parent).draw(bctx, dx, dy, dw, dh, x, y, width, height);
  }

  @Override
  public void draw(CGBitmapContext bctx, float dx, float dy, float dw, float dh,
                   float sx, float sy, float sw, float sh) {
    ((IOSAbstractImage) parent).draw(bctx, dx, dy, dw, dh, x+sx, y+sy, sw, sh);
  }
}
