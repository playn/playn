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
 * An image that provides a {@link Canvas} that allows you to draw directly into the image.
 */
public interface CanvasImage extends Image {

  /**
   * Returns this image's canvas.
   */
  Canvas canvas();

  /**
   * Sets pixel data for a rectangular area of this image. This method may perform poorly, in
   * particular on HTML and Flash platforms - avoid using this if possible.
   *
   * The pixel format is {@code (alpha << 24 | red << 16 | green << 8 | blue)}, where alpha, red,
   * green and blue are the corresponding channel values, ranging from 0 to 255 inclusive.
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
  void setRgb(int startX, int startY, int width, int height, int[] rgbArray,
              int offset, int scanSize);
}
