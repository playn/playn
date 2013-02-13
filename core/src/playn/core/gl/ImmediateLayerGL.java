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
import playn.core.Tint;
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
        public float width() {
          return width;
        }
        @Override
        public float height() {
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
      return scaleX() * width();
    }

    @Override
    public float scaledHeight() {
      return scaleY() * height();
    }

    @Override
    protected void render(InternalTransform xform) {
      xform.translate(originX, originY);
      xform.transform(pos.set(-originX, -originY), pos);
      xform.transform(size.set(width, height), size);
      xform.translate(-originX, -originY);
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
        public float width() {
          return graphics().width();
        }
        @Override
        public float height() {
          return graphics().height();
        }
    });
  }

  @Override
  public Renderer renderer () {
      return renderer;
  }

  protected ImmediateLayerGL(GLContext ctx, Renderer renderer, ImmediateSurfaceGL surface) {
    super(ctx);
    this.surface = surface;
    this.renderer = renderer;
  }

  @Override
  public void paint(InternalTransform curTransform, int curTint, GLShader curShader) {
    if (!visible()) return;

    InternalTransform xform = localTransform(curTransform);
    surface.topTransform().set(xform);
    if (tint != Tint.NOOP_TINT)
      curTint = Tint.combine(curTint, tint);
    surface.setTint(curTint);
    surface.setShader((shader == null) ? curShader : shader);
    render(xform);
    surface.setShader(null);
    // TODO: restore tint?
  }

  protected void render(InternalTransform xform) {
    renderer.render(surface);
  }
}
