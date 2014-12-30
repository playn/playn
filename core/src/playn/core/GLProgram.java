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
package playn.core;

/**
 * Encapsulates a GL vertex and fragment shader program pair.
 */
public class GLProgram {

  private final GL20 gl;
  private final int vertexShader, fragmentShader;
  protected final int program;

  /**
   * Compiles and links the shader program described by {@code vertexSource} and
   * {@code fragmentSource}.
   * @throws RuntimeException if the program fails to compile or link.
   */
  public GLProgram (GL20 gl, String vertexSource, String fragmentSource) {
    this.gl = gl;

    int program = 0, vertexShader = 0, fragmentShader = 0;
    try {
      program = gl.glCreateProgram();
      if (program == 0) {
        throw new RuntimeException("Failed to create program: " + gl.glGetError());
      }
      gl.checkError("glCreateProgram");

      vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexSource);
      gl.glAttachShader(program, vertexShader);
      gl.checkError("glAttachShader / vertex");

      fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSource);
      gl.glAttachShader(program, fragmentShader);
      gl.checkError("glAttachShader / fragment");

      gl.glLinkProgram(program);
      int[] linkStatus = new int[1];
      gl.glGetProgramiv(program, GL20.GL_LINK_STATUS, linkStatus, 0);
      if (linkStatus[0] == GL20.GL_FALSE) {
        String log = gl.glGetProgramInfoLog(program);
        gl.glDeleteProgram(program);
        throw new RuntimeException("Failed to link program: " + log);
      }

      this.program = program;
      this.vertexShader = vertexShader;
      this.fragmentShader = fragmentShader;
      program = vertexShader = fragmentShader = 0;

    } finally {
      if (program != 0) gl.glDeleteProgram(program);
      if (vertexShader != 0) gl.glDeleteShader(vertexShader);
      if (fragmentShader != 0) gl.glDeleteShader(fragmentShader);
    }
  }

  /** Binds this shader program, in preparation for rendering. */
  public void activate () {
    gl.glUseProgram(program);
  }

  /** Frees this program and associated compiled shaders. The program must not be used after
   * destruction. */
  public void destroy () {
    gl.glDeleteShader(vertexShader);
    gl.glDeleteShader(fragmentShader);
    gl.glDeleteProgram(program);
  }

  private int compileShader(int type, final String shaderSource) {
    int shader = gl.glCreateShader(type);
    if (shader == 0) throw new RuntimeException(
      "Failed to create shader (" + type + "): " + gl.glGetError());
    gl.glShaderSource(shader, shaderSource);
    gl.glCompileShader(shader);
    int[] compiled = new int[1];
    gl.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, compiled, 0);
    if (compiled[0] == GL20.GL_FALSE) {
      String log = gl.glGetShaderInfoLog(shader);
      gl.glDeleteShader(shader);
      throw new RuntimeException("Failed to compile shader (" + type + "): " + log);
    }
    return shader;
  }
}
