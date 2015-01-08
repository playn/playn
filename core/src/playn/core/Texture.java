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

  /** Used to configure texture at creation time. */
  public final static class Config {

    /** Default managed texture configuration: managed, no mipmaps, no repat, linear filters. */
    public static Config DEFAULT = new Config(true, false, false, GL_LINEAR, GL_LINEAR, false);
    /** Default unmanaged texture configuration: unmanaged, no mipmaps, no repat, linear filters. */
    public static Config UNMANAGED = new Config(false, false, false, GL_LINEAR, GL_LINEAR, false);

    /** Whether or not texture's lifecycle is automatically managed via reference counting. If the
      * texture will be used in an {@code ImageLayer}, it should be reference counted unless you
      * are doing something special. Otherwise you can decide whether you want to use the reference
      * counting mechanism or not. */
    public final boolean managed;

    /** Whether texture is configured to repeat in this direction. */
    public final boolean repeatX, repeatY;

    /** The filter to use when this texture is scaled: {@code GL_LINEAR} or {@code GL_NEAREST}. */
    public final int minFilter, magFilter;

    /** Whether texture has mipmaps generated. */
    public final boolean mipmaps;

    public Config (boolean managed, boolean repeatX, boolean repeatY,
                   int minFilter, int magFilter, boolean mipmaps) {
      this.managed = managed;
      this.repeatX = repeatX;
      this.repeatY = repeatY;
      this.minFilter = minFilter;
      this.magFilter = magFilter;
      this.mipmaps = mipmaps;
    }

    /** Returns a copy of this config with {@code repeatX}, {@code repeatY} set as specified. */
    public Config repeat (boolean repeatX, boolean repeatY) {
      return new Config(managed, repeatX, repeatY, minFilter, magFilter, mipmaps);
    }

    /** Returns {@code sourceWidth} rounded up to a POT if necessary. */
    public int toTexWidth (int sourceWidth) {
      return (repeatX || mipmaps) ? nextPOT(sourceWidth) : sourceWidth;
    }
    /** Returns {@code sourceHeight} rounded up to a POT if necessary. */
    public int toTexHeight (int sourceHeight) {
      return (repeatY || mipmaps) ? nextPOT(sourceHeight) : sourceHeight;
    }

    @Override public String toString () {
      String repstr = (repeatX ? "x" : "") + (repeatY ? "y" : "");
      return "[managed=" + managed + ", repeat=" + repstr +
        ", filter=" + minFilter + "/" + magFilter + ", mipmaps=" + mipmaps + "]";
    }
  }

  /**
   * Returns next largest power of two, or {@code value} if {@code value} is already a POT. Note:
   * this is limited to values less than {@code 0x10000}.
   */
  public static int nextPOT (int value) {
    assert value < 0x10000;
    int bit = 0x8000, highest = -1, count = 0;
    for (int ii = 15; ii >= 0; ii--, bit >>= 1) {
      if ((value & bit) == 0) continue;
      count++;
      if (highest == -1) highest = ii;
    }
    return (count > 1) ? (1 << (highest+1)) : value;
  }

  /** The GL texture handle. */
  public final int id;
  /** This texture's configuration. */
  public final Config config;

  /** The width of this texture in pixels. */
  public final int pixelWidth;
  /** The height of this texture in pixels. */
  public final int pixelHeight;

  /** The scale factor used by this texture. */
  public final Scale scale;
  /** The width of this texture in display units. */
  public final float displayWidth;
  /** The height of this texture in display units. */
  public final float displayHeight;

  // needed to access GL20 and to queue our destruction on finalize
  private final Graphics gfx;
  private int refs;
  private boolean destroyed;

  public Texture (Graphics gfx, int id, Config config, int pixWidth, int pixHeight,
                  Scale scale, float dispWidth, float dispHeight) {
    this.gfx = gfx;
    this.id = id;
    this.config = config;
    this.pixelWidth = pixWidth;
    this.pixelHeight = pixHeight;
    this.scale = scale;
    this.displayWidth = dispWidth;
    this.displayHeight = dispHeight;
  }

  /** Increments this texture's reference count. NOOP unless {@link #managed}. */
  public void reference () {
    if (config.managed) refs++;
  }

  /** Decrements this texture's reference count. If the reference count of a managed texture goes
    * to zero, the texture is destroyed (and is no longer usable). */
  public void release () {
    if (config.managed) {
      assert refs > 0 : "Released a texture with no references!";
      if (--refs == 0) close();
    }
  }

  /** Uploads {@code image} to this texture's GPU memory. {@code image} must have the exact same
    * size as this texture and must be fully loaded. This is generally useful for updating a
    * texture which was created from a canvas when the canvas has been changed. */
  public void update (Image image) {
    // if we're a repeating texture (or we want mipmaps) and this image is non-POT on the relevant
    // axes, we need to scale it before we upload it; we'll just do this on the CPU since it feels
    // like creating a second texture, a frame buffer to render into it, sending a GPU batch and
    // doing all the blah blah blah is going to be more expensive overall
    if (config.repeatX || config.repeatY || config.mipmaps) {
      int pixWidth = image.pixelWidth(), pixHeight = image.pixelHeight();
      int potWidth = config.toTexWidth(pixWidth), potHeight = config.toTexWidth(pixHeight);
      if (potWidth != pixWidth || potHeight != pixHeight) {
        Canvas scaled = gfx.createCanvasImpl(Scale.ONE, potWidth, potHeight);
        scaled.drawImage(image, 0, 0, potWidth, potHeight);
        scaled.image.upload(gfx, this);
        scaled.dispose();
      } else image.upload(gfx, this); // fast path, woo!
    }
    else image.upload(gfx, this); // fast path, woo!
    if (config.mipmaps) gfx.gl.glGenerateMipmap(GL_TEXTURE_2D);
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

  @Override public String toString () {
    return "Texture[id=" + id + ", psize=" + pixelWidth + "x" + pixelHeight +
      ", dsize=" + displayWidth + "x" + displayHeight + " @ " + scale + ", config=" + config + "]";
  }

  protected void finalize () {
    // if we're not yet destroyed, queue ourselves up to be destroyed on the next frame tick
    if (!destroyed) gfx.queueForDestroy(this);
  }

  // imageregiongl stuffs

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
}
