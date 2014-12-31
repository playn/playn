/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.scene;

import playn.core.Surface;
import pythagoras.f.Point;
import pythagoras.f.Transform;
import pythagoras.f.Vector;

/**
 * A layer whose rendering is (usually) clipped to a rectangle. The clipping rectangle is defined
 * to be the layer's {@code x, y} coordinate (as adjusted by its origin) extended to the layer's
 * scaled width and height.
 *
 * <p>NOTE: clipping rectangles cannot be rotated. If the layer has a rotation, the clipping region
 * will be undefined (and most certainly wacky).
 *
 * <p>NOTE: if a clipped layer's width or height are set to zero, it becomes unclipped. This is an
 * unfortunate implementation detail that allows {@link GroupLayer} to be clipped or not depending
 * on whether its width is configured.
 */
public abstract class ClippedLayer extends Layer {

  private final Point pos = new Point();
  private final Vector size = new Vector();
  private float width, height;

  public ClippedLayer (float width, float height) {
    this.width = width;
    this.height = height;
  }

  @Override public float width() {
    return this.width;
  }

  @Override public float height() {
    return this.height;
  }

  /** Updates the size of this group layer, and hence its clipping rectangle. */
  public ClippedLayer setSize (float width, float height) {
    this.width = width;
    this.height = height;
    return this;
  }

  /** Updates the width of this group layer, and hence its clipping rectangle. */
  public ClippedLayer setWidth(float width) {
    this.width = width;
    return this;
  }

  /** Updates the height of this group layer, and hence its clipping rectangle. */
  public ClippedLayer setHeight(float height) {
    this.height = height;
    return this;
  }

  @Override protected final void paintImpl (Surface surf) {
    // if we have zero width or height, we're not clipped
    if (width == 0 || height == 0) paintClipped(surf);
    else {
      Transform tx = surf.tx();
      tx.translate(originX, originY);
      tx.transform(pos.set(-originX, -originY), pos);
      tx.transform(size.set(width, height), size);
      tx.translate(-originX, -originY);
      boolean nonEmpty = surf.startClipped(
        (int)pos.x, (int)pos.y, Math.round(Math.abs(size.x)), Math.round(Math.abs(size.y)));
      try {
        if (nonEmpty) paintClipped(surf);
      } finally {
        surf.endClipped();
      }
    }
  }

  /**
   * Renders this layer with the clipping region in effect. NOTE: this layer's transform will
   * already have been applied to the surface.
   */
  protected abstract void paintClipped (Surface surf);
}
