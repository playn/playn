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
package playn.html;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;

import pythagoras.f.Point;

import playn.core.Asserts;
import playn.core.GroupLayer;
import playn.core.GroupLayerImpl;
import playn.core.Layer;
import playn.core.ParentLayer;

class HtmlGroupLayerDom extends HtmlLayerDom implements GroupLayer, ParentLayer {

  private GroupLayerImpl<HtmlLayerDom> impl = new GroupLayerImpl<HtmlLayerDom>();

  HtmlGroupLayerDom() {
    super(Document.get().createDivElement());
  }

  HtmlGroupLayerDom(Element elem) {
    super(elem);
  }

  @Override
  public Layer get(int index) {
    return impl.children.get(index);
  }

  @Override
  public void add(Layer layer) {
    Asserts.checkArgument(layer instanceof HtmlLayerDom);
    HtmlLayerDom hlayer = (HtmlLayerDom) layer;
    int size = size();
    int index = impl.add(this, hlayer);
    if (index == size) {
      element().appendChild(hlayer.element());
    } else {
      Node refChild = element().getChild(index);
      element().insertBefore(hlayer.element(), refChild);
    }
  }

  @Override @Deprecated
  public void add(int index, Layer layer) {
    Asserts.checkArgument(layer instanceof HtmlLayerDom);
    HtmlLayerDom hlayer = (HtmlLayerDom) layer;
    if (index == size()) {
      element().appendChild(hlayer.element());
    } else {
      Node refChild = element().getChild(index);
      element().insertBefore(hlayer.element(), refChild);
    }
    impl.add(this, index, hlayer);
  }

  @Override
  public void addAt(Layer layer, float tx, float ty) {
    impl.addAt(this, layer, tx, ty);
  }

  @Override
  public void remove(Layer layer) {
    Asserts.checkArgument(layer instanceof HtmlLayerDom);
    HtmlLayerDom hlayer = (HtmlLayerDom) layer;
    impl.remove(this, hlayer);
    element().removeChild(hlayer.element());
  }

  @Override @Deprecated
  public void remove(int index) {
    impl.remove(this, index);
    element().removeChild(element().getChild(index));
  }

  @Override
  public void clear() {
    impl.clear(this);
    while (element().hasChildNodes()) {
      element().removeChild(element().getFirstChild());
    }
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
    Asserts.checkArgument(layer instanceof HtmlLayerDom);
    HtmlLayerDom hlayer = (HtmlLayerDom) layer;
    element().removeChild(hlayer.element());
    int index = impl.depthChanged(this, layer, oldDepth);
    if (index == size()) {
      element().appendChild(hlayer.element());
    } else {
      Node refChild = element().getChild(index);
      element().insertBefore(hlayer.element(), refChild);
    }
  }

  @Override
  void update() {
    super.update();
    for (HtmlLayerDom child : impl.children) {
      child.update();
    }
  }
}
