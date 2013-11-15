package playn.java;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;

import org.lwjgl.opengl.GLContext;
import org.lwjgl.LWJGLException;

public class SWTGraphics extends JavaGraphics {

  private final SWTPlatform platform;
  private GLCanvas canvas; // initialized in createGLContext

  public SWTGraphics (SWTPlatform platform, JavaPlatform.Config config, final Composite comp) {
    super(platform, config);
    this.platform = platform;

    // create our GLCanvas
    GLData data = new GLData ();
    data.doubleBuffer = true;
    canvas = new GLCanvas(comp, SWT.NONE, data);
    makeCurrent();

    comp.addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event event) {
        // resize our GLCanvas to fill the window; we do manual layout so that other SWT widgets
        // can be overlaid on top of our GLCanvas
        Rectangle bounds = comp.getBounds();
        comp.setBounds(bounds);
        canvas.setBounds(bounds);
        makeCurrent();
        SWTGraphics.this.platform.log().info("Resized " + bounds.width + "x" + bounds.height);
        ctx.setSize((int)ctx.scale.invScaled(bounds.width), (int)ctx.scale.invScaled(bounds.height));
      }
    });

    platform.log().info("Setting size " + config.width + "x" + config.height);
    platform.shell.setSize(ctx.scale.scaledCeil(config.width), ctx.scale.scaledCeil(config.height));
  }

  boolean isDisposed () {
    return canvas.isDisposed();
  }

  @Override
  public void setSize(int width, int height, boolean fullscreen) {
    int rawWidth = ctx.scale.scaledCeil(width), rawHeight = ctx.scale.scaledCeil(height);
    platform.shell.setSize(rawWidth, rawHeight);
    platform.shell.setFullScreen(fullscreen);
  }

  @Override
  protected void init() {
    // don't call super here, as we don't want to init LWJGL
    ctx.init();
  }

  @Override
  protected void paint () {
    makeCurrent();
    super.paint();
    canvas.swapBuffers();
  }

  @Override
  protected void setDisplayMode(int width, int height, boolean fullscreen) {
    // nada
  }

  protected void makeCurrent () {
    canvas.setCurrent();
    try {
      GLContext.useContext(canvas);
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }
  }
}
