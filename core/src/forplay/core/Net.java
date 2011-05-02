/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.core;

/**
 * ForPlay network interface.
 * 
 * TODO(jgw): This is quite anemic at the moment, but it's a starting point.
 */
public interface Net {

  public interface Callback {
    /**
     * Called when a request is successful.
     */
    void success(String response);

    /**
     * Called when a request fails.
     */
    void failure(Throwable error);
  }

  /**
   * Performs an HTTP GET request to the specified URL.
   */
  void get(String url, Callback callback);

  /**
   * Performs an HTTP POST request to the specified URL.
   */
  void post(String url, String data, Callback callback);
}
