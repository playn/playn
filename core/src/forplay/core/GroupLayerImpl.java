//
// $Id$

package forplay.core;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides implementations for per-platform concrete {@link GroupLayer}s. Because of single
 * inheritance (and lack of traits) we have to delegate this implementation rather than provide an
 * abstract base class.
 */
public class GroupLayerImpl<L extends AbstractLayer>
{
  /** This group's children. */
  public List<L> children = new ArrayList<L>();

  public void add(GroupLayer self, L child) {
    add(self, children.size(), child);
  }

  public void add(GroupLayer self, int index, L child) {
    children.add(index, child);
    child.setParent(self);
    child.onAdd();
  }

  public void remove(GroupLayer self, L child) {
    children.remove(child);
    child.onRemove();
    child.setParent(null);
  }

  public L remove(GroupLayer self, int index) {
    L child = children.get(index);
    child.onRemove();
    children.remove(index);
    child.setParent(null);
    return child;
  }

  public void clear(GroupLayer self) {
    while (!children.isEmpty()) {
      self.remove(children.size() - 1);
    }
  }

  public void destroy(GroupLayer self) {
    AbstractLayer[] toDestroy = children.toArray(new AbstractLayer[children.size()]);
    // first remove all children efficiently
    self.clear();
    // now that the children have been detached, destroy them
    for (AbstractLayer child : toDestroy) {
      child.destroy();
    }
  }

  public void onAdd(GroupLayer self) {
    for (L child : children) {
      child.onAdd();
    }
  }

  public void onRemove(GroupLayer self) {
    for (L child : children) {
      child.onRemove();
    }
  }
}
