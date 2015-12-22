/**
 * Copyright 2011 The PlayN Authors
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
import java.util.Iterator;
import java.util.List;

import pythagoras.f.AffineTransform;
import pythagoras.f.MathUtil;
import pythagoras.f.Point;
import pythagoras.util.NoninvertibleTransformException;

import playn.core.Surface;

/**
 * GroupLayer creates a Layer hierarchy by maintaining an ordered group of child Layers.
 */
public class GroupLayer extends ClippedLayer implements Iterable<Layer> {

  private final List<Layer> children = new ArrayList<>();
  private final AffineTransform paintTx = new AffineTransform();
  private final boolean disableClip;

  /** Creates an unclipped group layer. Unclipped groups have no defined size. */
  public GroupLayer () {
    super(0, 0);
    disableClip = true;
  }

  /** Creates a clipped group layer with the specified size. */
  public GroupLayer (float width, float height) {
    super(width, height);
    disableClip = false;
  }

  /** Returns whether this group has any child layers. */
  public boolean isEmpty () { return children.isEmpty(); }

  /** Returns the number of child layers in this group. */
  public int children() {
    return children.size();
  }

  /**
   * Returns the layer at the specified index. Layers are ordered in terms of their depth and will
   * be returned in this order, with 0 being the layer on bottom.
   */
  public Layer childAt(int index) {
    return children.get(index);
  }

  /**
   * Adds a layer to the bottom of the group. Because the {@link Layer} hierarchy is a tree, if
   * {@code child} is already a child of another {@link GroupLayer}, it will be removed before
   * being added to this {@link GroupLayer}.
   */
  public void add(Layer child) {
    // optimization if we're requested to add a child that's already added
    GroupLayer parent = child.parent();
    if (parent == this) return;

    // if this child has equal or greater depth to the last child, we can append directly and avoid
    // a log(N) search; this is helpful when all children have the same depth
    int count = children.size(), index;
    if (count == 0 || children.get(count-1).depth() <= child.depth()) index = count;
    // otherwise find the appropriate insertion point via binary search
    else index = findInsertion(child.depth());

    // remove the child from any existing parent, preventing multiple parents
    if (parent != null) parent.remove(child);
    children.add(index, child);
    child.setParent(this);
    if (state.get() == State.ADDED) child.onAdd();

    // if this child is active, we need to become active
    if (child.interactive()) setInteractive(true);
  }

  /**
   * Adds all supplied children to this layer, in order. See {@link #add(Layer)}.
   */
  public void add(Layer child0, Layer child1, Layer... childN) {
    add(child0);
    add(child1);
    for (Layer child : childN) add(child);
  }

  /**
   * Adds the supplied layer to this group layer, adjusting its translation (relative to this group
   * layer) to the supplied values.
   *
   * <p>This is equivalent to: {@code add(child.setTranslation(tx, ty))}.
   */
  public void addAt(Layer child, float tx, float ty) {
    add(child.setTranslation(tx, ty));
  }

  /**
   * Adds {@code child} to this group layer, positioning it such that its center is at ({@code tx},
   * {@code tx}). The layer must report a non-zero size, thus this will not work on an unclipped
   * group layer.
   *
   * <p>This is equivalent to: {@code add(child.setTranslation(tx - child.width()/2,
   * ty - child.height()/2))}.
   */
  public void addCenterAt (Layer child, float tx, float ty) {
    add(child.setTranslation(tx - child.width()/2, ty - child.height()/2));
  }

  /**
   * Adds {@code child} to this group layer, adjusting its translation (relative to this group
   * layer) to {@code floor(tx), floor(ty)}. This is useful for adding layers which display text a
   * text can become blurry if it is positioned on sub-pixel boundaries.
   */
  public void addFloorAt (Layer child, float tx, float ty) {
    add(child.setTranslation(MathUtil.ifloor(tx), MathUtil.ifloor(ty)));
  }

  /**
   * Removes a layer from the group.
   */
  public void remove(Layer child) {
    int index = findChild(child, child.depth());
    if (index < 0) {
      throw new UnsupportedOperationException(
        "Could not remove Layer because it is not a child of the GroupLayer " +
          "[group=" + this + ", layer=" + child + "]");
    }
    remove(index);
  }

  /**
   * Removes all supplied children from this layer, in order. See {@link #remove(Layer)}.
   */
  public void remove(Layer child0, Layer child1, Layer... childN) {
    remove(child0);
    remove(child1);
    for (Layer child : childN) remove(child);
  }

  /**
   * Removes all child layers from this group.
   */
  public void removeAll() {
    while (!children.isEmpty()) remove(children.size()-1);
  }

