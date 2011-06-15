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

import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.typedarrays.client.Float32Array;
import com.google.gwt.typedarrays.client.Int32Array;
import com.google.gwt.typedarrays.client.Uint16Array;
import com.google.gwt.typedarrays.client.Uint8Array;
import com.google.gwt.webgl.client.WebGLBuffer;
import com.google.gwt.webgl.client.WebGLContextAttributes;
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLProgram;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLTexture;
import com.google.gwt.webgl.client.WebGLUniformLocation;
import com.google.gwt.webgl.client.WebGLUtil;

import forplay.core.CanvasLayer;
import forplay.core.ForPlay;
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

    void flush() {
      if (vertexOffset == 0) {
        return;
      }

      // TODO(jgw): Change this back. It only works because we've limited MAX_VERTS, which only
      // works because there are no >4 vertex draws happening.
      // gl.bufferData(ARRAY_BUFFER, vertexData.subarray(0, vertexOffset), STREAM_DRAW);
      // gl.bufferData(ELEMENT_ARRAY_BUFFER, elementData.subarray(0, elementOffset), STREAM_DRAW);
      gl.bufferData(ARRAY_BUFFER, vertexData, STREAM_DRAW);
      gl.bufferData(ELEMENT_ARRAY_BUFFER, elementData, STREAM_DRAW);

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
    WebGLUniformLocation uAlpha;
    WebGLTexture lastTex;
    float lastAlpha;

    TextureShader() {
      super(Shaders.INSTANCE.texFragmentShader().getText());
      uTexture = gl.getUniformLocation(program, "u_Texture");
      uAlpha = gl.getUniformLocation(program, "u_Alpha");
    }

    @Override
    void flush() {
      gl.bindTexture(TEXTURE_2D, lastTex);
      super.flush();
    }

    void prepare(WebGLTexture tex, float alpha) {
      if (super.prepare()) {
        gl.activeTexture(TEXTURE0);
        gl.uniform1i(uTexture, 0);
      }

      if (tex == lastTex && alpha == lastAlpha) {
        return;
      }
      flush();

      gl.uniform1f(uAlpha, alpha);
      lastAlpha = alpha;
      lastTex = tex;
    }
  }

  private class ColorShader extends Shader {
    WebGLUniformLocation uColor;
    WebGLUniformLocation uAlpha;
    Float32Array colors = Float32Array.create(4);
    int lastColor;
    float lastAlpha;

    ColorShader() {
      super(Shaders.INSTANCE.colorFragmentShader().getText());
      uColor = gl.getUniformLocation(program, "u_Color");
      uAlpha = gl.getUniformLocation(program, "u_Alpha");
    }

    void prepare(int color, float alpha) {
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
    rootLayer.paint(gl, Transform.IDENTITY, 1);

    // Guarantee a flush.
    useShader(null);
  }

  public void updateTexture(WebGLTexture tex, Element img) {
    gl.bindTexture(TEXTURE_2D, tex);
    gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, img.<ImageElement>cast());
  }

  void drawTexture(WebGLTexture tex, float texWidth, float texHeight, Transform local, float dw,
      float dh, boolean repeatX, boolean repeatY, float alpha) {
    drawTexture(tex, texWidth, texHeight, local, 0, 0, dw, dh, repeatX, repeatY, alpha);
  }

  void drawTexture(WebGLTexture tex, float texWidth, float texHeight, Transform local, float dx,
      float dy, float dw, float dh, boolean repeatX, boolean repeatY, float alpha) {
    float sw = repeatX ? dw : texWidth, sh = repeatY ? dh : texHeight;
    drawTexture(tex, texWidth, texHeight, local, dx, dy, dw, dh, 0, 0, sw, sh, alpha);
  }

  void drawTexture(WebGLTexture tex, float texWidth, float texHeight, Transform local, float dx,
      float dy, float dw, float dh, float sx, float sy, float sw, float sh, float alpha) {
    texShader.prepare(tex, alpha);

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

  void fillRect(Transform local, float dx, float dy, float dw, float dh, float texWidth,
      float texHeight, WebGLTexture tex, float alpha) {
    texShader.prepare(tex, alpha);

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

  void fillRect(Transform local, float dx, float dy, float dw, float dh, int color, float alpha) {
    colorShader.prepare(color, alpha);

    int idx = colorShader.beginPrimitive(4, 6);
    colorShader.buildVertex(local, dx,      dy);
    colorShader.buildVertex(local, dx + dw, dy);
    colorShader.buildVertex(local, dx,      dy + dh);
    colorShader.buildVertex(local, dx + dw, dy + dh);

    colorShader.addElement(idx + 0);
    colorShader.addElement(idx + 1);
    colorShader.addElement(idx + 2);
    colorShader.addElement(idx + 1);
    colorShader.addElement(idx + 3);
    colorShader.addElement(idx + 2);
  }

  void fillPoly(Transform local, float[] positions, int color, float alpha) {
    colorShader.prepare(color, alpha);

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
    if (!tryCreateContext(null)) {
      giveUp();
    }

    gl.disable(CULL_FACE);
    gl.enable(BLEND);
    gl.blendEquation(FUNC_ADD);
    gl.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, SRC_ALPHA, DST_ALPHA);

    if (!tryBasicGlCalls()) {
      giveUp();
    }
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

  /**
   * Try basic GL operations to detect failure cases early.
   * 
   * @return true if calls succeed, false otherwise.
   */
  private boolean tryBasicGlCalls() {
    int err;

    try {
      // test that our Float32 arrays work (a technique found in other WebGL checks)
      Float32Array testFloat32Array = Float32Array.create(new float[]{0.0f, 1.0f, 2.0f});
      if (testFloat32Array.get(0) != 0.0f || testFloat32Array.get(1) != 1.0f
          || testFloat32Array.get(2) != 2.0f) {
        throw new RuntimeException("Typed Float32Array check failed");
      }

      // test that our Int32 arrays work
      Int32Array testInt32Array = Int32Array.create(new int[]{0, 1, 2});
      if (testInt32Array.get(0) != 0 || testInt32Array.get(1) != 1 || testInt32Array.get(2) != 2) {
        throw new RuntimeException("Typed Int32Array check failed");
      }

      // test that our Uint16 arrays work
      Uint16Array testUint16Array = Uint16Array.create(new int[]{0, 1, 2});
      if (testUint16Array.get(0) != 0 || testUint16Array.get(1) != 1 || 
          testUint16Array.get(2) != 2) {
        throw new RuntimeException("Typed Uint16Array check failed");
      }

      // test that our Uint8 arrays work
      Uint8Array testUint8Array = Uint8Array.create(new int[]{0, 1, 2});
      if (testUint8Array.get(0) != 0 || testUint8Array.get(1) != 1 || testUint8Array.get(2) != 2) {
        throw new RuntimeException("Typed Uint8Array check failed");
      }

      // Perform GL read back test where we paint rgba(1, 1, 1, 1) and then read back that data.
      // (should be 100% opaque white).
      bindFramebuffer();
      gl.clearColor(1, 1, 1, 1);
      err = gl.getError();
      if (err != NO_ERROR) {
        throw new RuntimeException("Read back GL test failed to clear color (error " + err + ")");
      }
      updateLayers();
      Uint8Array pixelData = Uint8Array.create(4);
      gl.readPixels(0, 0, 1, 1, RGBA, UNSIGNED_BYTE, pixelData);
      if (pixelData.get(0) != 255 || pixelData.get(1) != 255 || pixelData.get(2) != 255) {
        throw new RuntimeException("Read back GL test failed to read back correct color");
      }
    } catch (RuntimeException e) {
      ForPlay.log().info("Basic GL check failed: " + e.getMessage());
      return false;
    } catch (Throwable t) {
      ForPlay.log().info("Basic GL check failed with an unknown error: " + t.getMessage());
      return false;
    }

    return true;
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
