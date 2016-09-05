/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
// import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;

/**
 * Implements the PlayN platform for Java, based on LWJGL and GLFW.
 *
 * Due to the way LWJGL works, a game must create the platform instance, then perform any of its
 * own initialization that requires access to GL resources, and then call {@link #start} to start
 * the game loop. The {@link #start} call does not return until the game exits.
 */
public class LWJGLPlatform extends JavaPlatform {

  // we have to keep strong references to GLFW callbacks
  private final GLFWErrorCallback errorCallback;

  private final GLFWGraphics graphics;
  private final GLFWInput input;

  /** The handle on our GLFW window; also used by GLFWInput. */
  private final long window;

  public LWJGLPlatform (Config config) {
    super(config);

    // on the Mac we have to force AWT into headless mode to avoid conflicts with GLFW
    if (needsHeadless()) {
      System.setProperty("java.awt.headless", "true");
    }

    glfwSetErrorCallback(errorCallback = new GLFWErrorCallback() {
      @Override public void invoke(int error, long description) {
        log().error("GL Error (" + error + "):" + getDescription(description));
      }
    });
    if (!glfwInit()) throw new RuntimeException("Failed to init GLFW.");

    long monitor = glfwGetPrimaryMonitor();
    GLFWVidMode vidMode = glfwGetVideoMode(monitor);

    int width = config.width, height = config.height;
    if (config.fullscreen) {
      width = vidMode.width();
      height = vidMode.height();
    } else {
      monitor = 0; // monitor == 0 means non-fullscreen window
    }

    // NOTE: it's easier to co-exist with GSLES2 if we leave the GLContext in "old and busted"
    // mode; so all the GL3.2 "new hotness" is commented out
    glfwDefaultWindowHints();
    // glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    // glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
    // glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, 1);
    // glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
    window = glfwCreateWindow(width, height, config.appName, monitor, 0);
    if (window == 0) throw new RuntimeException("Failed to create window; see error log.");

    graphics = new GLFWGraphics(this, window);
    input = new GLFWInput(this, window);

    glfwSetWindowPos(window, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);
    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    graphics.setSize(config.width, config.height, config.fullscreen);
    glfwShowWindow(window);

    GL.createCapabilities();
    // IntBuffer vao = BufferUtils.createIntBuffer(1);
    // GL30.glGenVertexArrays(vao);
    // GL30.glBindVertexArray(vao.get(0));
  }

  boolean needsHeadless() {
    return System.getProperty("os.name").equals("Mac OS X");
  }

  @Override public JavaGraphics graphics () { return graphics; }
  @Override public JavaInput input () { return input; }

  @Override protected void loop () {
    boolean wasActive = glfwGetWindowAttrib(window, GLFW_VISIBLE) > 0;
    while (!glfwWindowShouldClose(window)) {
      // notify the app if lose or regain focus (treat said as pause/resume)
      boolean newActive = glfwGetWindowAttrib(window, GLFW_VISIBLE) > 0;
      if (wasActive != newActive) {
        dispatchEvent(lifecycle, wasActive ? Lifecycle.PAUSE : Lifecycle.RESUME);
        wasActive = newActive;
      }
      // process frame, if we don't need to provide true pausing
      if (newActive || !config.truePause) {
        processFrame();
      }
      // sleep until it's time for the next frame
      glfwSwapBuffers(window);
    }
    input.shutdown();
    graphics.shutdown();
    errorCallback.close();
    glfwDestroyWindow(window);
    glfwTerminate();
  }
}
