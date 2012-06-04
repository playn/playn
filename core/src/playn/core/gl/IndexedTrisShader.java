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
public abstract class IndexedTrisShader implements GLShader {

  public static class Texture extends IndexedTrisShader implements GLShader.Texture {
    private final Uniform1i uTexture;
    private final Uniform1f uAlpha;
    private int lastTex;
    private float lastAlpha;

    public Texture(GLContext ctx) {
      super(ctx, TEX_FRAG_SHADER);
      uTexture = prog.getUniform1i("u_Texture");
      uAlpha = prog.getUniform1f("u_Alpha");
    }

    @Override
    public void flush() {
      ctx.bindTexture(lastTex);
      super.flush();
    }

    @Override
    public void prepare(int tex, float alpha, int fbufWidth, int fbufHeight) {
      ctx.checkGLError("textureShader.prepare start");
      boolean wasntAlreadyActive = super.prepare(fbufWidth, fbufHeight);
      if (wasntAlreadyActive) {
        ctx.activeTexture(GL20.GL_TEXTURE0);
        uTexture.bind(0);
      }

      if (wasntAlreadyActive || tex != lastTex || alpha != lastAlpha) {
        flush();
        uAlpha.bind(alpha);
        lastAlpha = alpha;
        lastTex = tex;
        ctx.checkGLError("textureShader.prepare end");
      }
    }
  }

  public static class Color extends IndexedTrisShader implements GLShader.Color {
    private final Uniform4f uColor;
    private final Uniform1f uAlpha;
    private int lastColor;
    private float lastAlpha;

    public Color(GLContext ctx) {
      super(ctx, COLOR_FRAG_SHADER);
      uColor = prog.getUniform4f("u_Color");
      uAlpha = prog.getUniform1f("u_Alpha");
    }

    @Override
    public void prepare(int color, float alpha, int fbufWidth, int fbufHeight) {
      ctx.checkGLError("colorShader.prepare start");
      boolean wasntAlreadyActive = super.prepare(fbufWidth, fbufHeight);
      if (wasntAlreadyActive || color != lastColor || alpha != lastAlpha) {
        flush();
        float a = ((color >> 24) & 0xff) / 255f;
        float r = ((color >> 16) & 0xff) / 255f;
        float g = ((color >> 8) & 0xff) / 255f;
        float b = ((color >> 0) & 0xff) / 255f;
        uColor.bind(r, g, b, 1);
        lastColor = color;
        uAlpha.bind(alpha * a);
        lastAlpha = alpha;
        ctx.checkGLError("colorShader.prepare end");
      }
    }
  }

  private static final int VERTEX_SIZE = 10; // 10 floats per vertex
  private static final int START_VERTS = 16*4;
  private static final int EXPAND_VERTS = 16*4;
  private static final int START_ELEMS = 6*START_VERTS/4;
  private static final int EXPAND_ELEMS = 6*EXPAND_VERTS/4;
  private static final int FLOAT_SIZE_BYTES = 4;
  private static final int VERTEX_STRIDE = VERTEX_SIZE * FLOAT_SIZE_BYTES;

  protected final GLContext ctx;
  protected final GLProgram prog;

  protected final Uniform2f uScreenSize;
  protected final Attrib aMatrix, aTranslation, aPosition, aTexCoord;

  protected final GLBuffer.Float vertices;
  protected final GLBuffer.Short elements;

