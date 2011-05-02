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

import forplay.core.GroupLayer;
import forplay.core.Layer;
import forplay.core.Transform;

import java.util.ArrayList;
import java.util.List;

class HtmlGroupLayerGL extends HtmlLayerGL implements GroupLayer {

  private List<HtmlLayerGL> children = new ArrayList<HtmlLayerGL>();

  public HtmlGroupLayerGL(HtmlGraphicsGL gfx) {
    super(gfx);
  }

  @Override
  public void add(Layer layer) {
    assert layer instanceof HtmlLayerGL;
    HtmlLayerGL hlayer = (HtmlLayerGL) layer;
    children.add(hlayer);
    hlayer.setParent(this);
    hlayer.onAdd();
  }

  @Override
  public void destroy() {
    super.destroy();

    for (HtmlLayerGL child : children) {
      child.destroy();
    }
  }

  @Override
  public void remove(Layer layer) {
    assert layer instanceof HtmlLayerGL;
    HtmlLayerGL hlayer = (HtmlLayerGL) layer;
    children.remove(hlayer);
    hlayer.onRemove();
    hlayer.setParent(null);
  }

  @Override
  public void onAdd() {
    super.onAdd();
    for (HtmlLayerGL child : children) {
      child.onAdd();
    }
  }

  @Override
  public void onRemove() {
    for (HtmlLayerGL child : children) {
      child.onRemove();
    }
  }

  void paint(WebGLRenderingContext gl, Transform parentTransform) {
    for (HtmlLayerGL child : children) {
      child.paint(gl, localTransform(parentTransform));
    }
  }

  @Override
  public Layer get(int index) {
    return children.get(index);
  }

  @Override
  public void add(int index, Layer layer) {
    assert layer instanceof HtmlLayerGL;
    HtmlLayerGL hlayer = (HtmlLayerGL) layer;
    children.add(index, hlayer);
    hlayer.setParent(this);
    hlayer.onAdd();
  }

  @Override
  public void remove(int index) {
    HtmlLayerGL hlayer = children.get(index);
    hlayer.onRemove();
    children.remove(index);
    hlayer.setParent(null);
  }

  @Override
  public void clear() {
    while (!children.isEmpty()) {
      remove(children.size() - 1);
    }
  }

  @Override
  public int size() {
    return children.size();
  }
}
