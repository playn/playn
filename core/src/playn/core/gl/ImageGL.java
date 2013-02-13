/**
 * Copyright 2010-2012 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.core.gl;

import playn.core.Asserts;
import playn.core.Image;
import playn.core.InternalTransform;
import playn.core.Pattern;
import playn.core.Tint;

public abstract class ImageGL implements Image {

  protected final GLContext ctx;

  /** This image's scale factor. This is effectively final, but can't be marked final because it
   * can be updated post-construction due to asynchronous image loading. */
  protected Scale scale;

  /** The current count of references to this image. */
  protected int refs;

  /** Our texture and repeatable texture handles. */
  protected int tex, reptex;

  /**
   * Creates a texture for this image (if one does not already exist) and returns it. May return
   * 0 if the underlying image data is not yet ready.
   */
  public int ensureTexture(boolean repeatX, boolean repeatY) {
    if (!isReady()) {
      return 0;
    } else if (repeatX || repeatY) {
      scaleTexture(repeatX, repeatY);
      return reptex;
    } else {
      loadTexture();
      return tex;
    }
  }

  /**
   * Releases this image's texture memory.
   */
  public void clearTexture() {
    if (tex > 0) {
      ctx.destroyTexture(tex);
      tex = 0;
    }
    if (reptex > 0) {
      ctx.destroyTexture(reptex);
      reptex = 0;
    }
  }

  /**
   * Increments this image's reference count. Called by {@link ImageLayerGL} to let the image know
   * that it's part of the scene graph. Note that this reference counting mechanism only exists to
   * make more efficient use of texture memory. Images are also used by things like {@link Pattern}
   * which does not support reference counting, thus images must also provide some fallback
   * mechanism for releasing their texture when no longer needed (like in their finalizer).
   */
  public void reference() {
    refs++; // we still create our texture on demand
  }

  /**
   * Decrements this image's reference count. Called by {@link ImageLayerGL} to let the image know
   * that may no longer be part of the scene graph.
   */
  public void release() {
    Asserts.checkState(refs > 0, "Released an image with no references!");
    if (--refs == 0) {
      clearTexture();
    }
  }

  /**
   * Draws this image with the supplied transform in the specified target dimensions.
   */
  void draw(GLShader shader, InternalTransform xform, float dx, float dy, float dw, float dh,
            boolean repeatX, boolean repeatY, int tint) {
    int tex = ensureTexture(repeatX, repeatY);
    if (tex > 0) {
      float sl = x(), st = y();
      float sr = sl + (repeatX ? dw : width()), sb = st + (repeatY ? dh : height());
      float texWidth = texWidth(repeatX), texHeight = texHeight(repeatY);
      ctx.quadShader(shader).prepareTexture(tex, tint).addQuad(
        xform, dx, dy, dx + dw, dy + dh,
        sl / texWidth, st / texHeight, sr / texWidth, sb / texHeight);
    }
  }

  /**
   * Draws this image with the supplied transform, and source and target dimensions.
   */
  void draw(GLShader shader, InternalTransform xform, float dx, float dy, float dw, float dh,
            float sx, float sy, float sw, float sh, int tint) {
    int tex = ensureTexture(false, false);
    if (tex > 0) {
      sx += x(); sy += y();
      float texWidth = texWidth(false), texHeight = texHeight(false);
      ctx.quadShader(shader).prepareTexture(tex, tint).addQuad(
        xform, dx, dy, dx + dw, dy + dh,
        sx / texWidth, sy / texHeight, (sx + sw) / texWidth, (sy + sh) / texHeight);
    }
  }

  protected ImageGL(GLContext ctx, Scale scale) {
    this.ctx = ctx;
    this.scale = scale;
  }

  /**
   * The x offset into our source image at which this image's region starts.
   */
  protected float x() {
    return 0;
  }

  /**
   * The y offset into our source image at which this image's region starts.
   */
  protected float y() {
    return 0;
  }

  /**
   * Returns the width of our underlying texture image.
   */
  protected float texWidth(boolean repeatX) {
    return width();
  }

  /**
   * Returns the height of our underlying texture image.
   */
  protected float texHeight(boolean repeatY) {
    return height();
  }

  /**
   * Copies our current image data into the supplied texture.
   */
  protected abstract void updateTexture(int tex);

  @Override
  protected void finalize() {
    if (tex > 0)
      ctx.queueDestroyTexture(tex);
    if (reptex > 0)
      ctx.queueDeleteFramebuffer(reptex);
  }

  private void loadTexture() {
    if (tex > 0)
      return;
    tex = ctx.createTexture(false, false);
    updateTexture(tex);
  }

  private void scaleTexture(boolean repeatX, boolean repeatY) {
    if (reptex > 0)
      return;

    int scaledWidth = scale.scaledCeil(width());
    int scaledHeight = scale.scaledCeil(height());

    // GL requires pow2 on axes that repeat
    int width = GLUtil.nextPowerOfTwo(scaledWidth), height = GLUtil.nextPowerOfTwo(scaledHeight);

    // TODO: if width/height > platform_max_size, repeatedly scale by 0.5 until within bounds
    // platform_max_size = 1024 for iOS, GL10.GL_MAX_TEXTURE_SIZE on android, etc.

    // no need to scale if our source data is already a power of two
    if ((width == 0) && (height == 0)) {
      reptex = ctx.createTexture(scaledWidth, scaledHeight, repeatX, repeatY);
      updateTexture(reptex);
      return;
    }

    // otherwise we need to scale our non-repeated texture, so load that normally
    loadTexture();

    // width/height == 0 => already a power of two.
    if (width == 0)
      width = scaledWidth;
    if (height == 0)
      height = scaledHeight;

    // create our texture and point a new framebuffer at it
    reptex = ctx.createTexture(width, height, repeatX, repeatY);
    int fbuf = ctx.createFramebuffer(reptex);
    ctx.pushFramebuffer(fbuf, width, height);
    try {
      // render the non-repeated texture into the framebuffer properly scaled
      ctx.clear(0, 0, 0, 0);
      ctx.quadShader(null).prepareTexture(tex, Tint.NOOP_TINT).addQuad(
        ctx.createTransform(), 0, height, width, 0, 0, 0, 1, 1);
    } finally {
      // we no longer need this framebuffer; rebind the previous framebuffer and delete ours
      ctx.popFramebuffer();
      ctx.deleteFramebuffer(fbuf);
    }
  }
}
