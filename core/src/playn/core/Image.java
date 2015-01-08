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

import react.RFuture;
import react.RPromise;

/**
 * A bitmapped image. May be loaded via {@link Assets} or created dynamically as in the backing
 * image for a {@link Canvas}.
 */
public abstract class Image {

  /** Reports the asynchronous loading of this image. This will be completed with success or
    * failure when the image's asynchronous load completes. */
  public final RFuture<Image> state;

  /**
   * Returns the scale of resolution independent pixels to actual pixels for this image. This will
   * be {@link Scale#ONE} unless HiDPI images are being used.
   */
  public abstract Scale scale ();

  /**
   * This image's width in display units. If this image is loaded asynchrously, this will return 0
   * until loading is complete. See {@link #state}.
   */
  public float width () {
    return scale().invScaled(pixelWidth());
  }

  /**
   * This image's height in display units. If this image is loaded asynchrously, this will return 0
   * until loading is complete. See {@link #state}.
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
   * Converts this image into a pattern which can be used to fill a canvas.
   */
  public abstract Pattern toPattern (boolean repeatX, boolean repeatY);

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

  /** Used with {@link #transform}. */
  public static interface BitmapTransformer {
  }

  /**
   * Generates a new image from this image's bitmap, using a bitmap transformer created for the
   * platform in use. See {@code JavaBitmapTransformer} for example.
   */
  public abstract Image transform (BitmapTransformer xform);

  protected Image (RFuture<Image> state) {
    this.state = state;
  }

  // this ctor is used for images that are constructed immediately with their bitmaps
  protected Image () {
    this.state = RFuture.success(this);
  }

  /** Uploads this image's data into {@code tex}. */
  protected abstract void upload (Graphics gfx, Texture tex);
}
