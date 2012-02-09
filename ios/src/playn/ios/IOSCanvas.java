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

import cli.System.Drawing.RectangleF;
import cli.System.IntPtr;
import cli.System.Runtime.InteropServices.Marshal;

import cli.MonoTouch.CoreGraphics.*;

import playn.core.Canvas;
import playn.core.Canvas.LineCap;
import playn.core.Canvas.LineJoin;
import playn.core.Gradient;
import playn.core.Image;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.PlayN;
import playn.core.TextLayout;

/**
 * Implements {@link Canvas}.
 */
public class IOSCanvas implements Canvas
{
  private final int width, height;

  private boolean isDirty;
  private IntPtr data;
  private CGBitmapContext bctx;

  private LinkedList<IOSCanvasState> states = new LinkedList<IOSCanvasState>();

  IOSCanvas(int width, int height) {
    this.width = width;
    this.height = height;
    states.addFirst(new IOSCanvasState());

    // create our raw image data
    data = Marshal.AllocHGlobal(width * height * 4);

    // create the bitmap context via which we'll render into it
    bctx = new CGBitmapContext(
      data, width, height, 8, 4 * width, IOSGraphics.colorSpace,
      CGImageAlphaInfo.wrap(CGImageAlphaInfo.PremultipliedLast));

    // // CG coordinate system is OpenGL-style (0,0 in lower left); so we flip it
    bctx.TranslateCTM(0, height);
    bctx.ScaleCTM(1, -1);

    // clear the canvas to start
    clear();
  }

  public IntPtr data() {
    return data;
  }

  public boolean dirty() {
    return isDirty;
  }

  public void clearDirty() {
    isDirty = false;
  }

  public void dispose() {
    bctx.Dispose();
    bctx = null;
    Marshal.FreeHGlobal(data);
    data = null;
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public Canvas clear() {
    bctx.ClearRect(new RectangleF(0, 0, width, height));
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
  public Canvas drawImage(Image image, float dx, float dy) {
    return drawImage(image, dx, dy, image.width(), image.height());
  }

  @Override
  public Canvas drawImageCentered(Image image, float dx, float dy) {
    return drawImage(image, dx - image.width()/2, dy - image.height()/2);
  }

  @Override
  public Canvas drawImage(Image image, float dx, float dy, float dw, float dh) {
    CGImage cgImage = ((IOSAbstractImage) image).cgImage();
    // pesky fiddling to cope with the fact that UIImages are flipped; TODO: make sure drawing a
    // canvas image on a canvas image does the right thing
    dy += dh;
    bctx.TranslateCTM(dx, dy);
    bctx.ScaleCTM(1, -1);
    bctx.DrawImage(new RectangleF(0, 0, dw, dh), cgImage);
    bctx.ScaleCTM(1, -1);
    bctx.TranslateCTM(-dx, -dy);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawImage(Image image, float dx, float dy, float dw, float dh,
                          float sx, float sy, float sw, float sh) {
    CGImage cgImage = ((IOSAbstractImage) image).cgImage();
    float iw = cgImage.get_Width(), ih = cgImage.get_Height();
    float scaleX = dw/sw, scaleY = dh/sh;

    // pesky fiddling to cope with the fact that UIImages are flipped; TODO: make sure drawing a
    // canvas image on a canvas image does the right thing
    bctx.SaveState();
    bctx.TranslateCTM(dx, dy+dh);
    bctx.ScaleCTM(1, -1);
    bctx.ClipToRect(new RectangleF(0, 0, dw, dh));
    bctx.TranslateCTM(-sx*scaleX, -(ih-(sy+sh))*scaleY);
    bctx.DrawImage(new RectangleF(0, 0, iw*scaleX, ih*scaleY), cgImage);
    bctx.RestoreState();
    isDirty = true;
    return this;
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
    return drawLine(x, y, x, y);
  }

  @Override
  public Canvas drawText(String text, float x, float y) {
    bctx.ShowTextAtPoint(x, y, text);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawText(TextLayout layout, float x, float y) {
    ((IOSTextLayout) layout).draw(bctx, x, y);
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
    bctx.SetStrokeColor(toCGColor(color));
    return this;
  }

  @Override
  public Canvas setStrokeWidth(float strokeWidth) {
    bctx.SetLineWidth(strokeWidth);
    return this;
  }

  @Override
  public Canvas setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    // TODO
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
  public Canvas transform(float m11, float m12, float m21, float m22, float dx, float dy) {
    // TODO
    return this;
  }

  @Override
  public Canvas translate(float x, float y) {
    bctx.TranslateCTM(x, y);
    return this;
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
