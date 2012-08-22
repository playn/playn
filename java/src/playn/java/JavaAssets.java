/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.java;

import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import pythagoras.f.MathUtil;

import playn.core.AbstractAssets;
import playn.core.Image;
import playn.core.Sound;
import playn.core.gl.Scale;
import playn.core.util.Callback;

/**
 * Loads Java assets via the classpath.
 */
public class JavaAssets extends AbstractAssets {

  /** Makes asset loading asynchronous to mimic the behavior of the HTML backend. */
  private static final boolean asyncLoad = Boolean.getBoolean("playn.java.asyncLoad");

  private final JavaPlatform platform;
  private String pathPrefix = "";
  private Scale assetScale = null;

  static void doResourceAction(Runnable action) {
    if (asyncLoad)
      EventQueue.invokeLater(action);
    else
      action.run();
  }

  public JavaAssets (JavaPlatform platform) {
    this.platform = platform;
  }

  /**
   * Configures the prefix prepended to asset paths before fetching them from the classpath. For
   * example, if your assets are in {@code src/main/java/com/mygame/assets} (or in {@code
   * src/main/resources/com/mygame/assets}), you can pass {@code com/mygame/assets} to this method
   * and then load your assets without prefixing their path with that value every time. The value
   * supplied to this method should not contain leading or trailing slashes. Note that this prefix
   * should always use '/' as a path separator as it is used to construct URLs, not filesystem
   * paths.
   */
  public void setPathPrefix(String prefix) {
    if (prefix.startsWith("/") || prefix.endsWith("/")) {
      throw new IllegalArgumentException("Prefix must not start or end with '/'.");
    }
    pathPrefix = (prefix.length() == 0) ? prefix : (prefix + "/");
  }

  /**
   * Returns the currently configured path prefix. Note that this value will always have a trailing
   * slash.
   */
  public String getPathPrefix() {
    return pathPrefix;
  }

  /**
   * Configures the default scale to use for assets. This allows one to specify an intermediate
   * graphics scale (like 1.5) and scale the 2x imagery down to 1.5x instead of scaling the 1.5x
   * imagery up (or displaying nothing at all).
   */
  public void setAssetScale(float scaleFactor) {
    this.assetScale = new Scale(scaleFactor);
  }

  @Override
  public Image getImage(String path) {
    JavaGraphics graphics = platform.graphics();
    Exception error = null;
    for (Scale.ScaledResource rsrc : assetScale().getScaledResources(pathPrefix + path)) {
      try {
        BufferedImage image = ImageIO.read(requireResource(rsrc.path));
        // if image is at a higher scale factor than the view, scale it to the view display factor
        Scale viewScale = platform.graphics().ctx().scale, imageScale = rsrc.scale;
        float viewImageRatio = viewScale.factor / imageScale.factor;
        if (viewImageRatio < 1) {
          image = scaleImage(image, viewImageRatio);
          imageScale = viewScale;
        }
        return graphics.createStaticImage(image, imageScale);
      } catch (FileNotFoundException fnfe) {
        error = fnfe; // keep going, checking for lower resolution images
      } catch (Exception e) {
        error = e;
        break; // the image was broken not missing, stop here
      }
    }
    platform.log().warn("Could not load image: " + pathPrefix + path, error);
    return createErrorImage(error != null ? error : new FileNotFoundException(path));
  }

  @Override
  public Image getRemoteImage(final String url, float width, float height) {
    final JavaAsyncImage image = platform.graphics().createAsyncImage(width, height);
    new Thread() {
      public void run () {
        try {
          final BufferedImage bufimg = ImageIO.read(new URL(url));
          platform.invokeLater(new Runnable() {
            public void run () {
              image.setImage(bufimg);
            }
          });
        } catch (final Exception error) {
          platform.invokeLater(new Runnable() {
            public void run () {
              image.setError(error);
            }
          });
        }
      }
    }.start();
    return image;
  }

  @Override
  public Sound getSound(String path) {
    final String soundPath = path + ".mp3";
    try {
      return platform.audio().createSound(soundPath, getAssetStream(soundPath));
    } catch (Exception e) {
      platform.log().warn("Sound load error " + soundPath + ": " + e);
      return new Sound.Error(e);
    }
  }

  @Override
  public void getText(final String path, final Callback<String> callback) {
    doResourceAction(new Runnable() {
      public void run() {
        try {
          callback.onSuccess(Resources.toString(requireResource(pathPrefix + path), Charsets.UTF_8));
        } catch (Exception e) {
          callback.onFailure(e);
        }
      }
    });
  }

  @Override
  protected Image createErrorImage(Throwable cause, float width, float height) {
    return platform.graphics().createErrorImage(cause, width, height);
  }

  InputStream getAssetStream(String path) throws IOException {
    InputStream in = getClass().getClassLoader().getResourceAsStream(pathPrefix + path);
    if (in == null) {
      throw new FileNotFoundException(path);
    }
    return in;
  }

  protected URL requireResource(String path) throws FileNotFoundException {
    URL url = getClass().getClassLoader().getResource(path);
    if (url == null) {
      throw new FileNotFoundException(path);
    }
    return url;
  }

  private BufferedImage scaleImage (BufferedImage image, float viewImageRatio) {
    int swidth = MathUtil.iceil(viewImageRatio * image.getWidth());
    int sheight = MathUtil.iceil(viewImageRatio * image.getHeight());
    BufferedImage scaled = new BufferedImage(swidth, sheight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D gfx = scaled.createGraphics();
    gfx.drawImage(image.getScaledInstance(swidth, sheight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
    gfx.dispose();
    return scaled;
  }

  private Scale assetScale () {
    return (assetScale != null) ? assetScale : platform.graphics().ctx().scale;
  }
}
