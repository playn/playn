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

import org.junit.Test;

import pythagoras.f.Point;
import static org.junit.Assert.*;

/**
 * Tests the Layer.Util class.
 */
public class LayerUtilTest {
  static float tol = 0.001f; // tolerance for floating point equality checks

  @Test public void testTransformWithScale() {
    GroupLayer root = new GroupLayer();
    GroupLayer middle = new GroupLayer();
    Layer child = new ImageLayer();
    root.add(middle);
    middle.add(child);

    middle.setScale(2f, 0.1f);
    Point point = new Point(100f, 100f);

    Point pointOnChild = new Point(0f, 0f);
    LayerUtil.screenToLayer(child, point, pointOnChild);
    assertEquals(point.x() * 0.5, pointOnChild.x(), tol);
    assertEquals(point.y() * 10, pointOnChild.y(), tol);

    Point pointOnParent = new Point(0f, 0f);
    LayerUtil.layerToScreen(child, pointOnChild, pointOnParent);
    assertEquals(point.x(), pointOnParent.x(), tol);
    assertEquals(point.y(), pointOnParent.y(), tol);

    root.removeAll();
  }

  @Test public void testTransformWithTrans() {
    GroupLayer root = new GroupLayer();
    GroupLayer middle = new GroupLayer();
    Layer child = new ImageLayer();
    root.add(middle);
    middle.add(child);

    middle.setTranslation(10, -10);
    Point point = new Point(100f, 100f);

    Point pointOnChild = new Point(0f, 0f);
    LayerUtil.screenToLayer(child, point, pointOnChild);
    assertEquals(point.x() - 10, pointOnChild.x(), tol);
    assertEquals(point.y() + 10, pointOnChild.y(), tol);

    Point pointOnParent = new Point(0f, 0f);
    LayerUtil.layerToScreen(child, pointOnChild, pointOnParent);
    assertEquals(point.x(), pointOnParent.x(), tol);
    assertEquals(point.y(), pointOnParent.y(), tol);

    root.removeAll();
  }

  @Test public void testTransformWithRot() {
    GroupLayer root = new GroupLayer();
    GroupLayer middle = new GroupLayer();
    Layer child = new ImageLayer();
    root.add(middle);
    middle.add(child);

    middle.setRotation((float)(Math.PI / 4.0));
    Point point = new Point(100f, 100f);

    Point pointOnChild = new Point(0f, 0f);
    LayerUtil.screenToLayer(child, point, pointOnChild);
    assertEquals(141.421356, pointOnChild.x(), tol);
    assertEquals(0, pointOnChild.y(), tol);

    Point pointOnParent = new Point(0f, 0f);
    LayerUtil.layerToScreen(child, pointOnChild, pointOnParent);
    assertEquals(point.x(), pointOnParent.x(), tol);
    assertEquals(point.y(), pointOnParent.y(), tol);

    root.removeAll();
  }

  @Test public void testTransformWithScaleRotTrans() {
    GroupLayer root = new GroupLayer();
    GroupLayer middle = new GroupLayer();
    Layer child = new ImageLayer();
    root.add(middle);
    middle.add(child);

    middle.setRotation((float)(Math.PI / 4.0));
    middle.setTranslation(10, -10);
    middle.setScale(10f, 0.5f);
    Point point = new Point(100f, 100f);

    Point pointOnChild = new Point(0f, 0f);
    LayerUtil.screenToLayer(child, point, pointOnChild);
    Point pointOnParent = new Point(0f, 0f);
    LayerUtil.layerToScreen(child, pointOnChild, pointOnParent);
    assertEquals(point.x(), pointOnParent.x(), tol);
    assertEquals(point.y(), pointOnParent.y(), tol);

    root.removeAll();
  }
}
