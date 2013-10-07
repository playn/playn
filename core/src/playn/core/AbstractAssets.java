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

import playn.core.gl.Scale;
import playn.core.util.Callback;

/**
 * Base {@link Assets} implementation shared among platforms.
 *
 * @param IMG the type of underlying image used by the image loading mechanism.
 */
public abstract class AbstractAssets<IMG> implements Assets {

  private final AbstractPlatform platform;

  protected interface ImageReceiver<I> {
    Image imageLoaded(I bufimg, Scale scale);
    Image loadFailed(Throwable error);
  }

  @Override
  public Image getImageSync(String path) {
    return loadImage(path, new ImageReceiver<IMG>() {
      @Override
      public Image imageLoaded(IMG impl, Scale scale) {
        return createStaticImage(impl, scale);
      }
      @Override
      public Image loadFailed(Throwable error) {
        return createErrorImage(error);
      }
    });
  }

  @Override
  public Image getImage(final String path) {
    final AsyncImage<IMG> image = createAsyncImage(0, 0);
    platform.invokeAsync(new Runnable() {
      public void run () {
        loadImage(path, new ImageReceiver<IMG>() {
          @Override
          public Image imageLoaded(final IMG impl, final Scale scale) {
            setImageLater(image, impl, scale);
            return image;
          }
          @Override
          public Image loadFailed(final Throwable error) {
            setErrorLater(image, error);
            return image;
          }
        });
      }
    });
    return image;
  }

  @Override
  public Image getRemoteImage(String url) {
    return getRemoteImage(url, 0, 0);
  }

  @Override
  public Image getRemoteImage(String url, float width, float height) {
    Exception error = new Exception("Remote image loading not yet supported: " + url +
                                    "@" + width + "x" + height);
    return createRemoteErrorImage(error, width, height);
  }

  @Override
  public Sound getMusic(String path) {
    return getSound(path);
  }

  @Override
  public void getText(final String path, final Callback<String> callback) {
    platform.invokeAsync(new Runnable() {
      public void run () {
        try {
          platform.notifySuccess(callback, getTextSync(path));
        } catch (Throwable t) {
          platform.notifyFailure(callback, t);
        }
      }
    });
  }

  @Override
  public void getBytes(final String path, final Callback<byte[]> callback) {
    platform.invokeAsync(new Runnable() {
      public void run () {
        try {
          platform.notifySuccess(callback, getBytesSync(path));
        } catch (Throwable t) {
          platform.notifyFailure(callback, t);
        }
      }
    });
  }

  protected AbstractAssets(AbstractPlatform platform) {
    this.platform = platform;
  }

  /**
   * Creates an {@link Image} with the immediately-ready underlying image and scale.
   */
  protected abstract Image createStaticImage(IMG iimpl, Scale scale);

  /**
   * Creates an {@link Image} that will be provided with its underlying image (or error)
   * asynchronously.
   */
  protected abstract AsyncImage<IMG> createAsyncImage(float width, float height);

  /**
   * Synchronously loads the underyling image at the specified path, passing the resulting image to
   * the supplied receiver, or notifying it of failure. NOTE: this may be called from a worker
   * thread.
   */
  protected abstract Image loadImage(String path, ImageReceiver<IMG> recv);

  protected Image createRemoteErrorImage(Throwable cause, float width, float height) {
    return (width <= 0 || height <= 0) ? createErrorImage(cause) :
      createErrorImage(cause, width, height);
  }

  protected Image createErrorImage(Throwable cause) {
    return createErrorImage(cause, 50, 50);
  }

  protected Image createErrorImage(Throwable cause, float width, float height) {
    AsyncImage<IMG> image = createAsyncImage(width, height);
    image.setError(cause);
    return image;
  }

  protected void setImageLater(final AsyncImage<IMG> image, final IMG impl, final Scale scale) {
    platform.invokeLater(new Runnable() {
      public void run () {
        image.setImage(impl, scale);
      }
    });
  }

  protected void setErrorLater(final AsyncImage<?> image, final Throwable error) {
    platform.invokeLater(new Runnable() {
      public void run () {
        image.setError(error);
      }
    });
  }

  /**
   * Normalizes the path, by removing {@code foo/..} pairs until the path contains no {@code ..}s.
   * For example:
   * {@code foo/bar/../baz/bif/../bonk.png} becomes {@code foo/baz/bonk.png} and
   * {@code foo/bar/baz/../../bing.png} becomes {@code foo/bing.png}.
   */
  protected static String normalizePath(String path) {
    int pathLen;
    do {
      pathLen = path.length();
      path = path.replaceAll("[^/]+/\\.\\./", "");
    } while (path.length() != pathLen);
    return path;
  }
}
