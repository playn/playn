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

/**
 * Used to communicate asynchronous results.
 */
public interface Callback<T>
{
  /** A callback that chains failure to the supplied delegate callback. */
  public static abstract class Chain<T> implements Callback<T> {
    private Callback<?> onFailure;

    public Chain(Callback<?> onFailure) {
      assert onFailure != null;
      this.onFailure = onFailure;
    }

    @Override
    public void onFailure(Throwable cause) {
      onFailure.onFailure(cause);
    }
  }

  /**
   * Called when the asynchronous request succeeded, supplying its result.
   */
  void onSuccess(T result);

  /**
   * Called when the asynchronous request failed, supplying a cause for failure.
   */
  void onFailure(Throwable cause);
}
