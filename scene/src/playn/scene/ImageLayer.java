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

import pythagoras.f.Rectangle;
import react.RFuture;
import react.Slot;

import playn.core.Graphics;
import playn.core.Image;
import playn.core.QuadBatch;
import playn.core.Surface;
import playn.core.Texture;

/**
 * A layer that displays a bitmapped image. By default, the layer is the same size as its
 * underlying image, but its size can be changed from that default and the layer will either scale
 * or repeat the image to cause it to fill its bounds depending on the {@link Texture} it renders.
 */
public class ImageLayer extends Layer {

  private Texture tex;

  /** An explicit width and height for this layer. If the width or height exceeds the underlying
    * texture width or height, it will be scaled or repeated depending on the texture's repeat
    * configuration in the pertinent axis. If either value is {@code < 0} that indicates that the
    * size of the texture being rendered should be used. */
  public float width = -1, height = -1;

  /** The subregion of the texture to render. If this is null, this is effectively {@code 0, 0,
    * texWidth, texHeight}. */
  public Rectangle region;

  /**
   * Creates an image layer with the supplied texture.
   */
  public ImageLayer (Texture texture) {
    setTexture(texture);
  }

  /**
   * Creates an image layer with the supplied async texture. When the texture is loaded, this layer
   * will configure itself with it and start rendering.
   */
  public ImageLayer (RFuture<Texture> texture) {
    texture.onSuccess(new Slot<Texture>() {
      public void onEmit (Texture texture) { setTexture(texture); }
    });
  }

  /**
   * Converts {@code image} into a texture and creates an image layer with it. The texture will be
   * destroyed when this layer is {@link #destroy}ed.
   */
  public ImageLayer (Graphics gfx, Image image) {
    this(gfx.createTexture(image));
  }

  /**
   * Creates an image layer with no texture. It will be invisible until a texture is set into it.
   */
  public ImageLayer () {
    // nada!
  }

  /**
   * Returns the texture rendered by this layer.
   */
  public Texture texture () {
    return tex;
  }

  /**
   * Sets the texture rendered by this layer. One can supplied {@code null} to release and clear
   * any texture currently being rendered and leave this layer in an uninitialized state. This
   * isn't something one would normally do, but could be useful if one was free-listing image
   * layers for some reason.
   */
  public ImageLayer setTexture (Texture tex) {
    // avoid releasing and rereferencing texture if nothing changes
    if (this.tex != tex) {
      if (this.tex != null) this.tex.release();
      this.tex = tex;
      if (tex != null) tex.reference();
    }
    return this;
  }

  /**
   * Sets the texture rendered by this layer to the asynchronous result of {@code texture}. When
   * the future completes, this layer's texture will be set. Until then, the current texture (if
   * any) will continue to be rendered.
   */
  public ImageLayer setTexture (RFuture<Texture> texture) {
    texture.onSuccess(new Slot<Texture>() {
      public void onEmit (Texture texture) { setTexture(texture); }
    });
    return this;
  }

  /**
   * Sets {@link #width} and {@link #height} and returns {@code this}, for convenient call
   * chaining.
   */
  public ImageLayer setSize (float width, float height) {
    this.width = width;
    this.height = height;
    return this;
  }

  @Override public float width() {
    if (width >= 0) return width;
    assert tex != null : "Texture has not yet been set";
    return tex.displayWidth;
  }

  @Override public float height() {
    if (height >= 0) return height;
    assert tex != null : "Texture has not yet been set";
    return tex.displayHeight;
  }

  @Override public void destroy() {
    super.destroy();
    setTexture((Texture)null);
  }

  @Override protected void paintImpl (Surface surf) {
    if (tex != null) {
      float dwidth = width(), dheight = height();
      if (region == null) surf.draw(tex, 0, 0, dwidth, dheight);
      else surf.draw(tex, 0, 0, dwidth, dheight, region.x, region.y, region.width, region.height);
    }
  }
}
