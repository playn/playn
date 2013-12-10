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

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;

import org.lwjgl.opengl.GLContext;
import org.lwjgl.LWJGLException;

public class SWTGraphics extends JavaGraphics {

  private final SWTPlatform platform;
  GLCanvas canvas; // initialized in createGLContext

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
        // SWTGraphics.this.platform.log().info("Resized " + bounds.width + "x" + bounds.height);
        ctx.setSize(ctx.scale.invScaledFloor(bounds.width), ctx.scale.invScaledFloor(bounds.height));
      }
    });

    // platform.log().info("Setting size " + config.width + "x" + config.height);
    platform.comp.setSize(ctx.scale.scaledCeil(config.width), ctx.scale.scaledCeil(config.height));
    platform.shell.pack();
  }

  @Override
  public void setSize(int width, int height, boolean fullscreen) {
    int rawWidth = ctx.scale.scaledCeil(width), rawHeight = ctx.scale.scaledCeil(height);
    platform.comp.setSize(rawWidth, rawHeight);
    platform.shell.setFullScreen(fullscreen);
    platform.shell.pack();
  }

  public GLCanvas canvas () {
    return canvas;
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
