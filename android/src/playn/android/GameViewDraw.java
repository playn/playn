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

import static playn.core.PlayN.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import playn.android.GameActivity;
import playn.core.GroupLayer;
import playn.core.PlayN;

public class GameViewDraw extends View implements GameView {

  private GameLoop loop;
  private final GameActivity activity;

  /**
   * Hardware-acceleration friendly game loop class...
   * but hardware-accelerated Canvas is still not
   * as good as OpenGL it would seem.
   * @param activity
   * @param context
   */
  public GameViewDraw(GameActivity activity, Context context) {
    super(context);
    this.activity = activity;
    AndroidPlatform.register(activity);
    activity.main();

    loop = new GameLoop(this) {
      @Override
      protected void paint() {  //loop.paint without args called
        invalidate();  //Makes it so onDraw(c) will be called, calling loop.paint(c)
                      //onDraw(c) is hardware accelerated presumably?
      }
    };
  }

  @Override
  protected void onDraw(Canvas c) {
    loop.paint(c);
  }

  public void notifyVisibilityChanged(int visibility) {
    Log.i("playn", "notifyVisibilityChanged: " + visibility);
    if (visibility == VISIBLE) {
      loop.start();
    } else if (visibility == INVISIBLE) {
      loop.pause();
    }
  }
  
  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    activity.onLayout(changed, left, top, right, bottom);
    GroupLayer rootLayer = graphics().rootLayer();
    int xOffset = (graphics().screenWidth() - graphics().width()) / 2;
    int yOffset = (graphics().screenHeight() - graphics().height()) / 2;
    rootLayer.setTranslation(xOffset > 0 ? xOffset : 0, yOffset > 0 ? yOffset : 0);
  }
}
