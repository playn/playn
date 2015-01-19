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

import react.RFuture;

/**
 * Provides a {@link Tile}, potentially asynchronously. This provides a uniform API for obtaining a
 * {@link Texture} or {@link Tile} directly from an instance thereof, or from an {@link Image}
 * which provides the {@link Texture} or an {@code Image.Region (TODO)} which provides the
 * {@link Tile}.
 */
public abstract class TileSource {

  /** Returns whether this tile source is loaded and ready to provide its tile. */
  public abstract boolean isLoaded ();

  /** Returns the tile provided by this source.
    * @throws IllegalStateException if {@code !isLoaded()}. */
  public abstract Tile tile ();

  /** Delivers the tile provided by this source once the source is loaded. */
  public abstract RFuture<Tile> tileAsync ();
}
