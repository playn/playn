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

import playn.core.Asserts;
import playn.core.InternalTransform;
import playn.core.Surface;

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
 * shader program, uniforms, attributes, etc. if a shader is bound again before being flushed. The
 * base implementation takes care of this, as well as provides a framework for handling the small
 * variance between texture and color shaders ({@link Core}, {@link Extras}).
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
  protected Core texCore, colorCore, curCore;
  protected Extras texExtras, colorExtras, curExtras;
  private int texEpoch, colorEpoch;

  /** Prepares this shader to render the specified texture, etc. */
  public GLShader prepareTexture(int tex, int tint) {
    // if our GL context has been lost and regained we may need to recreate our core
    if (texEpoch != ctx.epoch()) {
      // we don't destroy because the underlying resources are gone and destroying using our stale
      // handles might result in destroying some newly created resources
      texCore = null;
      texExtras = null;
    }
    // create our core lazily so that we ensure we're on the GL thread when it happens
    if (texCore == null) {
      this.texEpoch = ctx.epoch();
      this.texCore = createTextureCore();
      this.texExtras = createTextureExtras(texCore.prog);
    }
    boolean justActivated = ctx.useShader(this, curCore != texCore);
    if (justActivated) {
      curCore = texCore;
      curExtras = texExtras;
      texCore.activate(ctx.curFbufWidth, ctx.curFbufHeight);
      if (GLContext.STATS_ENABLED) ctx.stats.shaderBinds++;
    }
    texCore.prepare(tint, justActivated);
    texExtras.prepare(tex, justActivated);
    return this;
  }

  /** Prepares this shader to render the specified color, etc. */
  public GLShader prepareColor(int tint) {
    // if our GL context has been lost and regained we may need to recreate our core
    if (colorEpoch != ctx.epoch()) {
      // we don't destroy because the underlying resources are gone and destroying using our stale
      // handles might result in destroying some newly created resources
      colorCore = null;
      colorExtras = null;
    }
    // create our core lazily so that we ensure we're on the GL thread when it happens
    if (colorCore == null) {
      this.colorEpoch = ctx.epoch();
      this.colorCore = createColorCore();
      this.colorExtras = createColorExtras(colorCore.prog);
    }
    boolean justActivated = ctx.useShader(this, curCore != colorCore);
    if (justActivated) {
      curCore = colorCore;
      curExtras = colorExtras;
      colorCore.activate(ctx.curFbufWidth, ctx.curFbufHeight);
      if (GLContext.STATS_ENABLED) ctx.stats.shaderBinds++;
    }
    colorCore.prepare(tint, justActivated);
    colorExtras.prepare(0, justActivated);
    return this;
  }

  /** Sends all accumulated vertex/element info to GL. */
  public void flush() {
    curExtras.willFlush();
    curCore.flush();
    if (GLContext.STATS_ENABLED) ctx.stats.shaderFlushes++;
  }

  /** Does any necessary shutdown when no longer using this shader. */
  public void deactivate() {
    curCore.deactivate();
  }

  /** Adds an axis-aligned quad to the current render operation. {@code left, top, right, bottom}
   * define the bounds of the quad. {@code sl, st, sr, sb} define the texture coordinates. */
  public void addQuad(float m00, float m01, float m10, float m11, float tx, float ty,
                      float left, float top, float right, float bottom,
                      float sl, float st, float sr, float sb) {
    curCore.addQuad(m00, m01, m10, m11, tx, ty,
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
    curCore.addQuad(local.m00(), local.m01(), local.m10(), local.m11(), local.tx(), local.ty(),
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
   * @param tw the width of the texture for which we will auto-generate texture coordinates.
   * @param th the height of the texture for which we will auto-generate texture coordinates.
   * @param indices the index of the triangle vertices in the supplied {@code xys} array. This must
   * be in proper winding order for OpenGL rendering.
   */
  public void addTriangles(InternalTransform local, float[] xys, float tw, float th, int[] indices) {
    curCore.addTriangles(local.m00(), local.m01(), local.m10(), local.m11(), local.tx(), local.ty(),
                         xys, tw, th, indices);
    if (GLContext.STATS_ENABLED) ctx.stats.trisRendered += indices.length/3;
  }

  /**
   * Adds a collection of triangles to the current render operation.
   *
   * @param xys a list of x/y coordinates as: {@code [x1, y1, x2, y2, ...]}.
   * @param sxys a list of sx/sy texture coordinates as: {@code [sx1, sy1, sx2, sy2, ...]}. This
   * must be of the same length as {@code xys}.
   * @param indices the index of the triangle vertices in the supplied {@code xys} array. This must
   * be in proper winding order for OpenGL rendering.
   */
  public void addTriangles(InternalTransform local, float[] xys, float[] sxys, int[] indices) {
    curCore.addTriangles(local.m00(), local.m01(), local.m10(), local.m11(), local.tx(), local.ty(),
                         xys, sxys, indices);
    if (GLContext.STATS_ENABLED) ctx.stats.trisRendered += indices.length/3;
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
    Asserts.checkState(refs > 0, "Released an shader with no references!");
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
      texExtras.destroy();
      texCore = null;
      texExtras = null;
    }
    if (colorCore != null) {
      colorCore.destroy();
      colorExtras.destroy();
      colorCore = null;
      colorExtras = null;
    }
    curCore = null;
    curExtras = null;
  }

  /**
   * Forces the creation of our shader cores. Used during GLContext.init to determine whether we
   * need to fall back to a less sophisticated quad shader.
   */
  public void createCores() {
    this.texCore = createTextureCore();
    this.texExtras = createTextureExtras(texCore.prog);
    this.colorCore = createColorCore();
    this.colorExtras = createColorExtras(colorCore.prog);
  }

  protected GLShader(GLContext ctx) {
    this.ctx = ctx;
  }

  @Override
  protected void finalize() {
    if (texCore != null || colorCore != null) {
      ctx.queueClearShader(this);
    }
  }

  /** Creates the texture core for this shader. */
  protected abstract Core createTextureCore();

  /** Creates the color core for this shader. */
  protected abstract Core createColorCore();

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

  /**
   * Creates the extras instance that handles the texture fragment shader.
   */
  protected Extras createTextureExtras(GLProgram prog) {
    return new TextureExtras(prog);
  }

  /**
   * Returns the color fragment shader program. Note that this program <em>must</em> preserve the
   * use of the existing varying attributes. You can add new varying attributes, but you cannot
   * remove or change the defaults.
   */
  protected String colorFragmentShader() {
    return FRAGMENT_PREAMBLE +
      "varying lowp vec4 v_Color;\n" +

      "void main(void) {\n" +
      "  gl_FragColor = vec4(v_Color.rgb, 1) * v_Color.a;\n" +
      "}";
  }

  /**
   * Creates the extras instance that handles the color fragment shader.
   */
  protected Extras createColorExtras(GLProgram prog) {
    return new ColorExtras(prog);
  }

  /** Implements the core of the indexed tris shader. */
  protected abstract class Core {
    /** This core's shader program. */
    public final GLProgram prog;

    /** Called to setup this core's shader after initially being bound. */
    public abstract void activate(int fbufWidth, int fbufHeight);

    /** Called when this core is no longer being used. */
    public abstract void deactivate();

    /** Called before each primitive to update the current color. */
    public abstract void prepare(int tint, boolean justActivated);

    /** Flushes this core's queued geometry to the GPU. */
    public abstract void flush();

    /** See {@link GLShader#addQuad}. */
    public abstract void addQuad(float m00, float m01, float m10, float m11, float tx, float ty,
                                 float x1, float y1, float sx1, float sy1,
                                 float x2, float y2, float sx2, float sy2,
                                 float x3, float y3, float sx3, float sy3,
                                 float x4, float y4, float sx4, float sy4);

    /** See {@link GLShader#addTriangles}. */
    public void addTriangles(float m00, float m01, float m10, float m11, float tx, float ty,
                             float[] xys, float tw, float th, int[] indices) {
      throw new UnsupportedOperationException("Triangles not supported by this shader");
    }

    /** See {@link GLShader#addTriangles}. */
    public void addTriangles(float m00, float m01, float m10, float m11, float tx, float ty,
                             float[] xys, float[] sxys, int[] indices) {
      throw new UnsupportedOperationException("Triangles not supported by this shader");
    }

    /** Destroys this core's shader program and any other GL resources it maintains. */
    public void destroy() {
      prog.destroy();
    }

    protected Core(String vertShader, String fragShader) {
      this.prog = ctx.createProgram(vertShader, fragShader);
    }
  }

  /** Handles the extra bits needed when we're using textures or flat color. */
  protected static abstract class Extras {
    /** Performs additional binding to prepare for a texture or color render. */
    public abstract void prepare(int tex, boolean justActivated);

    /** Called prior to flushing this shader. Defaults to NOOP. */
    public void willFlush() {}

    /** Destroys any GL resources maintained by this extras. Defaults to NOOP. */
    public void destroy() {}
  }

  /** The default texture extras. */
  protected class TextureExtras extends Extras {
    protected final Uniform1i uTexture;
    protected int lastTex;

    public TextureExtras(GLProgram prog) {
      uTexture = prog.getUniform1i("u_Texture");
    }

    @Override
    public void prepare(int tex, boolean justActivated) {
      ctx.checkGLError("textureShader.prepare start");
      boolean stateChanged = (tex != lastTex);
      if (!justActivated && stateChanged) {
        flush();
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

    @Override
    public void willFlush () {
      ctx.bindTexture(lastTex);
    }
  }

  /** The default color extras. */
  // TODO(bruno): Remove?
  protected class ColorExtras extends Extras {
    public ColorExtras(GLProgram prog) {
    }

    @Override
    public void prepare(int tex, boolean justActivated) {
      // Nothing at all
    }
  }
}
