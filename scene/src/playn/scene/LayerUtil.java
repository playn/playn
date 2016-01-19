/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.scene;

import pythagoras.f.Point;
import pythagoras.f.XY;
import pythagoras.util.NoninvertibleTransformException;

import react.Closeable;
import react.Signal;
import react.Slot;

import playn.core.Clock;
import playn.core.Log;

/**
 * Utility class for transforming coordinates between {@link Layer}s.
 */
public class LayerUtil {

  /**
   * Converts the supplied point from coordinates relative to the specified layer to screen
   * coordinates. The results are stored into {@code into}, which is returned for convenience.
   */
  public static Point layerToScreen(Layer layer, XY point, Point into) {
    return layerToParent(layer, null, point, into);
  }

  /**
   * Converts the supplied point from coordinates relative to the specified
   * layer to screen coordinates.
   */
  public static Point layerToScreen(Layer layer, float x, float y) {
    Point into = new Point(x, y);
    return layerToScreen(layer, into, into);
  }

  /**
   * Converts the supplied point from coordinates relative to the specified
   * child layer to coordinates relative to the specified parent layer. The
   * results are stored into {@code into}, which is returned for convenience.
   */
  public static Point layerToParent(Layer layer, Layer parent, XY point, Point into) {
    into.set(point);
    while (layer != parent) {
      if (layer == null) {
        throw new IllegalArgumentException(
          "Failed to find parent, perhaps you passed parent, layer instead of "
          + "layer, parent?");
      }
      into.x -= layer.originX();
      into.y -= layer.originY();
      layer.transform().transform(into, into);
      layer = layer.parent();
    }
    return into;
  }

  /**
   * Converts the supplied point from coordinates relative to the specified
   * child layer to coordinates relative to the specified parent layer.
   */
  public static Point layerToParent(Layer layer, Layer parent, float x, float y) {
    Point into = new Point(x, y);
    return layerToParent(layer, parent, into, into);
  }

  /**
   * Converts the supplied point from screen coordinates to coordinates
   * relative to the specified layer. The results are stored into {@code into}
   * , which is returned for convenience.
   */
  public static Point screenToLayer(Layer layer, XY point, Point into) {
    Layer parent = layer.parent();
    XY cur = (parent == null) ? point : screenToLayer(parent, point, into);
    return parentToLayer(layer, cur, into);
  }

  /**
   * Converts the supplied point from screen coordinates to coordinates
   * relative to the specified layer.
   */
  public static Point screenToLayer(Layer layer, float x, float y) {
    Point into = new Point(x, y);
    return screenToLayer(layer, into, into);
  }

  /**
   * Converts the supplied point from coordinates relative to its parent
   * to coordinates relative to the specified layer. The results are stored
   * into {@code into}, which is returned for convenience.
   */
  public static Point parentToLayer(Layer layer, XY point, Point into) {
    layer.transform().inverseTransform(into.set(point), into);
    into.x += layer.originX();
    into.y += layer.originY();
    return into;
  }

  /**
   * Converts the supplied point from coordinates relative to the specified parent to coordinates
   * relative to the specified child layer. The results are stored into {@code into}, which is
   * returned for convenience.
   */
  public static Point parentToLayer(Layer parent, Layer layer, XY point, Point into) {
    into.set(point);
    Layer immediateParent = layer.parent();
    if (immediateParent != parent) parentToLayer(parent, immediateParent, into, into);
    parentToLayer(layer, into, into);
    return into;
  }

  /**
   * Returns the layer hit by (screen) position {@code p} (or null) in the scene graph rooted at
   * {@code root}, using {@link Layer#hitTest}. Note that {@code p} is mutated by this call.
   */
  public static Layer getHitLayer (Layer root, Point p) {
    root.transform().inverseTransform(p, p);
    p.x += root.originX();
    p.y += root.originY();
    return root.hitTest(p);
  }

  /**
   * Returns true if an {@link XY} touches a {@link Layer}. Note: if the supplied layer has no
   * size, this will always return false.
   */
  public static boolean hitTest(Layer layer, XY pos) {
    return hitTest(layer, pos.x(), pos.y());
  }

  /**
   * Returns true if a coordinate on the screen touches a {@link Layer}. Note: if the supplied
   * layer has no size, this will always return false.
   */
  public static boolean hitTest(Layer layer, float x, float y) {
    Point point = screenToLayer(layer, x, y);
    return (point.x() >= 0 &&  point.y() >= 0 &&
            point.x() <= layer.width() && point.y() <= layer.height());
  }

