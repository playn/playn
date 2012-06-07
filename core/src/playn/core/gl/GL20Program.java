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

import static playn.core.PlayN.log;

/**
 * A shader program implementation built on the {@link GL20} API.
 */
public class GL20Program implements GLProgram {

  private final GL20 gl;
  private final int vertexShader, fragmentShader, program;

  public GL20Program(GLContext ctx, GL20 gl, String vertexSource, String fragmentSource) {
    this.gl = gl;

    int program = 0, vertexShader = 0, fragmentShader = 0;
    try {
      program = gl.glCreateProgram();
      if (program == 0) {
        throw new RuntimeException("Failed to create program: " + gl.glGetError());
      }

      vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexSource);
      gl.glAttachShader(program, vertexShader);
      ctx.checkGLError("Attached vertex shader");

      fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSource);
      gl.glAttachShader(program, fragmentShader);
      ctx.checkGLError("Attached fragment shader");

      gl.glLinkProgram(program);
      int[] linkStatus = new int[1];
      gl.glGetProgramiv(program, GL20.GL_LINK_STATUS, linkStatus, 0);
      if (linkStatus[0] == GL20.GL_FALSE) {
        gl.glDeleteProgram(program);
        throw new RuntimeException("Failed to link program: " + gl.glGetProgramInfoLog(program));
      }

      this.program = program;
      this.vertexShader = vertexShader;
      this.fragmentShader = fragmentShader;
      program = vertexShader = fragmentShader = 0;

    } finally {
      if (program != 0)
        gl.glDeleteProgram(program);
      if (vertexShader != 0)
        gl.glDeleteShader(vertexShader);
      if (fragmentShader != 0)
        gl.glDeleteShader(program);
    }
  }

  @Override
  public GLShader.Uniform1f getUniform1f(String name) {
    final int loc = gl.glGetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform1f() {
      public void bind(float a) {
        gl.glUniform1f(loc, a);
      }
    };
  }
  @Override
  public GLShader.Uniform2f getUniform2f(String name) {
    final int loc = gl.glGetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform2f() {
      public void bind(float a, float b) {
        gl.glUniform2f(loc, a, b);
      }
    };
  }
  @Override
  public GLShader.Uniform3f getUniform3f(String name) {
    final int loc = gl.glGetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform3f() {
      public void bind(float a, float b, float c) {
        gl.glUniform3f(loc, a, b, c);
      }
    };
  }
  @Override
  public GLShader.Uniform4f getUniform4f(String name) {
    final int loc = gl.glGetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform4f() {
      public void bind(float a, float b, float c, float d) {
        gl.glUniform4f(loc, a, b, c, d);
      }
    };
  }
  @Override
  public GLShader.Uniform1i getUniform1i(String name) {
    final int loc = gl.glGetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform1i() {
      public void bind(int a) {
        gl.glUniform1i(loc, a);
      }
    };
  }
  @Override
  public GLShader.Uniform2i getUniform2i(String name) {
    final int loc = gl.glGetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform2i() {
      public void bind(int a, int b) {
        gl.glUniform2i(loc, a, b);
      }
    };
  }

  @Override
  public GLShader.Attrib getAttrib(String name, final int size, final int type) {
    final int loc = gl.glGetAttribLocation(program, name);
    return (loc < 0) ? null : new GLShader.Attrib() {
      public void bind(int stride, int offset, GLBuffer.Float data) {
        gl.glEnableVertexAttribArray(loc);
        gl.glVertexAttribPointer(loc, size, type, false, stride, offset);
      }
    };
  }

  @Override
  public void bind() {
    gl.glUseProgram(program);
  }

  @Override
  public void destroy() {
    gl.glDeleteShader(vertexShader);
    gl.glDeleteShader(fragmentShader);
    gl.glDeleteProgram(program);
  }

  private int compileShader(int type, final String shaderSource) {
    int shader = gl.glCreateShader(type);
    if (shader == 0)
      throw new RuntimeException("Failed to create shader (" + type + "): " + gl.glGetError());
    gl.glShaderSource(shader, shaderSource);
    gl.glCompileShader(shader);
    int[] compiled = new int[1];
    gl.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, compiled, 0);
    if (compiled[0] == GL20.GL_FALSE) {
      gl.glDeleteShader(shader);
      throw new RuntimeException("Failed to compile shader (" + type + "): " +
                                 gl.glGetShaderInfoLog(shader));
    }
    return shader;
  }
}
