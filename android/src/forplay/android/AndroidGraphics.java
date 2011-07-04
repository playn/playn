/**
 * Copyright 2011 The ForPlay Authors
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
import android.graphics.BitmapShader;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.util.DisplayMetrics;
import forplay.core.Asserts;
import forplay.core.CanvasImage;
import forplay.core.CanvasLayer;
import forplay.core.Gradient;
import forplay.core.Graphics;
import forplay.core.GroupLayer;
import forplay.core.Image;
import forplay.core.ImageLayer;
import forplay.core.Pattern;
import forplay.core.SurfaceLayer;

class AndroidGraphics implements Graphics {

  final AndroidGroupLayer rootLayer;
  private int width, height;
  private DisplayMetrics displayMetrics;

  public AndroidGraphics(GameActivity activity) {
    rootLayer = new AndroidGroupLayer();
    displayMetrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
  }

  @Override
  public CanvasImage createImage(int w, int h) {
    return new AndroidImage(w, h, true);
  }

  public CanvasImage createImage(int w, int h, boolean alpha) {
    return new AndroidImage(w, h, alpha);
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
    Asserts.checkArgument(img instanceof AndroidImage);
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
  public int screenHeight() {
    return displayMetrics.heightPixels;
  }

  @Override
  public int screenWidth() {
    return displayMetrics.widthPixels;
  }

  @Override
  public CanvasLayer createCanvasLayer(int width, int height) {
    return new AndroidCanvasLayer(width, height, true);
  }

  public CanvasLayer createCanvasLayer(int width, int height, boolean alpha) {
	return new AndroidCanvasLayer(width, height, alpha);
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new AndroidGroupLayer();
  }

  @Override
  public ImageLayer createImageLayer() {
    return new AndroidImageLayer();
  }

  @Override
  public ImageLayer createImageLayer(Image image) {
    return new AndroidImageLayer((AndroidImage) image);
  }

  @Override
  public SurfaceLayer createSurfaceLayer(int width, int height) {
    return new AndroidSurfaceLayer(width, height);
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public GroupLayer rootLayer() {
    return rootLayer;
  }

  @Override
  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public int width() {
    return width;
  }
}
