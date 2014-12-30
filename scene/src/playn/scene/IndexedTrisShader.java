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

  /** The same-for-all-verts-in-a-quad attribute variables for our shader. */
  public static final String VERT_ATTRS =
    "attribute vec4 a_Matrix;\n" +
    "attribute vec2 a_Translation;\n" +
    "attribute vec2 a_Color;\n";

  /** The varies-per-vert attribute variables for our shader. */
  public static final String PER_VERT_ATTRS =
    "attribute vec2 a_Position;\n" +
    "attribute vec2 a_TexCoord;\n";

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
    "float red = mod(a_Color.x, 256.0);\n" +
    "float alpha = (a_Color.x - red) / 256.0;\n" +
    "float blue = mod(a_Color.y, 256.0);\n" +
    "float green = (a_Color.y - blue) / 256.0;\n" +
    "v_Color = vec4(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0);\n";

  /** The GLSL code for our vertex shader. */
  public static final String VERTEX_SHADER =
    VERT_UNIFS +
    VERT_ATTRS +
    PER_VERT_ATTRS +
    VERT_VARS +
    "void main(void) {\n" +
    VERT_SETPOS +
    VERT_SETTEX +
    VERT_SETCOLOR +
    "}";

  private static final int START_VERTS = 16*4;
  private static final int EXPAND_VERTS = 16*4;
  private static final int START_ELEMS = 6*START_VERTS/4;
  private static final int EXPAND_ELEMS = 6*EXPAND_VERTS/4;
  private static final int FLOAT_SIZE_BYTES = 4;

  private final boolean delayedBinding;

  public IndexedTrisShader(GLContext ctx) {
    super(ctx);
    delayedBinding = "Intel".equals(ctx.getString(GL20.GL_VENDOR));
  }

  @Override
  public String toString() {
    return "itris/" + texCore;
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
  protected Core createTextureCore() {
    return new ITCore(vertexShader(), textureFragmentShader());
  }

  protected class ITCore extends Core {
    private final Uniform2f uScreenSize;
    private final Attrib aMatrix, aTranslation, aColor; // stable (same for whole quad)
    private final Attrib aPosition, aTexCoord; // changing (varies per quad vertex)

    protected final float[] stableAttrs;
    protected final GLBuffer.Float vertices;
    protected final GLBuffer.Short elements;

    private float arTint, gbTint;

    public ITCore(String vertShader, String fragShader) {
      super(vertShader, fragShader);

      // determine our various shader program locations
      uScreenSize = prog.getUniform2f("u_ScreenSize");
      aMatrix = prog.getAttrib("a_Matrix", 4, GL20.GL_FLOAT);
      aTranslation = prog.getAttrib("a_Translation", 2, GL20.GL_FLOAT);
      aColor = prog.getAttrib("a_Color", 2, GL20.GL_FLOAT);
      aPosition = prog.getAttrib("a_Position", 2, GL20.GL_FLOAT);
      aTexCoord = prog.getAttrib("a_TexCoord", 2, GL20.GL_FLOAT);

      // create our vertex and index buffers
      stableAttrs = new float[stableAttrsSize()];
      vertices = ctx.createFloatBuffer(START_VERTS*vertexSize());
      elements = ctx.createShortBuffer(START_ELEMS);
    }

    @Override
    public void activate(int fbufWidth, int fbufHeight) {
      prog.bind();
      uScreenSize.bind(fbufWidth, fbufHeight);

      // certain graphics cards (I'm looking at you, Intel) exhibit broken behavior if we bind our
      // attributes once during activation, so for those cards we bind every time in flush()
      if (!delayedBinding)
        bindAttribsBufs();

      ctx.checkGLError("Shader.activate bind");
    }

    @Override
    public void deactivate() {
      aMatrix.unbind();
      aTranslation.unbind();
      aColor.unbind();
      aPosition.unbind();
      if (aTexCoord != null)
        aTexCoord.unbind();
    }

    @Override
    public void prepare(int tex, int tint, boolean justActivated) {
      super.prepare(tex, tint, justActivated);
      this.arTint = (tint >> 16) & 0xFFFF;
      this.gbTint = tint & 0xFFFF;
    }

    @Override
    public void flush() {
      super.flush();
      if (vertices.position() == 0)
        return;
      ctx.checkGLError("Shader.flush");

      if (delayedBinding) { // see comments in activate()
        bindAttribsBufs();
        ctx.checkGLError("Shader.flush bind");
      }

      vertices.send(GL20.GL_ARRAY_BUFFER, GL20.GL_STREAM_DRAW);
      int elems = elements.send(GL20.GL_ELEMENT_ARRAY_BUFFER, GL20.GL_STREAM_DRAW);
      ctx.checkGLError("Shader.flush BufferData");

      elements.drawElements(GL20.GL_TRIANGLES, elems);
      ctx.checkGLError("Shader.flush DrawElements");
    }

    private void bindAttribsBufs() {
      vertices.bind(GL20.GL_ARRAY_BUFFER);

      // bind our stable attributes
      int stride = vertexStride();
      aMatrix.bind(stride, 0);
      aTranslation.bind(stride, 16);
      aColor.bind(stride, 24);

      // bind our changing attributes
      int offset = stableAttrsSize()*FLOAT_SIZE_BYTES;
      aPosition.bind(stride, offset);
      if (aTexCoord != null)
        aTexCoord.bind(stride, offset+8);

      elements.bind(GL20.GL_ELEMENT_ARRAY_BUFFER);
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

      // write our stable vertex attributes into a buffer, then copy that in four times
      stableAttrs[0] = m00;
      stableAttrs[1] = m01;
      stableAttrs[2] = m10;
      stableAttrs[3] = m11;
      stableAttrs[4] = tx;
      stableAttrs[5] = ty;
      addExtraStableAttrs(stableAttrs, 6);

      int vertIdx = beginPrimitive(4, 6), offset = vertices.position();
      float[] vertData = vertices.array();
      offset = addVert(vertData, offset, stableAttrs, x1, y1, sx1, sy1);
      offset = addVert(vertData, offset, stableAttrs, x2, y2, sx2, sy2);
      offset = addVert(vertData, offset, stableAttrs, x3, y3, sx3, sy3);
      offset = addVert(vertData, offset, stableAttrs, x4, y4, sx4, sy4);
      vertices.skip(offset - vertices.position());

      addElems(vertIdx, QUAD_INDICES, 0, QUAD_INDICES.length, 0);
    }

    @Override
    public void addTriangles(float m00, float m01, float m10, float m11, float tx, float ty,
                             float[] xys, int xysOffset, int xysLen, float tw, float th,
                             int[] indices, int indicesOffset, int indicesLen, int indexBase) {
      stableAttrs[0] = m00;
      stableAttrs[1] = m01;
      stableAttrs[2] = m10;
      stableAttrs[3] = m11;
      stableAttrs[4] = tx;
      stableAttrs[5] = ty;
      addExtraStableAttrs(stableAttrs, 6);

      int vertIdx = beginPrimitive(xysLen/2, indicesLen);
      int offset = vertices.position();
      float[] vertData = vertices.array();
      for (int ii = xysOffset, ll = ii+xysLen; ii < ll; ii += 2) {
        float x = xys[ii], y = xys[ii+1];
        offset = addVert(vertData, offset, stableAttrs, x, y, x/tw, y/th);
      }
      vertices.skip(offset - vertices.position());

      addElems(vertIdx, indices, indicesOffset, indicesLen, indexBase);
    }

    @Override
    public void addTriangles(float m00, float m01, float m10, float m11, float tx, float ty,
                             float[] xys, float[] sxys, int xysOffset, int xysLen,
                             int[] indices, int indicesOffset, int indicesLen, int indexBase) {
      stableAttrs[0] = m00;
      stableAttrs[1] = m01;
      stableAttrs[2] = m10;
      stableAttrs[3] = m11;
      stableAttrs[4] = tx;
      stableAttrs[5] = ty;
      addExtraStableAttrs(stableAttrs, 6);

      int vertIdx = beginPrimitive(xysLen/2, indicesLen);
      int offset = vertices.position();
      float[] vertData = vertices.array();
      for (int ii = xysOffset, ll = ii+xysLen; ii < ll; ii += 2) {
        offset = addVert(vertData, offset, stableAttrs, xys[ii], xys[ii+1], sxys[ii], sxys[ii+1]);
      }
      vertices.skip(offset - vertices.position());

      addElems(vertIdx, indices, indicesOffset, indicesLen, indexBase);
    }

    @Override
    public String toString() {
      return "cq=" + (elements.capacity()/6);
    }

    /** Returns the size (in floats) of the stable attributes. If a custom shader adds additional
     * stable attributes, it should use this to determine the offset at which to bind them, and
     * override this method to return the new size including their attributes. */
    protected int stableAttrsSize() {
      return 8;
    }

    protected int vertexSize() {
      return stableAttrsSize() + 4;
    }

    protected int vertexStride() {
      return vertexSize() * FLOAT_SIZE_BYTES;
    }

    protected int addExtraStableAttrs(float[] buf, int sidx) {
      buf[sidx++] = arTint;
      buf[sidx++] = gbTint;
      return sidx;
    }

    protected int beginPrimitive(int vertexCount, int elemCount) {
      int vertIdx = vertices.position() / vertexSize();
      int verts = vertIdx + vertexCount, elems = elements.position() + elemCount;
      int availVerts = vertices.capacity() / vertexSize(), availElems = elements.capacity();
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

    protected final void addElems(int vertIdx, int[] indices, int indicesOffset, int indicesLen,
                                  int indexBase) {
      short[] data = elements.array();
      int offset = elements.position();
      for (int ii = indicesOffset, ll = ii+indicesLen; ii < ll; ii++) {
        data[offset++] = (short)(vertIdx+indices[ii]-indexBase);
      }
      elements.skip(offset - elements.position());
    }

    private void expandVerts(int vertCount) {
      int newVerts = vertices.capacity() / vertexSize();
      while (newVerts < vertCount)
        newVerts += EXPAND_VERTS;
      vertices.expand(newVerts*vertexSize());
    }

    private void expandElems(int elemCount) {
      int newElems = elements.capacity();
      while (newElems < elemCount)
        newElems += EXPAND_ELEMS;
      elements.expand(newElems);
    }
  }

  protected static int addVert(float[] data, int offset,
                               float[] prefix, float x, float y, float sx, float sy) {
    System.arraycopy(prefix, 0, data, offset, prefix.length);
    offset += prefix.length;
    data[offset++] = x;
    data[offset++] = y;
    data[offset++] = sx;
    data[offset++] = sy;
    return offset;
  }

  protected static final int[] QUAD_INDICES = { 0, 1, 2, 1, 3, 2 };
}
