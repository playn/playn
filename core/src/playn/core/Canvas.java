/**
 * Copyright 2010 The PlayN Authors
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
 * A 2d drawing canvas.
 *
 * <p>
 * Colors are specified as integer ARGB values, with alpha in the
 * most-significant byte.
 * </p>
 *
 * <p>
 * All methods that modify the Canvas return it to allow calls to be chained.
 * </p>
 *
 * TODO: - alpha (Flash and Java2D implementations are tricky). - textAlign?
 *
 * TODO (maybe): - textBaseline? Don't see it in Android. - font? Abstracting
 * this is going to be tricky. - measureText? Canvas API is pretty anemic, but
 * would be easy to implement on Android/Flash. - pattern repetition flags? Not
 * clear that Flash supports them.
 *
 * Notes: - Clipping is going to be tricky in Flash. It requires that a separate
 * DisplayObject be used as a "mask". I think this can be made to work with this
 * API, but I'm not sure.
 */
public interface Canvas {

  /**
   * Values that may be used with
   * {@link Canvas#setCompositeOperation(Composite)}.
   */
  enum Composite {
    /**
     * A (B is ignored). Display the source image instead of the destination
     * image.
     *
     * [Sa, Sc]
     */
    SRC,

    /**
     * B atop A. Same as source-atop but using the destination image instead of
     * the source image and vice versa.
     *
     * [Sa, Sa * Dc + Sc * (1 - Da)]
     */
    DST_ATOP,

    /**
     * A over B. Display the source image wherever the source image is opaque.
     * Display the destination image elsewhere.
     *
     * [Sa + (1 - Sa)*Da, Rc = Sc + (1 - Sa)*Dc]
     */
    SRC_OVER,

    /**
     * B over A. Same as source-over but using the destination image instead of
     * the source image and vice versa.
     *
     * [Sa + (1 - Sa)*Da, Rc = Dc + (1 - Da)*Sc]
     */
    DST_OVER,

    /**
     * A in B. Display the source image wherever both the source image and
     * destination image are opaque. Display transparency elsewhere.
     *
     * [Sa * Da, Sc * Da]
     */
    SRC_IN,

    /**
     * B in A. Same as source-in but using the destination image instead of the
     * source image and vice versa.
     *
     * [Sa * Da, Sa * Dc]
     */
    DST_IN,

    /**
     * A out B. Display the source image wherever the source image is opaque and
     * the destination image is transparent. Display transparency elsewhere.
     *
     * [Sa * (1 - Da), Sc * (1 - Da)]
     */
    SRC_OUT,

    /**
     * B out A. Same as source-out but using the destination image instead of
     * the source image and vice versa.
     *
     * [Da * (1 - Sa), Dc * (1 - Sa)]
     */
    DST_OUT,

    /**
     * A atop B. Display the source image wherever both images are opaque.
     * Display the destination image wherever the destination image is opaque
     * but the source image is transparent. Display transparency elsewhere.
     *
     * [Da, Sc * Da + (1 - Sa) * Dc]
     */
    SRC_ATOP,

    /**
     * A xor B. Exclusive OR of the source image and destination image.
     *
     * [Sa + Da - 2 * Sa * Da, Sc * (1 - Da) + (1 - Sa) * Dc]
     */
    XOR,
  }

  /**
   * Values that may be used with {@link Canvas#setLineCap(LineCap)}.
   */
  enum LineCap {
    BUTT, ROUND, SQUARE
  }

  /**
   * Values that may be used with {@link Canvas#setLineJoin(LineJoin)}.
   */
  enum LineJoin {
    BEVEL, MITER, ROUND
  }

  /**
   * Clears the entire canvas to rgba(0, 0, 0, 0).
   */
  Canvas clear();

  /**
   * Intersects the current clip with the specified path.
   */
  Canvas clip(Path clipPath);

  /**
   * Creates a path object.
   */
  Path createPath();

  /**
   * Draws an image at the specified location.
   *
   * @param dx the destination x
   * @param dy the destination y
   */
  Canvas drawImage(Image image, float dx, float dy);

  /**
   * Draws an image, centered at the specified location.  Simply
   * subtracts image.width/2 from dx and image.height/2 from dy.
   *
   * @param image the image to draw
   * @param dx destination x
   * @param dy destination y
   */
  Canvas drawImageCentered(Image image, float dx, float dy);

  /**
   * Draws a scaled image at the specified location.
   *
   * @param dx the destination x
   * @param dy the destination y
   * @param dw the destination width
   * @param dh the destination height
   */
  Canvas drawImage(Image image, float dx, float dy, float dw, float dh);

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
  Canvas drawImage(Image image, float dx, float dy, float dw, float dh, float sx, float sy,
      float sw, float sh);

