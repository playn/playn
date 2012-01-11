/**
 * Copyright 2011 The PlayN Authors
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
package playn.core.gl;

import java.util.ArrayList;
import java.util.List;

import playn.core.Asserts;
import playn.core.Image;
import playn.core.InternalTransform;
import playn.core.Pattern;
import playn.core.Surface;

public class SurfaceGL implements Surface {

  protected final GLContext ctx;
  protected final int width;
  protected final int height;
  protected final List<InternalTransform> transformStack = new ArrayList<InternalTransform>();

  protected Object tex, fbuf;
  protected int fillColor;
  protected ImageGL fillPattern;

  protected SurfaceGL(GLContext ctx, int width, int height) {
    this.ctx = ctx;
    this.width = width;
    this.height = height;

    transformStack.add(ctx.createTransform());
    createTexture();
  }

  protected void paint(InternalTransform transform, float alpha) {
    // Draw this layer to the screen upside-down, because its contents are flipped (This happens
    // because it uses the same vertex program as everything else, which flips vertically to put
    // the origin at the top-left).
    ctx.drawTexture(tex, width, height, transform, 0, height, width, -height, false, false, alpha);
  }

  protected void destroy() {
    clearTexture();
  }

  protected void createTexture() {
    tex = ctx.createTexture(width, height, false, false);
    fbuf = ctx.createFramebuffer(tex);
    ctx.clear(0, 0, 0, 0);
  }

  protected void clearTexture() {
    ctx.destroyTexture(tex);
    tex = null;
    ctx.deleteFramebuffer(fbuf);
    fbuf = null;
  }

  @Override
  public Surface clear() {
    ctx.bindFramebuffer(fbuf, width, height);
    ctx.clear(0, 0, 0, 0);
    return this;
  }

  @Override
  public Surface drawImage(Image image, float x, float y) {
    drawImage(image, x, y, image.width(), image.height());
    return this;
  }

  @Override
  public Surface drawImage(Image image, float x, float y, float dw, float dh) {
    ctx.bindFramebuffer(fbuf, width, height);

    Object tex = ((ImageGL) image).ensureTexture(ctx, false, false);
    if (tex != null) {
      ctx.drawTexture(tex, image.width(), image.height(), topTransform(),
                      x, y, dw, dh, false, false, 1);
    }
    return this;
  }

  @Override
  public Surface drawImage(Image image, float dx, float dy, float dw, float dh, float sx, float sy,
      float sw, float sh) {
    ctx.bindFramebuffer(fbuf, width, height);

    Object tex = ((ImageGL) image).ensureTexture(ctx, false, false);
    if (tex != null) {
      ctx.drawTexture(tex, image.width(), image.height(), topTransform(),
                      dx, dy, dw, dh, sx, sy, sw, sh, 1);
    }
    return this;
  }

  @Override
  public Surface drawImageCentered(Image img, float x, float y) {
    drawImage(img, x - img.width()/2, y - img.height()/2);
    return this;
  }

  @Override
  public Surface drawLine(float x0, float y0, float x1, float y1, float width) {
    ctx.bindFramebuffer(fbuf, this.width, this.height);

    float dx = x1 - x0, dy = y1 - y0;
    float len = (float) Math.sqrt(dx * dx + dy * dy);
    dx = dx * (width / 2) / len;
    dy = dy * (width / 2) / len;

    float[] pos = new float[8];
    pos[0] = x0 - dy; pos[1] = y0 + dx;
    pos[2] = x1 - dy; pos[3] = y1 + dx;
    pos[4] = x1 + dy; pos[5] = y1 - dx;
    pos[6] = x0 + dy; pos[7] = y0 - dx;
    ctx.fillPoly(topTransform(), pos, fillColor, 1);
    return this;
  }

  @Override
  public Surface fillRect(float x, float y, float width, float height) {
    ctx.bindFramebuffer(fbuf, this.width, this.height);

    if (fillPattern != null) {
      Object tex = fillPattern.ensureTexture(ctx, true, true);
      ctx.fillRect(topTransform(), x, y, width, height,
                   fillPattern.width(), fillPattern.height(), tex, 1);
    } else {
      ctx.fillRect(topTransform(), x, y, width, height, fillColor, 1);
    }
    return this;
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public Surface restore() {
    Asserts.checkState(transformStack.size() > 1, "Unbalanced save/restore");
    transformStack.remove(transformStack.size() - 1);
    return this;
  }

  @Override
  public Surface rotate(float angle) {
    float sr = (float) Math.sin(angle);
    float cr = (float) Math.cos(angle);
    transform(cr, sr, -sr, cr, 0, 0);
    return this;
  }

  @Override
  public Surface save() {
    transformStack.add(ctx.createTransform().set(topTransform()));
    return this;
  }

  @Override
  public Surface scale(float sx, float sy) {
    topTransform().scale(sx, sy);
    return this;
  }

  @Override
  public Surface setTransform(float m00, float m01, float m10, float m11, float tx, float ty) {
    topTransform().setTransform(m00, m01, m10, m11, tx, ty);
    return this;
  }

  @Override
  public Surface setFillColor(int color) {
    // TODO: Add it to the state stack.
    this.fillColor = color;
    this.fillPattern = null;
    return this;
  }

  @Override
  public Surface setFillPattern(Pattern pattern) {
    // TODO: Add it to the state stack.
    Asserts.checkArgument(pattern instanceof GLPattern);
    this.fillPattern = ((GLPattern) pattern).image();
    return this;
  }

  @Override
  public Surface transform(float m00, float m01, float m10, float m11, float tx, float ty) {
    topTransform().concatenate(m00, m01, m10, m11, tx, ty, 0, 0);
    return this;
  }

  @Override
  public Surface translate(float x, float y) {
    topTransform().translate(x, y);
    return this;
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  protected void finalize() throws Throwable {
    // if we weren't destroyed earlier, queue up destruction of our texture and framebuffer to be
    // undertaken on the main OpenGL thread on the next frame
    if (tex != null)
      ctx.queueDestroyTexture(tex);
    if (fbuf != null)
      ctx.queueDeleteFramebuffer(fbuf);
  }

  private InternalTransform topTransform() {
    return transformStack.get(transformStack.size() - 1);
  }
}
