/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.core;

/**
 * TODO
 */
public interface Surface {

  /**
   * Sets the entire surface to the given color.
   */
  void clear();

  /**
   * Draws an image at the specified location.
   * 
   * @param dx the destination x
   * @param dy the destination y
   */
  void drawImage(Image image, float dx, float dy);

  /**
   * Draws a scaled image at the specified location.
   * 
   * @param dx the destination x
   * @param dy the destination y
   * @param dw the destination width
   * @param dh the destination height
   */
  void drawImage(Image image, float dx, float dy, float dw, float dh);

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
  void drawImage(Image image, float dx, float dy, float dw, float dh, float sx, float sy, float sw,
      float sh);

  /**
   * Draws an image, centered at the specified location.  Simply
   * subtracts image.width/2 from dx and image.height/2 from dy.
   * 
   * @param image the image to draw
   * @param dx destination x
   * @param dy destination y
   */
  void drawImageCentered(Image image, float dx, float dy);

  /**
   * TODO
   */
  void drawLine(float x0, float y0, float x1, float y1, float width);

  /**
   * Fills the specified rectangle.
   */
  void fillRect(float x, float y, float width, float height);

  /**
   * The height of this surface.
   */
  int height();

  /**
   * Restores the transform previously stored by {@link #save()}.
   */
  void restore();

  /**
   * Rotates the current transformation matrix by the specified angle in radians.
   */
  void rotate(float radians);

  /**
   * Saves the current transform.
   */
  void save();

  /**
   * Scales the current transformation matrix by the specified amount on each axis.
   */
  void scale(float sx, float sy);

  /**
   * Sets the color to be used for fill operations. This replaces any existing
   * fill gradient or pattern.
   */
  void setFillColor(int color);

  /**
   * TODO
   */
  void setFillPattern(Pattern pattern);

  /**
   * Sets the transformation matrix directly, replacing the existing matrix.
   */
  void setTransform(float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Multiplies the current transformation matrix by the given matrix.
   */
  void transform(float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Translates the current transformation matrix by the given amount.
   */
  void translate(float x, float y);

  /**
   * The width of this surface.
   */
  int width();
}
