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
public class TextureLayer extends Layer {

  private Texture tex;

  /** An explicit width and height for this layer. If the width or height exceeds the underlying
    * texture width or height, it will be scaled or repeated depending on the texture's repeat
    * configuration in the pertinent axis. If either value is {@code < 0} that indicates that the
    * size of the texture being rendered should be used. */
  public float forceWidth = -1, forceHeight = -1;

  /** The subregion of the texture to render. If this is {@code null} (the default) the entire
    * texture is rendered. If {@link #forceWidth} or {@link #forceHeight} are not set, the width
    * and height of this image layer will be the width and height of the supplied region.
    *
    * <p> <em>Note:</em> when a subregion is configured, a texture will always be
    * scaled, never repeated. If you want to repeat a texture, you have to use the whole texture.
    * This is a limitation of OpenGL. */
  public Rectangle region;

  /**
   * Creates an image layer with the supplied texture.
   */
  public TextureLayer (Texture texture) {
    setTexture(texture);
  }

  /**
   * Generates {@code image}'s default texture and creates a layer with it.
   */
  public TextureLayer (Image image) {
    if (image.isLoaded()) setTexture(image.texture());
    else image.state.onSuccess(new Slot<Image>() {
      public void onEmit (Image image) { setTexture(image.texture()); }
    });
  }

  /**
   * Creates a texture layer with no texture. It will be invisible until a texture is set into it.
   */
  public TextureLayer () {} // nada!

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
  public TextureLayer setTexture (Texture tex) {
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
  public TextureLayer setTexture (RFuture<Texture> texture) {
    texture.onSuccess(new Slot<Texture>() {
      public void onEmit (Texture texture) { setTexture(texture); }
    });
    return this;
  }

  /**
   * Sets {@link #forceWidth} and {@link #forceHeight} and returns {@code this}, for convenient
   * call chaining.
   */
  public TextureLayer setSize (float width, float height) {
    forceWidth = width;
    forceHeight = height;
    return this;
  }

  /**
   * Sets {@link #region} and returns {@code this}, for convenient call chaining.
   */
  public TextureLayer setRegion (Rectangle region) {
    this.region = region;
    return this;
  }

  @Override public float width() {
    if (forceWidth >= 0) return forceWidth;
    if (region != null) return region.width;
    return (tex == null) ? 0 : tex.displayWidth;
  }

  @Override public float height() {
    if (forceHeight >= 0) return forceHeight;
    if (region != null) return region.height;
    return (tex == null) ? 0 : tex.displayHeight;
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

  @Override protected void finalize () {
    setTexture((Texture)null);
  }
}
