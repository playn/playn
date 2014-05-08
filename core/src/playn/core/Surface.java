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

import playn.core.gl.GLShader;

/**
 * TODO
 */
public interface Surface {

  /**
   * Returns the width of this surface.
   */
  float width();

  /**
   * Returns the height of this surface.
   */
  float height();

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
   * @deprecated You almost certainly do not want to do this because this will wipe out any
   * pre-configured HiDPI transform and break automatic handling of HiDPI screens. You should
   * instead use {@link #transform}, and possibly {@link #save} and {@link #restore} if you need to
   * return to the "identity" (which is in quotes because the identity on a HiDPI screen will not
   * be the the actual identity matrix).
   */
  @Deprecated
  Surface setTransform(float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Set the alpha component of this surface's current tint. Note that this value will be quantized
   * to an integer between 0 and 255. Also see {@link #setTint}.
   *
   * <p>Values outside the range [0,1] will be clamped to the range [0,1].</p>
   *
   * @param alpha value in range [0,1] where 0 is transparent and 1 is opaque.
   */
  Surface setAlpha(float alpha);

  /**
   * Sets the tint for this layer, as {@code ARGB}.
   *
   * <p> <em>NOTE:</em> this will overwrite any value configured via {@link #setAlpha}. Either
   * include your desired alpha in the high bits of {@code tint} or call {@link #setAlpha} after
   * calling this method. </p>
   *
   * <p> <em>NOTE:</em> the RGB components of a layer's tint only work on GL-based backends. It is
   * not possible to tint layers using the HTML5 canvas and Flash backends. </p>
   */
  Surface setTint(int tint);

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
   * Configures a custom shader to use when drawing images and shapes to this surface. If the
   * supplied shader is null, the default shader will be used.
   */
  Surface setShader(GLShader shader);

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
   * Draws a scaled or repeated image at the specified location.
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
   * Renders the supplied scene graph into this surface. This renders the scene graph into the
   * surface's texture using the main render pipeline, so custom shaders (on platforms that support
   * them) are handled properly. <em>NOTE:</em> if there is a loop in the scene graph (like you add
   * an ImmediateLayer that calls {@code surf.drawLayer(graphics().rootLayer())}) you will get the
   * infinite loop that you deserve. Caveat renderer.
   */
  Surface drawLayer(Layer layer);

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
   * Fills the supplied batch of triangles with the current fill color or pattern.
   *
   * <p>Note: this method is only performant on OpenGL-based backends (Android, iOS, HTML-WebGL,
   * etc.). On non-OpenGL-based backends (HTML-Canvas, HTML-Flash) it converts the triangles to a
   * path on every rendering call.</p>
   *
   * @param xys the xy coordinates of the triangles, as an array: {@code [x1, y1, x2, y2, ...]}.
   * @param xysOffset the offset of the coordinates array, must not be negative and no greater than
   * {@code xys.length}. Note: this is an absolute offset; since {@code xys} contains pairs of
   * values, this will be some multiple of two.
   * @param xysLen the number of coordinates to read, must be no less than zero and no greater than
   * {@code xys.length - xysOffset}. Note: this is an absolute length; since {@code xys} contains
   * pairs of values, this will be some multiple of two.
   * @param indices the index of each vertex of each triangle in the {@code xys} array. Because
   * this method renders a slice of {@code xys}, one must also specify {@code indexBase} which
   * tells us how to interpret indices. The index into {@code xys} will be computed as: {@code
   * 2*(indices[ii] - indexBase)}, so if your indices reference vertices relative to the whole
   * array you should pass {@code xysOffset/2} for {@code indexBase}, but if your indices reference
   * vertices relative to <em>the slice</em> then you should pass zero.
   * @param indicesOffset the offset of the indices array, must not be negative and no greater than
   * {@code indices.length}.
   * @param indicesLen the number of indices to read, must be no less than zero and no greater than
   * {@code indices.length - indicesOffset}.
   * @param indexBase the basis for interpreting {@code indices}. See the docs for {@code indices}
   * for details.
   */
  Surface fillTriangles(float[] xys, int xysOffset, int xysLen,
                        int[] indices, int indicesOffset, int indicesLen, int indexBase);

  /**
   * Fills the supplied batch of triangles with the current fill pattern.
   *
   * <p>Note: this method only honors the texture coordinates on OpenGL-based backends (Anrdoid,
   * iOS, HTML-WebGL, etc.). On non-OpenGL-based backends (HTML-Canvas, HTML-Flash) it behaves like
   * a call to {@link #fillTriangles(float[],int[])}.</p>
   *
   * @param xys see {@link #fillTriangles(float[],int[])}.
   * @param sxys the texture coordinates for each vertex of the triangles, as an array:
   * {@code [sx1, sy1, sx2, sy2, ...]}. This must be the same length as {@code xys}.
   * @param indices see {@link #fillTriangles(float[],int[])}.
   *
   * @throws IllegalStateException if no fill pattern is currently set.
   */
  Surface fillTriangles(float[] xys, float[] sxys, int[] indices);

  /**
   * Fills the supplied batch of triangles with the current fill pattern.
   *
   * <p>Note: this method only honors the texture coordinates on OpenGL-based backends (Anrdoid,
   * iOS, HTML-WebGL, etc.). On non-OpenGL-based backends (HTML-Canvas, HTML-Flash) it behaves like
   * a call to {@link #fillTriangles(float[],int[])}.</p>
   *
   * @param xys see {@link #fillTriangles(float[],int,int,int[],int,int,int)}.
   * @param sxys the texture coordinates for each vertex of the triangles, as an array.
   * {@code [sx1, sy1, sx2, sy2, ...]}. This must be the same length as {@code xys}.
   * @param xysOffset see {@link #fillTriangles(float[],int,int,int[],int,int,int)}.
   * @param xysLen see {@link #fillTriangles(float[],int,int,int[],int,int,int)}.
   * @param indices see {@link #fillTriangles(float[],int,int,int[],int,int,int)}.
   * @param indicesOffset see {@link #fillTriangles(float[],int,int,int[],int,int,int)}.
   * @param indicesLen see {@link #fillTriangles(float[],int,int,int[],int,int,int)}.
   * @param indexBase see {@link #fillTriangles(float[],int,int,int[],int,int,int)}.
   *
   * @throws IllegalStateException if no fill pattern is currently set.
   */
  Surface fillTriangles(float[] xys, float[] sxys, int xysOffset, int xysLen,
                        int[] indices, int indicesOffset, int indicesLen, int indexBase);
}
