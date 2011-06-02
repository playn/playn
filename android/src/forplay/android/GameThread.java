package forplay.android;

import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * TODO
 */
class GameThread extends Thread {

  private final SurfaceHolder holder;
  private boolean running;

  private static final float MAX_DELTA = 100;
  private static final float FRAME_TIME = 50;

  GameThread(SurfaceHolder holder) {
    this.holder = holder;
  }

  private int updateRate = 0;
  private float accum = updateRate;
  private double lastTime;
  private Handler gameLoop;

  @Override
  public void run() {
    try {
      Looper.prepare();
      gameLoop = new Handler();
      gameLoop.post(mainLoop());
      Log.i("forplay", "Starting Game Loop");
      Looper.loop();
    } catch (Throwable t) {
      Log.e("forplay", "error in game loop", t);
    }
  }

  private Runnable mainLoop() {
    return new Runnable() {
      public void run() {
        Canvas c = null;
        try {
          c = holder.lockCanvas(null);
          synchronized (holder) {
            AndroidPlatform.instance.setCurrentCanvas(c);
            double now = time();
            float delta = (float)(now - lastTime);
            if (delta > MAX_DELTA) {
              delta = MAX_DELTA;
            }
            lastTime = now;

            if (updateRate == 0) {
              AndroidPlatform.instance.update(delta);
              accum = 0;
            } else {
              accum += delta;
              while (accum > updateRate) {
                AndroidPlatform.instance.update(updateRate);
                accum -= updateRate;
              }
            }

            AndroidPlatform.instance.draw(accum / updateRate);
          }
        } finally {
          // do this in a finally so that if an exception is thrown
          // during the above, we don't leave the Surface in an
          // inconsistent state
          if (c != null) {
            AndroidPlatform.instance.setCurrentCanvas(null);
            holder.unlockCanvasAndPost(c);
          }
          if (running) {
            gameLoop.post(this);
          }
        }
      }
    };
  }


  /**
   * @return
   */
  private long  time() {
    return System.nanoTime();
  }

  void setRunning(boolean running) {
    this.running = running;
  }

  void setSurfaceSize(int width, int height) {
    // TODO
  }

  public void post(Runnable r) {
    gameLoop.post(r);
  }
}
