/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.html;

import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.webgl.client.WebGLRenderingContext;
import com.google.gwt.webgl.client.WebGLTexture;

import forplay.core.Canvas;
import forplay.core.CanvasLayer;
import forplay.core.Transform;

class HtmlCanvasLayerGL extends HtmlLayerGL implements CanvasLayer {

  private final int width;
  private final int height;

  private HtmlCanvas canvas;
  private WebGLTexture tex;

  HtmlCanvasLayerGL(HtmlGraphicsGL gfx, int width, int height) {
    super(gfx);

    this.width = width;
    this.height = height;

    CanvasElement canvas = Document.get().createCanvasElement();
    canvas.setWidth(width);
    canvas.setHeight(height);

    this.canvas = new HtmlCanvas(canvas, width, height);
    tex = gfx.createTexture(false, false);
  }

  @Override
  public Canvas canvas() {
    return canvas;
  }

  @Override
  public void destroy() {
    super.destroy();
    gfx.destroyTexture(tex);
    tex = null;
    canvas = null;
  }

  @Override
  void paint(WebGLRenderingContext gl, Transform parentTransform, float parentAlpha) {
    if (!visible()) return;

    if (canvas.dirty()) {
      canvas.clearDirty();
      gfx.updateTexture(tex, canvas.canvas());
    }
    gfx.drawTexture(tex, width, height, localTransform(parentTransform), width, height, false,
        false, parentAlpha * alpha);
  }
}
