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

import pythagoras.f.IPoint;
import pythagoras.f.Point;
import pythagoras.f.Transform;

/**
 * Layer is the base element for all rendering in PlayN
 * <p>
 * Each layer has a transformation matrix {@link #transform()} and several other associated
 * properties which can be manipulated directly (changes take effect automatically on the next
 * rendered frame).
 * <p>
 * The root of the layer hierarchy is the {@link Graphics#rootLayer() rootLayer} . All coordinates
 * in a layer are transformed by the layer's transformation matrix, and each child layer is
 * positioned by the transformation matrix of it's parent.
 * <p>
 * TODO: clipping (?), transform-origin: allow explicit
 * "center, top-left, bottom-right" like CSS transform-origin?
 */
public interface Layer {

  /** Used to customize a layer's hit testing mechanism. */
  public interface HitTester {
    /** Returns {@code layer}, or a child of {@code layer} if the supplied coordinate (which is in
     * {@code layer}'s coordinate system) hits {@code layer}, or one of its children. This allows a
     * layer to customize the default hit testing approach, which is to simply check whether the
     * point intersects a layer's bounds. See {@link Layer#hitTest}. */
    Layer hitTest (Layer layer, Point p);
  }

  /**
   * Destroys this layer, removing it from its parent layer. Any resources associated with this
   * layer are freed, and it cannot be reused after being destroyed. Destroying a layer that has
   * children will destroy them as well.
   */
  void destroy();

  /**
   * Whether this layer has been destroyed. If true, the layer can no longer be used.
   */
  boolean destroyed();

  /**
   * Returns the parent that contains this layer, or {@code null}.
   */
  GroupLayer parent();

  /**
   * Returns the layer's transformation matrix.
   */
  Transform transform();

  /**
   * Returns true if this layer is visible (i.e. it is being rendered).
   */
  boolean visible();

  /**
   * Configures this layer's visibility: if true, it will be rendered as normal, if false it and
   * its children will not be rendered.
   */
  void setVisible(boolean visible);

  /**
   * Returns true if this layer reacts to clicks and touches. If a layer is interactive, it will
   * respond to {@link #hitTest}, which forms the basis for the click and touch processing provided
   * by {@link Pointer#addListener}, {@link Touch#addListener} and {@link Mouse#addListener}.
   */
  boolean interactive();

  /**
   * Configures this layer as reactive to clicks and touches, or not. Note that a layer's
   * interactivity is automatically activated when a listener is added to the layer (or to a child
   * of a {@link GroupLayer}) via {@link Pointer#addListener}, etc. Also a {@link GroupLayer} will
   * be made non-interactive automatically if an event is dispatched to it and it discovers that it
   * has no interactive children. Manual management of interactivity is thus generally only useful
   * for "leaf" nodes in the scene graph.
   */
  void setInteractive(boolean interactive);

  /**
   * Return the global alpha value for this layer.
   * <p>
   * The global alpha value for a layer controls the opacity of the layer but does not affect the
   * current drawing operation. I.e., when {@link Game#paint(float)} is called and the {@link Layer}
   * is drawn, this alpha value is applied to the alpha channel of the Layer.
   * <p>
   * By default, the alpha for a Layer is 1.0 (not transparent).
   *
   * @return alpha in range [0,1] where 0 is transparent and 1 is opaque
   */
  float alpha();

  /**
   * Set the global alpha value for this layer.
   * <p>
   * The global alpha value for a layer controls the opacity of the layer but does not affect the
   * current drawing operation. I.e., when {@link Game#paint(float)} is called and the {@link Layer}
   * is drawn, this alpha value is applied to the alpha channel of the Layer.
   * <p>
   * Values outside the range [0,1] will be clamped to the range [0,1].
   *
   * @param alpha alpha value in range [0,1] where 0 is transparent and 1 is opaque
   */
  void setAlpha(float alpha);

  /**
   * Returns the x-component of the layer's origin.
   */
  float originX();

  /**
   * Returns the y-component of the layer's origin.
   */
  float originY();

  /**
   * Sets the origin of the layer.
   * <p>
   * This sets the origin of the layer's transformation matrix.
   *
   * @param x origin on x axis
   * @param y origin on y axis
   */
  void setOrigin(float x, float y);

  /**
   * Sets the depth of this layer. Within a single {@link GroupLayer}, layers are rendered from
   * lowest depth to highest depth.
   */
  float depth();

  /**
   * Updates this layer's depth.
   */
  void setDepth(float depth);

  /**
   * Sets the translation of the layer.
   * <p>
   * This sets the translation of the layer's transformation matrix so coordinates in the layer will
   * be translated by this amount.
   *
   * @param x translation on x axis
   * @param y translation on y axis
   */
  void setTranslation(float x, float y);

  /**
   * Sets the scale of the layer.
   * <p>
   * This sets the scale of the layer's transformation matrix so coordinates in the layer will be
   * multiplied by this scale.
   * <p>
   * Note that a scale of {@code 1} is equivalent to no scale.
   *
   * @param x non-zero scale value
   */
  void setScale(float x);

  /**
   * Sets the scale of the layer.
   * <p>
   * This sets the scale of the layer's transformation matrix so coordinates in the layer will be
   * multiplied by this scale.
   * <p>
   * Note that a scale of {@code 1} is equivalent to no scale.
   *
   * @param x non-zero scale value on the x axis
   * @param y non-zero scale value on the y axis
   */
  void setScale(float x, float y);

  /**
   * Sets the rotation of the layer.
   * <p>
   * This sets the rotation of the layer's transformation matrix so coordinates in the layer will be
   * rotated by this angle.
   * <p>
   *
   * @param angle angle to rotate, in radians
   */
  void setRotation(float angle);

