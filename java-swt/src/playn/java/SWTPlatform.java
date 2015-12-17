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

/**
 * Implements the PlayN platform for Java, based on LWJGL and SWT.
 */
public class SWTPlatform extends JavaPlatform {

  private final Display display;
  private final Shell shell;
  private final Composite comp;

  private final SWTGraphics graphics;
  private final SWTInput input;

  /** Creates a new SWT platform and prepares it for operation. */
  public SWTPlatform (Config config) {
    super(config);

    Display.setAppName(config.appName);
    display = new Display();
    shell = new Shell(display);
    shell.setLayout(new FillLayout());
    shell.addShellListener(new ShellAdapter() {
      public void shellActivated (ShellEvent e) { dispatchEvent(lifecycle, Lifecycle.RESUME); }
      public void shellDeactivated (ShellEvent e) { dispatchEvent(lifecycle, Lifecycle.PAUSE); }
    });
    comp = new Composite(shell, SWT.NONE);
    comp.setLayout(null);

    graphics = new SWTGraphics(this, comp);
    input = new SWTInput(this);
  }

  /** Returns the SWT display on which the game is running. */
  public Display display () { return display; }
  /** Returns the SWT shell in which the game is running. */
  public Shell shell () { return shell; }
  /** Returns the SWT composite that hosts the game view. */
  public Composite composite () { return comp; }

  @Override public SWTGraphics graphics () { return graphics; }
  @Override public SWTInput input () { return input; }

  @Override protected void loop () {
    shell.setText(config.appName);
    shell.open();

    // this callback processes a single frame and queues itself up again for exec
    display.asyncExec(new Runnable() {
      public void run() {
        if (!shell.isDisposed()) {
          graphics.onBeforeFrame();
          emitFrame();
          graphics.onAfterFrame();
          display.asyncExec(this);
        }
      }
    });

    // now let SWT run the main loop
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
      display.sleep();
    }
    display.dispose();
  }
}
