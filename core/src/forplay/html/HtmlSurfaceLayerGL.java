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

import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLTexture;

import forplay.core.Surface;
import forplay.core.SurfaceLayer;
import forplay.core.Transform;

class HtmlSurfaceLayerGL extends HtmlLayerGL implements SurfaceLayer {

  private WebGLFramebuffer fbuf;
  private WebGLTexture tex;
  private HtmlSurfaceGL surface;
  private final int width;
  private final int height;

  HtmlSurfaceLayerGL(HtmlGraphicsGL gfx, int width, int height) {
    super(gfx);

    this.width = width;
    this.height = height;
    gfx.flush();

    WebGLRenderingContext gl = gfx.gl;
    tex = gfx.createTexture(false, false);
    gl.texImage2D(TEXTURE_2D, 0, RGBA, width, height, 0, RGBA, UNSIGNED_BYTE, null);

    fbuf = gl.createFramebuffer();
    gl.bindFramebuffer(FRAMEBUFFER, fbuf);
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, tex, 0);

    gl.bindTexture(TEXTURE_2D, null);
    gfx.bindFramebuffer();

    surface = new HtmlSurfaceGL(gfx, fbuf, width, height);
  }

  @Override
  public void destroy() {
    super.destroy();

    gfx.destroyTexture(tex);
    gfx.gl.deleteFramebuffer(fbuf);

    tex = null;
    fbuf = null;
    surface = null;
  }

  @Override
  public Surface surface() {
    return surface;
  }

  @Override
  void paint(WebGLRenderingContext gl, Transform parentTransform, float parentAlpha) {
    if (!visible()) return;

    // Draw this layer to the screen upside-down, because its contents are flipped
    // (This happens because it uses the same vertex program as everything else,
    //  which flips vertically to put the origin at the top-left).
    gfx.drawTexture(tex, width, height, localTransform(parentTransform), 0, height, width, -height,
        false, false, parentAlpha * alpha);
  }
}
