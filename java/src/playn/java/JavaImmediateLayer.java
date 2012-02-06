/**
 * Copyright 2012 The PlayN Authors
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
package playn.java;

import java.awt.Graphics2D;

import playn.core.CanvasSurface;
import playn.core.ImmediateLayer;

class JavaImmediateLayer extends JavaLayer implements ImmediateLayer {

  private final Renderer renderer;

  static class Clipped extends JavaImmediateLayer
      implements ImmediateLayer.Clipped, JavaCanvasState.Clipper {
    private final int width, height;

    public Clipped(int width, int height, Renderer renderer) {
      super(renderer);
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
      return transform().scaleX() * width();
    }

    @Override
    public float scaledHeight() {
      return transform().scaleY() * height();
    }

    @Override
    public void setClip(Graphics2D gfx) {
      gfx.setClip(0, 0, width, height);
    }

    protected void render(JavaCanvas canvas) {
      canvas.clip(this);
      super.render(canvas);
    }
  }

  public JavaImmediateLayer(Renderer renderer) {
    this.renderer = renderer;
  }

  @Override
  void paint(JavaCanvas canvas) {
    if (!visible()) return;

    canvas.save();
    transform(canvas);
    canvas.setAlpha(canvas.alpha() * alpha);
    render(canvas);
    canvas.restore();
  }

  protected void render(JavaCanvas canvas) {
    // TODO: this needless garbage generation annoys me
    renderer.render(new CanvasSurface(canvas));
  }
}
