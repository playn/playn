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

import pythagoras.f.IDimension;

import react.RFuture;

import playn.core.Canvas;
import playn.core.Graphics;
import playn.core.Image;
import playn.core.Texture;
import playn.core.Tile;

/**
 * Simplifies the process of displaying a {@link Canvas} which is updated after its initial
 * creation. When modifying the canvas, one must call {@link #begin} to obtain a reference to the
 * canvas, do the desired rendering, then call {@link #end} to upload the modified image data to
 * the GPU for display by this layer.
 */
public class CanvasLayer extends ImageLayer {

  private final Graphics gfx;
  private Canvas canvas;

  /**
   * Creates a canvas layer with a backing canvas of {@code size} (in display units). This layer
   * will display nothing until a {@link #begin}/{@link #end} pair is used to render something to
   * its backing canvas.
   */
  public CanvasLayer (Graphics gfx, IDimension size) { this(gfx, size.width(), size.height()); }

  /**
   * Creates a canvas layer with a backing canvas of size {@code width x height} (in display
   * units). This layer will display nothing until a {@link #begin}/{@link #end} pair is used to
   * render something to its backing canvas.
   */
  public CanvasLayer (Graphics gfx, float width, float height) {
    this.gfx = gfx;
    resize(width, height);
  }

  /**
   * Creates a canvas layer with the supplied backing canvas. The canvas will immediately be
   * uploaded to the GPU for display.
   */
  public CanvasLayer (Graphics gfx, Canvas canvas) {
    this.gfx = gfx;
    this.canvas = canvas;
    super.setTile(canvas.image.createTexture(Texture.Config.DEFAULT));
  }

  /**
   * Resizes the canvas that is displayed by this layer.
   *
   * <p>Note: this throws away the old canvas and creates a new blank canvas with the desired size.
   * Thus this should immediately be followed by a {@link #begin}/{@link #end} pair which updates
   * the contents of the new canvas. Until then, it will display the old image data.
   */
  public void resize (float width, float height) {
    if (canvas != null) canvas.close();
    canvas = gfx.createCanvas(width, height);
  }

  /** Starts a drawing operation on this layer's backing canvas. Thus must be follwed by a call to
    * {@link #end} when the drawing is complete. */
  public Canvas begin () {
    return canvas;
  }

  /** Informs this layer that a drawing operation has just completed. The backing canvas image data
    * is uploaded to the GPU. */
  public void end () {
    Texture tex = (Texture)tile();
    Image image = canvas.image;
    // if our texture is already the right size, just update it
    if (tex != null && tex.pixelWidth == image.pixelWidth() &&
        tex.pixelHeight == image.pixelHeight()) tex.update(image);
    // otherwise we need to create a new texture (setTexture will unreference the old texture which
    // will cause it to be destroyed)
    else super.setTile(canvas.image.createTexture(Texture.Config.DEFAULT));
  }

  @Override public ImageLayer setTile (Tile tile) {
    if (tile == null || tile instanceof Texture) return super.setTile(tile);
    else throw new UnsupportedOperationException();
  }
  @Override public ImageLayer setTile (RFuture<? extends Tile> tile) {
    throw new UnsupportedOperationException();
  }

  @Override public float width () {
    return (forceWidth < 0) ? canvas.width : forceWidth;
  }

  @Override public float height () {
    return (forceHeight < 0) ? canvas.height : forceHeight;
  }

  @Override public void close () {
    super.close();
    if (canvas != null) {
      canvas.close();
      canvas = null;
    }
  }
}