  /**
   * Tests whether the supplied (layer relative) point "hits" this layer or any of its children. By
   * default a hit is any point that falls in a layer's bounding box. A group layer checks its
   * children for hits.
   *
   * <p>Note that this method mutates the supplied point. If a layer is hit, the point will contain
   * the original point as translated into that layer's coordinate system. If no layer is hit, the
   * point will be changed to an undefined value.</p>
   *
   * @return this layer if it was the hit layer, a child of this layer if a child of this layer was
   * hit, or null if neither this layer, nor its children were hit.
   */
  Layer hitTest(Point p);

  /**
   * Like {@link #hitTest} except that it ignores a configured {@link HitTester}. This allows one
   * to configure a hit tester which checks custom properties and then falls back on the default
   * hit testing implementation.
   */
  Layer hitTestDefault(Point p);

  /**
   * Configures a custom hit tester for this layer. May also be called with null to clear out any
   * custom hit tester.
   */
  void setHitTester (HitTester tester);

  /**
   * Registers a listener with this layer that will be notified if a click/touch event happens
   * within its bounds. Events dispatched to this listener will have their {@link Event#localX} and
   * {@link Event#localY} values set to the coordinates of the click/touch as transformed into this
   * layer's coordinate system. {@link Event#x} and {@link Event#y} will always contain the screen
   * (global) coordinates of the click/touch.
   *
   * <p>When a listener is added, the layer and all of its parents are marked as interactive.
   * Interactive layers intercept touches/clicks. When all listeners are disconnected (including
   * Mouse and Touch listeners), the layer will be marked non-interactive. Its parents are lazily
   * marked non-interactive as it is discovered that they have no interactive children. Thus if you
   * require that a layer continue to intercept click/touch events to prevent them from being
   * dispatched to layers "below" it, you must register a NOOP listener on the layer, or manually
   * call {@link #setInteractive} after removing the last listener.</p>
   */
  Connection addListener(Pointer.Listener listener);

  /**
   * Registers a listener with this layer that will be notified if a mouse event happens within its
   * bounds. Events dispatched to this listener will have their {@link Event#localX} and {@link
   * Event#localY} values set to the coordinates of the mouse as transformed into this layer's
   * coordinate system. {@link Event#x} and {@link Event#y} will always contain the screen (global)
   * coordinates of the mouse.
   *
   * <p>Note that mouse wheel events are not dispatched, as they lack coordinates. If a game wishes
   * to determine the layer over which the mouse is hovering when wheel events are dispatched, it
   * can register a global mouse-movement handler to track the position of the mouse, and use the
   * latest position to determine which layer is under the mouse at the time of wheeling.</p>
   *
   * <p>When a listener is added, the layer and all of its parents are marked as interactive.
   * Interactive layers intercept mice events. When all listeners are disconnected (including
   * Pointer and Touch listeners), the layer will be marked non-interactive. Its parents are lazily
   * marked non-interactive as it is discovered that they have no interactive children. Thus if you
   * require that a layer continue to intercept mouse events to prevent them from being dispatched
   * to layers "below" it, you must register a NOOP listener on the layer, or manually call {@link
   * #setInteractive} after removing the last listener.</p>
   */
  Connection addListener(Mouse.Listener listener);

  /**
   * Interface for {@link Layer}s containing explicit sizes.
   */
  public interface HasSize extends Layer {
    /**
     * Return the width of the layer.
     */
    public float width();

    /**
     * Return the height of the layer.
     */
    public float height();

    /**
     * Return the width of the layer after applying scale.
     */
    public float scaledWidth();

    /**
     * Return the height of the layer after applying scale.
     */
    public float scaledHeight();
  }

  /**
   * Utility class for transforming coordinates between {@link Layer}s.
   */
  public static class Util {
    /**
     * Converts the supplied point from coordinates relative to the specified
     * layer to screen coordinates. The results are stored into {@code into},
     * which is returned for convenience.
     */
    public static Point layerToScreen(Layer layer, IPoint point, Point into) {
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
    public static Point layerToParent(Layer layer, Layer parent, IPoint point, Point into) {
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
    public static Point screenToLayer(Layer layer, IPoint point, Point into) {
      Layer parent = layer.parent();
      IPoint cur = (parent == null) ? point : screenToLayer(parent, point, into);
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
    public static Point parentToLayer(Layer layer, IPoint point, Point into) {
      layer.transform().inverseTransform(point, into);
      into.x += layer.originX();
      into.y += layer.originY();
      return into;
    }

    /**
     * Converts the supplied point from coordinates relative to the specified parent to coordinates
     * relative to the specified child layer. The results are stored into {@code into}, which is
     * returned for convenience.
     */
    public static Point parentToLayer(Layer parent, Layer layer, IPoint point, Point into) {
      Layer immediateParent = layer.parent();
      if (immediateParent != parent)
        point = parentToLayer(parent, immediateParent, point, into);
      parentToLayer(layer, point, into);
      return into;
    }

    /**
     * Returns true if a {@link IPoint} on the screen touches a {@link Layer.HasSize}.
     */
    public static boolean hitTest(Layer.HasSize layer, IPoint point) {
      return hitTest(layer, point.x(), point.y());
    }

    /**
     * Returns true if a {@link Events.Position} touches a {@link Layer.HasSize}.
     */
    public static boolean hitTest(Layer.HasSize layer, Events.Position position) {
      return hitTest(layer, position.x(), position.y());
    }

    /**
     * Returns true if a coordinate on the screen touches a {@link Layer.HasSize}.
     */
    public static boolean hitTest(Layer.HasSize layer, float x, float y) {
      Point point = screenToLayer(layer, x, y);
      return (
          point.x() >= 0 &&  point.y() >= 0 &&
          point.x() <= layer.width() && point.y() <= layer.height());
    }
  }
}
