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

/**
 * A {@link GLShader} implementation that decomposes quads into indexed triangles.
 */
public class IndexedTrisShader extends GLShader {

  /** Declares the uniform variables for our shader. */
  public static final String VERT_UNIFS =
    "uniform vec2 u_ScreenSize;\n";

  /** Declares the attribute variables for our shader. */
  public static final String VERT_ATTRS =
    "attribute vec4 a_Matrix;\n" +
    "attribute vec2 a_Translation;\n" +
    "attribute vec2 a_Position;\n" +
    "attribute vec2 a_TexCoord;\n" +
    "attribute vec2 a_Tint;\n";

  /** Declares the varying variables for our shader. */
  public static final String VERT_VARS =
    "varying vec2 v_TexCoord;\n" +
    "varying vec4 v_Color;\n";

  /** The shader code that computes {@code gl_Position}. */
  public static final String VERT_SETPOS =
    // Transform the vertex.
    "mat3 transform = mat3(\n" +
    "  a_Matrix[0], a_Matrix[1], 0,\n" +
    "  a_Matrix[2], a_Matrix[3], 0,\n" +
    "  a_Translation[0], a_Translation[1], 1);\n" +
    "gl_Position = vec4(transform * vec3(a_Position, 1.0), 1);\n" +
    // Scale from screen coordinates to [0, 2].
    "gl_Position.xy /= (u_ScreenSize.xy / 2.0);\n" +
    // Offset to [-1, 1] and flip y axis to put origin at top-left.
    "gl_Position.x -= 1.0;\n" +
    "gl_Position.y = 1.0 - gl_Position.y;\n";

  /** The shader code that computes {@code v_TexCoord}. */
  public static final String VERT_SETTEX =
    "v_TexCoord = a_TexCoord;\n";

  /** The shader code that computes {@code v_Color}. */
  public static final String VERT_SETCOLOR =
    // tint is encoded as two floats A*R and G*B where A, R, G, B are (0 - 255)
    "float red = mod(a_Tint.x, 256.0);\n" +
    "float alpha = (a_Tint.x - red) / 256.0;\n" +
    "float blue = mod(a_Tint.y, 256.0);\n" +
    "float green = (a_Tint.y - blue) / 256.0;\n" +
    "v_Color = vec4(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0);\n";

  /** The GLSL code for our vertex shader. */
  public static final String VERTEX_SHADER =
    VERT_UNIFS +
    VERT_ATTRS +
    VERT_VARS +
    "void main(void) {\n" +
    VERT_SETPOS +
    VERT_SETTEX +
    VERT_SETCOLOR +
    "}";

  private static final int VERTEX_SIZE = 12; // 12 floats per vertex
  private static final int START_VERTS = 16*4;
  private static final int EXPAND_VERTS = 16*4;
  private static final int START_ELEMS = 6*START_VERTS/4;
  private static final int EXPAND_ELEMS = 6*EXPAND_VERTS/4;
  private static final int FLOAT_SIZE_BYTES = 4;
  private static final int VERTEX_STRIDE = VERTEX_SIZE * FLOAT_SIZE_BYTES;

  public IndexedTrisShader(GLContext ctx) {
    super(ctx);
  }

  /**
   * Returns the vertex shader program. Note that this program <em>must</em> preserve the use of
   * the existing attributes and uniforms. You can add new uniforms and attributes, but you cannot
   * remove or change the defaults.
   */
  protected String vertexShader() {
    return VERTEX_SHADER;
  }

  @Override
  public String toString() {
    return "itris/" + texCore + "/" + colorCore;
  }

  @Override
  protected Core createTextureCore() {
    return new ITCore(vertexShader(), textureFragmentShader());
  }

  @Override
  protected Core createColorCore() {
    return new ITCore(vertexShader(), colorFragmentShader());
  }

  protected class ITCore extends Core {
    private final Uniform2f uScreenSize;
    private final Attrib aMatrix, aTranslation, aPosition, aTexCoord, aColor;

    private final GLBuffer.Float vertices;
    private final GLBuffer.Short elements;

    private float arTint, gbTint;

    public ITCore(String vertShader, String fragShader) {
      super(vertShader, fragShader);

      // determine our various shader program locations
      uScreenSize = prog.getUniform2f("u_ScreenSize");
      aMatrix = prog.getAttrib("a_Matrix", 4, GL20.GL_FLOAT);
      aTranslation = prog.getAttrib("a_Translation", 2, GL20.GL_FLOAT);
      aPosition = prog.getAttrib("a_Position", 2, GL20.GL_FLOAT);
      aTexCoord = prog.getAttrib("a_TexCoord", 2, GL20.GL_FLOAT);
      aColor = prog.getAttrib("a_Tint", 2, GL20.GL_FLOAT);

      // create our vertex and index buffers
      vertices = ctx.createFloatBuffer(START_VERTS*VERTEX_SIZE);
      elements = ctx.createShortBuffer(START_ELEMS);
    }

