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

package playn.core;

import java.util.List;
import java.util.ArrayList;

import pythagoras.f.Point;
import pythagoras.util.NoninvertibleTransformException;

/**
 * Provides implementations for per-platform concrete {@link GroupLayer}s. Because of single
 * inheritance (and lack of traits) we have to delegate this implementation rather than provide an
 * abstract base class.
 */
public class GroupLayerImpl<L extends AbstractLayer>
{
  /** This group's children. */
  public List<L> children = new ArrayList<L>();

  /**
   * @return the index into the children array at which the layer was inserted (based on depth).
   */
  public int add(GroupLayer self, L child) {
    // optimization if we're requested to add a child that's already added
    GroupLayer parent = child.parent();
    if (parent == self) {
      return findChild(child, child.depth());
    }

    // if this child has equal or greater depth to the last child, we can append directly and avoid
    // a log(N) search; this is helpful when all children have the same depth
    int count = children.size(), index;
    if (count == 0 || children.get(count-1).depth() <= child.depth()) {
      index = count;
    } else {
      // otherwise find the appropriate insertion point via binary search
      index = findInsertion(child.depth());
    }

    // remove the child from any existing parent, preventing multiple parents
    if (parent != null) {
      child.parent().remove(child);
    }
    children.add(index, child);
    child.setParent(self);
    child.onAdd();

    // if this child is active, we need to become active
    if (child.interactive())
      self.setInteractive(true);

    return index;
  }

  public void addAt(GroupLayer self, Layer layer, float tx, float ty) {
    layer.setTranslation(tx, ty);
    self.add(layer);
  }

  public void remove(GroupLayer self, L child) {
    int index = findChild(child, child.depth());
    if (index < 0) {
      throw new UnsupportedOperationException(
        "Could not remove Layer because it is not a child of the GroupLayer");
    }
    remove(index);
  }

  public void removeAll(GroupLayer self) {
    while (!children.isEmpty()) {
      remove(children.size() - 1);
    }
  }

  public void destroyAll(GroupLayer self) {
    AbstractLayer[] toDestroy = children.toArray(new AbstractLayer[children.size()]);
    // remove all of the children efficiently
    self.removeAll();
    // now that the children have been detached, destroy them
    for (AbstractLayer child : toDestroy) {
      child.destroy();
    }
  }

  public void destroy(GroupLayer self) {
    self.destroyAll();
  }

  public void onAdd(GroupLayer self) {
    for (int ii = 0, ll = children.size(); ii < ll; ii++) {
      children.get(ii).onAdd();
    }
  }

  public void onRemove(GroupLayer self) {
    for (int ii = 0, ll = children.size(); ii < ll; ii++) {
      children.get(ii).onRemove();
    }
  }

  public Layer hitTest(GroupLayer self, Point point) {
    float x = point.x, y = point.y;
    boolean sawInteractiveChild = false;
    // we check back to front as children are ordered "lowest" first
    for (int ii = children.size()-1; ii >= 0; ii--) {
      L child = children.get(ii);
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
    if (!sawInteractiveChild && !((AbstractLayer)self).hasInteractors())
      self.setInteractive(false);
    return null;
  }

  /**
   * @return the new index of the depth-changed layer.
   */
  public int depthChanged(GroupLayer self, Layer layer, float oldDepth) {
    // structuring things such that Java's type system knew what was going on here would require
    // making AbstractLayer and ParentLayer more complex than is worth it
    @SuppressWarnings("unchecked") L child = (L)layer;

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

  private void remove(int index) {
    L child = children.remove(index);
    child.onRemove();
    child.setParent(null);
  }

  // uses depth to improve upon a full linear search
  private int findChild(L child, float depth) {
    // findInsertion will find us some element with the same depth as the to-be-removed child
    int startIdx = findInsertion(depth);
    // search down for our child
    for (int ii = startIdx-1; ii >= 0; ii--) {
      L c = children.get(ii);
      if (c == child) {
        return ii;
      }
      if (c.depth() != depth) {
        break;
      }
    }
    // search up for our child
    for (int ii = startIdx, ll = children.size(); ii < ll; ii++) {
      L c = children.get(ii);
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
