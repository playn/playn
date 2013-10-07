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

import playn.core.util.Callback;

/**
 * Fetches and returns assets.
 */
public interface Assets {

  /**
   * Synchronously loads and returns an image. The calling thread will block while the image is
   * loaded from disk and decoded. When this call returns, the image's width and height will be
   * valid, and the image can be immediately rendered via layers and into canvases.
   *
   * @param path the path to the image asset.
   * @throws UnsupportedOperationException on platforms that cannot support synchronous asset
   * loading (e.g. HTML5 and Flash).
   */
  Image getImageSync(String path);

  /**
   * Asynchronously loads and returns an image. The calling thread will not block. The returned
   * image will not be immediately usable, will not report valid width and height and cannot be
   * immediately rendered into a canvas. The image can be added to an image layer and it will begin
   * rendering as soon as it is loaded. Add a callback to the image to be notified when its loading
   * completes (or fails).
   *
   * @param path the path to the image asset.
   */
  Image getImage(String path);

  /**
   * Asynchronously loads and returns the image at the specified URL. The width and height of the
   * image will be unset (0) until the image is loaded. <em>Note:</em> on non-HTML platforms, this
   * spawns a new thread for each loaded image. Thus, attempts to load large numbers of remote
   * images simultaneously may result in poor performance.
   */
  Image getRemoteImage(String url);

  /**
   * Asynchronously loads and returns the image at the specified URL. The width and height of the
   * image will be the supplied {@code width} and {@code height} until the image is loaded.
   * <em>Note:</em> on non-HTML platforms, this spawns a new thread for each loaded image. Thus,
   * attempts to load large numbers of remote images simultaneously may result in poor performance.
   */
  Image getRemoteImage(String url, float width, float height);

  /**
   * Asynchronously loads and returns a short sound effect.
   *
   * <p> Note: if a request to play the sound is made before the sound is loaded, it will be noted
   * and the sound will be played when loading has completed.
   *
   * @param path the path to the sound resource. NOTE: this should not include a file extension,
   * PlayN will automatically add {@code .mp3}, (or {@code .caf} on iOS).
   */
  Sound getSound(String path);

  /**
   * Asynchronously loads and returns a music resource. On some platforms, the backend will use a
   * different implementation from {@link #getSound} which is better suited to the much larger size
   * of music audio data.
   *
   * <p> Note: if a request to play the sound is made before the sound is loaded, it will be noted
   * and the sound will be played when loading has completed.
   *
   * @param path the path to the sound resource. NOTE: this should not include a file extension,
   * PlayN will automatically add {@code .mp3}, (or {@code .caf} on iOS).
   */
  Sound getMusic(String path);

  /**
   * Returns a text asset, encoded in UTF-8.
   *
   * @param path the path to the text asset.
   * @throws Exception if there is an error loading the text (for example, if it does not exist).
   * @throws UnsupportedOperationException on platforms that cannot support synchronous asset
   * loading (e.g. HTML5 and Flash).
   */
  String getTextSync(String path) throws Exception;

  /**
   * Calls back with a text asset, encoded in UTF-8.
   *
   * @param path the path to the text asset.
   */
  void getText(String path, Callback<String> callback);

  /**
   * Returns the raw bytes of the asset - useful for custom binary formatted files.
   *
   * @param path the path to the text asset.
   * @throws Exception if there is an error loading the data (for example, if it does not exist).
   * @throws UnsupportedOperationException on platforms that cannot support synchronous asset
   * loading (e.g. HTML5 and Flash).
   */
  byte[] getBytesSync(String path) throws Exception;

  /**
   * Calls back with the bytes of a binary asset - useful for custom binary formatted files.
   *
   * @param path the path to the binary asset.
   */
  void getBytes(String path, Callback<byte[]> callback);
}
