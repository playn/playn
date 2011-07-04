/**
 * Copyright 2011 The ForPlay Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.android;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameViewSurface extends SurfaceView implements SurfaceHolder.Callback, GameView {

  private final SurfaceHolder holder;
  private final GameLoop loop;
  private final GameActivity activity;

  public GameViewSurface(GameActivity activity, Context context, AttributeSet attrs) {
    super(context, attrs);
    this.activity = activity;
    holder = getHolder();
    holder.addCallback(this);
    setFocusable(true);

    loop = new GameLoop(this) {
      @Override
      protected void paint() {
        Canvas c = null;
        try {
          c = holder.lockCanvas();
          synchronized (holder) {
            loop.paint(c);
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
    };
  }
  
  public void notifyVisibilityChanged(int visibility) {
    Log.i("forplay", "notifyVisibilityChanged: " + visibility);
    if (visibility == INVISIBLE)
      loop.end();
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
  }

  public void surfaceCreated(SurfaceHolder holder) {
    loop.start();
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    loop.end();
  }
  
  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    activity.onLayout(changed, left, top, right, bottom);
  }
}
