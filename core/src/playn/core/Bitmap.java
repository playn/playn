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
import react.RPromise;

/**
 * Bitmapped image data. May be loaded via {@link Assets} or created dynamically as in the backing
 * image for a {@link Canvas}.
 */
public abstract class Bitmap {

  /** Reports the asynchronous loading of this bitmap. This will be completed with success or
    * failure when the bitmap's asynchronous load completes. */
  public final RFuture<Bitmap> state;

  /**
   * Returns whether this bitmap is fully loaded. In general you'll want to react to
   * {@link #state}, but this method is useful when you need to assert that something is only
   * allowed on a fully loaded bitmap.
   */
  public boolean isLoaded () { return state.isCompleteNow(); }

  /**
   * Returns the scale of resolution independent pixels to actual pixels for this bitmap. This will
   * be {@link Scale#ONE} unless HiDPI bitmaps are being used.
   */
  public abstract Scale scale ();

  /**
   * This bitmap's width in display units. If this bitmap is loaded asynchrously, this will return
   * 0 until loading is complete. See {@link #state}.
   */
  public float width () {
    return scale().invScaled(pixelWidth());
  }

  /**
   * This bitmap's height in display units. If this bitmap is loaded asynchrously, this will return
   * 0 until loading is complete. See {@link #state}.
   */
  public float height () {
    return scale().invScaled(pixelHeight());
  }

  /**
   * Returns the width of this bitmap in physical pixels. If this bitmap is loaded asynchrously,
   * this will return 0 until loading is complete. See {@link #state}.
   */
  public abstract int pixelWidth ();

  /**
   * Returns the height of this bitmap in physical pixels. If this bitmap is loaded asynchrously,
   * this will return 0 until loading is complete. See {@link #state}.
   */
  public abstract int pixelHeight ();

  /**
   * Extracts pixel data from a rectangular area of this bitmap. This method may perform poorly, in
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
   * Sets pixel data for a rectangular area of this bitmap. This method may perform poorly, in
   * particular on the HTML platform. On the HTML platform, due to brower security limitations,
   * this method is only allowed on bitmaps created via {@link Graphics#createCanvas}.
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
   * Converts this bitmap into a pattern which can be used to fill a canvas.
   */
  public abstract Pattern toPattern (boolean repeatX, boolean repeatY);

  /**
   * Creates a texture with the contents of this bitmap, with default config.
   * See {@link #toTexture(Texture.Config)}.
   */
  public Texture toTexture () { return toTexture(Texture.Config.DEFAULT); }

  /**
   * Uploads this bitmap's data to the GPU and returns a handle to the texture.
   * @throws IllegalStateException if this bitmap is not fully loaded.
   */
  public Texture toTexture (Texture.Config config) {
    if (!isLoaded()) throw new IllegalStateException("Cannot create texture from unready bitmap.");

    int texWidth = config.toTexWidth(pixelWidth());
    int texHeight = config.toTexHeight(pixelHeight());
    if (texWidth <= 0 || texHeight <= 0) throw new IllegalArgumentException(
      "Invalid texture size: " + texWidth + "x" + texHeight + " from: " + this);

    Texture tex = new Texture(gfx, gfx.createTexture(config), config, texWidth, texHeight,
                              scale(), width(), height());
    tex.update(this); // this will handle non-POT source bitmap conversion
    return tex;
  }

  /**
   * Returns a future which will deliver a texture for this bitmap once its loading has completed.
   * Uses {@link #toTexture()} to create texture.
   */
  public RFuture<Texture> toTextureAsync () {
    return state.map(new Function<Bitmap,Texture>() {
      public Texture apply (Bitmap bitmap) { return toTexture(); }
    });
  }

  /**
   * Returns a future which will deliver a texture for this bitmap once its loading has completed.
   * Uses {@link #toTexture(Texture.Config)} to create texture.
   */
  public RFuture<Texture> toTextureAsync (final Texture.Config config) {
    return state.map(new Function<Bitmap,Texture>() {
      public Texture apply (Bitmap bitmap) { return toTexture(config); }
    });
  }

  /** Used with {@link #transform}. */
  public static interface BitmapTransformer {}

  /**
   * Generates a new bitmap from this bitmap, using a transformer created for the platform in use.
   * See {@code JavaBitmapTransformer} for example.
   */
  public abstract Bitmap transform (BitmapTransformer xform);

  protected final Graphics gfx;

  protected Bitmap (Graphics gfx, RFuture<Bitmap> state) {
    this.gfx = gfx;
    this.state = state;
  }

  // this ctor is used for bitmaps that are constructed immediately with their bitmaps
  protected Bitmap (Graphics gfx) {
    this.gfx = gfx;
    this.state = RFuture.success(this);
  }

  /** Uploads this bitmap's data into {@code tex}. */
  protected abstract void upload (Graphics gfx, Texture tex);
}
