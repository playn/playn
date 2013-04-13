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

import playn.core.Canvas;
import playn.core.ImmediateLayer;
import playn.core.InternalTransform;

public class ImmediateLayerCanvas extends LayerCanvas implements ImmediateLayer {

  private final CanvasSurface surf = new CanvasSurface(null);
  private final Renderer renderer;

  public static class Clipped extends ImmediateLayerCanvas implements ImmediateLayer.Clipped {
    private final int width, height;

    public Clipped(InternalTransform xform, int width, int height, Renderer renderer) {
      super(xform, renderer);
      this.width = width;
      this.height = height;
    }

    @Override
    public float width() {
      return width;
    }

    @Override
    public float height() {
      return height;
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
    protected void render(Canvas canvas) {
      canvas.clipRect(0, 0, width, height);
      super.render(canvas);
    }
  }

  public ImmediateLayerCanvas(InternalTransform xform, Renderer renderer) {
    super(xform);
    this.renderer = renderer;
  }

  @Override
  public Renderer renderer () {
    return renderer;
  }

  @Override
  public void paint(Canvas canvas, float parentAlpha) {
    if (!visible()) return;

    canvas.save();
    transform(canvas);
    canvas.setAlpha(parentAlpha * alpha());
    render(canvas);
    canvas.restore();
  }

  protected void render(Canvas canvas) {
    surf.canvas = canvas;
    renderer.render(surf);
    surf.canvas = null;
  }
}
