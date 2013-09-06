/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core.gl;

import java.util.List;

import pythagoras.f.Point;
import pythagoras.f.Vector;

import playn.core.Asserts;
import playn.core.GroupLayer;
import playn.core.GroupLayerImpl;
import playn.core.InternalTransform;
import playn.core.Layer;
import playn.core.ParentLayer;
import playn.core.Tint;

public class GroupLayerGL extends LayerGL implements GroupLayer, ParentLayer {

  public static class Clipped extends GroupLayerGL implements GroupLayer.Clipped, HasSize {
    private final Point pos = new Point();
    private final Vector size = new Vector();
    private float width, height;

    public Clipped (GLContext ctx, float width, float height) {
      super(ctx);
      this.width = width;
      this.height = height;
    }

    @Override
    public void setSize(float width, float height) {
      this.width = width;
      this.height = height;
    }

    @Override
    public void setWidth(float width) {
      this.width = width;
    }

    @Override
    public void setHeight(float height) {
      this.height = height;
    }

    @Override
    public float width() {
      return this.width;
    }

    @Override
    public float height() {
      return this.height;
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
    protected void render(InternalTransform xform, int curTint, GLShader shader) {
      xform.translate(originX, originY);
      xform.transform(pos.set(-originX, -originY), pos);
      xform.transform(size.set(width, height), size);
      xform.translate(-originX, -originY);
      ctx.startClipped((int) pos.x, (int) pos.y,
                       Math.round(Math.abs(size.x)), Math.round(Math.abs(size.y)));
      try {
        super.render(xform, curTint, shader);
      } finally {
        ctx.endClipped();
      }
    }
  }

  private GroupLayerImpl<LayerGL> impl = new GroupLayerImpl<LayerGL>();

  public GroupLayerGL(GLContext ctx) {
    super(ctx);
  }

  @Override
  public Layer get(int index) {
    return impl.children.get(index);
  }

  @Override
  public void add(Layer layer) {
    Asserts.checkArgument(layer instanceof LayerGL);
    impl.add(this, (LayerGL) layer);
  }

  @Override
  public void addAt(Layer layer, float tx, float ty) {
    impl.addAt(this, layer, tx, ty);
  }

  @Override
  public void remove(Layer layer) {
    Asserts.checkArgument(layer instanceof LayerGL);
    impl.remove(this, (LayerGL) layer);
  }

  @Override
  public void removeAll() {
    impl.removeAll(this);
  }

  @Override
  public void destroyAll() {
    impl.destroyAll(this);
  }

  @Deprecated @Override
  public void clear() {
    removeAll();
  }

  @Override
  public int size() {
    return impl.children.size();
  }

  @Override
  public void destroy() {
    super.destroy();
    impl.destroy(this);
  }

  @Override
  public void onAdd() {
    super.onAdd();
    impl.onAdd(this);
  }

  @Override
  public void onRemove() {
    super.onRemove();
    impl.onRemove(this);
  }

  @Override
  public Layer hitTestDefault(Point p) {
    return impl.hitTest(this, p);
  }

  @Override
  public void depthChanged(Layer layer, float oldDepth) {
    impl.depthChanged(this, layer, oldDepth);
  }

  @Override
  public void paint(InternalTransform curTransform, int curTint, GLShader curShader) {
    if (!visible()) return;

    if (tint != Tint.NOOP_TINT)
      curTint = Tint.combine(curTint, tint);
    render(localTransform(curTransform), curTint, (shader == null) ? curShader : shader);
  }

  protected void render(InternalTransform xform, int curTint, GLShader shader) {
    // iterate manually to avoid creating an Iterator as garbage, this is inner-loop territory
    List<LayerGL> children = impl.children;
    for (int ii = 0, ll = children.size(); ii < ll; ii++) {
      children.get(ii).paint(xform, curTint, shader);
    }
  }
}
