/**
 * Copyright 2010 The ForPlay Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.html;

import static forplay.core.ForPlay.log;

import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.typedarrays.client.Float32Array;
import com.google.gwt.typedarrays.client.Uint16Array;
import com.google.gwt.webgl.client.WebGLBuffer;
import com.google.gwt.webgl.client.WebGLContextAttributes;
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLProgram;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLTexture;
import com.google.gwt.webgl.client.WebGLUniformLocation;
import com.google.gwt.webgl.client.WebGLUtil;

import forplay.core.CanvasLayer;
import forplay.core.GroupLayer;
import forplay.core.Image;
import forplay.core.ImageLayer;
import forplay.core.SurfaceLayer;
import forplay.core.Transform;

class HtmlGraphicsGL extends HtmlGraphics {

  private static final int VERTEX_SIZE = 10;              // 10 floats per vertex

// TODO(jgw): Re-enable longer element buffers once we figure out why they're causing weird
// performance degradation.
//  private static final int MAX_VERTS = 400;               // 100 quads
//  private static final int MAX_ELEMS = MAX_VERTS * 6 / 4; // At most 6 verts per quad

// These values allow only one quad at a time (there's no generalized polygon rendering available
// in Surface yet that would use more than 4 points / 2 triangles).
  private static final int MAX_VERTS = 4;
  private static final int MAX_ELEMS = 6;

  private class Shader {
    WebGLProgram program;
    WebGLUniformLocation uScreenSizeLoc;
    int aMatrix, aTranslation, aPosition, aTexture;

    WebGLBuffer buffer, indexBuffer;

    Float32Array vertexData = Float32Array.create(VERTEX_SIZE * MAX_VERTS);
    Uint16Array elementData = Uint16Array.create(MAX_ELEMS);
    int vertexOffset, elementOffset;

    Shader(String fragmentShader) {
      // Compile the shader.
      String vertexShader = Shaders.INSTANCE.vertexShader().getText();
      program = WebGLUtil.createShaderProgram(gl, vertexShader, fragmentShader);

      // glGet*() calls are slow; determine locations once.
      uScreenSizeLoc = gl.getUniformLocation(program, "u_ScreenSize");
      aMatrix = gl.getAttribLocation(program, "a_Matrix");
      aTranslation = gl.getAttribLocation(program, "a_Translation");
      aPosition = gl.getAttribLocation(program, "a_Position");
      aTexture = gl.getAttribLocation(program, "a_Texture");

      // Create the vertex and index buffers.
      buffer = gl.createBuffer();
      indexBuffer = gl.createBuffer();
    }

    boolean prepare() {
      if (useShader(this)) {
        gl.useProgram(program);
        gl.uniform2fv(uScreenSizeLoc, new float[] { screenWidth, screenHeight });
        gl.bindBuffer(ARRAY_BUFFER, buffer);
        gl.bindBuffer(ELEMENT_ARRAY_BUFFER, indexBuffer);

        gl.enableVertexAttribArray(aMatrix);
        gl.enableVertexAttribArray(aTranslation);
        gl.enableVertexAttribArray(aPosition);
        gl.enableVertexAttribArray(aTexture);

        gl.vertexAttribPointer(aMatrix, 4, FLOAT, false, 40, 0);
        gl.vertexAttribPointer(aTranslation, 2, FLOAT, false, 40, 16);
        gl.vertexAttribPointer(aPosition, 2, FLOAT, false, 40, 24);
        gl.vertexAttribPointer(aTexture, 2, FLOAT, false, 40, 32);

        return true;
      }
      return false;
    }

    void flush() {
      if (vertexOffset == 0) {
        return;
      }

      gl.bufferData(ARRAY_BUFFER, vertexData.subarray(0, vertexOffset), STREAM_DRAW);
      gl.bufferData(ELEMENT_ARRAY_BUFFER, elementData.subarray(0, elementOffset), STREAM_DRAW);

      gl.drawElements(TRIANGLES, elementOffset, UNSIGNED_SHORT, 0);
      vertexOffset = elementOffset = 0;
    }

    int beginPrimitive(int vertexCount, int elemCount) {
      int vertIdx = vertexOffset / VERTEX_SIZE;
      if ((vertIdx + vertexCount > MAX_VERTS) || (elementOffset + elemCount > MAX_ELEMS)) {
        flush();
        return 0;
      }
      return vertIdx;
    }

    void buildVertex(Transform local, float dx, float dy) {
      buildVertex(local, dx, dy, 0, 0);
    }

    void buildVertex(Transform local, float dx, float dy, float sx, float sy) {
      vertexData.set(vertexOffset + 0, local.m00());
      vertexData.set(vertexOffset + 1, local.m01());
      vertexData.set(vertexOffset + 2, local.m10());
      vertexData.set(vertexOffset + 3, local.m11());
      vertexData.set(vertexOffset + 4, local.tx());
      vertexData.set(vertexOffset + 5, local.ty());
      vertexData.set(vertexOffset + 6, dx);
      vertexData.set(vertexOffset + 7, dy);
      vertexData.set(vertexOffset + 8, sx);
      vertexData.set(vertexOffset + 9, sy);
      vertexOffset += VERTEX_SIZE;
    }

    void addElement(int index) {
      elementData.set(elementOffset++, index);
    }
  }

  private class TextureShader extends Shader {
    WebGLUniformLocation uTexture;
    WebGLTexture lastTex;

    TextureShader() {
      super(Shaders.INSTANCE.texFragmentShader().getText());
      uTexture = gl.getUniformLocation(program, "u_Texture");
    }

    @Override
    void flush() {
      gl.bindTexture(TEXTURE_2D, lastTex);
      super.flush();
    }

    void prepare(WebGLTexture tex) {
      if (super.prepare()) {
        gl.activeTexture(TEXTURE0);
        gl.uniform1i(uTexture, 0);
      }

      if (tex == lastTex) {
        return;
      }
      flush();

      lastTex = tex;
    }
  }

  private class ColorShader extends Shader {
    WebGLUniformLocation uColorLoc;
    Float32Array colors = Float32Array.create(4);
    int lastColor;

    ColorShader() {
      super(Shaders.INSTANCE.colorFragmentShader().getText());
      uColorLoc = gl.getUniformLocation(program, "u_Color");
    }

    void prepare(int color) {
      super.prepare();

      if (color == lastColor) {
        return;
      }
      flush();

      setColor(color);
    }

    private void setColor(int color) {
      // ABGR.
      colors.set(3, (float)((color >> 24) & 0xff) / 255);
      colors.set(0, (float)((color >> 16) & 0xff) / 255);
      colors.set(1, (float)((color >> 8) & 0xff) / 255);
      colors.set(2, (float)((color >> 0) & 0xff) / 255);
      gl.uniform4fv(uColorLoc, colors);

      lastColor = color;
    }
  }

  WebGLRenderingContext gl;

  private WebGLFramebuffer lastFBuf;
  private int screenWidth, screenHeight;

  private HtmlGroupLayerGL rootLayer;
  private CanvasElement canvas;

  // Shaders & Meshes.
  private Shader curShader;
  private TextureShader texShader;
  private ColorShader colorShader;

  // Debug counters.
  private int texCount;

  HtmlGraphicsGL() {
    rootLayer = new HtmlGroupLayerGL(this);

    createCanvas();
    initGL();

    texShader = new TextureShader();
    colorShader = new ColorShader();

    setSize(HtmlPlatform.DEFAULT_WIDTH, HtmlPlatform.DEFAULT_HEIGHT);
  }

  @Override
  public CanvasLayer createCanvasLayer(int width, int height) {
    return new HtmlCanvasLayerGL(this, width, height);
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new HtmlGroupLayerGL(this);
  }

  @Override
  public ImageLayer createImageLayer() {
    return new HtmlImageLayerGL(this);
  }

  @Override
  public ImageLayer createImageLayer(Image img) {
    return new HtmlImageLayerGL(this, img);
  }

  @Override
  public SurfaceLayer createSurfaceLayer(int width, int height) {
    return new HtmlSurfaceLayerGL(this, width, height);
  }

  @Override
  public int height() {
    return canvas.getOffsetHeight();
  }

  @Override
  public HtmlGroupLayerGL rootLayer() {
    return rootLayer;
  }

  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height);

    canvas.setWidth(width);
    canvas.setHeight(height);
    bindFramebuffer(null, width, height, true);
  }

  @Override
  public int width() {
    return canvas.getOffsetWidth();
  }

  void bindFramebuffer() {
    bindFramebuffer(null, canvas.getWidth(), canvas.getHeight());
  }

  void bindFramebuffer(WebGLFramebuffer fbuf, int width, int height) {
    bindFramebuffer(fbuf, width, height, false);
  }

  void bindFramebuffer(WebGLFramebuffer fbuf, int width, int height, boolean force) {
    if (force || lastFBuf != fbuf) {
      flush();

      lastFBuf = fbuf;
      gl.bindFramebuffer(FRAMEBUFFER, fbuf);
      gl.viewport(0, 0, width, height);
      screenWidth = width;
      screenHeight = height;
    }
  }

  WebGLTexture createTexture(boolean repeatX, boolean repeatY) {
    WebGLTexture tex = gl.createTexture();
    gl.bindTexture(TEXTURE_2D, tex);
    gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR);
    gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR);
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, repeatX ? REPEAT : CLAMP_TO_EDGE);
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, repeatY ? REPEAT : CLAMP_TO_EDGE);
    ++texCount;
    return tex;
  }

  void destroyTexture(WebGLTexture tex) {
    gl.deleteTexture(tex);
    --texCount;
  }

  void updateLayers() {
    bindFramebuffer(null, canvas.getWidth(), canvas.getHeight());

    // Clear to transparent.
    gl.clear(COLOR_BUFFER_BIT);

    // Paint all the layers.
    rootLayer.paint(gl, Transform.IDENTITY);

    // Guarantee a flush.
    useShader(null);
  }

  public void updateTexture(WebGLTexture tex, Element img) {
    gl.bindTexture(TEXTURE_2D, tex);
    gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, img.<ImageElement>cast());
  }

  void drawTexture(WebGLTexture tex, float texWidth, float texHeight, Transform local, float dw, float dh, boolean repeatX, boolean repeatY) {
    drawTexture(tex, texWidth, texHeight, local, 0, 0, dw, dh, repeatX, repeatY);
  }

  void drawTexture(WebGLTexture tex, float texWidth, float texHeight, Transform local, float dx, float dy, float dw, float dh, boolean repeatX, boolean repeatY) {
    float sw = repeatX ? dw : texWidth, sh = repeatY ? dh : texHeight;
    drawTexture(tex, texWidth, texHeight, local, dx, dy, dw, dh, 0, 0, sw, sh);
  }

  void drawTexture(WebGLTexture tex, float texWidth, float texHeight, Transform local,
      float dx, float dy, float dw, float dh, float sx, float sy, float sw, float sh) {
    texShader.prepare(tex);

    sx /= texWidth;  sw /= texWidth;
    sy /= texHeight; sh /= texHeight;

    int idx = texShader.beginPrimitive(4, 6);
    texShader.buildVertex(local, dx,      dy,      sx,      sy);
    texShader.buildVertex(local, dx + dw, dy,      sx + sw, sy);
    texShader.buildVertex(local, dx,      dy + dh, sx,      sy + sh);
    texShader.buildVertex(local, dx + dw, dy + dh, sx + sw, sy + sh);

    texShader.addElement(idx + 0); texShader.addElement(idx + 1); texShader.addElement(idx + 2);
    texShader.addElement(idx + 1); texShader.addElement(idx + 3); texShader.addElement(idx + 2);
  }

  void fillRect(Transform local, float dx, float dy, float dw, float dh, float texWidth, float texHeight, WebGLTexture tex) {
    texShader.prepare(tex);

    float sx = dx / texWidth, sy = dy / texHeight;
    float sw = dw / texWidth, sh = dh / texHeight;

    int idx = texShader.beginPrimitive(4, 6);
    texShader.buildVertex(local, dx,      dy,      sx,      sy);
    texShader.buildVertex(local, dx + dw, dy,      sx + sw, sy);
    texShader.buildVertex(local, dx,      dy + dh, sx,      sy + sh);
    texShader.buildVertex(local, dx + dw, dy + dh, sx + sw, sy + sh);

    texShader.addElement(idx + 0); texShader.addElement(idx + 1); texShader.addElement(idx + 2);
    texShader.addElement(idx + 1); texShader.addElement(idx + 3); texShader.addElement(idx + 2);
  }

  void fillRect(Transform local, float dx, float dy, float dw, float dh, int color) {
    colorShader.prepare(color);

    int idx = colorShader.beginPrimitive(4, 6);
    colorShader.buildVertex(local, dx,      dy);
    colorShader.buildVertex(local, dx + dw, dy);
    colorShader.buildVertex(local, dx,      dy + dh);
    colorShader.buildVertex(local, dx + dw, dy + dh);

    colorShader.addElement(idx + 0); colorShader.addElement(idx + 1); colorShader.addElement(idx + 2);
    colorShader.addElement(idx + 1); colorShader.addElement(idx + 3); colorShader.addElement(idx + 2);
  }

  void fillPoly(Transform local, float[] positions, int color) {
    colorShader.prepare(color);

    int idx = colorShader.beginPrimitive(4, 6);
    int points = positions.length / 2;
    for (int i = 0; i < points; ++i) {
      float dx = positions[i * 2];
      float dy = positions[i * 2 + 1];
      colorShader.buildVertex(local, dx, dy);
    }

    int a = idx + 0, b = idx + 1, c = idx + 2;
    int tris = points - 2;
    for (int i = 0; i < tris; ++i) {
      colorShader.addElement(a); colorShader.addElement(b); colorShader.addElement(c);
      a = c;
      b = a + 1;
      c = (i == tris - 2) ? idx : b + 1;
    }
  }

  @Override
  Element getRootElement() {
    return canvas;
  }

  void flush() {
    if (curShader != null) {
      curShader.flush();
      curShader = null;
    }
  }

  private void createCanvas() {
    canvas = Document.get().createCanvasElement();
    rootElement.appendChild(canvas);
  }

  private void initGL() {
    WebGLContextAttributes attrs = WebGLContextAttributes.create();
    attrs.setAlpha(false);
    attrs.setPremultipliedAlpha(false);
    attrs.setAntialias(true);
    if (!tryCreateContext(attrs)) {
      giveUp();
    }

    gl.disable(CULL_FACE);
    gl.enable(BLEND);
    gl.blendEquation(FUNC_ADD);
    gl.blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA);
  }

  private boolean tryCreateContext(WebGLContextAttributes attrs) {
    // Try to create a context. If this returns null, then the browser doesn't support WebGL
    // on this machine.
    gl = WebGLRenderingContext.getContext(canvas, attrs);
    if (gl == null) {
      return false;
    }

    // Some systems seem to have a problem where they return a valid context, but it's in an error
    // static initially. We give up and fall back to dom/canvas in this case, because nothing seems
    // to work properly.
    return (gl.getError() == NO_ERROR);
  }

  private void giveUp() {
    // Give up. HtmlPlatform will catch the exception and fall back to dom/canvas.
    rootElement.removeChild(canvas);
    throw new RuntimeException();
  }

  private boolean useShader(Shader shader) {
    if (curShader != shader) {
      flush();
      curShader = shader;
      return true;
    }
    return false;
  }

  @SuppressWarnings("unused")
  private void printArray(String prefix, Float32Array arr, int length) {
    StringBuffer buf = new StringBuffer();
    buf.append(prefix + ": [");
    for (int i = 0; i < length; ++i) {
      buf.append(arr.get(i) + " ");
    }
    buf.append("]");
    System.out.println(buf);
  }

  @SuppressWarnings("unused")
  private void printArray(String prefix, Uint16Array arr, int length) {
    StringBuffer buf = new StringBuffer();
    buf.append(prefix + ": [");
    for (int i = 0; i < length; ++i) {
      buf.append(arr.get(i) + " ");
    }
    buf.append("]");
    System.out.println(buf);
  }
}
