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
import playn.core.Canvas;
import playn.core.CanvasLayer;
import playn.core.InternalTransform;
import playn.core.gl.LayerGL;

class AndroidCanvasLayer extends LayerGL implements CanvasLayer {

  private AndroidImage image;

  AndroidCanvasLayer(AndroidGLContext ctx, int width, int height, boolean alpha) {
    super(ctx);
    image = (AndroidImage) (AndroidPlatform.instance.graphics().createImage(width, height, alpha));
  }

  @Override
  public Canvas canvas() {
    return image.canvas();
  }

  @Override
  public void destroy() {
    super.destroy();
    image.destroy(); // don't wait for finalization to release resources
    image = null;
  }

  @Override
  public void paint(InternalTransform parentTransform, float parentAlpha) {
    if (!visible())
      return;

    int tex = (Integer) image.ensureTexture((AndroidGLContext) ctx, false, false);
    if (tex != -1) {
      InternalTransform xform = localTransform(parentTransform);
      float childAlpha = parentAlpha * alpha;
      ctx.drawTexture(tex, image.width(), image.height(), xform, width(), height(),
                      false, false, childAlpha);
    }
  }

  @Override
  public float width() {
    Asserts.checkNotNull(image, "Canvas must not be null");
    return image.width();
  }

  @Override
  public float height() {
    Asserts.checkNotNull(image, "Canvas must not be null");
    return image.height();
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
