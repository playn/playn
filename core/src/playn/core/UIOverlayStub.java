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
 * A NOOP service for use on platforms that don't support native overlay widgets.
 */
public class UIOverlayStub implements UIOverlay {

  @Override
  public boolean hasOverlay() {
    return false;
  }

  @Override
  public void hideOverlay(IRectangle area) {
  }
}
