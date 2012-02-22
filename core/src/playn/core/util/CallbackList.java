/**
 * Copyright 2011 The PlayN Authors
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
package playn.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects a list of callbacks, and eases the process of propagating success or failure to all
 * listed callbacks.
 */
public class CallbackList<T> implements Callback<T>
{
  /** A list of callbacks which will be notified on success or failure. */
  private List<Callback<T>> callbacks = new ArrayList<Callback<T>>();

  /**
   * Creates a callback list, populated with the supplied callback.
   */
  public static <T> CallbackList<T> create(Callback<T> callback) {
    CallbackList<T> list = new CallbackList<T>();
    list.add(callback);
    return list;
  }

  /**
   * Adds the supplied callback to the list.
   *
   * @return this instance for conveninent chaining.
   * @throws IllegalStateException if this callback has already fired.
   */
  public CallbackList<T> add(Callback<T> callback) {
    checkState();
    callbacks.add(callback);
    return this;
  }

  /**
   * Removes the specified callback from the list.
   *
   * @throws IllegalStateException if this callback has already fired.
   */
  public void remove(Callback<T> callback) {
    checkState();
    callbacks.remove(callback);
  }

  /**
   * Dispatches success to all of the callbacks registered with this list. This may only be called
   * once.
   *
   * @throws IllegalStateException if this callback has already fired.
   */
  @Override
  public void onSuccess(T result) {
    checkState();
    for (Callback<T> cb : callbacks) {
      cb.onSuccess(result);
    }
    callbacks = null; // note that we've fired
  }

  /**
   * Dispatches failure to all of the callbacks registered with this list. This may only be called
   * once.
   *
   * @throws IllegalStateException if this callback has already fired.
   */
  @Override
  public void onFailure(Throwable cause) {
    checkState();
    for (Callback<T> cb : callbacks) {
      cb.onFailure(cause);
    }
    callbacks = null; // note that we've fired
  }

  protected void checkState() {
    if (callbacks == null) {
      throw new IllegalStateException("CallbackList has already fired.");
    }
  }
}
