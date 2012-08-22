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
 * Base {@link Assets} implementation shared among platforms.
 */
public abstract class AbstractAssets implements Assets {

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

  @Deprecated
  public final boolean isDone() {
    return true;
  }

  @Deprecated
  public final int getPendingRequestCount() {
    return 0;
  }

  protected Image createRemoteErrorImage(Throwable cause, float width, float height) {
    return (width <= 0 || height <= 0) ? createErrorImage(cause) :
      createErrorImage(cause, width, height);
  }

  protected Image createErrorImage(Throwable cause) {
    return createErrorImage(cause, 50, 50);
  }

  protected abstract Image createErrorImage(Throwable cause, float width, float height);

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
