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
package playn.html;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

import playn.core.Game;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.SurfaceLayer;
import playn.core.gl.Scale;

class HtmlGraphicsCanvas extends HtmlGraphics {

  private final Scale scale;
  private final HtmlGroupLayerCanvas rootLayer;
  private final CanvasElement canvas;
  private final Context2d ctx;

  public HtmlGraphicsCanvas(HtmlPlatform.Config config) {
    scale = new Scale(config.scaleFactor);
    rootLayer = new HtmlGroupLayerCanvas();
    canvas = Document.get().createCanvasElement();
    canvas.setWidth(rootElement.getOffsetWidth());
    canvas.setHeight(rootElement.getOffsetHeight());
    rootElement.appendChild(canvas);
    ctx = canvas.getContext2d();
    ctx.scale(config.scaleFactor, config.scaleFactor);
  }

  @Override
  public void setSize(int width, int height) {
    int swidth = scale.scaledCeil(width), sheight = scale.scaledCeil(height);
    super.setSize(swidth, sheight);
    canvas.setWidth(swidth);
    canvas.setHeight(sheight);
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new HtmlGroupLayerCanvas();
  }

  @Override
  public GroupLayer.Clipped createGroupLayer(float width, float height) {
    return new HtmlGroupLayerCanvas.Clipped(width, height);
  }

  @Override
  public ImageLayer createImageLayer() {
    return new HtmlImageLayerCanvas();
  }

  @Override
  public ImageLayer createImageLayer(Image img) {
    return new HtmlImageLayerCanvas(img);
  }

  @Override
  public SurfaceLayer createSurfaceLayer(float width, float height) {
    return new HtmlSurfaceLayerCanvas(width, height);
  }

  @Override
  public ImmediateLayer.Clipped createImmediateLayer(
      int width, int height, ImmediateLayer.Renderer renderer) {
    return new HtmlImmediateLayerCanvas.Clipped(ctx, width, height, renderer);
  }

  @Override
  public ImmediateLayer createImmediateLayer(ImmediateLayer.Renderer renderer) {
    return new HtmlImmediateLayerCanvas(ctx, renderer);
  }

  @Override
  public HtmlGroupLayerCanvas rootLayer() {
    return rootLayer;
  }

  @Override
  public int width() {
    return (int)scale.invScaled(canvas.getOffsetWidth());
  }

  @Override
  public int height() {
    return (int)scale.invScaled(canvas.getOffsetHeight());
  }

  @Override
  Scale scale() {
    return scale;
  }

  @Override
  Element rootElement() {
    return canvas;
  }

  @Override
  void paint(Game game, float paintAlpha) {
    ctx.clearRect(0, 0, width(), height());
    game.paint(paintAlpha);
    rootLayer.paint(ctx, 1);
    ctx.setGlobalAlpha(1);
  }
}
