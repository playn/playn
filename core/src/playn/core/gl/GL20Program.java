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
  private final int program;

  public GL20Program(GLContext ctx, GL20 gl, String vertShader, String fragShader) {
    this.gl = gl;
    this.program = createProgram(ctx, vertShader, fragShader);
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

  protected int createProgram(GLContext ctx, String vertexSource, String fragmentSource) {
    int vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexSource);
    int fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSource);
    int program = gl.glCreateProgram();
    if (vertexShader == 0 || fragmentShader == 0 || program == 0)
      return 0;

    gl.glAttachShader(program, vertexShader);
    ctx.checkGLError("createProgram Attaching vertex shader");
    gl.glAttachShader(program, fragmentShader);
    ctx.checkGLError("createProgram Attaching fragment shader");
    gl.glLinkProgram(program);

    int[] linkStatus = new int[1];
    gl.glGetProgramiv(program, GL20.GL_LINK_STATUS, linkStatus, 0);
    if (linkStatus[0] != GL20.GL_TRUE) {
      log().error("Could not link program: ");
      log().error(gl.glGetProgramInfoLog(program));
      gl.glDeleteProgram(program);
      program = 0;
    }
    return program;
  }

  private int compileShader(int type, final String shaderSource) {
    int shader = gl.glCreateShader(type);
    if (shader == 0)
      return 0;
    gl.glShaderSource(shader, shaderSource);
    gl.glCompileShader(shader);

    int[] compiled = new int[1];
    gl.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, compiled, 0);
    if (compiled[0] == GL20.GL_FALSE) {
      log().error("Could not compile shader " + type + ":");
      log().error(gl.glGetShaderInfoLog(shader));
      gl.glDeleteShader(shader);
      shader = 0;
    }

    return shader;
  }
}
