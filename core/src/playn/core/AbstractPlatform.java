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
import playn.core.util.RunQueue;

/**
 * Implements some common {@link Platform} bits.
 */
public abstract class AbstractPlatform implements Platform {

  protected final PlayN.ErrorReporter DEFAULT_REPORTER = new PlayN.ErrorReporter() {
    public void reportError(String message, Throwable err) {
      log.warn(message, err);
    }
  };

  protected final RunQueue runQueue;
  protected final Log log;

  private PlayN.LifecycleListener lifecycleListener;
  private PlayN.ErrorReporter errorReporter = DEFAULT_REPORTER;

  @Override
  public void reportError(String message, Throwable err) {
    errorReporter.reportError(message, err);
  }

  @Override
  public void invokeLater(Runnable runnable) {
    runQueue.add(runnable);
  }

  @Override
  public void setLifecycleListener(PlayN.LifecycleListener listener) {
    lifecycleListener = listener;
  }

  @Override
  public void setErrorReporter(PlayN.ErrorReporter reporter) {
    errorReporter = (reporter == null) ? DEFAULT_REPORTER : reporter;
  }

  @Override
  public Log log() {
    return log;
  }

  /**
   * Delivers {@code result} to {@code callback} on the next game tick (on the PlayN thread).
   */
  public <T> void notifySuccess(final Callback<T> callback, final T result) {
    invokeLater(new Runnable() {
      public void run() {
        callback.onSuccess(result);
      }
    });
  }

  /**
   * Delivers {@code error} to {@code callback} on the next game tick (on the PlayN thread).
   */
  public void notifyFailure(final Callback<?> callback, final Throwable error) {
    invokeLater(new Runnable() {
      public void run() {
        callback.onFailure(error);
      }
    });
  }

  /**
   * Invokes the supplied action on a separate thread. Used by {@link AbstractAssets} for
   * asynchronous asset loading.
   */
  public void invokeAsync(Runnable action) {
    throw new UnsupportedOperationException();
  }

  protected AbstractPlatform(Log log) {
    this.log = log;
    this.runQueue = new RunQueue(this);
  }

  protected void onPause() {
    if (lifecycleListener != null) {
      try {
        lifecycleListener.onPause();
      } catch (Exception e) {
        reportError("LifecycleListener.onPause failure", e);
      }
    }
  }

  protected void onResume() {
    if (lifecycleListener != null) {
      try {
        lifecycleListener.onResume();
      } catch (Exception e) {
        reportError("LifecycleListener.onResume failure", e);
      }
    }
  }

  protected void onExit() {
    if (lifecycleListener != null) {
      try {
        lifecycleListener.onExit();
      } catch (Exception e) {
        reportError("LifecycleListener.onExit failure", e);
      }
    }
  }
}
