/**
 * Copyright 2010-2015 The PlayN Authors
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
import com.bugvm.apple.coregraphics.CGImage;
import com.bugvm.apple.coregraphics.CGInterpolationQuality;

import playn.core.*;

public class BugCanvasImage extends BugImage {

  CGBitmapContext bctx;

  public BugCanvasImage (Graphics gfx, Scale scale, int pixelWidth, int pixelHeight,
                          boolean interpolate) {
    super(gfx, scale, pixelWidth, pixelHeight, "<canvas>");
    // create the bitmap context via which we'll render into it
    bctx = BugGraphics.createCGBitmap(pixelWidth, pixelHeight);
    if (!interpolate) bctx.setInterpolationQuality(CGInterpolationQuality.None);
  }

  // this isn't as inefficient as it seems because the returned CGImage will only copy the image
  // data on write, which means that if we don't modify the canvas, the snapshot is just a copy of
  // the image metadata, not the whole enchilada; there's unfortunately no way to modify the bitmap
  // data of a CGImage in place (that I can find...)
  @Override public CGImage cgImage () { return bctx.toImage(); }

  @Override protected void upload (Graphics gfx, Texture tex) {
    upload(gfx, tex.id, pixelWidth, pixelHeight, bctx.getData());
  }

  @Override public void dispose() {
    super.dispose();
    if (bctx != null) {
      bctx.dispose();
      bctx = null;
    }
  }
}
