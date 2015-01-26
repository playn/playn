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

import pythagoras.f.AffineTransform;
import pythagoras.f.FloatMath;
import pythagoras.f.MathUtil;
import pythagoras.f.Point;

import react.Signal;
import react.Slot;
import react.Value;
import react.ValueView;

import playn.core.*;

/**
 * A layer is a node in the scene graph. It has a transformation matrix and other properties which
 * can be manipulated directly and which "take effect" the next time the layer is {@link #paint}ed.
 *
 * <p>Everything can be accomplished by extending {@link Layer} and overriding {@link #paintImpl}.
 * However, {@link GroupLayer}, {@link ImageLayer}, {@link ClippedLayer} etc. are provided to
 * make it easy to implement common use cases "out of the box".
 */
public abstract class Layer implements Disposable {

  /** Enumerates layer lifecycle states; see {@link #state}. */
  public static enum State { REMOVED, ADDED, DISPOSED }

  /** Used to customize a layer's hit testing mechanism. */
  public interface HitTester {
    /** Returns {@code layer}, or a child of {@code layer} if the supplied coordinate (which is in
     * {@code layer}'s coordinate system) hits {@code layer}, or one of its children. This allows a
     * layer to customize the default hit testing approach, which is to simply check whether the
     * point intersects a layer's bounds. See {@link Layer#hitTest}. */
    Layer hitTest (Layer layer, Point p);
  }

  /**
   * A reactive value which tracks this layer's lifecycle. It starts out {@link State#REMOVED}, and
   * transitions to {@link State#ADDED} when the layer is added to a scene graph root and back to
   * {@link State#REMOVED} when removed, until it is finally {@link #close}d at which point it
   * transitions to {@link State#DISPOSED}.
   */
  public final ValueView<State> state = Value.create(State.REMOVED);

  /** Creates an unclipped layer. The {@link #paint} method must be overridden by the creator. */
  public Layer() {
    setFlag(Flag.VISIBLE, true);
  }

  /** Returns the layer that contains this layer, or {@code null}. */
  public GroupLayer parent() { return parent; }

  /**
   * Returns a signal via which events may be dispatched "on" this layer. The {@code Dispatcher}
   * mechanism uses this to dispatch (and listen for) mouse, pointer and touch events to the layers
   * affected by them. A game can also use this to dispatch any other kinds of events on a
   * per-layer basis, with the caveat that all listeners are notified of every event and each must
   * do a type test on the event to determine whether it matches.
   *
   * <p>Also, any layer that has one or more listeners on its events signal is marked as {@link
   * #interactive}. Further, any {@link GroupLayer} which has one or more interactive children is
   * also marked as interactive. This allows {@code Dispatcher}s to be more efficient in their
   * dispatching of UI events.
   */
  public Signal<Object> events () {
    if (events == null) events = new Signal<Object>() {
      @Override protected void connectionAdded () {
        setInteractive(true);
      }
      @Override protected void connectionRemoved () {
        if (!hasConnections() && deactivateOnNoListeners()) setInteractive(false);
      }
    };
    return events;
  }

  /** Returns true if {@link #events} has at least one listener. Use this instead of calling {@link
    * Signal#hasConnections} on {@code events} because {@code events} is created lazily this method
    * avoids creating it unnecessarily. */
  public boolean hasEventListeners () {
    return events != null && events.hasConnections();
  }

  /**
   * Returns true if this layer reacts to clicks and touches. If a layer is interactive, it will
   * respond to {@link #hitTest}, which forms the basis for the click and touch processing provided
   * by the {@code Dispatcher}s.
   */
  public boolean interactive() {
    return isSet(Flag.INTERACTIVE);
  }

  /**
   * Configures this layer as reactive to clicks and touches, or not. You usually don't have to do
   * this automatically because a layer is automatically marked as interactive (along with all of
   * its parents) when a listener is added to its {@link #events} signal.
   *
   * <p>A {@link GroupLayer} will be made non-interactive automatically if an event is dispatched
   * to it and it discovers that it no longer has any interactive children. Manual management of
   * interactivity is thus generally only useful for "leaf" nodes in the scene graph.
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setInteractive(boolean interactive) {
    if (interactive() != interactive) {
      // if we're being made interactive, active our parent as well, if we have one
      if (interactive && parent != null) parent.setInteractive(interactive);
      setFlag(Flag.INTERACTIVE, interactive);
    }
    return this;
  }

  /**
   * Returns true if this layer is visible (i.e. it is being rendered).
   */
  public boolean visible() {
    return isSet(Flag.VISIBLE);
  }

