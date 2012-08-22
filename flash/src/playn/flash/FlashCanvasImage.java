/*
 * Copyright 2010 Google Inc.
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
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Image;
import playn.core.util.Callback;

public class FlashCanvasImage extends FlashImage implements CanvasImage {

  private FlashCanvas canvas;

  public FlashCanvasImage(FlashCanvas canvas) {
    super(canvas.bitmapData());
    this.canvas = canvas;

  }

  @Override
  public Canvas canvas() {
    return canvas;
  }

  @Override
  public float height() {
    return canvas().height();
  }

  @Override
  public float width() {
    return canvas().width();
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void addCallback(Callback<? super Image> callback) {
    callback.onSuccess(this);
  }

  @Override
  BitmapData bitmapData() {
    return canvas.bitmapData();
  }
}
