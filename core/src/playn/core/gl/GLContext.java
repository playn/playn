/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core.gl;

import playn.core.Asserts;
import playn.core.InternalTransform;
import playn.core.Platform;
import playn.core.StockInternalTransform;

public abstract class GLContext {

  /** Used to configure texture image scaling. */
  public static enum Filter { LINEAR, NEAREST };

  protected final Platform platform;
  private GLShader curShader;
  private int lastFramebuffer;
  private int pushedFramebuffer = -1, pushedWidth, pushedHeight;

  /** The (actual screen pixel) width and height of our default frame buffer. */
  protected int defaultFbufWidth, defaultFbufHeight;

  /** The (actual screen pixel) width and height of our current frame buffer. */
  protected int curFbufWidth, curFbufHeight;

  /** The (logical pixel) width and height of our view. */
  public int viewWidth, viewHeight;

  /** The scale factor for HiDPI mode, or 1 if HDPI mode is not enabled. */
  public final Scale scale;

  /**
   * Sets the view width to the specified width and height (in pixels). The framebuffer will
   * potentially be larger than this size if a HiDPI scale factor is in effect.
   */
  public void setSize (int width, int height) {
    viewWidth = width;
    viewHeight = height;
    curFbufWidth = defaultFbufWidth = scale.scaledCeil(width);
    curFbufHeight = defaultFbufHeight = scale.scaledCeil(height);
    viewWasResized();
  }

  /**
   * Configures the filter function used when rendering scaled textures.
   *
   * @param minFilter the scaling to use when rendering textures that are scaled down.
   * @param magFilter the scaling to use when rendering textures that are scaled up.
   */
  public abstract void setTextureFilter (Filter minFilter, Filter magFilter);

  /** Returns the specified GL integer parameter. */
  public abstract int getInteger(int param);

  /** Returns the specified GL float parameter. */
  public abstract float getFloat(int param);

  /** Returns the specified GL boolean parameter. */
  public abstract boolean getBoolean(int param);

  /**
   * Creates a shader program, for use by a single {@link GLShader}.
   * @param vertShader the source code for the vertex shader.
   * @param fragShader the source code for the fragment shader.
   */
  public abstract GLProgram createProgram(String vertShader, String fragShader);

  /**
   * Creates a float buffer with the specified initial capacity.
   */
  public abstract GLBuffer.Float createFloatBuffer(int capacity);

  /**
   * Creates a short buffer with the specified initial capacity.
   */
  public abstract GLBuffer.Short createShortBuffer(int capacity);

  /** Creates a framebuffer that will render into the supplied texture. */
  public int createFramebuffer(int tex) {
    flush();
    return createFramebufferImpl(tex);
  }

  /** Deletes the supplied frame buffer (which will have come from {@link #createFramebuffer}). */
  public abstract void deleteFramebuffer(int fbuf);

  /** Creates a texture with the specified repeat behavior. */
  public abstract int createTexture(boolean repeatX, boolean repeatY);

  /** Creates a texture of the specified size, with the specified repeat behavior, into which we
   * can subsequently render. */
  public abstract int createTexture(int width, int height, boolean repeatX, boolean repeatY);

  /** Activates the specified texture unit.
   * @param glTextureN the texture unit to active (e.g. {@link GL20#GL_TEXTURE0}). */
  public abstract void activeTexture(int glTextureN);

  /** Binds the specified texture. */
  public abstract void bindTexture(int tex);

  /** Destroys the supplied texture. */
  public abstract void destroyTexture(int tex);

  /** Starts a series of drawing commands that are clipped to the specified rectangle (in view
   * coordinates, not OpenGL coordinates). Thus must be followed by a call to {@link #endClipped}
   * when the clipped drawing commands are done. */
  public abstract void startClipped(int x, int y, int width, int height);

  /** Ends a series of drawing commands that were clipped per a call to {@link #startClipped}. */
  public abstract void endClipped();

  /** Clears the bound framebuffer with the specified color. */
  public abstract void clear(float r, float g, float b, float a);

  /** NOOP except when debugging, checks and logs whether any GL errors have occurred. */
  public abstract void checkGLError(String op);

