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

import pythagoras.f.Point;

import playn.core.Asserts;
import playn.core.Canvas;
import playn.core.GroupLayer;
import playn.core.GroupLayerImpl;
import playn.core.InternalTransform;
import playn.core.Layer;
import playn.core.ParentLayer;

public class GroupLayerCanvas extends LayerCanvas implements GroupLayer, ParentLayer {

  public static class Clipped extends GroupLayerCanvas implements GroupLayer.Clipped, HasSize {
    private float width, height;

    public Clipped(InternalTransform xform, float width, float height) {
      super(xform);
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
    protected void paintChildren(Canvas canvas, float alpha) {
      canvas.clipRect(0, 0, width, height);
      super.paintChildren(canvas, alpha);
    }
  }

  private GroupLayerImpl<LayerCanvas> impl = new GroupLayerImpl<LayerCanvas>();

  public GroupLayerCanvas(InternalTransform xform) {
    super(xform);
  }

  @Override
  public Layer get(int index) {
    return impl.children.get(index);
  }

  @Override
  public void add(Layer layer) {
    Asserts.checkArgument(layer instanceof LayerCanvas);
    impl.add(this, (LayerCanvas) layer);
  }

  @Override
  public void addAt(Layer layer, float tx, float ty) {
    impl.addAt(this, layer, tx, ty);
  }

  @Override
  public void remove(Layer layer) {
    Asserts.checkArgument(layer instanceof LayerCanvas);
    impl.remove(this, (LayerCanvas) layer);
  }

  @Deprecated @Override
  public void clear() {
    removeAll();
  }

  @Override
  public void removeAll() {
    impl.removeAll(this);
  }

  @Override
  public void destroyAll() {
    impl.destroyAll(this);
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
    Asserts.checkArgument(layer instanceof LayerCanvas);
    impl.depthChanged(this, layer, oldDepth);
  }

  @Override
  public void paint(Canvas canvas, float parentAlpha) {
    if (!visible()) return;

    canvas.save();
    transform(canvas);
    paintChildren(canvas, parentAlpha * alpha());
    canvas.restore();
  }

  protected void paintChildren(Canvas canvas, float alpha) {
    for (LayerCanvas child : impl.children) {
      child.paint(canvas, alpha);
    }
  }
}
