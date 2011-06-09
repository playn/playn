/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.android;

import forplay.core.Asserts;
import forplay.core.GroupLayer;
import forplay.core.Layer;

import java.util.ArrayList;
import java.util.List;

class AndroidGroupLayer extends AndroidLayer implements GroupLayer {

  private List<AndroidLayer> children = new ArrayList<AndroidLayer>();

  @Override
  public void add(Layer layer) {
    Asserts.checkArgument(layer instanceof AndroidLayer);
    AndroidLayer jlayer = (AndroidLayer) layer;
    children.add(jlayer);
    jlayer.setParent(this);
    jlayer.onAdd();
  }

  @Override
  public void destroy() {
    super.destroy();

    for (AndroidLayer child : children) {
      child.destroy();
    }
  }

  @Override
  public void remove(Layer layer) {
    Asserts.checkArgument(layer instanceof AndroidLayer);
    AndroidLayer jlayer = (AndroidLayer) layer;
    jlayer.onRemove();
    children.remove(jlayer);
    jlayer.setParent(null);
  }

  @Override
  void paint(AndroidCanvas surf) {
    surf.save();
    transform(surf);

    for (AndroidLayer child : children) {
      child.paint(surf);
    }

    surf.restore();
  }

  @Override
  public Layer get(int index) {
    return children.get(index);
  }

  @Override
  public void add(int index, Layer layer) {
    Asserts.checkArgument(layer instanceof AndroidLayer);
    AndroidLayer jlayer = (AndroidLayer) layer;
    children.add(index, jlayer);
    jlayer.setParent(this);
    jlayer.onAdd();
  }

  @Override
  public void remove(int index) {
    AndroidLayer jlayer = children.get(index);
    jlayer.onRemove();
    children.remove(index);
    jlayer.setParent(null);
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
