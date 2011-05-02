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
import forplay.core.Image;
import forplay.core.Surface;
import forplay.core.SurfaceImage;

class AndroidImage implements SurfaceImage {

  private Bitmap bitmap;

  public AndroidImage(int w, int h) {
    // TODO(jgw): Is there a way to get the "native" bitmap config (i.e., whatever will be fastest)?
    bitmap = Bitmap.createBitmap(w, h, Config.ARGB_4444);
  }

  public AndroidImage(Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  public int height() {
    return bitmap.getHeight();
  }

  public void replaceWith(Image image) {
    assert image instanceof AndroidImage;
    bitmap = ((AndroidImage) image).bitmap;
  }

  public int width() {
    return bitmap.getWidth();
  }

  public Surface surface() {
    // TODO: Can we paint to any old bitmap, or not?
    throw new UnsupportedOperationException();
  }

  Bitmap getBitmap() {
    return bitmap;
  }
}
