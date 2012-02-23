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

import playn.core.Key;
import playn.core.Keyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * TODO: save/restore state
 */
public abstract class GameActivity extends Activity {
  private final int REQUIRED_CONFIG_CHANGES = ActivityInfo.CONFIG_ORIENTATION
      | ActivityInfo.CONFIG_KEYBOARD_HIDDEN;

  private GameViewGL gameView;
  private AndroidLayoutView viewLayout;
  private Context context;

  /**
   * The entry-point into a PlayN game. Developers should implement main() to call
   * platform().assets().setPathPrefix() and PlayN.run().
   */
  public abstract void main();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    context = getApplicationContext();

    // Build the AndroidPlatform and register this activity.
    AndroidGL20 gl20;
    if (isHoneycombOrLater()) {
      gl20 = new AndroidGL20();
    } else {
      // Provide our own native bindings for some missing methods.
      gl20 = new AndroidGL20Native();
    }

    // Build a View to hold the surface view and report changes to the screen
    // size.
    viewLayout = new AndroidLayoutView(this);
    gameView = new GameViewGL(gl20, this, context);
    viewLayout.addView(gameView);

    // Build the Window and View
    if (isHoneycombOrLater()) {
      // Use the raw constant rather than the flag to avoid blowing up on
      // earlier Android
      int flagHardwareAccelerated = 0x1000000;
      getWindow().setFlags(flagHardwareAccelerated, flagHardwareAccelerated);
    }

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    getWindow().setContentView(viewLayout, params);

