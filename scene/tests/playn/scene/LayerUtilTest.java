package playn.core;

import org.junit.Test;

import pythagoras.f.Point;
import static org.junit.Assert.*;

/**
 * Tests the Layer.Util class.
 */
public class LayerUtilTest {
  static float tol = 0.001f; // tolerance for floating point equality checks

  @Test public void testTransformWithScale() {
    TestGroupLayer root = new TestGroupLayer();
    TestGroupLayer middle = new TestGroupLayer();
    TestLayer child = new TestLayer();
    root.add(middle);
    middle.add(child);

    middle.setScale(2f, 0.1f);
    Point point = new Point(100f, 100f);

    Point pointOnChild = new Point(0f, 0f);
    Layer.Util.screenToLayer(child, point, pointOnChild);
    assertEquals(point.x() * 0.5, pointOnChild.x(), tol);
    assertEquals(point.y() * 10, pointOnChild.y(), tol);

    Point pointOnParent = new Point(0f, 0f);
    Layer.Util.layerToScreen(child, pointOnChild, pointOnParent);
    assertEquals(point.x(), pointOnParent.x(), tol);
    assertEquals(point.y(), pointOnParent.y(), tol);

    root.clear();
  }

  @Test public void testTransformWithTrans() {
    TestGroupLayer root = new TestGroupLayer();
    TestGroupLayer middle = new TestGroupLayer();
    TestLayer child = new TestLayer();
    root.add(middle);
    middle.add(child);

    middle.setTranslation(10, -10);
    Point point = new Point(100f, 100f);

    Point pointOnChild = new Point(0f, 0f);
    Layer.Util.screenToLayer(child, point, pointOnChild);
    assertEquals(point.x() - 10, pointOnChild.x(), tol);
    assertEquals(point.y() + 10, pointOnChild.y(), tol);

    Point pointOnParent = new Point(0f, 0f);
    Layer.Util.layerToScreen(child, pointOnChild, pointOnParent);
    assertEquals(point.x(), pointOnParent.x(), tol);
    assertEquals(point.y(), pointOnParent.y(), tol);

    root.clear();
  }

  @Test public void testTransformWithRot() {
    TestGroupLayer root = new TestGroupLayer();
    TestGroupLayer middle = new TestGroupLayer();
    TestLayer child = new TestLayer();
    root.add(middle);
    middle.add(child);

    middle.setRotation((float)(Math.PI / 4.0));
    Point point = new Point(100f, 100f);

    Point pointOnChild = new Point(0f, 0f);
    Layer.Util.screenToLayer(child, point, pointOnChild);
    assertEquals(141.421356, pointOnChild.x(), tol);
    assertEquals(0, pointOnChild.y(), tol);

    Point pointOnParent = new Point(0f, 0f);
    Layer.Util.layerToScreen(child, pointOnChild, pointOnParent);
    assertEquals(point.x(), pointOnParent.x(), tol);
    assertEquals(point.y(), pointOnParent.y(), tol);

    root.clear();
  }

  @Test public void testTransformWithScaleRotTrans() {
    TestGroupLayer root = new TestGroupLayer();
    TestGroupLayer middle = new TestGroupLayer();
    TestLayer child = new TestLayer();
    root.add(middle);
    middle.add(child);

    middle.setRotation((float)(Math.PI / 4.0));
    middle.setTranslation(10, -10);
    middle.setScale(10f, 0.5f);
    Point point = new Point(100f, 100f);

    Point pointOnChild = new Point(0f, 0f);
    Layer.Util.screenToLayer(child, point, pointOnChild);
    Point pointOnParent = new Point(0f, 0f);
    Layer.Util.layerToScreen(child, pointOnChild, pointOnParent);
    assertEquals(point.x(), pointOnParent.x(), tol);
    assertEquals(point.y(), pointOnParent.y(), tol);

    root.clear();
  }

  protected static class TestLayer extends AbstractLayer {
  }

  protected static class TestGroupLayer extends AbstractLayer implements GroupLayer {
    public final GroupLayerImpl<AbstractLayer> impl = new GroupLayerImpl<AbstractLayer>();
    @Override
    public Layer get(int index) {
      return impl.children.get(index);
    }
    @Override
    public void add(Layer layer) {
      impl.add(this, (AbstractLayer)layer);
    }
    @Override
    public void remove(Layer layer) {
      impl.remove(this, (AbstractLayer)layer);
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
    public void addAt (Layer layer, float tx, float ty) {
      impl.addAt(this, layer, tx, ty);
    }
  }
}
