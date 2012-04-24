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
package playn.html;

import com.google.gwt.typedarrays.client.Float32Array;
import com.google.gwt.typedarrays.client.Uint16Array;
import com.google.gwt.webgl.client.WebGLBuffer;
import com.google.gwt.webgl.client.WebGLRenderingContext;

import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

import playn.core.gl.GLShader;
import playn.core.gl.IndexedTrisShader;

abstract class HtmlIndexedTrisShader extends IndexedTrisShader {

  static class Texture extends HtmlIndexedTrisShader implements GLShader.Texture {
    private HtmlShaderCore.Texture core;

    Texture(HtmlGLContext ctx) {
      core = new HtmlShaderCore.Texture(ctx) {
        public void bindBuffers() {
          super.bindBuffers();
          gl.bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer);
        }
        private final WebGLBuffer elementBuffer = gl.createBuffer();
      };
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

  static class Color extends HtmlIndexedTrisShader implements GLShader.Color {
    private HtmlShaderCore.Color core;

    Color(HtmlGLContext ctx) {
      core = new HtmlShaderCore.Color(ctx) {
        public void bindBuffers() {
          super.bindBuffers();
          gl.bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer);
        }
        private final WebGLBuffer elementBuffer = gl.createBuffer();
      };
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

  private static final int START_VERTS = 16*4;
  private static final int EXPAND_VERTS = 16*4;
  private static final int START_ELEMS = 6*START_VERTS/4;
  private static final int EXPAND_ELEMS = 6*EXPAND_VERTS/4;

  private Float32Array vertexData;
  private Uint16Array elementData;
  private int vertexOffset, elementOffset;

  @Override
  public void flush() {
    if (vertexOffset == 0)
      return;

    WebGLRenderingContext gl = core().flush();
    gl.bufferData(ARRAY_BUFFER, vertexData, STREAM_DRAW);
    gl.bufferData(ELEMENT_ARRAY_BUFFER, elementData, STREAM_DRAW);
    gl.drawElements(TRIANGLES, elementOffset, UNSIGNED_SHORT, 0);
    vertexOffset = elementOffset = 0;
  }

  protected HtmlIndexedTrisShader() {
    expandVerts(START_VERTS);
    expandElems(START_ELEMS);
  }

  protected abstract HtmlShaderCore core();

  @Override
  protected int beginPrimitive(int vertexCount, int elemCount) {
    int vertIdx = vertexOffset / HtmlShaderCore.VERTEX_SIZE;
    int verts = vertIdx + vertexCount, elems = elementOffset + elemCount;
    int availVerts = vertexData.getLength() / HtmlShaderCore.VERTEX_SIZE;
    int availElems = elementData.getLength();
    if ((verts > availVerts) || (elems > availElems)) {
      flush();
      if (vertexCount > availVerts)
        expandVerts(vertexCount);
      if (elemCount > availElems)
        expandElems(elemCount);
      return 0;
    }
    return vertIdx;
  }

  @Override
  protected void addVertex(float m00, float m01, float m10, float m11, float tx, float ty,
                           float dx, float dy, float sx, float sy) {
    vertexData.set(vertexOffset + 0, m00);
    vertexData.set(vertexOffset + 1, m01);
    vertexData.set(vertexOffset + 2, m10);
    vertexData.set(vertexOffset + 3, m11);
    vertexData.set(vertexOffset + 4, tx);
    vertexData.set(vertexOffset + 5, ty);
    vertexData.set(vertexOffset + 6, dx);
    vertexData.set(vertexOffset + 7, dy);
    vertexData.set(vertexOffset + 8, sx);
    vertexData.set(vertexOffset + 9, sy);
    vertexOffset += HtmlShaderCore.VERTEX_SIZE;
  }

  @Override
  protected void addElement(int index) {
    elementData.set(elementOffset++, index);
  }

  private void expandVerts(int vertCount) {
    int newVerts = (vertexData == null) ? 0 : vertexData.getLength() / HtmlShaderCore.VERTEX_SIZE;
    while (newVerts < vertCount)
      newVerts += EXPAND_VERTS;
    vertexData = Float32Array.create(HtmlShaderCore.VERTEX_SIZE * vertCount);
  }

  private void expandElems(int elemCount) {
    int newElems = (elementData == null) ? 0 : elementData.getLength();
    while (newElems < elemCount)
      newElems += EXPAND_ELEMS;
    elementData = Uint16Array.create(elemCount);
  }
}
