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
 * A {@link GLShader} implementation that only handles quads.
 */
public class QuadShader extends GLShader {

  protected final int maxQuads;

  /**
   * Returns false if the GL context doesn't support sufficient numbers of vertex uniform vectors
   * to allow this shader to run with good performance, true otherwise.
   */
  public static boolean isLikelyToPerform(GLContext ctx) {
    int maxVecs = usableMaxUniformVectors(ctx);
    // assume we're better off with indexed tris if we can't push at least 16 quads at a time
    return (maxVecs >= 16*BASE_VEC4S_PER_QUAD);
  }

  private static int usableMaxUniformVectors(GLContext ctx) {
    // this returns the maximum number of vec4s; then we subtract one vec2 to account for the
    // uScreenSize uniform, and two more because some GPUs seem to need one for our vec3 attr
    return ctx.getInteger(GL20.GL_MAX_VERTEX_UNIFORM_VECTORS) - 3;
  }

  public QuadShader(GLContext ctx) {
    super(ctx);

    int maxVecs = usableMaxUniformVectors(ctx) - extraVec4s();
    if (maxVecs < vec4sPerQuad())
      throw new RuntimeException(
        "GL_MAX_VERTEX_UNIFORM_VECTORS too low: have " + maxVecs +
        ", need at least " + vec4sPerQuad());
    maxQuads = maxVecs / vec4sPerQuad();
  }

  @Override
  public String toString() {
    return "quad/" + maxQuads;
  }

  protected int vec4sPerQuad() {
    return BASE_VEC4S_PER_QUAD;
  }

  /**
   * Returns how many vec4s this shader uses above and beyond those in the base implementation.
   * If you add any extra attributes or uniforms, your subclass will need to account for them
   * here.
   */
  protected int extraVec4s() {
    return 0;
  }

  /**
   * Returns the vertex shader program. Note that this program <em>must</em> preserve the use of
   * the existing attributes and uniforms. You can add new uniforms and attributes, but you cannot
   * remove or change the defaults.
   */
  protected String vertexShader() {
    return baseVertexShader().replace("_MAX_QUADS_", ""+maxQuads).
      replace("_VEC4S_PER_QUAD_", ""+vec4sPerQuad());
  }

  /**
   * Returns the vertex shader program with placeholders for a few key constants.
   */
  protected String baseVertexShader() {
    return VERTEX_SHADER;
  }

  @Override
  protected Core createTextureCore() {
    return new QuadCore(vertexShader(), textureFragmentShader());
  }

  protected class QuadCore extends Core {

    public QuadCore(String vertShader, String fragShader) {
      super(vertShader, fragShader);

      // compile the shader and get our uniform and attribute
      uScreenSize = prog.getUniform2f("u_ScreenSize");
      uData = prog.getUniform4fv("u_Data");
      aVertex = prog.getAttrib("a_Vertex", VERTEX_SIZE, GL20.GL_SHORT);

      // create our stock supply of unit quads and stuff them into our buffers
      vertices = ctx.createShortBuffer(maxQuads*VERTICES_PER_QUAD*VERTEX_SIZE);
      elements = ctx.createShortBuffer(maxQuads*ELEMENTS_PER_QUAD);

      for (int ii = 0; ii < maxQuads; ii++) {
        vertices.add(0, 0).add(ii);
        vertices.add(1, 0).add(ii);
        vertices.add(0, 1).add(ii);
        vertices.add(1, 1).add(ii);
        int base = ii * VERTICES_PER_QUAD;
        elements.add(base+0).add(base+1).add(base+2);
        elements.add(base+1).add(base+3).add(base+2);
      }

      // create the buffer that will hold quad data, and the float array that we'll use to avoid
      // making too many calls to FloatBuffer.put() which has crap performance on Android
      data = ctx.createFloatBuffer(maxQuads*vec4sPerQuad()*4);

      vertices.bind(GL20.GL_ARRAY_BUFFER);
      vertices.send(GL20.GL_ARRAY_BUFFER, GL20.GL_STATIC_DRAW);
      elements.bind(GL20.GL_ELEMENT_ARRAY_BUFFER);
      elements.send(GL20.GL_ELEMENT_ARRAY_BUFFER, GL20.GL_STATIC_DRAW);
    }

    @Override
    public void destroy() {
      super.destroy();
    }

    @Override
    public void addQuad(float m00, float m01, float m10, float m11, float tx, float ty,
                        float x1, float y1, float sx1, float sy1,
                        float x2, float y2, float sx2, float sy2,
                        float x3, float y3, float sx3, float sy3,
                        float x4, float y4, float sx4, float sy4) {
      float dw = x2 - x1, dh = y3 - y1;
      float[] quadData = data.array();
      int opos = data.position(), pos = opos;
      quadData[pos++] = m00*dw;
      quadData[pos++] = m01*dw;
      quadData[pos++] = m10*dh;
      quadData[pos++] = m11*dh;
      quadData[pos++] = tx + m00*x1 + m10*y1;
      quadData[pos++] = ty + m01*x1 + m11*y1;
      quadData[pos++] = sx1;
      quadData[pos++] = sy1;
      quadData[pos++] = sx2 - sx1;
      quadData[pos++] = sy3 - sy1;
      pos = addExtraData(quadData, pos);
      data.skip(pos-opos);
      quadCounter++;

      if (quadCounter >= maxQuads)
        QuadShader.this.flush();
    }

    protected int addExtraData(float[] quadData, int pos) {
      quadData[pos++] = arTint;
      quadData[pos++] = gbTint;
      return pos;
    }
  }
}
