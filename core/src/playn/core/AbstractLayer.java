/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core;

import pythagoras.f.FloatMath;
import pythagoras.f.MathUtil;
import pythagoras.f.Point;
import pythagoras.f.Transform;

import playn.core.Layer;
import playn.core.gl.GLShader;

/**
 * Base {@link Layer} implementation shared among platforms (to avoid reinventing the transform
 * logic).
 */
public abstract class AbstractLayer implements Layer {

  // these values are cached in the layer to make the getters return sane values rather than have
  // to extract the values from the affine transform matrix (which is expensive, doesn't preserve
  // sign, and wraps rotation around at pi)
  private float scaleX = 1,  scaleY = 1, rotation = 0;
  private InternalTransform transform;

  /** Used to dispatch pointer/touch/mouse events to layers. */
  public interface Interaction<L, E> {
    void interact(L listener, E argument);
  }

  protected static class Interactor<L> {
    final Class<L> listenerType;
    final L listener;
    Interactor<?> next;

    Interactor(Class<L> listenerType, L listener, Interactor<?> next) {
      this.listenerType = listenerType;
      this.listener = listener;
      this.next = next;
    }
  }

  protected static enum Flag {
    DESTROYED(1 << 0),
    VISIBLE(1 << 1),
    INTERACTIVE(1 << 2),
    XFDIRTY(1 << 3);

    public final int bitmask;

    Flag(int bitmask) {
      this.bitmask = bitmask;
    }
  }

  private GroupLayer parent;

  protected float originX, originY;
  protected int tint = Tint.NOOP_TINT;
  // we keep a copy of alpha as a float so that we can return the exact alpha passed to setAlpha()
  // from alpha() to avoid funny business in clients due to the quantization; the actual alpha as
  // rendered by the shader will be quantized, but the eye won't know the difference
  protected float alpha = 1;
  protected float depth;
  protected int flags;
  protected Interactor<?> rootInteractor;
  protected HitTester hitTester;

  protected AbstractLayer() {
    this(new StockInternalTransform());
  }

  protected AbstractLayer(InternalTransform transform) {
    this.transform = transform;
    setFlag(Flag.VISIBLE, true);
  }

  @Override
  public void destroy() {
    if (parent() != null) {
      parent().remove(this);
    }
    setFlag(Flag.DESTROYED, true);
  }

  @Override
  public boolean destroyed() {
    return isSet(Flag.DESTROYED);
  }

  @Override
  public boolean visible() {
    return isSet(Flag.VISIBLE);
  }

  @Override
  public Layer setVisible(boolean visible) {
    setFlag(Flag.VISIBLE, visible);
    return this;
  }

  @Override
  public boolean interactive() {
    return isSet(Flag.INTERACTIVE);
  }

  @Override
  public Layer setInteractive(boolean interactive) {
    if (interactive() != interactive) {
      // if we're being made interactive, active our parent as well, if we have one
      if (interactive && parent != null)
        parent.setInteractive(interactive);
      setFlag(Flag.INTERACTIVE, interactive);
    }
    return this;
  }

  @Override
  public float alpha() {
    return alpha;
  }

  @Override
  public Layer setAlpha(float alpha) {
    this.alpha = alpha;
    int ialpha = (int)(0xFF * MathUtil.clamp(alpha, 0, 1));
    this.tint = (ialpha << 24) | (tint & 0xFFFFFF);
    return this;
  }

  @Override
  public int tint() {
    return tint;
  }

  @Override
  public Layer setTint(int tint) {
    this.tint = tint;
    this.alpha = ((tint >> 24) & 0xFF) / 255f;
    return this;
  }

  @Override
  public float originX() {
    return originX;
  }

  @Override
  public float originY() {
    return originY;
  }

  @Override
  public Layer setOrigin(float x, float y) {
    this.originX = x;
    this.originY = y;
    return this;
  }

  @Override
  public float depth() {
    return depth;
  }

  @Override
  public Layer setDepth(float depth) {
    float oldDepth = this.depth;
    if (depth != oldDepth) {
      this.depth = depth;
      if (parent != null) {
        ((ParentLayer)parent).depthChanged(this, oldDepth);
      }
    }
    return this;
  }

  @Override
  public float tx() {
    return transform.tx();
  }

  @Override
  public float ty() {
    return transform.ty();
  }

  @Override
  public Layer setTx(float x) {
    transform.setTx(x);
    return this;
  }

  @Override
  public Layer setTy(float y) {
    transform.setTy(y);
    return this;
  }

  @Override
  public Layer setTranslation(float x, float y) {
    transform.setTranslation(x, y);
    return this;
  }

  @Override
  public float rotation() {
    return rotation;
  }

  @Override
  public Layer setRotation(float angle) {
    if (rotation != angle) {
      rotation = angle;
      setFlag(Flag.XFDIRTY, true);
    }
    return this;
  }

  @Override
  public float scaleX() {
    return scaleX;
  }

