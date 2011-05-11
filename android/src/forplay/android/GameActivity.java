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

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

/**
 * TODO: pause/unpause
 * TODO: save/restore state
 */
public class GameActivity extends Activity {

  private GameView gameView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    gameView = (GameView) findViewById(R.id.game);

    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    AndroidPlatform.register(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    // TODO
  }

  @Override
  protected void onResume() {
    super.onResume();
    // TODO
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // TODO
  }

  GameView gameView() {
    return gameView;
  }
  
  GameThread getGameThread() {
	  return gameView.getThread();
  }
}
