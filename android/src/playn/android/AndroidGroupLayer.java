/**
 * Copyright 2011 The PlayN Authors
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
package playn.android;

import playn.core.Asserts;
import playn.core.GroupLayer;
import playn.core.GroupLayerImpl;
import playn.core.InternalTransform;
import playn.core.Layer;
import playn.core.ParentLayer;

class AndroidGroupLayer extends AndroidLayer implements GroupLayer, ParentLayer {

  private GroupLayerImpl<AndroidLayer> impl = new GroupLayerImpl<AndroidLayer>();

  public AndroidGroupLayer(AndroidGraphics gfx) {
    super(gfx);
  }

  @Override
  public Layer get(int index) {
    return impl.children.get(index);
  }

  @Override
  public void add(Layer layer) {
    Asserts.checkArgument(layer instanceof AndroidLayer);
    impl.add(this, (AndroidLayer) layer);
  }

  @Override
  public void add(int index, Layer layer) {
    Asserts.checkArgument(layer instanceof AndroidLayer);
    impl.add(this, index, (AndroidLayer) layer);
  }

  @Override
  public void remove(Layer layer) {
    Asserts.checkArgument(layer instanceof AndroidLayer);
    impl.remove(this, (AndroidLayer) layer);
  }

  @Override
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
    gfx.checkGlError("GroupLayer.paint");
    if (!visible())
      return;

    for (AndroidLayer child : impl.children) {
      child.paint(localTransform(parentTransform), parentAlpha * alpha);
    }
  }
}
