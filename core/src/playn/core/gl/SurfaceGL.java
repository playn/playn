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

public class SurfaceGL extends AbstractSurfaceGL {

  protected final int width, height;
  protected final int texWidth, texHeight;
  protected Object tex, fbuf;

  protected SurfaceGL(GLContext ctx, int width, int height) {
    super(ctx);
    this.width = width;
    this.height = height;
    this.texWidth = ctx.scaledCeil(width);
    this.texHeight = ctx.scaledCeil(height);
    createTexture();
    scale(ctx.scaleFactor, ctx.scaleFactor);
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  public int height() {
    return height;
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
    tex = ctx.createTexture(texWidth, texHeight, false, false);
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
  protected void bindFramebuffer() {
    ctx.bindFramebuffer(fbuf, texWidth, texHeight);
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
}
