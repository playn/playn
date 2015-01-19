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
package playn.scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests parts of {@link GroupLayer}.
 */
public class GroupLayerTest {

  @Test public void testAddPreservesDepth() {
    GroupLayer group = new GroupLayer();
    List<Layer> layers = createLayers();

    // add the layers highest to lowest and make sure they end up lowest to highest
    for (int ii = layers.size()-1; ii >= 0; ii--) group.add(layers.get(ii));
    validateOrder(group);
    group.removeAll();

    // add the layers lowest to highest and make sure they end up lowest to highest
    for (Layer l : layers) group.add(l);
    validateOrder(group);
    group.removeAll();

    // add the layers in random order and make sure they end up lowest to highest
    List<Layer> llist = new ArrayList<Layer>(layers);
    Collections.shuffle(llist);
    for (Layer l : layers) group.add(l);
    validateOrder(group);
    group.removeAll();
  }

  @Test public void testDepthUpdates() {
    GroupLayer group = new GroupLayer();
    List<Layer> layers = createLayers();

    // first just add the layers as is
    for (Layer l : layers) group.add(l);
    validateOrder(group);

    // now pick random layers and increase or decrease their depth by 0, 1, 2, 3, or 4
    Random rando = new Random();
    for (int iter = 0; iter < 500; iter++) {
      Layer l = group.childAt(rando.nextInt(group.children()));
      l.setDepth(l.depth() + (4 - rando.nextInt(9)));
      validateOrder(group);
    }
  }

  @Test public void testLifecycle () {
    RootLayer root = new RootLayer();
    GroupLayer group = new GroupLayer();
    ImageLayer leaf0 = new ImageLayer();

    // everything should start as REMOVED
    assertEquals(Layer.State.REMOVED, group.state.get());
    assertEquals(Layer.State.REMOVED, leaf0.state.get());

    // adding a layer to a REMOVED group leaves it as REMOVED
    group.add(leaf0);
    assertEquals(Layer.State.REMOVED, leaf0.state.get());

    // add the group to the root, everything becomes ADDED
    root.add(group);
    assertEquals(Layer.State.ADDED, group.state.get());
    assertEquals(Layer.State.ADDED, leaf0.state.get());

    // remove from an ADDED group and become REMOVED
    group.remove(leaf0);
    assertEquals(Layer.State.REMOVED, leaf0.state.get());
    // destroy and become DISPOSED
    leaf0.close();
    assertEquals(Layer.State.DISPOSED, leaf0.state.get());

    // add to an ADDED group and we immediately become ADDED
    ImageLayer leaf1 = new ImageLayer();
    assertEquals(Layer.State.REMOVED, leaf1.state.get());
    group.add(leaf1);
    assertEquals(Layer.State.ADDED, leaf1.state.get());

    // remove group from root and everything becomes REMOVED
    root.remove(group);
    assertEquals(Layer.State.REMOVED, group.state.get());
    assertEquals(Layer.State.REMOVED, leaf1.state.get());

    // destroy group and group and all children become DISPOSED
    group.close();
    assertEquals(Layer.State.DISPOSED, group.state.get());
    assertEquals(Layer.State.DISPOSED, leaf1.state.get());
  }

  protected List<Layer> createLayers() {
    int[] zs = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4 };
    Layer[] layers = new Layer[zs.length];
    for (int ii = 0; ii < layers.length; ii++) {
      layers[ii] = new ImageLayer();
      layers[ii].setDepth(zs[ii]);
    }
    return Arrays.asList(layers);
  }

  protected void validateOrder(GroupLayer group) {
    for (int ii = 0; ii < group.children(); ii++) {
      if (ii == 0) continue;
      assertTrue(group.childAt(ii-1).depth() <= group.childAt(ii).depth());
    }
  }
}
