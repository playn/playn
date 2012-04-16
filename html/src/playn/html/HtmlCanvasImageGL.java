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

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.gl.GLContext;

class HtmlCanvasImageGL extends HtmlImage implements CanvasImage {

  private HtmlCanvas canvas;

  public HtmlCanvasImageGL(HtmlCanvas canvas) {
    super(canvas.canvas());
    this.canvas = canvas;
  }

  @Override
  public Canvas canvas() {
    return canvas;
  }

  @Override
  public Object ensureTexture(GLContext ctx, boolean repeatX, boolean repeatY) {
    if (canvas.dirty()) {
      canvas.clearDirty();
      clearTexture(ctx);
    }
    return super.ensureTexture(ctx, repeatX, repeatY);
  }
}
