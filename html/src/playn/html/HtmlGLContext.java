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
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLTexture;

import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

import playn.core.InternalTransform;
import playn.core.gl.GLContext;
import playn.core.gl.GLShader;
import playn.core.gl.LayerGL;

/**
 * Implements {@link GLContext} via WebGL.
 */
public class HtmlGLContext extends GLContext
{
  final WebGLRenderingContext gl;

  private final CanvasElement canvas;
  private final GLShader.Texture texQuadShader;
  private final GLShader.Texture texTrisShader;
  private final GLShader.Color colorQuadShader;
  private final GLShader.Color colorTrisShader;

  private WebGLFramebuffer lastFBuf;

  // Debug counters.
  // private int texCount;

  HtmlGLContext(CanvasElement canvas) throws RuntimeException {
    super(1); // no HiDPI on the interwebs
    this.canvas = canvas;

    // Try to create a context. If this returns null, then the browser doesn't support WebGL on
    // this machine.
    this.gl = WebGLRenderingContext.getContext(canvas, null);
    // Some systems seem to have a problem where they return a valid context, but it's in an error
    // state initially. We give up and fall back to Canvas in this case, because nothing seems to
    // work properly.
    if (gl == null || gl.getError() != NO_ERROR) {
      throw new RuntimeException("GL context not created [err=" +
                                 (gl == null ? "null" : gl.getError()) + "]");
    }

    // try basic GL operations to detect failure cases early
    tryBasicGLCalls();

    if (HtmlUrlParameters.checkGLErrors) {
      HtmlPlatform.log.debug("GL error checking enabled.");
    }

    if (HtmlUrlParameters.quadShader) {
      texQuadShader = new HtmlQuadShader.Texture(this);
      colorQuadShader = new HtmlQuadShader.Color(this);
    } else {
      texQuadShader = new HtmlIndexedTrisShader.Texture(this);
      colorQuadShader = new HtmlIndexedTrisShader.Color(this);
    }
    texTrisShader = new HtmlIndexedTrisShader.Texture(this);
    colorTrisShader = new HtmlIndexedTrisShader.Color(this);

    gl.disable(CULL_FACE);
    gl.enable(BLEND);
    gl.blendEquation(FUNC_ADD);
    gl.blendFunc(ONE, ONE_MINUS_SRC_ALPHA);
    gl.pixelStorei(UNPACK_PREMULTIPLY_ALPHA_WEBGL, ONE);
  }

  void preparePaint() {
    // Clear to transparent.
    bindFramebuffer();
    clear(0, 0, 0, 0);
  }

  void paint(LayerGL rootLayer) {
    // Paint all the layers.
    bindFramebuffer();
    rootLayer.paint(HtmlInternalTransform.IDENTITY, 1);
    // Guarantee a flush.
    useShader(null);
  }

  @Override
  public void deleteFramebuffer(Object fbuf) {
    gl.deleteFramebuffer((WebGLFramebuffer) fbuf);
  }

  @Override
  public WebGLTexture createTexture(boolean repeatX, boolean repeatY) {
    WebGLTexture tex = gl.createTexture();
    gl.bindTexture(TEXTURE_2D, tex);
    gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR);
    gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR);
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, repeatX ? REPEAT : CLAMP_TO_EDGE);
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, repeatY ? REPEAT : CLAMP_TO_EDGE);
    // ++texCount;
    return tex;
  }

  @Override
  public WebGLTexture createTexture(int width, int height, boolean repeatX, boolean repeatY) {
    WebGLTexture tex = createTexture(repeatX, repeatY);
    gl.texImage2D(TEXTURE_2D, 0, RGBA, width, height, 0, RGBA, UNSIGNED_BYTE, null);
    return tex;
  }

  @Override
  public void destroyTexture(Object tex) {
    gl.deleteTexture((WebGLTexture)tex);
    // --texCount;
  }

  void updateTexture(WebGLTexture tex, ImageElement img) {
    gl.bindTexture(TEXTURE_2D, tex);
    gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, img);
  }

  @Override
  public void startClipped(int x, int y, int width, int height) {
    flush(); // flush any pending unclipped calls
    gl.scissor(x, curFbufHeight-y-height, width, height);
    gl.enable(SCISSOR_TEST);
  }

  @Override
  public void endClipped() {
    flush(); // flush our clipped calls with SCISSOR_TEST still enabled
    gl.disable(SCISSOR_TEST);
  }

  @Override
  public void clear(float red, float green, float blue, float alpha) {
    gl.clearColor(red, green, blue, alpha);
    gl.clear(COLOR_BUFFER_BIT);
  }

  @Override
  public void checkGLError(String op) {
    if (HtmlUrlParameters.checkGLErrors) {
      int error;
      while ((error = gl.getError()) != NO_ERROR) {
        HtmlPlatform.log.error(op + ": glError " + error);
      }
    }
  }

  @Override
  public InternalTransform createTransform() {
    return new HtmlInternalTransform();
  }

  @Override
  protected Object defaultFrameBuffer() {
    return null;
  }

  @Override
  protected Object createFramebufferImpl(Object tex) {
    WebGLFramebuffer fbuf = gl.createFramebuffer();
    gl.bindFramebuffer(FRAMEBUFFER, fbuf);
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, (WebGLTexture) tex, 0);
    return fbuf;
  }

  @Override
  protected void bindFramebufferImpl(Object fbuf, int width, int height) {
    gl.bindFramebuffer(FRAMEBUFFER, (WebGLFramebuffer) fbuf);
    gl.viewport(0, 0, width, height);
  }

  @Override
  protected GLShader.Texture quadTexShader() {
    return texQuadShader;
  }
  @Override
  protected GLShader.Texture trisTexShader() {
    return texTrisShader;
  }
  @Override
  protected GLShader.Color quadColorShader() {
    return colorQuadShader;
  }
  @Override
  protected GLShader.Color trisColorShader() {
    return colorTrisShader;
  }

  private void tryBasicGLCalls() throws RuntimeException {
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
    clear(1, 1, 1, 1);
    int err = gl.getError();
    if (err != NO_ERROR) {
      throw new RuntimeException("Read back GL test failed to clear color (error " + err + ")");
    }
    Uint8Array pixelData = Uint8Array.create(4);
    gl.readPixels(0, 0, 1, 1, RGBA, UNSIGNED_BYTE, pixelData);
    if (pixelData.get(0) != 255 || pixelData.get(1) != 255 || pixelData.get(2) != 255) {
      throw new RuntimeException("Read back GL test failed to read back correct color");
    }
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
