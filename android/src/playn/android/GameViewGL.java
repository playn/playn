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

  private final AndroidPlatform platform;
  private final AndroidGL20 gl20;
  private GameLoop loop;

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
      AndroidPlatform.debugLog("Surface dimensions changed to ( " + width + " , " + height + ")");
    }

    @Override
    public void onDrawFrame(GL10 gl) {
      // Wait until onDrawFrame to make sure all the metrics are in place at this point.
      if (loop == null) {
        loop = new GameLoop(platform);
        loop.start();
      }
      // Handle updating, clearing the screen, and drawing
      if (loop.running())
        loop.run();
    }
  }

  public GameViewGL(AndroidPlatform platform, AndroidGL20 gl20, Context context) {
    super(context);
    this.platform = platform;
    this.gl20 = gl20;
    getHolder().addCallback(this);
    setFocusable(true);
    setEGLContextClientVersion(2);
    // FIXME: Need to use android3.0 as a Maven artifact for this to work
    // if (platform.activity.isHoneycombOrLater()) {
    //   setPreserveEGLContextOnPause(true);
    // }
    this.setRenderer(new AndroidRendererGL());
    setRenderMode(RENDERMODE_CONTINUOUSLY);
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

  @Override
  protected void onSizeChanged(int width, int height, int owidth, int oheight) {
    super.onSizeChanged(width, height, owidth, oheight);
    platform.onSizeChanged(width, height);
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
}
