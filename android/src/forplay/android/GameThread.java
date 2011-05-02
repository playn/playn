package forplay.android;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * TODO
 */
class GameThread extends Thread {

  private final SurfaceHolder holder;
  private boolean running;

  GameThread(SurfaceHolder holder) {
    this.holder = holder;
  }

  @Override
  public void run() {
    while (running) {
      Canvas c = null;
      try {
        c = holder.lockCanvas(null);
        synchronized (holder) {
          update();
          draw(c);
        }
      } finally {
        // do this in a finally so that if an exception is thrown
        // during the above, we don't leave the Surface in an
        // inconsistent state
        if (c != null) {
          holder.unlockCanvasAndPost(c);
        }
      }
    }
  }

  void setRunning(boolean running) {
    this.running = running;
  }

  void setSurfaceSize(int width, int height) {
    // TODO
  }

  private void draw(Canvas canvas) {
    AndroidPlatform.instance.draw(new AndroidSurface(canvas));
  }

  private void update() {
    AndroidPlatform.instance.update();
  }
}
