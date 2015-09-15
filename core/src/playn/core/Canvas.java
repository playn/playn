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

import react.Closeable;

/**
 * A 2D drawing canvas. Rendering is performed by the CPU into a bitmap.
 */
public abstract class Canvas implements Closeable {

  /**
   * Values that may be used with
   * {@link Canvas#setCompositeOperation(Composite)}.
   */
  public static enum Composite {
    /** A (B is ignored). Display the source image instead of the destination image.
      * {@code [Sa, Sc]} */
    SRC,

    /** B atop A. Same as source-atop but using the destination image instead of the source image
      * and vice versa. {@code [Sa, Sa * Dc + Sc * (1 - Da)]}. */
    DST_ATOP,

    /** A over B. Display the source image wherever the source image is opaque. Display the
      * destination image elsewhere. {@code [Sa + (1 - Sa)*Da, Rc = Sc + (1 - Sa)*Dc]}. */
    SRC_OVER,

    /** B over A. Same as source-over but using the destination image instead of the source image
      * and vice versa. {@code [Sa + (1 - Sa)*Da, Rc = Dc + (1 - Da)*Sc]}. */
    DST_OVER,

    /** A in B. Display the source image wherever both the source image and destination image are
      * opaque. Display transparency elsewhere. {@code [Sa * Da, Sc * Da]}. */
    SRC_IN,

    /** B in A. Same as source-in but using the destination image instead of the
      * source image and vice versa. {@code [Sa * Da, Sa * Dc]}. */
    DST_IN,

    /** A out B. Display the source image wherever the source image is opaque and the destination
      * image is transparent. Display transparency elsewhere.
      * {@code [Sa * (1 - Da), Sc * (1 - Da)]}. */
    SRC_OUT,

    /** B out A. Same as source-out but using the destination image instead of
      * the source image and vice versa. {@code [Da * (1 - Sa), Dc * (1 - Sa)]}. */
    DST_OUT,

    /** A atop B. Display the source image wherever both images are opaque. Display the destination
      * image wherever the destination image is opaque but the source image is transparent. Display
      * transparency elsewhere. {@code [Da, Sc * Da + (1 - Sa) * Dc]}. */
    SRC_ATOP,

    /** A xor B. Exclusive OR of the source image and destination image.
      * {@code [Sa + Da - 2 * Sa * Da, Sc * (1 - Da) + (1 - Sa) * Dc]}. */
    XOR,

    /** A * B. Multiplies the source and destination images. <b>NOTE:</b> this is not supported by
      * the HTML5 and Flash backends. {@code [Sa * Da, Sc * Dc]}. */
    MULTIPLY
  }

  /**
   * Values that may be used with {@link Canvas#setLineCap(LineCap)}.
   */
  public static enum LineCap { BUTT, ROUND, SQUARE }

  /**
   * Values that may be used with {@link Canvas#setLineJoin(LineJoin)}.
   */
  public static enum LineJoin { BEVEL, MITER, ROUND }

  /** Facilitates drawing images and image regions to a canvas. */
  public interface Drawable {
    float width ();
    float height ();
    void draw (Object gc, float x, float y, float width, float height);
    void draw (Object gc, float dx, float dy, float dw, float dh,
               float sx, float sy, float sw, float sh);
  }

  /** The image that underlies this canvas. */
  public final Image image;

  /** The width of this canvas. */
  public final float width;

  /** The height of this canvas. */
  public final float height;

  /**
   * Returns an immutable snapshot of the image that backs this canvas. Subsequent changes to this
   * canvas will not be reflected in the returned image. If you are going to render a canvas
   * image into another canvas image a lot, using a snapshot can improve performance.
   */
  public abstract Image snapshot ();

  /**
   * Informs the platform that this canvas, and its backing image will no longer be used. On some
   * platforms this can free up memory earlier than if we waited for the canvas to be garbage
   * collected.
   */
  @Override public void close () {} // nada by default

  /** Clears the entire canvas to {@code rgba(0, 0, 0, 0)}. */
  public abstract Canvas clear ();

  /** Clears the specified region to {@code rgba (0, 0, 0, 0)}. */
  public abstract Canvas clearRect (float x, float y, float width, float height);

  /** Intersects the current clip with the specified path. */
  public abstract Canvas clip (Path clipPath);

  /** Intersects the current clip with the supplied rectangle. */
  public abstract Canvas clipRect (float x, float y, float width, float height);

  /** Creates a path object. */
  public abstract Path createPath ();

  /** Creates a gradient fill pattern. */
  public abstract Gradient createGradient (Gradient.Config config);

  /**
   * Draws {@code image} at the specified location {@code (x, y)}.
   */
  public Canvas draw (Drawable image, float x, float y) {
    return draw(image, x, y, image.width(), image.height());
  }

  /**
   * Draws {@code image} centered at the specified location. Subtracts {@code image.width/2} from x
   * and {@code image.height/2} from y.
   */
  public Canvas drawCentered (Drawable image, float x, float y) {
    return draw(image, x - image.width()/2, y - image.height()/2);
  }

  /**
   * Draws a scaled image at the specified location {@code (x, y)} size {@code (w x h)}.
   */
  public Canvas draw (Drawable image, float x, float y, float w, float h) {
    image.draw(gc(), x, y, w, h);
    isDirty = true;
    return this;
  }

  /**
   * Draws a subregion of a image {@code (sw x sh) @ (sx, sy)} at the specified size
   * {@code (dw x dh)} and location {@code (dx, dy)}.
   *
   * TODO (jgw): Document whether out-of-bounds source coordinates clamp, repeat, or do nothing.
   */
  public Canvas draw (Drawable image, float dx, float dy, float dw, float dh,
                      float sx, float sy, float sw, float sh) {
    image.draw(gc(), dx, dy, dw, dh, sx, sy, sw, sh);
    isDirty = true;
    return this;
  }

