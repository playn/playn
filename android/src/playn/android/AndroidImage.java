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

import playn.core.Image;
import playn.core.Pattern;
import playn.core.ResourceCallback;
import playn.core.gl.GLContext;
import playn.core.gl.ImageGL;

class AndroidImage extends ImageGL implements AndroidGLContext.Refreshable {

  private final AndroidGLContext ctx;
  private final Bitmap bitmap;

  AndroidImage(AndroidGLContext ctx, Bitmap bitmap) {
    this.ctx = ctx;
    this.bitmap = bitmap;
    ctx.addRefreshable(this);
  }

  Bitmap bitmap() {
    return bitmap;
  }

  @Override
  public void addCallback(ResourceCallback<? super Image> callback) {
    // we're always ready immediately
    callback.done(this);
  }

  @Override
  public void onSurfaceCreated() {
  }

  @Override
  public void onSurfaceLost() {
    clearTexture(ctx);
  }

  public void destroy() {
    ctx.removeRefreshable(this);
    clearTexture(ctx);
  }

  @Override
  public int height() {
    return bitmap.getHeight();
  }

  @Override
  public int width() {
    return bitmap.getWidth();
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public Region subImage(float x, float y, float width, float height) {
    return new AndroidImageRegion(ctx, this, x, y, width, height);
  }

  @Override
  public Pattern toPattern() {
    return new AndroidPattern(this);
  }

  @Override
  public void getRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    bitmap.getPixels(rgbArray, offset, scanSize, startX, startY, width, height);
  }

  @Override
  public Image transform(BitmapTransformer xform) {
    return new AndroidImage(ctx, ((AndroidBitmapTransformer) xform).transform(bitmap));
  }

  @Override
  protected void updateTexture(GLContext ctx, Object tex) {
    this.ctx.updateTexture((Integer)tex, bitmap);
  }

  @Override
  protected void finalize() {
    if (tex != null)
      ctx.queueDestroyTexture(tex);
    if (reptex != null)
      ctx.queueDeleteFramebuffer(reptex);
  }
}
