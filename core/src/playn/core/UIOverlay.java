/**
 * Copyright 2013 The PlayN Authors
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

import pythagoras.f.IRectangle;

/**
 * Platforms that support native widgets need to put those widgets in a native container above the
 * PlayN GL layers. This interface exists to ease the delivery of that container to other
 * platform-specific bits, and to offer some common functionality to non-platform-specific bits.
 */
public interface UIOverlay {

  /**
   * Returns true if the underlying platform has an overlay container for native widgets.
   */
  boolean hasOverlay();

  /**
   * Masks out the overlay over the given area. Pointer events should not interact with the overlay
   * within this area and native widgets should be invisible within this area, allowing the PlayN
   * layers under it to be visible.
   */
  void hideOverlay(IRectangle area);
}
