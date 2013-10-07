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
 * An {@link Assets} wrapper that tracks the loading status of all asynchronously loaded resources.
 */
public class WatchedAssets implements Assets {

  private final Assets delegate;
  private int totalRequestsCount = 0;
  private int successCount = 0;
  private int errorsCount = 0;

  private Callback<Object> callback = new Callback<Object>() {
    @Override
    public void onSuccess(Object resource) {
      ++successCount;
    }

    @Override
    public void onFailure(Throwable e) {
      ++errorsCount;
    }
  };

  public WatchedAssets (Assets delegate) {
    this.delegate = delegate;
  }

  @Override
  public final Image getImageSync(String path) {
    incrementRequestCount();
    Image image = delegate.getImageSync(path);
    image.addCallback(callback);
    return image;
  }

  @Override
  public final Image getImage(String path) {
    incrementRequestCount();
    Image image = delegate.getImage(path);
    image.addCallback(callback);
    return image;
  }

  @Override
  public final Image getRemoteImage(String url) {
    incrementRequestCount();
    Image image = delegate.getRemoteImage(url);
    image.addCallback(callback);
    return image;
  }

  @Override
  public final Image getRemoteImage(String url, float width, float height) {
    incrementRequestCount();
    Image image = delegate.getRemoteImage(url, width, height);
    image.addCallback(callback);
    return image;
  }

  @Override
  public final Sound getSound(String path) {
    incrementRequestCount();
    Sound sound = delegate.getSound(path);
    sound.addCallback(callback);
    return sound;
  }

  @Override
  public final Sound getMusic(String path) {
    incrementRequestCount();
    Sound sound = delegate.getMusic(path);
    sound.addCallback(callback);
    return sound;
  }

  @Override
  public String getTextSync(String path) throws Exception {
    // no tracking for text loading
    return delegate.getTextSync(path);
  }

  @Override
  public void getText(String path, Callback<String> callback) {
    // no tracking for text loading
    delegate.getText(path, callback);
  }

  @Override
  public byte[] getBytesSync(String path) throws Exception {
    // no tracking for arbitrary binary loading
    return delegate.getBytesSync(path);
  }

  @Override
  public void getBytes(String path, Callback<byte[]> callback) {
    // no tracking for arbitrary binary loading
    delegate.getBytes(path, callback);
  }

  /**
   * Return @code true} if all requested assets have been loaded or errored out,
   * or {@code false} if there are assets remaining to be retrieved
   */
  public boolean isDone() {
    return (this.totalRequestsCount == this.errorsCount + this.successCount);
  }

  /**
   * Return how many assets have not yet been loaded or errored out
   */
  public int getPendingRequestCount() {
    return this.totalRequestsCount - this.errorsCount - this.successCount;
  }

  private void incrementRequestCount() {
    ++totalRequestsCount;
  }
}
