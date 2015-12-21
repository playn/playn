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

import react.Closeable;

/**
 * Encapsulates a GL vertex and fragment shader program pair.
 */
public class GLProgram implements Closeable {

  private final GL20 gl;
  private final int vertexShader, fragmentShader;

  /** The GL id of this shader program. */
  public final int id;

  /**
   * Compiles and links the shader program described by {@code vertexSource} and
   * {@code fragmentSource}.
   * @throws RuntimeException if the program fails to compile or link.
   */
  public GLProgram (GL20 gl, String vertexSource, String fragmentSource) {
    this.gl = gl;

    int id = 0, vertexShader = 0, fragmentShader = 0;
    try {
      id = gl.glCreateProgram();
      if (id == 0) throw new RuntimeException("Failed to create program: " + gl.glGetError());
      gl.checkError("glCreateProgram");

      vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexSource);
      gl.glAttachShader(id, vertexShader);
      gl.checkError("glAttachShader / vertex");

      fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSource);
      gl.glAttachShader(id, fragmentShader);
      gl.checkError("glAttachShader / fragment");

      gl.glLinkProgram(id);
      int[] linkStatus = new int[1];
      gl.glGetProgramiv(id, GL20.GL_LINK_STATUS, linkStatus, 0);
      if (linkStatus[0] == GL20.GL_FALSE) {
        String log = gl.glGetProgramInfoLog(id);
        gl.glDeleteProgram(id);
        throw new RuntimeException("Failed to link program: " + log);
      }

      this.id = id;
      this.vertexShader = vertexShader;
      this.fragmentShader = fragmentShader;
      id = vertexShader = fragmentShader = 0;

    } finally {
      if (id != 0) gl.glDeleteProgram(id);
      if (vertexShader != 0) gl.glDeleteShader(vertexShader);
      if (fragmentShader != 0) gl.glDeleteShader(fragmentShader);
    }
  }

  /**
   * Returns the uniform location with the specified {@code name}.
   */
  public int getUniformLocation (String name) {
    int loc = gl.glGetUniformLocation(id, name);
    assert loc >= 0 : "Failed to get " + name + " uniform";
    return loc;
  }

  /**
   * Returns the attribute location with the specified {@code name}.
   */
  public int getAttribLocation (String name) {
    int loc = gl.glGetAttribLocation(id, name);
    assert loc >= 0 : "Failed to get " + name + " uniform";
    return loc;
  }

  /** Binds this shader program, in preparation for rendering. */
  public void activate () {
    gl.glUseProgram(id);
  }

  /** Frees this program and associated compiled shaders.
    * The program must not be used after closure. */
  @Override public void close () {
    gl.glDeleteShader(vertexShader);
    gl.glDeleteShader(fragmentShader);
    gl.glDeleteProgram(id);
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
