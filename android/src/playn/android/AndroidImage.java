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

import android.graphics.Bitmap;
import android.opengl.GLUtils;

import playn.core.*;

public class AndroidImage extends ImageImpl {

  protected Bitmap bitmap;

  public AndroidImage(Graphics gfx, Scale scale, Bitmap bitmap, String source) {
    super(gfx, scale, bitmap.getWidth(), bitmap.getHeight(), source, bitmap);
    // TODO: move elsewhere: ((AndroidGLContext) ctx).addRefreshable(this);
  }

  public AndroidImage (AndroidPlatform plat, boolean async, int preWidth, int preHeight,
                       String source) {
    super(plat, async, Scale.ONE, preWidth, preHeight, source);
  }

  /**
   * Returns the {@link Bitmap} that underlies this image. This is for games that need to write
   * custom backend code to do special stuff. No promises are made, caveat coder.
   */
  public Bitmap bitmap() {
    return bitmap;
  }

  @Override public Pattern createPattern (boolean repeatX, boolean repeatY) {
    return new AndroidPattern(repeatX, repeatY, bitmap);
  }

  @Override public void getRgb(int startX, int startY, int width, int height,
                               int[] rgbArray, int offset, int scanSize) {
    bitmap.getPixels(rgbArray, offset, scanSize, startX, startY, width, height);
  }

  @Override public void setRgb(int startX, int startY, int width, int height,
                               int[] rgbArray, int offset, int scanSize) {
    bitmap.setPixels(rgbArray, offset, scanSize, startX, startY, width, height);
  }

  @Override public Image transform(BitmapTransformer xform) {
    Bitmap nbitmap = ((AndroidBitmapTransformer) xform).transform(bitmap);
    return new AndroidImage(gfx, scale, nbitmap, source);
  }

  @Override public void draw (Object ctx, float x, float y, float w, float h) {
    draw(ctx, x, y, w, h, 0, 0, width(), height());
  }

  @Override public void draw (Object ctx, float dx, float dy, float dw, float dh,
                              float sx, float sy, float sw, float sh) {
    // adjust our source rect to account for the scale factor
    sx *= scale.factor;
    sy *= scale.factor;
    sw *= scale.factor;
    sh *= scale.factor;
    ((AndroidCanvas)ctx).draw(bitmap, dx, dy, dw, dh, sx, sy, sw, sh);
  }

  @Override public String toString () {
    return "Image[src=" + source + ", bitmap=" + bitmap + "]";
  }

  @Override protected void upload (Graphics gfx, Texture tex) {
    gfx.gl.glBindTexture(GL20.GL_TEXTURE_2D, tex.id);
    GLUtils.texImage2D(GL20.GL_TEXTURE_2D, 0, bitmap, 0);
    gfx.gl.checkError("updateTexture end");
  }

  @Override protected void setBitmap (Object bitmap) {
    this.bitmap = (Bitmap)bitmap;
  }

  @Override protected Object createErrorBitmap (int pixelWidth, int pixelHeight) {
    Bitmap bitmap = Bitmap.createBitmap(pixelWidth, pixelHeight, Bitmap.Config.ARGB_4444);
    android.graphics.Canvas c = new android.graphics.Canvas(bitmap);
    android.graphics.Paint p = new android.graphics.Paint();
    p.setColor(android.graphics.Color.RED);
    for (int yy = 0; yy <= pixelHeight / 15; yy++) {
      for (int xx = 0; xx <= pixelWidth / 45; xx++) {
        c.drawText("ERROR", xx * 45, yy * 15, p);
      }
    }
    return bitmap;
  }
}