  /**
   * Configures this layer's visibility: if true, it will be rendered as normal, if false it and
   * its children will not be rendered.
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setVisible(boolean visible) {
    setFlag(Flag.VISIBLE, visible);
    return this;
  }

  /**
   * Whether this layer has been disposed. If true, the layer can no longer be used.
   */
  public boolean disposed() {
    return state.get() == State.DISPOSED;
  }

  /** Connects {@code action} to {@link #state} such that it is triggered when this layer is added
    * to a rooted scene graph. */
  public void onAdded (final Slot<? super Layer> action) { onState(State.ADDED, action); }
  /** Connects {@code action} to {@link #state} such that it is triggered when this layer is
    * removed from a rooted scene graph. */
  public void onRemoved (final Slot<? super Layer> action) { onState(State.REMOVED, action); }
  /** Connects {@code action} to {@link #state} such that it is triggered when this layer is
    * disposed. */
  public void onDisposed (final Slot<? super Layer> action) { onState(State.DISPOSED, action); }

  private void onState (final State tgtState, final Slot<? super Layer> action) {
    state.connect(new Slot<State>() {
      public void onEmit (State state) {
        if (state == tgtState) action.onEmit(Layer.this);
      }
    });
  }

  /**
   * Disposes this layer, removing it from its parent layer. Any resources associated with this
   * layer are freed, and it cannot be reused after being disposed. Disposing a layer that has
   * children will dispose them as well.
   */
  @Override public void close() {
    if (parent != null) parent.remove(this);
    setState(State.DISPOSED);
    setBatch(null);
  }

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
  public AffineTransform transform() {
    if (isSet(Flag.XFDIRTY)) {
      float sina = FloatMath.sin(rotation), cosa = FloatMath.cos(rotation);
      float m00 =  cosa * scaleX, m01 = sina * scaleY;
      float m10 = -sina * scaleX, m11 = cosa * scaleY;
      float tx = transform.tx(), ty = transform.ty();
      transform.setTransform(m00, m01, m10, m11, tx, ty);
      setFlag(Flag.XFDIRTY, false);
    }
    return transform;
  }

  /**
   * Return the global alpha value for this layer.
   *
   * <p>The global alpha value for a layer controls the opacity of the layer but does not affect
   * the current drawing operation. I.e., when {@link Game#paint} is called and the {@link Layer}
   * is drawn, this alpha value is applied to the alpha channel of the Layer.</p>
   *
   * <p>By default, the alpha for a Layer is 1.0 (not transparent).</p>
   *
   * @return alpha in range [0,1] where 0 is transparent and 1 is opaque
   */
  public float alpha() {
    return alpha;
  }

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
  public Layer setAlpha(float alpha) {
    this.alpha = alpha;
    int ialpha = (int)(0xFF * MathUtil.clamp(alpha, 0, 1));
    this.tint = (ialpha << 24) | (tint & 0xFFFFFF);
    return this;
  }

