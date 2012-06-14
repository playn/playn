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
 * Provides the GL methods and buffer management needed to implement a GL shader.
 */
public interface GLProgram {

  /** Returns a handle on the uniform with the specified name.
   * Returns null if the program contains no uniform with that name. */
  GLShader.Uniform1f getUniform1f(String name);
  /** Returns a handle on the uniform with the specified name.
   * Returns null if the program contains no uniform with that name. */
  GLShader.Uniform2f getUniform2f(String name);
  /** Returns a handle on the uniform with the specified name.
   * Returns null if the program contains no uniform with that name. */
  GLShader.Uniform3f getUniform3f(String name);
  /** Returns a handle on the uniform with the specified name.
   * Returns null if the program contains no uniform with that name. */
  GLShader.Uniform4f getUniform4f(String name);

  /** Returns a handle on the uniform with the specified name.
   * Returns null if the program contains no uniform with that name. */
  GLShader.Uniform1i getUniform1i(String name);
  /** Returns a handle on the uniform with the specified name.
   * Returns null if the program contains no uniform with that name. */
  GLShader.Uniform2i getUniform2i(String name);

  /** Returns a handle on the uniform with the specified name.
   * Returns null if the program contains no uniform with that name. */
  GLShader.Uniform2fv getUniform2fv(String name);
  /** Returns a handle on the uniform with the specified name.
   * Returns null if the program contains no uniform with that name. */
  GLShader.UniformMatrix4fv getUniformMatrix4fv(String name);

  /** Returns a handle on the attribute with the specified name.
   * Returns null if the program contains no attribute with that name. */
  GLShader.Attrib getAttrib(String name, int size, int type);

  /** Binds this shader program, in preparation for rendering. */
  void bind();

  /** Frees this program and associated compiled shaders. The program must not be used after
   * destruction. */
  void destroy();
}
