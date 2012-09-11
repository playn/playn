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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;

import playn.core.Keyboard;
import playn.core.Pointer;
import playn.core.Touch;

public class GameViewGL extends GLSurfaceView implements SurfaceHolder.Callback {

  private static volatile int contextId = 1;

  private final AndroidGL20 gl20;
  private final GameActivity activity;
  private GameLoop loop;
  private boolean gameSizeSet = false; // Set by AndroidGraphics
  AndroidPlatform platform;

  private class AndroidRendererGL implements Renderer {
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
      contextId++;
      // EGLContext lost, so surfaces need to be rebuilt and redrawn.
      if (platform != null) {
        platform.graphics().ctx.onSurfaceCreated();
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
      // Wait until onDrawFrame to make sure all the metrics are in place at this point.
      if (platform == null) {
        platform = AndroidPlatform.register(gl20, activity);
        activity.main();
        loop = new GameLoop(platform);
        loop.start();
      }
      // Handle updating, clearing the screen, and drawing
      if (loop.running())
        loop.run();
    }
  }

  public GameViewGL(AndroidGL20 _gl20, GameActivity activity, Context context) {
    super(context);
    this.gl20 = _gl20;
    this.activity = activity;
    getHolder().addCallback(this);
    setFocusable(true);
    setEGLContextClientVersion(2);
    if (activity.isHoneycombOrLater()) {
      // FIXME: Need to use android3.0 as a Maven artifact for this to work
      // setPreserveEGLContextOnPause(true);
    }
    this.setRenderer(new AndroidRendererGL());
    setRenderMode(RENDERMODE_CONTINUOUSLY);
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Default to filling all the available space when the game is first loads
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
    if (platform != null) {
      queueEvent(new Runnable() {
        @Override
        public void run() {
          platform.graphics().ctx.onSurfaceLost();
          platform.onPause();
        }
      });
    }
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (platform != null) {
      queueEvent(new Runnable() {
        @Override
        public void run() {
          platform.onResume();
        }
      });
    }
  }

  void onKeyDown(final Keyboard.Event event) {
    queueEvent(new Runnable() {
      @Override
      public void run() {
        platform.keyboard().onKeyDown(event);
      }
    });
  }

  void onKeyTyped(final Keyboard.TypedEvent event) {
    queueEvent(new Runnable() {
      @Override
      public void run() {
        platform.keyboard().onKeyTyped(event);
      }
    });
  }

  void onKeyUp(final Keyboard.Event event) {
    queueEvent(new Runnable() {
      @Override
      public void run() {
        platform.keyboard().onKeyUp(event);
      }
    });
  }

  void onPointerStart(final Pointer.Event.Impl event) {
    queueEvent(new Runnable() {
      @Override
      public void run() {
        platform.pointer().onPointerStart(event);
      }
    });
  }

  void onPointerDrag(final Pointer.Event.Impl event) {
    queueEvent(new Runnable() {
      @Override
      public void run() {
        platform.pointer().onPointerDrag(event);
      }
    });
  }

  void onPointerEnd(final Pointer.Event.Impl event) {
    queueEvent(new Runnable() {
      @Override
      public void run() {
        platform.pointer().onPointerEnd(event);
      }
    });
  }

  void onTouchStart(final Touch.Event.Impl[] touches) {
    queueEvent(new Runnable() {
      @Override
      public void run() {
        platform.touch().onTouchStart(touches);
      }
    });
  }

  void onTouchMove(final Touch.Event.Impl[] touches) {
    queueEvent(new Runnable() {
      @Override
      public void run() {
        platform.touch().onTouchMove(touches);
      }
    });
  }

  void onTouchEnd(final Touch.Event.Impl[] touches) {
    queueEvent(new Runnable() {
      @Override
      public void run() {
        platform.touch().onTouchEnd(touches);
      }
    });
  }
}
