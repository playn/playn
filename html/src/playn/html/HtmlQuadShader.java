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
import com.google.gwt.typedarrays.client.Uint16Array;
import com.google.gwt.webgl.client.WebGLBuffer;
import com.google.gwt.webgl.client.WebGLProgram;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLTexture;
import com.google.gwt.webgl.client.WebGLUniformLocation;
import com.google.gwt.webgl.client.WebGLUtil;
import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

import playn.core.InternalTransform;
import playn.core.gl.GLShader;

abstract class HtmlQuadShader implements GLShader
{
  static class Texture extends HtmlQuadShader implements GLShader.Texture {
    private final WebGLUniformLocation uTexture;
    private final WebGLUniformLocation uAlpha;
    private WebGLTexture lastTex;
    private float lastAlpha;

    Texture(HtmlGLContext ctx) {
      super(ctx, GLShader.TEX_FRAG_SHADER);
      uTexture = gl.getUniformLocation(program, "u_Texture");
      uAlpha = gl.getUniformLocation(program, "u_Alpha");
    }

    @Override
    public void prepare(Object texObj, float alpha,  int fbufWidth, int fbufHeight) {
      boolean wasntAlreadyActive = super.prepare(this, fbufWidth, fbufHeight);
      if (wasntAlreadyActive) {
        gl.activeTexture(TEXTURE0);
        gl.uniform1i(uTexture, 0);
      }

      WebGLTexture tex = (WebGLTexture) texObj;
      if (wasntAlreadyActive || tex != lastTex || alpha != lastAlpha) {
        flush();
        gl.uniform1f(uAlpha, alpha);
        lastAlpha = alpha;
        lastTex = tex;
      }
    }

    @Override
    public void flush() {
      gl.bindTexture(TEXTURE_2D, lastTex);
      super.flush();
    }
  }

  static class Color extends HtmlQuadShader implements GLShader.Color {
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

    @Override
    public void prepare(int color, float alpha, int fbufWidth, int fbufHeight) {
      boolean wasntAlreadyActive = super.prepare(this, fbufWidth, fbufHeight);
      if (wasntAlreadyActive || color != lastColor || alpha != lastAlpha) {
        flush();
        gl.uniform1f(uAlpha, alpha);
        lastAlpha = alpha;
        colors.set(0, (float)((color >> 16) & 0xff) / 255);
        colors.set(1, (float)((color >> 8) & 0xff) / 255);
        colors.set(2, (float)((color >> 0) & 0xff) / 255);
        colors.set(3, (float)((color >> 24) & 0xff) / 255);
        gl.uniform4fv(uColor, colors);
        lastColor = color;
      }
    }
  }

  private static final int VERTICES_PER_QUAD = 4;
  private static final int ELEMENTS_PER_QUAD = 6;
  private static final int VERTEX_SIZE = 3; // 3 floats per vertex
  private static final int MATRIX_SIZE = 16; // 4x4 matrix

  protected final WebGLRenderingContext gl;
  private final HtmlGLContext ctx;
  protected final WebGLProgram program;
  private final WebGLUniformLocation dataMatrixLocation;

  private final WebGLBuffer vertexBuffer, elementBuffer;
  private final Float32Array verts;
  private final Uint16Array elems;

  private final HtmlInternalTransform tempLocal = new HtmlInternalTransform();
  private final Float32Array dataMatrix;
  private final int aVertices;
  private final int maxQuads;
  private int quadCounter;

