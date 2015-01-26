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
package playn.scene;

import pythagoras.f.IDimension;
import pythagoras.f.Rectangle;
import react.RFuture;
import react.Slot;

import playn.core.Surface;
import playn.core.Texture;
import playn.core.Tile;
import playn.core.TileSource;

/**
 * A layer that displays a texture or region of a texture (tile). By default, the layer is the same
 * size as its source, but its size can be changed from that default and the layer will either
 * scale or repeat the texture to cause it to fill its bounds depending on the {@link Texture} it
 * renders.
 */
public class ImageLayer extends Layer {

  private Tile tile;

  /** An explicit width and height for this layer. If the width or height exceeds the underlying
    * tile width or height, it will be scaled or repeated depending on the tile texture's repeat
    * configuration in the pertinent axis. If either value is {@code < 0} that indicates that the
    * size of the tile being rendered should be used.
    *
    * <p>Note: if you use these sizes in conjunction with a logical origin, you must set them via
    * {@link #setSize} to cause the origin to be recomputed. */
  public float forceWidth = -1, forceHeight = -1;

  /** The subregion of the tile to render. If this is {@code null} (the default) the entire tile is
    * rendered. If {@link #forceWidth} or {@link #forceHeight} are not set, the width and height of
    * this image layer will be the width and height of the supplied region.
    *
    * <p> <em>Note:</em> when a subregion is configured, a texture will always be scaled, never
    * repeated. If you want to repeat a texture, you have to use the whole texture. This is a
    * limitation of OpenGL.
    *
    * <p>Note: if you use this region in conjunction with a logical origin, you must set it via
    * {@link #setRegion} to cause the origin to be recomputed. */
  public Rectangle region;

  /**
   * Creates an image layer with the supplied texture tile.
   */
  public ImageLayer (Tile tile) {
    setTile(tile);
  }

  /**
   * Obtains the tile from {@code source}, asynchronously if necessary, and displays it. If the
   * source is not ready, this layer will display nothing until it becomes ready and delivers its
   * tile.
   */
  public ImageLayer (TileSource source) {
    setSource(source);
  }

  /**
   * Creates a texture layer with no texture. It will be invisible until a texture is set into it.
   */
  public ImageLayer () {} // nada!

  /**
   * Returns the tile rendered by this layer.
   */
  public Tile tile () {
    return tile;
  }

  /**
   * Sets the texture rendered by this layer. One can supplied {@code null} to release and clear
   * any texture currently being rendered and leave this layer in an uninitialized state. This
   * isn't something one would normally do, but could be useful if one was free-listing image
   * layers for some reason.
   */
  public ImageLayer setTile (Tile tile) {
    // avoid releasing and rereferencing texture if nothing changes
    if (this.tile != tile) {
      if (this.tile != null) this.tile.texture().release();
      this.tile = tile;
      checkOrigin();
      if (tile != null) tile.texture().reference();
    }
    return this;
  }

  /**
   * Sets the texture rendered by this layer to the texture provided by {@code source}. If {@code
   * source} is not yet ready, the texture will be set when it becomes ready. Until then any
   * previous texture will continue to be displayed.
   */
  public ImageLayer setSource (TileSource source) {
    if (source.isLoaded()) setTile(source.tile());
    else source.tileAsync().onSuccess(new Slot<Tile>() {
      public void onEmit (Tile tile) { setTile(tile); }
    });
    return this;
  }

  /**
   * Sets the tile rendered by this layer to the asynchronous result of {@code tile}. When the
   * future completes, this layer's tile will be set. Until then, the current tile (if any) will
   * continue to be rendered.
   */
  public ImageLayer setTile (RFuture<? extends Tile> tile) {
    tile.onSuccess(new Slot<Tile>() {
      public void onEmit (Tile tile) { setTile(tile); }
    });
    return this;
  }

  /** Sets {@link #forceWidth} and {@link #forceHeight}.
    * @return {@code this}, for convenient call chaining. */
  public ImageLayer setSize (float width, float height) {
    forceWidth = width;
    forceHeight = height;
    checkOrigin();
    return this;
  }

  /** Sets {@link #forceWidth} and {@link #forceHeight}.
    * @return {@code this}, for convenient call chaining. */
  public ImageLayer setSize (IDimension size) { return setSize(size.width(), size.height()); }

  /** Sets {@link #region}.
    * @return {@code this}, for convenient call chaining. */
  public ImageLayer setRegion (Rectangle region) {
    this.region = region;
    checkOrigin();
    return this;
  }

  @Override public float width() {
    if (forceWidth >= 0) return forceWidth;
    if (region != null) return region.width;
    return (tile == null) ? 0 : tile.width();
  }

  @Override public float height() {
    if (forceHeight >= 0) return forceHeight;
    if (region != null) return region.height;
    return (tile == null) ? 0 : tile.height();
  }

  @Override public void close() {
    super.close();
    setTile((Tile)null);
  }

  @Override protected void paintImpl (Surface surf) {
    if (tile != null) {
      float dwidth = width(), dheight = height();
      if (region == null) surf.draw(tile, 0, 0, dwidth, dheight);
      else surf.draw(tile, 0, 0, dwidth, dheight, region.x, region.y, region.width, region.height);
    }
  }

  @Override protected void finalize () {
    setTile((Tile)null);
  }
}
