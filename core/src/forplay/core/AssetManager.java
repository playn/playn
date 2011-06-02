/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.core;

/**
 * Fetches and returns assets.  This interface assumes that some or all assets
 * are asynchronously loaded.  Loading
 */
public interface AssetManager {

  /**
   * Return an Image, given a path to the image resource.
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
   * Return a String, given a path to a text resource.
   * 
   * @param path a path to the resource
   * @return the text
   */
  void getText(String path, ResourceCallback<String> callback);

  /**
   * @return <code>true</code> if all requested assets have been loaded or errored out,
   * or <code>false</code> if there are assets remaining to be retrieved
   */
  boolean isDone();

  /**
   * @return how many assets have not yet been loaded or errored out
   */
  int getPendingRequestCount();
}
