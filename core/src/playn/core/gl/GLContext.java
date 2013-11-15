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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import pythagoras.i.Rectangle;

import playn.core.Asserts;
import playn.core.Image;
import playn.core.InternalTransform;
import playn.core.Platform;
import playn.core.StockInternalTransform;

public abstract class GLContext {

  /** Used to configure texture image scaling. */
  public static enum Filter { LINEAR, NEAREST }

  /** Used to track and report rendering statistics. */
  public static class Stats {
    public int frames;

    public int shaderCreates;
    public int frameBufferCreates;
    public int texCreates;

    public int shaderBinds;
    public int frameBufferBinds;
    public int texBinds;

    public int quadsRendered;
    public int trisRendered;
    public int shaderFlushes;

    /** Resets all counters. */
    public void reset() {
      frames = 0;
      shaderCreates = 0;
      frameBufferCreates = 0;
      texCreates = 0;
      shaderBinds = 0;
      frameBufferBinds = 0;
      texBinds = 0;
      quadsRendered = 0;
      trisRendered = 0;
      shaderFlushes = 0;
    }
  }

  protected static final boolean STATS_ENABLED = true;
  protected final Stats stats = new Stats();

  protected final Platform platform;
  private GLShader curShader;
  private int lastFramebuffer, epoch;
  private int pushedFramebuffer = -1, pushedWidth, pushedHeight;
  private List<Rectangle> scissors = new ArrayList<Rectangle>();
  private int scissorDepth;

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
  public final void setSize(int width, int height) {
    viewWidth = width;
    viewHeight = height;
    curFbufWidth = defaultFbufWidth = scale.scaledCeil(width);
    curFbufHeight = defaultFbufHeight = scale.scaledCeil(height);
    viewConfigChanged();
  }

  /**
   * Configures the filter function used when rendering scaled textures.
   *
   * @param minFilter the scaling to use when rendering textures that are scaled down.
   * @param magFilter the scaling to use when rendering textures that are scaled up.
   */
  public abstract void setTextureFilter(Filter minFilter, Filter magFilter);

  /** Returns the specified GL string parameter. */
  public abstract String getString(int param);

  /** Returns the specified GL integer parameter. */
  public abstract int getInteger(int param);

  /** Returns the specified GL float parameter. */
  public abstract float getFloat(int param);

  /** Returns the specified GL boolean parameter. */
  public abstract boolean getBoolean(int param);

  /**
   * See http://www.khronos.org/opengles/sdk/docs/man/xhtml/glTexImage2D.xml
   *
   * <p>The default implementation is based on {@link Image#getRgb} and will hand over an RGBA byte
   * array. Please set the (internal)format and type parameters accordingly; they are mainly
   * present for future support of different formats. The WebGL implementation will pass through
   * all parameters.</p>
   */
  public void texImage2D(Image image, int target, int level, int internalformat, int format,
                         int type) {
    throw new UnsupportedOperationException();
  }

  /**
   * See http://www.khronos.org/opengles/sdk/docs/man/xhtml/glTexSubImage2D.xml
   *
   * <p>The default implementation is based on {@link Image#getRgb} and will hand over a RGBA byte
   * array. Please set the (internal)format and type parameters accordingly; they are mainly
   * present for future support of different formats. The WebGL implementation will pass through
   * all parameters.</p>
   */
  public void texSubImage2D(Image image, int target, int level, int xOffset, int yOffset, int format,
                            int type) {
    throw new UnsupportedOperationException();
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

  /** Creates a framebuffer that will render into the supplied texture. <em>NOTE:</em> this must be
   * followed immediately by a call to {@link #bindFramebuffer(int,int,int)} or {@link
   * #pushFramebuffer}. */
  public int createFramebuffer(int tex) {
    flush();
    return createFramebufferImpl(tex);
  }

  /** Deletes the supplied frame buffer (which will have come from {@link #createFramebuffer}). */
  public abstract void deleteFramebuffer(int fbuf);

  /** Creates a texture with the specified repeat behavior. */
  public abstract int createTexture(boolean repeatX, boolean repeatY, boolean mipmaps);

  /** Creates a texture of the specified size, with the specified repeat behavior, into which we
   * can subsequently render. */
  public abstract int createTexture(int width, int height,
                                    boolean repeatX, boolean repeatY, boolean mipmaps);

  /** Generates mipmaps for the specified texture. */
  public abstract void generateMipmap(int tex);

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
    pushedFramebuffer = -1;
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
      flush(false);
  }

