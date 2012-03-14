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
 * A {@link GLShader} implementation that decomposes quads into indexed triangles.
 */
public abstract class IndexedTrisShader implements GLShader
{
  @Override
  public void addQuad(InternalTransform local,
                      float x1, float y1, float sx1, float sy1,
                      float x2, float y2, float sx2, float sy2,
                      float x3, float y3, float sx3, float sy3,
                      float x4, float y4, float sx4, float sy4) {
    float m00 = local.m00(), m01 = local.m01(), m10 = local.m10(), m11 = local.m11();
    float tx = local.tx(), ty = local.ty();
    int vertIdx = beginPrimitive(4, 6);
    addVertex(m00, m01, m10, m11, tx, ty, x1, y1, sx1, sy1);
    addVertex(m00, m01, m10, m11, tx, ty, x2, y2, sx2, sy2);
    addVertex(m00, m01, m10, m11, tx, ty, x3, y3, sx3, sy3);
    addVertex(m00, m01, m10, m11, tx, ty, x4, y4, sx4, sy4);
    addElement(vertIdx+0);
    addElement(vertIdx+1);
    addElement(vertIdx+2);
    addElement(vertIdx+1);
    addElement(vertIdx+3);
    addElement(vertIdx+2);
  }

  @Override
  public void addQuad(InternalTransform local,
                      float x1, float y1, float x2, float y2,
                      float x3, float y3, float x4, float y4) {
    addQuad(local, x1, y1, 0, 0, x2, y2, 0, 0, x3, y3, 0, 0, x4, y4, 0, 0);
  }

  @Override
  public void addTriangles(InternalTransform local, float[] xys, float tw, float th, int[] indices) {
    float m00 = local.m00(), m01 = local.m01(), m10 = local.m10(), m11 = local.m11();
    float tx = local.tx(), ty = local.ty();
    int vertIdx = beginPrimitive(xys.length/2, indices.length);
    for (int ii = 0, ll = xys.length; ii < ll; ii += 2) {
      float x = xys[ii], y = xys[ii+1];
      addVertex(m00, m01, m10, m11, tx, ty, x, y, x/tw, y/th);
    }
    for (int ii = 0, ll = indices.length; ii < ll; ii++)
      addElement(vertIdx+indices[ii]);
  }

  @Override
  public void addTriangles(InternalTransform local, float[] xys, float[] sxys, int[] indices) {
    float m00 = local.m00(), m01 = local.m01(), m10 = local.m10(), m11 = local.m11();
    float tx = local.tx(), ty = local.ty();
    int vertIdx = beginPrimitive(xys.length/2, indices.length);
    for (int ii = 0, ll = xys.length; ii < ll; ii += 2)
      addVertex(m00, m01, m10, m11, tx, ty, xys[ii], xys[ii+1], sxys[ii], sxys[ii+1]);
    for (int ii = 0, ll = indices.length; ii < ll; ii++)
      addElement(vertIdx+indices[ii]);
  }

  /**
   * Begins a primitive with the specified vertex and element count.
   */
  protected abstract int beginPrimitive(int vertexCount, int elemCount);

  /**
   * Adds a vertex to a primitive that was started with {@link #beginPrimitive}.
   */
  protected abstract void addVertex(float m00, float m01, float m10, float m11, float tx, float ty,
                                    float dx, float dy, float sx, float sy);

  /**
   * Adds an element to a primitive that was started with {@link #beginPrimitive}.
   */
  protected abstract void addElement(int index);
}
