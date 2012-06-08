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
 * Defines the interface to shaders used by the GL core. The general usage contract for a shader is
 * the following series of calls:
 *
 * <ul>
 * <li> One or more of the following call pairs:<br/>
 * {@link #prepareTexture} or {@link #prepareColor} followed by
 * {@link #addQuad} or {@link #addTriangles}.
 * <li> A call to {@link #flush} to send everything to the GPU.
 * </li>
 *
 * Because a shader may be prepared multiple times, care should be taken to avoid rebinding the
 * shader program, uniforms, attributes, etc. if a shader is bound again before being flushed.
 * {@link AbstractShader} takes care of this, as well as provides a framework for handling the
 * small variance between texture and color shaders. Custom shader authors should almost certainly
 * extend {@link AbstractShader} rather than implementing things from scratch.
 */
public interface GLShader {

  /** Provides the ability to bind a uniform float value. */
  interface Uniform1f {
    /** Binds a uniform float value. */
    void bind(float a);
  }
  /** Provides the ability to bind a uniform float pair. */
  interface Uniform2f {
    /** Binds a uniform float pair. */
    void bind(float a, float b);
  }
  /** Provides the ability to bind a uniform float triple. */
  interface Uniform3f {
    /** Binds a uniform float triple. */
    void bind(float a, float b, float c);
  }
  /** Provides the ability to bind a uniform float four-tuple. */
  interface Uniform4f {
    /** Binds a uniform float four-tuple. */
    void bind(float a, float b, float c, float d);
  }

  /** Provides the ability to bind a single uniform int. */
  interface Uniform1i {
    /** Binds a uniform int value. */
    void bind(int a);
  }
  /** Provides the ability to bind a uniform int pair. */
  interface Uniform2i {
    /** Binds a uniform int pair. */
    void bind(int a, int b);
  }

  /** Provides the ability to bind a vertex attrib array. */
  interface Attrib {
    /** Binds the this attribute to the vertex array at the specified offset.
     * @param stride the size of a single "bundle" of values in the vertex array.
     * @param offset the offset of this attribute into the "bundle" of values.
     * @param buffer the buffer that supplies this attribute's data. */
    void bind(int stride, int offset, GLBuffer.Float buffer);
  }

  /** Prepares this shader to render the specified texture, etc. */
  void prepareTexture(int tex, float alpha, int fbufWidth, int fbufHeight);

  /** Prepares this shader to render the specified color, etc. */
  void prepareColor(int color, float alpha, int fbufWidth, int fbufHeight);

  /** Sends all accumulated vertex/element info to GL. */
  void flush();

  /** Adds a quad to the current render operation. */
  void addQuad(InternalTransform local,
               float x1, float y1, float sx1, float sy1,
               float x2, float y2, float sx2, float sy2,
               float x3, float y3, float sx3, float sy3,
               float x4, float y4, float sx4, float sy4);

  /** Adds a quad to the current render operation. */
  void addQuad(InternalTransform local,
               float x1, float y1, float x2, float y2,
               float x3, float y3, float x4, float y4);

  /**
   * Adds a collection of triangles to the current render operation.
   *
   * @param xys a list of x/y coordinates as: {@code [x1, y1, x2, y2, ...]}.
   * @param texWidth the width of the texture for which we will auto-generate texture coordinates.
   * @param texHeight the height of the texture for which we will auto-generate texture coordinates.
   * @param indices the index of the triangle vertices in the supplied {@code xys} array. This must
   * be in proper winding order for OpenGL rendering.
   */
  void addTriangles(InternalTransform local, float[] xys, float texWidth, float texHeight,
                    int[] indices);

  /**
   * Adds a collection of triangles to the current render operation.
   *
   * @param xys a list of x/y coordinates as: {@code [x1, y1, x2, y2, ...]}.
   * @param sxys a list of sx/sy texture coordinates as: {@code [sx1, sy1, sx2, sy2, ...]}. This
   * must be of the same length as {@code xys}.
   * @param indices the index of the triangle vertices in the supplied {@code xys} array. This must
   * be in proper winding order for OpenGL rendering.
   */
  void addTriangles(InternalTransform local, float[] xys, float[] sxys, int[] indices);

  /**
   * Notes that this shader is in use by a layer. This is used for reference counted resource
   * management. When all layers release a shader, it can destroy its shader programs and release
   * the GL resources it uses.
   */
  void reference();

  /**
   * Notes that this shader is no longer in use by a layer. This is used for reference counted
   * resource management. When all layers release a shader, it can destroy its shader programs and
   * release the GL resources it uses.
   */
  void release();

  /**
   * Destroys this shader's programs and releases any GL resources. The programs will be recreated
   * if the shader is used again. If a shader is used in a {@link Surface}, where it cannot be
   * reference counted, the caller may wish to manually clear its GL resources when it knows the
   * shader will no longer be used. Alternatively, the resources will be reclaimed when this shader
   * is garbage collected.
   */
  void clearProgram();
}
