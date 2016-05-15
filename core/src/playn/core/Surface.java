/**
 * Copyright 2011 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0  (the "License"); you may not
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

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.AffineTransform;
import pythagoras.f.FloatMath;
import pythagoras.f.Point;
import pythagoras.f.Transforms;
import pythagoras.f.Vector;
import pythagoras.f.XY;
import pythagoras.i.Rectangle;

import react.Closeable;

/**
 * A surface provides a simple drawing API to a GPU accelerated render target. This can be either
 * the main frame buffer, or a frame buffer bound to a texture.
 *
 * <p>Note: all rendering operations to a surface must be enclosed in calls to
 * {@link Surface#begin} and {@link Surface#end}. This ensures that the batch into which
 * the surface is rendering is properly flushed to the GPU at the right times.
 */
public class Surface implements Closeable {

  private final List<AffineTransform> transformStack = new ArrayList<>();
  private final Texture colorTex;
  protected final RenderTarget target;

  private final List<Rectangle> scissors = new ArrayList<Rectangle>();
  private int scissorDepth;

  private QuadBatch batch;
  private int fillColor;
  private int tint = Tint.NOOP_TINT;
  private Texture patternTex;
  private AffineTransform lastTrans;

  private boolean checkIntersection;
  private Point intersectionTestPoint = new Point();
  private Vector intersectionTestSize = new Vector();

  /**
   * Creates a surface which will render to {@code target} using {@code defaultBatch} as its
   * default quad renderer.
   */
  public Surface (Graphics gfx, RenderTarget target, QuadBatch defaultBatch) {
    this.target = target;
    this.batch = defaultBatch;
    transformStack.add(lastTrans = new AffineTransform());
    colorTex = gfx.colorTex();
    scale(target.xscale(), target.yscale());
  }

  /**
   * Configures this surface to check the bounds of drawn {@link Tile}s to ensure that they
   * intersect our visible bounds before adding them to our GPU batch. If you draw a lot of totally
   * out of bounds images, this may increase your draw performance.
   */
  public void setCheckIntersection (boolean checkIntersection) {
    this.checkIntersection = checkIntersection;
  }

  /** Starts a series of drawing commands to this surface. */
  public Surface begin () {
    target.bind();
    beginBatch(batch);
    return this;
  }

  /** Completes a series of drawing commands to this surface. */
  public Surface end () {
    batch.end();
    return this;
  }

  /** Configures this surface to use {@code batch}, if non-null. NOOPs otherwise.
    * @return a batch which should be passed to {@link #popBatch} when rendering is done with this
    * batch. */
  public QuadBatch pushBatch (QuadBatch newBatch) {
    if (newBatch == null) return null;
    QuadBatch oldBatch = batch;
    batch.end();
    batch = beginBatch(newBatch);
    return oldBatch;
  }

  /** Restores the batch that was in effect prior to a {@link #pushBatch} call. */
  public void popBatch (QuadBatch oldBatch) {
    if (oldBatch != null) {
      batch.end();
      batch = beginBatch(oldBatch);
    }
  }

  /** Returns the current transform. */
  public AffineTransform tx () {
    return lastTrans;
  }

  /** Saves the current transform. */
  public Surface saveTx () {
    transformStack.add(lastTrans = lastTrans.copy());
    return this;
  }

  /** Restores the transform previously stored by {@link #saveTx}. */
  public Surface restoreTx () {
    int tsSize = transformStack.size();
    assert tsSize > 1 : "Unbalanced save/restore";
    transformStack.remove(--tsSize);
    lastTrans = transformStack.isEmpty() ? null : transformStack.get(tsSize-1);
    return this;
  }

  /** Starts a series of drawing commands that are clipped to the specified rectangle (in view
    * coordinates, not OpenGL coordinates). Thus must be followed by a call to {@link #endClipped}
    * when the clipped drawing commands are done.
    *
    * @return whether the resulting clip rectangle is non-empty. <em>Note:</em> the caller may wish
    * to skip their drawing if this returns false, but they must still call {@link #endClipped}. */
  public boolean startClipped (int x, int y, int width, int height) {
    batch.flush(); // flush any pending unclipped calls
    Rectangle r = pushScissorState(x, target.flip() ? target.height()-y-height : y, width, height);
    batch.gl.glScissor(r.x, r.y, r.width, r.height);
    if (scissorDepth == 1) batch.gl.glEnable(GL20.GL_SCISSOR_TEST);
    batch.gl.checkError("startClipped");
    return !r.isEmpty();
  }

