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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import forplay.core.AbstractAssetManager;
import forplay.core.Image;
import forplay.core.ResourceCallback;
import forplay.core.Sound;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

class AndroidAssetManager extends AbstractAssetManager {

  @Override
  protected Image doGetImage(String path) {
    // TODO(jgw): Temporary hack: Load everything from /sdcard.
    return new AndroidImage(BitmapFactory.decodeFile("/sdcard/" + path));
//    return new AndroidImage(path);
  }

  @Override
  protected Sound doGetSound(String path) {
    // TODO(jgw): Implement me.
    return null;
  }

  @Override
  protected void doGetText(final String path, final ResourceCallback<String> callback) {
    File f = new File("/sdcard/" + path);
    if (!f.exists()) {
      callback.error(new FileNotFoundException(path));
      return;
    }

    try {
      StringBuffer fileData = new StringBuffer(1000);
      BufferedReader reader = new BufferedReader(new FileReader(f));
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
    } catch (IOException e) {
      callback.error(e);
    }

//    new DownloaderTask<String>(callback) {
//      @Override
//      public String download(String url) {
//        try {
//          HttpClient httpclient = new DefaultHttpClient();
//          HttpResponse response = httpclient.execute(new HttpGet(path));
//
//          return EntityUtils.toString(response.getEntity());
//        } catch (Exception e) {
//          return null;
//        }
//      }
//    }.execute(path);
  }

  public abstract static class DownloaderTask<T> extends AsyncTask<String, Void, T> {
    private String url;
    private ResourceCallback<T> callback;

    public DownloaderTask() {
    }

    public DownloaderTask(ResourceCallback<T> callback) {
      this.callback = callback;
    }

    @Override
    // Actual download method, run in the task thread
        protected
        T doInBackground(String... params) {
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
          final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
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