  @Override
  public float scaleY() {
    return scaleY;
  }

  @Override
  public Layer setScale(float s) {
    return setScale(s, s);
  }

  @Override
  public Layer setScaleX(float sx) {
    if (scaleX != sx) {
      scaleX = sx;
      setFlag(Flag.XFDIRTY, true);
    }
    return this;
  }

  @Override
  public Layer setScaleY(float sy) {
    if (scaleY != sy) {
      scaleY = sy;
      setFlag(Flag.XFDIRTY, true);
    }
    return this;
  }

  @Override
  public Layer setScale(float sx, float sy) {
    if (sx != scaleX || sy != scaleY) {
      scaleX = sx;
      scaleY = sy;
      setFlag(Flag.XFDIRTY, true);
    }
    return this;
  }

  @Override
  public Layer hitTest(Point p) {
    return (hitTester == null) ? hitTestDefault(p) : hitTester.hitTest(this, p);
  }

  @Override
  public Layer hitTestDefault(Point p) {
    return (p.x >= 0 && p.y >= 0 && p.x < width() && p.y < height()) ? this : null;
  }

  @Override
  public Layer setHitTester (HitTester tester) {
    hitTester = tester;
    return this;
  }

  @Override
  public Transform transform() {
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

  @Override
  public GroupLayer parent() {
    return parent;
  }

  @Override
  public Connection addListener(Pointer.Listener listener) {
    return addInteractor(Pointer.Listener.class, listener);
  }

  @Override
  public Connection addListener(Mouse.LayerListener listener) {
    return addInteractor(Mouse.LayerListener.class, listener);
  }

  @Override
  public Connection addListener(Touch.LayerListener listener) {
    return addInteractor(Touch.LayerListener.class, listener);
  }

  @Override
  public Layer setShader(GLShader shader) {
    // NOOP
    return this;
  }

  // width() and height() exist so that we can share hitTest among all layer implementations;
  // GroupLayer, which does not have a size, overrides hitTest to properly test its children;
  // (non-Clipped) ImmediateLayer inherits this "no size" and always returns null for hitTest
  public float width() {
    return 0;
  }

  public float height() {
    return 0;
  }

  public void onAdd() {
    if (destroyed()) throw new IllegalStateException("Illegal to use destroyed layer: " + this);
  }

  public void onRemove() {
  }

  public void setParent(GroupLayer parent) {
    this.parent = parent;
  }

  @Override
  public String toString () {
    String cname = getClass().getName();
    StringBuilder bldr = new StringBuilder(cname.substring(cname.lastIndexOf(".")+1));
    bldr.append(" [hashCode=").append(hashCode());
    bldr.append(", tx=").append(transform());
    if (hitTester != null) bldr.append(", hitTester=").append(hitTester);
    return bldr.toString();
  }

  protected boolean isSet(Flag flag) {
    return (flags & flag.bitmask) != 0;
  }

  protected void setFlag(Flag flag, boolean active) {
    if (active) {
      flags |= flag.bitmask;
    } else {
      flags &= ~flag.bitmask;
    }
  }

  <L, E> void interact(Class<L> listenerType, Interaction<L, E> interaction, E argument) {
    interact(listenerType, interaction, rootInteractor, argument);
  }

  boolean hasInteractors() {
    return rootInteractor != null;
  }

  // dispatch interactions recursively, so as to dispatch in the order they were added; we assume
  // one will not have such a large number of listeners registered on a single layer that this will
  // blow the stack; note that this also avoids issues with interactor modifications during
  // dispatch: we essentially build a list of interactors to which to dispatch on the stack, before
  // dispatching to any interactors; if an interactor is added or removed during dispatch, it
  // neither affects, nor conflicts with, the current dispatch
  private <L, E> void interact(Class<L> type, Interaction<L, E> interaction,
                               Interactor<?> current, E argument) {
    if (current == null)
      return;
    interact(type, interaction, current.next, argument);
    if (current.listenerType == type) {
      @SuppressWarnings("unchecked") L listener = (L)current.listener;
      interaction.interact(listener, argument);
    }
  }

  private <L> Connection addInteractor(Class<L> listenerType, L listener) {
    final Interactor<L> newint = new Interactor<L>(listenerType, listener, rootInteractor);
    rootInteractor = newint;
    // note that we (and our parents) are now interactive
    setInteractive(true);
    return new Connection() {
      public void disconnect() {
        rootInteractor = removeInteractor(rootInteractor, newint);
        // if we have no more interactors, become non-interactive. But not if we're a
        // GroupLayer; we may be interactive for the sake of our children. In that case,
        // we'll lazily realize and deal with it later
        if (rootInteractor == null && !(AbstractLayer.this instanceof GroupLayer))
          setInteractive(false);
      }
    };
  }

  private Interactor<?> removeInteractor(Interactor<?> current, Interactor<?> target) {
    if (current == null)
      return null;
    if (current == target)
      return current.next;
    current.next = removeInteractor(current.next, target);
    return current;
  }
}
