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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import pythagoras.f.MathUtil;

import playn.core.AbstractAssets;
import playn.core.Image;
import playn.core.ResourceCallback;
import playn.core.Sound;
import playn.core.gl.Scale;
import static playn.core.PlayN.log;

public class AndroidAssets extends AbstractAssets {

  private final AndroidPlatform platform;
  private String pathPrefix = null;
  private Scale assetScale = null;

  AndroidAssets(AndroidPlatform platform) {
    this.platform = platform;
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

  @Override
  public Image getImage(String path) {
    Exception error = null;
    for (Scale.ScaledResource rsrc : assetScale().getScaledResources(path)) {
      try {
        InputStream is = openAsset(rsrc.path);
        try {
          Bitmap bitmap = decodeBitmap(is);
          // if this image is at a higher scale factor than the view, scale the bitmap down to the
          // view display factor (because otherwise the GPU will end up doing that every time the
          // bitmap is drawn, and it will do a crappy job of it)
          Scale viewScale = platform.graphics().ctx.scale, imageScale = rsrc.scale;
          float viewImageRatio = viewScale.factor / imageScale.factor ;
          if (viewImageRatio < 1) {
            int swidth = MathUtil.iceil(viewImageRatio * bitmap.getWidth());
            int sheight = MathUtil.iceil(viewImageRatio * bitmap.getHeight());
            bitmap = Bitmap.createScaledBitmap(bitmap, swidth, sheight, true);
            imageScale = viewScale;
          }
          return new AndroidImage(platform.graphics().ctx, bitmap, imageScale);
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
    // TODO: create error image which reports failure to callbacks
    // error != null ? error : new FileNotFoundException(path);
    return new AndroidImage(platform.graphics().ctx, createErrorBitmap(), Scale.ONE);
  }

  @Override
  public Sound getSound(String path) {
    try {
      return platform.audio().createSound(path + ".mp3");
    } catch (IOException e) {
      log().error("Unable to load sound: " + path, e);
      return new Sound.Error(e);
    }
  }

  @Override
  public void getText(final String path, final ResourceCallback<String> callback) {
    try {
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
        String text = fileData.toString();
        callback.done(text);
      } finally {
        is.close();
      }
    } catch (IOException e) {
      callback.error(e);
    }
  }

  /**
   * Copies a resource from our APK into a temporary file and returns a handle on that file.
   *
   * @param path the path to the to-be-cached asset.
   * @param cacheName the name to use for the cache file.
   */
  File cacheAsset(String path, String cacheName) throws IOException {
    InputStream in = openAsset(path);
    File cachedFile = new File(platform.activity.getCacheDir(), cacheName);
    try {
      FileOutputStream out = new FileOutputStream(cachedFile);
      try {
        byte[] buffer = new byte[16 * 1024];
        while (true) {
          int r = in.read(buffer);
          if (r < 0)
            break;
          out.write(buffer, 0, r);
        }
      } finally {
        out.close();
      }
    } finally {
      in.close();
    }
    return cachedFile;
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
    InputStream is = getClass().getClassLoader().getResourceAsStream(fullPath);
    if (is == null)
      throw new FileNotFoundException("Missing resource: " + fullPath);
    return is;
  }

  private Bitmap decodeBitmap(InputStream is) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inDither = true;
    // Prefer the bitmap config we computed from the window parameter
    options.inPreferredConfig = platform.graphics().preferredBitmapConfig;
    // Never scale bitmaps based on device parameters
    options.inScaled = false;
    return BitmapFactory.decodeStream(is, null, options);
  }

  private Bitmap createErrorBitmap() {
    int height = 100, width = 100;
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
    android.graphics.Canvas c = new android.graphics.Canvas(bitmap);
    android.graphics.Paint p = new android.graphics.Paint();
    p.setColor(android.graphics.Color.RED);
    for (int yy = 0; yy <= height / 15; yy++) {
      for (int xx = 0; xx <= width / 45; xx++) {
        c.drawText("ERROR", xx * 45, yy * 15, p);
      }
    }
    return bitmap;
  }

  public abstract static class DownloaderTask<T> extends AsyncTask<String, Void, T> {
    private ResourceCallback<T> callback;

    public DownloaderTask() {
    }

    public DownloaderTask(ResourceCallback<T> callback) {
      this.callback = callback;
    }

    @Override
    // Actual download method, run in the task thread
    protected T doInBackground(String... params) {
      // params comes from the execute() call: params[0] is the url.
      return download(params[0]);
    }

    public abstract T download(String url);

    @Override
    protected void onPostExecute(T data) {
      if (callback != null) {
        callback.done(data);
      }
    }
  }

  // Taken from
  // http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
  public Bitmap downloadBitmap(String url) {
    final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
    final HttpGet getRequest = new HttpGet(url);

    try {
      HttpResponse response = client.execute(getRequest);
      final int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url);
        return null;
      }

      final HttpEntity entity = response.getEntity();
      if (entity != null) {
        InputStream inputStream = null;
        try {
          inputStream = entity.getContent();
          return decodeBitmap(inputStream);
        } finally {
          if (inputStream != null) {
            inputStream.close();
          }
          entity.consumeContent();
        }
      }
    } catch (Exception e) {
      // Could provide a more explicit error message for IOException or
      // IllegalStateException
      getRequest.abort();
      Log.w("ImageDownloader", "Error while retrieving bitmap from " + url, e);
    } finally {
      if (client != null) {
        client.close();
      }
    }
    return null;
  }
}
