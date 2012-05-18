/**
 * Copyright 2012 The PlayN Authors
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
 * A NOOP mouse service for use on platforms that don't support mouse interaction.
 */
public class MouseStub implements Mouse {

  @Override
  public boolean hasMouse() {
    return false;
  }

  @Override
  public void setListener(Listener listener) {
    // noop!
  }

  @Override
  public void lock() {
  }

  @Override
  public void unlock() {
  }

  @Override
  public boolean isLocked() {
    return false;
  }

  @Override
  public boolean isLockSupported() {
    return false;
  }
}
