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
 * An image.
 */
public interface Image {

  /**
   * This image's height in pixels.
   */
  int height();

  /**
   * Replace this image's surface with that of another image.
   */
  void replaceWith(Image image);

  /**
   * Adds a callback to be notified when this image is loaded. If the image is
   * already loaded, the callback will be notified immediately. The callback is
   * discarded once the image is loaded.
   */
  void addCallback(ResourceCallback<Image> callback);

  /**
   * This image's width in pixels.
   */
  int width();

  /**
   * Whether or not this image is ready to be used.
   */
  boolean isReady();
}
