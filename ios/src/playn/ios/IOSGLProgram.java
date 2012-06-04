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

import playn.core.gl.GLBuffer;
import playn.core.gl.GLProgram;
import playn.core.gl.GLShader;
import static playn.core.PlayN.log;

public class IOSGLProgram implements GLProgram {

  private final int program;

  public IOSGLProgram(IOSGLContext ctx, String vertShader, String fragShader) {
    this.program = createProgram(ctx, vertShader, fragShader);
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
  public GLShader.Attrib getAttrib(String name, final int size, final int type) {
    final int loc = GL.GetAttribLocation(program, name);
    return (loc < 0) ? null : new GLShader.Attrib() {
      public void bind(int stride, int offset, GLBuffer.Float data) {
        GL.EnableVertexAttribArray(loc);
        IOSGLBuffer.FloatImpl idata = (IOSGLBuffer.FloatImpl) data;
        IntPtr daddr = new IntPtr(idata.pointer().ToInt64() + offset);
        GL.VertexAttribPointer(loc, size, All.wrap(type), false, stride, daddr);
      }
    };
  }

  // IntPtr vaddr = vertexHandle.AddrOfPinnedObject();
  // long baseVAddr = vaddr.ToInt64();
  // GL.VertexAttribPointer(aMatrix, 4, All.wrap(All.Float), false, VERTEX_STRIDE, vaddr);
  // vaddr = new IntPtr(baseVAddr + 16);
  // GL.VertexAttribPointer(aTranslation, 2, All.wrap(All.Float), false, VERTEX_STRIDE, vaddr);
  // vaddr = new IntPtr(baseVAddr + 24);
  // GL.VertexAttribPointer(aPosition, 2, All.wrap(All.Float), false, VERTEX_STRIDE, vaddr);
  // if (aTexture != -1) {
  //   vaddr = new IntPtr(baseVAddr + 32);
  //   GL.VertexAttribPointer(aTexture, 2, All.wrap(All.Float), false, VERTEX_STRIDE, vaddr);
  // }

  @Override
  public void bind() {
    GL.UseProgram(program);
  }

  protected int createProgram(IOSGLContext ctx, String vertexSource, String fragmentSource) {
    int vertexShader = loadShader(All.wrap(All.VertexShader), vertexSource);
    int fragmentShader = loadShader(All.wrap(All.FragmentShader), fragmentSource);
    int program = GL.CreateProgram();
    if (program == 0)
      throw new RuntimeException("Unable to create GL program: " + GL.GetError());

    GL.AttachShader(program, vertexShader);
    ctx.checkGLError("createProgram Attaching vertex shader");
    GL.AttachShader(program, fragmentShader);
    ctx.checkGLError("createProgram Attaching fragment shader");
    GL.LinkProgram(program);

    int[] linkStatus = new int[1];
    GL.GetProgram(program, All.wrap(All.LinkStatus), linkStatus);
    if (linkStatus[0] == All.True)
      return program;

    int[] llength = new int[1];
    GL.GetProgram(program, All.wrap(All.InfoLogLength), llength);
    cli.System.Text.StringBuilder log = new cli.System.Text.StringBuilder(llength[0]);
    GL.GetProgramInfoLog(program, llength[0], llength, log);

    log().error("Could not link program: ");
    log().error(log.ToString());
    GL.DeleteProgram(program);
    return 0;
  }

  private int loadShader(All type, final String shaderSource) {
    int shader = GL.CreateShader(type);
    if (shader == 0)
      throw new RuntimeException("Unable to create GL shader: " + GL.GetError());
    GL.ShaderSource(shader, 1, new String[] { shaderSource }, null);
    GL.CompileShader(shader);

    int[] compiled = new int[1];
    GL.GetShader(shader, All.wrap(All.CompileStatus), compiled);
    if (compiled[0] != All.False)
      return shader;

    int[] llength = new int[1];
    GL.GetShader(shader, All.wrap(All.InfoLogLength), llength);
    cli.System.Text.StringBuilder log = new cli.System.Text.StringBuilder(llength[0]);
    GL.GetShaderInfoLog(shader, llength[0], llength, log);

    log().error("Could not compile shader " + type + ":");
    log().error(log.ToString());
    GL.DeleteShader(shader);
    return 0;
  }
}
