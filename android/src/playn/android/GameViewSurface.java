/**
 * Copyright 2011 The PlayN Authors
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
package playn.android;

import android.content.Context;
import android.graphics.Canvas;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameViewSurface extends GLSurfaceView implements SurfaceHolder.Callback, GameView {

  private final SurfaceHolder holder;
  private final GameLoop loop;
  private final GameActivity activity;
  
  private boolean loopStarted;

  /**
   * Software-acceleration friendly game loop class
   * @param activity
   * @param context
   */
  public GameViewSurface(GameActivity activity, Context context) {
    super(context);
    this.activity = activity;
    holder = getHolder();
    holder.addCallback(this);
    setFocusable(true);
    
    AndroidPlatform.register(activity);
    activity.main();
    
    loop = new GameLoop(this) {
      @Override
      protected void paint() {
        Canvas c = null;
        try {
          //Get the Canvas of this Surface so that we can draw directly into it.
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
    Log.i("playn", "notifyVisibilityChanged: " + visibility);
    if (visibility == INVISIBLE)
      loop.pause();
  }

  //These aren't called from anywhere?  From SurfaceHolder.Callback
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    loop.start();
  }

  public void surfaceCreated(SurfaceHolder holder) {
    //Set rendering code here!
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
   loop.pause();
  }
  
  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    activity.onLayout(changed, left, top, right, bottom);
  }
}
