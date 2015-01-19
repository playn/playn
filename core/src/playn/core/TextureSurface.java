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

/**
 * A {@link Surface} which renders to a {@link Texture} instead of to the default frame buffer.
 *
 * <p>Note: a {@code TextureSurface} makes use of three GPU resources: a framebuffer, a quad batch
 * and a texture. The framebuffer's lifecycle is tied to the lifecycle of the {@code
 * TextureSurface}. When you {@link close} it the framebuffer is disposed.
 *
 * <p>The quad batch's lifecycle is independent of the {@code TextureSurface}. Most likely you will
 * use the default quad batch for your game which lives for the lifetime of your game.
 *
 * <p>The texture's lifecycle is also independent of the {@code TextureSurface} and is managed by
 * reference counting. The texture is neither referenced, nor released by the {@code
 * TextureSurface}. It is assumed that the texture will be stuffed into an {@code ImageLayer} or
 * used for rendering elsewhere and that code will manage the texture's lifecycle (even if the
 * texture is created by {@code TextureSurface} in the first place).
 */
public class TextureSurface extends Surface {

  /** The texture into which we're rendering. */
  public final Texture texture;

  /** Creates a texture surface which is {@code width x height} in display units.
    * A managed backing texture will be automatically created. */
  public TextureSurface (Graphics gfx, QuadBatch defaultBatch, float width, float height) {
    this(gfx, defaultBatch, gfx.createTexture(width, height, Texture.Config.DEFAULT));
  }

  /** Creates a texture surface which renders to {@code texture}. */
  public TextureSurface (Graphics gfx, QuadBatch defaultBatch, Texture texture) {
    super(gfx, RenderTarget.create(gfx, texture), defaultBatch);
    this.texture = texture;
  }

  @Override public void close () {
    super.close();
    target.close();
  }
}
