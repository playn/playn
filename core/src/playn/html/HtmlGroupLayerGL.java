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
package playn.html;

import com.google.gwt.webgl.client.WebGLRenderingContext;

import playn.core.Asserts;
import playn.core.GroupLayer;
import playn.core.GroupLayerImpl;
import playn.core.InternalTransform;
import playn.core.Layer;
import playn.core.ParentLayer;

class HtmlGroupLayerGL extends HtmlLayerGL implements GroupLayer, ParentLayer {

  private GroupLayerImpl<HtmlLayerGL> impl = new GroupLayerImpl<HtmlLayerGL>();

  public HtmlGroupLayerGL(HtmlGraphicsGL gfx) {
    super(gfx);
  }

  @Override
  public Layer get(int index) {
    return impl.children.get(index);
  }

  @Override
  public void add(Layer layer) {
    Asserts.checkArgument(layer instanceof HtmlLayerGL);
    impl.add(this, (HtmlLayerGL) layer);
  }

  @Override @Deprecated
  public void add(int index, Layer layer) {
    Asserts.checkArgument(layer instanceof HtmlLayerGL);
    impl.add(this, index, (HtmlLayerGL) layer);
  }

  @Override
  public void remove(Layer layer) {
    Asserts.checkArgument(layer instanceof HtmlLayerGL);
    impl.remove(this, (HtmlLayerGL) layer);
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
  public void depthChanged(Layer layer, float oldDepth) {
    impl.depthChanged(this, layer, oldDepth);
  }

  @Override
  public void paint(InternalTransform parentTransform, float parentAlpha) {
    if (!visible()) return;

    for (HtmlLayerGL child : impl.children) {
      child.paint(localTransform(parentTransform), parentAlpha * alpha);
    }
  }
}
