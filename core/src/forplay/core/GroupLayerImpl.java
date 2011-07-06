/**
 * Copyright 2011 The ForPlay Authors
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
    // remove the child from any existing parent, preventing multiple parents
    if (child.parent() != null) {
      child.parent().remove(child);
    }
    children.add(index, child);
    child.setParent(self);
    child.onAdd();
  }

  public void remove(GroupLayer self, L child) {
    boolean wasRemoved = children.remove(child);
    if (!wasRemoved) {
      throw new UnsupportedOperationException(
          "Could not remove Layer because it is not a child of the GroupLayer");
    }
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
