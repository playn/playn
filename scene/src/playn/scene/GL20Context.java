/**
 * Copyright 2012 The PlayN Authors
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
package playn.core.gl;

import java.nio.ByteBuffer;

import pythagoras.i.Rectangle;

import playn.core.Image;
import playn.core.InternalTransform;
import playn.core.AbstractPlatform;
import playn.core.Tint;
import static playn.core.gl.GL20.*;

/**
 * A {@link GLContext} implementation based on {@link GL20}.
 */
public class GL20Context extends GLContext {

  public final GL20 gl;

  private final boolean checkErrors;
  private final InternalTransform rootXform;
  private int minFilter = GL_LINEAR, magFilter = GL_LINEAR;
  private GLShader quadShader, trisShader;

  public GL20Context(AbstractPlatform platform, GL20 gl, float scaleFactor, boolean checkErrors) {
    super(platform, scaleFactor);
    this.gl = gl;
    this.checkErrors = checkErrors;
    // create our root transform with our scale factor
    rootXform = createTransform();
    rootXform.uniformScale(scaleFactor);
  }

  public void init() {
    gl.glDisable(GL_CULL_FACE);
    gl.glEnable(GL_BLEND);
    gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    gl.glClearColor(0, 0, 0, 1);
    if (quadShader != null) {
      quadShader.clearProgram();
    }
    if (trisShader != null) {
      trisShader.clearProgram();
    }
    quadShader = createQuadShader();
    trisShader = new IndexedTrisShader(this);
    checkGLError("initGL");
  }

  public void paint(GroupLayerGL rootLayer) {
    if (rootLayer.size() > 0) {
      checkGLError("paint");
      bindFramebuffer();
      gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear to transparent
      rootLayer.paint(rootXform, Tint.NOOP_TINT, null); // paint all the layers
      useShader(null); // flush any pending shader
    }
    if (STATS_ENABLED) stats.frames++;
  }

  @Override
  public InternalTransform rootTransform() {
    return rootXform;
  }

  @Override
  public void setTextureFilter (Filter minFilter, Filter magFilter) {
    this.minFilter = toGL(minFilter);
    this.magFilter = toGL(magFilter);
  }

  @Override
  public String getString(int param) {
    return gl.glGetString(param);
  }

  @Override
  public int getInteger(int param) {
    return gl.glGetInteger(param);
  }

  @Override
  public float getFloat(int param) {
    return gl.glGetFloat(param);
  }

  @Override
  public boolean getBoolean(int param) {
    return gl.glGetBoolean(param);
  }

  @Override
  public void texImage2D(Image image, int target, int level, int internalformat, int format,
                         int type) {
    gl.glTexImage2D(target, level, internalformat, (int) image.width(),  (int) image.height(), 0,
                    format, type, getRgba(image));
  }

  @Override
  public void texSubImage2D(Image image, int target, int level, int xOffset, int yOffset, int format,
                            int type) {
    gl.glTexSubImage2D(target, level, xOffset, yOffset, (int) image.width(),  (int) image.height(),
                       format, type, getRgba(image));
  }

  @Override
  public GLProgram createProgram(String vertShader, String fragShader) {
    if (STATS_ENABLED) stats.shaderCreates++;
    return new GL20Program(this, gl, vertShader, fragShader);
  }

  @Override
  public GLBuffer.Float createFloatBuffer(int capacity) {
    return new GL20Buffer.FloatImpl(gl, capacity);
  }

  @Override
  public GLBuffer.Short createShortBuffer(int capacity) {
    return new GL20Buffer.ShortImpl(gl, capacity);
  }

  @Override
  public void deleteFramebuffer(int fbuf) {
    gl.glDeleteFramebuffers(1, new int[] { fbuf }, 0);
  }

