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

import java.util.ArrayList;

public abstract class AbstractSound implements Sound {
  private ArrayList<ResourceCallback<? super Sound>> resourceCallbacks;

  private Boolean soundLoaded; // null indicates result not yet known


  @Override
  public final void addCallback(ResourceCallback<? super Sound> callback) {
    if (soundLoaded != null) {
      if (soundLoaded) {
        callback.done(AbstractSound.this);
      } else {
        callback.error(new RuntimeException());
      }
      return;
    }
    if (resourceCallbacks == null) {
      resourceCallbacks = new ArrayList<ResourceCallback<? super Sound>>();
    }
    resourceCallbacks.add(callback);
  }

  protected void onLoadError(Throwable err) {
    if (resourceCallbacks == null) {
      return;
    }
    for (ResourceCallback<? super Sound> callback : resourceCallbacks) {
      callback.error(err);
    }
    resourceCallbacks.clear();
  }

  protected void onLoadComplete() {
    if (resourceCallbacks == null) {
      return;
    }
    for (ResourceCallback<? super Sound> callback : resourceCallbacks) {
      callback.done(AbstractSound.this);
    }
    resourceCallbacks.clear();
  }
}
