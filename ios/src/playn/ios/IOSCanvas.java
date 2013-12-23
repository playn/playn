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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cli.MonoTouch.CoreGraphics.*;
import cli.System.Drawing.RectangleF;
import cli.System.IntPtr;
import cli.System.Runtime.InteropServices.Marshal;

import playn.core.Canvas;
import playn.core.Gradient;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.TextLayout;
import playn.core.gl.AbstractCanvasGL;

/**
 * Implements {@link Canvas}.
 */
public class IOSCanvas extends AbstractCanvasGL<CGBitmapContext> {

  public interface Drawable {
    void stroke(CGBitmapContext bctx, float x, float y, float strokeWidth, int strokeColor);
    void fill(CGBitmapContext bctx, float x, float y);
  }

  private final int texWidth, texHeight;

  private float strokeWidth = 1;
  private int strokeColor = 0xFF000000;
  private IntPtr data;
  private CGBitmapContext bctx;
  private IOSGLContext ctx;

  private LinkedList<IOSCanvasState> states = new LinkedList<IOSCanvasState>();

  public IOSCanvas(IOSGLContext ctx, float width, float height, boolean interpolate) {
    super(width, height);
    // if our size is invalid, we'll fail below at CGBitmapContext, so fail here more usefully
    if (width <= 0 || height <= 0) throw new IllegalArgumentException(
      "Invalid size " + width + "x" + height);
    states.addFirst(new IOSCanvasState());

    this.ctx = ctx;

    // create our raw image data
    texWidth = ctx.scale.scaledCeil(width);
    texHeight = ctx.scale.scaledCeil(height);
    data = Marshal.AllocHGlobal(texWidth * texHeight * 4);

    // create the bitmap context via which we'll render into it
    bctx = new CGBitmapContext(
      data, texWidth, texHeight, 8, 4 * texWidth, IOSGraphics.colorSpace,
      CGImageAlphaInfo.wrap(CGImageAlphaInfo.PremultipliedLast));
    if (!interpolate) {
      bctx.set_InterpolationQuality(CGInterpolationQuality.wrap(CGInterpolationQuality.None));
    }

    // CG coordinate system is OpenGL-style (0,0 in lower left); so we flip it
    bctx.TranslateCTM(0, ctx.scale.scaled(height));
    bctx.ScaleCTM(ctx.scale.factor, -ctx.scale.factor);

    // clear the canvas to start
    clear();
  }

  public IntPtr data() {
    return data;
  }

  public int texWidth() {
    return texWidth;
  }

  public int texHeight() {
    return texHeight;
  }

  public CGImage cgImage() {
    // TODO: make sure the image created by this call doesn't require any manual resource
    // releasing, other than being eventually garbage collected
    return bctx.ToImage();
  }

  public void dispose() {
    if (bctx != null) {
      bctx.Dispose();
      bctx = null;
    }
    if (data != null) {
      Marshal.FreeHGlobal(data);
      data = null;
    }
  }

  @Override
  public Canvas clear() {
    bctx.ClearRect(new RectangleF(0, 0, texWidth, texHeight));
    isDirty = true;
    return this;
  }

  @Override
  public Canvas clearRect(float x, float y, float width, float height) {
    bctx.ClearRect(new RectangleF(x, y, width, height));
    isDirty = true;
    return this;
  }

  @Override
  public Canvas clip(Path clipPath) {
    bctx.AddPath(((IOSPath) clipPath).cgPath);
    bctx.Clip();
    return this;
  }

  @Override
  public Canvas clipRect(float x, float y, float width, float height) {
    bctx.ClipToRect(new RectangleF(x, y, width, height));
    return this;
  }

  @Override
  public Path createPath() {
    return new IOSPath();
  }

