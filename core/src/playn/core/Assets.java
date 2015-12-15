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

import java.nio.ByteBuffer;

import react.RFuture;
import react.RPromise;

/**
 * Fetches and returns assets.
 */
public abstract class Assets {

  /**
   * Synchronously loads and returns an image. The calling thread will block while the image is
   * loaded from disk and decoded. When this call returns, the image's width and height will be
   * valid, and the image can be immediately converted to a texture and drawn into a canvas.
   *
   * @param path the path to the image asset.
   * @throws UnsupportedOperationException on platforms that cannot support synchronous asset
   * loading (HTML).
   */
  public Image getImageSync (String path) {
    ImageImpl image = createImage(false, 0, 0, path);
    try {
      image.succeed(load(path));
    } catch (Throwable t) {
      image.fail(t);
    }
    return image;
  }

  /**
   * Asynchronously loads and returns an image. The calling thread will not block. The returned
   * image will not be immediately usable, will not report valid width and height, and cannot be
   * immediately rendered into a canvas or converted into a texture. Use {@link Image#state} to be
   * notified when loading succeeds or fails.
   *
   * @param path the path to the image asset.
   */
  public Image getImage (final String path) {
    final ImageImpl image = createImage(true, 0, 0, path);
    exec.invokeAsync(new Runnable() {
      public void run () {
        try {
          image.succeed(load(path));
        } catch (Throwable t) {
          image.fail(t);
        }
      }
    });
    return image;
  }

  /**
   * Asynchronously loads and returns the image at the specified URL. The width and height of the
   * image will be unset (0) until the image is loaded. <em>Note:</em> on non-HTML platforms, this
   * spawns a new thread for each loaded image. Thus, attempts to load large numbers of remote
   * images simultaneously may result in poor performance.
   */
  public Image getRemoteImage (String url) {
    return getRemoteImage(url, 0, 0);
  }

  /**
   * Asynchronously loads and returns the image at the specified URL. The width and height of the
   * image will be the supplied {@code width} and {@code height} until the image is loaded.
   * <em>Note:</em> on non-HTML platforms, this spawns a new thread for each loaded image. Thus,
   * attempts to load large numbers of remote images simultaneously may result in poor performance.
   */
  public Image getRemoteImage (String url, int width, int height) {
    Exception error = new Exception(
      "Remote image loading not yet supported: " + url + "@" + width + "x" + height);
    ImageImpl image = createImage(false, width, height, url);
    image.fail(error);
    return image;
  }

  /**
   * Asynchronously loads and returns a short sound effect.
   *
   * <p> Note: if a request to play the sound is made before the sound is loaded, it will be noted
   * and the sound will be played when loading has completed.
   *
   * @param path the path to the sound resource. NOTE: this should not include a file extension,
   * PlayN will automatically add {@code .mp3}, (or {@code .caf} on iOS).
   */
  public abstract Sound getSound (String path);

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
  public Sound getMusic (String path) {
    return getSound(path);
  }

  /**
   * Loads and returns a UTF-8 encoded text asset.
   *
   * @param path the path to the text asset.
   * @throws Exception if there is an error loading the text (for example, if it does not exist).
   * @throws UnsupportedOperationException on platforms that cannot support synchronous asset
   * loading (e.g. HTML5 and Flash).
   */
  public abstract String getTextSync (String path) throws Exception;

  /**
   * Loads UTF-8 encoded text asynchronously. The returned state instance provides a means to
   * listen for the arrival of the text.
   *
   * @param path the path to the text asset.
   */
  public RFuture<String> getText (final String path) {
    final RPromise<String> result = exec.deferredPromise();
    exec.invokeAsync(new Runnable() {
      public void run () {
        try {
          result.succeed(getTextSync(path));
        } catch (Throwable t) {
          result.fail(t);
        }
      }
    });
    return result;
  }

  /**
   * Loads and returns the raw bytes of the asset - useful for custom binary formatted files.
   *
   * @param path the path to the text asset.
   * @throws Exception if there is an error loading the data (for example, if it does not exist).
   * @throws UnsupportedOperationException on platforms that cannot support synchronous asset
   * loading (e.g. HTML5 and Flash).
   */
  public abstract ByteBuffer getBytesSync (String path) throws Exception;

  /**
   * Loads binary data asynchronously. The returned state instance provides a means to listen for
   * the arrival of the data.
   *
   * @param path the path to the binary asset.
   */
  public RFuture<ByteBuffer> getBytes (final String path) {
    final RPromise<ByteBuffer> result = exec.deferredPromise();
    exec.invokeAsync(new Runnable() {
      public void run () {
        try {
          result.succeed(getBytesSync(path));
        } catch (Throwable t) {
          result.fail(t);
        }
      }
    });
    return result;
  }

  protected final Exec exec;

  protected Assets (Exec exec) {
    this.exec = exec;
  }

  /**
   * Synchronously loads image data at {@code path}.
   */
  protected abstract ImageImpl.Data load (String path) throws Exception;

  /**
   * Creates an image with the specified width and height.
   * @param async whether the image is being loaded synchronously or not. This should be passed
   * through to the {@link ImageImpl} constructor.
   */
  protected abstract ImageImpl createImage (
    boolean async, int rawWidth, int rawHeight, String source);

  /**
   * Normalizes the path, by removing {@code foo/..} pairs until the path contains no {@code ..}s.
   * For example:
   * {@code foo/bar/../baz/bif/../bonk.png} becomes {@code foo/baz/bonk.png} and
   * {@code foo/bar/baz/../../bing.png} becomes {@code foo/bing.png}.
   */
  protected static String normalizePath (String path) {
    int pathLen;
    do {
      pathLen = path.length();
      path = path.replaceAll("[^/]+/\\.\\./", "");
    } while (path.length() != pathLen);
    return path;
  }
}
