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

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.glfw.GLFW.*;

import playn.core.Scale;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

public class GLFWGraphics extends LWJGLGraphics {

  private final GLFWFramebufferSizeCallback fbSizeCallback = new GLFWFramebufferSizeCallback() {
    public void invoke (long window, int width, int height) {
      viewportAndScaleChanged(width, height);
    }
  };

  private final Dimension screenSize = new Dimension();
  private final JavaPlatform plat;
  private final long window;

  public GLFWGraphics(JavaPlatform jplat, long window) {
    super(jplat);
    this.plat = jplat;
    this.window = window;
    glfwSetFramebufferSizeCallback(window, fbSizeCallback);
  }

  void shutdown () {
    fbSizeCallback.close();
  }

  @Override void setTitle (String title) {
    if (window != 0L) glfwSetWindowTitle(window, title);
  }

  @Override public void setSize (int width, int height, boolean fullscreen) {
    if (plat.config.fullscreen != fullscreen) {
      plat.log().warn("fullscreen cannot be changed via setSize, use config.fullscreen instead");
      return;
    }
    GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    if (width > vidMode.width()) {
      plat.log().debug("Capping window width at desktop width: " + width + " -> " +
                       vidMode.width());
      width = vidMode.width();
    }
    if (height > vidMode.height()) {
      plat.log().debug("Capping window height at desktop height: " + height + " -> " +
                       vidMode.height());
      height = vidMode.height();
    }
    glfwSetWindowSize(window, width, height);
    // plat.log().info("setSize: " + width + "x" + height);
    viewSizeM.setSize(width, height);

    IntBuffer fbSize = BufferUtils.createIntBuffer(2);
    long addr = MemoryUtil.memAddress(fbSize);
    nglfwGetFramebufferSize(window, addr, addr + 4);
    viewportAndScaleChanged(fbSize.get(0), fbSize.get(1));
  }

  @Override public IDimension screenSize () {
    GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    screenSize.width = vidMode.width();
    screenSize.height = vidMode.height();
    return screenSize;
  }

  private void viewportAndScaleChanged (int fbWidth, int fbHeight) {
    float scale = fbWidth / viewSizeM.width;
    // plat.log().info("viewportAndScaleChanged: " + fbWidth + "x" + fbHeight + "@" + scale);
    if (scale != scale().factor) scaleChanged(new Scale(scale));
    viewportChanged(fbWidth, fbHeight);
  }
}
