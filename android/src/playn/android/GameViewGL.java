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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import playn.core.Keyboard;
import playn.core.Platform;
import playn.core.Pointer;
import playn.core.Touch;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameViewGL extends GLSurfaceView implements SurfaceHolder.Callback {
  private static volatile int contextId = 1;

  public final AndroidGL20 gl20;
  private final AndroidRendererGL renderer;
  private final SurfaceHolder holder;
  private GameLoop loop;
  private final GameActivity activity;
  private AndroidGraphics gfx;
  private AndroidKeyboard keyboard;
  private AndroidPointer pointer;
  private AndroidTouch touch;
  private boolean gameInitialized = false;
  private boolean gameSizeSet = false; // Set by AndroidGraphics

  private class AndroidRendererGL implements Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
      contextId++;
      // EGLContext lost, so surfaces need to be rebuilt and redrawn.
      if (gfx != null) {
        gfx.refreshGL();
      }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
      gl20.glViewport(0, 0, width, height);
      if (AndroidPlatform.DEBUG_LOGS)
        Log.d("playn", "Surface dimensions changed to ( " + width + " , " + height + ")");
    }

    @Override
    public void onDrawFrame(GL10 gl) {
      // Wait until onDrawFrame to make sure all the metrics
      // are in place at this point.
      if (!gameInitialized) {
        AndroidPlatform.register(gl20, activity);
        gfx = AndroidPlatform.instance.graphics();
        keyboard = AndroidPlatform.instance.keyboard();
        pointer = AndroidPlatform.instance.pointer();
        touch = AndroidPlatform.instance.touch();
        activity.main();
        loop = new GameLoop();
        loop.start();
        gameInitialized = true;
      }
      // Handle updating, clearing the screen, and drawing
      if (loop.running() && gameInitialized)
        loop.run();
    }

    void onPause() {
      if (gfx != null) {
        gfx.storeSurfaces();
      }
    }
  }

  public GameViewGL(AndroidGL20 _gl20, GameActivity activity, Context context) {
    super(context);
    this.gl20 = _gl20;
    this.activity = activity;
    holder = getHolder();
    holder.addCallback(this);
    setFocusable(true);
    setEGLContextClientVersion(2);
    if (activity.isHoneycombOrLater()) {
      // FIXME: Need to use android3.0 as a Maven artifact for this to work
      // setPreserveEGLContextOnPause(true);
    }
    this.setRenderer(renderer = new AndroidRendererGL());
    setRenderMode(RENDERMODE_CONTINUOUSLY);
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Default to filling all the available space when the game is first loads
    Platform platform = activity.platform();
    if (platform != null && gameSizeSet) {
      int width = platform.graphics().width();
      int height = platform.graphics().height();
      if (width == 0 || height == 0) {
        Log.e("playn", "Invalid game size set: (" + width + " , " + height + ")");
      } else {
        int minWidth = getSuggestedMinimumWidth();
        int minHeight = getSuggestedMinimumHeight();
        width = width > minWidth ? width : minWidth;
        height = height > minHeight ? height : minHeight;
        setMeasuredDimension(width, height);
        if (AndroidPlatform.DEBUG_LOGS)
          Log.d("playn", "Using game-specified sizing. (" + width + " , " + height + ")");
        return;
      }
    }

    if (AndroidPlatform.DEBUG_LOGS) Log.d("playn", "Using default sizing.");
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  void gameSizeSet() {
    gameSizeSet = true;
  }

  static int contextId() {
    return contextId;
  }

  /*
   * Input and lifecycle functions called by the UI thread.
   */
  public void notifyVisibilityChanged(int visibility) {
    Log.i("playn", "notifyVisibilityChanged: " + visibility);
    if (visibility == INVISIBLE) {
      if (loop != null)
        loop.pause();
      onPause();
    } else {
      if (loop != null)
        loop.start();
      onResume();
    }
  }

  @Override
  public void onPause() {
    queueEvent(new Runnable() {
      // This method will be called on the rendering
      // thread:
      @Override
      public void run() {
        renderer.onPause();
      }
    });
    super.onPause();
  }

  void onKeyDown(Keyboard.Event event) {
    queueEvent(new onKeyDownRunnable(event));
  }

  void onKeyUp(Keyboard.Event event) {
    queueEvent(new onKeyUpRunnable(event));
  }

  void onPointerStart(Pointer.Event event) {
    queueEvent(new onPointerStartRunnable(event));
  }

  void onPointerDrag(Pointer.Event event) {
    queueEvent(new onPointerDragRunnable(event));
  }

  void onPointerEnd(Pointer.Event event) {
    queueEvent(new onPointerEndRunnable(event));
  }

  void onTouchStart(Touch.Event[] touches) {
    queueEvent(new onTouchStartRunnable(touches));
  }

  void onTouchMove(Touch.Event[] touches) {
    queueEvent(new onTouchMoveRunnable(touches));
  }

  void onTouchEnd(Touch.Event[] touches) {
    queueEvent(new onTouchEndRunnable(touches));
  }

  /*
   * Runnables for posting inputs to the GL thread for processing
   */
  private class onPointerStartRunnable implements Runnable {
    private Pointer.Event event;

    onPointerStartRunnable(Pointer.Event event) {
      super();
      this.event = event;
    }

    @Override
    public void run() {
      if (pointer != null)
        pointer.onPointerStart(event);
    }
  }

  private class onPointerDragRunnable implements Runnable {
    private Pointer.Event event;

    onPointerDragRunnable(Pointer.Event event) {
      super();
      this.event = event;
    }

    @Override
    public void run() {
      if (pointer != null)
        pointer.onPointerDrag(event);
    }
  }

  private class onPointerEndRunnable implements Runnable {
    private Pointer.Event event;

    onPointerEndRunnable(Pointer.Event event) {
      super();
      this.event = event;
    }

    @Override
    public void run() {
      if (pointer != null)
        pointer.onPointerEnd(event);
    }
  }

  private class onTouchStartRunnable implements Runnable {
    private Touch.Event[] touches;

    onTouchStartRunnable(Touch.Event[] touches) {
      super();
      this.touches = touches;
    }

    @Override
    public void run() {
      if (touch != null)
        touch.onTouchStart(touches);
    }
  }

  private class onTouchMoveRunnable implements Runnable {
    private Touch.Event[] touches;

    onTouchMoveRunnable(Touch.Event[] touches) {
      super();
      this.touches = touches;
    }

    @Override
    public void run() {
      if (touch != null)
        touch.onTouchMove(touches);
    }
  }

  private class onTouchEndRunnable implements Runnable {
    private Touch.Event[] touches;

    onTouchEndRunnable(Touch.Event[] touches) {
      super();
      this.touches = touches;
    }

    @Override
    public void run() {
      if (touch != null)
        touch.onTouchEnd(touches);
    }
  }

  private class onKeyDownRunnable implements Runnable {
    private Keyboard.Event event;

    onKeyDownRunnable(Keyboard.Event event) {
      super();
      this.event = event;
    }

    @Override
    public void run() {
      if (keyboard != null)
        keyboard.onKeyDown(event);
    }
  }

  private class onKeyUpRunnable implements Runnable {
    private Keyboard.Event event;

    onKeyUpRunnable(Keyboard.Event event) {
      super();
      this.event = event;
    }

    @Override
    public void run() {
      if (keyboard != null)
        keyboard.onKeyUp(event);
    }
  }
}
