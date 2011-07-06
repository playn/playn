/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.html;

import com.google.gwt.webgl.client.WebGLRenderingContext;

import forplay.core.Asserts;
import forplay.core.GroupLayer;
import forplay.core.GroupLayerImpl;
import forplay.core.Layer;
import forplay.core.Transform;

class HtmlGroupLayerGL extends HtmlLayerGL implements GroupLayer {

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

  @Override
  public void add(int index, Layer layer) {
    Asserts.checkArgument(layer instanceof HtmlLayerGL);
    impl.add(this, index, (HtmlLayerGL) layer);
  }

  @Override
  public void remove(Layer layer) {
    Asserts.checkArgument(layer instanceof HtmlLayerGL);
    impl.remove(this, (HtmlLayerGL) layer);
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

  void paint(WebGLRenderingContext gl, Transform parentTransform, float parentAlpha) {
    if (!visible()) return;

    for (HtmlLayerGL child : impl.children) {
      child.paint(gl, localTransform(parentTransform), parentAlpha * alpha);
    }
  }
}
