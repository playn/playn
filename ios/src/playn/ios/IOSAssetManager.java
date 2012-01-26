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
package playn.ios;

import cli.System.IO.FileAccess;
import cli.System.IO.FileMode;
import cli.System.IO.FileShare;
import cli.System.IO.FileStream;
import cli.System.IO.Path;
import cli.System.IO.Stream;
import cli.System.IO.StreamReader;

import cli.MonoTouch.Foundation.NSData;
import cli.MonoTouch.UIKit.UIImage;

import playn.core.Asserts;
import playn.core.AssetManager;
import playn.core.Image;
import playn.core.PlayN;
import playn.core.ResourceCallback;
import playn.core.Sound;

class IOSAssetManager implements AssetManager
{
  private String pathPrefix = "";

  /**
   * Configures the prefix prepended to asset paths before fetching them from the app directory.
   * Note that you specify path components as an array, <em>not</em> a single string that contains
   * multiple components with embedded path separators.
   */
  public void setPathPrefix(String... components) {
    Asserts.checkArgument(components.length > 0);
    for (String component : components) {
      Asserts.checkArgument(!component.contains("/") && !component.contains("\\"),
                            "Path components must not contain path separators: " + component);
    }
    pathPrefix = Path.Combine(components);
  }

  @Override
  public Image getImage(String path) {
    PlayN.log().debug("Loading image " + path);
    String fullPath = Path.Combine(pathPrefix, path);
    try {
      Stream stream = new FileStream(fullPath, FileMode.wrap(FileMode.Open),
                                     FileAccess.wrap(FileAccess.Read),
                                     FileShare.wrap(FileShare.Read));
      NSData data = NSData.FromStream(stream);
      return new IOSImage(IOSPlatform.instance.graphics().ctx, UIImage.LoadFromData(data));
    } catch (Throwable t) {
      PlayN.log().warn("Failed to load image: " + path + " [full=" + fullPath + "]", t);
      return new IOSImage(IOSPlatform.instance.graphics().ctx, new UIImage());
    }
  }

  @Override
  public Sound getSound(String path) {
    PlayN.log().debug("Loading sound " + path);
    return new IOSSound(); // TODO
  }

  @Override
  public void getText(String path, ResourceCallback<String> callback) {
    PlayN.log().debug("Loading text " + path);
    String fullPath = Path.Combine(pathPrefix, path);
    StreamReader reader = null;
    try {
      reader = new StreamReader(fullPath);
      callback.done(reader.ReadToEnd());
    } catch (Throwable t) {
      callback.error(t);
    } finally {
      reader.Close();
    }
  }

  @Override
  public boolean isDone() {
    return true; // nothing is async
  }

  @Override
  public int getPendingRequestCount() {
    return 0; // nothing is async
  }
}
