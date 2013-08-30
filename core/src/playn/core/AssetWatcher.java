/**
 * Copyright 2011 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core;

import playn.core.util.Callback;

/**
 * A utility class that helps keep track of image loading.
 * <p>
 * To use: create a new {@link AssetWatcher}, then add images using
 * {@link AssetWatcher#add(Image)} and finally call {@link AssetWatcher#start()}.
 */
public class AssetWatcher {

  /**
   * A listener that is notified of asset loading progress and failures.
   */
  public static abstract class Listener {
    /**
     * Informs the listener of progress as each asset load completes or fails.
     */
    public void progress (int loaded, int errors, int total) {
      // default implementation does nothing
    }

    /**
     * Called when all assets are done loading (or had an error). This will be called after the
     * final call to {@link #progress}.
     */
    public abstract void done();

    /**
     * Called for each asset that failed to load.
     */
    public abstract void error(Throwable e);
  }

  private int total, loaded, errors;
  private boolean start;
  private final Listener listener;

  private Callback<Object> callback = new Callback<Object>() {
    @Override
    public void onSuccess(Object resource) {
      ++loaded;
      update();
    }

    @Override
    public void onFailure(Throwable e) {
      ++errors;
      if (listener != null)
        listener.error(e);
      update();
    }
  };

  /**
   * Creates a new watcher without a listener.
   */
  public AssetWatcher() {
    this(null);
    start();
  }

  /**
   * Creates a new watcher with the given listener.
   * <p>
   * Note: must call {@link AssetWatcher#start()} after adding your resources.
   */
  public AssetWatcher(Listener listener) {
    this.listener = listener;
  }

  /**
   * Adds an image resource to be watched.
   */
  public void add(Image image) {
    Asserts.checkState(!start || listener == null);
    ++total;
    image.addCallback(callback);
  }

  /**
   * Adds a sound resource to be watched.
   */
  public void add(Sound sound) {
    Asserts.checkState(!start || listener == null);
    ++total;
    sound.addCallback(callback);
  }

  /**
   * Whether all resources have completed loading, either successfully or in error.
   */
  public boolean isDone() {
    return start && (loaded + errors == total);
  }

  /**
   * Done adding resources; {@link Listener#done()} will be called as soon as all assets are done
   * being loaded.
   *
   * There is no need to call this method if there is no listener. {@link #isDone()} will return
   * {@code true} as soon as all pending assets are loaded.
   */
  public void start() {
    start = true;
    update();
  }

  private void update() {
    if (listener == null)
      return;
    listener.progress(loaded, errors, total);
    if (isDone())
      listener.done();
  }
}
