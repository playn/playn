package playn.java;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import pythagoras.f.Point;

public class SWTMouse extends JavaMouse {

  private SWTPlatform platform;

  public SWTMouse(SWTPlatform platform) {
    super(platform);
    this.platform = platform;
  }

  public void init() {
    platform.display.addFilter(SWT.MouseDown, new org.eclipse.swt.widgets.Listener() {
      public void handleEvent (Event event) {
        if (event.widget == platform.graphics().canvas) {
          Point xy = scaleCoord(event);
          onMouseDown(event.time, xy.x, xy.y, mapButton(event.button));
        }
      }
    });
    platform.display.addFilter(SWT.MouseUp, new org.eclipse.swt.widgets.Listener() {
      public void handleEvent (Event event) {
        if (event.widget == platform.graphics().canvas) {
          Point xy = scaleCoord(event);
          onMouseUp(event.time, xy.x, xy.y, mapButton(event.button));
        }
      }
    });
    platform.display.addFilter(SWT.MouseMove, new org.eclipse.swt.widgets.Listener() {
      public void handleEvent (Event event) {
        if (event.widget == platform.graphics().canvas) {
          Point xy = scaleCoord(event);
          float dx = xy.x - lastX, dy = xy.y - lastY;
          onMouseMove(event.time, xy.x, xy.y, dx, dy);
        }
      }
      private float lastX, lastY;
    });
    platform.display.addFilter(SWT.MouseWheel, new org.eclipse.swt.widgets.Listener() {
      public void handleEvent (Event event) {
        if (event.widget == platform.graphics().canvas) {
          Point xy = scaleCoord(event);
          onMouseWheelScroll(event.time, xy.x, xy.y, -event.count);
        }
      }
    });
  }

  public void update() {
    // not needed
  }

  private Point scaleCoord(Event event) {
    return platform.graphics().transformMouse(new Point(event.x, event.y));
  }

  private int mapButton(int swtButton) {
    switch (swtButton) {
    case 1:  return BUTTON_LEFT;
    case 2:  return BUTTON_MIDDLE;
    case 3:  return BUTTON_RIGHT;
    default: return swtButton;
    }
  }
}
