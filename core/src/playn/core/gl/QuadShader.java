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

import playn.core.gl.GLShader;
import static playn.core.gl.GL20.*;

/**
 * A {@link GLShader} implementation that only handles quads.
 */
public class QuadShader extends GLShader {

  /** The GLSL code for quad-specific vertex shader. */
  public static final String VERTEX_SHADER =
    "uniform vec2 u_ScreenSize;\n" +
    "uniform vec4 u_Data[3*_MAX_QUADS_];\n" +
    "attribute vec3 a_Vertex;\n" +
    "varying vec2 v_TexCoord;\n" +

    "void main(void) {\n" +
    // Extract the transform &c data for this quad.
    "int index = 3*int(a_Vertex.z);\n" +
    "vec4 mat = u_Data[index+0];\n" +
    "vec4 txc = u_Data[index+1];\n" +
    "vec4 tcs = u_Data[index+2];\n" +

    // Transform the vertex.
    "mat3 transform = mat3(\n" +
    "  mat.x, mat.y, 0,\n" +
    "  mat.z, mat.w, 0,\n" +
    "  txc.x, txc.y, 1);\n" +
    "gl_Position = vec4(transform * vec3(a_Vertex.xy, 1), 1);\n" +

    // Scale from screen coordinates to [0, 2].
    "gl_Position.x /= u_ScreenSize.x;\n" +
    "gl_Position.y /= u_ScreenSize.y;\n" +

    // Offset to [-1, 1] and flip y axis to put origin at top-left.
    "gl_Position.x -= 1.0;\n" +
    "gl_Position.y = 1.0 - gl_Position.y;\n" +

    // Compute our texture coordinate.
    "v_TexCoord = a_Vertex.xy * vec2(tcs.x, tcs.y) + vec2(txc.z, txc.w);\n" +
    "}";

  private static final int VERTICES_PER_QUAD = 4;
  private static final int ELEMENTS_PER_QUAD = 6;
  private static final int VERTEX_SIZE = 3; // 3 floats per vertex
  private static final int VEC4S_PER_QUAD = 3; // 3 vec4s per matrix

  private final int maxQuads;

  public QuadShader(GLContext ctx) {
    super(ctx);

    // this returns the maximum number of vec4s; then we subtract one vec2 to account for the
    // uScreenSize uniform
    int maxVecs = ctx.getInteger(GL20.GL_MAX_VERTEX_UNIFORM_VECTORS) - 1;
    if (maxVecs < VEC4S_PER_QUAD)
      throw new RuntimeException(
        "GL_MAX_VERTEX_UNIFORM_VECTORS too low: have " + maxVecs +
        ", need at least " + VEC4S_PER_QUAD);
    this.maxQuads = maxVecs / VEC4S_PER_QUAD;
  }

  /**
   * Returns the vertex shader program. Note that this program <em>must</em> preserve the use of
   * the existing attributes and uniforms. You can add new uniforms and attributes, but you cannot
   * remove or change the defaults.
   */
  protected String vertexShader() {
    return VERTEX_SHADER.replace("_MAX_QUADS_", ""+maxQuads);
  }

  @Override
  protected Core createTextureCore() {
    return new QuadCore(vertexShader(), textureFragmentShader());
  }

  @Override
  protected Core createColorCore() {
    return new QuadCore(vertexShader(), colorFragmentShader());
  }

  protected class QuadCore extends Core {
    private final Uniform2f uScreenSize;
    private final Uniform4fv uData;
    private final Attrib aVertices;
    private final GLBuffer.Float verts, data;
    private final GLBuffer.Short elems;
    private int quadCounter;

    public QuadCore(String vertShader, String fragShader) {
      super(vertShader, fragShader);

      data = ctx.createFloatBuffer(maxQuads*VEC4S_PER_QUAD*4);

      // compile the shader and get our uniform and attribute
      uScreenSize = prog.getUniform2f("u_ScreenSize");
      uData = prog.getUniform4fv("u_Data");
      aVertices = prog.getAttrib("a_Vertex", VERTEX_SIZE, GL_FLOAT);

      // create our stock supply of unit quads and stuff them into our buffers
      verts = ctx.createFloatBuffer(maxQuads*VERTICES_PER_QUAD*VERTEX_SIZE);
      elems = ctx.createShortBuffer(maxQuads*ELEMENTS_PER_QUAD);

      float[] tv1 = {0.0f, 0.0f}, tv2 = {1.0f, 0.0f}, tv3 = {0.0f, 1.0f}, tv4 = {1.0f, 1.0f};
      for (int ii = 0; ii < maxQuads; ii++) {
        verts.add(0, 0).add(ii);
        verts.add(1, 0).add(ii);
        verts.add(0, 1).add(ii);
        verts.add(1, 1).add(ii);
        int base = ii * VERTICES_PER_QUAD;
        elems.add(base+0).add(base+1).add(base+2);
        elems.add(base+1).add(base+3).add(base+2);
      }

      verts.bind(GL_ARRAY_BUFFER);
      verts.send(GL_ARRAY_BUFFER, GL_STATIC_DRAW);
      elems.bind(GL_ELEMENT_ARRAY_BUFFER);
      elems.send(GL_ELEMENT_ARRAY_BUFFER, GL_STATIC_DRAW);
    }

    @Override
    public void prepare(int fbufWidth, int fbufHeight) {
      prog.bind();
      uScreenSize.bind(fbufWidth/2f, fbufHeight/2f);
      verts.bind(GL_ARRAY_BUFFER);
      aVertices.bind(0, 0);
      elems.bind(GL_ELEMENT_ARRAY_BUFFER);
    }

    @Override
    public void flush() {
      if (quadCounter == 0)
        return;
      uData.bind(data, quadCounter * VEC4S_PER_QUAD);
      elems.drawElements(GL_TRIANGLES, ELEMENTS_PER_QUAD*quadCounter);
      quadCounter = 0;
    }

    @Override
    public void destroy() {
      super.destroy();
      verts.destroy();
      elems.destroy();
      data.destroy();
    }

    @Override
    public void addQuad(float m00, float m01, float m10, float m11, float tx, float ty,
                        float x1, float y1, float sx1, float sy1,
                        float x2, float y2, float sx2, float sy2,
                        float x3, float y3, float sx3, float sy3,
                        float x4, float y4, float sx4, float sy4) {
      float dw = x2 - x1, dh = y3 - y1;
      data.add(m00*dw, m01*dw, m10*dh, m11*dh, tx + m00*x1 + m10*y1, ty + m01*x1 + m11*y1);
      data.add(sx1, sy1);
      data.add(sx2 - sx1, sy3 - sy1);
      data.skip(2);
      quadCounter++;

      if (quadCounter >= maxQuads)
        QuadShader.this.flush();
    }
  }
}
