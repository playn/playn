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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.lwjgl.opengl.GL;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

public class SWTGraphics extends LWJGLGraphics {

  private final SWTPlatform plat;
  GLCanvas canvas; // initialized in createGLContext

  public SWTGraphics (SWTPlatform plat, final Composite comp) {
    super(plat);
    this.plat = plat;

    // create our GLCanvas
    GLData data = new GLData();
    data.doubleBuffer = true;
    canvas = new GLCanvas(comp, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE, data);
    canvas.setCurrent();
    GL.createCapabilities();

    comp.addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event event) {
        // resize our GLCanvas to fill the window; we do manual layout so that other SWT widgets
        // can be overlaid on top of our GLCanvas
        Rectangle bounds = comp.getBounds();
        comp.setBounds(bounds);
        canvas.setBounds(bounds);
        canvas.setCurrent();
        viewportChanged(bounds.width, bounds.height);
      }
    });

    plat.log().info("Setting size " + plat.config.width + "x" + plat.config.height);
    setSize(plat.config.width, plat.config.height, plat.config.fullscreen);
  }

  public GLCanvas canvas () { return canvas; }

  @Override public IDimension screenSize () {
    return new Dimension(); // TODO
  }

  @Override public void setSize (int width, int height, boolean fullscreen) {
    int rawWidth = scale().scaledCeil(width), rawHeight = scale().scaledCeil(height);
    plat.composite().setSize(rawWidth, rawHeight);
    plat.shell().setFullScreen(fullscreen);
    plat.shell().pack();
    // viewSizeChanged(rawWidth, rawHeight);
  }

  @Override void setTitle (String title) { plat.shell().setText(title); }

  void onBeforeFrame () { canvas.setCurrent(); }
  void onAfterFrame () { canvas.swapBuffers(); }
}
