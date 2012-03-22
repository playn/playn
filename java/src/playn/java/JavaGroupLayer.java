/**
 * Copyright 2010 The PlayN Authors
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

import pythagoras.f.Point;

import playn.core.Asserts;
import playn.core.GroupLayer;
import playn.core.GroupLayerImpl;
import playn.core.Layer;
import playn.core.ParentLayer;

class JavaGroupLayer extends JavaLayer implements GroupLayer, ParentLayer {

  private GroupLayerImpl<JavaLayer> impl = new GroupLayerImpl<JavaLayer>();

  @Override
  public Layer get(int index) {
    return impl.children.get(index);
  }

  @Override
  public void add(Layer layer) {
    Asserts.checkArgument(layer instanceof JavaLayer);
    impl.add(this, (JavaLayer) layer);
  }

  @Override @Deprecated
  public void add(int index, Layer layer) {
    Asserts.checkArgument(layer instanceof JavaLayer);
    impl.add(this, index, (JavaLayer) layer);
  }

  @Override
  public void addAt(Layer layer, float tx, float ty) {
    impl.addAt(this, layer, tx, ty);
  }

  @Override
  public void remove(Layer layer) {
    Asserts.checkArgument(layer instanceof JavaLayer);
    impl.remove(this, (JavaLayer) layer);
  }

  @Override @Deprecated
  public void remove(int index) {
    impl.remove(this, index);
  }

  @Override
  public void clear() {
    impl.clear(this);
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
  void paint(JavaCanvas surf) {
    if (!visible()) return;

    surf.save();
    transform(surf);
    surf.setAlpha(surf.alpha() * alpha);
    for (JavaLayer child : impl.children) {
      child.paint(surf);
    }
    surf.restore();
  }
}
