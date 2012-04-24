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
package playn.html;

import com.google.gwt.typedarrays.client.Float32Array;
import com.google.gwt.webgl.client.WebGLRenderingContext;

import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

import playn.core.InternalTransform;
import playn.core.gl.GLShader;

abstract class HtmlQuadShader implements GLShader
{
  static class Texture extends HtmlQuadShader implements GLShader.Texture {
    private HtmlShaderCore.Texture core;

    Texture(HtmlGLContext ctx) {
      core = new HtmlShaderCore.Texture(ctx);
    }

    @Override
    public void prepare(Object texObj, float alpha, int fbufWidth, int fbufHeight) {
      core.prepare(this, texObj, alpha, fbufWidth, fbufHeight);
    }

    @Override
    protected HtmlShaderCore core() {
      return core;
    }
  }

  static class Color extends HtmlQuadShader implements GLShader.Color {
    private HtmlShaderCore.Color core;

    Color(HtmlGLContext ctx) {
      core = new HtmlShaderCore.Color(ctx);
    }

    @Override
    public void prepare(int color, float alpha, int fbufWidth, int fbufHeight) {
      core.prepare(this, color, alpha, fbufWidth, fbufHeight);
    }

    @Override
    protected HtmlShaderCore core() {
      return core;
    }
  }

  private final Float32Array vertexData = Float32Array.create(HtmlShaderCore.VERTEX_SIZE * 4);
  private int vertexOffset;

  @Override
  public void addQuad(InternalTransform local,
                      float x1, float y1, float sx1, float sy1,
                      float x2, float y2, float sx2, float sy2,
                      float x3, float y3, float sx3, float sy3,
                      float x4, float y4, float sx4, float sy4) {
    HtmlInternalTransform xform = (HtmlInternalTransform) local;
    addVertex(xform, x1, y1, sx1, sy1);
    addVertex(xform, x2, y2, sx2, sy2);
    addVertex(xform, x3, y3, sx3, sy3);
    addVertex(xform, x4, y4, sx4, sy4);

    WebGLRenderingContext gl = core().flush();
    gl.bufferData(ARRAY_BUFFER, vertexData, STREAM_DRAW);
    gl.drawArrays(TRIANGLE_STRIP, 0, vertexOffset/HtmlShaderCore.VERTEX_SIZE);
    vertexOffset = 0;
  }

  @Override
  public void addQuad(InternalTransform local,
                      float x1, float y1, float x2, float y2,
                      float x3, float y3, float x4, float y4) {
    addQuad(local, x1, y1, 0, 0, x2, y2, 0, 0, x3, y3, 0, 0, x4, y4, 0, 0);
  }

  @Override
  public void addTriangles(InternalTransform local, float[] xys, float texWidth, float texHeight,
                           int[] indices) {
    throw new UnsupportedOperationException("Should only be used for quads");
  }

  @Override
  public void addTriangles(InternalTransform local, float[] xys, float[] sxys, int[] indices) {
    throw new UnsupportedOperationException("Should only be used for quads");
  }

  @Override
  public void flush() {
    // noop! we flush immediately after every addQuad
  }

  protected void addVertex(HtmlInternalTransform xform, float dx, float dy, float sx, float sy) {
    vertexData.set(xform.matrix(), vertexOffset);
    vertexOffset += 6;
    vertexData.set(vertexOffset++, dx);
    vertexData.set(vertexOffset++, dy);
    vertexData.set(vertexOffset++, sx);
    vertexData.set(vertexOffset++, sy);
  }

  protected abstract HtmlShaderCore core();
}