  @Override
  public void addQuad(InternalTransform local,
                      float x1, float y1, float sx1, float sy1,
                      float x2, float y2, float sx2, float sy2,
                      float x3, float y3, float sx3, float sy3,
                      float x4, float y4, float sx4, float sy4) {
    float m00 = local.m00(), m01 = local.m01(), m10 = local.m10(), m11 = local.m11();
    float tx = local.tx(), ty = local.ty();
    int vertIdx = beginPrimitive(4, 6);
    vertices.add(m00, m01, m10, m11, tx, ty).add(x1, y1).add(sx1, sy1);
    vertices.add(m00, m01, m10, m11, tx, ty).add(x2, y2).add(sx2, sy2);
    vertices.add(m00, m01, m10, m11, tx, ty).add(x3, y3).add(sx3, sy3);
    vertices.add(m00, m01, m10, m11, tx, ty).add(x4, y4).add(sx4, sy4);

    elements.add(vertIdx+0);
    elements.add(vertIdx+1);
    elements.add(vertIdx+2);
    elements.add(vertIdx+1);
    elements.add(vertIdx+3);
    elements.add(vertIdx+2);
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
      vertices.add(m00, m01, m10, m11, tx, ty).add(x, y).add(x/tw, y/th);
    }
    for (int ii = 0, ll = indices.length; ii < ll; ii++)
      elements.add(vertIdx+indices[ii]);
  }

  @Override
  public void addTriangles(InternalTransform local, float[] xys, float[] sxys, int[] indices) {
    float m00 = local.m00(), m01 = local.m01(), m10 = local.m10(), m11 = local.m11();
    float tx = local.tx(), ty = local.ty();
    int vertIdx = beginPrimitive(xys.length/2, indices.length);
    for (int ii = 0, ll = xys.length; ii < ll; ii += 2)
      vertices.add(m00, m01, m10, m11, tx, ty).add(xys[ii], xys[ii+1]).add(sxys[ii], sxys[ii+1]);
    for (int ii = 0, ll = indices.length; ii < ll; ii++)
      elements.add(vertIdx+indices[ii]);
  }

  @Override
  public void flush() {
    if (vertices.position() == 0)
      return;
    ctx.checkGLError("Shader.flush");

    vertices.flush(GL20.GL_ARRAY_BUFFER, GL20.GL_STREAM_DRAW);
    int elems = elements.flush(GL20.GL_ELEMENT_ARRAY_BUFFER, GL20.GL_STREAM_DRAW);
    ctx.checkGLError("Shader.flush BufferData");

    elements.drawElements(GL20.GL_TRIANGLES, elems);
    ctx.checkGLError("Shader.flush DrawElements");
  }

  protected IndexedTrisShader(GLContext ctx, String fragShader) {
    this.ctx = ctx;
    this.prog = ctx.createProgram(VERTEX_SHADER, fragShader);

    // determine our various shader program locations
    uScreenSize = prog.getUniform2f("u_ScreenSize");
    aMatrix = prog.getAttrib("a_Matrix", 4, GL20.GL_FLOAT);
    aTranslation = prog.getAttrib("a_Translation", 2, GL20.GL_FLOAT);
    aPosition = prog.getAttrib("a_Position", 2, GL20.GL_FLOAT);
    aTexCoord = prog.getAttrib("a_TexCoord", 2, GL20.GL_FLOAT);

    // create our vertex and index buffers
    vertices = ctx.createFloatBuffer(START_VERTS*VERTEX_SIZE);
    elements = ctx.createShortBuffer(START_ELEMS);
  }

  protected boolean prepare(int fbufWidth, int fbufHeight) {
    if (!ctx.useShader(this))
      return false;

    prog.bind();
    uScreenSize.bind(fbufWidth, fbufHeight);
    vertices.bind(GL20.GL_ARRAY_BUFFER);
    elements.bind(GL20.GL_ELEMENT_ARRAY_BUFFER);
    ctx.checkGLError("Shader.prepare bind");

    aMatrix.bind(VERTEX_STRIDE, 0, vertices);
    aTranslation.bind(VERTEX_STRIDE, 16, vertices);
    aPosition.bind(VERTEX_STRIDE, 24, vertices);
    if (aTexCoord != null)
      aTexCoord.bind(VERTEX_STRIDE, 32, vertices);
    return true;
  }

  /**
   * Begins a primitive with the specified vertex and element count.
   */
  protected int beginPrimitive(int vertexCount, int elemCount) {
    int vertIdx = vertices.position() / VERTEX_SIZE;
    int verts = vertIdx + vertexCount, elems = elements.position() + elemCount;
    int availVerts = vertices.capacity() / VERTEX_SIZE, availElems = elements.capacity();
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

  private void expandVerts(int vertCount) {
    int newVerts = vertices.capacity() / VERTEX_SIZE;
    while (newVerts < vertCount)
      newVerts += EXPAND_VERTS;
    vertices.expand(newVerts*VERTEX_SIZE);
  }

  private void expandElems(int elemCount) {
    int newElems = elements.capacity();
    while (newElems < elemCount)
      newElems += EXPAND_ELEMS;
    elements.expand(newElems);
  }
}
