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
package playn.core;

import static playn.core.GL20.*;

/**
 * A batch which renders quads by stuffing them into a big(ish) GLSL uniform variable. Turns out to
 * work pretty well for 2D rendering as we rarely render more than a modest number of quads before
 * flushing the shader and it allows us to avoid sending a lot of duplicated data as is necessary
 * when rendering quads via a batch of triangles.
 */
public class UniformQuadBatch extends QuadBatch {

  /** The source for the stock quad batch shader program. */
  public static class Source extends TexturedBatch.Source {

    /** Declares the uniform variables for our shader. */
    public static final String VERT_UNIFS =
      "uniform vec2 u_HScreenSize;\n" +
      "uniform float u_Flip;\n" +
      "uniform vec4 u_Data[_VEC4S_PER_QUAD_*_MAX_QUADS_];\n";

    /** Declares the attribute variables for our shader. */
    public static final String VERT_ATTRS =
      "attribute vec3 a_Vertex;\n";

    /** Declares the varying variables for our shader. */
    public static final String VERT_VARS =
      "varying vec2 v_TexCoord;\n" +
      "varying vec4 v_Color;\n";

    /** Extracts the values from our data buffer. */
    public static final String VERT_EXTRACTDATA =
      "int index = _VEC4S_PER_QUAD_*int(a_Vertex.z);\n" +
      "vec4 mat = u_Data[index+0];\n" +
      "vec4 txc = u_Data[index+1];\n" +
      "vec4 tcs = u_Data[index+2];\n";

    /** The shader code that computes {@code gl_Position}. */
    public static final String VERT_SETPOS =
      // Transform the vertex.
      "mat3 transform = mat3(\n" +
      "  mat.x, mat.y, 0,\n" +
      "  mat.z, mat.w, 0,\n" +
      "  txc.x, txc.y, 1);\n" +
      "gl_Position = vec4(transform * vec3(a_Vertex.xy, 1.0), 1.0);\n" +
      // Scale from screen coordinates to [0, 2].
      "gl_Position.xy /= u_HScreenSize.xy;\n" +
      // Offset to [-1, 1].
      "gl_Position.xy -= 1.0;\n" +
      // If requested, flip the y-axis.
      "gl_Position.y *= u_Flip;\n";

    /** The shader code that computes {@code v_TexCoord}. */
    public static final String VERT_SETTEX =
      "v_TexCoord = a_Vertex.xy * tcs.xy + txc.zw;\n";

    /** The shader code that computes {@code v_Color}. */
    public static final String VERT_SETCOLOR =
      // tint is encoded as two floats A*R and G*B where A, R, G, B are (0 - 255)
      "float red = mod(tcs.z, 256.0);\n" +
      "float alpha = (tcs.z - red) / 256.0;\n" +
      "float blue = mod(tcs.w, 256.0);\n" +
      "float green = (tcs.w - blue) / 256.0;\n" +
      "v_Color = vec4(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0);\n";

    /** Returns the source to the vertex shader program. */
    public String vertex (UniformQuadBatch batch) {
      return vertex().
        replace("_MAX_QUADS_", ""+batch.maxQuads).
        replace("_VEC4S_PER_QUAD_", ""+batch.vec4sPerQuad());
    }

    /** Returns the raw vertex source, which will have some parameters subbed into it. */
    protected String vertex () {
      return (VERT_UNIFS +
              VERT_ATTRS +
              VERT_VARS +
              "void main(void) {\n" +
              VERT_EXTRACTDATA +
              VERT_SETPOS +
              VERT_SETTEX +
              VERT_SETCOLOR +
              "}");
    }
  }

  /**
   * Returns false if the GL context doesn't support sufficient numbers of vertex uniform vectors
   * to allow this shader to run with good performance, true otherwise.
   */
  public static boolean isLikelyToPerform(GL20 gl) {
    int maxVecs = usableMaxUniformVectors(gl);
    // assume we're better off with indexed tris if we can't push at least 16 quads at a time
    return (maxVecs >= 16*BASE_VEC4S_PER_QUAD);
  }

