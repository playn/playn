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
package playn.java;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import pythagoras.f.MathUtil;

import playn.core.Asserts;
import playn.core.StockInternalTransform;
import playn.core.gl.GLContext;
import playn.core.gl.GLShader;
import playn.core.gl.GroupLayerGL;
import static playn.core.PlayN.*;

/**
 * Implements the GL context via LWJGL bindings.
 */
class JavaGLContext extends GLContext {

  public static final boolean CHECK_ERRORS = Boolean.getBoolean("playn.glerrors");

  private StockInternalTransform rootXform;
  private GLShader.Texture texShader;
  private GLShader.Color colorShader;

  JavaGLContext(float scaleFactor, int screenWidth, int screenHeight) {
    super(scaleFactor);
    setSize(screenWidth, screenHeight);
    // create our root transform with our scale factor
    rootXform = new StockInternalTransform();
    rootXform.uniformScale(scaleFactor);
  }

  @Override
  public void deleteFramebuffer(Object fbuf) {
    glDeleteFramebuffersEXT((Integer) fbuf);
  }

  @Override
  public Object createTexture(boolean repeatX, boolean repeatY) {
    int texture = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, repeatX ? GL_REPEAT : GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, repeatY ? GL_REPEAT : GL_CLAMP_TO_EDGE);
    return texture;
  }

  @Override
  public Object createTexture(int width, int height, boolean repeatX, boolean repeatY) {
    int tex = (Integer) createTexture(repeatX, repeatY);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                 (ByteBuffer) null);
    return tex;
  }

  @Override
  public void destroyTexture(Object tex) {
    flush(); // flush in case this texture is queued up to be drawn
    glDeleteTextures((Integer) tex);
  }

  @Override
  public void startClipped(int x, int y, int width, int height) {
    flush(); // flush any pending unclipped calls
    glScissor(x, curFbufHeight - y - height, width, height);
    glEnable(GL_SCISSOR_TEST);
  }

  @Override
  public void endClipped() {
    flush(); // flush our clipped calls with SCISSOR_TEST still enabled
    glDisable(GL_SCISSOR_TEST);
  }

  @Override
  public void clear(float r, float g, float b, float a) {
    glClearColor(r, g, b, a);
    glClear(GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void checkGLError(String op) {
    if (CHECK_ERRORS) {
      int error;
      while ((error = glGetError()) != GL_NO_ERROR) {
        log().error(this.getClass().getName() + " -- " + op + ": glError " + error);
      }
    }
  }

  @Override
  protected void viewWasResized() {
    try {
      Display.setDisplayMode(new DisplayMode(defaultFbufWidth, defaultFbufHeight));
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }
    if (Display.isCreated())
      super.viewWasResized();
  }

  @Override
  protected Object defaultFrameBuffer() {
    return 0;
  }

  @Override
  protected Object createFramebufferImpl(Object tex) {
    // Generate the framebuffer and attach the texture
    int fbuf = glGenFramebuffersEXT();
    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fbuf);
    glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D,
                              (Integer) tex, 0);
    return fbuf;
  }

  @Override
  protected void bindFramebufferImpl(Object fbuf, int width, int height) {
    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, (Integer) fbuf);
    glViewport(0, 0, width, height);
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

  void initGL() {
    try {
      Display.create();
      super.viewWasResized();
      glDisable(GL_CULL_FACE);
      glEnable(GL_BLEND);
      glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
      glClearColor(0, 0, 0, 1);
      texShader = new JavaGLShader.Texture(this);
      colorShader = new JavaGLShader.Color(this);
      checkGLError("initGL");
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }
  }

  void paintLayers(GroupLayerGL rootLayer) {
    // Bind the default frameBuffer (the SurfaceView's Surface)
    checkGLError("updateLayers Start");

    bindFramebuffer();

    // Clear to transparent
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Paint all the layers
    rootLayer.paint(rootXform, 1);
    checkGLError("updateLayers");

    // Guarantee a flush
    useShader(null);
  }

  void updateTexture(int texture, BufferedImage image) {
    ByteBuffer buf = convertImageData(image);

    glBindTexture(GL_TEXTURE_2D, texture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA,
                 GL_UNSIGNED_BYTE, buf);

    checkGLError("updateTexture");
  }

  private ByteBuffer convertImageData(BufferedImage img) {
    Asserts.checkNotNull(img);
    ByteBuffer imageBuffer;

    // TODO(jgw): There has *got* to be a better way. None of the BufferedImage types match
    // GL_RGBA, so we have to go through these stupid contortions to get a color model.
    ColorModel glAlphaColorModel = new ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false,
        Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
    WritableRaster raster = Raster.createInterleavedRaster(
      DataBuffer.TYPE_BYTE, img.getWidth(), img.getHeight(), 4, null);
    BufferedImage texImage = new BufferedImage(glAlphaColorModel, raster, true, null);

    Graphics g = texImage.getGraphics();
    g.setColor(new Color(0f, 0f, 0f, 0f));
    g.fillRect(0, 0, 256, 256);
    g.drawImage(img, 0, 0, null);

    // Build a byte buffer from the temporary image that be used by OpenGL to produce a texture.
    DataBufferByte dbuf = (DataBufferByte) texImage.getRaster().getDataBuffer();
    imageBuffer = ByteBuffer.allocateDirect(dbuf.getSize());
    imageBuffer.order(ByteOrder.nativeOrder());
    imageBuffer.put(dbuf.getData());
    imageBuffer.flip();

    return imageBuffer;
  }
}
