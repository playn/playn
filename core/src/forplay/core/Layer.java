/**
 * Copyright 2010 The ForPlay Authors
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
 * TODO
 * 
 * TODO: alpha, z-index, clipping (?), visibility
 * transform-origin: allow explicit "center, top-left, bottom-right" like CSS transform-origin?
 */
public interface Layer {

  /**
   * Destroys this layer, removing it from its parent layer. Any resources associated with this
   * layer are freed, and it cannot be reused after being destroyed. Destroying a layer that has
   * children will destroy them as well.
   */
  void destroy();

  /**
   * Whether this layer has been destroyed. If so, it can no longer be used.
   */
  boolean isDestroyed();

  /**
   * TODO
   */
  GroupLayer parent();

  /**
   * TODO
   */
  Transform transform();

  /**
   * TODO
   */
  void setOrigin(float x, float y);

  /**
   * TODO
   */
  void setTranslation(float x, float y);

  /**
   * TODO
   */
  void setScale(float x);

  /**
   * TODO
   */
  void setScale(float x, float y);

  /**
   * TODO
   */
  void setRotation(float angle);
}
