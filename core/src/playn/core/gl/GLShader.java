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
import playn.core.Surface;

/**
 * Defines the interface to shaders used by the GL core. The general usage contract for a shader is
 * the following series of calls:
 *
 * <ul>
 * <li> One or more of the following call pairs:<br>
 * {@link #prepareTexture} followed by {@link #addQuad} or {@link #addTriangles}.
 * <li> A call to {@link #flush} to send everything to the GPU.
 * </ul>
 *
 * Because a shader may be prepared multiple times, care should be taken to avoid rebinding the
 * shader program, uniforms, attributes, etc. if a shader is bound again before being flushed. The
 * base implementation takes care of this.
 */
public abstract class GLShader {

  /** Provides the ability to bind a uniform float value. */
  public static interface Uniform1f {
    /** Binds a uniform float value. */
    void bind(float a);
  }
  /** Provides the ability to bind a uniform float pair. */
  public static interface Uniform2f {
    /** Binds a uniform float pair. */
    void bind(float a, float b);
  }
  /** Provides the ability to bind a uniform float triple. */
  public static interface Uniform3f {
    /** Binds a uniform float triple. */
    void bind(float a, float b, float c);
  }
  /** Provides the ability to bind a uniform float four-tuple. */
  public static interface Uniform4f {
    /** Binds a uniform float four-tuple. */
    void bind(float a, float b, float c, float d);
  }

  /** Provides the ability to bind a single uniform int. */
  public static interface Uniform1i {
    /** Binds a uniform int value. */
    void bind(int a);
  }
  /** Provides the ability to bind a uniform int pair. */
  public static interface Uniform2i {
    /** Binds a uniform int pair. */
    void bind(int a, int b);
  }

  /** Provides the ability to bind a uniform vec2 vector. */
  public static interface Uniform2fv {
    /** Binds a uniform vec2 vector to the supplied data.
     * @param count the number of <em>vec2</em>s to bind (not individual floats). */
    void bind(GLBuffer.Float data, int count);
  }
  /** Provides the ability to bind a uniform vec4 vector. */
  public static interface Uniform4fv {
    /** Binds a uniform vec4 vector to the supplied data.
     * @param count the number of <em>vec4</em>s to bind (not individual floats). */
    void bind(GLBuffer.Float data, int count);
  }
  /** Provides the ability to bind a uniform matrix4 vector. */
  public static interface UniformMatrix4fv {
    /** Binds a uniform matrix4 vector to the supplied data.
     * @param count the number of <em>matrices</em> to bind (whole matrices, not floats). */
    void bind(GLBuffer.Float data, int count);
  }

  /** Provides the ability to bind a vertex attrib array. */
  public static interface Attrib {
    /** Binds the this attribute to the vertex array at the specified offset.
     * @param stride the size of a single "bundle" of values in the vertex array.
     * @param offset the offset of this attribute into the "bundle" of values. */
    void bind(int stride, int offset);

    /** Disables the vertex array index for this attribute. */
    void unbind();
  }

  protected static final String FRAGMENT_PREAMBLE =
      "#ifdef GL_ES\n" +
      "precision lowp float;\n" +
      "#else\n" +
      // Not all versions of regular OpenGL supports precision qualifiers, define placeholders
      "#define lowp\n" +
      "#define mediump\n" +
      "#define highp\n" +
      "#endif\n";

  protected final GLContext ctx;
  protected int refs;
  protected Core texCore;
  private int texEpoch;

  /** Prepares this shader to render the specified texture, etc. */
  public GLShader prepareTexture(int tex, int tint) {
    // if our GL context has been lost and regained we may need to recreate our core; we don't
    // destroy the old core because the underlying resources are gone and destroying using our
    // stale handles might result in destroying someone else's newly created resources
    if (texEpoch != ctx.epoch()) {
      texCore = null;
    }
    // create our core lazily so that we ensure we're on the GL thread when it happens
    if (texCore == null) {
      createCore();
    }
    boolean justActivated = ctx.useShader(this);
    if (justActivated) {
      texCore.activate(ctx.curFbufWidth, ctx.curFbufHeight);
      if (GLContext.STATS_ENABLED) ctx.stats.shaderBinds++;
    }
    texCore.prepare(tex, tint, justActivated);
    return this;
  }

  /** Sends all accumulated vertex/element info to GL. */
  public void flush() {
    texCore.flush();
    if (GLContext.STATS_ENABLED) ctx.stats.shaderFlushes++;
  }

  /** Does any necessary shutdown when no longer using this shader. */
  public void deactivate() {
    texCore.deactivate();
  }

