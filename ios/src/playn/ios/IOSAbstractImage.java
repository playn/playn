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

import cli.OpenTK.Graphics.ES20.All;
import cli.OpenTK.Graphics.ES20.GL;

import playn.core.Image;
import playn.core.ResourceCallback;
import playn.core.StockInternalTransform;
import playn.core.gl.GLContext;
import playn.core.gl.GLUtil;
import playn.core.gl.ImageGL;

/**
 * Provides some shared bits for {@link IOSImage} and {@link IOSCanvasImage}.
 */
abstract class IOSAbstractImage extends ImageGL implements Image
{
  protected final IOSGLContext ctx;
  protected int tex = -1, reptex = -1;

  /**
   * Uploads our image data into the supplied texture id.
   */
  protected abstract void updateTexture(int tex);

  @Override
  public boolean isReady() {
    return true; // we're always ready
  }

  @Override
  public void addCallback(ResourceCallback<Image> callback) {
    callback.done(this); // we're always ready
  }

  @Override
  public Object ensureTexture(GLContext ctx, boolean repeatX, boolean repeatY) {
    if (repeatX || repeatY) {
      ensureScaledTexture(repeatX, repeatY);
      return reptex;
    } else {
      ensureTexture();
      return tex;
    }
  }

  @Override
  public void clearTexture(GLContext ctx) {
    if (tex != -1) {
      ctx.destroyTexture(tex);
      tex = -1;
    }
    if (reptex != -1) {
      ctx.destroyTexture(reptex);
      reptex = -1;
    }
  }

  @Override
  protected void finalize() {
    if (tex != -1)
      ctx.queueDestroyTexture(tex);
    if (reptex != -1)
      ctx.queueDeleteFramebuffer(reptex);
  }

  protected IOSAbstractImage(IOSGLContext ctx) {
    this.ctx = ctx;
  }

  private void ensureTexture() {
    if (tex != -1)
      return;
    tex = ctx.createTexture(false, false);
    updateTexture(tex);
  }

  private void ensureScaledTexture(boolean repeatX, boolean repeatY) {
    if (reptex != -1)
      return;

    // TODO: if width/height > 1024, repeatedly scale by 0.5 until within bounds

    // GL requires pow2 on axes that repeat
    int width = GLUtil.nextPowerOfTwo(width()), height = GLUtil.nextPowerOfTwo(height());
    reptex = ctx.createTexture(width, height, repeatX, repeatY);

    // no need to scale if our source data is already a power of two
    if ((width == 0) && (height == 0)) {
      updateTexture(reptex);
      return;
    }

    // otherwise we need to scale our non-repeated texture, which we'll load normally
    ensureTexture();

    // width/height == 0 => already a power of two.
    if (width == 0)
      width = width();
    if (height == 0)
      height = height();

    // point a new framebuffer at it
    int fbuf = ctx.createFramebuffer(reptex);
    GL.BindTexture(All.wrap(All.Texture2D), reptex);
    ctx.clear(0, 0, 0, 0);

    // render the non-repeated texture into the framebuffer properly scaled
    ctx.drawTexture(tex, width(), height(), StockInternalTransform.IDENTITY,
                    0, height, width, -height, false, false, 1);
    ctx.bindFramebuffer();

    ctx.deleteFramebuffer(fbuf);
  }
}
