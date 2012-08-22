/**
 * Copyright 2012 The PlayN Authors
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

import android.graphics.Bitmap;

import pythagoras.f.MathUtil;

import playn.core.Image;
import playn.core.gl.GLContext;
import playn.core.gl.Scale;
import playn.core.util.Callback;

public class AndroidErrorImage extends AndroidImage {

  private final Throwable error;

  public AndroidErrorImage(GLContext ctx, Throwable error, float width, float height) {
    super(ctx, createErrorBitmap(width, height), Scale.ONE);
    this.error = error;
  }

  @Override
  public boolean isReady() {
    return false;
  }

  @Override
  public void addCallback(Callback<? super Image> callback) {
    callback.onFailure(error);
  }

  private static Bitmap createErrorBitmap(float fwidth, float fheight) {
    int height = MathUtil.iceil(fheight), width = MathUtil.iceil(fwidth);
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
}
