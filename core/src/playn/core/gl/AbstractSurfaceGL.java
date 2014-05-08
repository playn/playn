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

import pythagoras.f.FloatMath;
import pythagoras.f.MathUtil;

import playn.core.Image;
import playn.core.InternalTransform;
import playn.core.Layer;
import playn.core.Pattern;
import playn.core.Surface;
import playn.core.Tint;

/**
 * The bulk of the {@link Surface} implementation, shared by {@link SurfaceGL} which renders into a
 * texture and {@link ImmediateLayerGL} which renders directly into the framebuffer.
 */
abstract class AbstractSurfaceGL implements Surface {

  protected final GLContext ctx;
  protected final List<InternalTransform> transformStack = new ArrayList<InternalTransform>();

  protected int fillColor;
  protected int tint = Tint.NOOP_TINT;
  protected AbstractImageGL<?> fillPattern;
  protected GLShader shader;

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
    ((AbstractImageGL<?>) image).draw(shader, topTransform(), tint, x, y, dw, dh);
    return this;
  }

  @Override
  public Surface drawImage(Image image, float dx, float dy, float dw, float dh,
                           float sx, float sy, float sw, float sh) {
    bindFramebuffer();
    ((AbstractImageGL<?>) image).draw(shader, topTransform(), tint, dx, dy, dw, dh, sx, sy, sw, sh);
    return this;
  }

  @Override
  public Surface drawImageCentered(Image img, float x, float y) {
    return drawImage(img, x - img.width()/2, y - img.height()/2);
  }

  @Override
  public Surface drawLayer(Layer layer) {
    bindFramebuffer();
    ((LayerGL) layer).paint(topTransform(), tint, shader);
    return this;
  }

  @Override
  public Surface drawLine(float x0, float y0, float x1, float y1, float width) {
    bindFramebuffer();

    // swap the line end points if x1 is less than x0
    if (x1 < x0) {
      float temp = x0;
      x0 = x1;
      x1 = temp;
      temp = y0;
      y0 = y1;
      y1 = temp;
    }

    float dx = x1 - x0, dy = y1 - y0;
    float length = FloatMath.sqrt(dx * dx + dy * dy);
    float wx = dx * (width / 2) / length;
    float wy = dy * (width / 2) / length;

    InternalTransform l = ctx.createTransform();
    l.setRotation(FloatMath.atan2(dy, dx));
    l.setTranslation(x0 + wy, y0 - wx);
    l.preConcatenate(topTransform());

    GLShader shader = ctx.quadShader(this.shader);
    if (fillPattern != null) {
      int tex = fillPattern.ensureTexture();
      if (tex > 0) {
        shader.prepareTexture(tex, tint);
        shader.addQuad(l, 0, 0, length, width,
                       0, 0, length/fillPattern.width(), width/fillPattern.height());
      }
    } else {
      int tex = ctx.fillImage().ensureTexture();
      shader.prepareTexture(tex, Tint.combine(fillColor, tint));
      shader.addQuad(l, 0, 0, length, width, 0, 0, 1, 1);
    }
    return this;
  }

  @Override
  public Surface fillRect(float x, float y, float width, float height) {
    bindFramebuffer();

    GLShader shader = ctx.quadShader(this.shader);
    if (fillPattern != null) {
      int tex = fillPattern.ensureTexture();
      if (tex > 0) {
        shader.prepareTexture(tex, tint);
        float tw = fillPattern.width(), th = fillPattern.height(), r = x+width, b = y+height;
        shader.addQuad(topTransform(), x, y, x+width, y+height, x / tw, y / th, r / tw, b / th);
      }
    } else {
      int tex = ctx.fillImage().ensureTexture();
      shader.prepareTexture(tex, Tint.combine(fillColor, tint));
      shader.addQuad(topTransform(), x, y, x+width, y+height, 0, 0, 1, 1);
    }
    return this;
  }

  @Override
  public Surface fillTriangles(float[] xys, int[] indices) {
    return fillTriangles(xys, 0, xys.length, indices, 0, indices.length, 0);
  }

  @Override
  public Surface fillTriangles(float[] xys, int xysOffset, int xysLen,
                               int[] indices, int indicesOffset, int indicesLen, int indexBase) {
    bindFramebuffer();

    GLShader shader = ctx.trisShader(this.shader);
    if (fillPattern != null) {
      int tex = fillPattern.ensureTexture();
      if (tex > 0) {
        shader.prepareTexture(tex, tint);
        shader.addTriangles(topTransform(), xys, xysOffset, xysLen,
                            fillPattern.width(), fillPattern.height(),
                            indices, indicesOffset, indicesLen, indexBase);
      }
    } else {
      int tex = ctx.fillImage().ensureTexture();
      shader.prepareTexture(tex, Tint.combine(fillColor, tint));
      shader.addTriangles(topTransform(), xys, xysOffset, xysLen, 1, 1,
                          indices, indicesOffset, indicesLen, indexBase);
    }
    return this;
  }

  @Override
  public Surface fillTriangles(float[] xys, float[] sxys, int[] indices) {
    return fillTriangles(xys, sxys, 0, xys.length, indices, 0, indices.length, 0);
  }

  @Override
  public Surface fillTriangles(float[] xys, float[] sxys, int xysOffset, int xysLen,
                               int[] indices, int indicesOffset, int indicesLen, int indexBase) {
    bindFramebuffer();

    if (fillPattern == null)
      throw new IllegalStateException("No fill pattern currently set");
    int tex = fillPattern.ensureTexture();
    if (tex > 0) {
      GLShader shader = ctx.trisShader(this.shader).prepareTexture(tex, tint);
      shader.addTriangles(topTransform(), xys, sxys, xysOffset, xysLen,
                          indices, indicesOffset, indicesLen, indexBase);
    }
    return this;
  }

  @Override
  public Surface restore() {
    assert transformStack.size() > 1 : "Unbalanced save/restore";
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

  @Deprecated @Override
  public Surface setTransform(float m00, float m01, float m10, float m11, float tx, float ty) {
    topTransform().setTransform(m00, m01, m10, m11, tx, ty);
    return this;
  }

  @Override
  public Surface setAlpha(float alpha) {
    int ialpha = (int)(0xFF * MathUtil.clamp(alpha, 0, 1));
    this.tint = (ialpha << 24) | (tint & 0xFFFFFF);
    return this;
  }

  @Override
  public Surface setTint(int tint) {
    this.tint = tint;
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
    assert pattern instanceof GLPattern;
    this.fillPattern = ((GLPattern) pattern).image();
    this.fillPattern.setRepeat(true, true);
    return this;
  }

  @Override
  public Surface setShader(GLShader shader) {
    this.shader = shader;
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
