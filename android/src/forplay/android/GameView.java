/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

  private final SurfaceHolder holder;
  private GameThread thread;

  public GameView(Context context, AttributeSet attrs) {
    super(context, attrs);
    holder = getHolder();
    holder.addCallback(this);
    setFocusable(true);
  }

  GameThread getThread() {
    return thread;
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    thread.setSurfaceSize(width, height);
  }

  public void surfaceCreated(SurfaceHolder holder) {
    // Start the thread here so that we don't busy-wait in run()
    // waiting for the surface to be created.
    thread = new GameThread(holder);
    thread.setRunning(true);
    thread.start();
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    // We have to tell thread to shut down & wait for it to finish, or else
    // it might touch the Surface after we return and explode.
    boolean retry = true;
    thread.setRunning(false);
    while (retry) {
      try {
        thread.join();
        retry = false;
      } catch (InterruptedException e) {
      }
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    AndroidPlatform.instance.onKeyDown(keyCode);
    return true;
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    AndroidPlatform.instance.onKeyUp(keyCode);
    return true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    AndroidPlatform plat = AndroidPlatform.instance;
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        plat.onPointerStart(event.getX(), event.getY());;
        break;
      case MotionEvent.ACTION_UP:
        plat.onPointerEnd(event.getX(), event.getY());;
        break;
      case MotionEvent.ACTION_MOVE:
        plat.onPointerMove(event.getX(), event.getY());;
        break;
    }
    return true;
  }
}
