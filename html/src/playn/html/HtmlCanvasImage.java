/**
 * Copyright 2010 The PlayN Authors
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

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.webgl.client.WebGLTexture;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.gl.GLContext;

class HtmlCanvasImage extends HtmlImage implements CanvasImage {

  private HtmlCanvas canvas;

  public HtmlCanvasImage(HtmlCanvas canvas) {
    super(canvas.canvas());
    this.canvas = canvas;
  }

  @Override
  public Canvas canvas() {
    return canvas;
  }

  @Override
  public WebGLTexture ensureTexture(GLContext ctx, boolean repeatX, boolean repeatY) {
    if (canvas.dirty()) {
      canvas.clearDirty();
      if (tex != null) {
        ((HtmlGLContext) ctx).updateTexture(tex, canvas.canvas().<ImageElement>cast());
      } // if tex is null, loadTexture will grab the latest canvas data from the image
    }
    return super.ensureTexture(ctx, repeatX, repeatY);
  }
}
