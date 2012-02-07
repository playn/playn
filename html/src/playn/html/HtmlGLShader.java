/**
 * Copyright 2011 The PlayN Authors
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

import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.typedarrays.client.Float32Array;
import com.google.gwt.typedarrays.client.Int32Array;
import com.google.gwt.typedarrays.client.Uint16Array;
import com.google.gwt.typedarrays.client.Uint8Array;
import com.google.gwt.webgl.client.WebGLBuffer;
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLProgram;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLTexture;
import com.google.gwt.webgl.client.WebGLUniformLocation;
import com.google.gwt.webgl.client.WebGLUtil;

import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

import playn.core.InternalTransform;
import playn.core.gl.GLContext;
import playn.core.gl.GLShader;
import playn.core.gl.LayerGL;

public class HtmlGLShader implements GLShader {
  public static class Texture extends HtmlGLShader implements GLShader.Texture {
    private WebGLUniformLocation uTexture;
    private WebGLUniformLocation uAlpha;
    private WebGLTexture lastTex;
    private float lastAlpha;

    Texture(HtmlGLContext ctx) {
      super(ctx, TEX_FRAG_SHADER);
      uTexture = gl.getUniformLocation(program, "u_Texture");
      uAlpha = gl.getUniformLocation(program, "u_Alpha");
    }

    @Override
    public void flush() {
      gl.bindTexture(TEXTURE_2D, lastTex);
      super.flush();
    }

    @Override
    public void prepare(Object texObj, float alpha) {
      if (super.prepare()) {
        gl.activeTexture(TEXTURE0);
        gl.uniform1i(uTexture, 0);
      }

      WebGLTexture tex = (WebGLTexture) texObj;
      if (tex == lastTex && alpha == lastAlpha) {
        return;
      }
      flush();

      gl.uniform1f(uAlpha, alpha);
      lastAlpha = alpha;
      lastTex = tex;
    }
  }

  public static class Color extends HtmlGLShader implements GLShader.Color {
    private WebGLUniformLocation uColor;
    private WebGLUniformLocation uAlpha;
    private Float32Array colors = Float32Array.create(4);
    private int lastColor;
    private float lastAlpha;

    Color(HtmlGLContext ctx) {
      super(ctx, COLOR_FRAG_SHADER);
      uColor = gl.getUniformLocation(program, "u_Color");
      uAlpha = gl.getUniformLocation(program, "u_Alpha");
    }

    @Override
    public void prepare(int color, float alpha) {
      super.prepare();

      if (color == lastColor && alpha == lastAlpha) {
        return;
      }
      flush();

      gl.uniform1f(uAlpha, alpha);
      lastAlpha = alpha;
      setColor(color);
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

  private static final int VERTEX_SIZE = 10;              // 10 floats per vertex

  // TODO(jgw): Re-enable longer element buffers once we figure out why they're causing weird
  // performance degradation.
  //  private static final int MAX_VERTS = 400;               // 100 quads
  //  private static final int MAX_ELEMS = MAX_VERTS * 6 / 4; // At most 6 verts per quad

  // These values allow only one quad at a time (there's no generalized polygon rendering available
  // in Surface yet that would use more than 4 points / 2 triangles).
  private static final int MAX_VERTS = 4;
  private static final int MAX_ELEMS = 6;

  protected final HtmlGLContext ctx;
  protected final WebGLRenderingContext gl;

  protected WebGLProgram program;
  protected WebGLUniformLocation uScreenSizeLoc;
  protected int aMatrix, aTranslation, aPosition, aTexture;

  protected WebGLBuffer vertexBuffer, elementBuffer;

  protected Float32Array vertexData = Float32Array.create(VERTEX_SIZE * MAX_VERTS);
  protected Uint16Array elementData = Uint16Array.create(MAX_ELEMS);
  protected int vertexOffset, elementOffset;

  protected HtmlGLShader(HtmlGLContext ctx, String fragmentShader) {
    this.ctx = ctx;
    this.gl = ctx.gl;

    // Compile the shader.
    program = WebGLUtil.createShaderProgram(gl, VERTEX_SHADER, fragmentShader);

    // glGet*() calls are slow; determine locations once.
    uScreenSizeLoc = gl.getUniformLocation(program, "u_ScreenSize");
    aMatrix = gl.getAttribLocation(program, "a_Matrix");
    aTranslation = gl.getAttribLocation(program, "a_Translation");
    aPosition = gl.getAttribLocation(program, "a_Position");
    aTexture = gl.getAttribLocation(program, "a_Texture");

    // Create the vertex and index buffers.
    vertexBuffer = gl.createBuffer();
    elementBuffer = gl.createBuffer();
  }

  protected boolean prepare() {
    if (ctx.useShader(this)) {
      gl.useProgram(program);
      gl.uniform2fv(uScreenSizeLoc, new float[] { ctx.screenWidth, ctx.screenHeight });
      gl.bindBuffer(ARRAY_BUFFER, vertexBuffer);
      gl.bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer);

      gl.enableVertexAttribArray(aMatrix);
      gl.enableVertexAttribArray(aTranslation);
      gl.enableVertexAttribArray(aPosition);
      if (aTexture != -1) {
        gl.enableVertexAttribArray(aTexture);
      }

      gl.vertexAttribPointer(aMatrix, 4, FLOAT, false, 40, 0);
      gl.vertexAttribPointer(aTranslation, 2, FLOAT, false, 40, 16);
      gl.vertexAttribPointer(aPosition, 2, FLOAT, false, 40, 24);
      if (aTexture != -1) {
        gl.vertexAttribPointer(aTexture, 2, FLOAT, false, 40, 32);
      }

      return true;
    }
    return false;
  }

  @Override
  public void flush() {
    if (vertexOffset == 0) {
      return;
    }

    // TODO(jgw): Change this back. It only works because we've limited MAX_VERTS, which only
    // works because there are no >4 vertex draws happening.
    // gl.bufferData(ARRAY_BUFFER, vertexData.subarray(0, vertexOffset), STREAM_DRAW);
    // gl.bufferData(ELEMENT_ARRAY_BUFFER, elementData.subarray(0, elementOffset), STREAM_DRAW);
    gl.bufferData(ARRAY_BUFFER, vertexData, STREAM_DRAW);
    gl.bufferData(ELEMENT_ARRAY_BUFFER, elementData, STREAM_DRAW);

    gl.drawElements(TRIANGLE_STRIP, elementOffset, UNSIGNED_SHORT, 0);
    vertexOffset = elementOffset = 0;
  }

  @Override
  public int beginPrimitive(int vertexCount, int elemCount) {
    int vertIdx = vertexOffset / VERTEX_SIZE;
    if ((vertIdx + vertexCount > MAX_VERTS) || (elementOffset + elemCount > MAX_ELEMS)) {
      flush();
      return 0;
    }
    return vertIdx;
  }

  @Override
  public void buildVertex(InternalTransform local, float dx, float dy) {
    buildVertex(local, dx, dy, 0, 0);
  }

  @Override
  public void buildVertex(InternalTransform local, float dx, float dy, float sx, float sy) {
    vertexData.set(((HtmlInternalTransform)local).matrix(), vertexOffset);
    vertexData.set(vertexOffset + 6, dx);
    vertexData.set(vertexOffset + 7, dy);
    vertexData.set(vertexOffset + 8, sx);
    vertexData.set(vertexOffset + 9, sy);
    vertexOffset += VERTEX_SIZE;
  }

  @Override
  public void addElement(int index) {
    elementData.set(elementOffset++, index);
  }
}
