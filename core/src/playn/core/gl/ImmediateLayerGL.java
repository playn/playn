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
package playn.core.gl;

import pythagoras.f.Point;
import pythagoras.f.Vector;

import playn.core.ImmediateLayer;
import playn.core.InternalTransform;
import static playn.core.PlayN.*;

public class ImmediateLayerGL extends LayerGL implements ImmediateLayer {

  private static abstract class ImmediateSurfaceGL extends AbstractSurfaceGL {
    public ImmediateSurfaceGL(GLContext ctx) {
      super(ctx);
    }
    @Override
    protected void bindFramebuffer() {} // noop!
  }

  private final ImmediateSurfaceGL surface;
  private final Renderer renderer;

  public static class Clipped extends ImmediateLayerGL implements ImmediateLayer.Clipped {
    private final int width, height;
    private Point pos = new Point();
    private Vector size = new Vector();

    public Clipped(GLContext ctx, final int width, final int height, Renderer renderer) {
      super(ctx, renderer, new ImmediateSurfaceGL(ctx) {
        @Override
        public int width() {
          return width;
        }
        @Override
        public int height() {
          return height;
        }
      });
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

    protected void render(InternalTransform xform) {
      xform.transform(pos.set(0, 0), pos);
      xform.transform(size.set(width, height), size);
      ctx.startClipped((int) pos.x, (int) pos.y, (int) Math.abs(size.x), (int) Math.abs(size.y));
      try {
        super.render(xform);
      } finally {
        ctx.endClipped();
      }
    }
  }

  public ImmediateLayerGL(GLContext ctx, Renderer renderer) {
    this(ctx, renderer, new ImmediateSurfaceGL(ctx) {
        @Override
        public int width() {
          return graphics().width();
        }
        @Override
        public int height() {
          return graphics().height();
        }
    });
  }

  protected ImmediateLayerGL(GLContext ctx, Renderer renderer, ImmediateSurfaceGL surface) {
    super(ctx);
    this.surface = surface;
    this.renderer = renderer;
  }

  @Override
  public void paint(InternalTransform parentTransform, float parentAlpha) {
    if (!visible()) return;
    InternalTransform xform = localTransform(parentTransform);
    surface.topTransform().set(xform);
    render(xform);
  }

  protected void render(InternalTransform xform) {
    renderer.render(surface);
  }
}
