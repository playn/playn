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
 * Combines a texture, a default quad batch, and a surface into a reasonably easy to use package.
 *
 * <p>Note: a {@code SurfaceTexture} makes use of three GPU resources: a framebuffer, a quad batch
 * and a texture. The framebuffer's lifecycle is tied to the lifecycle of the {@code
 * SurfaceTexture}. When you {@link close} the {@code SurfaceTexture} the framebuffer is destroyed.
 *
 * <p>The quad batch's lifecycle is independent of the {@code SurfaceTexture}. Most likely you will
 * use the default quad batch for your game which lives for the lifetime of your game.
 *
 * <p>The texture's lifecycle is also independent of the {@code SurfaceTexture} and is managed by
 * reference counting. The texture is neither referenced, nor released by the {@code
 * SurfaceTexture}. It is assumed that the texture will be stuffed into an {@code ImageLayer} or
 * used for rendering elsewhere and that code will manage the texture's lifecycle (even if the
 * texture is created by {@code SurfaceTexture} in the first place).
 */
public class SurfaceTexture implements Disposable {

  private final Surface surface;
  private final QuadBatch batch;
  private final RenderTarget target;

  /** The texture into which we're rendering. */
  public final Texture texture;

  /**
   * Begins a drawing session to this texture's surface.
   * This must be paired with a call to {@link #end} when drawing is complete.
   */
  public Surface begin () {
    target.bind();
    surface.beginBatch(batch);
    return surface;
  }

  /** Completes a drawing session.
    * @return {@code this} for easy chaining to a call to {@link #close} for one-shot drawing. */
  public SurfaceTexture end () {
    surface.endBatch();
    return this;
  }

  /** Creates a surface texture which is {@code width x height} in display units. A (managed)
    * backing texture will be automatically created. */
  public SurfaceTexture (Graphics gfx, QuadBatch batch, float width, float height) {
    this(gfx, batch, gfx.createTexture(width, height, true, false));
  }

  /** Creates a surface texture which renders to {@code texture}. */
  public SurfaceTexture (Graphics gfx, QuadBatch batch, Texture texture) {
    this.texture = texture;
    this.batch = batch;
    this.target = RenderTarget.create(gfx, texture);
    this.surface = new Surface(gfx, target);
  }

  @Override public void close () {
    target.close();
  }
}
