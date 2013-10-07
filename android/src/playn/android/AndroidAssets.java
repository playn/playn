/**
 * Copyright 2011 The PlayN Authors
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
package playn.android;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

import playn.core.AbstractAssets;
import playn.core.AsyncImage;
import playn.core.Image;
import playn.core.Sound;
import playn.core.gl.Scale;

public class AndroidAssets extends AbstractAssets<Bitmap> {

  /** Extends {@link BitmapFactory.Options} with PlayN asset scale configuration. */
  public class BitmapOptions extends BitmapFactory.Options {
    /** The asset scale for the resulting image. This will be populated with the appropriate scale
     * prior to the call to {@link BitmapOptionsAdjuster#adjustOptions} and can be adjusted by the
     * adjuster if it, for example, adjusts {@link BitmapFactory.Options#inSampleSize} to
     * downsample the image. */
    public Scale scale;
  }

  /** See {@link #setBitmapOptionsAdjuster}. */
  public interface BitmapOptionsAdjuster {
    /** Adjusts the {@link BitmapOptions} based on the app's special requirements.
     * @param path the path passed to {@link #getImage} or URL passed to {@link #getRemoteImage}.*/
    void adjustOptions(String path, BitmapOptions options);
  }

  private final AndroidPlatform platform;
  private final AssetManager assetMgr;
  private String pathPrefix = ""; // 'assets/' is always prepended by AssetManager
  private Scale assetScale = null;

  private BitmapOptionsAdjuster optionsAdjuster = new BitmapOptionsAdjuster() {
    public void adjustOptions(String path, BitmapOptions options) {} // noop!
  };

  AndroidAssets(AndroidPlatform platform) {
    super(platform);
    this.platform = platform;
    this.assetMgr = platform.activity.getResources().getAssets();
  }

  public void setPathPrefix(String prefix) {
    if (prefix.startsWith("/") || prefix.endsWith("/")) {
      throw new IllegalArgumentException("Prefix must not start or end with '/'.");
    }
    pathPrefix = (prefix.length() == 0) ? prefix : (prefix + "/");
  }

  /**
   * Configures the default scale to use for assets. This allows one to use higher resolution
   * imagery than the device might normally. For example, one can supply scale 2 here, and
   * configure the graphics scale to 1.25 in order to use iOS Retina graphics (640x960) on a WXGA
   * (480x800) device.
   */
  public void setAssetScale(float scaleFactor) {
    this.assetScale = new Scale(scaleFactor);
  }

  /**
   * Configures a class that will adjust the {@link BitmapOptions} used to decode loaded bitmaps.
   * An app may wish to use different bitmap configs for different images (say {@code RGB_565} for
   * its non-transparent images) or adjust the dithering settings.
   */
  public void setBitmapOptionsAdjuster(BitmapOptionsAdjuster optionsAdjuster) {
    this.optionsAdjuster = optionsAdjuster;
  }

  @Override
  public Image getRemoteImage(final String url, float width, float height) {
    final AndroidAsyncImage image = new AndroidAsyncImage(platform.graphics().ctx, width, height);
    platform.invokeAsync(new Runnable() {
      public void run () {
        try {
          BitmapOptions options = createOptions(url, false, Scale.ONE);
          setImageLater(image, downloadBitmap(url, options), options.scale);
        } catch (Exception error) {
          setErrorLater(image, error);
        }
      }
    });
    return image;
  }

  @Override
  public Sound getSound(String path) {
    return platform.audio().createSound(path + ".mp3");
  }

  @Override
  public Sound getMusic(String path) {
    return platform.audio().createMusic(path + ".mp3");
  }

  @Override
  public String getTextSync(String path) throws Exception {
    InputStream is = openAsset(path);
    try {
      StringBuilder fileData = new StringBuilder(1000);
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      char[] buf = new char[1024];
      int numRead = 0;
      while ((numRead = reader.read(buf)) != -1) {
        String readData = String.valueOf(buf, 0, numRead);
        fileData.append(readData);
      }
      reader.close();
      return fileData.toString();
    } finally {
      is.close();
    }
  }

  @Override
  public byte[] getBytesSync(String path) throws Exception {
      InputStream is = openAsset(path);
      try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (true) {
          int r = is.read(buf);
          if (r == -1) {
            break;
          }
          out.write(buf, 0, r);
        }

        return out.toByteArray();
      } finally {
        is.close();
      }
  }

  @Override
  protected Image createStaticImage(Bitmap bitmap, Scale scale) {
    return new AndroidImage(platform.graphics().ctx, bitmap, scale);
  }

  @Override
  protected AsyncImage<Bitmap> createAsyncImage(float width, float height) {
    return new AndroidAsyncImage(platform.graphics().ctx, width, height);
  }

  @Override
  protected Image loadImage(String path, ImageReceiver<Bitmap> recv) {
    Exception error = null;
    for (Scale.ScaledResource rsrc : assetScale().getScaledResources(path)) {
      try {
        InputStream is = openAsset(rsrc.path);
        try {
          BitmapOptions options = createOptions(path, true, rsrc.scale);
          return recv.imageLoaded(BitmapFactory.decodeStream(is, null, options), options.scale);
        } finally {
          is.close();
        }
      } catch (FileNotFoundException fnfe) {
        error = fnfe; // keep going, checking for lower resolution images
      } catch (Exception e) {
        error = e;
        break; // the image was broken not missing, stop here
      }
    }
    platform.log().warn("Could not load image: " + pathPrefix + path, error);
    return recv.loadFailed(error != null ? error : new FileNotFoundException(path));
  }

  Typeface getTypeface(String path) {
    return Typeface.createFromAsset(assetMgr, normalizePath(pathPrefix + path));
  }

  AssetFileDescriptor openAssetFd(String path) throws IOException {
    return assetMgr.openFd(normalizePath(pathPrefix + path));
  }

  private Scale assetScale () {
    return (assetScale != null) ? assetScale : platform.graphics().ctx.scale;
  }

  /**
   * Attempts to open the asset with the given name, throwing an {@link IOException} in case of
   * failure.
   */
  private InputStream openAsset(String path) throws IOException {
    String fullPath = normalizePath(pathPrefix + path);
    InputStream is = assetMgr.open(fullPath, AssetManager.ACCESS_STREAMING);
    if (is == null)
      throw new FileNotFoundException("Missing resource: " + fullPath);
    return is;
  }

  private BitmapOptions createOptions(String path, boolean purgeable, Scale scale) {
    BitmapOptions options = new BitmapOptions();
    options.inScaled = false; // don't scale bitmaps based on device parameters
    options.inDither = true;
    options.inPreferredConfig = platform.graphics().preferredBitmapConfig;
    options.inPurgeable = purgeable;
    options.inInputShareable = true;
    options.scale = scale;
    // give the game an opportunity to customize the bitmap options based on the image path
    optionsAdjuster.adjustOptions(path, options);
    return options;
  }

  // Taken from
  // http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
  private Bitmap downloadBitmap(String url, BitmapOptions options) throws Exception {
    AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
    HttpGet getRequest = new HttpGet(url);

    try {
      HttpResponse response = client.execute(getRequest);
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        // we could check the status, but we'll just assume that if there's a Location header,
        // we should follow it
        Header[] headers = response.getHeaders("Location");
        if (headers != null && headers.length > 0) {
          return downloadBitmap(headers[headers.length-1].getValue(), options);
        }
        throw new Exception("Error " + statusCode + " while retrieving bitmap from " + url);
      }

      HttpEntity entity = response.getEntity();
      if (entity == null)
        throw new Exception("Error: getEntity returned null for " + url);

      InputStream inputStream = null;
      try {
        inputStream = entity.getContent();
        return BitmapFactory.decodeStream(inputStream, null, options);
      } finally {
        if (inputStream != null)
          inputStream.close();
        entity.consumeContent();
      }

    } catch (Exception e) {
      // Could provide a more explicit error message for IOException or IllegalStateException
      getRequest.abort();
      platform.log().warn("Error while retrieving bitmap from " + url, e);
      throw e;

    } finally {
      client.close();
    }
  }
}
