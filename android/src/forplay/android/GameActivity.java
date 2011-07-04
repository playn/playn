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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

/**
 * TODO: pause/unpause TODO: save/restore state
 */
public class GameActivity extends Activity {
  private GameView gameView;
  private WakeLock wakeLock;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (supportsHardwareAcceleration()) {
      // Use the raw constant rather than the flag to avoid blowing up on
      // earlier Android
      int flagHardwareAccelerated = 0x1000000;

      getWindow().setFlags(flagHardwareAccelerated, flagHardwareAccelerated);
      gameView = new GameViewDraw(this, getApplicationContext(), null);
      Log.i("forplay", "Using hardware-acceleration-friendly game loop");
    } else {
      gameView = new GameViewSurface(this, getApplicationContext(), null);
      Log.i("forplay", "Using software-acceleration-friendly game loop");
    }

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    getWindow().setContentView((View) gameView, params);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    AndroidPlatform.register(this);

    try {
      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
          | PowerManager.ON_AFTER_RELEASE, "forplay");
      wakeLock.acquire();
    } catch (SecurityException e) {
      // Warn the developer of a missing permission. The other calls to
      // wakeLock.acquire/release will throw.
      new AlertDialog.Builder(this).setMessage(
          "Unable to acquire wake lock. Please add <uses-permission android:name=\"android.permission.WAKE_LOCK\" /> to the manifest.").show();
    }
  }

  protected AndroidPlatform platform() {
    return AndroidPlatform.instance;
  }

  private boolean supportsHardwareAcceleration() {
    return android.os.Build.VERSION.SDK_INT >= 11;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    wakeLock.release();
    platform().audio().destroy();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    // TODO: check for display size changes.
  }

  @Override
  protected void onPause() {
    Log.i("forplay", "onPause");
    gameView.notifyVisibilityChanged(View.INVISIBLE);
    platform().audio().pause();
    wakeLock.release();
    super.onPause();

    // TODO: Notify game
  }

  @Override
  protected void onResume() {
    Log.i("forplay", "onResume");
    gameView.notifyVisibilityChanged(View.VISIBLE);
    platform().audio().resume();
    wakeLock.acquire();
    super.onResume();

    // TODO: Notify game
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // TODO
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    platform().keyboard().onKeyDown(keyCode);
    return true;
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    platform().keyboard().onKeyUp(keyCode);
    return true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        platform().pointer().onPointerStart(event.getX(), event.getY());
        break;
      case MotionEvent.ACTION_UP:
        platform().pointer().onPointerEnd(event.getX(), event.getY());
        break;
      case MotionEvent.ACTION_MOVE:
        platform().pointer().onPointerMove(event.getX(), event.getY());
        break;
    }
    return true;
  }

  public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    int displayWidth = right - left;
    int displayHeight = bottom - top;
    
    /*
     * TODO: Pass the width and height here into AndroidGraphics as the display
     * width/height (this is the only way to take into account the size of the
     * Honeycomb bezel). This requires the game activity lifecycle to be
     * reworked, so it is currently not implemented.
     */
  }
}
