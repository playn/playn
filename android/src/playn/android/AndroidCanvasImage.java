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
package playn.android;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.gl.GLContext;

class AndroidCanvasImage extends AndroidImage implements CanvasImage {

  private final AndroidCanvas canvas;

  AndroidCanvasImage(AndroidGraphics graphics, int width, int height, boolean alpha) {
    super(graphics.ctx, graphics.createBitmap(width, height, alpha));
    this.canvas = new AndroidCanvas(bitmap());
  }

  @Override
  public Canvas canvas() {
    return canvas;
  }

  @Override
  public Object ensureTexture(GLContext ctx, boolean repeatX, boolean repeatY) {
    // if we have a canvas, and it's dirty, force the recreation of our texture which will obtain
    // the latest canvas data
    if (canvas.dirty()) {
      canvas.clearDirty();
      clearTexture(ctx);
    }
    return super.ensureTexture(ctx, repeatX, repeatY);
  }
}