  /** Ends a series of drawing commands that were clipped per a call to {@link #startClipped}. */
  public void endClipped () {
    batch.flush(); // flush our clipped calls with SCISSOR_TEST still enabled
    Rectangle r = popScissorState();
    if (r == null) batch.gl.glDisable(GL20.GL_SCISSOR_TEST);
    else batch.gl.glScissor(r.x, r.y, r.width, r.height);
    batch.gl.checkError("endClipped");
  }

  /** Translates the current transformation matrix by the given amount. */
  public Surface translate (float x, float y) {
    tx().translate(x, y);
    return this;
  }

  /** Scales the current transformation matrix by the specified amount on each axis. */
  public Surface scale (float sx, float sy) {
    tx().scale(sx, sy);
    return this;
  }

  /** Rotates the current transformation matrix by the specified angle in radians. */
  public Surface rotate (float angle) {
    float sr = (float) Math.sin(angle);
    float cr = (float) Math.cos(angle);
    transform(cr, sr, -sr, cr, 0, 0);
    return this;
  }

  /** Multiplies the current transformation matrix by the given matrix. */
  public Surface transform (float m00, float m01, float m10, float m11, float tx, float ty) {
    AffineTransform top = tx();
    Transforms.multiply(top, m00, m01, m10, m11, tx, ty, top);
    return this;
  }

  /**
   * Concatenates {@code xf} onto this surface's transform, accounting for the {@code origin}.
   */
  public Surface concatenate (AffineTransform xf, float originX, float originY) {
    AffineTransform txf = tx();
    Transforms.multiply(txf, xf.m00, xf.m01, xf.m10, xf.m11, xf.tx, xf.ty, txf);
    if (originX != 0 || originY != 0) txf.translate(-originX, -originY);
    return this;
  }

  /**
   * Pre-concatenates {@code xf} onto this surface's transform.
   */
  public Surface preConcatenate (AffineTransform xf) {
    AffineTransform txf = tx();
    Transforms.multiply(xf.m00, xf.m01, xf.m10, xf.m11, xf.tx, xf.ty, txf, txf);
    return this;
  }

  /** Returns the currently configured alpha. */
  public float alpha () {
    return Tint.getAlpha(tint);
  }

  /** Set the alpha component of this surface's current tint. Note that this value will be
    * quantized to an integer between 0 and 255. Also see {@link #setTint}.
    * <p>Values outside the range [0,1] will be clamped to the range [0,1].
    * @param alpha value in range [0,1] where 0 is transparent and 1 is opaque.
    */
  public Surface setAlpha (float alpha) {
    tint = Tint.setAlpha(tint, alpha);
    return this;
  }

  /** Returns the currently configured tint. */
  public int tint () {
    return tint;
  }

  /** Sets the tint to be applied to draw operations, as {@code ARGB}. <em>NOTE:</em> this will
    * overwrite any value configured via {@link #setAlpha}. Either include your desired alpha in
    * the high bits of {@code tint} or call {@link #setAlpha} after calling this method.
    */
  public Surface setTint (int tint) {
    this.tint = tint;
    return this;
  }

  /**
   * Combines {@code tint} with the current tint via {@link Tint#combine}.
   * @return the tint prior to combination.
   */
  public int combineTint (int tint) {
    int otint = this.tint;
    if (tint != Tint.NOOP_TINT) this.tint = Tint.combine(tint, otint);
    return otint;
  }

  /** Sets the color to be used for fill operations. This replaces any existing fill color or
    * pattern. */
  public Surface setFillColor (int color) {
    // TODO: add this to state stack
    this.fillColor = color;
    this.patternTex = null;
    return this;
  }

  /** Sets the texture to be used for fill operations. This replaces any existing fill color or
    * pattern. */
  public Surface setFillPattern (Texture texture) {
    // TODO: add fill pattern to state stack
    this.patternTex = texture;
    return this;
  }

