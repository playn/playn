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

import playn.core.gl.Scale;
import playn.core.util.Callback;

/**
 * The base image interface. See {@link CanvasImage} and {@link SurfaceImage} for specialized
 * variants.
 */
public interface Image {

  /**
   * This image's width in pixels. When the image is loaded asynchronously, this will be 0 until the
   * image is loaded and ready for use. See {@link Image#isReady} and {@link Assets#getImage}.
   */
  float width();

  /**
   * This image's height in pixels. When the image is loaded asynchronously, this will be 0 until
   * the image is loaded and ready for use. See {@link Image#isReady} and {@link Assets#getImage}.
   */
  float height();

  /**
   * Returns the pixel-scale of this image. This will be {@link Scale#ONE} unless HiDPI images are
   * being used.
   */
  Scale scale();

  /**
   * Whether or not this image is ready to be used.
   */
  boolean isReady();

  /**
   * Adds a callback to be notified when this image has loaded. If the image is
   * already loaded, the callback will be notified immediately; otherwise on the main playn
   * thread at a later time. The callback is discarded once the image is loaded.
   */
  void addCallback(Callback<? super Image> callback);

  /**
   * Returns whether this image repeats in the x direction. See {@link #setRepeat}.
   */
  boolean repeatX();

  /**
   * Returns whether this image repeats in the y direction. See {@link #setRepeat}.
   */
  boolean repeatY();

  /**
   * Configures whether this image repeats in the x or y direction (rather than scaling) when used
   * in a layer whose width or height is larger or smaller than the image. Both axes default to
   * {@code false}.
   */
  void setRepeat(boolean repeatX, boolean repeatY);

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
     * Updates this region's bounds in its parent image. If you need a rapidly changing region of
     * an image, this can be more efficient than repeatedly creating new subimages as it creates no
     * garbage to be collected.
     */
    void setBounds(float x, float y, float width, float height);

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
   * Creates a {@link Pattern} that can be used to use this image as a fill in a canvas. This
   * pattern will repeat based on the image's repeat configuration.
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

  /** Used with {@link #transform}. */
  public interface BitmapTransformer {
  }

  /**
   * Generates a new image from this image's bitmap, using a bitmap transformer created for the
   * platform in use. See {@code JavaBitmapTransformer} and {@code IOSBitmapTransformer}. This does
   * not work on sub-images nor {@link SurfaceImage}s.
   */
  Image transform(BitmapTransformer xform);

  /**
   * Configures the use of mipmaps when rendering this image at scales less than 1. This only
   * applies to GL-based backends (it is a NOOP on other backends).
   */
  void setMipmapped(boolean mipmapped);

  /**
   * Creates a texture for this image (if one does not already exist) and returns its OpenGL
   * texture id. Returns 0 if the underlying image data is not yet ready or if this platform does
   * not use OpenGL. The texture will remain in GPU memory until {@link #clearTexture} is called or
   * this image is garbage collected.
   *
   * <p><em>Note:</em> this texture handle should not be held for longer than the scope of the
   * enclosing function call. It is not safe to hang onto an image's texture handle as the image
   * may delete its texture and recreate it under any number of circumstances.</p>
   */
  int ensureTexture();

  /**
   * Clears the GPU texture associated with this image, on platforms implemented via OpenGL. Does
   * nothing on non-OpenGL platforms. In general it is not necessary to call this method. Images
   * added to {@link ImageLayer} instances automatically clear their texture when the image layer
   * is removed from the scene graph. Textures are also cleared when the image is garbage
   * collected. However, if you manually call {@link #ensureTexture}, or if you draw images to a
   * {@link Surface}, you may wish to clear textures manually to avoid running out of GPU memory
   * before garbage collection has a chance to run and clear the textures for you.
   */
  void clearTexture();
}
