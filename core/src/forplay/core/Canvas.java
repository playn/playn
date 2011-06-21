/**
 * Copyright 2010 The ForPlay Authors
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
 * A 2d drawing canvas.
 * 
 * <p>
 * Colors are specified as integer ARGB values, with alpha in the
 * most-significant byte.
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
  void clear();

  /**
   * Intersects the current clip with the specified path.
   */
  void clip(Path clipPath);

  /**
   * Draws an image at the specified location.
   * 
   * @param dx the destination x
   * @param dy the destination y
   */
  void drawImage(Image image, float dx, float dy);

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
   * Draws a line between the two specified points.
   */
  void drawLine(float x0, float y0, float x1, float y1);

  /**
   * Draws a single point at the specified location.
   */
  void drawPoint(float x, float y);

  /**
   * Draws text at the specified location.
   */
  void drawText(String text, float x, float y);

  /**
   * Fills a circle at the specified center and radius.
   */
  void fillCircle(float x, float y, float radius);

  /**
   * Fills the specified path.
   */
  void fillPath(Path path);

  /**
   * Fills the specified rectangle.
   */
  void fillRect(float x, float y, float width, float height);

  /**
   * The height of this canvas.
   */
  int height();

  /**
   * Restores the canvas's previous state.
   * 
   * @see #save()
   */
  void restore();

  /**
   * Rotates the current transformation matrix by the specified angle in radians.
   */
  void rotate(float radians);

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
  void save();

  /**
   * Scales the current transformation matrix by the specified amount.
   */
  void scale(float x, float y);

  /**
   * Sets the Porter-Duff composite operation to be used for all painting.
   */
  void setCompositeOperation(Composite composite);

  /**
   * Sets the color to be used for fill operations. This replaces any existing
   * fill gradient or pattern.
   */
  void setFillColor(int color);

  /**
   * Sets the gradient to be used for fill operations. This replaces any
   * existing fill color or pattern.
   */
  void setFillGradient(Gradient gradient);

  /**
   * Sets the pattern to be used for fill operations. This replaces any existing
   * fill color or gradient.
   */
  void setFillPattern(Pattern pattern);

  /**
   * Sets the line-cap mode for strokes.
   */
  void setLineCap(LineCap cap);

  /**
   * Sets the line-join mode for strokes.
   */
  void setLineJoin(LineJoin join);

  /**
   * Sets the miter limit for strokes.
   */
  void setMiterLimit(float miter);

  /**
   * Sets the color for strokes.
   */
  void setStrokeColor(int color);

  /**
   * Sets the width for strokes, in pixels.
   */
  void setStrokeWidth(float strokeWidth);

  /**
   * Sets the transformation matrix directly, replacing the existing matrix.
   */
  void setTransform(float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Strokes a circle at the specified center and radius.
   */
  void strokeCircle(float x, float y, float radius);

  /**
   * Strokes the specified path.
   */
  void strokePath(Path path);

  /**
   * Strokes the specified rectangle.
   */
  void strokeRect(float x, float y, float width, float height);

  /**
   * Multiplies the current transformation matrix by the given matrix.
   */
  void transform(float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Translates the current transformation matrix by the given amount.
   */
  void translate(float x, float y);

  /**
   * The width of this canvas.
   */
  int width();
}
