/**
 * Copyright 2010-2011 The ForPlay Authors
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
 * Provides some implementation details needed by {@link Layer}. Ignore these methods, they're not
 * part of the public API and are only visible because the backends live in separate packages and
 * Java's modularity mechanisms are not sufficiently complex to accommodate our needs.
 */
public interface ParentLayer {

  /**
   * Called by a {@link Layer} when its depth changes.
   */
  void depthChanged(Layer layer, float oldDepth);
}
