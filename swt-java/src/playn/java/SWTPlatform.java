package playn.java;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

import playn.core.Game;
import playn.core.PlayN;

public class SWTPlatform extends JavaPlatform {

  // initialized in createGraphics(), sigh
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

    final Runnable run = new Runnable() {
      public void run () {
        if (!((SWTGraphics)graphics()).isDisposed()) {
          processFrame(game);
          display.asyncExec(this);
        }
      }
    };
    // canvas.addListener(SWT.Paint, new Listener() {
    //   public void handleEvent (Event event) {
    //     run.run();
    //   }
    // });
    display.asyncExec(run);
    shell.open();

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();

    shutdown();
  }

  protected SWTPlatform (Config config) {
    super(config);
  }

  @Override protected JavaGraphics createGraphics (Config config) {
    display = new Display();
    shell = new Shell(display);
    shell.setLayout(new FillLayout());
    shell.setText("Game");
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

  @Override protected JavaMouse createMouse() {
    return new SWTMouse(this);
  }

  @Override protected JavaKeyboard createKeyboard() {
    return new SWTKeyboard(this);
  }
}
