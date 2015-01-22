/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.core;

/**
 * A batch that renders textured primitives.
 */
public class TexturedBatch extends GLBatch {

  /** Provides some standard bits for a shader program that uses a tint and a texture. */
  public static abstract class Source {

    /** Returns the source of the texture fragment shader program. Note that this program
      * <em>must</em> preserve the use of the existing varying attributes. You can add new varying
      * attributes, but you cannot remove or change the defaults. */
    public String fragment () {
      StringBuilder str = new StringBuilder(FRAGMENT_PREAMBLE);
      str.append(textureUniforms());
      str.append(textureVaryings());
      str.append("void main(void) {\n");
      str.append(textureColor());
      str.append(textureTint());
      str.append(textureAlpha());
      str.append("  gl_FragColor = textureColor;\n" +
                 "}");
      return str.toString();
    }

    protected String textureUniforms () {
      return "uniform lowp sampler2D u_Texture;\n";
    }
    protected String textureVaryings () {
      return ("varying mediump vec2 v_TexCoord;\n" +
              "varying lowp vec4 v_Color;\n");
    }
    protected String textureColor () {
      return "  vec4 textureColor = texture2D(u_Texture, v_TexCoord);\n";
    }
    protected String textureTint () {
      return "  textureColor.rgb *= v_Color.rgb;\n";
    }
    protected String textureAlpha () {
      return "  textureColor *= v_Color.a;\n";
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
  }

  public final GL20 gl;
  protected int curTexId;

  /** Prepares this batch to render using the supplied texture. If pending operations have been
    * added to this batch for a different texture, this call will trigger a {@link #flush}.
    * <p>Note: if you call {@code add} methods that take a texture, you do not need to call this
    * method manually. Only if you're adding bare primitives is it needed. */
  public void setTexture (Texture texture) {
    if (curTexId != 0 && curTexId != texture.id) flush();
    this.curTexId = texture.id;
  }

  @Override public void end () {
    super.end();
    curTexId = 0;
  }

  protected TexturedBatch (GL20 gl) {
    this.gl = gl;
  }

  /** Binds our current texture. Subclasses need to call this in {@link #flush}. */
  protected void bindTexture () {
    gl.glBindTexture(GL20.GL_TEXTURE_2D, curTexId);
    gl.checkError("QuadBatch glBindTexture");
  }
}