  /** Queues a texture to be destroyed on the GL thread. */
  public void queueDestroyTexture(final int tex) {
    platform.invokeLater(new Runnable() {
      public void run() {
        destroyTexture(tex);
      }
    });
  }

  /** Queues a framebuffer to be destroyed on the GL thread. */
  public void queueDeleteFramebuffer(final int fbuf) {
    platform.invokeLater(new Runnable() {
      public void run() {
        deleteFramebuffer(fbuf);
      }
    });
  }

  /** Queues a custom shader to be cleaned up on the GL thread. */
  public void queueClearShader(final GLShader shader) {
    platform.invokeLater(new Runnable() {
      public void run() {
        shader.clearProgram();
      }
    });
  }

  /** Creates an identity transform, which may subsequently be mutated. */
  public InternalTransform createTransform() {
    return new StockInternalTransform();
  }

  /** Returns the root transform which converts scale-independent coordinates into pixels. On some
   * platforms this may also handle screen rotation. Do not modify! */
  public abstract InternalTransform rootTransform();

  public void bindFramebuffer(int fbuf, int width, int height) {
    if (fbuf != lastFramebuffer) {
      checkGLError("bindFramebuffer");
      flush();
      bindFramebufferImpl(lastFramebuffer = fbuf, curFbufWidth = width, curFbufHeight = height);
    }
  }

  public void bindFramebuffer() {
    bindFramebuffer(defaultFrameBuffer(), defaultFbufWidth, defaultFbufHeight);
  }

  /** Stores the metadata for the currently bound frame buffer, and binds the supplied framebuffer.
   * This must be followed by a call to {@link #popFramebuffer}. Also, it is not allowed to push a
   * framebuffer if a framebuffer is already pushed. Only one level of nesting is supported. */
  public void pushFramebuffer(int fbuf, int width, int height) {
    Asserts.checkState(pushedFramebuffer == -1, "Already have a pushed framebuffer");
    pushedFramebuffer = lastFramebuffer;
    pushedWidth = curFbufWidth;
    pushedHeight = curFbufHeight;
    bindFramebuffer(fbuf, width, height);
  }

  /** Pops the framebuffer pushed by a previous call to {@link #pushFramebuffer} and restores the
   * framebuffer that was active prior to that call. */
  public void popFramebuffer() {
    Asserts.checkState(pushedFramebuffer != -1, "Have no pushed framebuffer");
    bindFramebuffer(pushedFramebuffer, pushedWidth, pushedHeight);
    pushedFramebuffer = 0;
  }

  /** Returns the supplied shader if non-null, or the default quad shader if null. */
  public GLShader quadShader (GLShader custom) {
    return custom == null ? quadShader() : custom;
  }

  /** Returns the supplied shader if non-null, or the default triangles shader if null. */
  public GLShader trisShader (GLShader custom) {
    return custom == null ? trisShader() : custom;
  }

  public void flush() {
    if (curShader != null) {
      checkGLError("flush()");
      curShader.flush();
      curShader = null;
    }
  }

  /**
   * Makes the supplied shader the current shader, flushing any previous shader.
   */
  public boolean useShader(GLShader shader, boolean forceFlush) {
    if (curShader == shader && !forceFlush)
      return false;
    checkGLError("useShader");
    flush();
    curShader = shader;
    return true;
  }

  protected GLContext(Platform platform, float scaleFactor) {
    this.scale = new Scale(scaleFactor);
    this.platform = platform;
  }

  protected void viewWasResized () {
    bindFramebufferImpl(defaultFrameBuffer(), defaultFbufWidth, defaultFbufHeight);
  }

  /**
   * Returns the default framebuffer.
   */
  protected abstract int defaultFrameBuffer();

  /**
   * Creates a framebuffer that will render into the supplied texture.
   */
  protected abstract int createFramebufferImpl(int tex);

  /**
   * Binds the specified framebuffer and sets the viewport to the specified dimensions.
   */
  protected abstract void bindFramebufferImpl(int fbuf, int width, int height);

  protected abstract GLShader quadShader();
  protected abstract GLShader trisShader();
}
