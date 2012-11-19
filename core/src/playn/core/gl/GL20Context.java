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

import playn.core.InternalTransform;
import playn.core.Platform;
import static playn.core.PlayN.log;
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

  public GL20Context(Platform platform, GL20 gl, float scaleFactor, boolean checkErrors) {
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

  public void preparePaint(GroupLayerGL rootLayer) {
    checkGLError("preparePaint");
    if (rootLayer.size() > 0) {
      bindFramebuffer();
      gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear to transparent
    }
  }

  public void paintLayers(GroupLayerGL rootLayer) {
    checkGLError("paintLayers");
    bindFramebuffer();
    rootLayer.paint(rootXform, 1, null); // paint all the layers
    useShader(null, false); // flush any pending shader
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
  public GLProgram createProgram(String vertShader, String fragShader) {
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
  public int createTexture(boolean repeatX, boolean repeatY) {
    int[] tex = new int[1];
    gl.glGenTextures(1, tex, 0);
    gl.glBindTexture(GL_TEXTURE_2D, tex[0]);
    gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
    gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
    gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, repeatX ? GL_REPEAT : GL_CLAMP_TO_EDGE);
    gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, repeatY ? GL_REPEAT : GL_CLAMP_TO_EDGE);
    return tex[0];
  }

  @Override
  public int createTexture(int width, int height, boolean repeatX, boolean repeatY) {
    int tex = createTexture(repeatX, repeatY);
    gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                    (ByteBuffer) null);
    return tex;
  }

  @Override
  public void activeTexture(int glTextureN) {
    gl.glActiveTexture(glTextureN);
  }

  @Override
  public void bindTexture(int tex) {
    // TODO: track last bound texture, and avoid calling if it didn't change?
    gl.glBindTexture(GL_TEXTURE_2D, tex);
  }

  @Override
  public void destroyTexture(int tex) {
    flush(); // flush in case this texture is queued up to be drawn
    gl.glDeleteTextures(1, new int[] { tex }, 0);
  }

  @Override
  public void startClipped(int x, int y, int width, int height) {
    flush(); // flush any pending unclipped calls
    gl.glScissor(x, curFbufHeight - y - height, width, height);
    gl.glEnable(GL_SCISSOR_TEST);
  }

  @Override
  public void endClipped() {
    flush(); // flush our clipped calls with SCISSOR_TEST still enabled
    gl.glDisable(GL_SCISSOR_TEST);
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
        log().error(this.getClass().getName() + " -- " + op + ": glError " + error);
      }
    }
  }

  @Override
  protected int defaultFrameBuffer() {
    return 0;
  }

  @Override
  protected int createFramebufferImpl(int tex) {
    // generate the framebuffer and attach the texture
    int[] fbuf = new int[1];
    gl.glGenFramebuffers(1, fbuf, 0);
    gl.glBindFramebuffer(GL_FRAMEBUFFER, fbuf[0]);
    gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex, 0);
    return fbuf[0];
  }

  @Override
  protected void bindFramebufferImpl(int fbuf, int width, int height) {
    gl.glBindFramebuffer(GL_FRAMEBUFFER, fbuf);
    gl.glViewport(0, 0, width, height);
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
}
