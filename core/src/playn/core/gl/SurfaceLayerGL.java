/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core.gl;

import playn.core.Asserts;
import playn.core.InternalTransform;
import playn.core.Surface;
import playn.core.SurfaceLayer;

public class SurfaceLayerGL extends LayerGL implements SurfaceLayer {

  private SurfaceGL surface;

  public SurfaceLayerGL(GLContext ctx, float width, float height) {
    this(ctx, new SurfaceGL(ctx, width, height));
  }

  public SurfaceLayerGL(GLContext ctx, SurfaceGL surface) {
    super(ctx);
    this.surface = surface;
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
  public void paint(InternalTransform parentTransform, float parentAlpha, GLShader curShader) {
    if (!visible()) return;
    surface.paint(localTransform(parentTransform), parentAlpha * alpha,
                  (shader == null) ? curShader : shader);
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
