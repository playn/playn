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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import forplay.core.Asserts;
import forplay.core.Canvas;
import forplay.core.CanvasImage;
import forplay.core.Image;
import forplay.core.ResourceCallback;

class AndroidImage implements CanvasImage {
  
  // TODO: Hack to deal with low memory devices
  static List<Bitmap> mru = new ArrayList<Bitmap>();
  static List<Bitmap> prevMru;
  
  private Bitmap bitmap;
  private SoftReference<Bitmap> bitmapRef;
  private AndroidCanvas canvas;
  private List<ResourceCallback<Image>> callbacks = new ArrayList<ResourceCallback<Image>>();
  private int width, height;
  private String path;

  public AndroidImage(String path, Bitmap bitmap) {
    this.path = path;
    bitmapRef = new SoftReference<Bitmap>(bitmap);
    width = bitmap.getWidth();
    height = bitmap.getHeight();
  }

  public AndroidImage(int w, int h, boolean alpha) {
    bitmap = Bitmap.createBitmap(w, h, alpha ? AndroidPlatform.instance.preferredBitmapConfig
        : Bitmap.Config.RGB_565);
    canvas = new AndroidCanvas(new android.graphics.Canvas(bitmap));
  }

  public AndroidImage(String url) {
    AsyncTask<String, Void, Bitmap> execute = new AndroidAssetManager.DownloaderTask<Bitmap>() {
      @Override
      public Bitmap download(String url) {
        return AndroidAssetManager.downloadBitmap(url);
      }

      @Override
      protected void onPostExecute(Bitmap data) {
        super.onPostExecute(data);
        runCallbacks(data != null);
      }
    };
    execute.execute(url);
  }

  public void addCallback(ResourceCallback<Image> callback) {
    callbacks.add(callback);
    if (isReady()) {
      runCallbacks(true);
    }
  }

  public Canvas canvas() {
    return canvas;
  }

  public int height() {
    return bitmap == null ? height : bitmap.getHeight();
  }

  public boolean isReady() {
    return bitmap != null || bitmapRef != null;
  }

  public void replaceWith(Image image) {
    Asserts.checkArgument(image instanceof AndroidImage);
    bitmap = ((AndroidImage) image).bitmap;
  }

  public int width() {
    return bitmap == null ? width : bitmap.getWidth();
  }

  Bitmap getBitmap() {
    if (bitmap != null)
      return bitmap;
    if (bitmapRef != null) {
      Bitmap bm = bitmapRef.get();
      if (bm == null) {
        Log.i("forplay", "Bitmap " + path + " fell out of memory");
        bitmapRef = new SoftReference<Bitmap>(
            bm = ((AndroidAssetManager) AndroidPlatform.instance.assetManager()).doGetBitmap(path));
      }
      mru.add(bm);
      return bm;
    }

    return null;
  }

  private void runCallbacks(boolean success) {
    for (ResourceCallback<Image> cb : callbacks) {
      if (success) {
        cb.done(this);
      } else {
        cb.error(new Exception("Error loading image"));
      }
    }
    callbacks.clear();
  }
}
