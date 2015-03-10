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
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

public class SWTPlatform extends JavaPlatform {

  protected static final long FRAME_MILLIS = 1000/60; // TODO: allow config?
  private long lastFrame;

  // these are initialized in createGraphics(), sigh
  Display display;
  Shell shell;
  Composite comp;

  /** Returns the SWT shell in which the game is running. */
  public Shell shell () { return shell; }

  /** Returns the SWT composite that hosts the game view. */
  public Composite composite () { return comp; }

  /** Creates a new SWT platform and prepares it for operation. */
  public SWTPlatform (Config config) {
    super(config);
  }

  @Override public void setTitle (String title) { shell.setText(title); }

  @Override public SWTGraphics graphics () { return (SWTGraphics)super.graphics(); }

  @Override public void start () {
    // canvas.addListener(SWT.Paint, new Listener() {
    //   public void handleEvent (Event event) {
    //     run.run();
    //   }
    // });
    shell.open();

    while (!shell.isDisposed()) {
      long now = tick();
      if (now - lastFrame >= FRAME_MILLIS) {
        graphics().onBeforeFrame();
        emitFrame();
        graphics().onAfterFrame();
        lastFrame = now;
      }
      if (!display.readAndDispatch()) {
        try { Thread.sleep(1); }
        catch (InterruptedException ie) {} // no problem!
      }
    }
    display.dispose();

    shutdown();
  }

  @Override protected void preInit () {
    Display.setAppName(config.appName);
    display = new Display();
    shell = new Shell(display);
    shell.setLayout(new FillLayout());
    shell.addShellListener(new ShellAdapter() {
      public void shellActivated (ShellEvent e) { lifecycle.emit(Lifecycle.RESUME); }
      public void shellDeactivated (ShellEvent e) { lifecycle.emit(Lifecycle.PAUSE); }
    });
    comp = new Composite(shell, SWT.NONE);
    comp.setLayout(null);
  }

  @Override protected JavaGraphics createGraphics () { return new SWTGraphics(this, comp); }
  @Override protected JavaInput createInput() { return new SWTInput(this); }
}
