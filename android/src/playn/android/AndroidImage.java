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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

import playn.core.*;
import playn.core.gl.GL20;
import playn.core.gl.GLContext;
import playn.core.gl.GLUtil;
import playn.core.gl.ImageGL;

import static playn.core.PlayN.log;

/**
 * Android implementation of CanvasImage class. Prioritizes the SoftReference to
 * the bitmap, and only holds a hard reference if the game has requested that a
 * Canvas be created.
 */
class AndroidImage implements CanvasImage, ImageGL, AndroidGLContext.Refreshable {
  private AndroidGLContext ctx;
  private SoftReference<Bitmap> bitmapRef;
  private AndroidCanvas canvas;
  private Bitmap canvasBitmap;
  private List<ResourceCallback<Image>> callbacks = new ArrayList<ResourceCallback<Image>>();
  private int width, height;
  private String path;
  private int tex = -1, pow2tex = -1;

  AndroidImage(AndroidGLContext ctx, String path, Bitmap bitmap) {
    this(ctx, bitmap.getWidth(), bitmap.getHeight());
    this.path = path;
    // Use a soft reference if we have a path to restore the bitmap from.
    bitmapRef = new SoftReference<Bitmap>(bitmap);
  }

  AndroidImage(AndroidGLContext ctx, int width, int height, boolean alpha) {
    this(ctx, width, height);
    // TODO: Why not always use the preferredBitmapConfig?  (Preserved from pre-GL code)
    canvasBitmap = Bitmap.createBitmap(
      width, height, alpha ? AndroidPlatform.instance.preferredBitmapConfig :
      Bitmap.Config.ARGB_8888);
  }

  private AndroidImage(AndroidGLContext ctx, int width, int height) {
    this.ctx = ctx;
    this.width = width;
    this.height = height;
    ctx.addRefreshable(this);
  }

  @Override
  public void addCallback(ResourceCallback<Image> callback) {
    callbacks.add(callback);
    if (isReady()) {
      runCallbacks(true);
    }
  }

  @Override
  public Canvas canvas() {
    if (canvas == null) {
      canvasBitmap = getBitmap();
      if (canvasBitmap != null) {
        canvas = new AndroidCanvas(canvasBitmap);
      } else {
        canvas = new AndroidCanvas(width, height);
      }
    }
    bitmapRef = null;
    return canvas;
  }

  @Override
  public void onSurfaceCreated() {
  }

  @Override
  public void onSurfaceLost() {
    clearTexture();
  }

  boolean canvasDirty() {
    return (canvas != null && canvas.dirty());
  }

  void clearDirty() {
    canvas.clearDirty();
  }

  public void destroy() {
    ctx.removeRefreshable(this);
    clearTexture();
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  public boolean isReady() {
    return bitmapRef != null || canvas != null;
  }

  /*
   * getBitmap() can be exceptionally slow, as it will likely need to retrieve the bitmap from the
   * file path. As such it should only be called when a direct reference to the Bitmap is
   * necessary. (Note that this is less of an issue for an AndroidImage that has had a canvas
   * built, as a hard reference to the bitmap is held in memory then).
   */
  Bitmap getBitmap() {
    if (canvasBitmap != null) {
      return canvasBitmap;
    }
    if (bitmapRef != null) {
      Bitmap bm = bitmapRef.get();
      if (bm == null && path != null) {
        if (AndroidPlatform.DEBUG_LOGS) log().debug("Bitmap " + path + " fell out of memory");
        bitmapRef = new SoftReference<Bitmap>(
            bm = AndroidPlatform.instance.assetManager().doGetBitmap(path));
      }
      return bm;
    }
    return null;
  }

  String getPath() {
    return path;
  }

  private void runCallbacks(boolean success) {
    for (ResourceCallback<Image> cb : callbacks) {
      if (success) {
        cb.done(this);
      } else {
        cb.error(new Exception("Error loading image"));
      }
    }
    callbacks.clear();
  }

  /*
   * Clears textures associated with this image. This does not destroy the image
   * -- a subsequent call to ensureTexture() will recreate them.
   */
  void clearTexture() {
    if (pow2tex == tex) {
      pow2tex = -1;
    }
    if (tex != -1) {
      ctx.destroyTexture(tex);
      tex = -1;
    }
    if (pow2tex != -1) {
      ctx.destroyTexture(pow2tex);
      pow2tex = -1;
    }
  }

  @Override
  public Object ensureTexture(GLContext ctx, boolean repeatX, boolean repeatY) {
    // Create requested textures if loaded.
    if (canvasDirty()) {
      // Force texture refresh
      if (canvas != null) clearDirty();
      clearTexture();
    }
    if (isReady()) {
      if (repeatX || repeatY) {
        scaleTexture((AndroidGLContext) ctx, repeatX, repeatY);
        return pow2tex;
      } else {
        loadTexture((AndroidGLContext) ctx);
        return tex;
      }
    }
    log().error("Image not ready to draw -- cannot ensure texture.");
    return null;
  }

  /*
   * Called from ensureTexture() and scaleTexture()
   */
  private void loadTexture(AndroidGLContext ctx) {
    if (tex != -1 && ctx.gl20.glIsTexture((Integer) tex))
      return;
    tex = (Integer) ctx.createTexture(false, false);
    ctx.updateTexture(tex, getBitmap());
  }

  /*
   * Creates a pow2 texture for repeating images. Called from ensureTexture()
   */
  private void scaleTexture(AndroidGLContext ctx, boolean repeatX, boolean repeatY) {
    // Ensure that 'tex' is loaded. We use it below.
    loadTexture(ctx);

    if (pow2tex != -1 && ctx.gl20.glIsTexture(pow2tex))
      return;

    // GL requires pow2 on axes that repeat.
    int width = GLUtil.nextPowerOfTwo(width()), height = GLUtil.nextPowerOfTwo(height());

    // Don't scale if it's already a power of two.
    if ((width == 0) && (height == 0)) {
      pow2tex = tex;
      return;
    }

    // width/height == 0 => already a power of two.
    if (width == 0) {
      width = width();
    }
    if (height == 0) {
      height = height();
    }

    // TODO: Throw error if the size is bigger than GL_MAX_RENDERBUFFER_SIZE?

    // Create the pow2 texture.
    pow2tex = (Integer) ctx.createTexture(width, height, repeatX, repeatY);

    // Point a new framebuffer at it.
    int fbuf = (Integer) ctx.createFramebuffer(pow2tex);

    // Render the scaled texture into the framebuffer. (rebind the texture because
    // ctx.bindFramebuffer() may have unbound it when flushing)
    ctx.gl20.glBindTexture(GL20.GL_TEXTURE_2D, pow2tex);
    ctx.clear(0, 0, 0, 0);

    ctx.drawTexture(tex, width(), height(), StockInternalTransform.IDENTITY,
                    0, height, width, -height, false, false, 1);
    ctx.flush();
    ctx.bindFramebuffer();

    ctx.deleteFramebuffer(fbuf);
  }

  @Override
  protected void finalize() {
    if (pow2tex == tex)
      pow2tex = -1;
    if (tex != -1)
      ctx.queueDestroyTexture(tex);
    if (pow2tex != -1)
      ctx.queueDeleteFramebuffer(pow2tex);
  }
}
