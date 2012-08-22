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

/**
 * Fetches and returns assets.  This interface assumes that some or all assets
 * are asynchronously loaded.
 */
public interface Assets {

  /**
   * Return an Image, given a path to the image resource.
   *
   * @param path a path to the resource
   * @return the image
   */
  Image getImage(String path);

  /**
   * Return a Sound, given a path to the sound resource.
   *
   * @param path a path to the resource
   * @return the sound
   */
  Sound getSound(String path);

  /**
   * Calls back with String, given a path to a text resource encoded with UTF-8.
   *
   * @param path a path to the resource
   */
  void getText(String path, ResourceCallback<String> callback);

  /**
   * @deprecated Use {@link WatchedAssets}.
   */
  @Deprecated
  boolean isDone();

  /**
   * @deprecated Use {@link WatchedAssets}.
   */
  @Deprecated
  int getPendingRequestCount();
}
