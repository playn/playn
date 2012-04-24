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
package playn.html;

import com.google.gwt.typedarrays.client.Float32Array;
import com.google.gwt.webgl.client.WebGLBuffer;
import com.google.gwt.webgl.client.WebGLProgram;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLTexture;
import com.google.gwt.webgl.client.WebGLUniformLocation;
import com.google.gwt.webgl.client.WebGLUtil;

import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

import playn.core.gl.GLShader;

class HtmlShaderCore {

  static class Texture extends HtmlShaderCore {
    private final WebGLUniformLocation uTexture;
    private final WebGLUniformLocation uAlpha;
    private WebGLTexture lastTex;
    private float lastAlpha;

    Texture(HtmlGLContext ctx) {
      super(ctx, GLShader.TEX_FRAG_SHADER);
      uTexture = gl.getUniformLocation(program, "u_Texture");
      uAlpha = gl.getUniformLocation(program, "u_Alpha");
    }

    public void prepare(GLShader shader, Object texObj, float alpha, int fbufWidth, int fbufHeight) {
      boolean wasntAlreadyActive = prepare(shader, fbufWidth, fbufHeight);
      if (wasntAlreadyActive) {
        gl.activeTexture(TEXTURE0);
        gl.uniform1i(uTexture, 0);
      }

      WebGLTexture tex = (WebGLTexture) texObj;
      if (wasntAlreadyActive || tex != lastTex || alpha != lastAlpha) {
        shader.flush();
        gl.uniform1f(uAlpha, alpha);
        lastAlpha = alpha;
        lastTex = tex;
      }
    }

    @Override
    public WebGLRenderingContext flush() {
      gl.bindTexture(TEXTURE_2D, lastTex);
      return super.flush();
    }
  }

  static class Color extends HtmlShaderCore {
    private final WebGLUniformLocation uColor;
    private final WebGLUniformLocation uAlpha;
    private final Float32Array colors = Float32Array.create(4);
    private int lastColor;
    private float lastAlpha;

    Color(HtmlGLContext ctx) {
      super(ctx, GLShader.COLOR_FRAG_SHADER);
      uColor = gl.getUniformLocation(program, "u_Color");
      uAlpha = gl.getUniformLocation(program, "u_Alpha");
    }

    public void prepare(GLShader shader, int color, float alpha, int fbufWidth, int fbufHeight) {
      boolean wasntAlreadyActive = prepare(shader, fbufWidth, fbufHeight);
      if (wasntAlreadyActive || color != lastColor || alpha != lastAlpha) {
        shader.flush();
        gl.uniform1f(uAlpha, alpha);
        lastAlpha = alpha;
        setColor(color);
      }
    }

    private void setColor(int color) {
      // ABGR.
      colors.set(3, (float)((color >> 24) & 0xff) / 255);
      colors.set(0, (float)((color >> 16) & 0xff) / 255);
      colors.set(1, (float)((color >> 8) & 0xff) / 255);
      colors.set(2, (float)((color >> 0) & 0xff) / 255);
      gl.uniform4fv(uColor, colors);

      lastColor = color;
    }
  }

  public static final int VERTEX_SIZE = 10; // 10 floats per vertex

  protected final WebGLRenderingContext gl;
  protected final HtmlGLContext ctx;

  protected final WebGLProgram program;
  private final WebGLUniformLocation uScreenSizeLoc;
  private final int aMatrix, aTranslation, aPosition, aTexture;

  private final WebGLBuffer vertexBuffer;

  public WebGLRenderingContext flush() {
    return gl;
  }

  protected HtmlShaderCore(HtmlGLContext ctx, String fragmentShader) {
    this.ctx = ctx;
    this.gl = ctx.gl;

    // Compile the shader.
    program = WebGLUtil.createShaderProgram(gl, GLShader.VERTEX_SHADER, fragmentShader);

    // glGet*() calls are slow; determine locations once.
    uScreenSizeLoc = gl.getUniformLocation(program, "u_ScreenSize");
    aMatrix = gl.getAttribLocation(program, "a_Matrix");
    aTranslation = gl.getAttribLocation(program, "a_Translation");
    aPosition = gl.getAttribLocation(program, "a_Position");
    aTexture = gl.getAttribLocation(program, "a_Texture");

    // Create the vertex buffers.
    vertexBuffer = gl.createBuffer();
  }

  protected boolean prepare(GLShader shader, int fbufWidth, int fbufHeight) {
    if (!ctx.useShader(shader))
      return false;

    gl.useProgram(program);
    gl.uniform2fv(uScreenSizeLoc, new float[] { fbufWidth, fbufHeight });
    bindBuffers();

    gl.enableVertexAttribArray(aMatrix);
    gl.enableVertexAttribArray(aTranslation);
    gl.enableVertexAttribArray(aPosition);
    if (aTexture != -1)
      gl.enableVertexAttribArray(aTexture);

    gl.vertexAttribPointer(aMatrix, 4, FLOAT, false, 40, 0);
    gl.vertexAttribPointer(aTranslation, 2, FLOAT, false, 40, 16);
    gl.vertexAttribPointer(aPosition, 2, FLOAT, false, 40, 24);
    if (aTexture != -1)
      gl.vertexAttribPointer(aTexture, 2, FLOAT, false, 40, 32);

    return true;
  }

  protected void bindBuffers() {
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer);
  }
}
