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
package playn.core.gl;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.MathUtil;

import playn.core.Asserts;
import playn.core.Image;
import playn.core.InternalTransform;
import playn.core.Pattern;
import playn.core.Surface;

/**
 * The bulk of the {@link Surface} implementation, shared by {@link SurfaceGL} which renders into a
 * texture and {@link ImmediateLayerGL} which renders directly into the framebuffer.
 */
abstract class AbstractSurfaceGL implements Surface {

  protected final GLContext ctx;
  protected final List<InternalTransform> transformStack = new ArrayList<InternalTransform>();

  protected int fillColor;
  protected float alpha = 1;
  protected ImageGL fillPattern;

  protected AbstractSurfaceGL(GLContext ctx) {
    this.ctx = ctx;
    transformStack.add(ctx.createTransform());
  }

  protected abstract void bindFramebuffer();

  @Override
  public Surface clear() {
    bindFramebuffer();
    ctx.clear(0, 0, 0, 0);
    return this;
  }

  @Override
  public Surface drawImage(Image image, float x, float y) {
    return drawImage(image, x, y, image.width(), image.height());
  }

  @Override
  public Surface drawImage(Image image, float x, float y, float dw, float dh) {
    bindFramebuffer();
    ((ImageGL) image).draw(ctx, topTransform(), x, y, dw, dh, false, false, alpha);
    return this;
  }

  @Override
  public Surface drawImage(Image image, float dx, float dy, float dw, float dh,
                           float sx, float sy, float sw, float sh) {
    bindFramebuffer();
    ((ImageGL) image).draw(ctx, topTransform(), dx, dy, dw, dh, sx, sy, sw, sh, alpha);
    return this;
  }

  @Override
  public Surface drawImageCentered(Image img, float x, float y) {
    return drawImage(img, x - img.width()/2, y - img.height()/2);
  }

  @Override
  public Surface drawLine(float x0, float y0, float x1, float y1, float width) {
    bindFramebuffer();

    float dx = x1 - x0, dy = y1 - y0;
    float len = (float) Math.sqrt(dx * dx + dy * dy);
    dx = dx * (width / 2) / len;
    dy = dy * (width / 2) / len;

    float qx1 = x0 - dy, qy1 = y0 + dx;
    float qx2 = x0 + dy, qy2 = y0 - dx;
    float qx3 = x1 - dy, qy3 = y1 + dx;
    float qx4 = x1 + dy, qy4 = y1 - dx;

    if (fillPattern != null) {
      Object tex = fillPattern.ensureTexture(ctx, true, true);
      if (tex != null) {
        ctx.fillQuad(topTransform(), qx1, qy1, qx2, qy2, qx3, qy3, qx4, qy4,
                     fillPattern.width(), fillPattern.height(), tex, alpha);
      }
    } else {
      ctx.fillQuad(topTransform(), qx1, qy1, qx2, qy2, qx3, qy3, qx4, qy4, fillColor, alpha);
    }
    return this;
  }

  @Override
  public Surface fillRect(float x, float y, float width, float height) {
    bindFramebuffer();

    if (fillPattern != null) {
      Object tex = fillPattern.ensureTexture(ctx, true, true);
      if (tex != null) {
        ctx.fillRect(topTransform(), x, y, width, height,
                     fillPattern.width(), fillPattern.height(), tex, alpha);
      }
    } else {
      ctx.fillRect(topTransform(), x, y, width, height, fillColor, alpha);
    }
    return this;
  }

  @Override
  public Surface fillTriangles(float[] xys, int[] indices) {
    bindFramebuffer();

    if (fillPattern != null) {
      Object tex = fillPattern.ensureTexture(ctx, true, true);
      if (tex != null) {
        ctx.fillTriangles(topTransform(), xys, indices,
                          fillPattern.width(), fillPattern.height(), tex, alpha);
      }
    } else {
      ctx.fillTriangles(topTransform(), xys, indices, fillColor, alpha);
    }
    return this;
  }

  @Override
  public Surface fillTriangles(float[] xys, float[] sxys, int[] indices) {
    bindFramebuffer();

    if (fillPattern == null)
      throw new IllegalStateException("No fill pattern currently set");
    Object tex = fillPattern.ensureTexture(ctx, true, true);
    if (tex != null) {
      ctx.fillTriangles(topTransform(), xys, sxys, indices, tex, alpha);
    }
    return this;
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
  public Surface setAlpha(float alpha) {
    this.alpha = MathUtil.clamp(alpha, 0, 1);
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

  InternalTransform topTransform() {
    return transformStack.get(transformStack.size() - 1);
  }
}
