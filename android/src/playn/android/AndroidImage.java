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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import playn.core.Asserts;
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Image;
import playn.core.ResourceCallback;

/**
 * Android implementation of CanvasImage class. Prioritizes the SoftReference to
 * the bitmap, and only holds a hard reference if the game has requested that a
 * Canvas be created.
 */
class AndroidImage implements CanvasImage {

  private SoftReference<Bitmap> bitmapRef;
  private AndroidCanvas canvas;
  private Bitmap canvasBitmap;
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
    Bitmap newBitmap = Bitmap.createBitmap(w, h, alpha
        ? AndroidPlatform.instance.preferredBitmapConfig : Bitmap.Config.RGB_565);
    bitmapRef = new SoftReference<Bitmap>(newBitmap);
    width = w;
    height = h;
  }

  public void addCallback(ResourceCallback<Image> callback) {
    callbacks.add(callback);
    if (isReady()) {
      runCallbacks(true);
    }
  }

  public Canvas canvas() {
    if (canvas == null) {
      canvasBitmap = getBitmap();
      canvas = new AndroidCanvas(new android.graphics.Canvas(canvasBitmap));
    }
    bitmapRef = null;
    return canvas;
  }

  public int height() {
    return height;
  }

  public boolean isReady() {
    return bitmapRef != null || canvas != null;
  }

  public void replaceWith(Image image) {
    Asserts.checkArgument(image instanceof AndroidImage);
    bitmapRef = new SoftReference<Bitmap>(((AndroidImage) image).getBitmap());
    canvas = null;
  }

  public int width() {
    return width;
  }

  public Bitmap getBitmap() {
    if (canvasBitmap != null) {
      return canvasBitmap;
    }
    if (bitmapRef != null) {
      Bitmap bm = bitmapRef.get();
      if (bm == null && path != null) {
        // Log.i("playn", "Bitmap " + path + " fell out of memory");
        bitmapRef = new SoftReference<Bitmap>(
            bm = AndroidPlatform.instance.assetManager().doGetBitmap(path));
      }
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
