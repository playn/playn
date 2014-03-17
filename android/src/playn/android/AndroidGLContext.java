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

import java.util.*;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

import playn.core.gl.GL20;
import playn.core.gl.GL20Context;

class AndroidGLContext extends GL20Context {

  /** An interface implemented by entities that need to store things when our GL context is lost
   * and restore them when we are given a new context. */
  public interface Refreshable {
    /** Called when our GL context is about to go away. */
    void onSurfaceLost();
    /** Called when we have been given a new GL context. */
    void onSurfaceCreated();
  }

  public static final boolean CHECK_ERRORS = false;

  private Map<Refreshable, Void> refreshables =
    Collections.synchronizedMap(new WeakHashMap<Refreshable, Void>());

  AndroidGLContext(AndroidPlatform platform, AndroidGL20 gfx) {
    super(platform, gfx, platform.activity.scaleFactor(), CHECK_ERRORS);
  }

  void onSurfaceCreated() {
    incrementEpoch(); // increment our GL context epoch
    init(); // reinitialize GL
    for (Refreshable ref : refreshables.keySet()) {
      ref.onSurfaceCreated();
    }
  }

  void onSurfaceLost() {
    for (Refreshable ref : refreshables.keySet()) {
      ref.onSurfaceLost();
    }
  }

  void updateTexture(int texture, Bitmap image) {
    gl.glBindTexture(GL20.GL_TEXTURE_2D, texture);
    GLUtils.texImage2D(GL20.GL_TEXTURE_2D, 0, image, 0);
    checkGLError("updateTexture end");
  }

  void addRefreshable(Refreshable ref) {
    assert ref != null;
    refreshables.put(ref, null);
  }

  void removeRefreshable(Refreshable ref) {
    assert ref != null;
    refreshables.remove(ref);
  }
}
