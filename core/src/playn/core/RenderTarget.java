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

import static playn.core.GL20.*;

/**
 * Encapsulates an OpenGL render target (i.e. a framebuffer).
 * @see Graphics#defaultRenderTarget
 */
public abstract class RenderTarget implements Disposable {

  /** Creates a render target that renders to {@code texture}. */
  public static RenderTarget create (Graphics gfx, final Texture tex) {
    GL20 gl = gfx.gl;
    final int fb = gl.glGenFramebuffer();
    if (fb == 0) throw new RuntimeException("Failed to gen framebuffer: " + gl.glGetError());
    gl.glBindFramebuffer(GL_FRAMEBUFFER, fb);
    gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex.id, 0);
    gl.checkError("RenderTarget.create");
    return new RenderTarget (gfx) {
      public int id () { return fb; }
      public int width () { return tex.pixelWidth; }
      public int height () { return tex.pixelHeight; }
    };
  }

  /** A handle on our graphics services. */
  public final Graphics gfx;

  public RenderTarget (Graphics gfx) {
    this.gfx = gfx;
  }

  /** The framebuffer id. */
  public abstract int id ();

  /** The width of the framebuffer in pixels. */
  public abstract int width ();

  /** The height of the framebuffer in pixels. */
  public abstract int height ();

  /** Binds the framebuffer. */
  public void bind () {
    gfx.gl.glBindFramebuffer(GL_FRAMEBUFFER, id());
    gfx.gl.glViewport(0, 0, width(), height());
  }

  /** Deletes the framebuffer associated with this render target. */
  @Override public void close () {
    if (!destroyed) {
      destroyed = true;
      gfx.gl.glDeleteFramebuffer(id());
    }
  }

  @Override protected void finalize () {
    if (!destroyed) gfx.queueForDestroy(this);
  }

  private boolean destroyed;
}