  /** Returns whether the given rectangle intersects the render target area of this surface. */
  public boolean intersects (float x, float y, float w, float h) {
    tx().transform(intersectionTestPoint.set(x, y), intersectionTestPoint);
    tx().transform(intersectionTestSize.set(w, h), intersectionTestSize);
    float ix = intersectionTestPoint.x, iy = intersectionTestPoint.y;
    float iw = intersectionTestSize.x, ih = intersectionTestSize.y;

    if (scissorDepth > 0) {
      Rectangle scissor = scissors.get(scissorDepth - 1);
      return scissor.intersects((int)ix, (int)iy, (int)iw, (int)ih);
    }

    float tw = target.width(), th = target.height();
    return (ix + iw > 0) && (ix < tw) && (iy + ih > 0) && (iy < th);
  }

  /** Clears the entire surface to transparent blackness. */
  public Surface clear () { return clear(0, 0, 0, 0); }

  /** Clears the entire surface to the specified color.
    * The channels are values in the range {@code [0,1]}. */
  public Surface clear (float red, float green, float blue, float alpha) {
    batch.gl.glClearColor(red, green, blue, alpha);
    batch.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    return this;
  }

  /** Draws a tile at the specified location: {@code x, y}. */
  public Surface draw (Tile tile, float x, float y) {
    return draw(tile, x, y, tile.width(), tile.height());
  }

  /**
   * Draws a tile at the specified location {@code (x, y)} and size {@code (w x h)}.
   */
  public Surface draw (Tile tile, float x, float y, float w, float h) {
    if (!checkIntersection || intersects(x, y, w, h)) {
      tile.addToBatch(batch, tint, tx(), x, y, w, h);
    }
    return this;
  }

  /**
   * Draws a tile at the specified location {@code (x, y)} and size {@code (w x h)}, with tint
   * {@code tint}. <em>Note:</em> this will override any tint and alpha currently configured on
   * this surface.
   */
  public Surface draw (Tile tile, int tint, float x, float y, float w, float h) {
    if (!checkIntersection || intersects(x, y, w, h)) {
      tile.addToBatch(batch, tint, tx(), x, y, w, h);
    }
    return this;
  }

  /**
   * Draws a scaled subset of an image (defined by {@code (sx, sy)} and {@code (w x h)}) at the
   * specified location {@code (dx, dy)} and size {@code (dw x dh)}.
   */
  public Surface draw (Tile tile, float dx, float dy, float dw, float dh,
                       float sx, float sy, float sw, float sh) {
    if (!checkIntersection || intersects(dx, dy, dw, dh)) {
      tile.addToBatch(batch, tint, tx(), dx, dy, dw, dh, sx, sy, sw, sh);
    }
    return this;
  }

  /**
   * Draws a scaled subset of an image (defined by {@code (sx, sy)} and {@code (w x h)}) at the
   * specified location {@code (dx, dy)} and size {@code (dw x dh)}, with tint {@code tint}.
   * <em>Note:</em> this will override any tint and alpha currently configured on this surface.
   */
  public Surface draw (Tile tile, int tint, float dx, float dy, float dw, float dh,
                       float sx, float sy, float sw, float sh) {
    if (!checkIntersection || intersects(dx, dy, dw, dh)) {
      tile.addToBatch(batch, tint, tx(), dx, dy, dw, dh, sx, sy, sw, sh);
    }
    return this;
  }

  /**
   * Draws a texture tile, centered at the specified location.
   */
  public Surface drawCentered (Tile tile, float x, float y) {
    return draw(tile, x - tile.width()/2, y - tile.height()/2);
  }

  /**
   * Fills a line between the specified coordinates, of the specified display unit width.
   */
  public Surface drawLine (XY a, XY b, float width) {
    return drawLine(a.x(), a.y(), b.x(), b.y(), width);
  }

