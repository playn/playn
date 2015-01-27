/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.scene;

/**
 * Serves as the root of the scene graph. This is just a {@link GroupLayer} with minor tweaks to
 * ensure that when layers are added to it, they transition properly to the "added to scene graph"
 * state.
 */
public class RootLayer extends GroupLayer {

  /** Creates an unclipped root layer. This is almost always what you want. */
  public RootLayer () {
    setState(State.ADDED);
  }

  /** Creates a root layer clipped to the specified dimensions. This is rarely what you want. */
  public RootLayer (float width, float height) {
    super(width, height);
    setState(State.ADDED);
  }
}
