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
package playn.core;

import pythagoras.f.AffineTransform;
import pythagoras.f.Points;

import react.RFuture;

/**
 * Represents a square region of a texture. This makes it easy to render tiles from texture
 * atlases.
 */
public abstract class Tile extends TileSource {

  /** The texture which contains this tile. */
  public abstract Texture texture ();
  /** The width of this tile (in display units). */
  public abstract float width ();
  /** The height of this tile (in display units). */
  public abstract float height ();

  /** Returns the {@code s} texture coordinate for the x-axis. */
  public abstract float sx ();
  /** Returns the {@code s} texture coordinate for the y-axis. */
  public abstract float sy ();
  /** Returns the {@code t} texture coordinate for the x-axis. */
  public abstract float tx ();
  /** Returns the {@code t} texture coordinate for the y-axis. */
  public abstract float ty ();

  /** Adds this tile to the supplied quad batch. */
  public abstract void addToBatch (QuadBatch batch, int tint, AffineTransform tx,
                                   float x, float y, float width, float height);

  /** Adds this tile to the supplied quad batch. */
  public abstract void addToBatch (QuadBatch batch, int tint, AffineTransform tx,
                                   float dx, float dy, float dw, float dh,
                                   float sx, float sy, float sw, float sh);

  @Override public boolean isLoaded () { return true; }
  @Override public Tile tile () { return this; }
  @Override public RFuture<Tile> tileAsync () { return RFuture.success(this); }

  @Override public String toString () {
    return "Tile[" + width() + "x" + height() +
      "/" + Points.pointToString(sx(), sy()) + "/" + Points.pointToString(tx(), ty()) +
      "] <- " + texture();
  }
}
