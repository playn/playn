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
package playn.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Callback}-related helper functions.
 */
public class Callbacks {

  /**
   * Adds {@code callback} to {@code list}, creating {@code list} if it is null, and returning (the
   * possibly newly created) {@code list}.
   */
  public static <T> List<Callback<? super T>> createAdd(
      List<Callback<? super T>> list, Callback<? super T> callback) {
    if (list == null)
      list = new ArrayList<Callback<? super T>>();
    list.add(callback);
    return list;
  }

  /**
   * Dispatches {@link Callback#onSuccess} to all callbacks in {@code list} using {@code result}.
   * If {@code list} is null, does nothing. Returns {@code null} to support standard usage pattern.
   */
  public static <T> List<Callback<? super T>> dispatchSuccessClear(
    List<Callback<? super T>> list, T result) {
    if (list != null) {
      for (int ii = 0, ll = list.size(); ii < ll; ii++)
        list.get(ii).onSuccess(result);
    }
    return null;
  }

  /**
   * Dispatches {@link Callback#onSuccess} to all callbacks in {@code list} using {@code result}.
   * If {@code list} is null, does nothing. Returns {@code null} to support standard usage pattern.
   */
  public static <T> List<Callback<? super T>> dispatchFailureClear(
    List<Callback<? super T>> list, Throwable cause) {
    if (list != null) {
      for (int ii = 0, ll = list.size(); ii < ll; ii++)
        list.get(ii).onFailure(cause);
    }
    return null;
  }
}
