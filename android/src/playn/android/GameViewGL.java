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

public class GameViewGL extends GLSurfaceView {

  private final AndroidPlatform platform;
  private final AndroidGL20 gl20;
  private GameLoop loop;

  public GameViewGL(Context context, AndroidPlatform platform, AndroidGL20 gl20) {
    super(context);
    this.platform = platform;
    this.gl20 = gl20;

    setFocusable(true);
    setEGLContextClientVersion(2);
    // FIXME: Need to use android3.0 as a Maven artifact for this to work
    // if (platform.activity.isHoneycombOrLater()) {
    //   setPreserveEGLContextOnPause(true);
    // }

    // set up our renderer
    setRenderer(new Renderer() {
      @Override
      public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GameViewGL.this.platform.graphics().ctx.onSurfaceCreated();
      }
      @Override
      public void onSurfaceChanged(GL10 gl, int width, int height) {
        GameViewGL.this.platform.graphics().onSizeChanged(width, height);
        // we defer the start of the game until we've received our initial surface size; thus we
        // create our game loop lazily, then initialize the game and start the loop
        if (loop == null) {
          startGame();
        }
      }
      @Override
      public void onDrawFrame(GL10 gl) {
        if (loop.running())
          loop.run();
      }
    });
    setRenderMode(RENDERMODE_CONTINUOUSLY);
  }

  @Override
  public void onPause() {
    loop.pause();
    // this is a terribly unfortunate hack; we would like to override surfaceDestroyed and indicate
    // that our surface was lost only when that method was called, but surfaceDestroyed is not
    // called when the screen is locked while our game is running (even though we do in fact lose
    // our GL context at that point); so we have to assume that we ALWAYS lose our GL context when
    // paused; that's generally true, so we're probably safe in assuming so, but it sucks
    platform.graphics().ctx.onSurfaceLost();
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    // the very first time we're resumed, our loop will still be null because we wait to create it
    // until after our UI has been laid out and we know the size of the game screen
    if (loop != null)
      loop.start();
  }

  void startGame() {
    loop = new GameLoop(platform);
    queueEvent(new Runnable() {
      @Override
      public void run() {
        platform.activity.main();
        loop.start();
      }
    });
  }
}
