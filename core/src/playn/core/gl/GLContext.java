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

import pythagoras.f.MathUtil;

import playn.core.Asserts;
import playn.core.InternalTransform;
import playn.core.PlayN;
import playn.core.StockInternalTransform;

public abstract class GLContext {

  // a queue of pending actions to execute on the GL thread
  private Pender penders = null;
  private Object penderLock = new Object();

  private GLShader curShader;
  private Object lastFramebuffer;

  /** The (actual screen pixel) width and height of our default frame buffer. */
  protected int defaultFbufWidth, defaultFbufHeight;

  /** The (actual screen pixel) width and height of our current frame buffer. */
  protected int curFbufWidth, curFbufHeight;

  /** The (logical pixel) width and height of our view. */
  public int viewWidth, viewHeight;

  /** The scale factor for HiDPI mode, or 1 if HDPI mode is not enabled. */
  public final float scaleFactor;

  /**
   * Sets the view width to the specified width and height (in pixels). The framebuffer will
   * potentially be larger than this size if a HiDPI scale factor is in effect.
   */
  public void setSize (int width, int height) {
    viewWidth = width;
    viewHeight = height;
    curFbufWidth = defaultFbufWidth = scaledCeil(width);
    curFbufHeight = defaultFbufHeight = scaledCeil(height);
    viewWasResized();
  }

  /** Returns the supplied length scaled by our scale factor. */
  public float scaled(float length) {
    return scaleFactor*length;
  }

  /** Returns the supplied length scaled by our scale factor and rounded up. */
  public int scaledCeil(float length) {
    return MathUtil.iceil(scaled(length));
  }

  /** Returns the supplied length scaled by our scale factor and rounded down. */
  public int scaledFloor(float length) {
    return MathUtil.ifloor(scaled(length));
  }

  /** Returns the supplied length inverse scaled by our scale factor. */
  public float invScaled(float length) {
    return length/scaleFactor;
  }

  /** Returns the supplied length inverse scaled by our scale factor and rounded up. */
  public int invScaledCeil(float length) {
    return MathUtil.iceil(invScaled(length));
  }

  /** Returns the supplied length inverse scaled by our scale factor and rounded down. */
  public int invScaledFloor(float length) {
    return MathUtil.ifloor(invScaled(length));
  }

  /** Creates a framebuffer that will render into the supplied texture. */
  public Object createFramebuffer(Object tex) {
    flush();
    return createFramebufferImpl(tex);
  }

  /** Deletes the supplied frame buffer (which will have come from {@link #createFramebuffer}). */
  public abstract void deleteFramebuffer(Object fbuf);

  /** Creates a texture with the specified repeat behavior. */
  public abstract Object createTexture(boolean repeatX, boolean repeatY);

  /** Creates a texture of the specified size, with the specified repeat behavior, into which we
   * can subsequently render. */
  public abstract Object createTexture(int width, int height, boolean repeatX, boolean repeatY);

  /** Destroys the supplied texture. */
  public abstract void destroyTexture(Object tex);

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

  /** Processes any pending GL actions. Should be called once per frame. */
  public void processPending() {
    Pender head;
    synchronized (penderLock) {
      head = penders;
      penders = null;
    }
    if (head != null)
      head.process();
  }

  /** Queues a texture to be destroyed on the GL thread. */
  public void queueDestroyTexture(final Object tex) {
    queuePender(new Runnable() {
      public void run() {
        destroyTexture(tex);
      }
    });
  }

  /** Queues a framebuffer to be destroyed on the GL thread. */
  public void queueDeleteFramebuffer(final Object fbuf) {
    queuePender(new Runnable() {
      public void run() {
        deleteFramebuffer(fbuf);
      }
    });
  }

  /** Creates an identity transform, which may subsequently be mutated. */
  public InternalTransform createTransform() {
    return new StockInternalTransform();
  }

  public void bindFramebuffer(Object fbuf, int width, int height) {
    if ((lastFramebuffer == null && fbuf != null) || !fbuf.equals(lastFramebuffer)) {
      checkGLError("bindFramebuffer");
      flush();
      bindFramebufferImpl(lastFramebuffer = fbuf, curFbufWidth = width, curFbufHeight = height);
    }
  }

  public void bindFramebuffer() {
    bindFramebuffer(defaultFrameBuffer(), defaultFbufWidth, defaultFbufHeight);
  }

  public void drawTexture(Object tex, float texWidth, float texHeight, InternalTransform local,
                          float dw, float dh, boolean repeatX, boolean repeatY, float alpha) {
    drawTexture(tex, texWidth, texHeight, local, 0, 0, dw, dh, repeatX, repeatY, alpha);
  }