  public void flush(boolean deactivate) {
    if (curShader != null) {
      checkGLError("flush()");
      curShader.flush();
      if (deactivate) curShader.deactivate();
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
    flush(true);
    curShader = shader;
    return true;
  }

  /**
   * Returns the current rendering stats. These will be all zeros unless the library was compiled
   * with stats enabled (which is not the default).
   */
  public Stats stats() {
    return stats;
  }

  /**
   * Returns debugging info on the quad shader. Useful for performance analysis.
   */
  public String quadShaderInfo() {
    return String.valueOf(quadShader());
  }

  /**
   * Returns debugging info on the triangles shader. Useful for performance analysis.
   */
  public String trisShaderInfo() {
    return String.valueOf(trisShader());
  }

  /**
   * Adds the given rectangle to the scissors stack, intersecting with the previous one if it
   * exists. Intended for use by subclasses to implement {@link #startClipped} and {@link
   * #endClipped}.
   *
   * <p>NOTE: calls to this method <b>must</b> be matched by a corresponding call {@link
   * #popScissorState}, or all hell will break loose.</p>
   *
   * @return the new clipping rectangle to use
   */
  protected Rectangle pushScissorState (int x, int y, int width, int height) {
      // grow the scissors buffer if necessary
      if (scissorDepth == scissors.size()) {
        scissors.add(new Rectangle());
      }

      Rectangle r = scissors.get(scissorDepth);
      if (scissorDepth == 0) {
        r.setBounds(x, y, width, height);
      } else {
        // intersect current with previous
        Rectangle pr = scissors.get(scissorDepth - 1);
        r.setLocation(Math.max(pr.x, x), Math.max(pr.y, y));
        r.setSize(Math.min(pr.maxX(), x + width - 1) - r.x,
            Math.min(pr.maxY(), y + height - 1) - r.y);
      }
      scissorDepth++;
      return r;
  }

  /**
   * Removes the most recently pushed scissor state and returns the rectangle that should now
   * be used for clipping, or null if clipping should be disabled.
   */
  protected Rectangle popScissorState () {
      scissorDepth--;
      return scissorDepth == 0 ? null : scissors.get(scissorDepth - 1);
  }

  /**
   * Returns the current scissor stack size. Zero means no scissors are currently pushed.
   */
  protected int getScissorDepth () {
      return scissorDepth;
  }

  protected GLContext(Platform platform, float scaleFactor) {
    this.scale = new Scale(scaleFactor);
    this.platform = platform;
  }

  protected void viewConfigChanged () {
    bindFramebufferImpl(defaultFrameBuffer(), defaultFbufWidth, defaultFbufHeight);
  }

  /**
   * Increments our GL context epoch. This should be called by platform backends when the GL
   * context has been lost and a new one created.
   */
  protected void incrementEpoch () {
    ++epoch;
  }

  /**
   * Returns the current GL context epoch. This is used to invalidate shaders when we lose and
   * regain our GL context.
   */
  protected int epoch () {
    return epoch;
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

  protected boolean shouldTryQuadShader() {
    return QuadShader.isLikelyToPerform(this);
  }

  protected GLShader createQuadShader() {
    if (shouldTryQuadShader()) {
      try {
        GLShader quadShader = new QuadShader(this);
        quadShader.createCores(); // force core creation to test whether it fails
        return quadShader;
      } catch (Throwable t) {
        platform.log().warn("Failed to create QuadShader: " + t);
      }
    }
    return new IndexedTrisShader(this);
  }

  // used by GLContext.tex(Sub)Image2D impls
  protected static ByteBuffer getRgba(Image image) {
    int w = (int) image.width(), h = (int) image.height(), size = w * h;
    int[] rawPixels = new int[size];
    ByteBuffer pixels = ByteBuffer.allocateDirect(size * 4);
    pixels.order(ByteOrder.nativeOrder());
    IntBuffer rgba = pixels.asIntBuffer();
    image.getRgb(0, 0, w, h, rawPixels, 0, w);

    for (int i = 0; i < size; i++) {
      int argb = rawPixels[i];
      // Order is inverted because this is read as a byte array, and we store intel ints.
      rgba.put(i, ((argb >> 16) & 0x0ff) | (argb & 0x0ff00ff00) | ((argb & 0xff) << 16));
    }
    return pixels;
  }

  protected abstract GLShader quadShader();
  protected abstract GLShader trisShader();
}
