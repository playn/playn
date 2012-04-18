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

import static playn.core.PlayN.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import playn.core.AbstractAssets;
import playn.core.Image;
import playn.core.ResourceCallback;
import playn.core.Sound;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class AndroidAssets extends AbstractAssets {

  private final AssetManager assets;
  private final AndroidGraphics graphics;
  private final AndroidAudio audio;
  private String pathPrefix = null;

  AndroidAssets(AssetManager assets, AndroidGraphics graphics, AndroidAudio audio) {
    this.assets = assets;
    this.graphics = graphics;
    this.audio = audio;
  }

  public void setPathPrefix(String prefix) {
    if (prefix.startsWith("/") || prefix.endsWith("/")) {
      throw new IllegalArgumentException("Prefix must not start or end with '/'.");
    }
    pathPrefix = (prefix.length() == 0) ? prefix : (prefix + "/");
  }

  /**
   * Attempts to open the asset with the given name, throwing an
   * {@link IOException} in case of failure.
   */
  private InputStream openAsset(String path) throws IOException {
    InputStream is = getClass().getClassLoader().getResourceAsStream(pathPrefix + path);
    if (is == null)
      throw new IOException("Unable to load resource: " + pathPrefix + path);
    return is;
  }

  @Override
  protected Image doGetImage(String path) {
    return createImage(graphics.ctx, doGetBitmap(path));
  }

  protected AndroidImage createImage(AndroidGLContext ctx, Bitmap bitmap) {
    return new AndroidImage(ctx, bitmap);
  }

  /**
   * Decodes a resource to a bitmap. Always succeeds, returning an error
   * placeholder if something goes wrong.
   */
  Bitmap doGetBitmap(String path) {
    try {
      InputStream is = openAsset(path);
      try {
        Bitmap bitmap = decodeBitmap(is);
        return bitmap;
      } finally {
        is.close();
      }
    } catch (IOException e) {
      return createErrorBitmap(e);
    }
  }

  private Bitmap decodeBitmap(InputStream is) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inDither = true;
    // Prefer the bitmap config we computed from the window parameter
    options.inPreferredConfig = graphics.preferredBitmapConfig;
    // Never scale bitmaps based on device parameters
    options.inScaled = false;
    return BitmapFactory.decodeStream(is, null, options);
  }

  private Bitmap createErrorBitmap(Exception e) {
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

  private class ErrorSound implements Sound {
    private final String path;
    private final IOException exception;

    public ErrorSound(String path, IOException exception) {
      this.path = path;
      this.exception = exception;
    }

    @Override
    public boolean play() {
      log().error("Attempted to play sound that was unable to load: " + path);
      return false;
    }

    @Override
    public void stop() {
    }

    @Override
    public void setLooping(boolean looping) {
    }

    @Override
    public void setVolume(float volume) {
    }

    @Override
    public boolean isPlaying() {
      return false;
    }

    @Override
    public void addCallback(ResourceCallback<? super Sound> callback) {
      callback.error(exception);
    }
  }

  @Override
  protected Sound doGetSound(String path) {
    try {
      InputStream in = openAsset(path + ".mp3");
      return audio.createSound(path + ".mp3", in);
    } catch (IOException e) {
      log().error("Unable to load sound: " + path, e);
      return new ErrorSound(path, e);
    }
  }

  @Override
  protected void doGetText(final String path, final ResourceCallback<String> callback) {
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