  /**
   * Fills a line between the specified coordinates, of the specified display unit width.
   */
  public Surface drawLine (float x0, float y0, float x1, float y1, float width) {
    // swap the line end points if x1 is less than x0
    if (x1 < x0) {
      float temp = x0;
      x0 = x1;
      x1 = temp;
      temp = y0;
      y0 = y1;
      y1 = temp;
    }

    float dx = x1 - x0, dy = y1 - y0;
    float length = FloatMath.sqrt(dx * dx + dy * dy);
    float wx = dx * (width / 2) / length;
    float wy = dy * (width / 2) / length;

    AffineTransform xf = new AffineTransform();
    xf.setRotation(FloatMath.atan2(dy, dx));
    xf.setTranslation(x0 + wy, y0 - wx);
    Transforms.multiply(tx(), xf, xf);

    if (patternTex != null) {
      batch.addQuad(patternTex, tint, xf, 0, 0, length, width);
    } else {
      batch.addQuad(colorTex, Tint.combine(fillColor, tint), xf, 0, 0, length, width);
    }
    return this;
  }

  /**
   * Fills the specified rectangle.
   */
  public Surface fillRect (float x, float y, float width, float height) {
    if (patternTex != null) {
      batch.addQuad(patternTex, tint, tx(), x, y, width, height);
    } else {
      batch.addQuad(colorTex, Tint.combine(fillColor, tint), tx(), x, y, width, height);
    }
    return this;
  }

  @Override public void close () {
    // nothing; this exists to make life easier for users of TextureSurface
  }

  private QuadBatch beginBatch (QuadBatch batch) {
    batch.begin(target.width(), target.height(), target.flip());
    return batch;
  }

  private Rectangle pushScissorState (int x, int y, int width, int height) {
    // grow the scissors buffer if necessary
    if (scissorDepth == scissors.size()) scissors.add(new Rectangle());

    Rectangle r = scissors.get(scissorDepth);
    if (scissorDepth == 0) r.setBounds(x, y, width, height);
    else {
      // intersect current with previous
      Rectangle pr = scissors.get(scissorDepth - 1);
      r.setLocation(Math.max(pr.x, x), Math.max(pr.y, y));
      r.setSize(Math.max(Math.min(pr.maxX(), x + width - 1) - r.x, 0),
                Math.max(Math.min(pr.maxY(), y + height - 1) - r.y, 0));
    }
    scissorDepth++;
    return r;
  }

  private Rectangle popScissorState () {
    scissorDepth--;
    return scissorDepth == 0 ? null : scissors.get(scissorDepth - 1);
  }

  // /**
  //  * Fills the supplied batch of triangles with the current fill color or pattern. Note: this
  //  * method is only performant on OpenGL-based backends  (Android, iOS, HTML-WebGL, etc.). On
  //  * non-OpenGL-based backends  (HTML-Canvas, HTML-Flash) it converts the triangles to a path on
  //  * every rendering call.
  //  *
  //  * @param xys the xy coordinates of the triangles, as an array: {@code [x1, y1, x2, y2, ...]}.
  //  * @param indices the index of each vertex of each triangle in the {@code xys} array.
  //  */
  // public Surface fillTriangles (float[] xys, int[] indices) {
  //   return fillTriangles(xys, 0, xys.length, indices, 0, indices.length, 0);
  // }

