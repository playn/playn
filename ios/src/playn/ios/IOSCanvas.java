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

import cli.System.Drawing.RectangleF;
import cli.System.IntPtr;
import cli.System.Runtime.InteropServices.Marshal;

import cli.MonoTouch.CoreGraphics.CGBitmapContext;
import cli.MonoTouch.CoreGraphics.CGColorSpace;
import cli.MonoTouch.CoreGraphics.CGImageAlphaInfo;
import cli.OpenTK.Graphics.ES20.All;
import cli.OpenTK.Graphics.ES20.GL;

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

  IOSCanvas(int width, int height) {
    this.width = width;
    this.height = height;

    // create our raw image data
    data = Marshal.AllocHGlobal(width * height * 4);

    // create the bitmap context via which we'll render into it
    CGColorSpace colorSpace = CGColorSpace.CreateDeviceRGB();
    bctx = new CGBitmapContext(
      data, width, height, 8, 4 * width, colorSpace,
      CGImageAlphaInfo.wrap(CGImageAlphaInfo.PremultipliedLast));
    colorSpace.Dispose();
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
    return this;
  }

  @Override
  public Canvas drawImage(Image image, float dx, float dy) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawImageCentered(Image image, float dx, float dy) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawImage(Image image, float dx, float dy, float dw, float dh) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawImage(Image image, float dx, float dy, float dw, float dh,
                          float sx, float sy, float sw, float sh) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawLine(float x0, float y0, float x1, float y1) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawPoint(float x, float y) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawText(String text, float x, float y) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawText(TextLayout layout, float x, float y) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillCircle(float x, float y, float radius) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillPath(Path path) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillRect(float x, float y, float width, float height) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas restore() {
    return this;
  }

  @Override
  public Canvas rotate(float radians) {
    return this;
  }

  @Override
  public Canvas save() {
    return this;
  }

  @Override
  public Canvas scale(float x, float y) {
    return this;
  }

  @Override
  public Canvas setAlpha(float alpha) {
    return this;
  }

  @Override
  public Canvas setCompositeOperation(Composite composite) {
    return this;
  }

  @Override
  public Canvas setFillColor(int color) {
    return this;
  }

  @Override
  public Canvas setFillGradient(Gradient gradient) {
    return this;
  }

  @Override
  public Canvas setFillPattern(Pattern pattern) {
    return this;
  }

  @Override
  public Canvas setLineCap(LineCap cap) {
    return this;
  }

  @Override
  public Canvas setLineJoin(LineJoin join) {
    return this;
  }

  @Override
  public Canvas setMiterLimit(float miter) {
    return this;
  }

  @Override
  public Canvas setStrokeColor(int color) {
    return this;
  }

  @Override
  public Canvas setStrokeWidth(float strokeWidth) {
    return this;
  }

  @Override
  public Canvas setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    return this;
  }

  @Override
  public Canvas strokeCircle(float x, float y, float radius) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokePath(Path path) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokeRect(float x, float y, float width, float height) {
    isDirty = true;
    return this;
  }

  @Override
  public Canvas transform(float m11, float m12, float m21, float m22, float dx, float dy) {
    return this;
  }

  @Override
  public Canvas translate(float x, float y) {
    return this;
  }
}
