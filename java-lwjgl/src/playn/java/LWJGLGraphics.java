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
package playn.java;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL11;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;

import playn.core.Scale;
import playn.core.Texture;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

public class LWJGLGraphics extends JavaGraphics {

  // we have to keep strong references to GLFW callbacks
  private final GLFWErrorCallback errorCallback;
  private final GLFWFramebufferSizeCallback fbSizeCallback = new GLFWFramebufferSizeCallback() {
    public void invoke (long window, int width, int height) {
      viewportAndScaleChanged(width, height);
    }
  };

  private final Dimension desktopSize = new Dimension();
  private final Dimension screenSize = new Dimension();
  private final JavaPlatform.Config config;

  protected long window;
  // private final JavaPlatform.Config config;
  // private final Log log;

  public LWJGLGraphics(JavaPlatform jplat) {
    super(jplat, new LWJGLGL20(), Scale.ONE); // real scale factor set in init()
    this.config = jplat.config;

    if (glfwInit() != GL11.GL_TRUE) throw new RuntimeException("Failed to init GLFW.");
    glfwSetErrorCallback(errorCallback = new GLFWErrorCallback() {
      @Override public void invoke(int error, long description) {
        plat.log().error("GL Error (" + error + "):" + getDescription(description));
      }
    });

    long monitor = glfwGetPrimaryMonitor();
    GLFWVidMode vidMode = glfwGetVideoMode(monitor);
    desktopSize.setSize(vidMode.width(), vidMode.height());

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_RESIZABLE, GL11.GL_FALSE);
    glfwWindowHint(GLFW_VISIBLE, GL11.GL_FALSE);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, 1);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
  }

  public void update () {
    glfwPollEvents();
  }

  public void shutdown () {
    glfwDestroyWindow(window);
    glfwTerminate();
  }

  public void setTitle (String title) {
    if (window != 0L) glfwSetWindowTitle(window, title);
  }

  public boolean isActive () {
    return glfwGetWindowAttrib(window, GLFW_VISIBLE) > 0;
  }

  public boolean isCloseRequested () {
    return glfwWindowShouldClose(window) == GL11.GL_TRUE;
  }

  public void sync () {
    glfwSwapBuffers(window);
  }

  public Dimension size () {
    IntBuffer wBuf = BufferUtils.createIntBuffer(1), hBuf = BufferUtils.createIntBuffer(1);
    glfwGetWindowSize(window, wBuf, hBuf);
    return new Dimension(wBuf.get(0), hBuf.get(0));
  }

  public Dimension framebufferSize () {
    IntBuffer fbWid = BufferUtils.createIntBuffer(1), fbHei = BufferUtils.createIntBuffer(1);
    glfwGetFramebufferSize(window, fbWid, fbHei);
    return new Dimension(fbWid.get(0), fbHei.get(0));
  }

  @Override public IDimension screenSize () {
    screenSize.width = scale().invScaled(desktopSize.width);
    screenSize.height = scale().invScaled(desktopSize.height);
    return screenSize;
  }

  @Override public void setSize (int width, int height, boolean fullscreen) {
    if (config.fullscreen != fullscreen) {
      plat.log().warn("fullscreen cannot be changed via setSize, use config.fullscreen instead");
      return;
    }
    glfwSetWindowSize(window, width, height);
    plat.log().info("setSize: " + width + "x" + height);
    viewSizeM.setSize(width, height);
    viewportAndScaleChanged();
  }

  private void viewportAndScaleChanged () {
    IntBuffer fbWid = BufferUtils.createIntBuffer(1), fbHei = BufferUtils.createIntBuffer(1);
    glfwGetFramebufferSize(window, fbWid, fbHei);
    viewportAndScaleChanged(fbWid.get(0), fbHei.get(0));
  }

  private void viewportAndScaleChanged (int fbWidth, int fbHeight) {
    float scale = fbWidth / viewSizeM.width;
    plat.log().info("viewportAndScaleChanged: " + fbWidth + "x" + fbHeight + "@" + scale);
    if (scale != scale().factor) scaleChanged(new Scale(scale));
    viewportChanged(fbWidth, fbHeight);
  }

  @Override protected void init () {
    int width = config.width, height = config.height;
    long monitor = 0;
    if (config.fullscreen) {
      monitor = glfwGetPrimaryMonitor();
      GLFWVidMode vidMode = glfwGetVideoMode(monitor);
      width = vidMode.width();
      height = vidMode.height();
    }
    window = glfwCreateWindow(width, height, config.appName, monitor, 0);
    if (window == 0) throw new RuntimeException("Failed to create window; see error log.");

    glfwSetFramebufferSizeCallback(window, fbSizeCallback);
    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    setSize(config.width, config.height, config.fullscreen);
    glfwShowWindow(window);

    GL.createCapabilities();
    IntBuffer vao = BufferUtils.createIntBuffer(1);
    GL30.glGenVertexArrays(vao);
    GL30.glBindVertexArray(vao.get(0));
  }

  @Override protected void upload (BufferedImage img, Texture tex) {
    // Convert the bitmap into a format for quick uploading (NOOPs if already optimized)
    BufferedImage bitmap = convertImage(img);

    DataBuffer dbuf = bitmap.getRaster().getDataBuffer();
    ByteBuffer bbuf;
    int format, type;

    if (bitmap.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
      DataBufferInt ibuf = (DataBufferInt)dbuf;
      int iSize = ibuf.getSize()*4;
      bbuf = checkGetImageBuffer(iSize);
      bbuf.asIntBuffer().put(ibuf.getData());
      bbuf.position(bbuf.position()+iSize);
      bbuf.flip();
      format = GL12.GL_BGRA;
      type = GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

    } else if (bitmap.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
      DataBufferByte dbbuf = (DataBufferByte)dbuf;
      bbuf = checkGetImageBuffer(dbbuf.getSize());
      bbuf.put(dbbuf.getData());
      bbuf.flip();
      format = GL11.GL_RGBA;
      type = GL12.GL_UNSIGNED_INT_8_8_8_8;

    } else {
      // Something went awry and convertImage thought this image was in a good form already,
      // except we don't know how to deal with it
      throw new RuntimeException("Image type wasn't converted to usable: " + bitmap.getType());
    }

    gl.glBindTexture(GL11.GL_TEXTURE_2D, tex.id);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(),
                      0, format, type, bbuf);
    gl.checkError("updateTexture");
  }
}