  public void drawTexture(Object tex, float texWidth, float texHeight, InternalTransform local,
                          float dx, float dy, float dw, float dh,
                          boolean repeatX, boolean repeatY, float alpha) {
    float sw = repeatX ? dw : texWidth, sh = repeatY ? dh : texHeight;
    drawTexture(tex, texWidth, texHeight, local, dx, dy, dw, dh, 0, 0, sw, sh, alpha);
  }

  public void drawTexture(Object tex, float texWidth, float texHeight, InternalTransform local,
                          float dx, float dy, float dw, float dh,
                          float sx, float sy, float sw, float sh, float alpha) {
    GLShader.Texture shader = quadTexShader();
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

  public void fillRect(InternalTransform local, float dx, float dy, float dw, float dh,
                       float texWidth, float texHeight, Object tex, float alpha) {
    GLShader.Texture shader = quadTexShader();
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

  public void fillRect(InternalTransform local, float dx, float dy, float dw, float dh,
                       int color, float alpha) {
    GLShader.Color shader = quadColorShader();
    shader.prepare(color, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillRect color prepared");
    shader.addQuad(local,
                   dx,      dy,
                   dx + dw, dy,
                   dx,      dy + dh,
                   dx + dw, dy + dh);
    checkGLError("fillRect color end");
  }

  public void fillQuad(InternalTransform local, float x1, float y1, float x2, float y2,
                       float x3, float y3, float x4, float y4,
                       float texWidth, float texHeight, Object tex, float alpha) {
    GLShader.Texture shader = quadTexShader();
    shader.prepare(tex, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillQuad tex prepared");
    shader.addQuad(local,
                   x1, y1, x1/texWidth, y1/texHeight,
                   x2, y2, x2/texWidth, y2/texHeight,
                   x3, y3, x3/texWidth, y3/texHeight,
                   x4, y4, x4/texWidth, y4/texHeight);
    checkGLError("fillQuad tex end");
  }

  public void fillQuad(InternalTransform local, float x1, float y1, float x2, float y2,
                       float x3, float y3, float x4, float y4, int color, float alpha) {
    GLShader.Color shader = quadColorShader();
    shader.prepare(color, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillQuad color prepared");
    shader.addQuad(local, x1, y1, x2, y2, x3, y3, x4, y4);
    checkGLError("fillQuad color end");
  }

  public void fillTriangles(InternalTransform local, float[] xys, int[] indices,
                            float texWidth, float texHeight, Object tex, float alpha) {
    GLShader.Texture shader = trisTexShader();
    shader.prepare(tex, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillTris tex prepared");
    shader.addTriangles(local, xys, texWidth, texHeight, indices);
    checkGLError("fillTris tex end");
  }

  public void fillTriangles(InternalTransform local, float[] xys, int[] indices,
                            int color, float alpha) {
    GLShader.Color shader = trisColorShader();
    shader.prepare(color, alpha, curFbufWidth, curFbufHeight);
    checkGLError("fillTris color prepared");
    shader.addTriangles(local, xys, 1, 1, indices);
    checkGLError("fillTris color end");
  }

  public void fillTriangles(InternalTransform local, float[] xys, float[] sxys, int[] indices,
                            Object tex, float alpha) {
    GLShader.Texture shader = trisTexShader();
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

  protected GLContext(float scaleFactor) {
    Asserts.checkArgument(scaleFactor >= 1, "Scale factor cannot be less than one.");
    this.scaleFactor = scaleFactor;
  }

  protected void viewWasResized () {
    bindFramebufferImpl(defaultFrameBuffer(), defaultFbufWidth, defaultFbufHeight);
  }

  /**
   * Returns the default framebuffer.
   */
  protected abstract Object defaultFrameBuffer();

  /**
   * Creates a framebuffer that will render into the supplied texture.
   */
  protected abstract Object createFramebufferImpl(Object tex);

  /**
   * Binds the specified framebuffer and sets the viewport to the specified dimensions.
   */
  protected abstract void bindFramebufferImpl(Object fbuf, int width, int height);

  protected abstract GLShader.Texture quadTexShader();

  protected abstract GLShader.Texture trisTexShader();

  protected abstract GLShader.Color quadColorShader();

  protected abstract GLShader.Color trisColorShader();

  private void queuePender(Runnable action) {
    synchronized (penderLock) {
      penders = new Pender(action, penders);
    }
  }

  private static class Pender {
    public final Runnable action;
    public final Pender next;

    public Pender(Runnable action, Pender next) {
      this.action = action;
      this.next = next;
    }

    public void process() {
      if (next != null)
        next.process();
      try {
        action.run();
      } catch (Throwable t) {
        PlayN.log().warn("Pending GL action choked.", t);
      }
    }
  }
}
