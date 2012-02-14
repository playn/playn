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
package playn.ios;

import playn.core.ResourceCallback;

import playn.core.Sound;

class IOSSound implements Sound
{
  @Override
  public boolean play() {
    return false; // TODO
  }

  @Override
  public void stop() {
    // TODO
  }

  @Override
  public void setLooping(boolean looping) {
    // TODO
  }

  @Override
  public void setVolume(float volume) {
    // TODO
  }

  @Override
  public boolean isPlaying() {
    return false; // TODO
  }

  @Override
  public void addCallback(ResourceCallback<Sound> callback) {
    callback.done(this); // we're always ready
  }
}
