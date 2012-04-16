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
package playn.flash;

import flash.display.BitmapData;
import flash.geom.Point;
import flash.geom.Rectangle;

import pythagoras.f.MathUtil;

import playn.core.Image;

class FlashImageRegion extends FlashImage implements Image.Region {

  private final FlashImage parent;
  private final float x, y;

  public FlashImageRegion(FlashImage parent, float x, float y, float width, float height) {
    super(crop(parent.bitmapData(), x, y, width, height));
    this.parent = parent;
    this.x = x;
    this.y = y;
  }

  @Override
  public float x () {
    return x;
  }

  @Override
  public float y () {
    return y;
  }

  @Override
  public Image parent () {
    return parent;
  }

  protected static BitmapData crop(BitmapData data, float x, float y, float w, float h) {
    BitmapData sub = BitmapData.create(MathUtil.iceil(w), MathUtil.iceil(h), true, 0x00000000);
    sub.copyPixels(data, Rectangle.create(x, y, w, h), Point.create(0, 0), null, null, true);
    return sub;
  }
}
