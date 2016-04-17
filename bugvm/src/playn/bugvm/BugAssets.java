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
package playn.bugvm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.bugvm.apple.coregraphics.CGImage;
import com.bugvm.apple.foundation.NSBundle;
import com.bugvm.apple.foundation.NSData;
import com.bugvm.apple.foundation.NSDataReadingOptions;
import com.bugvm.apple.foundation.NSErrorException;
import com.bugvm.apple.uikit.UIImage;

import playn.core.*;
import react.Slot;

public class BugAssets extends Assets {

  private final BugPlatform plat;
  private final File bundleRoot = new File(NSBundle.getMainBundle().getBundlePath());
  private File assetRoot = new File(bundleRoot, "assets");

  public BugAssets(BugPlatform plat) {
    super(plat.exec());
    this.plat = plat;
  }

  /**
   * Configures the prefix prepended to asset paths before fetching them from the app directory.
   */
  public void setPathPrefix(String pathPrefix) {
    this.assetRoot = new File(bundleRoot, pathPrefix);
  }

  @Override public Image getRemoteImage(final String url, int width, int height) {
    final ImageImpl image = createImage(true, width, height, url);
    plat.net().req(url).execute().
      onSuccess(new Slot<Net.Response>() {
        public void onEmit (Net.Response rsp) {
          try {
            image.succeed(toData(Scale.ONE, new UIImage(new NSData(rsp.payload()))));
          } catch (Throwable t) {
            plat.log().warn("Failed to decode remote image [url=" + url + "]", t);
            image.fail(t);
          }
        }
      }).
      onFailure(new Slot<Throwable>() {
        public void onEmit (Throwable cause) { image.fail(cause); }
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
    plat.log().debug("Loading text " + path);
    ByteBuffer data = getBytesSync(path);
    byte[] bytes;
    if (data.hasArray()) bytes = data.array();
    else data.get(bytes = new byte[data.remaining()]);
    return new String(bytes, "UTF-8");
  }

  @Override
  public ByteBuffer getBytesSync(String path) throws Exception {
    File fullPath = resolvePath(path);
    plat.log().debug("Loading bytes " + fullPath);
    // this NSData will release resources when garbage collected
    return NSData.read(fullPath, READ_OPTS).asByteBuffer();
  }

  @Override protected ImageImpl.Data load (String path) throws Exception {
    Exception error = null;
    for (Scale.ScaledResource rsrc : plat.graphics().scale().getScaledResources(path)) {
      File fullPath = resolvePath(rsrc.path);
      if (!fullPath.exists()) continue;

      // plat.log().debug("Loading image: " + fullPath);
      UIImage img = UIImage.getImage(fullPath.toString());
      if (img != null) return toData(rsrc.scale, img);

      // note this error if this is the lowest resolution image, but fall back to lower resolution
      // images if not; in the Java backend we'd fail here, but this is a production backend, so we
      // want to try to make things work
      plat.log().warn("Failed to load image '" + fullPath + "'.");
      error = new Exception("Failed to load " + fullPath);
    }
    if (error == null) {
      File fullPath = resolvePath(path);
      plat.log().warn("Missing image '" + fullPath + "'.");
      error = new FileNotFoundException(fullPath.toString());
    }
    throw error;
  }

  @Override protected ImageImpl createImage (boolean async, int rwid, int rhei, String source) {
    return new BugImage(plat, async, rwid, rhei, source);
  }

  private ImageImpl.Data toData (Scale scale, UIImage image) {
    CGImage bitmap = image.getCGImage();
    return new ImageImpl.Data(scale, bitmap, (int)bitmap.getWidth(), (int)bitmap.getHeight());
  }

  protected File resolvePath (String path) {
    return new File(assetRoot, path);
  }

  private Sound createSound(String path, boolean isMusic) {
    // look for .caf (uncompressed), .aifc (compressed, but fast), then .mp3
    for (String encpath : new String[] { path + ".caf", path + ".aifc", path + ".mp3" }) {
      File fullPath = resolvePath(encpath);
      if (!fullPath.exists()) continue;
      return plat.audio().createSound(fullPath, isMusic);
    }

    plat.log().warn("Missing sound: " + path);
    return new Sound.Error(new FileNotFoundException(path));
  }

  protected static final NSDataReadingOptions READ_OPTS = new NSDataReadingOptions(
    NSDataReadingOptions.MappedIfSafe.value()|NSDataReadingOptions.Uncached.value());
}
