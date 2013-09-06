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
package playn.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests parts of {@link GroupLayerImpl}.
 */
public class GroupLayerImplTest {

  @Test public void testAddPreservesDepth() {
    TestGroupLayer group = new TestGroupLayer();
    List<TestLayer> layers = createLayers();

    // add the layers highest to lowest and make sure they end up lowest to highest
    for (int ii = layers.size()-1; ii >= 0; ii--) group.add(layers.get(ii));
    validateOrder(group);
    group.clear();

    // add the layers lowest to highest and make sure they end up lowest to highest
    for (TestLayer l : layers) group.add(l);
    validateOrder(group);
    group.clear();

    // add the layers in random order and make sure they end up lowest to highest
    List<TestLayer> llist = new ArrayList<TestLayer>(layers);
    Collections.shuffle(llist);
    for (TestLayer l : layers) group.add(l);
    validateOrder(group);
    group.clear();
  }

  @Test public void testDepthUpdates() {
    TestGroupLayer group = new TestGroupLayer();
    List<TestLayer> layers = createLayers();

    // first just add the layers as is
    for (TestLayer l : layers) group.add(l);
    validateOrder(group);

    // now pick random layers and increase or decrease their depth by 0, 1, 2, 3, or 4
    Random rando = new Random();
    for (int iter = 0; iter < 500; iter++) {
      Layer l = group.get(rando.nextInt(group.size()));
      l.setDepth(l.depth() + (4 - rando.nextInt(9)));
      validateOrder(group);
    }
  }

  protected List<TestLayer> createLayers() {
    int[] zs = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4 };
    TestLayer[] layers = new TestLayer[zs.length];
    for (int ii = 0; ii < layers.length; ii++) {
      layers[ii] = new TestLayer();
      layers[ii].setDepth(zs[ii]);
    }
    return Arrays.asList(layers);
  }

  protected void validateOrder(TestGroupLayer group) {
    for (int ii = 0; ii < group.size(); ii++) {
      if (ii == 0) continue;
      assertTrue(group.get(ii-1).depth() <= group.get(ii).depth());
    }
  }

  protected static class TestLayer extends AbstractLayer {
  }

  protected static class TestGroupLayer extends AbstractLayer implements GroupLayer, ParentLayer {
    public final GroupLayerImpl<TestLayer> impl = new GroupLayerImpl<TestLayer>();
    @Override
    public Layer get(int index) {
      return impl.children.get(index);
    }
    @Override
    public void add(Layer layer) {
      impl.add(this, (TestLayer)layer);
    }
    @Override
    public void remove(Layer layer) {
      impl.remove(this, (TestLayer)layer);
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
    public void depthChanged(Layer layer, float oldDepth) {
      impl.depthChanged(this, layer, oldDepth);
    }
    @Override
    public void addAt (Layer layer, float tx, float ty) {
      impl.addAt(this, layer, tx, ty);
    }
  }
}
