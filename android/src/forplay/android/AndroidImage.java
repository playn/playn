/**
 * Copyright 2010 The ForPlay Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package forplay.android;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;

import forplay.core.Asserts;
import forplay.core.Canvas;
import forplay.core.CanvasImage;
import forplay.core.Image;
import forplay.core.ResourceCallback;

import java.util.ArrayList;
import java.util.List;

class AndroidImage implements CanvasImage {

  private Bitmap bitmap;
  private AndroidCanvas canvas;
  private List<ResourceCallback<Image>> callbacks = new ArrayList<ResourceCallback<Image>>();

  public AndroidImage(Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  public AndroidImage(int w, int h) {
    // TODO(jgw): Is there a way to get the "native" bitmap config (i.e., whatever will be fastest)?
    bitmap = Bitmap.createBitmap(w, h, Config.ARGB_4444);
    canvas = new AndroidCanvas(new android.graphics.Canvas(bitmap));
  }

  public AndroidImage(String url) {
    AsyncTask<String,Void,Bitmap> execute = new AndroidAssetManager.DownloaderTask<Bitmap>() {
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
    return bitmap == null ? 0 : bitmap.getHeight();
  }

  public boolean isReady() {
    return bitmap != null;
  }

  public void replaceWith(Image image) {
    Asserts.checkArgument(image instanceof AndroidImage);
    bitmap = ((AndroidImage) image).bitmap;
  }

  public int width() {
    return bitmap == null ? 0 : bitmap.getWidth();
  }

  Bitmap getBitmap() {
    return bitmap;
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
