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

import java.io.File;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * The main activity for a PlayN game. Override {@link #main} to create your platform independent
 * game and wire it into the Android {@link #platform()}.
 */
public abstract class GameActivity extends Activity {

  private final int REQUIRED_CONFIG_CHANGES = (ActivityInfo.CONFIG_ORIENTATION |
                                               ActivityInfo.CONFIG_KEYBOARD_HIDDEN |
                                               ActivityInfo.CONFIG_SCREEN_SIZE);

  private AndroidPlatform platform;
  private GameViewGL gameView;

  /**
   * The entry-point into a PlayN game activity. Create and initialize your game here. The platform
   * will have been initialized when this method is called, but the first frame tick will not yet
   * have been emitted.
   */
  public abstract void main ();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Build the AndroidPlatform and register this activity.
    Context appctx = getApplicationContext();
    this.platform = createPlatform();
    this.gameView = new GameViewGL(appctx, platform);

    // Build the Window and View
    int windowFlags = makeWindowFlags();
    getWindow().setFlags(windowFlags, windowFlags);

    // Create our layout and configure the window.
    setContentView(gameView);

    // Make sure the AndroidManifest.xml is set up correctly.
    try {
      ActivityInfo info = this.getPackageManager().getActivityInfo(
        new ComponentName(appctx, this.getClass()), 0);
      if ((info.configChanges & REQUIRED_CONFIG_CHANGES) != REQUIRED_CONFIG_CHANGES) {
        new AlertDialog.Builder(this).setMessage(
          "Unable to guarantee application will handle configuration changes. " +
          "Please add the following line to the Activity manifest: " +
          "      android:configChanges=\"keyboardHidden|orientation|screenSize\"").show();
      }
    } catch (NameNotFoundException e) {
      platform.log().warn("Cannot access game AndroidManifest.xml file.");
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    AndroidPlatform.debugLog("onWindowFocusChanged(" + hasFocus + ")");
    if (hasFocus) {
      platform.audio().onResume();
    } else {
      platform.audio().onPause();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    platform.input().onKeyDown(keyCode, event);
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    platform.input().onKeyUp(keyCode, event);
    return super.onKeyUp(keyCode, event);
  }

  @Override
  public void onBackPressed() {
    moveTaskToBack(false);
  }

  @Override
  protected void onDestroy() {
    AndroidPlatform.debugLog("onDestroy");
    for (File file : getCacheDir().listFiles()) {
      file.delete();
    }
    platform.audio().onDestroy();
    platform.onExit();
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    AndroidPlatform.debugLog("onPause");
    gameView.onPause();
    // TODO: we should really wait for the renderer to stop here, because otherwise
    // Platform.onPause could be racing with one final frame on the GL thread; however I've seen
    // scary things about deadlock and other crap by people who have tried to do this "correctly"
    platform.onPause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    AndroidPlatform.debugLog("onResume");
    // since the GL thread is not running, we go ahead and run these onResumes on the UI thread,
    // then resume the GL thread as our last action
    platform.onResume();
    gameView.onResume();
    super.onResume();
  }

  /**
   * Constructs the window flags used when creating our main window. By default this will request a
   * full-screen window, so apps that wish to preserve the notification bar will have to undo that
   * flag.
   */
 protected int makeWindowFlags () {
    return (WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
  }

  /**
   * Returns the name to be used for the preferences that back {@link AndroidStorage}. Defaults to
   * {@code playn}.
   */
  protected String prefsName() {
    return "playn";
  }

  /**
   * Returns the identifier to use for log messages. Defaults to {@code playn}.
   */
  protected String logIdent() {
    return "playn";
  }

  /**
   * Returns the configuration that will be used to decode bitmaps. The default implementation uses
   * {@code ARGB_8888} unless the device memory class is 16MB or less or the device screen is
   * itself {@code ARGB_4444}. NOTE: this is called once during platform initialization and the
   * result is used for the lifetime of the game.
   */
  protected Bitmap.Config preferredBitmapConfig() {
    ActivityManager activityManager = (ActivityManager)
      getApplication().getSystemService(Context.ACTIVITY_SERVICE);
    int memoryClass = activityManager.getMemoryClass();
    int format = getWindowManager().getDefaultDisplay().getPixelFormat();
    // for low memory devices (like the HTC Magic), prefer 16-bit bitmaps
    return (format == PixelFormat.RGBA_4444 || memoryClass <= 16) ?
      Bitmap.Config.ARGB_4444 : Bitmap.Config.ARGB_8888;
  }

  protected float scaleFactor() {
    return getResources().getDisplayMetrics().density;
  }

  /** Configures the maximum simultaneous sounds that may be played back. */
  protected int maxSimultaneousSounds() {
    return 8;
  }

  public GameViewGL gameView() {
    return gameView;
  }

  /**
   * Returns a new AndroidPlatform. This method is protected to provide subclasses a chance to
   * adjust the platform.
   */
  protected AndroidPlatform createPlatform () {
    return new AndroidPlatform(this);
  }

  protected AndroidPlatform platform() {
    return platform;
  }

  protected void setContentView(GameViewGL view) {
    LinearLayout layout = new LinearLayout(this);
    layout.setBackgroundColor(0xFF000000);
    layout.setGravity(Gravity.CENTER);
    layout.addView(gameView);
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    getWindow().setContentView(layout, params);
  }
}