  /**
   * Draws a line between the two specified points.
   */
  Canvas drawLine(float x0, float y0, float x1, float y1);

  /**
   * Draws a single point at the specified location.
   */
  Canvas drawPoint(float x, float y);

  /**
   * Draws text at the specified location.
   */
  Canvas drawText(String text, float x, float y);

  /**
   * Draws the supplied text layout at the specified location. The text will be drawn in the
   * current fill color.
   */
  Canvas drawText(TextLayout layout, float x, float y);

  /**
   * Fills a circle at the specified center and radius.
   */
  Canvas fillCircle(float x, float y, float radius);

  /**
   * Fills the specified path.
   */
  Canvas fillPath(Path path);

  /**
   * Fills the specified rectangle.
   */
  Canvas fillRect(float x, float y, float width, float height);

  /**
   * Fills the specified rounded rectangle.
   *
   * @param x the x coordinate of the upper left of the rounded rectangle.
   * @param y the y coordinate of the upper left of the rounded rectangle.
   * @param width the width of the rounded rectangle.
   * @param height the width of the rounded rectangle.
   * @param radius the radius of the circle to use for the corner.
   */
  Canvas fillRoundRect(float x, float y, float width, float height, float radius);

  /**
   * The height of this canvas.
   */
  int height();

  /**
   * Restores the canvas's previous state.
   *
   * @see #save()
   */
  Canvas restore();

  /**
   * Rotates the current transformation matrix by the specified angle in radians.
   */
  Canvas rotate(float radians);

  /**
   * The save and restore methods preserve and restore the state of the canvas,
   * but not specific paths or graphics.
   *
   * The following values are saved:
   * <ul>
   * <li>transformation matrix</li>
   * <li>clipping path</li>
   * <li>stroke color</li>
   * <li>stroke width</li>
   * <li>line cap</li>
   * <li>line join</li>
   * <li>miter limit</li>
   * <li>fill color or gradient</li>
   * <li>composite operation</li>
   * </ul>
   */
  Canvas save();

  /**
   * Scales the current transformation matrix by the specified amount.
   */
  Canvas scale(float x, float y);

  /**
   * Set the global alpha value to be used for all painting.
   * <p>
   * Values outside the range [0,1] will be clamped to the range [0,1].
   *
   * @param alpha alpha value in range [0,1] where 0 is transparent and 1 is opaque
   */
  Canvas setAlpha(float alpha);

  /**
   * Sets the Porter-Duff composite operation to be used for all painting.
   */
  Canvas setCompositeOperation(Composite composite);

  /**
   * Sets the color to be used for fill operations. This replaces any existing
   * fill gradient or pattern.
   */
  Canvas setFillColor(int color);

  /**
   * Sets the gradient to be used for fill operations. This replaces any
   * existing fill color or pattern.
   */
  Canvas setFillGradient(Gradient gradient);

  /**
   * Sets the pattern to be used for fill operations. This replaces any existing
   * fill color or gradient.
   */
  Canvas setFillPattern(Pattern pattern);

  /**
   * Sets the line-cap mode for strokes.
   */
  Canvas setLineCap(LineCap cap);

  /**
   * Sets the line-join mode for strokes.
   */
  Canvas setLineJoin(LineJoin join);

  /**
   * Sets the miter limit for strokes.
   */
  Canvas setMiterLimit(float miter);

  /**
   * Sets the color for strokes.
   */
  Canvas setStrokeColor(int color);

  /**
   * Sets the width for strokes, in pixels.
   */
  Canvas setStrokeWidth(float strokeWidth);

  /**
   * Sets the transformation matrix directly, replacing the existing matrix.
   */
  Canvas setTransform(float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Strokes a circle at the specified center and radius.
   */
  Canvas strokeCircle(float x, float y, float radius);

  /**
   * Strokes the specified path.
   */
  Canvas strokePath(Path path);

  /**
   * Strokes the specified rectangle.
   */
  Canvas strokeRect(float x, float y, float width, float height);

  /**
   * Strokes the specified rounded rectangle.
   *
   * @param x the x coordinate of the upper left of the rounded rectangle.
   * @param y the y coordinate of the upper left of the rounded rectangle.
   * @param width the width of the rounded rectangle.
   * @param height the width of the rounded rectangle.
   * @param radius the radius of the circle to use for the corner.
   */
  Canvas strokeRoundRect(float x, float y, float width, float height, float radius);

  /**
   * Multiplies the current transformation matrix by the given matrix.
   */
  Canvas transform(float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Translates the current transformation matrix by the given amount.
   */
  Canvas translate(float x, float y);

  /**
   * The width of this canvas.
   */
  int width();
}
