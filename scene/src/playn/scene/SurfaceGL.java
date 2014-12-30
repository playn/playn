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

import playn.core.InternalTransform;
import playn.core.Tint;
import playn.core.gl.GLShader;

public class SurfaceGL extends AbstractSurfaceGL {

  protected final float width, height;
  protected final int texWidth, texHeight;
  protected int tex, fbuf;

  public SurfaceGL(GLContext ctx, float width, float height) {
    super(ctx);
    this.width = width;
    this.height = height;
    this.texWidth = ctx.scale.scaledCeil(width);
    this.texHeight = ctx.scale.scaledCeil(height);
    createTexture();
    scale(ctx.scale.factor, ctx.scale.factor);
  }

  public void destroy() {
    clearTexture();
  }

  @Override
  public float width() {
    return width;
  }

  @Override
  public float height() {
    return height;
  }

  void draw(GLShader shader, InternalTransform xform, int curTint) {
    if (tint != Tint.NOOP_TINT)
      curTint = Tint.combine(curTint, tint);
    // Draw this layer to the screen upside-down, because its contents are flipped (This happens
    // because it uses the same vertex program as everything else, which flips vertically to put
    // the origin at the top-left).
    ctx.quadShader(shader).prepareTexture(tex, curTint).addQuad(
      xform, 0, height, width, 0, 0, 0, 1, 1);
  }

  protected void createTexture() {
    tex = ctx.createTexture(texWidth, texHeight, false, false, false);
    fbuf = ctx.createFramebuffer(tex);
    ctx.bindFramebuffer(fbuf, texWidth, texHeight);
    ctx.clear(0, 0, 0, 0);
  }

  protected void clearTexture() {
    ctx.destroyTexture(tex);
    tex = 0;
    ctx.deleteFramebuffer(fbuf);
    fbuf = 0;
  }

  @Override
  protected void bindFramebuffer() {
    ctx.bindFramebuffer(fbuf, texWidth, texHeight);
  }

  @Override
  protected void finalize() throws Throwable {
    // if we weren't destroyed earlier, queue up destruction of our texture and framebuffer to be
    // undertaken on the main OpenGL thread on the next frame
    if (tex > 0)
      ctx.queueDestroyTexture(tex);
    if (fbuf > 0)
      ctx.queueDeleteFramebuffer(fbuf);
  }
}
