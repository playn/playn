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
package playn.core.gl;

import playn.core.CanvasLayer;
import playn.core.Graphics;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.SurfaceLayer;

/**
 * Handles the common implementation of {@link Graphics} for GL backends.
 */
public abstract class GraphicsGL implements Graphics {

  /**
   * Adjusts the path of the supplied image based on our HiDPI scale mode. If we have no scale, the
   * image path is unadjusted. If we have a scale, the scale is tacked onto the image path (before
   * the extension). The scale factor will be converted to an integer per the following examples:
   * <ul>
   * <li> Scale factor 2: {@code foo.png} becomes {@code foo@2x.png}</li>
   * <li> Scale factor 4: {@code foo.png} becomes {@code foo@4x.png}</li>
   * <li> Scale factor 1.5: {@code foo.png} becomes {@code foo@15x.png}</li>
   * <li> Scale factor 1.25: {@code foo.png} becomes {@code foo@13x.png}</li>
   * </ul>
   */
  public String adjustImagePath(String path) {
    int scaleFactor = (int)(ctx().scaleFactor * 10);
    if (scaleFactor % 10 == 0)
      scaleFactor /= 10;
    if (scaleFactor == 1) {
      return path;
    } else {
      int didx = path.lastIndexOf(".");
      if (didx == -1) {
        return path; // no extension!?
      } else {
        return path.substring(0, didx) + "@" + scaleFactor + "x" + path.substring(didx);
      }
    }
  }

  @Override
  public void setSize(int width, int height) {
    ctx().setSize(width, height);
  }

  @Override
  public int width() {
    return ctx().viewWidth;
  }

  @Override
  public int height() {
    return ctx().viewHeight;
  }

  @Override @Deprecated
  public CanvasLayer createCanvasLayer(int width, int height) {
    return new CanvasLayerGL(ctx(), createImage(width, height));
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new GroupLayerGL(ctx());
  }

  @Override
  public ImageLayer createImageLayer() {
    return new ImageLayerGL(ctx());
  }

  @Override
  public ImageLayer createImageLayer(Image image) {
    return new ImageLayerGL(ctx(), image);
  }

  @Override
  public SurfaceLayer createSurfaceLayer(int width, int height) {
    return new SurfaceLayerGL(ctx(), createSurface(width, height));
  }

  @Override
  public ImmediateLayer.Clipped createImmediateLayer(
      int width, int height, ImmediateLayer.Renderer renderer) {
    return new ImmediateLayerGL.Clipped(ctx(), width, height, renderer);
  }

  @Override
  public ImmediateLayer createImmediateLayer(ImmediateLayer.Renderer renderer) {
    return new ImmediateLayerGL(ctx(), renderer);
  }

  protected SurfaceGL createSurface(int width, int height) {
    return new SurfaceGL(ctx(), width, height);
  }

  protected abstract GLContext ctx();
}
