/**
 * Copyright 2013 The PlayN Authors
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
package playn.core.canvas;

import playn.core.Asserts;
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.InternalTransform;
import playn.core.Surface;
import playn.core.SurfaceLayer;

public class SurfaceLayerCanvas extends LayerCanvas implements SurfaceLayer {

  private CanvasImage image;
  private CanvasSurface surface;

  public SurfaceLayerCanvas(InternalTransform xform, CanvasImage image) {
    super(xform);
    this.image = image;
    this.surface = new CanvasSurface(image.canvas());
  }

  @Override
  public Surface surface() {
    return surface;
  }

  @Override
  public float width() {
    Asserts.checkNotNull(surface, "Surface has been destroyed");
    return surface.width();
  }

  @Override
  public float height() {
    Asserts.checkNotNull(surface, "Surface has been destroyed");
    return surface.height();
  }

  @Override
  public float scaledWidth() {
    return scaleX() * width();
  }

  @Override
  public float scaledHeight() {
    return scaleY() * height();
  }

  @Override
  public void destroy() {
    super.destroy();
    image = null;
    surface = null;
  }

  @Override
  public void paint(Canvas canvas, float parentAlpha) {
    if (!visible()) return;

    canvas.save();
    transform(canvas);
    canvas.setAlpha(parentAlpha * alpha());
    canvas.drawImage(image, 0, 0);
    canvas.restore();
  }
}
