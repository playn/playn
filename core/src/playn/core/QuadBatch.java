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
package playn.core;

import pythagoras.f.AffineTransform;

/**
 * A batch which can render textured quads. Since that's a pretty common thing to do, we factor out
 * this API, and allow for different implementations thereof.
 */
public abstract class QuadBatch extends GLBatch {

  public final GL20 gl;
  protected int curTexId;

  /** Prepares this batch to render quads using the supplied texture. If pending quads have been
    * added to this batch for a different texture, this call will trigger a {@link #flush}.
    * <p>Note: if you call {@code add} methods that take a texture, you do not need to call this
    * method manually. Only if you're adding bare quads is it needed. */
  public void setTexture (Texture texture) {
    if (curTexId != 0 && curTexId != texture.id) flush();
    this.curTexId = texture.id;
  }

  /** Adds {@code tex} as a transformed axis-aligned quad to this batch.
    * {@code x, y, w, h} define the size and position of the quad. */
  public void add (Texture tex, int tint, AffineTransform xf, float x, float y, float w, float h) {
    setTexture(tex);
    float sr = tex.repeatX() ? w/tex.displayWidth : 1, sb = tex.repeatY() ? h/tex.displayHeight : 1;
    add(tint, xf, x, y, x+w, y+h, 0, 0, sr, sb);
  }

  /** Adds {@code tex} as a transformed axis-aligned quad to this batch.
    * {@code dx, dy, dw, dh} define the size and position of the quad.
    * {@code sx, sy, sw, sh} define region of the texture which will be displayed in the quad. */
  public void add (Texture tex, int tint, AffineTransform xf,
                   float dx, float dy, float dw, float dh,
                   float sx, float sy, float sw, float sh) {
    setTexture(tex);
    float texWidth = tex.displayWidth, texHeight = tex.displayHeight;
    add(tint, xf, dx, dy, dx+dw, dy+dh,
        sx/texWidth, sy/texHeight, (sx+sw)/texWidth, (sy+sh)/texHeight);
  }

  /** Adds a transformed axis-aligned quad to this batch.
    * {@code left, top, right, bottom} define the bounds of the quad.
    * {@code sl, st, sr, sb} define the texture coordinates. */
  public void add (int tint, AffineTransform xf,
                   float left, float top, float right, float bottom,
                   float sl, float st, float sr, float sb) {
    add(tint, xf.m00, xf.m01, xf.m10, xf.m11, xf.tx, xf.ty,
        left, top, right, bottom, sl, st, sr, sb);
  }

  /** Adds a transformed axis-aligned quad to this batch.
    * {@code m00, m01, m10, m11, tx, ty} define the affine transform applied to the quad.
    * {@code left, top, right, bottom} define the bounds of the quad.
    * {@code sl, st, sr, sb} define the texture coordinates. */
  public void add (int tint, float m00, float m01, float m10, float m11, float tx, float ty,
                   float left, float top, float right, float bottom,
                   float sl, float st, float sr, float sb) {
    add(tint, m00, m01, m10, m11, tx, ty,
        left,  top,    sl, st,
        right, top,    sr, st,
        left,  bottom, sl, sb,
        right, bottom, sr, sb);
  }

  /** Adds a transformed axis-aligned quad to this batch.
    * {@code m00, m01, m10, m11, tx, ty} define the affine transform applied to the quad.
    * {@code x1, y1, .., x4, y4} define the corners of the quad.
    * {@code sx1, sy1, .., sx4, sy4} define the texture coordinate of the quad. */
  public abstract void add (int tint,
                            float m00, float m01, float m10, float m11, float tx, float ty,
                            float x1, float y1, float sx1, float sy1,
                            float x2, float y2, float sx2, float sy2,
                            float x3, float y3, float sx3, float sy3,
                            float x4, float y4, float sx4, float sy4);

  protected QuadBatch (GL20 gl) {
    this.gl = gl;
  }

  /** Binds our current texture. Subclasses need to call this in {@link #flush}. */
  protected void bindTexture () {
    gl.glBindTexture(GL20.GL_TEXTURE_2D, curTexId);
    gl.checkError("QuadBatch glBindTexture");
  }
}
