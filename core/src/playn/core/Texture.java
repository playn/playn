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

import playn.core.Graphics;
import playn.core.Scale;
import static playn.core.GL20.*;

/**
 * A handle to an OpenGL texture.
 */
public class Texture implements Disposable {

  // needed to access GL20 and to queue our destruction on finalize
  private final Graphics gfx;

  /** The GL texture handle. */
  public final int id;
  /** Whether or not this texture's lifecycle is automatically managed via reference counting. */
  public final boolean managed;
  /** Whether this texture has mipmaps generated. */
  public final boolean mipmaps;

  /** The width of this texture in pixels. */
  public final int pixelWidth;
  /** The height of this texture in pixels. */
  public final int pixelHeight;

  /** The scale factor to use when rendering this texture as a quad. */
  public final Scale scale;

  /** The width of this texture in display units. */
  public final float displayWidth;
  /** The height of this texture in display units. */
  public final float displayHeight;

  /** Returns whether this texture is configured to repeat in the X direction. */
  public boolean repeatX () {
    return repeatX;
  }
  /** Returns whether this texture is configured to repeat in the Y direction. */
  public boolean repeatY () {
    return repeatY;
  }

  /** Updates the repeat settings for this texture. */
  public void setRepeat (boolean repeatX, boolean repeatY) {
    if (this.repeatX != repeatX) {
      this.repeatX = repeatX;
      gfx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                             repeatX ? GL_REPEAT : GL_CLAMP_TO_EDGE);
    }
    if (this.repeatY != repeatY) {
      this.repeatY = repeatY;
      gfx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                             repeatY ? GL_REPEAT : GL_CLAMP_TO_EDGE);
    }
  }

  private boolean repeatX, repeatY;
  private int refs;
  private boolean destroyed;

  public Texture (Graphics gfx, int id, boolean managed, boolean mipmaps,
                  int pixWidth, int pixHeight, Scale scale, float dispWidth, float dispHeight) {
    this.gfx = gfx;
    this.id = id;
    this.managed = managed;
    this.mipmaps = mipmaps;
    this.scale = scale;
    this.pixelWidth = pixWidth;
    this.pixelHeight = pixHeight;
    this.displayWidth = dispWidth;
    this.displayHeight = dispHeight;
  }

  /** Increments this texture's reference count. NOOP unless {@link #managed}. */
  public void reference () {
    if (managed) refs++;
  }

  /** Decrements this texture's reference count. If the reference count of a managed texture goes
    * to zero, the texture is destroyed (and is no longer usable). */
  public void release () {
    if (managed) {
      assert refs > 0 : "Released a texture with no references!";
      if (--refs == 0) close();
    }
  }

  /** Returns whether this texture is been destroyed. */
  public boolean destroyed () {
    return destroyed;
  }

  /** Deletes this texture's GPU resources and renders it unusable. */
  @Override public void close () {
    if (!destroyed) {
      destroyed = true;
      gfx.gl.glDeleteTexture(id);
    }
  }

  protected void finalize () {
    // if we're not yet destroyed, queue ourselves up to be destroyed on the next frame tick
    if (!destroyed) gfx.queueForDestroy(this);
  }

  // TODO: put these somewhere...

  // imageregiongl stuffs

  // @Override
  // public void draw(GC gc, float dx, float dy, float dw, float dh) {
  //   draw(gc, dx, dy, dw, dh, 0, 0, width, height);
  // }

  // @Override
  // public void draw(GC gc, float dx, float dy, float dw, float dh,
  //                  float sx, float sy, float sw, float sh) {
  //   parent.draw(gc, dx, dy, dw, dh, x+sx, y+sy, sw, sh);
  // }

  // @Override
  // void draw(GLShader shader, InternalTransform xform, int tint,
  //           float dx, float dy, float dw, float dh, float sx, float sy, float sw, float sh) {
  //   if (repeatX || repeatY) {
  //     // if we're repeating, then we have our own texture and want to draw it normally
  //     super.draw(shader, xform, tint, dx, dy, dw, dh, sx, sy, sw, sh);
  //   } else {
  //     float texWidth = (tex > 0) ? width : parent.width();
  //     float texHeight = (tex > 0) ? height : parent.height();
  //     sx += x();
  //     sy += y();
  //     parent.drawImpl(shader, xform, ensureTexture(), tint, dx, dy, dw, dh,
  //                     sx / texWidth, sy / texHeight, (sx + sw) / texWidth, (sy + sh) / texHeight);
  //   }
  // }

  // private int scaleTexture() {
  //   int scaledWidth = scale().scaledCeil(this.width);
  //   int scaledHeight = scale().scaledCeil(this.height);

  //   // GL requires pow2 on axes that repeat
  //   int width = GLUtil.nextPowerOfTwo(scaledWidth), height = GLUtil.nextPowerOfTwo(scaledHeight);

  //   // width/height == 0 => already a power of two.
  //   if (width == 0)
  //     width = scaledWidth;
  //   if (height == 0)
  //     height = scaledHeight;

  //   // our source image is our parent's texture
  //   int tex = parent.ensureTexture();

  //   // create our texture and point a new framebuffer at it
  //   int reptex = ctx.createTexture(width, height, repeatX, repeatY, mipmapped);
  //   int fbuf = ctx.createFramebuffer(reptex);
  //   ctx.pushFramebuffer(fbuf, width, height);
  //   try {
  //     // render the parent texture into the framebuffer properly scaled
  //     ctx.clear(0, 0, 0, 0);
  //     float tw = parent.width(), th = parent.height();
  //     float sl = this.x, st = this.y, sr = sl + this.width, sb = st + this.height;
  //     GLShader shader = ctx.quadShader(null).prepareTexture(tex, Tint.NOOP_TINT);
  //     shader.addQuad(ctx.createTransform(), 0, height, width, 0,
  //                    sl / tw, st / th, sr / tw, sb / th);
  //     shader.flush();
  //     // if we're mipmapped, we can now generate our mipmaps
  //     if (mipmapped) ctx.generateMipmap(reptex);
  //     return reptex;

  //   } finally {
  //     // we no longer need this framebuffer; rebind the previous framebuffer and delete ours
  //     ctx.popFramebuffer();
  //     ctx.deleteFramebuffer(fbuf);
  //   }
  // }
}
