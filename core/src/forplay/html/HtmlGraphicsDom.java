/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.html;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;

import forplay.core.CanvasLayer;
import forplay.core.GroupLayer;
import forplay.core.Image;
import forplay.core.ImageLayer;
import forplay.core.SurfaceLayer;

class HtmlGraphicsDom extends HtmlGraphics {

  private final HtmlGroupLayerDom rootLayer;

  public HtmlGraphicsDom() {
    Element div = Document.get().createDivElement();
    div.getStyle().setOverflow(Overflow.HIDDEN);
    rootElement.appendChild(div);

    rootLayer = new HtmlGroupLayerDom(div);

    setSize(HtmlPlatform.DEFAULT_WIDTH, HtmlPlatform.DEFAULT_HEIGHT);
  }

  @Override
  public CanvasLayer createCanvasLayer(int width, int height) {
    return new HtmlCanvasLayerDom(width, height);
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new HtmlGroupLayerDom();
  }

  @Override
  public ImageLayer createImageLayer() {
    return new HtmlImageLayerDom();
  }

  @Override
  public ImageLayer createImageLayer(Image img) {
    return new HtmlImageLayerDom(img);
  }

  @Override
  public SurfaceLayer createSurfaceLayer(int width, int height) {
    return new HtmlSurfaceLayerDom(width, height);
  }

  @Override
  public int height() {
    return rootLayer.element().getOffsetHeight();
  }

  @Override
  public HtmlGroupLayerDom rootLayer() {
    return rootLayer;
  }

  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height);

    rootLayer.element().getStyle().setWidth(width, Unit.PX);
    rootLayer.element().getStyle().setHeight(height, Unit.PX);
  }

  @Override
  public int width() {
    return rootLayer.element().getOffsetWidth();
  }

  void updateLayers() {
    rootLayer.update();
  }

  @Override
  Element getRootElement() {
    return rootLayer.element();
  }
}