  @Override
  public int createTexture(boolean repeatX, boolean repeatY, boolean mipmaps) {
    int[] tex = new int[1];
    gl.glGenTextures(1, tex, 0);
    gl.glBindTexture(GL_TEXTURE_2D, tex[0]);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mipmapify(minFilter, mipmaps));
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, repeatX ? GL_REPEAT : GL_CLAMP_TO_EDGE);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, repeatY ? GL_REPEAT : GL_CLAMP_TO_EDGE);
    if (STATS_ENABLED) stats.texCreates++;
    return tex[0];
  }

  @Override
  public int createTexture(int width, int height,
                           boolean repeatX, boolean repeatY, boolean mm) {
    int tex = createTexture(repeatX, repeatY, mm);
    gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                    (ByteBuffer) null);
    return tex;
  }

  @Override
  public void generateMipmap(int tex) {
    gl.glBindTexture(GL_TEXTURE_2D, tex);
    gl.glGenerateMipmap(GL_TEXTURE_2D);
  }

  @Override
  public void activeTexture(int glTextureN) {
    gl.glActiveTexture(glTextureN);
  }

  @Override
  public void bindTexture(int tex) {
    // TODO: track last bound texture, and avoid calling if it didn't change?
    gl.glBindTexture(GL_TEXTURE_2D, tex);
    if (STATS_ENABLED) stats.texBinds++;
  }

  @Override
  public void destroyTexture(int tex) {
    flush(); // flush in case this texture is queued up to be drawn
    gl.glDeleteTextures(1, new int[] { tex }, 0);
  }

  @Override
  public boolean startClipped(int x, int y, int width, int height) {
    flush(); // flush any pending unclipped calls
    Rectangle r = pushScissorState(x, curFbufHeight - y - height, width, height);
    gl.glScissor(r.x, r.y, r.width, r.height);
    if (getScissorDepth() == 1) gl.glEnable(GL_SCISSOR_TEST);
    return !r.isEmpty();
  }

  @Override
  public void endClipped() {
    flush(); // flush our clipped calls with SCISSOR_TEST still enabled
    Rectangle r = popScissorState();
    if (r == null) gl.glDisable(GL_SCISSOR_TEST);
    else gl.glScissor(r.x, r.y, r.width, r.height);
  }

  @Override
  public void clear(float r, float g, float b, float a) {
    gl.glClearColor(r, g, b, a);
    gl.glClear(GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void checkGLError(String op) {
    if (checkErrors) {
      int error;
      while ((error = gl.glGetError()) != GL_NO_ERROR) {
        platform.log().warn(this.getClass().getName() + " -- " + op + ": glError " + error);
      }
    }
  }

  @Override
  protected int defaultFramebuffer() {
    return 0;
  }

  @Override
  protected int createFramebufferImpl(int tex) {
    // generate the framebuffer and attach the texture
    int[] fbuf = new int[1];
    gl.glGenFramebuffers(1, fbuf, 0);
    gl.glBindFramebuffer(GL_FRAMEBUFFER, fbuf[0]);
    gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex, 0);
    if (STATS_ENABLED) stats.frameBufferCreates++;
    return fbuf[0];
  }

  @Override
  protected void bindFramebufferImpl(int fbuf, int width, int height) {
    gl.glBindFramebuffer(GL_FRAMEBUFFER, fbuf);
    gl.glViewport(0, 0, width, height);
    if (STATS_ENABLED) stats.frameBufferBinds++;
  }

  @Override
  protected GLShader quadShader() {
    return quadShader;
  }
  @Override
  protected GLShader trisShader() {
    return trisShader;
  }

  private static int toGL(Filter filter) {
    switch (filter) {
    default:
    case  LINEAR: return GL_LINEAR;
    case NEAREST: return GL_NEAREST;
    }
  }

  private static int mipmapify (int filter, boolean mipmaps) {
    if (!mipmaps)
      return filter;
    // we don't do trilinear filtering (i.e. GL_LINEAR_MIPMAP_LINEAR);
    // it's expensive and not super useful when only rendering in 2D
    switch (filter) {
    case GL_NEAREST: return GL_NEAREST_MIPMAP_NEAREST;
    case GL_LINEAR: return GL_LINEAR_MIPMAP_NEAREST;
    default: return filter;
    }
  }
}