  /**
   * Removes and disposes all child layers from this group.
   */
  public void disposeAll() {
    Layer[] toDispose = children.toArray(new Layer[children.size()]);
    // remove all of the children efficiently
    removeAll();
    // now that the children have been detached, dispose them
    for (Layer child : toDispose) child.close();
  }

  @Override public Iterator<Layer> iterator () {
    return children.iterator();
  }

  @Override public void close() {
    super.close();
    disposeAll();
  }

  @Override public Layer hitTestDefault(Point point) {
    float x = point.x, y = point.y;
    boolean sawInteractiveChild = false;
    // we check back to front as children are ordered "lowest" first
    for (int ii = children.size()-1; ii >= 0; ii--) {
      Layer child = children.get(ii);
      if (!child.interactive()) continue; // ignore non-interactive children
      sawInteractiveChild = true; // note that we saw an interactive child
      if (!child.visible()) continue; // ignore invisible children
      try {
        // transform the point into the child's coordinate system
        child.transform().inverseTransform(point.set(x, y), point);
        point.x += child.originX();
        point.y += child.originY();
        Layer l = child.hitTest(point);
        if (l != null)
          return l;
      } catch (NoninvertibleTransformException nte) {
        // Degenerate transform means no hit
        continue;
      }
    }
    // if we saw no interactive children and we don't have listeners registered directly on this
    // group, clear our own interactive flag; this lazily deactivates this group after its
    // interactive children have been deactivated or removed
    if (!sawInteractiveChild && !hasEventListeners()) setInteractive(false);
    return super.hitTestDefault(point);
  }

  @Override protected boolean disableClip () {
    return disableClip;
  }

  @Override protected void toString (StringBuilder buf) {
    super.toString(buf);
    buf.append(", children=").append(children.size());
  }

  @Override protected void paintClipped (Surface surf) {
    // save our current transform and restore it before painting each child
    paintTx.set(surf.tx());
    // iterate manually to avoid creating an Iterator as garbage, this is inner-loop territory
    List<Layer> children = this.children;
    for (int ii = 0, ll = children.size(); ii < ll; ii++) {
      surf.tx().set(paintTx);
      children.get(ii).paint(surf);
    }
  }

  int depthChanged(Layer child, float oldDepth) {
    // locate the child whose depth changed
    int oldIndex = findChild(child, oldDepth);

    // fast path for depth changes that don't change ordering
    float newDepth = child.depth();
    boolean leftCorrect = (oldIndex == 0 || children.get(oldIndex-1).depth() <= newDepth);
    boolean rightCorrect = (oldIndex == children.size()-1 ||
                            children.get(oldIndex+1).depth() >= newDepth);
    if (leftCorrect && rightCorrect) {
      return oldIndex;
    }

    // it would be great if we could move an element from one place in an ArrayList to another
    // (portably), but instead we have to remove and re-add
    children.remove(oldIndex);
    int newIndex = findInsertion(newDepth);
    children.add(newIndex, child);
    return newIndex;
  }

  @Override void onAdd() {
    super.onAdd();
    for (int ii = 0, ll = children.size(); ii < ll; ii++) children.get(ii).onAdd();
  }

  @Override void onRemove() {
    super.onRemove();
    for (int ii = 0, ll = children.size(); ii < ll; ii++) children.get(ii).onRemove();
  }

  // group layers do not deactivate when their last event listener is removed; they may still have
  // interactive children to which events need to be dispatched; when a hit test is performed on a
  // group layer and it discovers that it has no interactive children, it will deactivate itself
  @Override protected boolean deactivateOnNoListeners () { return false; }

  private void remove(int index) {
    Layer child = children.remove(index);
    child.onRemove();
    child.setParent(null);
  }

  // uses depth to improve upon a full linear search
  private int findChild(Layer child, float depth) {
    // findInsertion will find us some element with the same depth as the to-be-removed child
    int startIdx = findInsertion(depth);
    // search down for our child
    for (int ii = startIdx-1; ii >= 0; ii--) {
      Layer c = children.get(ii);
      if (c == child) {
        return ii;
      }
      if (c.depth() != depth) {
        break;
      }
    }
    // search up for our child
    for (int ii = startIdx, ll = children.size(); ii < ll; ii++) {
      Layer c = children.get(ii);
      if (c == child) {
        return ii;
      }
      if (c.depth() != depth) {
        break;
      }
    }
    return -1;
  }

  // who says you never have to write binary search?
  private int findInsertion(float depth) {
    int low = 0, high = children.size()-1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      float midDepth = children.get(mid).depth();
      if (depth > midDepth) {
        low = mid + 1;
      } else if (depth < midDepth) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return low;
  }
}
