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

import com.google.gwt.canvas.dom.client.Context2d;
import playn.core.CanvasSurface;
import playn.core.ImmediateLayer;

import static playn.core.PlayN.graphics;

class FlashImmediateLayerCanvas extends FlashCanvasLayer implements ImmediateLayer {

  private final Renderer renderer;
  private final CanvasSurface surf;

  static class Clipped extends FlashImmediateLayerCanvas
      implements ImmediateLayer.Clipped {
    private final int width, height;

    public Clipped(FlashCanvasLayer.Context2d ctx, int width, int height, Renderer renderer) {
      super(renderer, new CanvasSurface(new FlashCanvas(width, height, ctx)));
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
    protected void render(Context2d ctx) {
      ctx.beginPath();
      ctx.rect(0, 0, width, height);
      ctx.clip();
      super.render(ctx);
    }
  }

  public FlashImmediateLayerCanvas(FlashCanvasLayer.Context2d ctx, Renderer renderer) {
    this(renderer, new CanvasSurface(new FlashCanvas(graphics().width(), graphics().height(), ctx)));
  }

  protected FlashImmediateLayerCanvas(Renderer renderer, CanvasSurface surf) {
    super(surf.width(), surf.height());
    this.renderer = renderer;
    this.surf = surf;
  }

  void paint(FlashCanvasLayer.Context2d ctx, float parentAlpha) {
    if (!visible()) return;

    ctx.save();
    transform(ctx);
    ctx.setGlobalAlpha(parentAlpha * alpha);
    render(ctx);
    ctx.restore();
  }

  void transform(Context2d ctx) {
    ctx.translate(originX, originY);
    ctx.transform(transform.m00(), transform.m01(), transform.m10(),
        transform.m11(), transform.tx() - originX, transform.ty() - originY);
    ctx.translate(-originX, -originY);
  }

  protected void render(Context2d ctx) {
    renderer.render(surf);
  }
}
