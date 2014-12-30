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

/**
 * GroupLayer creates a Layer hierarchy by maintaining an ordered group of child Layers.
 */
public interface GroupLayer extends Layer {

  /** A clipped group layer. */
  interface Clipped extends GroupLayer, HasSize {
    /** Updates the size of this group layer, and hence its clipping rectangle. */
    void setSize(float width, float height);

    /** Updates the width of this group layer, and hence its clipping rectangle. */
    void setWidth(float width);

    /** Updates the height of this group layer, and hence its clipping rectangle. */
    void setHeight(float height);
  }

  /**
   * Returns the layer at the specified index.
   * <p>
   * Layers are ordered in terms of their depth and will be returned in this order, with 0 being the
   * layer on bottom.
   */
  Layer get(int index);

  /**
   * Adds a layer to the bottom of the group.
   * <p>
   * Because the {@link Layer} hierarchy is a tree, if the {@link Layer} is already a child of
   * another {@link GroupLayer}, it will be removed before being added to this {@link GroupLayer}.
   */
  void add(Layer layer);

  /**
   * Adds the supplied layer to this group layer, adjusting its translation (relative to this group
   * layer) to the supplied values. This is equivalent to:
   * <pre>{@code
   * layer.setTranslation(tx, ty);
   * group.add(layer);
   * }</pre>
   * but is such a common operation that this helper method is provided.
   */
  void addAt(Layer layer, float tx, float ty);

  /**
   * Removes a layer from the group.
   */
  void remove(Layer layer);

  /**
   * Removes all child layers from this group.
   */
  void removeAll();

  /**
   * Removes and destroys all child layers from this group.
   */
  void destroyAll();

  /**
   * Returns the number of layers in this group.
   */
  int size();

  /** @deprecated Use {@link #removeAll}. */
  @Deprecated
  void clear();
}
