/**
 * Copyright 2013 The PlayN Authors
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
package playn.core.canvas;

import playn.core.Canvas;
import playn.core.Graphics;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.InternalTransform;
import playn.core.gl.Scale;

/**
 * Handles the common implementation of {@link Graphics} for canvas-based backends.
 */
public abstract class GraphicsCanvas implements Graphics {

  private final Scale scale;
  private final GroupLayerCanvas rootLayer;

  protected GraphicsCanvas(Scale scale) {
    this.scale = scale;
    this.rootLayer = new GroupLayerCanvas(createXform());
  }

  public Scale scale() {
    return scale;
  }

  public void paint(Canvas canvas) {
    canvas.clear();
    rootLayer.paint(canvas, 1);
  }

  @Override
  public GroupLayer rootLayer() {
    return rootLayer;
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new GroupLayerCanvas(createXform());
  }

  @Override
  public GroupLayer.Clipped createGroupLayer(float width, float height) {
    return new GroupLayerCanvas.Clipped(createXform(), width, height);
  }

  @Override
  public ImageLayer createImageLayer() {
    return new ImageLayerCanvas(createXform());
  }

  @Override
  public ImageLayer createImageLayer(Image img) {
    return createImageLayer().setImage(img);
  }

  @Override
  public ImmediateLayer.Clipped createImmediateLayer(
      int width, int height, ImmediateLayer.Renderer renderer) {
    return new ImmediateLayerCanvas.Clipped(createXform(), width, height, renderer);
  }

  @Override
  public ImmediateLayer createImmediateLayer(ImmediateLayer.Renderer renderer) {
    return new ImmediateLayerCanvas(createXform(), renderer);
  }

  protected abstract Canvas createCanvas(Scale scale, float width, float height);

  protected abstract InternalTransform createXform();
}