    // Default to landscape orientation.
    if (usePortraitOrientation()) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    // Make sure the AndroidManifest.xml is set up correctly.
    try {
      ActivityInfo info = this.getPackageManager().getActivityInfo(
          new ComponentName(context, this.getPackageName() + "." + this.getLocalClassName()), 0);
      if ((info.configChanges & REQUIRED_CONFIG_CHANGES) != REQUIRED_CONFIG_CHANGES) {
        new AlertDialog.Builder(this).setMessage(
            "Unable to guarantee application will handle configuration changes. "
                + "Please add the following line to the Activity manifest: "
                + "      android:configChanges=\"keyboardHidden|orientation\"").show();
      }
    } catch (NameNotFoundException e) {
      Log.w("playn", "Cannot access game AndroidManifest.xml file.");
    }
  }

  /**
   * Determines whether or not a game should run in portrait orientation or not.
   * Defaults to false. Override this method to return true to use portrait.
   *
   * @return Whether or not the game will run in portrait orientation
   */
  public boolean usePortraitOrientation() {
    return false;
  }

  public LinearLayout viewLayout() {
    return viewLayout;
  }

  public GameViewGL gameView() {
    return gameView;
  }

  protected AndroidPlatform platform() {
    return AndroidPlatform.instance;
  }

  protected Context context() {
    return context;
  }

  boolean isHoneycombOrLater() {
    return android.os.Build.VERSION.SDK_INT >= 11;
  }

  @Override
  protected void onDestroy() {
    File cacheDir = getCacheDir();
    File[] tempFiles = cacheDir.listFiles();
    for (File file : tempFiles) {
      file.delete();
    }
    platform().audio().onDestroy();
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    if (AndroidPlatform.DEBUG_LOGS) Log.d("playn", "onPause");
    gameView.notifyVisibilityChanged(View.INVISIBLE);
    if (platform() != null)
      platform().audio().onPause();
    super.onPause();

    // TODO: Notify game
  }

  @Override
  protected void onResume() {
    if (AndroidPlatform.DEBUG_LOGS) Log.d("playn", "onResume");
    gameView.notifyVisibilityChanged(View.VISIBLE);
    if (platform() != null)
      platform().audio().onResume();
    super.onResume();

    // TODO: Notify game
  }

  /**
   * Called automatically to handle keyboard events. Automatically passes through
   * the parsed keyboard event to {@GameViewGL} for processing in the {@Keyboard}
   * Listener instance on the render thread.
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent nativeEvent) {
    long time = nativeEvent.getEventTime();
    Keyboard.Event event = new Keyboard.Event.Impl(time, keyForCode(keyCode));
    gameView.onKeyDown(event);
    boolean downPrevent = event.getPreventDefault();

    boolean typedPrevent = false;
    int unicodeChar = nativeEvent.getUnicodeChar();
    if (unicodeChar != 0) {
      Keyboard.TypedEvent typedEvent = new Keyboard.TypedEvent.Impl(time, (char)unicodeChar);
      gameView.onKeyTyped(typedEvent);
      typedPrevent = typedEvent.getPreventDefault();
    }

    return downPrevent || typedPrevent;
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent nativeEvent) {
    Keyboard.Event event = new Keyboard.Event.Impl(nativeEvent.getEventTime(), keyForCode(keyCode));
    gameView.onKeyUp(event);
    return event.getPreventDefault();
  }

  /**
   * Called automatically to handle touch events. Automatically passes through
   * the parsed MotionEvent to {@GameViewGL} for processing in the {@Touch}
   * and {@Pointer} Listener instances on the render thread.
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return AndroidPlatform.instance.touchEventHandler().onMotionEvent(event);
  }

  // TODO: uncomment the remaining key codes when we upgrade to latest Android jars
  private static Key keyForCode(int keyCode) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_0: return Key.K0;
    case KeyEvent.KEYCODE_1: return Key.K1;
    case KeyEvent.KEYCODE_2: return Key.K2;
    case KeyEvent.KEYCODE_3: return Key.K3;
    case KeyEvent.KEYCODE_4: return Key.K4;
    case KeyEvent.KEYCODE_5: return Key.K5;
    case KeyEvent.KEYCODE_6: return Key.K6;
    case KeyEvent.KEYCODE_7: return Key.K7;
    case KeyEvent.KEYCODE_8: return Key.K8;
    case KeyEvent.KEYCODE_9: return Key.K9;
    case KeyEvent.KEYCODE_A: return Key.A;
    case KeyEvent.KEYCODE_ALT_LEFT: return Key.ALT;
    case KeyEvent.KEYCODE_ALT_RIGHT: return Key.ALT;
    case KeyEvent.KEYCODE_APOSTROPHE: return Key.QUOTE;
    // case KeyEvent.KEYCODE_APP_SWITCH: return Key.APP_SWITCH;
    case KeyEvent.KEYCODE_AT: return Key.AT;
    // case KeyEvent.KEYCODE_AVR_INPUT: return Key.AVR_INPUT;
    // case KeyEvent.KEYCODE_AVR_POWER: return Key.AVR_POWER;
    case KeyEvent.KEYCODE_B: return Key.B;
    case KeyEvent.KEYCODE_BACK: return Key.BACK;
    case KeyEvent.KEYCODE_BACKSLASH: return Key.BACKSLASH;
    // case KeyEvent.KEYCODE_BOOKMARK: return Key.BOOKMARK;
    // case KeyEvent.KEYCODE_BREAK: return Key.BREAK;
    // case KeyEvent.KEYCODE_BUTTON_1: return Key.BUTTON_1;
    // case KeyEvent.KEYCODE_BUTTON_2: return Key.BUTTON_2;
    // case KeyEvent.KEYCODE_BUTTON_3: return Key.BUTTON_3;
    // case KeyEvent.KEYCODE_BUTTON_4: return Key.BUTTON_4;
    // case KeyEvent.KEYCODE_BUTTON_5: return Key.BUTTON_5;
    // case KeyEvent.KEYCODE_BUTTON_6: return Key.BUTTON_6;
    // case KeyEvent.KEYCODE_BUTTON_7: return Key.BUTTON_7;
    // case KeyEvent.KEYCODE_BUTTON_8: return Key.BUTTON_8;
    // case KeyEvent.KEYCODE_BUTTON_9: return Key.BUTTON_9;
    // case KeyEvent.KEYCODE_BUTTON_10: return Key.BUTTON_10;
    // case KeyEvent.KEYCODE_BUTTON_11: return Key.BUTTON_11;
    // case KeyEvent.KEYCODE_BUTTON_12: return Key.BUTTON_12;
    // case KeyEvent.KEYCODE_BUTTON_13: return Key.BUTTON_13;
    // case KeyEvent.KEYCODE_BUTTON_14: return Key.BUTTON_14;
    // case KeyEvent.KEYCODE_BUTTON_15: return Key.BUTTON_15;
    // case KeyEvent.KEYCODE_BUTTON_16: return Key.BUTTON_16;
    // case KeyEvent.KEYCODE_BUTTON_A: return Key.BUTTON_A;
    // case KeyEvent.KEYCODE_BUTTON_B: return Key.BUTTON_B;
    // case KeyEvent.KEYCODE_BUTTON_C: return Key.BUTTON_C;
    // case KeyEvent.KEYCODE_BUTTON_L1: return Key.BUTTON_L1;
    // case KeyEvent.KEYCODE_BUTTON_L2: return Key.BUTTON_L2;
    // case KeyEvent.KEYCODE_BUTTON_MODE: return Key.BUTTON_MODE;
    // case KeyEvent.KEYCODE_BUTTON_R1: return Key.BUTTON_R1;
    // case KeyEvent.KEYCODE_BUTTON_R2: return Key.BUTTON_R2;
    // case KeyEvent.KEYCODE_BUTTON_SELECT: return Key.BUTTON_SELECT;
    // case KeyEvent.KEYCODE_BUTTON_START: return Key.BUTTON_START;
    // case KeyEvent.KEYCODE_BUTTON_THUMBL: return Key.BUTTON_THUMBL;
    // case KeyEvent.KEYCODE_BUTTON_THUMBR: return Key.BUTTON_THUMBR;
    // case KeyEvent.KEYCODE_BUTTON_X: return Key.BUTTON_X;
    // case KeyEvent.KEYCODE_BUTTON_Y: return Key.BUTTON_Y;
    // case KeyEvent.KEYCODE_BUTTON_Z: return Key.BUTTON_Z;
    case KeyEvent.KEYCODE_C: return Key.C;
    case KeyEvent.KEYCODE_CALL: return Key.CALL;
    case KeyEvent.KEYCODE_CAMERA: return Key.CAMERA;
    // case KeyEvent.KEYCODE_CAPS_LOCK: return Key.CAPS_LOCK;
    // case KeyEvent.KEYCODE_CAPTIONS: return Key.CAPTIONS;
    // case KeyEvent.KEYCODE_CHANNEL_DOWN: return Key.CHANNEL_DOWN;
    // case KeyEvent.KEYCODE_CHANNEL_UP: return Key.CHANNEL_UP;
    case KeyEvent.KEYCODE_CLEAR: return Key.CLEAR;
    case KeyEvent.KEYCODE_COMMA: return Key.COMMA;
    // case KeyEvent.KEYCODE_CTRL_LEFT: return Key.CTRL;
    // case KeyEvent.KEYCODE_CTRL_RIGHT: return Key.CTRL;
    case KeyEvent.KEYCODE_D: return Key.D;
    case KeyEvent.KEYCODE_DEL: return Key.DELETE;
    case KeyEvent.KEYCODE_DPAD_CENTER: return Key.DPAD_CENTER;
    case KeyEvent.KEYCODE_DPAD_DOWN: return Key.DPAD_DOWN;
    case KeyEvent.KEYCODE_DPAD_LEFT: return Key.DPAD_LEFT;
    case KeyEvent.KEYCODE_DPAD_RIGHT: return Key.DPAD_RIGHT;
    case KeyEvent.KEYCODE_DPAD_UP: return Key.DPAD_UP;
    // case KeyEvent.KEYCODE_DVR: return Key.DVR;
    case KeyEvent.KEYCODE_E: return Key.E;
    case KeyEvent.KEYCODE_ENDCALL: return Key.ENDCALL;
    case KeyEvent.KEYCODE_ENTER: return Key.ENTER;
    case KeyEvent.KEYCODE_ENVELOPE: return Key.ENVELOPE;
    case KeyEvent.KEYCODE_EQUALS: return Key.EQUALS;
    // case KeyEvent.KEYCODE_ESCAPE: return Key.ESCAPE;
    case KeyEvent.KEYCODE_EXPLORER: return Key.EXPLORER;
    case KeyEvent.KEYCODE_F: return Key.F;
    // case KeyEvent.KEYCODE_F1: return Key.F1;
    // case KeyEvent.KEYCODE_F2: return Key.F2;
    // case KeyEvent.KEYCODE_F3: return Key.F3;
    // case KeyEvent.KEYCODE_F4: return Key.F4;
    // case KeyEvent.KEYCODE_F5: return Key.F5;
    // case KeyEvent.KEYCODE_F6: return Key.F6;
    // case KeyEvent.KEYCODE_F7: return Key.F7;
    // case KeyEvent.KEYCODE_F8: return Key.F8;
    // case KeyEvent.KEYCODE_F9: return Key.F9;
    // case KeyEvent.KEYCODE_F10: return Key.F10;
    // case KeyEvent.KEYCODE_F11: return Key.F11;
    // case KeyEvent.KEYCODE_F12: return Key.F12;
    case KeyEvent.KEYCODE_FOCUS: return Key.FOCUS;
    // case KeyEvent.KEYCODE_FORWARD: return Key.FORWARD;
    // case KeyEvent.KEYCODE_FORWARD_DEL: return Key.FORWARD_DEL;
    // case KeyEvent.KEYCODE_FUNCTION: return Key.FUNCTION;
    case KeyEvent.KEYCODE_G: return Key.G;
    case KeyEvent.KEYCODE_GRAVE: return Key.BACKQUOTE;
    // case KeyEvent.KEYCODE_GUIDE: return Key.GUIDE;
    case KeyEvent.KEYCODE_H: return Key.H;
    case KeyEvent.KEYCODE_HEADSETHOOK: return Key.HEADSETHOOK;
    case KeyEvent.KEYCODE_HOME: return Key.HOME;
    case KeyEvent.KEYCODE_I: return Key.I;
    // case KeyEvent.KEYCODE_INFO: return Key.INFO;
    // case KeyEvent.KEYCODE_INSERT: return Key.INSERT;
    case KeyEvent.KEYCODE_J: return Key.J;
    case KeyEvent.KEYCODE_K: return Key.K;
    case KeyEvent.KEYCODE_L: return Key.L;
    case KeyEvent.KEYCODE_LEFT_BRACKET: return Key.LEFT_BRACKET;
    case KeyEvent.KEYCODE_M: return Key.M;
    // case KeyEvent.KEYCODE_MEDIA_CLOSE: return Key.MEDIA_CLOSE;
    // case KeyEvent.KEYCODE_MEDIA_EJECT: return Key.MEDIA_EJECT;
    // case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: return Key.MEDIA_FAST_FORWARD;
    // case KeyEvent.KEYCODE_MEDIA_NEXT: return Key.MEDIA_NEXT;
    // case KeyEvent.KEYCODE_MEDIA_PAUSE: return Key.MEDIA_PAUSE;
    // case KeyEvent.KEYCODE_MEDIA_PLAY: return Key.MEDIA_PLAY;
    // case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: return Key.MEDIA_PLAY_PAUSE;
    // case KeyEvent.KEYCODE_MEDIA_PREVIOUS: return Key.MEDIA_PREVIOUS;
    // case KeyEvent.KEYCODE_MEDIA_RECORD: return Key.MEDIA_RECORD;
    // case KeyEvent.KEYCODE_MEDIA_REWIND: return Key.MEDIA_REWIND;
    // case KeyEvent.KEYCODE_MEDIA_STOP: return Key.MEDIA_STOP;
    case KeyEvent.KEYCODE_MENU: return Key.MENU;
    // case KeyEvent.KEYCODE_META_LEFT: return Key.META;
    // case KeyEvent.KEYCODE_META_RIGHT: return Key.META;
    case KeyEvent.KEYCODE_MINUS: return Key.MINUS;
    // case KeyEvent.KEYCODE_MOVE_END: return Key.END;
    // case KeyEvent.KEYCODE_MOVE_HOME: return Key.HOME;
    case KeyEvent.KEYCODE_MUTE: return Key.MUTE;
    case KeyEvent.KEYCODE_N: return Key.N;
    case KeyEvent.KEYCODE_NOTIFICATION: return Key.NOTIFICATION;
    case KeyEvent.KEYCODE_NUM: return Key.NUM;
    // case KeyEvent.KEYCODE_NUMPAD_0: return Key.NP0;
    // case KeyEvent.KEYCODE_NUMPAD_1: return Key.NP1;
    // case KeyEvent.KEYCODE_NUMPAD_2: return Key.NP2;
    // case KeyEvent.KEYCODE_NUMPAD_3: return Key.NP3;
    // case KeyEvent.KEYCODE_NUMPAD_4: return Key.NP4;
    // case KeyEvent.KEYCODE_NUMPAD_5: return Key.NP5;
    // case KeyEvent.KEYCODE_NUMPAD_6: return Key.NP6;
    // case KeyEvent.KEYCODE_NUMPAD_7: return Key.NP7;
    // case KeyEvent.KEYCODE_NUMPAD_8: return Key.NP8;
    // case KeyEvent.KEYCODE_NUMPAD_9: return Key.NP9;
    // case KeyEvent.KEYCODE_NUMPAD_ADD: return Key.NP_ADD;
    // case KeyEvent.KEYCODE_NUMPAD_COMMA: return Key.COMMA;
    // case KeyEvent.KEYCODE_NUMPAD_DIVIDE: return Key.NP_DIVIDE;
    // case KeyEvent.KEYCODE_NUMPAD_DOT: return Key.NP_DECIMAL;
    // case KeyEvent.KEYCODE_NUMPAD_ENTER: return Key.NP_ENTER;
    // case KeyEvent.KEYCODE_NUMPAD_EQUALS: return Key.EQUALS;
    // case KeyEvent.KEYCODE_NUMPAD_LEFT_PAREN: return Key.LEFT_PAREN;
    // case KeyEvent.KEYCODE_NUMPAD_MULTIPLY: return Key.NP_MULTIPLY;
    // case KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN: return Key.RIGHT_PAREN;
    // case KeyEvent.KEYCODE_NUMPAD_SUBTRACT: return Key.NP_SUBTRACT;
    // case KeyEvent.KEYCODE_NUM_LOCK: return Key.NP_NUM_LOCK;
    case KeyEvent.KEYCODE_O: return Key.O;
    case KeyEvent.KEYCODE_P: return Key.P;
    case KeyEvent.KEYCODE_PAGE_DOWN: return Key.PAGE_DOWN;
    case KeyEvent.KEYCODE_PAGE_UP: return Key.PAGE_UP;
    case KeyEvent.KEYCODE_PERIOD: return Key.PERIOD;
    case KeyEvent.KEYCODE_PICTSYMBOLS: return Key.PICTSYMBOLS;
    case KeyEvent.KEYCODE_PLUS: return Key.PLUS;
    case KeyEvent.KEYCODE_POUND: return Key.HASH;
    case KeyEvent.KEYCODE_POWER: return Key.POWER;
    // case KeyEvent.KEYCODE_PROG_BLUE: return Key.BLUE;
    // case KeyEvent.KEYCODE_PROG_GREEN: return Key.GREEN;
    // case KeyEvent.KEYCODE_PROG_RED: return Key.RED;
    // case KeyEvent.KEYCODE_PROG_YELLOW: return Key.YELLOW;
    case KeyEvent.KEYCODE_Q: return Key.Q;
    case KeyEvent.KEYCODE_R: return Key.R;
    case KeyEvent.KEYCODE_RIGHT_BRACKET: return Key.RIGHT_BRACKET;
    case KeyEvent.KEYCODE_S: return Key.S;
    // case KeyEvent.KEYCODE_SCROLL_LOCK: return Key.SCROLL_LOCK;
    case KeyEvent.KEYCODE_SEARCH: return Key.SEARCH;
    case KeyEvent.KEYCODE_SEMICOLON: return Key.SEMICOLON;
    // case KeyEvent.KEYCODE_SETTINGS: return Key.SETTINGS;
    case KeyEvent.KEYCODE_SHIFT_LEFT: return Key.SHIFT;
    case KeyEvent.KEYCODE_SHIFT_RIGHT: return Key.SHIFT;
    case KeyEvent.KEYCODE_SLASH: return Key.SLASH;
    case KeyEvent.KEYCODE_SOFT_LEFT: return Key.SOFT_LEFT;
    case KeyEvent.KEYCODE_SOFT_RIGHT: return Key.SOFT_RIGHT;
    case KeyEvent.KEYCODE_SPACE: return Key.SPACE;
    case KeyEvent.KEYCODE_STAR: return Key.STAR;
    // case KeyEvent.KEYCODE_STB_INPUT: return Key.STB_INPUT;
    // case KeyEvent.KEYCODE_STB_POWER: return Key.STB_POWER;
    case KeyEvent.KEYCODE_SWITCH_CHARSET: return Key.SWITCH_CHARSET;
    case KeyEvent.KEYCODE_SYM: return Key.SYM;
    // case KeyEvent.KEYCODE_SYSRQ: return Key.SYSRQ;
    case KeyEvent.KEYCODE_T: return Key.T;
    case KeyEvent.KEYCODE_TAB: return Key.TAB;
    // case KeyEvent.KEYCODE_TV: return Key.TV;
    // case KeyEvent.KEYCODE_TV_INPUT: return Key.TV_INPUT;
    // case KeyEvent.KEYCODE_TV_POWER: return Key.TV_POWER;
    case KeyEvent.KEYCODE_U: return Key.U;
    case KeyEvent.KEYCODE_UNKNOWN: return Key.UNKNOWN;
    case KeyEvent.KEYCODE_V: return Key.V;
    case KeyEvent.KEYCODE_VOLUME_DOWN: return Key.VOLUME_DOWN;
    // case KeyEvent.KEYCODE_VOLUME_MUTE: return Key.VOLUME_MUTE;
    case KeyEvent.KEYCODE_VOLUME_UP: return Key.VOLUME_UP;
    case KeyEvent.KEYCODE_W: return Key.W;
    // case KeyEvent.KEYCODE_WINDOW: return Key.WINDOW;
    case KeyEvent.KEYCODE_X: return Key.X;
    case KeyEvent.KEYCODE_Y: return Key.Y;
    case KeyEvent.KEYCODE_Z: return Key.Z;
    // case KeyEvent.KEYCODE_ZOOM_IN: return Key.ZOOM_IN;
    // case KeyEvent.KEYCODE_ZOOM_OUT: return Key.ZOOM_OUT;
    default: return Key.UNKNOWN;
    }
  }
}