  HtmlQuadShader(HtmlGLContext ctx, String fragmentShader) {
    this.ctx = ctx;
    this.gl = ctx.gl;

    maxQuads = Math.round(gl.getParameteri(MAX_VERTEX_UNIFORM_VECTORS) / (float)MATRIX_SIZE);
    dataMatrix = Float32Array.create(maxQuads*MATRIX_SIZE); // Array 4x4 matrix

    HtmlPlatform.log.debug("Using quad-at-a-time shaders, maxQuads: " + maxQuads);

    // compile the shader and get our uniform and attribute
    program = WebGLUtil.createShaderProgram(gl, GLShader.QUAD_VERTEX_SHADER, fragmentShader);
    dataMatrixLocation = gl.getUniformLocation(program, "dataMatrix");
    aVertices = gl.getAttribLocation(program, "vertex");

    // create our stock supply of unit quads and stuff them into our buffers
    verts = Float32Array.create(maxQuads*VERTICES_PER_QUAD*VERTEX_SIZE);
    elems = Uint16Array.create(maxQuads*ELEMENTS_PER_QUAD);
    int quadStart = 0, quadSize = VERTEX_SIZE*VERTICES_PER_QUAD;
    float[] tv1 = {0.0f, 0.0f}, tv2 = {1.0f, 0.0f}, tv3 = {0.0f, 1.0f}, tv4 = {1.0f, 1.0f};
    for (int ii = 0; ii < maxQuads; ii++) {
      verts.set(tv1, quadStart + 0 * VERTEX_SIZE); verts.set(quadStart + 1 * VERTEX_SIZE - 1, ii);
      verts.set(tv2, quadStart + 1 * VERTEX_SIZE); verts.set(quadStart + 2 * VERTEX_SIZE - 1, ii);
      verts.set(tv3, quadStart + 2 * VERTEX_SIZE); verts.set(quadStart + 3 * VERTEX_SIZE - 1, ii);
      verts.set(tv4, quadStart + 3 * VERTEX_SIZE); verts.set(quadStart + 4 * VERTEX_SIZE - 1, ii);

      elems.set(ii*ELEMENTS_PER_QUAD + 0, ii*VERTICES_PER_QUAD + 0);
      elems.set(ii*ELEMENTS_PER_QUAD + 1, ii*VERTICES_PER_QUAD + 1);
      elems.set(ii*ELEMENTS_PER_QUAD + 2, ii*VERTICES_PER_QUAD + 2);
      elems.set(ii*ELEMENTS_PER_QUAD + 3, ii*VERTICES_PER_QUAD + 1);
      elems.set(ii*ELEMENTS_PER_QUAD + 4, ii*VERTICES_PER_QUAD + 3);
      elems.set(ii*ELEMENTS_PER_QUAD + 5, ii*VERTICES_PER_QUAD + 2);

      quadStart += quadSize;
    }
    vertexBuffer = gl.createBuffer();
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer);
    gl.bufferData(ARRAY_BUFFER, verts, STATIC_DRAW);
    elementBuffer = gl.createBuffer();
    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer);
    gl.bufferData(ELEMENT_ARRAY_BUFFER, elems, STATIC_DRAW);
  }

  @Override
  public void addQuad(InternalTransform local,
                      float x1, float y1, float sx1, float sy1,
                      float x2, float y2, float sx2, float sy2,
                      float x3, float y3, float sx3, float sy3,
                      float x4, float y4, float sx4, float sy4) {
    float dw = x2 - x1;
    float dh = y3 - y1;
    float sw = sx2 - sx1;
    float sh = sy3 - sy1;

    float m00 = local.m00();
    float m01 = local.m01();
    float m10 = local.m10();
    float m11 = local.m11();

    tempLocal.setTransform(m00*dw, m01*dw, m10*dh, m11*dh,
                           local.tx() + m00*x1 + m10*y1, local.ty() + m01*x1 + m11*y1);

    int quadStart = quadCounter*MATRIX_SIZE;
    dataMatrix.set(tempLocal.matrix(), quadStart);
    dataMatrix.set(quadStart + 6, sx1);
    dataMatrix.set(quadStart + 7, sy1);
    dataMatrix.set(quadStart + 8, sw);
    dataMatrix.set(quadStart + 9, sh);
    quadCounter++;

    if (quadCounter >= maxQuads) {
      flush();
    }
  }

  @Override
  public void addQuad(InternalTransform local,
                      float x1, float y1, float x2, float y2,
                      float x3, float y3, float x4, float y4) {
    addQuad(local, x1, y1, 0, 0, x2, y2, 0, 0, x3, y3, 0, 0, x4, y4, 0, 0);
  }

  @Override
  public void addTriangles(InternalTransform local, float[] xys, float texWidth, float texHeight,
                           int[] indices) {
    throw new UnsupportedOperationException("Should only be used for quads");
  }

  @Override
  public void addTriangles(InternalTransform local, float[] xys, float[] sxys, int[] indices) {
    throw new UnsupportedOperationException("Should only be used for quads");
  }

  @Override
  public void flush() {
    if (quadCounter == 0) {
      return;
    }

    gl.uniformMatrix4fv(dataMatrixLocation, false, dataMatrix.subarray(0, quadCounter*MATRIX_SIZE));
    gl.drawElements(TRIANGLES, ELEMENTS_PER_QUAD*quadCounter, UNSIGNED_SHORT, 0);
    quadCounter = 0;
  }

  protected boolean prepare(GLShader shader, int fbufWidth, int fbufHeight) {
    if (!ctx.useShader(shader))
      return false;

    gl.useProgram(program);
    dataMatrix.set(10, fbufWidth);
    dataMatrix.set(11, fbufHeight);

    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer);
    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, elementBuffer);
    ctx.checkGLError("Shader.prepare BindBuffer");

    gl.enableVertexAttribArray(aVertices);
    gl.vertexAttribPointer(aVertices, VERTEX_SIZE, FLOAT, false, 4*VERTEX_SIZE, 0);  // 4 bytes
    ctx.checkGLError("Shader.prepare AttribPointer");
    return true;
  }
}