  /**
   * Gets the layer underneath the given screen coordinates, ignoring hit testers. This is
   * useful for inspecting the scene graph for debugging purposes, and is not intended for use
   * is shipped code. The layer returned is the one that has a size and is the deepest within
   * the graph and contains the coordinate.
   */
  public static Layer layerUnderPoint (Layer root, float x, float y) {
    Point p = new Point(x, y);
    root.transform().inverseTransform(p, p);
    p.x += root.originX();
    p.y += root.originY();
    return layerUnderPoint(root, p);
  }

  /**
   * Returns the index of the given layer within its parent, or -1 if the parent is null.
   */
  public static int indexInParent (Layer layer) {
    GroupLayer parent = layer.parent();
    if (parent == null) return -1;
    for (int ii = parent.children()-1; ii >= 0; ii--) {
      if (parent.childAt(ii) == layer) return ii;
    }
    throw new AssertionError();
  }

  /**
   * Automatically connects {@code onPaint} to {@code paint} when {@code layer} is added to a scene
   * graph, and disconnects it when {@code layer} is removed.
   */
  public static void bind (Layer layer, final Signal<Clock> paint, final Slot<Clock> onPaint) {
    layer.state.connectNotify(new Slot<Layer.State>() {
      public void onEmit (Layer.State state) {
        _pcon = Closeable.Util.close(_pcon);
        if (state == Layer.State.ADDED) _pcon = paint.connect(onPaint);
      }
      private Closeable _pcon = Closeable.Util.NOOP;
    });
  }

  /**
   * Automatically connects {@code onUpdate} to {@code update}, and {@code onPaint} to {@code
   * paint} when {@code layer} is added to a scene graph, and disconnects them when {@code layer}
   * is removed.
   */
  public static void bind (Layer layer, final Signal<Clock> update, final Slot<Clock> onUpdate,
                           final Signal<Clock> paint, final Slot<Clock> onPaint) {
    layer.state.connectNotify(new Slot<Layer.State>() {
      public void onEmit (Layer.State state) {
        _pcon = Closeable.Util.close(_pcon);
        _ucon = Closeable.Util.close(_ucon);
        if (state == Layer.State.ADDED) {
          _ucon = update.connect(onUpdate);
          _pcon = paint.connect(onPaint);
        }
      }
      private Closeable _ucon = Closeable.Util.NOOP, _pcon = Closeable.Util.NOOP;
    });
  }

  /**
   * Returns the depth of the given layer in its local scene graph. A root layer (one with null
   * parent) will always return 0.
   */
  public static int graphDepth (Layer layer) {
    int depth = -1;
    while (layer != null) {
      layer = layer.parent();
      depth++;
    }
    return depth;
  }

  /**
   * Prints the layer heirarchy starting at {@code layer}, using {@link Log#debug}.
   */
  public static void print (Log log, Layer layer) {
    print(log, layer, "");
  }

  /** Performs the recursion for {@link #layerUnderPoint(Layer,float,float)}. */
  protected static Layer layerUnderPoint (Layer layer, Point pt) {
    float x = pt.x, y = pt.y;
    if (layer instanceof GroupLayer) {
      GroupLayer gl = (GroupLayer)layer;
      for (int ii = gl.children()-1; ii >= 0; ii--) {
        Layer child = gl.childAt(ii);
        if (!child.visible()) continue; // ignore invisible children
        try {
          // transform the point into the child's coordinate system
          child.transform().inverseTransform(pt.set(x, y), pt);
          pt.x += child.originX();
          pt.y += child.originY();
          Layer l = layerUnderPoint(child, pt);
          if (l != null)
          return l;
        } catch (NoninvertibleTransformException nte) {
          continue;
        }
      }
    }
    if (x >= 0 && x < layer.width() && y >= 0 && y < layer.height()) {
      return layer;
    }
    return null;
  }

  private static void print (Log log, Layer layer, String prefix) {
    log.debug(prefix + layer);
    if (layer instanceof GroupLayer) {
      String gprefix = prefix + "  ";
      GroupLayer glayer = (GroupLayer)layer;
      for (int ii = 0, ll = glayer.children(); ii < ll; ii++) {
        print(log, glayer.childAt(ii), gprefix);
      }
    }
  }
}
