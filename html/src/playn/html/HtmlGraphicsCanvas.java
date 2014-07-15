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
import com.google.gwt.dom.client.Style;

import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.InternalTransform;
import playn.core.StockInternalTransform;
import playn.core.SurfaceImage;
import playn.core.canvas.GroupLayerCanvas;
import playn.core.canvas.ImageLayerCanvas;
import playn.core.canvas.ImmediateLayerCanvas;
import playn.core.gl.Scale;

class HtmlGraphicsCanvas extends HtmlGraphics {

  private final Scale scale;
  private final GroupLayerCanvas rootLayer;
  private final CanvasElement elem;
  private final Context2d ctx;
  private final AbstractHtmlCanvas canvas;
  private int width;
  private int height;

  public HtmlGraphicsCanvas(HtmlPlatform.Config config) {
    super(config);
    scale = new Scale(config.scaleFactor);
    rootLayer = new GroupLayerCanvas(createXform());

    int width = rootElement.getOffsetWidth();
    int height = rootElement.getOffsetHeight();
    if (width == 0 || height == 0) {
      // If the container doesn't have an offsetWidth, it has or is a child of a node
      // that has display:none. We need to temporarily move it out to a visible
      // state to determine the size.
      Element rootClone = (Element) rootElement.cloneNode(false);
      Style style = rootClone.getStyle();
      style.setPosition(Style.Position.ABSOLUTE);
      style.setDisplay(Style.Display.BLOCK);
      style.setTop(-9999, Style.Unit.PX);
      Document.get().getBody().appendChild(rootClone);
      width = rootElement.getOffsetWidth();
      height = rootElement.getOffsetHeight();
      rootClone.removeFromParent();
    }
    this.width = width;
    this.height = height;

    elem = Document.get().createCanvasElement();
    elem.setWidth(width);
    elem.setHeight(height);
    rootElement.appendChild(elem);

    ctx = elem.getContext2d();
    ctx.scale(config.scaleFactor, config.scaleFactor);
    canvas = new AbstractHtmlCanvas(ctx, 0, 0) {
      @Override public float width() {
        return HtmlGraphicsCanvas.this.width();
      }
      @Override public float height() {
        return HtmlGraphicsCanvas.this.height();
      }
    };
  }

  @Override
  public int width() {
    return scale.invScaledFloor(width);
  }

  @Override
  public int height() {
    return scale.invScaledFloor(height);
  }

  @Override
  public GroupLayerCanvas rootLayer() {
    return rootLayer;
  }

  @Override
  public void setSize(int width, int height) {
    int swidth = scale.scaledCeil(width), sheight = scale.scaledCeil(height);
    super.setSize(swidth, sheight);
    elem.setWidth(swidth);
    elem.setHeight(sheight);
    this.width = swidth;
    this.height = sheight;
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

  @Override
  public SurfaceImage createSurface(float width, float height) {
    return new HtmlSurfaceImageCanvas(ctx(), scale, HtmlCanvas.create(scale, width, height));
  }

  @Override
  Scale scale() {
    return scale;
  }

  @Override
  Element rootElement() {
    return elem;
  }

  @Override
  void paint() {
    canvas.clear();
    rootLayer.paint(canvas, 1);
    canvas.setAlpha(1);
  }

  protected InternalTransform createXform() {
    return HtmlPlatform.hasTypedArraySupport ?
      new HtmlInternalTransform() : new StockInternalTransform();
  }
}
