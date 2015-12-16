package playn.java;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import playn.core.Log;
import playn.java.JavaPlatform.Config;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

public class LWJGLWindow {
  
  private final Config config;
  private final Log log;
  private long window;
  final private Dimension desktopSize;
  
  public LWJGLWindow (Config config, Log logger) {
    this.config = config;
    this.log = logger;
    glfwInit();
    glfwSetErrorCallback(new GLFWErrorCallback() {
      @Override public void invoke(int error, long description) {
        log.error("GL Error (" + error + "):" + getDescription(description));
      }
    });
    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
    //TODO: Shall we use a lower version for compatibility? 
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
   
    long monitor = glfwGetPrimaryMonitor();
    GLFWVidMode vidMode = glfwGetVideoMode(monitor);
    desktopSize = new Dimension(vidMode.width(), vidMode.height()); 
  }

  public void init () {
    //TODO: not tested yet, find an equivalent way.
    System.setProperty("org.lwjgl.opengl.Display.enableHighDPI", "true");
    int width = config.width;
    int height = config.height;
    long monitor = 0;
    
    if(config.fullscreen) {
        monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = glfwGetVideoMode(monitor);
        width = vidMode.width();
        height = vidMode.height();
    }
    window = glfwCreateWindow(width, height, config.appName, monitor, 
        config.fullscreen ? 1:0 );
    if(window == 0) throw new RuntimeException("Failed to create window");
    glfwMakeContextCurrent(window);
    //TODO: do we need this GLContext.createFromCurrent();
    glfwShowWindow(window);
  }

  public void update() { glfwPollEvents(); }
  
  public void shutdown (){
    glfwDestroyWindow(window);   
    glfwTerminate();
  }


  public void setTitle (String title) { glfwSetWindowTitle(window, title);}

  public boolean isActive () { return glfwGetWindowAttrib(window, GLFW_VISIBLE) > 0; }

  public boolean isCloseRequested () { return glfwWindowShouldClose(window) == GL_TRUE; }


  public void sync(int interval) {glfwSwapInterval(interval);}

  public float calPixelScaleFactor() {
    long monitor = glfwGetPrimaryMonitor();
    IntBuffer widthMM = IntBuffer.allocate(1);
    IntBuffer heightMM = IntBuffer.allocate(1);
    glfwGetMonitorPhysicalSize(monitor, widthMM, heightMM);;
    return widthMM.get(0) * 1.0F / desktopSize.width;
  }
  
  public Dimension size(){
    IntBuffer wBuf = IntBuffer.allocate(1);
    IntBuffer hBuf = IntBuffer.allocate(1);
    glfwGetWindowSize(window, wBuf, hBuf);
    return new Dimension(wBuf.get(0), hBuf.get(0));
  }

  public IDimension destopSize () { return this.desktopSize; }

  public boolean setSize(int width, int height, boolean fullscreen) {
    if (config.fullscreen != fullscreen) {
      log.warn("fullscreen cannot be changed via setSize, use config.fullscreen instead");
      return false;
    }
    glfwSetWindowSize(window, width, height);
    return true;
  }


}
