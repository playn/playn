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
package playn.flash;

import flash.display.Sprite;

import pythagoras.f.MathUtil;

import playn.core.CanvasSurface;
import playn.core.ImmediateLayer;
import playn.core.InternalTransform;

import static playn.core.PlayN.graphics;

class FlashImmediateLayerCanvas extends FlashLayer implements ImmediateLayer {

  private final Renderer renderer;
  private final CanvasSurface surf;

  static class Clipped extends FlashImmediateLayerCanvas implements ImmediateLayer.Clipped {
    private final int width, height;

    public Clipped(FlashCanvas.Context2d ctx, int width, int height, Renderer renderer) {
      super(ctx, width, height, renderer);
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
    protected void render(FlashCanvas.Context2d ctx) {
      ctx.beginPath();
      ctx.rect(0, 0, width, height);
      ctx.clip();
      super.render(ctx);
    }
  }

  public FlashImmediateLayerCanvas(FlashCanvas.Context2d ctx, Renderer renderer) {
    this(ctx, graphics().width(), graphics().height(), renderer);
  }

  @Override
  public Renderer renderer () {
      return renderer;
  }

  protected FlashImmediateLayerCanvas(FlashCanvas.Context2d ctx, float width, float height,
                                      Renderer renderer) {
    super((Sprite) FlashCanvas.CanvasElement.create(
            MathUtil.iceil(width), MathUtil.iceil(height)).cast());
    this.renderer = renderer;
    this.surf = new CanvasSurface(new FlashCanvas(width, height, ctx));
  }

  void paint(FlashCanvas.Context2d ctx, float parentAlpha) {
    if (!visible()) return;

    ctx.save();
    transform(ctx);
    ctx.setGlobalAlpha(parentAlpha * alpha);
    render(ctx);
    ctx.restore();
  }

  void transform(FlashCanvas.Context2d ctx) {
    ctx.translate(originX, originY);
    InternalTransform transform = (InternalTransform) transform();
    ctx.transform(transform.m00(), transform.m01(), transform.m10(),
        transform.m11(), transform.tx() - originX, transform.ty() - originY);
    ctx.translate(-originX, -originY);
  }

  protected void render(FlashCanvas.Context2d ctx) {
    renderer.render(surf);
  }
}
