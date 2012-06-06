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

import playn.core.InternalTransform;
import playn.core.Platform;
import playn.core.StockInternalTransform;

public abstract class GLContext {

  private final Platform platform;
  private GLShader curShader;
  private int lastFramebuffer;

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

  /** Creates an identity transform, which may subsequently be mutated. */
  public InternalTransform createTransform() {
    return new StockInternalTransform();
  }

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

  public void drawTexture(GLShader.Texture shader, int tex, float texWidth, float texHeight,
                          InternalTransform local, float dw, float dh,
                          boolean repeatX, boolean repeatY, float alpha) {
    drawTexture(shader, tex, texWidth, texHeight, local, 0, 0, dw, dh, repeatX, repeatY, alpha);
  }

  public void drawTexture(GLShader.Texture shader, int tex, float texWidth, float texHeight,
                          InternalTransform local, float dx, float dy, float dw, float dh,
                          boolean repeatX, boolean repeatY, float alpha) {
    float sw = repeatX ? dw : texWidth, sh = repeatY ? dh : texHeight;
    drawTexture(shader, tex, texWidth, texHeight, local, dx, dy, dw, dh, 0, 0, sw, sh, alpha);
  }

  public void drawTexture(GLShader.Texture shader, int tex, float texWidth, float texHeight,
                          InternalTransform local, float dx, float dy, float dw, float dh,
                          float sx, float sy, float sw, float sh, float alpha) {
    if (shader == null) shader = quadTexShader();
    shader.prepare(tex, alpha, curFbufWidth, curFbufHeight);
    checkGLError("drawTexture texture prepared");
    sx /= texWidth; sy /= texHeight;
    sw /= texWidth; sh /= texHeight;
    shader.addQuad(local,
                   dx,      dy,      sx,      sy,
                   dx + dw, dy,      sx + sw, sy,
                   dx,      dy + dh, sx,      sy + sh,
                   dx + dw, dy + dh, sx + sw, sy + sh);
    checkGLError("drawTexture end");
  }

  public void fillRect(GLShader.Texture shader, InternalTransform local,
                       float dx, float dy, float dw, float dh, float texWidth, float texHeight,
                       int tex, float alpha) {
    if (shader == null) shader = quadTexShader();
    shader.prepare(tex, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillRect tex prepared");
    float sx = dx / texWidth, sy = dy / texHeight;
    float sw = dw / texWidth, sh = dh / texHeight;
    shader.addQuad(local,
                   dx,      dy,      sx,      sy,
                   dx + dw, dy,      sx + sw, sy,
                   dx,      dy + dh, sx,      sy + sh,
                   dx + dw, dy + dh, sx + sw, sy + sh);
    checkGLError("fillRect tex end");
  }

  public void fillRect(GLShader.Color shader, InternalTransform local,
                       float dx, float dy, float dw, float dh, int color, float alpha) {
    if (shader == null) shader = quadColorShader();
    shader.prepare(color, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillRect color prepared");
    shader.addQuad(local,
                   dx,      dy,
                   dx + dw, dy,
                   dx,      dy + dh,
                   dx + dw, dy + dh);
    checkGLError("fillRect color end");
  }

  public void fillQuad(GLShader.Texture shader, InternalTransform local, float x1, float y1,
                       float x2, float y2, float x3, float y3, float x4, float y4,
                       float texWidth, float texHeight, int tex, float alpha) {
    if (shader == null) shader = quadTexShader();
    shader.prepare(tex, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillQuad tex prepared");
    shader.addQuad(local,
                   x1, y1, x1/texWidth, y1/texHeight,
                   x2, y2, x2/texWidth, y2/texHeight,
                   x3, y3, x3/texWidth, y3/texHeight,
                   x4, y4, x4/texWidth, y4/texHeight);
    checkGLError("fillQuad tex end");
  }

  public void fillQuad(GLShader.Color shader, InternalTransform local, float x1, float y1,
                       float x2, float y2, float x3, float y3, float x4, float y4,
                       int color, float alpha) {
    if (shader == null) shader = quadColorShader();
    shader.prepare(color, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillQuad color prepared");
    shader.addQuad(local, x1, y1, x2, y2, x3, y3, x4, y4);
    checkGLError("fillQuad color end");
  }

  public void fillTriangles(GLShader.Texture shader, InternalTransform local,
                            float[] xys, int[] indices, float texWidth, float texHeight,
                            int tex, float alpha) {
    if (shader == null) shader = trisTexShader();
    shader.prepare(tex, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillTris tex prepared");
    shader.addTriangles(local, xys, texWidth, texHeight, indices);
    checkGLError("fillTris tex end");
  }

  public void fillTriangles(GLShader.Color shader, InternalTransform local,
                            float[] xys, int[] indices, int color, float alpha) {
    if (shader == null) shader = trisColorShader();
    shader.prepare(color, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillTris color prepared");
    shader.addTriangles(local, xys, 1, 1, indices);
    checkGLError("fillTris color end");
  }

  public void fillTriangles(GLShader.Texture shader, InternalTransform local,
                            float[] xys, float[] sxys, int[] indices, int tex, float alpha) {
    if (shader == null) shader = trisTexShader();
    shader.prepare(tex, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillTris tex prepared");
    shader.addTriangles(local, xys, sxys, indices);
    checkGLError("fillTris tex end");
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
  public boolean useShader(GLShader shader) {
    if (curShader == shader)
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

  protected abstract GLShader.Texture quadTexShader();

  protected abstract GLShader.Texture trisTexShader();

  protected abstract GLShader.Color quadColorShader();

  protected abstract GLShader.Color trisColorShader();
}
