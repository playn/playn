/**
 * Copyright 2012 The PlayN Authors
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
package playn.core.gl;

import playn.core.InternalTransform;

/**
 * Handles the machinations needed to maintain a separate texture and color shader, each of which
 * differ from the other only slightly.
 */
public abstract class AbstractShader implements GLShader {

  protected final GLContext ctx;
  protected final Core texCore, colorCore;
  protected final Extras texExtras, colorExtras;

  protected Core curCore;
  protected Extras curExtras;

  protected AbstractShader(GLContext ctx) {
    this.ctx = ctx;
    this.texCore = createTextureCore(ctx);
    this.colorCore = createColorCore(ctx);
    this.texExtras = createTextureExtras(texCore.program());
    this.colorExtras = createColorExtras(colorCore.program());
  }

  @Override
  public void prepareTexture(int tex, float alpha, int fbufWidth, int fbufHeight) {
    boolean wasntAlreadyActive = ctx.useShader(this, curCore != texCore);
    if (wasntAlreadyActive) {
      curCore = texCore;
      curExtras = texExtras;
      texCore.prepare(fbufWidth, fbufHeight);
    }
    texExtras.prepare(tex, alpha, wasntAlreadyActive);
  }

  @Override
  public void prepareColor(int color, float alpha, int fbufWidth, int fbufHeight) {
    boolean wasntAlreadyActive = ctx.useShader(this, curCore != colorCore);
    if (wasntAlreadyActive) {
      curCore = colorCore;
      curExtras = colorExtras;
      colorCore.prepare(fbufWidth, fbufHeight);
    }
    colorExtras.prepare(color, alpha, wasntAlreadyActive);
  }

  @Override
  public void flush() {
    curExtras.willFlush();
    curCore.flush();
  }

  @Override
  public void addQuad(InternalTransform local,
                      float x1, float y1, float sx1, float sy1,
                      float x2, float y2, float sx2, float sy2,
                      float x3, float y3, float sx3, float sy3,
                      float x4, float y4, float sx4, float sy4) {
    curCore.addQuad(local, x1, y1, sx1, sy1, x2, y2, sx2, sy2,
                    x3, y3, sx3, sy3, x4, y4, sx4, sy4);
  }

  @Override
  public void addQuad(InternalTransform local,
                      float x1, float y1, float x2, float y2,
                      float x3, float y3, float x4, float y4) {
    addQuad(local, x1, y1, 0, 0, x2, y2, 0, 0, x3, y3, 0, 0, x4, y4, 0, 0);
  }

  @Override
  public void addTriangles(InternalTransform local, float[] xys, float tw, float th, int[] indices) {
    curCore.addTriangles(local, xys, tw, th, indices);
  }

  @Override
  public void addTriangles(InternalTransform local, float[] xys, float[] sxys, int[] indices) {
    curCore.addTriangles(local, xys, sxys, indices);
  }

  /** Implements the core of the indexed tris shader. */
  protected static abstract class Core {
    /** Returns this core's shader program. */
    public abstract GLProgram program();

    /** Prepares this core's shader to render. */
    public abstract void prepare(int fbufWidth, int fbufHeight);

    /** Flushes this core's queued geometry to the GPU. */
    public abstract void flush();

    /** See {@link GLShader#addQuad}. */
    public abstract void addQuad(InternalTransform local,
                                 float x1, float y1, float sx1, float sy1,
                                 float x2, float y2, float sx2, float sy2,
                                 float x3, float y3, float sx3, float sy3,
                                 float x4, float y4, float sx4, float sy4);

    /** See {@link GLShader#addTriangles}. */
    public abstract void addTriangles(InternalTransform local,
                                      float[] xys, float tw, float th, int[] indices);

    /** See {@link GLShader#addTriangles}. */
    public abstract void addTriangles(InternalTransform local,
                                      float[] xys, float[] sxys, int[] indices);
  }

  /** Handles the extra bits needed when we're using textures or flat color. */
  protected static abstract class Extras {
    /** Performs additional binding to prepare for a texture or color render. */
    public abstract void prepare(int texOrColor, float alpha, boolean wasntAlreadyActive);

    /** Called prior to flushing this shader. */
    public void willFlush() {} // NOOP by default
  }

  protected abstract Core createTextureCore(GLContext ctx);
  protected abstract Core createColorCore(GLContext ctx);
  protected abstract Extras createTextureExtras(GLProgram prog);
  protected abstract Extras createColorExtras(GLProgram prog);
}
