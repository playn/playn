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

/**
 * An image.
 */
public interface Image {

  /** Used with {@link #transform}. */
  public interface BitmapTransformer {
  }

  /**
   * This image's width in pixels.
   */
  int width();

  /**
   * This image's height in pixels.
   */
  int height();

  /**
   * Whether or not this image is ready to be used.
   */
  boolean isReady();

  /**
   * Adds a callback to be notified when this image has loaded. If the image is
   * already loaded, the callback will be notified immediately. The callback is
   * discarded once the image is loaded.
   */
  void addCallback(ResourceCallback<? super Image> callback);

  /**
   * A subregion of an image. See {@link Image#subImage}.
   */
  public interface Region extends Image {
    /**
     * The x offset (in pixels) of this subimage into its parent image.
     */
    float x();

    /**
     * The y offset (in pixels) of this subimage into its parent image.
     */
    float y();

    /**
     * The image of which this subimage is part.
     */
    Image parent();
  }

  /**
   * Returns an image that draws the specified sub-region of this image.
   * @param x the x offset (in pixels) of the subimage.
   * @param y the y offset (in pixels) of the subimage.
   * @param width the width (in pixels) of the subimage.
   * @param height the height (in pixels) of the subimage.
   */
  Region subImage(float x, float y, float width, float height);

  /**
   * Creates a {@link Pattern} that can be used to use this image as a fill in a canvas.
   */
  Pattern toPattern();

  /**
   * Extracts pixel data from a rectangular area of this image. This method may perform poorly, in
   * particular on HTML and Flash platforms - avoid using this if possible.
   *
   * The returned pixel format is {@code (alpha << 24 | red << 16 | green << 8 | blue)}, where
   * alpha, red, green and blue are the corresponding channel values, ranging from 0 to 255
   * inclusive.
   *
   * Currently not implemented for iOS.
   *
   * @param startX x-coordinate of the upper left corner of the area.
   * @param startY y-coordinate of the upper left corner of the area.
   * @param width width of the area.
   * @param height height of the area.
   * @param rgbArray will be filled with the pixel data from the area
   * @param offset fill start offset in rgbArray.
   * @param scanSize number of pixels in a row in rgbArray.
   */
  void getRgb(int startX, int startY, int width, int height, int[] rgbArray,
              int offset, int scanSize);

  /**
   * Generates a new image from this image's bitmap, using a bitmap transformer created for the
   * platform in use. See {@link JavaBitmapTransformer} and {@code IOSBitmapTransformer}. This does
   * not work on sub-images.
   */
  Image transform(BitmapTransformer xform);
}
