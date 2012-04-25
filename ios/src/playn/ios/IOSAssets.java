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

import cli.System.IO.File;
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
import playn.core.Assets;
import playn.core.Image;
import playn.core.PlayN;
import playn.core.ResourceCallback;
import playn.core.Sound;

public class IOSAssets implements Assets
{
  private String pathPrefix = "";
  private final IOSGraphics graphics;
  private final IOSAudio audio;

  public IOSAssets(IOSGraphics graphics, IOSAudio audio) {
    this.graphics = graphics;
    this.audio = audio;
  }

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
    String fullPath = Path.Combine(pathPrefix, path);
    String scaledPath = graphics.adjustImagePath(fullPath);
    if (File.Exists(scaledPath))
      fullPath = scaledPath;
    PlayN.log().debug("Loading image: " + fullPath);
    try {
      Stream stream = new FileStream(fullPath, FileMode.wrap(FileMode.Open),
                                     FileAccess.wrap(FileAccess.Read),
                                     FileShare.wrap(FileShare.Read));
      NSData data = NSData.FromStream(stream);
      return createImage(graphics.ctx, UIImage.LoadFromData(data));
    } catch (Throwable t) {
      PlayN.log().warn("Failed to load image: " + fullPath, t);
      return createImage(graphics.ctx, new UIImage());
    }
  }

  @Override
  public Sound getSound(String path) {
    path += ".mp3";
    PlayN.log().debug("Loading sound " + path);
    String fullPath = Path.Combine(pathPrefix, path);
    return audio.createSound(fullPath);
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
      if (reader != null) {
        reader.Close();
      }
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

  protected IOSAbstractImage createImage(IOSGLContext ctx, UIImage image) {
    return new IOSImage(ctx, image);
  }
}