  /**
   * Draws a line between the two specified points.
   */
  public abstract Canvas drawLine (float x0, float y0, float x1, float y1);

  /**
   * Draws a single point at the specified location.
   */
  public abstract Canvas drawPoint (float x, float y);

  /**
   * Draws text at the specified location. The text will be drawn in the current fill color.
   */
  public abstract Canvas drawText (String text, float x, float y);

  /**
   * Fills a circle at the specified center and radius.
   */
  public abstract Canvas fillCircle (float x, float y, float radius);

  /**
   * Fills the specified path.
   */
  public abstract Canvas fillPath (Path path);

  /**
   * Fills the specified rectangle.
   */
  public abstract Canvas fillRect (float x, float y, float width, float height);

  /**
   * Fills the specified rounded rectangle.
   *
   * @param x the x coordinate of the upper left of the rounded rectangle.
   * @param y the y coordinate of the upper left of the rounded rectangle.
   * @param width the width of the rounded rectangle.
   * @param height the width of the rounded rectangle.
   * @param radius the radius of the circle to use for the corner.
   */
  public abstract Canvas fillRoundRect (float x, float y, float width, float height, float radius);

  /**
   * Fills the text at the specified location. The text will use the current fill color.
   */
  public abstract Canvas fillText (TextLayout text, float x, float y);

  /**
   * Restores the canvas's previous state.
   *
   * @see #save ()
   */
  public abstract Canvas restore ();

  /**
   * Rotates the current transformation matrix by the specified angle in radians.
   */
  public abstract Canvas rotate (float radians);

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
  public abstract Canvas save ();

  /**
   * Scales the current transformation matrix by the specified amount.
   */
  public abstract Canvas scale (float x, float y);

  /**
   * Set the global alpha value to be used for all painting.
   * <p>
   * Values outside the range [0,1] will be clamped to the range [0,1].
   *
   * @param alpha alpha value in range [0,1] where 0 is transparent and 1 is opaque
   */
  public abstract Canvas setAlpha (float alpha);

  /**
   * Sets the Porter-Duff composite operation to be used for all painting.
   */
  public abstract Canvas setCompositeOperation (Composite composite);

  /**
   * Sets the color to be used for fill operations. This replaces any existing
   * fill gradient or pattern.
   */
  public abstract Canvas setFillColor (int color);

  /**
   * Sets the gradient to be used for fill operations. This replaces any
   * existing fill color or pattern.
   */
  public abstract Canvas setFillGradient (Gradient gradient);

  /**
   * Sets the pattern to be used for fill operations. This replaces any existing
   * fill color or gradient.
   */
  public abstract Canvas setFillPattern (Pattern pattern);

  /**
   * Sets the line-cap mode for strokes.
   */
  public abstract Canvas setLineCap (LineCap cap);

  /**
   * Sets the line-join mode for strokes.
   */
  public abstract Canvas setLineJoin (LineJoin join);

  /**
   * Sets the miter limit for strokes.
   */
  public abstract Canvas setMiterLimit (float miter);

  /**
   * Sets the color for strokes.
   */
  public abstract Canvas setStrokeColor (int color);

  /**
   * Sets the width for strokes, in pixels.
   */
  public abstract Canvas setStrokeWidth (float strokeWidth);

  /**
   * Strokes a circle at the specified center and radius.
   */
  public abstract Canvas strokeCircle (float x, float y, float radius);

  /**
   * Strokes the specified path.
   */
  public abstract Canvas strokePath (Path path);

  /**
   * Strokes the specified rectangle.
   */
  public abstract Canvas strokeRect (float x, float y, float width, float height);

  /**
   * Strokes the specified rounded rectangle.
   *
   * @param x the x coordinate of the upper left of the rounded rectangle.
   * @param y the y coordinate of the upper left of the rounded rectangle.
   * @param width the width of the rounded rectangle.
   * @param height the width of the rounded rectangle.
   * @param radius the radius of the circle to use for the corner.
   */
  public abstract Canvas strokeRoundRect (float x, float y, float width, float height,
                                          float radius);

  /**
   * Strokes the text at the specified location. The text will use the current stroke configuration
   *  (color, width, etc.).
   */
  public abstract Canvas strokeText (TextLayout text, float x, float y);

  /** Calls {@link #toTexture(Texture.Config)} with the default texture config. */
  public Texture toTexture () { return toTexture(Texture.Config.DEFAULT); }

  /** A helper function for creating a texture from this canvas's image, and then disposing this
    * canvas. This is useful for situations where you create a canvas, draw something in it, turn
    * it into a texture and then never use the canvas again. */
  public Texture toTexture (Texture.Config config) {
    try { return image.createTexture(config); }
    finally { close(); }
  }

  /**
   * Multiplies the current transformation matrix by the given matrix.
   */
  public abstract Canvas transform (float m11, float m12, float m21, float m22, float dx, float dy);

  /**
   * Translates the current transformation matrix by the given amount.
   */
  public abstract Canvas translate (float x, float y);

  /** Used to track modifications to our underlying image. */
  protected boolean isDirty;

  protected final Graphics gfx;

  protected Canvas (Graphics gfx, Image image) {
    this.gfx = gfx;
    this.image = image;
    this.width = image.width();
    this.height = image.height();
    if (width <= 0 || height <= 0) throw new IllegalArgumentException(
      "Canvas must be > 0 in width and height: " + width + "x" + height);
  }

  /** Returns the platform dependent graphics context for this canvas. */
  protected abstract Object gc ();
}
