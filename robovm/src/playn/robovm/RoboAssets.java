/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.robovm.apple.foundation.NSBundle;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.uikit.UIImage;

import playn.core.AbstractAssets;
import playn.core.AsyncImage;
import playn.core.Image;
import playn.core.Net;
import playn.core.Sound;
import playn.core.gl.Scale;
import playn.core.util.Callback;

public class RoboAssets extends AbstractAssets<UIImage> {

  private final RoboPlatform platform;
  private final File bundleRoot = new File(NSBundle.getMainBundle().getBundlePath());
  private File assetRoot = new File(bundleRoot, "assets");

  public RoboAssets(RoboPlatform platform) {
    super(platform);
    this.platform = platform;
  }

  /**
   * Configures the prefix prepended to asset paths before fetching them from the app directory.
   */
  public void setPathPrefix(String pathPrefix) {
    this.assetRoot = new File(bundleRoot, pathPrefix);
  }

  @Override
  public Image getRemoteImage(String url, float width, float height) {
    final RoboAsyncImage image = new RoboAsyncImage(platform.graphics().ctx, width, height);
    platform.net().req(url).execute(new Callback<Net.Response>() {
      public void onSuccess (Net.Response rsp) {
        image.setImage(UIImage.create(new NSData(rsp.payload())), Scale.ONE);
      }
      public void onFailure (Throwable cause) {
        image.setError(cause);
      }
    });
    return image;
  }

  @Override
  public Sound getSound(String path) {
    return createSound(path, false);
  }

  @Override
  public Sound getMusic(String path) {
    return createSound(path, true);
  }

  @Override
  public String getTextSync(String path) throws Exception {
    platform.log().debug("Loading text " + path);
    return new String(getBytesSync(path), "UTF-8");
  }

  @Override
  public byte[] getBytesSync(String path) throws Exception {
    File fullPath = resolvePath(path);
    platform.log().debug("Loading bytes " + fullPath);
    FileInputStream in = new FileInputStream(fullPath);
    try {
      byte[] data = new byte[(int)fullPath.length()];
      if (in.read(data) != data.length) {
        throw new IOException("Failed to read entire file: " + fullPath);
      }
      return data;
    } finally {
      in.close();
    }
  }

  @Override
  protected Image createStaticImage(UIImage uiImage, Scale scale) {
    return new RoboImage(platform.graphics().ctx, uiImage.getCGImage(), scale);
  }

  @Override
  protected AsyncImage<UIImage> createAsyncImage(float width, float height) {
    return new RoboAsyncImage(platform.graphics().ctx, width, height);
  }

  @Override
  protected Image loadImage(String path, ImageReceiver<UIImage> recv) {
    Throwable error = null;
    for (Scale.ScaledResource rsrc : platform.graphics().ctx().scale.getScaledResources(path)) {
      File fullPath = resolvePath(rsrc.path);
      if (!fullPath.exists()) continue;

      // platform.log().debug("Loading image: " + fullPath);
      UIImage img = UIImage.create(fullPath);
      if (img != null) return recv.imageLoaded(img, rsrc.scale);

      // note this error if this is the lowest resolution image, but fall back to lower resolution
      // images if not; in the Java backend we'd fail here, but this is a production backend, so we
      // want to try to make things work
      platform.log().warn("Failed to load image '" + fullPath + "'.");
      error = new Exception("Failed to load " + fullPath);
    }
    if (error == null) {
      File fullPath = resolvePath(path);
      platform.log().warn("Missing image '" + fullPath + "'.");
      error = new FileNotFoundException(fullPath.toString());
    }
    return recv.loadFailed(error);
  }

  protected File resolvePath (String path) {
    return new File(assetRoot, path);
  }

  private Sound createSound(String path, boolean isMusic) {
    // look for .caf (uncompressed), .aifc (compressed, but fast), then .mp3
    for (String encpath : new String[] { path + ".caf", path + ".aifc", path + ".mp3" }) {
      File fullPath = resolvePath(encpath);
      if (!fullPath.exists()) continue;
      return platform.audio().createSound(fullPath, isMusic);
    }

    platform.log().warn("Missing sound: " + path);
    return new Sound.Error(new FileNotFoundException(path));
  }
}
