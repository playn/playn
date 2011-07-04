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
import android.view.View;

public class GameViewDraw extends View implements GameView {

  private GameLoop loop;
  private final GameActivity activity;

  public GameViewDraw(GameActivity activity, Context context, AttributeSet attrs) {
    super(context, attrs);
    this.activity = activity;

    loop = new GameLoop(this) {
      @Override
      protected void paint() {
        invalidate();
      }
    };
  }

  @Override
  protected void onDraw(Canvas c) {
    loop.paint(c);
  }

  public void notifyVisibilityChanged(int visibility) {
    Log.i("forplay", "notifyVisibilityChanged: " + visibility);
    if (visibility == VISIBLE) {
      loop.start();
    } else if (visibility == INVISIBLE) {
      loop.end();
    }
  }
  
  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    activity.onLayout(changed, left, top, right, bottom);
  }
}
