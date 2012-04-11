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
   * Returns the width of this surface.
   */
  int width();

  /**
   * Returns the height of this surface.
   */
  int height();

  /**
   * Saves the current transform.
   */
  Surface save();

  /**
   * Restores the transform previously stored by {@link #save()}.
   */
  Surface restore();

  /**
   * Translates the current transformation matrix by the given amount.
   */
  Surface translate(float x, float y);

  /**
   * Scales the current transformation matrix by the specified amount on each axis.
   */
  Surface scale(float sx, float sy);

  /**
   * Rotates the current transformation matrix by the specified angle in radians.
   */
  Surface rotate(float radians);

  /**
   * Multiplies the current transformation matrix by the given matrix.
   */
  Surface transform(float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Sets the transformation matrix directly, replacing the existing matrix.
   */
  Surface setTransform(float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Set the global alpha value to be used for all rendering.
   * <p>Values outside the range [0,1] will be clamped to the range [0,1].</p>
   *
   * @param alpha value in range [0,1] where 0 is transparent and 1 is opaque.
   */
  Surface setAlpha(float alpha);

  /**
   * Sets the color to be used for fill operations. This replaces any existing fill gradient or
   * pattern.
   */
  Surface setFillColor(int color);

  /**
   * Sets the pattern to be used for fill operations. This replaces any existing fill gradient or
   * pattern.
   */
  Surface setFillPattern(Pattern pattern);

  /**
   * Clears the entire surface to transparent blackness.
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
  Surface drawImage(Image image, float dx, float dy, float dw, float dh,
      float sx, float sy, float sw, float sh);

  /**
   * Draws an image, centered at the specified location. Simply subtracts image.width/2 from dx and
   * image.height/2 from dy.
   *
   * @param image the image to draw
   * @param dx destination x
   * @param dy destination y
   */
  Surface drawImageCentered(Image image, float dx, float dy);

  /**
   * Fills a line between the specified coordinates, of the specified (pixel) width.
   */
  Surface drawLine(float x0, float y0, float x1, float y1, float width);

  /**
   * Fills the specified rectangle.
   */
  Surface fillRect(float x, float y, float width, float height);

  /**
   * Fills the supplied batch of triangles with the current fill color or pattern. Note: this
   * method is only performant on OpenGL-based backends (Android, iOS, HTML-WebGL, etc.). On
   * non-OpenGL-based backends (HTML-Canvas, HTML-Flash) it converts the triangles to a path on
   * every rendering call.
   *
   * @param xys the xy coordinates of the triangles, as an array: {@code [x1, y1, x2, y2, ...]}.
   * @param indices the index of each vertex of each triangle in the {@code xys} array.
   */
  Surface fillTriangles(float[] xys, int[] indices);

  /**
   * Fills the supplied batch of triangles with the current fill pattern. Note: this method only
   * honors the texture coordinates on OpenGL-based backends (Anrdoid, iOS, HTML-WebGL, etc.). On
   * non-OpenGL-based backends (HTML-Canvas, HTML-Flash) it behaves like a call to {@link
   * #fillTriangles(float[],int[])}.
   *
   * @param xys the xy coordinates of the triangles, as an array: {@code [x1, y1, x2, y2, ...]}.
   * @param sxys the texture coordinates for each vertex of the triangles, as an array:
   * {@code [sx1, sy1, sx2, sy2, ...]}. This must be the same length as {@code xys}.
   * @param indices the index of each vertex of each triangle in the {@code xys} array.
   *
   * @throws IllegalStateException if no fill pattern is currently set.
   */
  Surface fillTriangles(float[] xys, float[] sxys, int[] indices);
}
