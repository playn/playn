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

/**
 * Handles the machinations needed to maintain a separate texture and color shader, each of which
 * differ from the other only slightly.
 */
public abstract class AbstractShader implements GLShader {

  /** The GLSL code for our texture fragment shader. */
  public static final String TEX_FRAG_SHADER =
    "#ifdef GL_ES\n" +
    "precision highp float;\n" +
    "#endif\n" +

    "uniform sampler2D u_Texture;\n" +
    "varying vec2 v_TexCoord;\n" +
    "uniform float u_Alpha;\n" +

    "void main(void) {\n" +
    "  vec4 textureColor = texture2D(u_Texture, v_TexCoord);\n" +
    "  gl_FragColor = textureColor * u_Alpha;\n" +
    "}";

  /** The GLSL code for our color fragment shader. */
  public static final String COLOR_FRAG_SHADER =
    "#ifdef GL_ES\n" +
    "precision highp float;\n" +
    "#endif\n" +

    "uniform vec4 u_Color;\n" +
    "uniform float u_Alpha;\n" +

    "void main(void) {\n" +
    "  gl_FragColor = u_Color * u_Alpha;\n" +
    "}";

  protected final GLContext ctx;
  protected int refs;
  protected Core texCore, colorCore, curCore;
  protected Extras texExtras, colorExtras, curExtras;