  protected final int maxQuads;

  protected final GLProgram program;
  protected final int uTexture;
  protected final int uHScreenSize;
  protected final int uFlip;
  protected final int uData;
  protected final int aVertex;

  protected final int verticesId, elementsId;
  protected final float[] data;
  protected int quadCounter;

  /** Creates a uniform quad batch with the default shader programs. */
  public UniformQuadBatch (GL20 gl) {
    this(gl, new Source());
  }

  /** Creates a uniform quad batch with the supplied custom shader program builder. */
  public UniformQuadBatch (GL20 gl, Source source) {
    super(gl);
    int maxVecs = usableMaxUniformVectors(gl) - extraVec4s();
    if (maxVecs < vec4sPerQuad())
    throw new RuntimeException(
      "GL_MAX_VERTEX_UNIFORM_VECTORS too low: have " + maxVecs +
        ", need at least " + vec4sPerQuad());
    maxQuads = maxVecs / vec4sPerQuad();

    program = new GLProgram(gl, source.vertex(this), source.fragment());
    uTexture = program.getUniformLocation("u_Texture");
    uHScreenSize = program.getUniformLocation("u_HScreenSize");
    uFlip = program.getUniformLocation("u_Flip");
    uData = program.getUniformLocation("u_Data");
    aVertex = program.getAttribLocation("a_Vertex");

    // create our stock supply of unit quads and stuff them into our buffers
    short[] verts = new short[maxQuads*VERTICES_PER_QUAD*VERTEX_SIZE];
    short[] elems = new short[maxQuads*ELEMENTS_PER_QUAD];
    int vv = 0, ee = 0;
    for (short ii = 0; ii < maxQuads; ii++) {
      verts[vv++] = 0; verts[vv++] = 0; verts[vv++] = ii;
      verts[vv++] = 1; verts[vv++] = 0; verts[vv++] = ii;
      verts[vv++] = 0; verts[vv++] = 1; verts[vv++] = ii;
      verts[vv++] = 1; verts[vv++] = 1; verts[vv++] = ii;
      short base = (short)(ii * VERTICES_PER_QUAD);
      short base0 = base, base1 = ++base, base2 = ++base, base3 = ++base;
      elems[ee++] = base0; elems[ee++] = base1; elems[ee++] = base2;
      elems[ee++] = base1; elems[ee++] = base3; elems[ee++] = base2;
    }

    data = new float[maxQuads*vec4sPerQuad()*4];

    // create our GL buffers
    int[] ids = new int[2];
    gl.glGenBuffers(2, ids, 0);
    verticesId = ids[0]; elementsId = ids[1];

    gl.glBindBuffer(GL_ARRAY_BUFFER, verticesId);
    gl.bufs.setShortBuffer(verts, 0, verts.length);
    gl.glBufferData(GL_ARRAY_BUFFER, verts.length*2, gl.bufs.shortBuffer, GL_STATIC_DRAW);

    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementsId);
    gl.bufs.setShortBuffer(elems, 0, elems.length);
    gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elems.length*2, gl.bufs.shortBuffer, GL_STATIC_DRAW);

    gl.checkError("UniformQuadBatch end ctor");
  }

  @Override public void addQuad (int tint,
                                 float m00, float m01, float m10, float m11, float tx, float ty,
                                 float x1, float y1, float sx1, float sy1,
                                 float x2, float y2, float sx2, float sy2,
                                 float x3, float y3, float sx3, float sy3,
                                 float x4, float y4, float sx4, float sy4) {
    int pos = quadCounter * vec4sPerQuad()*4;
    float dw = x2 - x1, dh = y3 - y1;
    data[pos++] = m00*dw;
    data[pos++] = m01*dw;
    data[pos++] = m10*dh;
    data[pos++] = m11*dh;
    data[pos++] = tx + m00*x1 + m10*y1;
    data[pos++] = ty + m01*x1 + m11*y1;
    data[pos++] = sx1;
    data[pos++] = sy1;
    data[pos++] = sx2 - sx1;
    data[pos++] = sy3 - sy1;
    data[pos++] = (tint >> 16) & 0xFFFF;
    data[pos++] = tint & 0xFFFF;
    pos = addExtraQuadData(data, pos);
    quadCounter++;

    if (quadCounter >= maxQuads) flush();
  }

  @Override public void begin (float fbufWidth, float fbufHeight, boolean flip) {
    super.begin(fbufWidth, fbufHeight, flip);
    program.activate();
    // TODO: apparently we can avoid glUniform calls because they're part of the program state; so
    // we can cache the last set values for all these glUniform calls and only set them anew if
    // they differ...
    gl.glUniform2f(uHScreenSize, fbufWidth/2f, fbufHeight/2f);
    gl.glUniform1f(uFlip, flip ? -1 : 1);
    gl.glBindBuffer(GL_ARRAY_BUFFER, verticesId);
    gl.glEnableVertexAttribArray(aVertex);
    gl.glVertexAttribPointer(aVertex, VERTEX_SIZE, GL_SHORT, false, 0, 0);
    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementsId);
    gl.glActiveTexture(GL_TEXTURE0);
    gl.glUniform1i(uTexture, 0);
    gl.checkError("UniformQuadBatch begin");
  }

  @Override public void flush () {
    super.flush();
    if (quadCounter > 0) {
      bindTexture();
      gl.glUniform4fv(uData, quadCounter * vec4sPerQuad(), data, 0);
      gl.glDrawElements(GL_TRIANGLES, quadCounter*ELEMENTS_PER_QUAD, GL_UNSIGNED_SHORT, 0);
      gl.checkError("UniformQuadBatch flush");
      quadCounter = 0;
    }
  }

  @Override public void end () {
    super.end();
    gl.glDisableVertexAttribArray(aVertex);
    gl.checkError("UniformQuadBatch end");
  }

  @Override public void close () {
    super.close();
    program.close();
    gl.glDeleteBuffers(2, new int[] { verticesId, elementsId }, 0);
    gl.checkError("UniformQuadBatch close");
  }

  @Override public String toString () {
    return "uquad/" + maxQuads;
  }

  protected int vec4sPerQuad () {
    return BASE_VEC4S_PER_QUAD;
  }

  /** Returns how many vec4s this shader uses above and beyond those in the base implementation. If
    * you add any extra attributes or uniforms, your subclass will need to account for them here. */
  protected int extraVec4s () {
    return 0;
  }

  protected int addExtraQuadData (float[] data, int pos) {
    return pos;
  }

  private static int usableMaxUniformVectors (GL20 gl) {
    // this returns the maximum number of vec4s; then we subtract one vec2 to account for the
    // uHScreenSize uniform, and two more because some GPUs seem to need one for our vec3 attr
    int maxVecs = gl.glGetInteger(GL_MAX_VERTEX_UNIFORM_VECTORS) - 3;
    // we have to check errors always in this case, because if GL failed to return a value we would
    // otherwise return the value of uninitialized memory which could be some huge number which we
    // might turn around and try to compile into a shader causing GL to crash (you might think from
    // such a careful description that such a thing has in fact come to pass, and you would not be
    // incorrect)
    int glErr = gl.glGetError();
    if (glErr != GL20.GL_NO_ERROR) throw new RuntimeException(
      "Unable to query GL_MAX_VERTEX_UNIFORM_VECTORS,  error " + glErr);
    return maxVecs;
  }

  private static final int VERTICES_PER_QUAD = 4;
  private static final int ELEMENTS_PER_QUAD = 6;
  private static final int VERTEX_SIZE = 3; // 3 floats per vertex
  private static final int BASE_VEC4S_PER_QUAD = 3; // 3 vec4s per matrix
}
