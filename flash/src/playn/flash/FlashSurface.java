/*
 * Copyright 2011 Google Inc.
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

package playn.flash;

import pythagoras.f.MathUtil;

import playn.core.Asserts;
import playn.core.Image;
import playn.core.Layer;
import playn.core.Pattern;
import playn.core.Surface;
import playn.core.gl.GLShader;
import playn.flash.FlashCanvas.Context2d;

public class FlashSurface implements Surface {

  private final float width, height;
  private final Context2d context2d;
  private boolean dirty = true;

  FlashSurface(float width, float height, Context2d context2d) {
    this.width = width;
    this.height = height;
    this.context2d = context2d;
  }

  @Override
  public Surface clear() {
    context2d.clearRect(0, 0, MathUtil.iceil(width), MathUtil.iceil(height));
    dirty = true;
    return this;
  }

  @Override
  public Surface drawImage(Image img, float x, float y) {
    Asserts.checkArgument(img instanceof FlashImage);
    dirty = true;
    context2d.drawImage(((FlashImage) img).bitmapData(), x, y);
    return this;
  }

  @Override
  public Surface drawImage(Image img, float x, float y, float w, float h) {
    Asserts.checkArgument(img instanceof FlashImage);
    dirty = true;
    context2d.drawImage(((FlashImage) img).bitmapData(), x, y);
    return this;
  }

  @Override
  public Surface drawImage(Image img, float dx, float dy, float dw, float dh,
                           float sx, float sy, float sw, float sh) {
    Asserts.checkArgument(img instanceof FlashImage);
    dirty = true;
    context2d.drawImage(((FlashImage) img).bitmapData(), dx, dy, dw, dh, sx, sy, sw, sh);
    return this;
  }

  @Override
  public Surface drawImageCentered(Image img, float x, float y) {
    drawImage(img, x - img.width()/2, y - img.height()/2);
    dirty = true;
    return this;
  }

  @Override
  public Surface drawLayer(Layer layer) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Surface fillRect(float x, float y, float w, float h) {
    context2d.fillRect(x, y, w, h);
    dirty = true;
    return this;
  }

  @Override
  public Surface fillTriangles(float[] xys, int[] indices) {
    FlashPath path = new FlashPath();
    for (int ii = 0; ii < indices.length; ii += 3) {
      int a = 2*indices[ii], b = 2*indices[ii+1], c = 2*indices[ii+2];
      path.moveTo(xys[a], xys[a+1]);
      path.lineTo(xys[b], xys[b+1]);
      path.lineTo(xys[c], xys[c+1]);
      path.close();
    }
    path.replay(context2d);
    context2d.fill();
    dirty = true;
    return this;
  }

  @Override
  public Surface fillTriangles(float[] xys, float[] sxys, int[] indices) {
    // canvas-based surfaces can't handle texture coordinates, so ignore them; the caller has been
    // warned of this sub-optimal fallback behavior in this method's javadocs
    return fillTriangles(xys, indices);
  }

  @Override
  public final float height() {
    return height;
  }

  @Override
  public Surface restore() {
    context2d.restore();
    return this;
  }

  @Override
  public Surface rotate(float radians) {
    context2d.rotate(radians);
    return this;
  }

  @Override
  public Surface save() {
    context2d.save();
    return this;
  }

  @Override
  public Surface scale(float x, float y) {
    context2d.scale(x,y);
    return this;
  }

  @Override
  public Surface setAlpha(float alpha) {
    context2d.setGlobalAlpha(alpha);
    return this;
  }

  @Override
  public Surface setTint(int tint) {
    // NOOP: tint not supported in Flash backend
    return this;
  }

  @Override
  public Surface setFillColor(int color) {
    context2d.setFillStyle("rgba("
                           + ((color >> 16) & 0xff) + ","
                           + ((color >> 8) & 0xff) + ","
                           + (color & 0xff) + ","
                           + ((color >> 24) & 0xff) + ")");
    return this;
  }

  public Surface setStrokeColor(int color) {
    context2d.setStrokeStyle("rgba("
                             + ((color >> 16) & 0xff) + ","
                             + ((color >> 8) & 0xff) + ","
                             + (color & 0xff) + ","
                             + ((color >> 24) & 0xff) + ")");
    return this;
  }

  @Override
  public Surface setFillPattern(Pattern pattern) {
    return this;
  }

  @Override
  public Surface setShader(GLShader shader) {
    // NOOP
    return this;
  }

  @Deprecated @Override
  public Surface setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    context2d.setTransform(m11, m12, m21, m22, dx, dy);
    return this;
  }

  @Override
  public Surface transform(float m11, float m12, float m21, float m22, float dx,
                           float dy) {
    context2d.transform(m11, m12, m21, m22, dx, dy);
    return this;
  }

  @Override
  public Surface translate(float x, float y) {
    context2d.translate(x,y);
    return this;
  }

  @Override
  public final float width() {
    return width;
  }

  void clearDirty() {
    dirty = false;
  }

  boolean dirty() {
    return dirty;
  }

  /* (non-Javadoc)
   * @see playn.core.Surface#drawLine(float, float, float, float, float)
   */
  @Override
  public Surface drawLine(float x0, float y0, float x1, float y1, float width) {
    context2d.setLineWidth(width);
    context2d.beginPath();
    context2d.moveTo(x0, y0);
    context2d.lineTo(x1, y1);
    context2d.stroke();
    return this;
  }
}
