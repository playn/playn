/**
 * Copyright 2010 The PlayN Authors
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
package playn.core;

import react.Function;
import react.RFuture;

/**
 * Bitmapped image data. May be loaded via {@link Assets} or created dynamically as in the backing
 * image for a {@link Canvas}.
 */
public abstract class Image extends TileSource implements Canvas.Drawable {

  /** Reports the asynchronous loading of this image. This will be completed with success or
    * failure when the image's asynchronous load completes. */
  public final RFuture<Image> state;

  /**
   * Returns whether this image is fully loaded. In general you'll want to react to
   * {@link #state}, but this method is useful when you need to assert that something is only
   * allowed on a fully loaded image.
   */
  public boolean isLoaded () { return state.isCompleteNow(); }

  /**
   * Returns the scale of resolution independent pixels to actual pixels for this image. This will
   * be {@link Scale#ONE} unless HiDPI images are being used.
   */
  public abstract Scale scale ();

  /**
   * This image's width in display units. If this image is loaded asynchrously, this will return
   * 0 until loading is complete. See {@link #state}.
   */
  public float width () {
    return scale().invScaled(pixelWidth());
  }

  /**
   * This image's height in display units. If this image is loaded asynchrously, this will return
   * 0 until loading is complete. See {@link #state}.
   */
  public float height () {
    return scale().invScaled(pixelHeight());
  }

  /**
   * Returns the width of this image in physical pixels. If this image is loaded asynchrously,
   * this will return 0 until loading is complete. See {@link #state}.
   */
  public abstract int pixelWidth ();

  /**
   * Returns the height of this image in physical pixels. If this image is loaded asynchrously,
   * this will return 0 until loading is complete. See {@link #state}.
   */
  public abstract int pixelHeight ();

  /**
   * Extracts pixel data from a rectangular area of this image. This method may perform poorly, in
   * particular on the HTML platform.
   *
   * The returned pixel format is {@code  (alpha << 24 | red << 16 | green << 8 | blue)}, where
   * alpha, red, green and blue are the corresponding channel values, ranging from 0 to 255
   * inclusive.
   *
   * @param startX x-coordinate of the upper left corner of the area.
   * @param startY y-coordinate of the upper left corner of the area.
   * @param width width of the area.
   * @param height height of the area.
   * @param rgbArray will be filled with the pixel data from the area
   * @param offset fill start offset in rgbArray.
   * @param scanSize number of pixels in a row in rgbArray.
   */
  public abstract void getRgb (int startX, int startY, int width, int height, int[] rgbArray,
                               int offset, int scanSize);

  /**
   * Sets pixel data for a rectangular area of this image. This method may perform poorly, in
   * particular on the HTML platform. On the HTML platform, due to brower security limitations,
   * this method is only allowed on images created via {@link Graphics#createCanvas}.
   *
   * The pixel format is {@code (alpha << 24 | red << 16 | green << 8 | blue)}, where alpha, red,
   * green and blue are the corresponding channel values, ranging from 0 to 255 inclusive.
   *
   * @param startX x-coordinate of the upper left corner of the area.
   * @param startY y-coordinate of the upper left corner of the area.
   * @param width width of the area.
   * @param height height of the area.
   * @param rgbArray will be filled with the pixel data from the area
   * @param offset fill start offset in rgbArray.
   * @param scanSize number of pixels in a row in rgbArray.
   */
  public abstract void setRgb (int startX, int startY, int width, int height, int[] rgbArray,
                               int offset, int scanSize);

  /**
   * Creates a pattern from this image which can be used to fill a canvas.
   */
  public abstract Pattern createPattern (boolean repeatX, boolean repeatY);

  /**
   * Sets the texture config used when creating this image's default texture. Note: this must be
   * called before the first call to {@link #texture} so that it is configured before the default
   * texture is created and cached.
   */
  public Image setConfig (Texture.Config config) {
    texconf = config;
    return this;
  }

  /**
   * Returns, creating if necessary, this image's default texture. When the texture is created, it
   * will use the {@link Texture.Config} set via {@link #setConfig}. If an image's default texture
   * is {@link Texture#close}d, a subsequent call to this method will create a new default texture.
   */
  public Texture texture () {
    if (texture == null || texture.disposed()) texture = createTexture(texconf);
    return texture;
  }