  /** Adds an axis-aligned quad to the current render operation. {@code left, top, right, bottom}
   * define the bounds of the quad. {@code sl, st, sr, sb} define the texture coordinates. */
  public void addQuad(float m00, float m01, float m10, float m11, float tx, float ty,
                      float left, float top, float right, float bottom,
                      float sl, float st, float sr, float sb) {
    texCore.addQuad(m00, m01, m10, m11, tx, ty,
                    left,  top,    sl, st,
                    right, top,    sr, st,
                    left,  bottom, sl, sb,
                    right, bottom, sr, sb);
    if (GLContext.STATS_ENABLED) ctx.stats.quadsRendered++;
  }

  /** Adds an axis-aligned quad to the current render operation. {@code left, top, right, bottom}
   * define the bounds of the quad. {@code sl, st, sr, sb} define the texture coordinates. */
  public void addQuad(InternalTransform local, float left, float top, float right, float bottom,
                      float sl, float st, float sr, float sb) {
    texCore.addQuad(local.m00(), local.m01(), local.m10(), local.m11(), local.tx(), local.ty(),
                    left,  top,    sl, st,
                    right, top,    sr, st,
                    left,  bottom, sl, sb,
                    right, bottom, sr, sb);
    if (GLContext.STATS_ENABLED) ctx.stats.quadsRendered++;
  }

  /**
   * Adds a collection of triangles to the current render operation.
   *
   * @param xys a list of x/y coordinates as: {@code [x1, y1, x2, y2, ...]}.
   * @param xysOffset the offset of the coordinates array, must not be negative and no greater than
   * {@code xys.length}. Note: this is an absolute offset; since {@code xys} contains pairs of
   * values, this will be some multiple of two.
   * @param xysLen the number of coordinates to read, must be no less than zero and no greater than
   * {@code xys.length - xysOffset}. Note: this is an absolute length; since {@code xys} contains
   * pairs of values, this will be some multiple of two.
   * @param tw the width of the texture for which we will auto-generate texture coordinates.
   * @param th the height of the texture for which we will auto-generate texture coordinates.
   * @param indices the index of the triangle vertices in the {@code xys} array. Because this
   * method renders a slice of {@code xys}, one must also specify {@code indexBase} which tells us
   * how to interpret indices. The index into {@code xys} will be computed as:
   * {@code 2*(indices[ii] - indexBase)}, so if your indices reference vertices relative to the
   * whole array you should pass {@code xysOffset/2} for {@code indexBase}, but if your indices
   * reference vertices relative to <em>the slice</em> then you should pass zero.
   * @param indicesOffset the offset of the indices array, must not be negative and no greater than
   * {@code indices.length}.
   * @param indicesLen the number of indices to read, must be no less than zero and no greater than
   * {@code indices.length - indicesOffset}.
   * @param indexBase the basis for interpreting {@code indices}. See the docs for {@code indices}
   * for details.
   */
  public void addTriangles(InternalTransform local, float[] xys, int xysOffset, int xysLen,
                           float tw, float th,
                           int[] indices, int indicesOffset, int indicesLen, int indexBase) {
    texCore.addTriangles(
      local.m00(), local.m01(), local.m10(), local.m11(), local.tx(), local.ty(),
      xys, xysOffset, xysLen, tw, th, indices, indicesOffset, indicesLen, indexBase);
    if (GLContext.STATS_ENABLED) ctx.stats.trisRendered += indicesLen/3;
  }

  /**
   * Adds a collection of triangles to the current render operation. See
   * {@link #addTriangles(InternalTransform,float[],int,int,float,float,int[],int,int,int)} for
   * parameter documentation.
   *
   * @param sxys a list of sx/sy texture coordinates as: {@code [sx1, sy1, sx2, sy2, ...]}. This
   * must be of the same length as {@code xys}.
   */
  public void addTriangles(InternalTransform local,
                           float[] xys, float[] sxys, int xysOffset, int xysLen,
                           int[] indices, int indicesOffset, int indicesLen, int indexBase) {
    texCore.addTriangles(
      local.m00(), local.m01(), local.m10(), local.m11(), local.tx(), local.ty(),
      xys, sxys, xysOffset, xysLen, indices, indicesOffset, indicesLen, indexBase);
    if (GLContext.STATS_ENABLED) ctx.stats.trisRendered += indicesLen/3;
  }

  /**
   * Notes that this shader is in use by a layer. This is used for reference counted resource
   * management. When all layers release a shader, it can destroy its shader programs and release
   * the GL resources it uses.
   */
  public void reference() {
    refs++;
  }

  /**
   * Notes that this shader is no longer in use by a layer. This is used for reference counted
   * resource management. When all layers release a shader, it can destroy its shader programs and
   * release the GL resources it uses.
   */
  public void release() {
    assert refs > 0 : "Released an shader with no references!";
    if (--refs == 0) {
      clearProgram();
    }
  }

