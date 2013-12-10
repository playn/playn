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

import playn.core.Game;
import playn.core.PlayN;
import playn.core.TouchImpl;

public class SWTPlatform extends JavaPlatform {

  protected static final long FRAME_MILLIS = 1000/60; // TODO: allow config?
  private long lastFrame;

  // these are initialized in createGraphics(), sigh
  Display display;
  Shell shell;
  Composite comp;

  public static SWTPlatform register (Config config) {
    SWTPlatform instance = new SWTPlatform(config);
    PlayN.setPlatform(instance);
    return instance;
  }

  @Override
  public void setTitle(String title) {
    shell.setText(title);
  }

  @Override
  public void run(final Game game) {
    init(game);

    // canvas.addListener(SWT.Paint, new Listener() {
    //   public void handleEvent (Event event) {
    //     run.run();
    //   }
    // });
    shell.open();

    while (!shell.isDisposed()) {
      long now = tick();
      if (now - lastFrame >= FRAME_MILLIS) {
        processFrame(game);
        lastFrame = now;
      }
      if (!display.readAndDispatch()) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException ie) {} // no problem!
      }
    }
    display.dispose();

    shutdown();
  }

  @Override
  public SWTGraphics graphics () {
    return (SWTGraphics)super.graphics();
  }

  public Shell shell () {
    return shell;
  }

  public Composite composite () {
    return comp;
  }

  protected SWTPlatform (Config config) {
    super(config);
  }

  @Override protected JavaGraphics createGraphics (Config config) {
    Display.setAppName(config.appName);
    display = new Display();
    shell = new Shell(display);
    shell.setLayout(new FillLayout());
    shell.addShellListener(new ShellAdapter() {
      public void shellActivated (ShellEvent e) {
        onResume();
      }
      public void shellDeactivated (ShellEvent e) {
        onPause();
      }
    });
    comp = new Composite(shell, SWT.NONE);
    comp.setLayout(null);
    return new SWTGraphics(this, config, comp);
  }

  @Override protected TouchImpl createTouch (Config config) {
    if (config.emulateTouch) {
      return new SWTEmulatedTouch(config.multiTouchKey);
    } else {
      return super.createTouch(config);
    }
  }

  @Override protected JavaMouse createMouse() {
    return new SWTMouse(this);
  }

  @Override protected JavaKeyboard createKeyboard() {
    return new SWTKeyboard(this);
  }
}
