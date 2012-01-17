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
 * Defines the interface to shaders used by the GL core.
 */
public interface GLShader
{
  /** Defines the interface to the texture shader. */
  interface Texture extends GLShader {
    void prepare(Object tex, float alpha);
  }

  /** Defines the interface to the color shader. */
  interface Color extends GLShader {
    void prepare(int color, float alpha);
  }

  /**
   * Begins a primitive with the specified vertex and element count.
   */
  int beginPrimitive(int vertexCount, int elemCount);

  /**
   * Adds a vertex to a primitive that was started with {@link #beginPrimitive}.
   */
  void buildVertex(InternalTransform local, float dx, float dy);

  /**
   * Adds a vertex to a primitive that was started with {@link #beginPrimitive}.
   */
  void buildVertex(InternalTransform local, float dx, float dy, float sx, float sy);

  /**
   * Adds an element to a primitive that was started with {@link #beginPrimitive}.
   */
  void addElement(int index);

  /**
   * Sends all accumulated vertex/element info to GL.
   */
  void flush();

  /** The GLSL code for the vertex shader. */
  String VERTEX_SHADER =
    "uniform vec2 u_ScreenSize;\n" +
    "attribute vec4 a_Matrix;\n" +
    "attribute vec2 a_Translation;\n" +
    "attribute vec2 a_Position;\n" +
    "attribute vec2 a_Texture;\n" +
    "varying vec2 v_TexCoord;\n" +

    "void main(void) {\n" +
    // Transform the vertex.
    "  mat3 transform = mat3(\n" +
    "    a_Matrix[0], a_Matrix[1], 0,\n" +
    "    a_Matrix[2], a_Matrix[3], 0,\n" +
    "    a_Translation[0], a_Translation[1], 1);\n" +
    "  gl_Position = vec4(transform * vec3(a_Position, 1.0), 1);\n" +
    // Scale from screen coordinates to [0, 2].
    "  gl_Position.x /= (u_ScreenSize.x / 2.0);\n" +
    "  gl_Position.y /= (u_ScreenSize.y / 2.0);\n" +
    // Offset to [-1, 1] and flip y axis to put origin at top-left.
    "  gl_Position.x -= 1.0;\n" +
    "  gl_Position.y = 1.0 - gl_Position.y;\n" +

    "  v_TexCoord = a_Texture;\n" +
    "}";

  /** The GLSL code for the texture fragment shader. */
  String TEX_FRAG_SHADER =
    "#ifdef GL_ES\n" +
    "precision highp float;\n" +
    "#endif\n" +

    "uniform sampler2D u_Texture;\n" +
    "varying vec2 v_TexCoord;\n" +
    "uniform float u_Alpha;\n" +

    "void main(void) {\n" +
    "  vec4 textureColor = texture2D(u_Texture, v_TexCoord);\n" +
    "  gl_FragColor = vec4(textureColor.rgb * u_Alpha, textureColor.a * u_Alpha);\n" +
    "}";

  /** The GLSL code for the color fragment shader. */
  String COLOR_FRAG_SHADER =
    "#ifdef GL_ES\n" +
    "precision highp float;\n" +
    "#endif\n" +

    "uniform vec4 u_Color;\n" +
    "uniform float u_Alpha;\n" +

    "void main(void) {\n" +
    "  gl_FragColor = vec4(u_Color.rgb * u_Alpha, u_Color.a * u_Alpha);\n" +
    "}";
}
