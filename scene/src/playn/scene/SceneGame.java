/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.scene;

import playn.core.*;
import react.Slot;

/**
 * A simple class for games which wish to use a single scene graph.
 */
public abstract class SceneGame extends Game {

  private float cred, cgreen, cblue, calpha; // default to zero

  public final QuadBatch defaultBatch;
  public final Surface viewSurf;
  public final RootLayer rootLayer;

  public SceneGame (Platform plat, int updateRate) {
    super(plat, updateRate);

    GL20 gl = plat.graphics().gl;
    gl.glDisable(GL20.GL_CULL_FACE);
    gl.glEnable(GL20.GL_BLEND);
    gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

    defaultBatch = createDefaultBatch(gl);
    viewSurf = new Surface(plat.graphics(), plat.graphics().defaultRenderTarget, defaultBatch);
    rootLayer = new RootLayer();

    paint.connect(new Slot<Clock>() {
      public void onEmit (Clock clock) { paintScene(); }
    }).atPrio(scenePaintPrio());
  }

  /**
   * Configures the color to which the frame buffer is cleared prior to painting the scene graph.
   */
  public void setClearColor (float red, float green, float blue, float alpha) {
    cred = red; cgreen = green; cblue = blue; calpha = alpha;
  }

  /**
   * Configures the color to which the frame buffer is cleared prior to painting the scene graph.
   */
  public void setClearColor (int argb) {
    float red   = ((argb >> 16) & 0xFF) / 255f;
    float green = ((argb >>  8) & 0xFF) / 255f;
    float blue  = ((argb >>  0) & 0xFF) / 255f;
    float alpha = ((argb >> 24) & 0xFF) / 255f;
    setClearColor(red, green, blue, alpha);
  }

  /**
   * Renders the main scene graph into the OpenGL frame buffer.
   */
  protected void paintScene () {
    viewSurf.saveTx();
    viewSurf.begin();
    viewSurf.clear(cred, cgreen, cblue, calpha);
    try {
      rootLayer.paint(viewSurf);
    } finally {
      viewSurf.end();
      viewSurf.restoreTx();
    }
  }

  /** Defines the priority at which the scene graph is painted. By default this is -1 which causes
    * the scene graph to be painted <em>after</em> any slots listening to the paint tick at the
    * default priority (0). */
  protected int scenePaintPrio () {
    return -1;
  }

  /** Creates the {@link QuadBatch} used as the default top-level batch when rendering the scene
    * graph. This uses {@link UniformQuadBatch} if possible, {@link TriangleBatch} otherwise. */
  protected QuadBatch createDefaultBatch (GL20 gl) {
    try {
      if (UniformQuadBatch.isLikelyToPerform(gl)) return new UniformQuadBatch(gl);
    } catch (Exception e) {
      // oops, fall through and use a TriangleBatch
    }
    return new TriangleBatch(gl);
  }
}