  /** Returns the current tint for this layer, as {@code ARGB}. */
  public int tint() {
    return tint;
  }

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
   * drawing operation. I.e., when {@link Game#paint} is called and the {@link Layer} is drawn,
   * this tint is applied when rendering the layer. </p>
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setTint(int tint) {
    this.tint = tint;
    this.alpha = ((tint >> 24) & 0xFF) / 255f;
    return this;
  }

  /**
   * Returns the x-component of the layer's origin.
   */
  public float originX() {
    return originX;
  }

  /**
   * Returns the y-component of the layer's origin.
   */
  public float originY() {
    return originY;
  }

  /**
   * Sets the origin of the layer.
   * <p>
   * This sets the origin of the layer's transformation matrix.
   *
   * @param x origin on x axis in pixels.
   * @param y origin on y axis in pixels.
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setOrigin(float x, float y) {
    this.originX = x;
    this.originY = y;
    return this;
  }

  /**
   * Returns this layer's current depth.
   */
  public float depth() {
    return depth;
  }

  /**
   * Sets the depth of this layer.
   * <p>
   * Within a single {@link GroupLayer}, layers are rendered from lowest depth to highest depth.
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setDepth(float depth) {
    float oldDepth = this.depth;
    if (depth != oldDepth) {
      this.depth = depth;
      if (parent != null) parent.depthChanged(this, oldDepth);
    }
    return this;
  }

  /**
   * Returns this layer's current translation in the x direction.
   */
  public float tx() {
    return transform.tx();
  }

  /**
   * Returns this layer's current translation in the y direction.
   */
  public float ty() {
    return transform.ty();
  }

  /**
   * Sets the x translation of this layer.
   *
   * <p><em>Note:</em> all transform changes are deferred until {@link #transform} is called
   * (which happens during rendering, if not before) at which point the current scale, rotation and
   * translation are composed into an affine transform matrix. This means that, for example,
   * setting rotation and then setting scale will not flip the rotation like it would were these
   * applied to the transform matrix one operation at a time. </p>
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setTx(float x) {
    transform.setTx(x);
    return this;
  }

  /**
   * Sets the y translation of this layer.
   *
   * <p><em>Note:</em> all transform changes are deferred until {@link #transform} is called
   * (which happens during rendering, if not before) at which point the current scale, rotation and
   * translation are composed into an affine transform matrix. This means that, for example,
   * setting rotation and then setting scale will not flip the rotation like it would were these
   * applied to the transform matrix one operation at a time. </p>
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setTy(float y) {
    transform.setTy(y);
    return this;
  }

  /**
   * Sets the x and y translation of this layer.
   *
   * <p><em>Note:</em> all transform changes are deferred until {@link #transform} is called
   * (which happens during rendering, if not before) at which point the current scale, rotation and
   * translation are composed into an affine transform matrix. This means that, for example,
   * setting rotation and then setting scale will not flip the rotation like it would were these
   * applied to the transform matrix one operation at a time. </p>
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setTranslation(float x, float y) {
    transform.setTranslation(x, y);
    return this;
  }

  /**
   * Returns this layer's current scale in the x direction. <em>Note:</em> this is the most recent
   * value supplied to {@link #setScale(float)} or {@link #setScale(float,float)}, it is
   * <em>not</em> extracted from the underlying transform. Thus the sign of the scale returned by
   * this method is preserved. It's also substantially cheaper than extracting the scale from the
   * affine transform matrix. This also means that if you change the scale directly on the {@link
   * #transform} that scale <em>will not</em> be returned by this method.
   */
  public float scaleX() {
    return scaleX;
  }

  /**
   * Returns this layer's current scale in the y direction. <em>Note:</em> this is the most recent
   * value supplied to {@link #setScale(float)} or {@link #setScale(float,float)}, it is
   * <em>not</em> extracted from the underlying transform. Thus the sign of the scale returned by
   * this method is preserved. It's also substantially cheaper than extracting the scale from the
   * affine transform matrix. This also means that if you change the scale directly on the {@link
   * #transform} that scale <em>will not</em> be returned by this method.
   */
  public float scaleY() {
    return scaleY;
  }

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
   * @param scale non-zero scale value.
   * @return a reference to this layer for call chaining.
   */
  public Layer setScale(float scale) {
    return setScale(scale, scale);
  }

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
   * @param sx non-zero scale value.
   * @return a reference to this layer for call chaining.
   */
  public Layer setScaleX(float sx) {
    if (scaleX != sx) {
      scaleX = sx;
      setFlag(Flag.XFDIRTY, true);
    }
    return this;
  }

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
   * @param sy non-zero scale value.
   * @return a reference to this layer for call chaining.
   */
  public Layer setScaleY(float sy) {
    if (scaleY != sy) {
      scaleY = sy;
      setFlag(Flag.XFDIRTY, true);
    }
    return this;
  }

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
   * @param sx non-zero scale value for the x axis.
   * @param sy non-zero scale value for the y axis.
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setScale(float sx, float sy) {
    if (sx != scaleX || sy != scaleY) {
      scaleX = sx;
      scaleY = sy;
      setFlag(Flag.XFDIRTY, true);
    }
    return this;
  }

  /**
   * Returns this layer's current rotation. <em>Note:</em> this is the most recent value supplied
   * to {@link #setRotation}, it is <em>not</em> extracted from the underlying transform. Thus the
   * value may lie outside the range [-pi, pi] and the most recently set value is preserved. It's
   * also substantially cheaper than extracting the rotation from the affine transform matrix. This
   * also means that if you change the scale directly on the {@link #transform} that rotation
   * <em>will not</em> be returned by this method.
   */
  public float rotation() {
    return rotation;
  }

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
   * @param angle angle to rotate, in radians.
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setRotation(float angle) {
    if (rotation != angle) {
      rotation = angle;
      setFlag(Flag.XFDIRTY, true);
    }
    return this;
  }

  /** Returns the width of the layer.
    * NOTE: not all layers know their size. Those that don't return 0. */
  public float width () {
    return 0;
  }

  /** Returns the height of the layer.
    * NOTE: not all layers know their size. Those that don't return 0. */
  public float height () {
    return 0;
  }

  /** Returns the width of the layer multiplied by its x scale.
    * NOTE: not all layers know their size. Those that don't return 0. */
  public float scaledWidth () {
    return scaleX() * width();
  }

  /** Returns the height of the layer multiplied by its y scale.
    * NOTE: not all layers know their size. Those that don't return 0. */
  public float scaledHeight () {
    return scaleX() * height();
  }

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
  public Layer hitTest(Point p) {
    return (hitTester == null) ? hitTestDefault(p) : hitTester.hitTest(this, p);
  }

  /**
   * Like {@link #hitTest} except that it ignores a configured {@link HitTester}. This allows one
   * to configure a hit tester which checks custom properties and then falls back on the default
   * hit testing implementation.
   */
  public Layer hitTestDefault(Point p) {
    return (p.x >= 0 && p.y >= 0 && p.x < width() && p.y < height()) ? this : null;
  }

  /**
   * Configures a custom hit tester for this layer. May also be called with null to clear out any
   * custom hit tester.
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setHitTester (HitTester tester) {
    hitTester = tester;
    return this;
  }

  /**
   * Configures a custom batch (i.e. shader) for use when rendering this layer (and its children).
   * Passing null will cause the default batch to be used. Configuring a batch on a group layer
   * will cause that shader to be used when rendering the group layer's children, unless the child
   * has a custom batch configured itself.
   *
   * @return a reference to this layer for call chaining.
   */
  public Layer setBatch (QuadBatch batch) {
    this.batch = batch;
    return this;
  }

  /**
   * Renders this layer to {@code surf}, including its children.
   */
  public final void paint (Surface surf) {
    if (!visible()) return;

    int otint = surf.combineTint(tint);
    QuadBatch obatch = surf.pushBatch(batch);
    surf.concatenate(transform(), originX, originY);
    try {
      paintImpl(surf);
    } finally {
      surf.popBatch(obatch);
      surf.setTint(otint);
    }
  }

  /**
   * Implements the actual rendering of this layer. The surface will be fully prepared for this
   * layer's rendering prior to calling this method: the layer transform will have been
   * concatenated with the surface transform, the layer's tint will have been applied, and any
   * custom batch will have been pushed onto the layer.
   */
  protected abstract void paintImpl (Surface surf);

  protected void setState (State state) {
    ((Value<State>)this.state).update(state);
  }

  @Override public String toString () {
    String cname = getClass().getName();
    StringBuilder bldr = new StringBuilder(cname.substring(cname.lastIndexOf(".")+1));
    bldr.append(" [hashCode=").append(hashCode());
    bldr.append(", tx=").append(transform());
    if (hitTester != null) bldr.append(", hitTester=").append(hitTester);
    return bldr.toString();
  }

  protected int flags;
  protected float depth;

  private GroupLayer parent;
  private Signal<Object> events; // created lazily
  private HitTester hitTester;
  private QuadBatch batch;

  // these values are cached in the layer to make the getters return sane values rather than have
  // to extract the values from the affine transform matrix (which is expensive, doesn't preserve
  // sign, and wraps rotation around at pi)
  private float scaleX = 1, scaleY = 1, rotation = 0;
  private final AffineTransform transform = new AffineTransform();

  private float clipWidth, clipHeight;
  protected float originX, originY;
  protected int tint = Tint.NOOP_TINT;
  // we keep a copy of alpha as a float so that we can return the exact alpha passed to setAlpha()
  // from alpha() to avoid funny business in clients due to the quantization; the actual alpha as
  // rendered by the shader will be quantized, but the eye won't know the difference
  protected float alpha = 1;

  void onAdd() {
    if (disposed()) throw new IllegalStateException("Illegal to use disposed layer: " + this);
    setState(State.ADDED);
  }
  void onRemove() {
    setState(State.REMOVED);
  }
  void setParent(GroupLayer parent) { this.parent = parent; }

  /** Enumerates bit flags tracked by this layer. */
  protected static enum Flag {
    VISIBLE(1 << 0),
    INTERACTIVE(1 << 1),
    XFDIRTY(1 << 2);

    public final int bitmask;

    Flag(int bitmask) {
      this.bitmask = bitmask;
    }
  }

  /** Returns true if {@code flag} is set. */
  protected boolean isSet(Flag flag) {
    return (flags & flag.bitmask) != 0;
  }

  /** Sets {@code flag} to {@code active}. */
  protected void setFlag(Flag flag, boolean active) {
    if (active) {
      flags |= flag.bitmask;
    } else {
      flags &= ~flag.bitmask;
    }
  }

  /** Whether or not to deactivate this layer when its last event listener is removed. */
  protected boolean deactivateOnNoListeners () { return true; }
}
