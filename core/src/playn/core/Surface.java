/**
 * Copyright 2011 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core;

/**
 * TODO
 */
public interface Surface {

  /**
   * Sets the entire surface to the given color.
   */
  Surface clear();

  /**
   * Draws an image at the specified location.
   *
   * @param dx the destination x
   * @param dy the destination y
   */
  Surface drawImage(Image image, float dx, float dy);

  /**
   * Draws a scaled image at the specified location.
   *
   * @param dx the destination x
   * @param dy the destination y
   * @param dw the destination width
   * @param dh the destination height
   */
  Surface drawImage(Image image, float dx, float dy, float dw, float dh);

  /**
   * Draws a scaled subset of an image at the specified location.
   *
   * TODO(jgw): Document whether out-of-bounds source coordinates clamp, repeat,
   * or do nothing.
   *
   * @param dx the destination x
   * @param dy the destination y
   * @param dw the destination width
   * @param dh the destination height
   * @param sx the source x
   * @param sy the source y
   * @param sw the source width
   * @param sh the source height
   */
  Surface drawImage(Image image, float dx, float dy, float dw, float dh, float sx, float sy, float sw,
      float sh);

  /**
   * Draws an image, centered at the specified location.  Simply
   * subtracts image.width/2 from dx and image.height/2 from dy.
   *
   * @param image the image to draw
   * @param dx destination x
   * @param dy destination y
   */
  Surface drawImageCentered(Image image, float dx, float dy);

  /**
   * TODO
   */
  Surface drawLine(float x0, float y0, float x1, float y1, float width);

  /**
   * Fills the specified rectangle.
   */
  Surface fillRect(float x, float y, float width, float height);

  /**
   * The height of this surface.
   */
  int height();

  /**
   * Restores the transform previously stored by {@link #save()}.
   */
  Surface restore();

  /**
   * Rotates the current transformation matrix by the specified angle in radians.
   */
  Surface rotate(float radians);

  /**
   * Saves the current transform.
   */
  Surface save();

  /**
   * Scales the current transformation matrix by the specified amount on each axis.
   */
  Surface scale(float sx, float sy);

  /**
   * Sets the color to be used for fill operations. This replaces any existing
   * fill gradient or pattern.
   */
  Surface setFillColor(int color);

  /**
   * TODO
   */
  Surface setFillPattern(Pattern pattern);

  /**
   * Sets the transformation matrix directly, replacing the existing matrix.
   */
  Surface setTransform(float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Multiplies the current transformation matrix by the given matrix.
   */
  Surface transform(float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Translates the current transformation matrix by the given amount.
   */
  Surface translate(float x, float y);

  /**
   * The width of this surface.
   */
  int width();
}
