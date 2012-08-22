/**
 * Copyright 2012 The PlayN Authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.core;

import java.util.List;

import playn.core.util.Callback;
import playn.core.util.Callbacks;

public abstract class AbstractSound implements Sound {

  private List<Callback<? super Sound>> callbacks;
  private Boolean soundLoaded; // null indicates result not yet known

  @Override
  public final void addCallback(Callback<? super Sound> callback) {
    if (soundLoaded != null) {
      if (soundLoaded)
        callback.onSuccess(AbstractSound.this);
      else
        callback.onFailure(new RuntimeException("Sound already failed to load."));
    } else
      callbacks = Callbacks.createAdd(callbacks, callback);
  }

  protected void onLoadError(Throwable err) {
    this.soundLoaded = false;
    callbacks = Callbacks.dispatchFailureClear(callbacks, err);
  }

  protected void onLoadComplete() {
    this.soundLoaded = true;
    callbacks = Callbacks.dispatchSuccessClear(callbacks, this);
  }
}
