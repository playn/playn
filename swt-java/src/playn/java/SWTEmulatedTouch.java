package playn.java;

import playn.core.Key;
import playn.core.Mouse;
import playn.core.Mouse.ButtonEvent.Impl;

public class SWTEmulatedTouch extends JavaEmulatedTouch
{
  public SWTEmulatedTouch (Key multiTouchKey) {
    super(multiTouchKey);
  }

  @Override JavaMouse createMouse (JavaPlatform platform) {
    final JavaEmulatedTouch self = this;
    return new SWTMouse((SWTPlatform)platform) {
      @Override public boolean hasMouse() {
        return false;
      }

      @Override protected boolean onMouseDown(Mouse.ButtonEvent.Impl event) {
        self.onMouseDown(event.time(), event.x(), event.y());
        return false;
      }

      @Override protected boolean onMouseMove(Mouse.MotionEvent.Impl event) {
        self.onMouseMove(event.time(), event.x(), event.y());
        return false;
      }

      @Override protected boolean onMouseUp(Impl event) {
        self.onMouseUp(event.time(), event.x(), event.y());
        return false;
      }

      @Override protected boolean onMouseWheelScroll(playn.core.Mouse.WheelEvent.Impl event) {
        return false;
      }
    };
  }
}
