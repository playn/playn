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
import cli.MonoTouch.CoreGraphics.CGGradient;
import cli.MonoTouch.CoreGraphics.CGGradientDrawingOptions;
import cli.System.Drawing.PointF;

import playn.core.Gradient;

public abstract class IOSGradient implements Gradient
{
  private static final CGGradientDrawingOptions gdOptions = CGGradientDrawingOptions.wrap(
    CGGradientDrawingOptions.DrawsBeforeStartLocation|CGGradientDrawingOptions.DrawsAfterEndLocation);

  final CGGradient cgGradient;

  public static class Linear extends IOSGradient {
    final PointF start, end;

    public Linear(float x0, float y0, float x1, float y1, int[] colors, float[] positions) {
      super(colors, positions);
      this.start = new PointF(x0, y0);
      this.end = new PointF(x1, y1);
    }

    @Override
    void fill(CGBitmapContext bctx) {
      bctx.DrawLinearGradient(cgGradient, start, end, gdOptions);
    }
  }

  public static class Radial extends IOSGradient {
    final PointF center;
    final float r;

    public Radial(float x, float y, float r, int[] colors, float[] positions) {
      super(colors, positions);
      this.center = new PointF(x, y);
      this.r = r;
    }

    @Override
    void fill(CGBitmapContext bctx) {
      bctx.DrawRadialGradient(cgGradient, center, 0, center, r, gdOptions);
    }
  }

  abstract void fill(CGBitmapContext bctx);

  protected IOSGradient(int[] colors, float[] positions) {
    // expand the color components from ARGB into an array of floats in RGBA order
    float[] comps = new float[colors.length*4];
    int cc = 0;
    for (int color : colors) {
      comps[cc++] = ((color >> 16) & 0xFF) / 255f;
      comps[cc++] = ((color >>  8) & 0xFF) / 255f;
      comps[cc++] = ((color >>  0) & 0xFF) / 255f;
      comps[cc++] = ((color >> 24) & 0xFF) / 255f;
    }
    cgGradient = new CGGradient(IOSGraphics.colorSpace, comps, positions);
  }

  @Override
  protected void finalize () {
    cgGradient.Dispose(); // meh
  }
}
