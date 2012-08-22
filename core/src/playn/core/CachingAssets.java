/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.core;

import java.util.HashMap;
import java.util.Map;

import playn.core.util.Callback;

/**
 * An {@link Assets} wrapper that caches all loaded images and sounds with no expiration mechanism.
 */
public class CachingAssets implements Assets {

  private final Assets delegate;
  private final Map<String, Object> cache = new HashMap<String, Object>();

  public CachingAssets (Assets delegate) {
    this.delegate = delegate;
  }

  @Override
  public Image getImage(String path) {
    Object object = null;
    if ((object = cache.get(path)) == null) {
      object = delegate.getImage(path);
      cache.put(path, object);
    }
    return (Image) object;
  }

  @Override
  public Sound getSound(String path) {
    Object object = null;
    if ((object = cache.get(path)) == null) {
      object = delegate.getSound(path);
      cache.put(path, object);
    }
    return (Sound) object;
  }

  @Override
  public void getText(String path, Callback<String> callback) {
    // no caching for text loading
    delegate.getText(path, callback);
  }

  @Deprecated
  public boolean isDone() {
    return delegate.isDone();
  }

  @Deprecated
  public int getPendingRequestCount() {
    return delegate.getPendingRequestCount();
  }
}