    @Override
    public void activate(int fbufWidth, int fbufHeight) {
      prog.bind();
      uScreenSize.bind(fbufWidth, fbufHeight);

      vertices.bind(GL20.GL_ARRAY_BUFFER);
      aMatrix.bind(VERTEX_STRIDE, 0);
      aTranslation.bind(VERTEX_STRIDE, 16);
      aPosition.bind(VERTEX_STRIDE, 24);
      if (aTexCoord != null)
        aTexCoord.bind(VERTEX_STRIDE, 32);
      aColor.bind(VERTEX_STRIDE, 40);

      elements.bind(GL20.GL_ELEMENT_ARRAY_BUFFER);
      ctx.checkGLError("Shader.prepare bind");
    }

    @Override
    public void prepare(int tint, boolean justActivated) {
      this.arTint = (tint >> 16) & 0xFFFF;
      this.gbTint = tint & 0xFFFF;
    }

    @Override
    public void flush() {
      if (vertices.position() == 0)
        return;
      ctx.checkGLError("Shader.flush");

      vertices.send(GL20.GL_ARRAY_BUFFER, GL20.GL_STREAM_DRAW);
      int elems = elements.send(GL20.GL_ELEMENT_ARRAY_BUFFER, GL20.GL_STREAM_DRAW);
      ctx.checkGLError("Shader.flush BufferData");

      elements.drawElements(GL20.GL_TRIANGLES, elems);
      ctx.checkGLError("Shader.flush DrawElements");
    }

    @Override
    public void destroy() {
      super.destroy();
      vertices.destroy();
      elements.destroy();
    }

    @Override
    public void addQuad(float m00, float m01, float m10, float m11, float tx, float ty,
                        float x1, float y1, float sx1, float sy1,
                        float x2, float y2, float sx2, float sy2,
                        float x3, float y3, float sx3, float sy3,
                        float x4, float y4, float sx4, float sy4) {
      int vertIdx = beginPrimitive(4, 6);
      vertices.add(m00, m01, m10, m11, tx, ty).add(x1, y1).add(sx1, sy1).add(arTint, gbTint);
      vertices.add(m00, m01, m10, m11, tx, ty).add(x2, y2).add(sx2, sy2).add(arTint, gbTint);
      vertices.add(m00, m01, m10, m11, tx, ty).add(x3, y3).add(sx3, sy3).add(arTint, gbTint);
      vertices.add(m00, m01, m10, m11, tx, ty).add(x4, y4).add(sx4, sy4).add(arTint, gbTint);

      elements.add(vertIdx+0);
      elements.add(vertIdx+1);
      elements.add(vertIdx+2);
      elements.add(vertIdx+1);
      elements.add(vertIdx+3);
      elements.add(vertIdx+2);
    }

    @Override
    public void addTriangles(float m00, float m01, float m10, float m11, float tx, float ty,
                             float[] xys, float tw, float th, int[] indices) {
      int vertIdx = beginPrimitive(xys.length/2, indices.length);
      for (int ii = 0, ll = xys.length; ii < ll; ii += 2) {
        float x = xys[ii], y = xys[ii+1];
        vertices.add(m00, m01, m10, m11, tx, ty).add(x, y).add(x/tw, y/th).add(arTint, gbTint);
      }
      for (int ii = 0, ll = indices.length; ii < ll; ii++)
        elements.add(vertIdx+indices[ii]);
    }

    @Override
    public void addTriangles(float m00, float m01, float m10, float m11, float tx, float ty,
                             float[] xys, float[] sxys, int[] indices) {
      int vertIdx = beginPrimitive(xys.length/2, indices.length);
      for (int ii = 0, ll = xys.length; ii < ll; ii += 2)
        vertices.add(m00, m01, m10, m11, tx, ty).add(xys[ii], xys[ii+1]).add(sxys[ii], sxys[ii+1]).
          add(arTint, gbTint);
      for (int ii = 0, ll = indices.length; ii < ll; ii++)
        elements.add(vertIdx+indices[ii]);
    }

    @Override
    public String toString() {
      return "cq=" + (elements.capacity()/6);
    }

    protected int beginPrimitive(int vertexCount, int elemCount) {
      int vertIdx = vertices.position() / VERTEX_SIZE;
      int verts = vertIdx + vertexCount, elems = elements.position() + elemCount;
      int availVerts = vertices.capacity() / VERTEX_SIZE, availElems = elements.capacity();
      if ((verts > availVerts) || (elems > availElems)) {
        IndexedTrisShader.this.flush();
        if (verts > availVerts)
          expandVerts(verts);
        if (elems > availElems)
          expandElems(elems);
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
}
