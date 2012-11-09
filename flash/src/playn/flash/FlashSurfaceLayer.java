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

import pythagoras.f.MathUtil;

import flash.display.Sprite;
import playn.flash.FlashCanvas.CanvasElement;

import playn.core.Asserts;
import playn.core.Surface;
import playn.core.SurfaceLayer;

/**
 *
 */
public class FlashSurfaceLayer extends FlashLayer implements SurfaceLayer {

  private FlashSurface surface;

  /**
   * @param width
   * @param height
   */
  public FlashSurfaceLayer(float width, float height) {
    super((Sprite) CanvasElement.create(MathUtil.iceil(width), MathUtil.iceil(height)).cast());
    surface = new FlashSurface(width, height, ((CanvasElement) display().cast()).getContext());
  }

  /* (non-Javadoc)
   * @see playn.core.SurfaceLayer#surface()
   */
  @Override
  public Surface surface() {
    // TODO Auto-generated method stub
    return surface;
  }

  @Override
  public float width() {
    Asserts.checkNotNull(surface, "Surface must not be null");
    return surface.width();
  }

  @Override
  public float height() {
    Asserts.checkNotNull(surface, "Surface must not be null");
    return surface.height();
  }

  @Override
  public float scaledWidth() {
    return scaleX() * width();
  }

  @Override
  public float scaledHeight() {
    return scaleY() * height();
  }
}
