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
package playn.android;

import android.graphics.Bitmap;

import playn.core.*;
import playn.core.gl.GL20;
import playn.core.gl.GLContext;
import playn.core.gl.GLUtil;
import playn.core.gl.ImageGL;

import static playn.core.PlayN.log;

class AndroidImage extends ImageGL implements AndroidGLContext.Refreshable {
  private final AndroidGLContext ctx;
  private final Bitmap bitmap;
  private int tex = -1, reptex = -1;

  AndroidImage(AndroidGLContext ctx, Bitmap bitmap) {
    this.ctx = ctx;
    this.bitmap = bitmap;
    ctx.addRefreshable(this);
  }

  @Override
  public void addCallback(ResourceCallback<Image> callback) {
    // we're always ready immediately
    callback.done(this);
  }

  @Override
  public void onSurfaceCreated() {
  }

  @Override
  public void onSurfaceLost() {
    clearTexture();
  }

  public void destroy() {
    ctx.removeRefreshable(this);
    clearTexture();
  }

  @Override
  public int height() {
    return bitmap.getHeight();
  }

  @Override
  public int width() {
    return bitmap.getWidth();
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public Object ensureTexture(GLContext ctx, boolean repeatX, boolean repeatY) {
    if (isReady()) {
      if (repeatX || repeatY) {
        scaleTexture((AndroidGLContext) ctx, repeatX, repeatY);
        return reptex;
      } else {
        loadTexture((AndroidGLContext) ctx);
        return tex;
      }
    }
    log().error("Image not ready to draw -- cannot ensure texture.");
    return null;
  }

  @Override
  public void clearTexture(GLContext ctx) {
    clearTexture(); // we don't need the ctx arg
  }

  Bitmap bitmap() {
    return bitmap;
  }

  @Override
  protected void finalize() {
    if (tex != -1)
      ctx.queueDestroyTexture(tex);
    if (reptex != -1)
      ctx.queueDeleteFramebuffer(reptex);
  }

  private void clearTexture() {
    if (tex != -1) {
      ctx.destroyTexture(tex);
      tex = -1;
    }
    if (reptex != -1) {
      ctx.destroyTexture(reptex);
      reptex = -1;
    }
  }

  private void loadTexture(AndroidGLContext ctx) {
    if (tex != -1)
      return;
    tex = ctx.createTexture(false, false);
    ctx.updateTexture(tex, bitmap);
  }

  private void scaleTexture(AndroidGLContext ctx, boolean repeatX, boolean repeatY) {
    if (reptex != -1)
      return;

    // GL requires pow2 on axes that repeat
    int width = GLUtil.nextPowerOfTwo(width()), height = GLUtil.nextPowerOfTwo(height());
    reptex = ctx.createTexture(width, height, repeatX, repeatY);

    // no need to scale if our source data is already a power of two
    if ((width == 0) && (height == 0)) {
      ctx.updateTexture(reptex, bitmap);
      return;
    }

    // otherwise we need to scale our non-repeated texture, which we'll load normally
    loadTexture(ctx);

    // width/height == 0 => already a power of two.
    if (width == 0)
      width = width();
    if (height == 0)
      height = height();

    // TODO: Throw error if the size is bigger than GL_MAX_RENDERBUFFER_SIZE?

    // point a new framebuffer at it
    int fbuf = ctx.createFramebuffer(reptex);
    ctx.gl20.glBindTexture(GL20.GL_TEXTURE_2D, reptex);
    ctx.clear(0, 0, 0, 0);

    // render the non-repeated texture into the framebuffer properly scaled
    ctx.drawTexture(tex, width(), height(), StockInternalTransform.IDENTITY,
                    0, height, width, -height, false, false, 1);
    ctx.bindFramebuffer();

    ctx.deleteFramebuffer(fbuf);
  }
}
