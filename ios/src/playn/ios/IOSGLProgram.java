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
package playn.ios;

import cli.System.IntPtr;

import cli.OpenTK.Graphics.ES20.All;
import cli.OpenTK.Graphics.ES20.GL;
import cli.OpenTK.Graphics.ES20.ProgramParameter;
import cli.OpenTK.Graphics.ES20.ShaderParameter;
import cli.OpenTK.Graphics.ES20.ShaderType;
import cli.OpenTK.Graphics.ES20.VertexAttribPointerType;

import playn.core.gl.GLBuffer;
import playn.core.gl.GLProgram;
import playn.core.gl.GLShader;

public class IOSGLProgram implements GLProgram {

  private final int program, vertexShader, fragmentShader;

  public IOSGLProgram(IOSGLContext ctx, String vertexSource, String fragmentSource) {
    int program = 0, vertexShader = 0, fragmentShader = 0;
    try {
      program = GL.CreateProgram();
      if (program == 0)
        throw new RuntimeException("Failed to create program: " + GL.GetError());

      vertexShader = compileShader(ShaderType.wrap(ShaderType.VertexShader), vertexSource);
      GL.AttachShader(program, vertexShader);
      ctx.checkGLError("Attached vertex shader");

      fragmentShader = compileShader(ShaderType.wrap(ShaderType.FragmentShader), fragmentSource);
      GL.AttachShader(program, fragmentShader);
      ctx.checkGLError("Attached fragment shader");

      GL.LinkProgram(program);
      int[] linkStatus = new int[1];
      GL.GetProgram(program, ProgramParameter.wrap(ProgramParameter.LinkStatus), linkStatus);
      if (linkStatus[0] != All.True) {
        int[] llength = new int[1];
        GL.GetProgram(program, ProgramParameter.wrap(ProgramParameter.InfoLogLength), llength);
        cli.System.Text.StringBuilder log = new cli.System.Text.StringBuilder(llength[0]);
        GL.GetProgramInfoLog(program, llength[0], llength, log);
        throw new RuntimeException("Failed to link program: " + log.ToString());
      }

      this.program = program;
      this.vertexShader = vertexShader;
      this.fragmentShader = fragmentShader;
      program = vertexShader = fragmentShader = 0;

    } finally {
      if (program != 0)
        GL.DeleteProgram(program);
      if (vertexShader != 0)
        GL.DeleteShader(vertexShader);
      if (fragmentShader != 0)
        GL.DeleteShader(fragmentShader);
    }
  }

  @Override
  public GLShader.Uniform1f getUniform1f(String name) {
    final int loc = GL.GetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform1f() {
      public void bind(float a) {
        GL.Uniform1(loc, a);
      }
    };
  }
  @Override
  public GLShader.Uniform2f getUniform2f(String name) {
    final int loc = GL.GetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform2f() {
      public void bind(float a, float b) {
        GL.Uniform2(loc, a, b);
      }
    };
  }
  @Override
  public GLShader.Uniform3f getUniform3f(String name) {
    final int loc = GL.GetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform3f() {
      public void bind(float a, float b, float c) {
        GL.Uniform3(loc, a, b, c);
      }
    };
  }
  @Override
  public GLShader.Uniform4f getUniform4f(String name) {
    final int loc = GL.GetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform4f() {
      public void bind(float a, float b, float c, float d) {
        GL.Uniform4(loc, a, b, c, d);
      }
    };
  }
  @Override
  public GLShader.Uniform1i getUniform1i(String name) {
    final int loc = GL.GetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform1i() {
      public void bind(int a) {
        GL.Uniform1(loc, a);
      }
    };
  }
  @Override
  public GLShader.Uniform2i getUniform2i(String name) {
    final int loc = GL.GetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform2i() {
      public void bind(int a, int b) {
        GL.Uniform2(loc, a, b);
      }
    };
  }

  @Override
  public GLShader.Uniform2fv getUniform2fv(String name) {
    final int loc = GL.GetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform2fv() {
      public void bind(GLBuffer.Float data, int count) {
        IOSGLBuffer.FloatImpl idata = (IOSGLBuffer.FloatImpl) data;
        idata.position = 0;
        GL.Uniform2(loc, count, idata.data);
      }
    };
  }

  @Override
  public GLShader.Uniform4fv getUniform4fv(String name) {
    final int loc = GL.GetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.Uniform4fv() {
      public void bind(GLBuffer.Float data, int count) {
        IOSGLBuffer.FloatImpl idata = (IOSGLBuffer.FloatImpl) data;
        idata.position = 0;
        GL.Uniform4(loc, count, idata.data);
      }
    };
  }

  @Override
  public GLShader.UniformMatrix4fv getUniformMatrix4fv(String name) {
    final int loc = GL.GetUniformLocation(program, name);
    return (loc < 0) ? null : new GLShader.UniformMatrix4fv() {
      public void bind(GLBuffer.Float data, int count) {
        IOSGLBuffer.FloatImpl idata = (IOSGLBuffer.FloatImpl) data;
        idata.position = 0;
        GL.UniformMatrix4(loc, count, false, idata.data);
      }
    };
  }

  @Override
  public GLShader.Attrib getAttrib(String name, final int size, final int type) {
    final int loc = GL.GetAttribLocation(program, name);
    return (loc < 0) ? null : new GLShader.Attrib() {
      public void bind(int stride, int offset) {
        GL.EnableVertexAttribArray(loc);
        GL.VertexAttribPointer(
          loc, size, VertexAttribPointerType.wrap(type), false, stride, new IntPtr(offset));
      }

      public void unbind() {
        GL.DisableVertexAttribArray(loc);
      }
    };
  }

  @Override
  public void bind() {
    GL.UseProgram(program);
  }

  @Override
  public void destroy() {
    GL.DeleteShader(vertexShader);
    GL.DeleteShader(fragmentShader);
    GL.DeleteProgram(program);
  }

  private int compileShader(ShaderType type, final String shaderSource) {
    int shader = GL.CreateShader(type);
    if (shader == 0)
      throw new RuntimeException("Failed to create shader: " + GL.GetError());
    GL.ShaderSource(shader, 1, new String[] { shaderSource }, null);
    GL.CompileShader(shader);
    int[] compiled = new int[1];
    GL.GetShader(shader, ShaderParameter.wrap(ShaderParameter.CompileStatus), compiled);
    if (compiled[0] == All.False) {
      int[] llength = new int[1];
      GL.GetShader(shader, ShaderParameter.wrap(ShaderParameter.InfoLogLength), llength);
      cli.System.Text.StringBuilder log = new cli.System.Text.StringBuilder(llength[0]);
      GL.GetShaderInfoLog(shader, llength[0], llength, log);
      GL.DeleteShader(shader);
      throw new RuntimeException("Failed to compile shader (" + type + "): " + log.ToString());
    }
    return shader;
  }
}
