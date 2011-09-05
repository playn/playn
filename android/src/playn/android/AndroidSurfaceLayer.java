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

import playn.core.Asserts;
import playn.core.InternalTransform;
import playn.core.Surface;
import playn.core.SurfaceLayer;

class AndroidSurfaceLayer extends AndroidLayer implements SurfaceLayer {

  private AndroidSurface surface;

  private final int width, height;

  AndroidSurfaceLayer(AndroidGraphics gfx, int width, int height) {
    super(gfx);
    this.width = width;
    this.height = height;
    surface = new AndroidSurface(gfx, width, height);

  }

  @Override
  public void destroy() {
    super.destroy();
    surface.destroy();
    surface = null;
  }

  @Override
  public Surface surface() {
    return surface;
  }

  @Override
  public void paint(InternalTransform parentTransform, float parentAlpha) {
    if (!visible())
      return;

    // Draw this layer to the screen upside-down, because its contents are
    // flipped
    // (This happens because it uses the same vertex program as everything else,
    // which flips vertically to put the origin at the top-left).
    gfx.drawTexture(surface.tex(), width, height, localTransform(parentTransform), 0, height, width, -height,
        false, false, parentAlpha * alpha);
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
    return transform().scaleX() * width();
  }

  @Override
  public float scaledHeight() {
    return transform().scaleY() * height();
  }

}
