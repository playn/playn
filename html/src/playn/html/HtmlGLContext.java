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
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint16Array;
import com.google.gwt.typedarrays.shared.Uint8Array;
import com.google.gwt.webgl.client.ArrayUtils;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

import pythagoras.f.MathUtil;

import playn.core.Image;
import playn.core.InternalTransform;
import playn.core.gl.GL20Context;
import playn.core.gl.GLContext;

/**
 * Implements {@link GLContext} via WebGL.
 */
public class HtmlGLContext extends GL20Context {

  private final WebGLRenderingContext glc;

  HtmlGLContext(HtmlPlatform platform, float scaleFactor, WebGLRenderingContext gl,
                CanvasElement canvas) {
    super(platform, new HtmlGL20(gl), scaleFactor, HtmlUrlParameters.checkGLErrors);
    this.glc = gl;
    // try basic GL operations to detect failure cases early
    tryBasicGLCalls();
    setSize(MathUtil.iceil(canvas.getWidth() / scaleFactor),
            MathUtil.iceil(canvas.getHeight() / scaleFactor));
    init();
    glc.pixelStorei(UNPACK_PREMULTIPLY_ALPHA_WEBGL, ONE);
  }

  void updateTexture(int tex, ImageElement img) {
    gl.glBindTexture(HtmlGL20.GL_TEXTURE_2D, tex);
    glc.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, img);
  }

  @Override
  public InternalTransform createTransform() {
    return new HtmlInternalTransform();
  }

  @Override
  public void texImage2D(Image image, int target, int level, int internalformat, int format,
                         int type) {
    // we can do this more efficiently by passing the image element right to WebGL
    glc.texImage2D(target, level, internalformat, format, type, ((HtmlImage) image).img);
  }

  @Override
  public void texSubImage2D(Image image, int target, int level, int xOffset, int yOffset, int format,
                            int type) {
    // we can do this more efficiently by passing the image element right to WebGL
    glc.texSubImage2D(target, level, xOffset, yOffset, format, type, ((HtmlImage) image).img);
  }

  private void tryBasicGLCalls() throws RuntimeException {
   // test that our Float32 arrays work (a technique found in other WebGL checks)
    Float32Array testFloat32Array = ArrayUtils.createFloat32Array(new float[]{0.0f, 1.0f, 2.0f});
    if (testFloat32Array.get(0) != 0.0f || testFloat32Array.get(1) != 1.0f
        || testFloat32Array.get(2) != 2.0f) {
      throw new RuntimeException("Typed Float32Array check failed");
    }

    // test that our Int32 arrays work
    Int32Array testInt32Array = ArrayUtils.createInt32Array(new int[]{0, 1, 2});
    if (testInt32Array.get(0) != 0 || testInt32Array.get(1) != 1 || testInt32Array.get(2) != 2) {
      throw new RuntimeException("Typed Int32Array check failed");
    }

    // test that our Uint16 arrays work
    Uint16Array testUint16Array = ArrayUtils.createUint16Array(new int[]{0, 1, 2});
    if (testUint16Array.get(0) != 0 || testUint16Array.get(1) != 1 ||
        testUint16Array.get(2) != 2) {
      throw new RuntimeException("Typed Uint16Array check failed");
    }

    // test that our Uint8 arrays work
    Uint8Array testUint8Array = ArrayUtils.createUint8Array(new int[]{0, 1, 2});
    if (testUint8Array.get(0) != 0 || testUint8Array.get(1) != 1 || testUint8Array.get(2) != 2) {
      throw new RuntimeException("Typed Uint8Array check failed");
    }

    // Perform GL read back test where we paint rgba(1, 1, 1, 1) and then read back that data.
    // (should be 100% opaque white).
    bindFramebuffer();
    clear(1, 1, 1, 1);
    int err = glc.getError();
    if (err != NO_ERROR) {
      throw new RuntimeException("Read back GL test failed to clear color (error " + err + ")");
    }
    Uint8Array pixelData = TypedArrays.createUint8Array(4);
    glc.readPixels(0, 0, 1, 1, RGBA, UNSIGNED_BYTE, pixelData);
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