  protected AbstractShader(GLContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public void prepareTexture(int tex, float alpha, int fbufWidth, int fbufHeight) {
    // create our core lazily so that we ensure we're on the GL thread when it happens
    if (texCore == null) {
      this.texCore = createTextureCore();
      this.texExtras = createTextureExtras(texCore.prog);
    }
    boolean wasntAlreadyActive = ctx.useShader(this, curCore != texCore);
    if (wasntAlreadyActive) {
      curCore = texCore;
      curExtras = texExtras;
      texCore.prepare(fbufWidth, fbufHeight);
    }
    texExtras.prepare(tex, alpha, wasntAlreadyActive);
  }

  @Override
  public void prepareColor(int color, float alpha, int fbufWidth, int fbufHeight) {
    // create our core lazily so that we ensure we're on the GL thread when it happens
    if (colorCore == null) {
      this.colorCore = createColorCore();
      this.colorExtras = createColorExtras(colorCore.prog);
    }
    boolean wasntAlreadyActive = ctx.useShader(this, curCore != colorCore);
    if (wasntAlreadyActive) {
      curCore = colorCore;
      curExtras = colorExtras;
      colorCore.prepare(fbufWidth, fbufHeight);
    }
    colorExtras.prepare(color, alpha, wasntAlreadyActive);
  }

  @Override
  public void flush() {
    curExtras.willFlush();
    curCore.flush();
  }

  @Override
  public void addQuad(InternalTransform local,
                      float x1, float y1, float sx1, float sy1,
                      float x2, float y2, float sx2, float sy2,
                      float x3, float y3, float sx3, float sy3,
                      float x4, float y4, float sx4, float sy4) {
    curCore.addQuad(local, x1, y1, sx1, sy1, x2, y2, sx2, sy2,
                    x3, y3, sx3, sy3, x4, y4, sx4, sy4);
  }

  @Override
  public void addQuad(InternalTransform local,
                      float x1, float y1, float x2, float y2,
                      float x3, float y3, float x4, float y4) {
    addQuad(local, x1, y1, 0, 0, x2, y2, 0, 0, x3, y3, 0, 0, x4, y4, 0, 0);
  }

  @Override
  public void addTriangles(InternalTransform local, float[] xys, float tw, float th, int[] indices) {
    curCore.addTriangles(local, xys, tw, th, indices);
  }

  @Override
  public void addTriangles(InternalTransform local, float[] xys, float[] sxys, int[] indices) {
    curCore.addTriangles(local, xys, sxys, indices);
  }

  @Override
  public void reference() {
    refs++;
  }

  @Override
  public void release() {
    Asserts.checkState(refs > 0, "Released an shader with no references!");
    if (--refs == 0) {
      clearProgram();
    }
  }

  @Override
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

  @Override
  protected void finalize() {
    if (texCore != null || colorCore != null) {
      ctx.queueClearShader(this);
    }
  }

  /** Implements the core of the indexed tris shader. */
  protected static abstract class Core {
    /** The shader of which this core is a part. */
    public final AbstractShader shader;

    /** This core's shader program. */
    public final GLProgram prog;

    /** Prepares this core's shader to render. */
    public abstract void prepare(int fbufWidth, int fbufHeight);

    /** Flushes this core's queued geometry to the GPU. */
    public abstract void flush();

    /** See {@link GLShader#addQuad}. */
    public abstract void addQuad(InternalTransform local,
                                 float x1, float y1, float sx1, float sy1,
                                 float x2, float y2, float sx2, float sy2,
                                 float x3, float y3, float sx3, float sy3,
                                 float x4, float y4, float sx4, float sy4);

    /** See {@link GLShader#addTriangles}. */
    public abstract void addTriangles(InternalTransform local,
                                      float[] xys, float tw, float th, int[] indices);

    /** See {@link GLShader#addTriangles}. */
    public abstract void addTriangles(InternalTransform local,
                                      float[] xys, float[] sxys, int[] indices);

    /** Destroys this core's shader program and any other GL resources it maintains. */
    public void destroy() {
      prog.destroy();
    }

    protected Core(AbstractShader shader, GLProgram prog) {
      this.shader = shader;
      this.prog = prog;
    }
  }

  protected abstract Core createTextureCore();
  protected abstract Core createColorCore();

  /** Handles the extra bits needed when we're using textures or flat color. */
  protected static abstract class Extras {
    /** Performs additional binding to prepare for a texture or color render. */
    public abstract void prepare(int texOrColor, float alpha, boolean wasntAlreadyActive);

    /** Called prior to flushing this shader. Defaults to NOOP. */
    public void willFlush() {}

    /** Destroys any GL resources maintained by this extras. Defaults to NOOP. */
    public void destroy() {}
  }

  protected class TextureExtras extends Extras {
    private final Uniform1i uTexture;
    private final Uniform1f uAlpha;
    private int lastTex;
    private float lastAlpha;

    public TextureExtras(GLProgram prog) {
      uTexture = prog.getUniform1i("u_Texture");
      uAlpha = prog.getUniform1f("u_Alpha");
    }

    @Override
    public void prepare(int tex, float alpha, boolean wasntAlreadyActive) {
      ctx.checkGLError("textureShader.prepare start");
      if (wasntAlreadyActive || tex != lastTex || alpha != lastAlpha) {
        flush();
        uAlpha.bind(alpha);
        lastAlpha = alpha;
        lastTex = tex;
        ctx.checkGLError("textureShader.prepare end");
      }

      if (wasntAlreadyActive) {
        ctx.activeTexture(GL20.GL_TEXTURE0);
        uTexture.bind(0);
      }
    }

    @Override
    public void willFlush () {
      ctx.bindTexture(lastTex);
    }
  }

  /**
   * Returns the texture fragment shader program. Note that this program <em>must</em> preserve the
   * use of the existing varying attributes. You can add new varying attributes, but you cannot
   * remove or change the defaults.
   */
  protected String textureFragmentShader() {
    return TEX_FRAG_SHADER;
  }

  /**
   * Creates the extras instance that handles the texture fragment shader.
   */
  protected Extras createTextureExtras(GLProgram prog) {
    return new TextureExtras(prog);
  }

  protected class ColorExtras extends Extras {
    private final Uniform4f uColor;
    private final Uniform1f uAlpha;
    private int lastColor;
    private float lastAlpha;

    public ColorExtras(GLProgram prog) {
      uColor = prog.getUniform4f("u_Color");
      uAlpha = prog.getUniform1f("u_Alpha");
    }

    @Override
    public void prepare(int color, float alpha, boolean wasntAlreadyActive) {
      ctx.checkGLError("colorShader.prepare start");
      if (wasntAlreadyActive || color != lastColor || alpha != lastAlpha) {
        flush();
        float a = ((color >> 24) & 0xff) / 255f;
        float r = ((color >> 16) & 0xff) / 255f;
        float g = ((color >> 8) & 0xff) / 255f;
        float b = ((color >> 0) & 0xff) / 255f;
        uColor.bind(r, g, b, 1);
        lastColor = color;
        uAlpha.bind(alpha * a);
        lastAlpha = alpha;
        ctx.checkGLError("colorShader.prepare end");
      }
    }
  }

  /**
   * Returns the color fragment shader program. Note that this program <em>must</em> preserve the
   * use of the existing varying attributes. You can add new varying attributes, but you cannot
   * remove or change the defaults.
   */
  protected String colorFragmentShader() {
    return COLOR_FRAG_SHADER;
  }

  /**
   * Creates the extras instance that handles the color fragment shader.
   */
  protected Extras createColorExtras(GLProgram prog) {
    return new ColorExtras(prog);
  }
}
