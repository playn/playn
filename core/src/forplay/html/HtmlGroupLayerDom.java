/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.html;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;

import java.util.ArrayList;
import java.util.List;

import forplay.core.Asserts;
import forplay.core.GroupLayer;
import forplay.core.Layer;

class HtmlGroupLayerDom extends HtmlLayerDom implements GroupLayer {

  private List<HtmlLayerDom> children = new ArrayList<HtmlLayerDom>();

  HtmlGroupLayerDom() {
    super(Document.get().createDivElement());
  }

  HtmlGroupLayerDom(Element elem) {
    super(elem);
  }

  @Override
  public void add(Layer layer) {
    Asserts.checkArgument(layer instanceof HtmlLayerDom);
    HtmlLayerDom hlayer = (HtmlLayerDom) layer;
    children.add(hlayer);
    element().appendChild(hlayer.element());
    hlayer.setParent(this);
    hlayer.onAdd();
  }

  @Override
  public void destroy() {
    super.destroy();

    for (HtmlLayerDom child : children) {
      child.destroy();
    }
  }

  @Override
  public void remove(Layer layer) {
    Asserts.checkArgument(layer instanceof HtmlLayerDom);
    HtmlLayerDom hlayer = (HtmlLayerDom) layer;
    hlayer.onRemove();
    children.remove(hlayer);
    element().removeChild(hlayer.element());
    hlayer.setParent(null);
  }

  void update() {
    super.update();
    for (HtmlLayerDom child : children) {
      child.update();
    }
  }

  @Override
  public Layer get(int index) {
    return children.get(index);
  }

  @Override
  public void add(int index, Layer layer) {
    Asserts.checkArgument(layer instanceof HtmlLayerDom);
    HtmlLayerDom hlayer = (HtmlLayerDom) layer;

    if (index == size()) {
      element().appendChild(hlayer.element());
    } else {
      Node refChild = element().getChild(index);
      element().insertBefore(hlayer.element(), refChild);
    }
    children.add(index, hlayer);

    hlayer.setParent(this);
    hlayer.onAdd();
  }

  @Override
  public void remove(int index) {
    HtmlLayerDom hlayer = children.get(index);
    hlayer.onRemove();
    children.remove(index);
    element().removeChild(element().getChild(index));
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
