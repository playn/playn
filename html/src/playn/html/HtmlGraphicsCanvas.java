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

import playn.core.CanvasLayer;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.SurfaceLayer;

class HtmlGraphicsCanvas extends HtmlGraphics {

  private final HtmlGroupLayerCanvas rootLayer;
  private final CanvasElement canvas;
  private final Context2d ctx;

  public HtmlGraphicsCanvas() {
    canvas = Document.get().createCanvasElement();
    rootElement.appendChild(canvas);
    ctx = canvas.getContext2d();
    rootLayer = new HtmlGroupLayerCanvas();
  }

  @Override @Deprecated
  public CanvasLayer createCanvasLayer(int width, int height) {
    return new HtmlCanvasLayerCanvas(width, height);
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new HtmlGroupLayerCanvas();
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
  public SurfaceLayer createSurfaceLayer(int width, int height) {
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
  public void setSize(int width, int height) {
    super.setSize(width, height);
    canvas.setWidth(width);
    canvas.setHeight(height);
  }

  @Override
  public int width() {
    return canvas.getOffsetWidth();
  }

  @Override
  public int height() {
    return canvas.getOffsetHeight();
  }

  @Override
  Element rootElement() {
    return canvas;
  }

  @Override
  void preparePaint() {
    ctx.clearRect(0, 0, width(), height());
  }

  @Override
  void paintLayers() {
    rootLayer.paint(ctx, 1);
    ctx.setGlobalAlpha(1);
  }
}
