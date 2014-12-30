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
import pythagoras.util.NoninvertibleTransformException;

import playn.core.gl.GLShader;

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
  interface HitTester {
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
   * Returns the layer's current transformation matrix. If any changes have been made to the
   * layer's scale, rotation or translation, they will be applied to the transform matrix before it
   * is returned.
   *
   * <p><em>Note:</em> any direct modifications to this matrix <em>except</em> modifications to its
   * translation, will be overwritten if a call is subsequently made to {@link #setScale(float)},
   * {@link #setScale(float,float)}, {@link #setScaleX}, {@link #setScaleY} or {@link #setRotation}.
   * If you intend to manipulate a layer's transform matrix directly, <em>do not</em> call those
   * other methods. Also do not expect {@link #scaleX}, {@link #scaleY}, or {@link #rotation} to
   * reflect the direct changes you've made to the transform matrix. They will not. </p>
   */
  Transform transform();

  /**
   * Returns true if this layer is visible (i.e. it is being rendered).
   */
  boolean visible();

  /**
   * Configures this layer's visibility: if true, it will be rendered as normal, if false it and
   * its children will not be rendered.
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setVisible(boolean visible);

  /**
   * Returns true if this layer reacts to clicks and touches. If a layer is interactive, it will
   * respond to {@link #hitTest}, which forms the basis for the click and touch processing provided
   * by the various {@code addListener} methods.
   */
  boolean interactive();

  /**
   * Configures this layer as reactive to clicks and touches, or not. Note that a layer's
   * interactivity is automatically activated when a listener is added to the layer (or to a child
   * of a {@link GroupLayer}) via {@link #addListener}, etc. Also a {@link GroupLayer} will be made
   * non-interactive automatically if an event is dispatched to it and it discovers that it has no
   * interactive children. Manual management of interactivity is thus generally only useful for
   * "leaf" nodes in the scene graph.
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setInteractive(boolean interactive);

  /**
   * Return the global alpha value for this layer.
   *
   * <p>The global alpha value for a layer controls the opacity of the layer but does not affect
   * the current drawing operation. I.e., when {@link Game.Default#paint(float)} is called and the
   * {@link Layer} is drawn, this alpha value is applied to the alpha channel of the Layer.</p>
   *
   * <p>By default, the alpha for a Layer is 1.0 (not transparent).</p>
   *
   * @return alpha in range [0,1] where 0 is transparent and 1 is opaque
   */
  float alpha();

  /**
   * Sets the alpha component of this layer's current tint. Note that this value will be quantized
   * to an integer between 0 and 255. Also see {@link #setTint}.
   *
   * <p> Values outside the range [0,1] will be clamped to the range [0,1]. </p>
   *
   * @param alpha alpha value in range [0,1] where 0 is transparent and 1 is opaque.
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setAlpha(float alpha);

  /** Returns the current tint for this layer, as {@code ARGB}. */
  int tint();

  /**
   * Sets the tint for this layer, as {@code ARGB}.
   *
   * <p> <em>NOTE:</em> this will overwrite any value configured via {@link #setAlpha}. Either
   * include your desired alpha in the high bits of {@code tint} or call {@link #setAlpha} after
   * calling this method. </p>
   *
   * <p> <em>NOTE:</em> the RGB components of a layer's tint only work on GL-based backends. It is
   * not possible to tint layers using the HTML5 canvas and Flash backends. </p>
   *
   * <p> The tint for a layer controls the opacity of the layer but does not affect the current
   * drawing operation. I.e., when {@link Game.Default#paint(float)} is called and the {@link
   * Layer} is drawn, this tint is applied when rendering the layer. </p>
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setTint(int tint);

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
   * @param x origin on x axis in pixels
   * @param y origin on y axis in pixels
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setOrigin(float x, float y);

  /**
   * Returns this layer's current depth.
   */
  float depth();

  /**
   * Sets the depth of this layer.
   * <p>
   * Within a single {@link GroupLayer}, layers are rendered from lowest depth to highest depth.
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setDepth(float depth);

  /**
   * Returns this layer's current translation in the x direction.
   */
  float tx();

  /**
   * Returns this layer's current translation in the y direction.
   */
  float ty();

  /**
   * Sets the x translation of this layer.
   *
   * <p><em>Note:</em> all transform changes are deferred until {@link #transform} is called
   * (which happens during rendering, if not before) at which point the current scale, rotation and
   * translation are composed into an affine transform matrix. This means that, for example,
   * setting rotation and then setting scale will not flip the rotation like it would were these
   * applied to the transform matrix one operation at a time. </p>
   *
   * @param tx translation on x axis
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setTx(float tx);

  /**
   * Sets the y translation of this layer.
   *
   * <p><em>Note:</em> all transform changes are deferred until {@link #transform} is called
   * (which happens during rendering, if not before) at which point the current scale, rotation and
   * translation are composed into an affine transform matrix. This means that, for example,
   * setting rotation and then setting scale will not flip the rotation like it would were these
   * applied to the transform matrix one operation at a time. </p>
   *
   * @param ty translation on y axis
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setTy(float ty);

  /**
   * Sets the x and y translation of this layer.
   *
   * <p><em>Note:</em> all transform changes are deferred until {@link #transform} is called
   * (which happens during rendering, if not before) at which point the current scale, rotation and
   * translation are composed into an affine transform matrix. This means that, for example,
   * setting rotation and then setting scale will not flip the rotation like it would were these
   * applied to the transform matrix one operation at a time. </p>
   *
   * @param x translation on x axis
   * @param y translation on y axis
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setTranslation(float x, float y);

  /**
   * Returns this layer's current scale in the x direction. <em>Note:</em> this is the most recent
   * value supplied to {@link #setScale(float)} or {@link #setScale(float,float)}, it is
   * <em>not</em> extracted from the underlying transform. Thus the sign of the scale returned by
   * this method is preserved. It's also substantially cheaper than extracting the scale from the
   * affine transform matrix. This also means that if you change the scale directly on the {@link
   * #transform} that scale <em>will not</em> be returned by this method.
   */
  float scaleX();

  /**
   * Returns this layer's current scale in the y direction. <em>Note:</em> this is the most recent
   * value supplied to {@link #setScale(float)} or {@link #setScale(float,float)}, it is
   * <em>not</em> extracted from the underlying transform. Thus the sign of the scale returned by
   * this method is preserved. It's also substantially cheaper than extracting the scale from the
   * affine transform matrix. This also means that if you change the scale directly on the {@link
   * #transform} that scale <em>will not</em> be returned by this method.
   */
  float scaleY();

  /**
   * Sets the current x and y scale of this layer to {@code scale}.. Note that a scale of {@code 1}
   * is equivalent to no scale.
   *
   * <p><em>Note:</em> all transform changes are deferred until {@link #transform} is called
   * (which happens during rendering, if not before) at which point the current scale, rotation and
   * translation are composed into an affine transform matrix. This means that, for example,
   * setting rotation and then setting scale will not flip the rotation like it would were these
   * applied to the transform matrix one operation at a time. </p>
   *
   * @param scale non-zero scale value
   * @return a reference to this layer for call chaining.
   */
  Layer setScale(float scale);

  /**
   * Sets the current x scale of this layer. Note that a scale of {@code 1} is equivalent to no
   * scale.
   *
   * <p><em>Note:</em> all transform changes are deferred until {@link #transform} is called
   * (which happens during rendering, if not before) at which point the current scale, rotation and
   * translation are composed into an affine transform matrix. This means that, for example,
   * setting rotation and then setting scale will not flip the rotation like it would were these
   * applied to the transform matrix one operation at a time. </p>
   *
   * @param scaleX non-zero scale value
   * @return a reference to this layer for call chaining.
   */
  Layer setScaleX(float scaleX);

  /**
   * Sets the current y scale of this layer. Note that a scale of {@code 1} is equivalent to no
   * scale.
   *
   * <p><em>Note:</em> all transform changes are deferred until {@link #transform} is called
   * (which happens during rendering, if not before) at which point the current scale, rotation and
   * translation are composed into an affine transform matrix. This means that, for example,
   * setting rotation and then setting scale will not flip the rotation like it would were these
   * applied to the transform matrix one operation at a time. </p>
   *
   * @param scaleY non-zero scale value
   * @return a reference to this layer for call chaining.
   */
  Layer setScaleY(float scaleY);

  /**
   * Sets the current x and y scale of this layer. Note that a scale of {@code 1} is equivalent to
   * no scale.
   *
   * <p><em>Note:</em> all transform changes are deferred until {@link #transform} is called
   * (which happens during rendering, if not before) at which point the current scale, rotation and
   * translation are composed into an affine transform matrix. This means that, for example,
   * setting rotation and then setting scale will not flip the rotation like it would were these
   * applied to the transform matrix one operation at a time. </p>
   *
   * @param scaleX non-zero scale value on the x axis
   * @param scaleY non-zero scale value on the y axis
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setScale(float scaleX, float scaleY);

  /**
   * Returns this layer's current rotation. <em>Note:</em> this is the most recent value supplied
   * to {@link #setRotation}, it is <em>not</em> extracted from the underlying transform. Thus the
   * value may lie outside the range [-pi, pi] and the most recently set value is preserved. It's
   * also substantially cheaper than extracting the rotation from the affine transform matrix. This
   * also means that if you change the scale directly on the {@link #transform} that rotation
   * <em>will not</em> be returned by this method.
   */
  float rotation();

  /**
   * Sets the current rotation of this layer, in radians. The rotation is done around the currently
   * set origin, See {@link Layer#setOrigin}.
   *
   * <p><em>Note:</em> all transform changes are deferred until {@link #transform} is called
   * (which happens during rendering, if not before) at which point the current scale, rotation and
   * translation are composed into an affine transform matrix. This means that, for example,
   * setting rotation and then setting scale will not flip the rotation like it would were these
   * applied to the transform matrix one operation at a time. </p>
   *
   * @param angle angle to rotate, in radians
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setRotation(float angle);

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
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setHitTester(HitTester tester);

  /**
   * Registers a listener with this layer that will be notified if a click/touch event happens
   * within its bounds. Events dispatched to this listener will have their {@code localX} and
   * {@code localY} values set to the coordinates of the click/touch as transformed into this
   * layer's coordinate system. {@code x} and {@code y} will always contain the screen (global)
   * coordinates of the click/touch.
   *
   * <p>When a listener is added, the layer and all of its parents are marked as interactive.
   * Interactive layers intercept touches/clicks. When all listeners are disconnected (including
   * Mouse and Touch listeners), the layer will be marked non-interactive. Its parents are lazily
   * marked non-interactive as it is discovered that they have no interactive children. Thus if you
   * require that a layer continue to intercept click/touch events to prevent them from being
   * dispatched to layers "below" it, you must register a NOOP listener on the layer, or manually
   * call {@link #setInteractive} after removing the last listener.</p>
   */
  Connection addListener(Pointer.Listener pointerListener);

  /**
   * Registers a listener with this layer that will be notified if a mouse event happens within its
   * bounds. Events dispatched to this listener will have their {@code localX} and {@code localY}
   * values set to the coordinates of the mouse as transformed into this layer's coordinate system.
   * {@code x} and {@code y} will always contain the screen (global) coordinates of the mouse.
   *
   * <p>When a listener is added, the layer and all of its parents are marked as interactive.
   * Interactive layers intercept mice events. When all listeners are disconnected (including
   * Pointer and Touch listeners), the layer will be marked non-interactive. Its parents are lazily
   * marked non-interactive as it is discovered that they have no interactive children. Thus if you
   * require that a layer continue to intercept mouse events to prevent them from being dispatched
   * to layers "below" it, you must register a NOOP listener on the layer, or manually call {@link
   * #setInteractive} after removing the last listener.</p>
   */
  Connection addListener(Mouse.LayerListener mouseListener);

  /**
   * Registers a listener with this layer that will be notified if a touch event happens within its
   * bounds. Events dispatched to this listener will have their {@code localX} and {@code localY}
   * values set to the coordinates of the touch as transformed into this layer's coordinate system.
   * {@code x} and {@code y} will always contain the screen (global) coordinates of the touch.
   *
   * <p>When a listener is added, the layer and all of its parents are marked as interactive.
   * Interactive layers intercept touches/clicks. When all listeners are disconnected (including
   * Mouse and Touch listeners), the layer will be marked non-interactive. Its parents are lazily
   * marked non-interactive as it is discovered that they have no interactive children. Thus if you
   * require that a layer continue to intercept click/touch events to prevent them from being
   * dispatched to layers "below" it, you must register a NOOP listener on the layer, or manually
   * call {@link #setInteractive} after removing the last listener.</p>
   */
  Connection addListener(Touch.LayerListener touchListener);

  /**
   * Configures a custom shader for use when rendering this layer (and its children). Passing null
   * will cause the default shader to be used. Configuring a shader on a group layer will cause
   * that shader to be used when rendering the group layer's children, unless the child has a
   * custom shader configured itself.
   *
   * @return a reference to this layer for call chaining.
   */
  Layer setShader(GLShader shader);

  /**
   * Interface for {@link Layer}s containing explicit sizes.
   */
  interface HasSize extends Layer {
    /** Returns the width of the layer. */
    float width();

    /** Returns the height of the layer. */
    float height();

    /** Returns the width of the layer after applying scale. */
    float scaledWidth();

    /** Returns the height of the layer after applying scale. */
    float scaledHeight();
  }

  /**
   * Utility class for transforming coordinates between {@link Layer}s.
   */
  static class Util {
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

    /**
     * Gets the layer underneath the given screen coordinates, ignoring hit testers. This is
     * useful for inspecting the scene graph for debugging purposes, and is not intended for use
     * is shipped code. The layer returned is the one that has a size and is the deepest within
     * the graph and contains the coordinate.
     */
    public static Layer.HasSize layerUnderPoint (float x, float y) {
      GroupLayer root = PlayN.graphics().rootLayer();
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
      if (parent == null) {
        return -1;
      }
      for (int ii = parent.size()-1; ii >= 0; ii--) {
        if (parent.get(ii) == layer) {
          return ii;
        }
      }
      throw new AssertionError();
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
     * Performs the recursion for {@link layerUnderPoint(float, float)}.
     */
    protected static Layer.HasSize layerUnderPoint (Layer layer, Point pt) {
      float x = pt.x, y = pt.y;
      if (layer instanceof GroupLayer) {
        GroupLayer gl = (GroupLayer)layer;
        for (int ii = gl.size()-1; ii >= 0; ii--) {
          Layer child = gl.get(ii);
          if (!child.visible()) continue; // ignore invisible children
          try {
            // transform the point into the child's coordinate system
            child.transform().inverseTransform(pt.set(x, y), pt);
            pt.x += child.originX();
            pt.y += child.originY();
            Layer.HasSize l = layerUnderPoint(child, pt);
            if (l != null)
              return l;
          } catch (NoninvertibleTransformException nte) {
            continue;
          }
        }
      }
      if (layer instanceof Layer.HasSize) {
        Layer.HasSize sl = (Layer.HasSize)layer;
        if (x >= 0 && x < sl.width() && y >= 0 && y < sl.height()) {
          return sl;
        }
      }
      return null;
    }
  }
}
