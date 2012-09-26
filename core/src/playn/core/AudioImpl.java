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
 * Handles some shared bits for {@link Audio} implementations.
 */
public class AudioImpl implements Audio {

  protected final Platform platform;

  public AudioImpl(Platform platform) {
    this.platform = platform;
  }

  protected <I> void dispatchLoaded(final AbstractSound<I> sound, final I impl) {
    platform.invokeLater(new Runnable() {
      public void run () {
        sound.onLoaded(impl);
      }
    });
  }

  protected void dispatchLoadError(final AbstractSound<?> sound, final Throwable error) {
    platform.invokeLater(new Runnable() {
      public void run () {
        sound.onLoadError(error);
      }
    });
  }
}
