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
package playn.core.gl;

import playn.core.Image;
import playn.core.InternalTransform;
import playn.core.Pattern;
import playn.core.Surface;
import playn.core.SurfaceImage;
import playn.core.Tint;
import playn.core.util.Callback;

public class SurfaceImageGL extends AbstractImageGL implements SurfaceImage {

  private final SurfaceGL surface;

  public SurfaceImageGL(GLContext ctx, SurfaceGL surface) {
    super(ctx);
    this.surface = surface;
  }

  @Override
  public Surface surface() {
    return surface;
  }

  @Override
  public void destroy() {
    surface.clearTexture();
  }

  @Override
  public float width() {
    return surface.width();
  }

  @Override
  public float height() {
    return surface.height();
  }

  @Override
  public Scale scale() {
    return ctx.scale;
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
  public Region subImage(float sx, float sy, float swidth, float sheight) {
    return new ImageRegionGL(this, sx, sy, swidth, sheight);
  }

  @Override
  public int ensureTexture() {
    return surface.tex;
  }

  @Override
  public void clearTexture() {
    // this is a NOOP for surface images; those must retain their texture until they are garbage
    // collected or manually destroy()ed
  }

  void addQuad(GLShader shader, InternalTransform xform, float dx, float dy, float dw, float dh,
               float sl, float st, float sr, float sb) {
    shader.addQuad(xform, dx, dy, dw, dh, sl, 1-st, sr, 1-sb);
  }
}
