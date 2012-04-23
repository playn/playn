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

class AndroidImageRegion extends AndroidImage implements Image.Region {

  private final AndroidImage parent;
  private final float x, y;

  public AndroidImageRegion(AndroidGLContext ctx, AndroidImage parent,
                            float x, float y, float width, float height) {
    super(ctx, Bitmap.createBitmap(parent.bitmap(), MathUtil.ifloor(x), MathUtil.ifloor(y),
                                   MathUtil.iceil(width), MathUtil.iceil(height)));
    this.parent = parent;
    this.x = x;
    this.y = y;
  }

  @Override
  public float x() {
    return x;
  }

  @Override
  public float y() {
    return y;
  }

  @Override
  public Image parent() {
    return parent;
  }
}