  /**
   * Destroys this shader's programs and releases any GL resources. The programs will be recreated
   * if the shader is used again. If a shader is used in a {@link Surface}, where it cannot be
   * reference counted, the caller may wish to manually clear its GL resources when it knows the
   * shader will no longer be used. Alternatively, the resources will be reclaimed when this shader
   * is garbage collected.
   */
  public void clearProgram() {
    if (texCore != null) {
      texCore.destroy();
      texCore = null;
    }
  }

  /**
   * Forces the creation of our shader core. Used during GLContext.init to determine whether we
   * need to fall back to a less sophisticated quad shader.
   */
  void createCore() {
    this.texEpoch = ctx.epoch();
    this.texCore = createTextureCore();
  }

  protected GLShader(GLContext ctx) {
    this.ctx = ctx;
  }

  @Override
  protected void finalize() {
    if (texCore != null) {
      ctx.queueClearShader(this);
    }
  }

  /** Creates the texture core for this shader. */
  protected abstract Core createTextureCore();

  /**
   * Returns the texture fragment shader program. Note that this program <em>must</em> preserve the
   * use of the existing varying attributes. You can add new varying attributes, but you cannot
   * remove or change the defaults.
   */
  protected String textureFragmentShader() {
    StringBuilder str = new StringBuilder(FRAGMENT_PREAMBLE);

    str.append(textureUniforms());
    str.append(textureVaryings());

    str.append("void main(void) {\n");

    str.append(textureColor());
    str.append(textureTint());
    str.append(textureAlpha());

    str.append(
      "  gl_FragColor = textureColor;\n" +
      "}");

    return str.toString();
  }

  protected String textureUniforms() {
    return "uniform lowp sampler2D u_Texture;\n";
  }

  protected String textureVaryings() {
    return
      "varying mediump vec2 v_TexCoord;\n" +
      "varying lowp vec4 v_Color;\n";
  }

  protected String textureColor() {
    return "  vec4 textureColor = texture2D(u_Texture, v_TexCoord);\n";
  }

  protected String textureTint() {
    return "  textureColor.rgb *= v_Color.rgb;\n";
  }

  protected String textureAlpha() {
    return "  textureColor *= v_Color.a;\n";
  }

  /** Implements the actual core of the shader. This is factored out to allow the core to be
   * created/destroyed multiple times within the lifespan of its containing GLShader instance. */
  protected abstract class Core {
    /** This core's shader program. */
    public final GLProgram prog;

    /** Called to setup this core's shader after initially being bound. */
    public abstract void activate(int fbufWidth, int fbufHeight);

    /** Called when this core is no longer being used. */
    public abstract void deactivate();

    /** Called before each primitive to update the current color. */
    public void prepare(int tex, int tint, boolean justActivated) {
      ctx.checkGLError("textureShader.prepare start");
      boolean stateChanged = (tex != lastTex);
      if (!justActivated && stateChanged) {
        GLShader.this.flush();
        ctx.checkGLError("textureShader.prepare flush");
      }
      if (stateChanged) {
        lastTex = tex;
        ctx.checkGLError("textureShader.prepare end");
      }
      if (justActivated) {
        ctx.activeTexture(GL20.GL_TEXTURE0);
        uTexture.bind(0);
      }
    }

    /** Flushes this core's queued geometry to the GPU. */
    public void flush() {
      ctx.bindTexture(lastTex);
    }

    /** Destroys this core's shader program and any other GL resources it maintains. */
    public void destroy() {
      prog.destroy();
    }

    /** See {@link GLShader#addQuad}. */
    public abstract void addQuad(float m00, float m01, float m10, float m11, float tx, float ty,
                                 float x1, float y1, float sx1, float sy1,
                                 float x2, float y2, float sx2, float sy2,
                                 float x3, float y3, float sx3, float sy3,
                                 float x4, float y4, float sx4, float sy4);

    /** See {@link GLShader#addTriangles}. */
    public void addTriangles(float m00, float m01, float m10, float m11, float tx, float ty,
                             float[] xys, int xysOffset, int xysLen, float tw, float th,
                             int[] indices, int indicesOffset, int indicesLen, int indexBase) {
      throw new UnsupportedOperationException("Triangles not supported by this shader");
    }

    /** See {@link GLShader#addTriangles}. */
    public void addTriangles(float m00, float m01, float m10, float m11, float tx, float ty,
                             float[] xys, float[] sxys, int xysOffset, int xysLen,
                             int[] indices, int indicesOffset, int indicesLen, int indexBase) {
      throw new UnsupportedOperationException("Triangles not supported by this shader");
    }

    protected final Uniform1i uTexture;
    protected int lastTex;

    protected Core(String vertShader, String fragShader) {
      this.prog = ctx.createProgram(vertShader, fragShader);
      this.uTexture = prog.getUniform1i("u_Texture");
    }
  }
}
