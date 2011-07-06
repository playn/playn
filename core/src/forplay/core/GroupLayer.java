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

/**
 * TODO - Need to add javadoc
 */
public interface GroupLayer extends Layer {

  /**
   * Returns the layer at the specified position.
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
   * Inserts a layer at a given position in the group.
   * <p>
   * Because the {@link Layer} hierarchy is a tree, if the {@link Layer} is already a child of
   * another {@link GroupLayer}, it will be removed before being added to this {@link GroupLayer}.
   */
  void add(int index, Layer layer);

  /**
   * Removes a layer from the group.
   */
  void remove(Layer layer);

  /**
   * Removes the layer at the specified location in the group. Any subsequent
   * elements are shifted to fill the removed layer's place.
   */
  void remove(int index);

  /**
   * Removes all the layers from this group.
   */
  void clear();

  /**
   * Returns the number of layers in this group.
   */
  int size();
}
