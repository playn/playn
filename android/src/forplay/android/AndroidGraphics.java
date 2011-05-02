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
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import forplay.core.AbstractGraphics;
import forplay.core.Gradient;
import forplay.core.Image;
import forplay.core.Pattern;
import forplay.core.ResourceCallback;
import forplay.core.SurfaceImage;

class AndroidGraphics extends AbstractGraphics {

  private final GameActivity activity;

  AndroidGraphics(GameActivity activity) {
    this.activity = activity;
  }

  @Override
  public SurfaceImage createImage(int w, int h) {
    return new AndroidImage(w, h);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1, int[] colors, float[] positions) {
    LinearGradient gradient = new LinearGradient(x0, y0, x1, y1, colors, positions, TileMode.CLAMP);
    return new AndroidGradient(gradient);
  }

  public forplay.core.Path createPath() {
    return new AndroidPath();
  }

  @Override
  public Pattern createPattern(Image img) {
    assert img instanceof AndroidImage;
    Bitmap bitmap = ((AndroidImage) img).getBitmap();
    BitmapShader shader = new BitmapShader(bitmap, TileMode.REPEAT, TileMode.REPEAT);
    return new AndroidPattern(shader);
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors, float[] positions) {
    RadialGradient gradient = new RadialGradient(x, y, r, colors, positions, TileMode.CLAMP);
    return new AndroidGradient(gradient);
  }

  @Override
  protected void doLoadImage(String name, ResourceCallback<Image> cb) {
    // TODO(jgw): Total hack. Find some way to map cached files into resources.
    Bitmap decodeFile = BitmapFactory.decodeFile("/sdcard/" + name);
    if (decodeFile != null) {
      cb.done(new AndroidImage(decodeFile));
    } else {
      cb.error(new RuntimeException("Unable to load " + name));
    }
  }

  @Override
  public int screenHeight() {
    return activity.gameView().getHeight();
  }

  @Override
  public int screenWidth() {
    return activity.gameView().getWidth();
  }
}
