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

import playn.core.Tint;

public abstract class ImageGL<GC> extends AbstractImageGL<GC> {

  /** This image's scale factor. This is effectively final, but can't be marked final because it
   * can be updated post-construction due to asynchronous image loading. */
  protected Scale scale;

  /** Our texture handle. */
  protected int tex;

  @Override
  public Scale scale() {
    return scale;
  }

  @Override
  public int ensureTexture() {
    if (tex > 0) {
      return tex;
    } else if (!isReady()) {
      return 0;
    } else if (repeatX || repeatY || mipmapped) {
      return (tex = scaleTexture());
    } else {
      return (tex = createMainTex());
    }
  }

  @Override
  public void clearTexture() {
    if (tex > 0) {
      ctx.destroyTexture(tex);
      tex = 0;
    }
  }

  protected ImageGL(GLContext ctx, Scale scale) {
    super(ctx);
    this.scale = scale;
  }

  /**
   * Copies our current image data into the supplied texture.
   */
  protected abstract void updateTexture(int tex);

  @Override
  protected void finalize() {
    if (tex > 0)
      ctx.queueDestroyTexture(tex);
  }

  /**
   * Creates and populates a (not necessarily power of two) texture for use as our main texture.
   */
  protected int createMainTex() {
    // the mipmaps flag is always false here because we only ever generate mipmaps for our
    // power-of-two textures; scaleTexture will use tex to create the POT texture, so tex should
    // not have mipmaps enabled, or it will hose up that process
    int tex = ctx.createTexture(false, false, false);
    updateTexture(tex);
    return tex;
  }

  /**
   * Creates and populates a texture for use as our power-of-two texture. This is used when our
   * main image data is already power-of-two-sized.
   */
  protected int createPow2RepTex(int width, int height, boolean repeatX, boolean repeatY,
                                 boolean mipmapped) {
    int powtex = ctx.createTexture(width, height, repeatX, repeatY, mipmapped);
    updateTexture(powtex);
    return powtex;
  }

  /**
   * Called by canvas image implementations in {@link #ensureTexture} to either cause their texture
   * data to be reuploaded (in the simple case where the image is neither repeated nor mipmapped),
   * or their texture to be destroyed so that it is subsequently recreated with updated texture
   * data.
   */
  protected void refreshTexture() {
    if (repeatX || repeatY || mipmapped) clearTexture();
    else if (tex > 0) updateTexture(tex);
  }

  private int scaleTexture() {
    int scaledWidth = scale.scaledCeil(width());
    int scaledHeight = scale.scaledCeil(height());

    // GL requires pow2 on axes that repeat
    int width = GLUtil.nextPowerOfTwo(scaledWidth), height = GLUtil.nextPowerOfTwo(scaledHeight);

    // TODO: if width/height > platform_max_size, repeatedly scale by 0.5 until within bounds
    // platform_max_size = 1024 for iOS, GL10.GL_MAX_TEXTURE_SIZE on android, etc.

    // no need to scale if our source data is already a power of two
    if ((width == 0) && (height == 0)) {
      int reptex = createPow2RepTex(scaledWidth, scaledHeight, repeatX, repeatY, mipmapped);
      if (mipmapped) ctx.generateMipmap(reptex);
      return reptex;
    }

    // otherwise we need to scale our non-repeated texture, so load that normally
    int tex = createMainTex();

    // width/height == 0 => already a power of two.
    if (width == 0)
      width = scaledWidth;
    if (height == 0)
      height = scaledHeight;

    // create our texture and point a new framebuffer at it
    try {
      return convertToRepTex(ctx, tex, width, height, repeatX, repeatY, mipmapped);
    } finally {
      // delete the non-repeated texture
      ctx.destroyTexture(tex);
    }
  }

  protected static int convertToRepTex(GLContext ctx, int tex, int width, int height,
                                       boolean repeatX, boolean repeatY, boolean mipmapped) {
    int reptex = ctx.createTexture(width, height, repeatX, repeatY, mipmapped);
    int fbuf = ctx.createFramebuffer(reptex);
    ctx.pushFramebuffer(fbuf, width, height);
    try {
      // render the non-repeated texture into the framebuffer properly scaled
      ctx.clear(0, 0, 0, 0);
      GLShader shader = ctx.quadShader(null).prepareTexture(tex, Tint.NOOP_TINT);
      shader.addQuad(ctx.createTransform(), 0, height, width, 0, 0, 0, 1, 1);
      shader.flush();
      // if we're mipmapped, we can now generate our mipmaps
      if (mipmapped) ctx.generateMipmap(reptex);
      return reptex;

    } finally {
      // we no longer need this framebuffer; rebind the previous framebuffer and delete ours
      ctx.popFramebuffer();
      ctx.deleteFramebuffer(fbuf);
    }
  }
}
