/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.android;

import static forplay.core.ForPlay.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import forplay.core.AbstractAssetManager;
import forplay.core.Image;
import forplay.core.ResourceCallback;
import forplay.core.Sound;

public class AndroidAssetManager extends AbstractAssetManager {

  public String pathPrefix = null;
  public AssetManager assets;
  public static LayoutParams windowAttributes;

  public void setPathPrefix(String prefix) {
    pathPrefix = prefix;
  }

  private InputStream openResource(String path) throws IOException {
    // Insert a slash to make this consistent with the Java asset manager
    return getClass().getClassLoader().getResourceAsStream(pathPrefix + "/" + path);
  }

  @Override
  protected Image doGetImage(String path) {
    try {
      InputStream is = openResource(path);
      if (is == null) {
        // TODO: This should return an error image like JavaAssetManager does
        throw new RuntimeException("Unable to load image " + path);
      }
      try {
        return new AndroidImage(path, BitmapFactory.decodeStream(is));
      } finally {
        is.close();
      }
    } catch (IOException e) {
      // TODO: This should return an error image like JavaAssetManager does
      throw new RuntimeException(e);
    }
  }

  Bitmap doGetBitmap(String path) {
    try {
      InputStream is = openResource(path);
      if (is == null) {
        // TODO: This should return an error image like JavaAssetManager does
        throw new RuntimeException("Unable to load image " + path);
      }
      try {
        return BitmapFactory.decodeStream(is);
      } finally {
        is.close();
      }
    } catch (IOException e) {
      // TODO: This should return an error image like JavaAssetManager does
      throw new RuntimeException(e);
    }
  }

  private class ErrorSound implements Sound {
    @Override
    public boolean play() {
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
  }
  
  @Override
  protected Sound doGetSound(String path) {
    try {
      InputStream in = openResource(path + ".wav");
      
      if (in == null) {
        log().error("Unable to find sound resource: " + path);
        return new ErrorSound();
      }
      
      Sound sound = ((AndroidAudio) AndroidPlatform.instance.audio()).getSound(path + ".wav", in);
      return sound == null ? new ErrorSound() : sound;
    } catch (IOException e) {
      log().error("Unable to load sound: " + path, e);
      return new ErrorSound();
    }
  }

  @Override
  protected void doGetText(final String path, final ResourceCallback<String> callback) {
    try {
      InputStream is = openResource(path);
      try {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
          String readData = String.valueOf(buf, 0, numRead);
          fileData.append(readData);
          buf = new char[1024];
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

  // taken from
  // http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
  public static Bitmap downloadBitmap(String url) {
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
          BitmapFactory.Options options = new BitmapFactory.Options();
          options.inDither = true;
          options.inPreferredConfig = AndroidPlatform.instance.preferredBitmapConfig;
          options.inScaled = false;
          final Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
          return bitmap;
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