  @Override
  public Canvas drawLine(float x0, float y0, float x1, float y1) {
    bctx.BeginPath();
    bctx.MoveTo(x0, y0);
    bctx.AddLineToPoint(x1, y1);
    bctx.StrokePath();
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawPoint(float x, float y) {
    save();
    setStrokeWidth(0.5f);
    strokeRect(x + 0.25f, y + 0.25f, 0.5f, 0.5f);
    restore();
    return this;
  }

  @Override
  public Canvas drawText(String text, float x, float y) {
    bctx.SaveState();
    bctx.TranslateCTM(x, y + IOSGraphics.defaultFont.ctFont.get_DescentMetric());
    bctx.ScaleCTM(1, -1);
    bctx.SelectFont(IOSGraphics.defaultFont.iosName(), IOSGraphics.defaultFont.size(),
                    CGTextEncoding.wrap(CGTextEncoding.MacRoman));
    bctx.ShowTextAtPoint(0, 0, text);
    bctx.RestoreState();
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillCircle(float x, float y, float radius) {
    IOSGradient gradient = currentState().gradient;
    if (gradient == null) {
      bctx.FillEllipseInRect(new RectangleF(x-radius, y-radius, 2*radius, 2*radius));
    } else {
      // TODO: clip to circle
      gradient.fill(bctx);
    }
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillPath(Path path) {
    bctx.AddPath(((IOSPath) path).cgPath);
    IOSGradient gradient = currentState().gradient;
    if (gradient == null) {
      bctx.FillPath();
    } else {
      bctx.Clip();
      gradient.fill(bctx);
    }
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillRect(float x, float y, float width, float height) {
    IOSGradient gradient = currentState().gradient;
    if (gradient == null) {
      bctx.FillRect(new RectangleF(x, y, width, height));
    } else {
      bctx.SaveState();
      bctx.ClipToRect(new RectangleF(x, y, width, height));
      gradient.fill(bctx);
      bctx.RestoreState();
    }
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillRoundRect(float x, float y, float width, float height, float radius) {
    addRoundRectPath(x, y, width, height, radius);
    IOSGradient gradient = currentState().gradient;
    if (gradient == null) {
      bctx.FillPath();
    } else {
      bctx.Clip();
      gradient.fill(bctx);
    }
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillText(TextLayout layout, float x, float y) {
    IOSGradient gradient = currentState().gradient;
    Drawable ilayout = (Drawable) layout;
    if (gradient == null) {
      ilayout.fill(bctx, x, y);

    } else {
      // draw our text into a fresh context so we can use it as a mask for the gradient
      IntPtr data = Marshal.AllocHGlobal(texWidth * texHeight * 4);
      CGBitmapContext maskContext = new CGBitmapContext(
        data, texWidth, texHeight, 8, 4 * texWidth, IOSGraphics.colorSpace,
        CGImageAlphaInfo.wrap(CGImageAlphaInfo.PremultipliedLast));
      maskContext.ClearRect(new RectangleF(0, 0, texWidth, texHeight));
      // scale the context based on our scale factor
      maskContext.ScaleCTM(ctx.scale.factor, ctx.scale.factor);
      // fill the text into this temp context in white for use as a mask
      maskContext.SetFillColor(toCGColor(0xFFFFFFFF));
      ilayout.fill(maskContext, 0, 0);

      // now fill the gradient, using our temp context as a mask
      bctx.SaveState();
      bctx.ClipToMask(new RectangleF(x, y, width, height), maskContext.ToImage());
      gradient.fill(bctx);
      bctx.RestoreState();

      // finally free the temp context and its associated buffer
      maskContext.Dispose();
      Marshal.FreeHGlobal(data);
    }

    isDirty = true;
    return this;
  }

  @Override
  public Canvas restore() {
    states.removeFirst();
    bctx.RestoreState();
    return this;
  }

  @Override
  public Canvas rotate(float radians) {
    bctx.RotateCTM(radians);
    return this;
  }

  @Override
  public Canvas save() {
    states.addFirst(new IOSCanvasState(currentState()));
    bctx.SaveState();
    return this;
  }

  @Override
  public Canvas scale(float x, float y) {
    bctx.ScaleCTM(x, y);
    return this;
  }

  @Override
  public Canvas setAlpha(float alpha) {
    bctx.SetAlpha(alpha);
    return this;
  }

  @Override
  public Canvas setCompositeOperation(Composite composite) {
    bctx.SetBlendMode(CGBlendMode.wrap(compToBlend.get(composite)));
    return this;
  }

  @Override
  public Canvas setFillColor(int color) {
    currentState().gradient = null;
    bctx.SetFillColor(toCGColor(color));
    return this;
  }

  @Override
  public Canvas setFillGradient(Gradient gradient) {
    currentState().gradient = (IOSGradient) gradient;
    return this;
  }

  @Override
  public Canvas setFillPattern(Pattern pattern) {
    currentState().gradient = null;
    // TODO: this anchors the fill pattern in the lower left; sigh
    bctx.SetFillColor(((IOSPattern) pattern).colorWithPattern);
    return this;
  }

  @Override
  public Canvas setLineCap(LineCap cap) {
    bctx.SetLineCap(CGLineCap.wrap(decodeCap.get(cap)));
    return this;
  }

  @Override
  public Canvas setLineJoin(LineJoin join) {
    bctx.SetLineJoin(CGLineJoin.wrap(decodeJoin.get(join)));
    return this;
  }

  @Override
  public Canvas setMiterLimit(float miter) {
    bctx.SetMiterLimit(miter);
    return this;
  }

  @Override
  public Canvas setStrokeColor(int color) {
    this.strokeColor = color;
    bctx.SetStrokeColor(toCGColor(color));
    return this;
  }

  @Override
  public Canvas setStrokeWidth(float strokeWidth) {
    this.strokeWidth = strokeWidth;
    bctx.SetLineWidth(strokeWidth);
    return this;
  }

  @Override
  public Canvas strokeCircle(float x, float y, float radius) {
    bctx.StrokeEllipseInRect(new RectangleF(x-radius, y-radius, 2*radius, 2*radius));
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokePath(Path path) {
    bctx.AddPath(((IOSPath) path).cgPath);
    bctx.StrokePath();
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokeRect(float x, float y, float width, float height) {
    bctx.StrokeRect(new RectangleF(x, y, width, height));
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokeRoundRect(float x, float y, float width, float height, float radius) {
    addRoundRectPath(x, y, width, height, radius);
    bctx.StrokePath();
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokeText(TextLayout layout, float x, float y) {
    ((Drawable) layout).stroke(bctx, x, y, strokeWidth, strokeColor);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas transform(float m11, float m12, float m21, float m22, float dx, float dy) {
    bctx.ConcatCTM(new CGAffineTransform(m11, m12, m21, m22, dx, dy));
    return this;
  }

  @Override
  public Canvas translate(float x, float y) {
    bctx.TranslateCTM(x, y);
    return this;
  }

  @Override
  protected void finalize() {
    dispose(); // meh
  }

  @Override
  protected CGBitmapContext gc() {
    return bctx;
  }

  private void addRoundRectPath(float x, float y, float width, float height, float radius) {
    float midx = x + width/2, midy = y + height/2, maxx = x + width, maxy = y + height;
    bctx.BeginPath();
    bctx.MoveTo(x, midy);
    bctx.AddArcToPoint(x, y, midx, y, radius);
    bctx.AddArcToPoint(maxx, y, maxx, midy, radius);
    bctx.AddArcToPoint(maxx, maxy, midx, maxy, radius);
    bctx.AddArcToPoint(x, maxy, x, midy, radius);
    bctx.ClosePath();
  }

  private IOSCanvasState currentState() {
    return states.peek();
  }

  static CGColor toCGColor(int color) {
    float blue = (color & 0xFF) / 255f;
    color >>= 8;
    float green = (color & 0xFF) / 255f;
    color >>= 8;
    float red = (color & 0xFF) / 255f;
    color >>= 8;
    float alpha = (color & 0xFF) / 255f;
    return new CGColor(red, green, blue, alpha);
  }

  private static Map<Composite,Integer> compToBlend = new HashMap<Composite,Integer>();
  static {
    compToBlend.put(Composite.SRC, CGBlendMode.Copy);
    compToBlend.put(Composite.DST_ATOP, CGBlendMode.DestinationAtop);
    compToBlend.put(Composite.SRC_OVER, CGBlendMode.Normal);
    compToBlend.put(Composite.DST_OVER, CGBlendMode.DestinationOver);
    compToBlend.put(Composite.SRC_IN, CGBlendMode.SourceIn);
    compToBlend.put(Composite.DST_IN, CGBlendMode.DestinationIn);
    compToBlend.put(Composite.SRC_OUT, CGBlendMode.SourceOut);
    compToBlend.put(Composite.DST_OUT, CGBlendMode.DestinationOut);
    compToBlend.put(Composite.SRC_ATOP, CGBlendMode.SourceAtop);
    compToBlend.put(Composite.XOR, CGBlendMode.XOR);
    compToBlend.put(Composite.MULTIPLY, CGBlendMode.Multiply);
  }

  private static Map<LineCap,Integer> decodeCap = new HashMap<LineCap,Integer>();
  static {
    decodeCap.put(LineCap.BUTT, CGLineCap.Butt);
    decodeCap.put(LineCap.ROUND, CGLineCap.Round);
    decodeCap.put(LineCap.SQUARE, CGLineCap.Square);
  }

  private static Map<LineJoin,Integer> decodeJoin = new HashMap<LineJoin,Integer>();
  static {
    decodeJoin.put(LineJoin.BEVEL, CGLineJoin.Bevel);
    decodeJoin.put(LineJoin.MITER, CGLineJoin.Miter);
    decodeJoin.put(LineJoin.ROUND, CGLineJoin.Round);
  }
}
