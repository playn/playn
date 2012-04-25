/**
 * Copyright 2011 The PlayN Authors
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
package playn.android;

import static playn.core.PlayN.log;
import playn.core.gl.GLShader;

import java.util.*;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

import playn.core.Asserts;
import playn.core.StockInternalTransform;
import playn.core.gl.GL20;
import playn.core.gl.GLContext;
import playn.core.gl.GroupLayerGL;

/**
 * Implements the GL context via Android OpenGL bindings.
 */
public class AndroidGLContext extends GLContext
{
  /** An interface implemented by entities that need to store things when our GL context is lost
   * and restore them when we are given a new context. */
  public interface Refreshable {
    /** Called when our GL context is about to go away. */
    void onSurfaceLost();
    /** Called when we have been given a new GL context. */
    void onSurfaceCreated();
  }

  public static final boolean CHECK_ERRORS = false;

  private GLShader.Texture texShader;
  private GLShader.Color colorShader;

  final AndroidGL20 gl20;

  private Map<Refreshable, Void> refreshables =
    Collections.synchronizedMap(new WeakHashMap<Refreshable, Void>());

  // Debug
  private int texCount;

  AndroidGLContext(float scaleFactor, AndroidGL20 gfx, int screenWidth, int screenHeight) {
    super(scaleFactor);
    gl20 = gfx;
    setSize(screenWidth, screenHeight);
    reinitGL();
  }

  void onSurfaceCreated() {
    reinitGL();
    for (Refreshable ref : refreshables.keySet()) {
      ref.onSurfaceCreated();
    }
  }

  void onSurfaceLost() {
    for (Refreshable ref : refreshables.keySet()) {
      ref.onSurfaceLost();
    }
  }

  void preparePaint() {
    // Clear to transparent
    bindFramebuffer();
    gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
  }

  void paintLayers(GroupLayerGL rootLayer) {
    checkGLError("updateLayers start");
    // Paint all the layers
    bindFramebuffer();
    rootLayer.paint(StockInternalTransform.IDENTITY, 1);
    checkGLError("updateLayers end");
    // Guarantee a flush
    useShader(null);
  }

  void updateTexture(int texture, Bitmap image) {
    gl20.glBindTexture(GL20.GL_TEXTURE_2D, texture);
    GLUtils.texImage2D(GL20.GL_TEXTURE_2D, 0, image, 0);
    checkGLError("updateTexture end");
  }

  @Override
  public void deleteFramebuffer(Object fbuf) {
    gl20.glDeleteFramebuffers(1, new int[] {(Integer) fbuf}, 0);
  }

  @Override
  public Integer createTexture(boolean repeatX, boolean repeatY) {
    int[] texId = new int[1];
    gl20.glGenTextures(1, texId, 0);
    int texture = texId[0];
    gl20.glBindTexture(GL20.GL_TEXTURE_2D, texture);
    gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
    gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
    gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, repeatX ? GL20.GL_REPEAT
        : GL20.GL_CLAMP_TO_EDGE);
    gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, repeatY ? GL20.GL_REPEAT
        : GL20.GL_CLAMP_TO_EDGE);
    ++texCount;
    if (AndroidPlatform.DEBUG_LOGS) log().debug(texCount + " textures created.");
    return texture;
  }

  @Override
  public Integer createTexture(int width, int height, boolean repeatX, boolean repeatY) {
    int tex = createTexture(repeatX, repeatY);
    gl20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, width, height, 0, GL20.GL_RGBA,
                      GL20.GL_UNSIGNED_BYTE, null);
    return tex;
  }

  @Override
  public void destroyTexture(Object tex) {
    flush(); // flush in case this texture is queued up to be drawn
    gl20.glDeleteTextures(1, new int[] {(Integer) tex}, 0);
    --texCount;
    if (AndroidPlatform.DEBUG_LOGS) log().debug(texCount + " textures remain.");
  }

  @Override
  public void startClipped(int x, int y, int width, int height) {
    flush(); // flush any pending unclipped calls
    gl20.glScissor(x, curFbufHeight-y-height, width, height);
    gl20.glEnable(GL20.GL_SCISSOR_TEST);
  }

  @Override
  public void endClipped() {
    flush(); // flush our clipped calls with SCISSOR_TEST still enabled
    gl20.glDisable(GL20.GL_SCISSOR_TEST);
  }

  @Override
  public void clear(float r, float g, float b, float a) {
    gl20.glClearColor(r, g, b, a);
    gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void checkGLError(String op) {
    if (CHECK_ERRORS) {
      int error;
      while ((error = gl20.glGetError()) != GL20.GL_NO_ERROR) {
        log().error(this.getClass().getName() + " -- " + op + ": glError " + error);
      }
    }
  }

  @Override
  protected Object defaultFrameBuffer() {
    return 0;
  }

  @Override
  protected Integer createFramebufferImpl(Object tex) {
    // Generate the framebuffer and attach the texture
    int[] fbufBuffer = new int[1];
    gl20.glGenFramebuffers(1, fbufBuffer, 0);

    int fbuf = fbufBuffer[0];
    gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, fbuf);
    gl20.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0,
                                GL20.GL_TEXTURE_2D, (Integer) tex, 0);
    return fbuf;
  }

  @Override
  protected void bindFramebufferImpl(Object frameBuffer, int width, int height) {
    gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, (Integer) frameBuffer);
    gl20.glViewport(0, 0, width, height);
  }

  @Override
  protected GLShader.Texture quadTexShader() {
    return texShader;
  }
  @Override
  protected GLShader.Texture trisTexShader() {
    return texShader;
  }
  @Override
  protected GLShader.Color quadColorShader() {
    return colorShader;
  }
  @Override
  protected GLShader.Color trisColorShader() {
    return colorShader;
  }

  void addRefreshable(Refreshable ref) {
    refreshables.put(Asserts.checkNotNull(ref), null);
  }

  void removeRefreshable(Refreshable ref) {
    refreshables.remove(Asserts.checkNotNull(ref));
  }

  private void reinitGL() {
    gl20.glDisable(GL20.GL_CULL_FACE);
    gl20.glEnable(GL20.GL_BLEND);
    gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
    gl20.glClearColor(0, 0, 0, 1);
    texShader = new AndroidGLShader.Texture(this);
    colorShader = new AndroidGLShader.Color(this);
  }
}