  /**
   * Updates this image's default texture with the current contents of the image, and returns the
   * texture. If the texture has not yet been created, then this simply creates it. This is only
   * necessary if you want to update the default texture for an image associated with a {@link
   * Canvas}, or if you have used {@link #setRgb} to change the contents of this image.
   */
  public Texture updateTexture () {
    if (texture == null || texture.disposed()) texture = createTexture(texconf);
    else texture.update(this);
    return texture;
  }

  /**
   * Returns a future which will deliver the default texture for this image once its loading has
   * completed. Uses {@link #texture} to create the texture.
   */
  public RFuture<Texture> textureAsync () {
    return state.map(new Function<Image,Texture>() {
      public Texture apply (Image image) { return texture(); }
    });
  }

  /**
   * Creates a texture with this image's bitmap data using {@code config}. NOTE: this creates a new
   * texture with every call. This is generally only needed if you plan to create multiple textures
   * from the same bitmap, with different configurations. Otherwise just use {@link #texture} to
   * create the image's "default" texture which will be shared by all callers.
   */
  public Texture createTexture (Texture.Config config) {
    if (!isLoaded()) throw new IllegalStateException(
      "Cannot create texture from unready image: " + this);

    int texWidth = config.toTexWidth(pixelWidth());
    int texHeight = config.toTexHeight(pixelHeight());
    if (texWidth <= 0 || texHeight <= 0) throw new IllegalArgumentException(
      "Invalid texture size: " + texWidth + "x" + texHeight + " from: " + this);

    Texture tex = new Texture(gfx, gfx.createTexture(config), config, texWidth, texHeight,
                              scale(), width(), height());
    tex.update(this); // this will handle non-POT source image conversion
    return tex;
  }

  /** A region of an image which can be rendered to {@link Canvas}es and turned into a texture
    * (which is a {@link Tile} of the original image's texture). */
  public static abstract class Region extends TileSource implements Canvas.Drawable {}

  /** Returns a region of this image which can be drawn independently. */
  public Region region (final float rx, final float ry, final float rwidth, final float rheight) {
    final Image image = this;
    return new Region() {
      private Tile tile;
      @Override public boolean isLoaded () { return image.isLoaded(); }
      @Override public Tile tile () {
        if (tile == null) tile = image.texture().tile(rx, ry, rwidth, rheight);
        return tile;
      }
      @Override public RFuture<Tile> tileAsync () {
        return image.state.map(new Function<Image,Tile>() {
          public Tile apply (Image image) { return tile(); }
        });
      }

      @Override public float width () { return rwidth; }
      @Override public float height () { return rheight; }

      @Override public void draw (Object ctx, float x, float y, float width, float height) {
        image.draw(ctx, x, y, width, height, rx, ry, rwidth, rheight);
      }
      @Override public void draw (Object ctx, float dx, float dy, float dw, float dh,
                                  float sx, float sy, float sw, float sh) {
        image.draw(ctx, dx, dy, dw, dh, rx+sx, ry+sy, sw, sh);
      }
    };
  }

  /** Used with {@link #transform}. */
  public static interface BitmapTransformer {}

  /**
   * Generates a new image from this image's bitmap, using a transformer created for the platform
   * in use. See {@code JavaBitmapTransformer} for example.
   */
  public abstract Image transform (BitmapTransformer xform);

  @Override public Tile tile () { return texture(); }
  @Override public RFuture<Tile> tileAsync () {
    return state.map(new Function<Image,Tile>() {
      public Tile apply (Image image) { return texture(); }
    });
  }

  protected final Graphics gfx;
  protected Texture.Config texconf = Texture.Config.DEFAULT;
  protected Texture texture;

  protected Image (Graphics gfx, RFuture<Image> state) {
    this.gfx = gfx;
    this.state = state;
  }

  // this ctor is used for images that are constructed immediately with their images
  protected Image (Graphics gfx) {
    this.gfx = gfx;
    this.state = RFuture.success(this);
  }

  /** Uploads this image's data into {@code tex}. */
  protected abstract void upload (Graphics gfx, Texture tex);
}
