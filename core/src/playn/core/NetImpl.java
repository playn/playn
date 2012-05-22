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

import playn.core.util.Callback;

/**
 * An abstract implementation of {@link Net} shared by multiple backends.
 */
public abstract class NetImpl implements Net {

  private final Platform platform;

  protected NetImpl(Platform platform) {
    this.platform = platform;
  }

  protected void notifySuccess(final Callback<String> callback, final String result) {
    platform.invokeLater(new Runnable() {
      public void run() {
        callback.onSuccess(result);
      }
    });
  }

  protected void notifyFailure(final Callback<String> callback, final Throwable cause) {
    platform.invokeLater(new Runnable() {
      public void run() {
        callback.onFailure(cause);
      }
    });
  }
}