  // /**
  //  * Fills the supplied batch of triangles with the current fill color or pattern.
  //  *
  //  * <p>Note: this method is only performant on OpenGL-based backends  (Android, iOS, HTML-WebGL,
  //  * etc.). On non-OpenGL-based backends  (HTML-Canvas, HTML-Flash) it converts the triangles to a
  //  * path on every rendering call.</p>
  //  *
  //  * @param xys the xy coordinates of the triangles, as an array: {@code [x1, y1, x2, y2, ...]}.
  //  * @param xysOffset the offset of the coordinates array, must not be negative and no greater than
  //  * {@code xys.length}. Note: this is an absolute offset; since {@code xys} contains pairs of
  //  * values, this will be some multiple of two.
  //  * @param xysLen the number of coordinates to read, must be no less than zero and no greater than
  //  * {@code xys.length - xysOffset}. Note: this is an absolute length; since {@code xys} contains
  //  * pairs of values, this will be some multiple of two.
  //  * @param indices the index of each vertex of each triangle in the {@code xys} array. Because
  //  * this method renders a slice of {@code xys}, one must also specify {@code indexBase} which
  //  * tells us how to interpret indices. The index into {@code xys} will be computed as: {@code
  //  * 2* (indices[ii] - indexBase)}, so if your indices reference vertices relative to the whole
  //  * array you should pass {@code xysOffset/2} for {@code indexBase}, but if your indices reference
  //  * vertices relative to <em>the slice</em> then you should pass zero.
  //  * @param indicesOffset the offset of the indices array, must not be negative and no greater than
  //  * {@code indices.length}.
  //  * @param indicesLen the number of indices to read, must be no less than zero and no greater than
  //  * {@code indices.length - indicesOffset}.
  //  * @param indexBase the basis for interpreting {@code indices}. See the docs for {@code indices}
  //  * for details.
  //  */
  // public Surface fillTriangles (float[] xys, int xysOffset, int xysLen,
  //                               int[] indices, int indicesOffset, int indicesLen,
  //                               int indexBase) {
  //   GLShader shader = ctx.trisShader(this.shader);
  //   if (patternTex != null) {
  //     int tex = patternTex.ensureTexture();
  //     if (tex > 0) {
  //       shader.prepareTexture(tex, tint);
  //       shader.addTriangles(tx(), xys, xysOffset, xysLen,
  //                           patternTex.width(), patternTex.height(),
  //                           indices, indicesOffset, indicesLen, indexBase);
  //     }
  //   } else {
  //     int tex = ctx.fillImage().ensureTexture();
  //     shader.prepareTexture(tex, Tint.combine(fillColor, tint));
  //     shader.addTriangles(tx(), xys, xysOffset, xysLen, 1, 1,
  //                         indices, indicesOffset, indicesLen, indexBase);
  //   }
  //   return this;
  // }

  // /**
  //  * Fills the supplied batch of triangles with the current fill pattern.
  //  *
  //  * <p>Note: this method only honors the texture coordinates on OpenGL-based backends  (Anrdoid,
  //  * iOS, HTML-WebGL, etc.). On non-OpenGL-based backends  (HTML-Canvas, HTML-Flash) it behaves like
  //  * a call to {@link #fillTriangles (float[],int[])}.</p>
  //  *
  //  * @param xys see {@link #fillTriangles (float[],int[])}.
  //  * @param sxys the texture coordinates for each vertex of the triangles, as an array:
  //  * {@code [sx1, sy1, sx2, sy2, ...]}. This must be the same length as {@code xys}.
  //  * @param indices see {@link #fillTriangles (float[],int[])}.
  //  *
  //  * @throws IllegalStateException if no fill pattern is currently set.
  //  */
  // public Surface fillTriangles (float[] xys, float[] sxys, int[] indices) {
  //   return fillTriangles(xys, sxys, 0, xys.length, indices, 0, indices.length, 0);
  // }

  // /**
  //  * Fills the supplied batch of triangles with the current fill pattern.
  //  *
  //  * <p>Note: this method only honors the texture coordinates on OpenGL-based backends  (Anrdoid,
  //  * iOS, HTML-WebGL, etc.). On non-OpenGL-based backends  (HTML-Canvas, HTML-Flash) it behaves like
  //  * a call to {@link #fillTriangles (float[],int[])}.</p>
  //  *
  //  * @param xys see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
  //  * @param sxys the texture coordinates for each vertex of the triangles, as an array.
  //  * {@code [sx1, sy1, sx2, sy2, ...]}. This must be the same length as {@code xys}.
  //  * @param xysOffset see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
  //  * @param xysLen see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
  //  * @param indices see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
  //  * @param indicesOffset see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
  //  * @param indicesLen see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
  //  * @param indexBase see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
  //  *
  //  * @throws IllegalStateException if no fill pattern is currently set.
  //  */
  // public Surface fillTriangles (float[] xys, float[] sxys, int xysOffset, int xysLen,
  //                               int[] indices, int indicesOffset, int indicesLen,
  //                               int indexBase) {
  //   if (patternTex == null) throw new IllegalStateException("No fill pattern currently set");
  //   int tex = patternTex.ensureTexture();
  //   if (tex > 0) {
  //     GLShader shader = ctx.trisShader(this.shader).prepareTexture(tex, tint);
  //     shader.addTriangles(tx(), xys, sxys, xysOffset, xysLen,
  //                         indices, indicesOffset, indicesLen, indexBase);
  //   }
  //   return this;
  // }
}
