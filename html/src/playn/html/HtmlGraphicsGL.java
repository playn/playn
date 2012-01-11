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

import playn.core.CanvasLayer;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.SurfaceLayer;
import playn.core.gl.CanvasLayerGL;
import playn.core.gl.GroupLayerGL;
import playn.core.gl.ImageLayerGL;
import playn.core.gl.SurfaceLayerGL;

import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

class HtmlGraphicsGL extends HtmlGraphics {

  private final CanvasElement canvas;
  private final HtmlGLContext ctx;
  private final GroupLayerGL rootLayer;

  HtmlGraphicsGL() throws RuntimeException {
    canvas = Document.get().createCanvasElement();
    rootElement.appendChild(canvas);
    try {
      ctx = new HtmlGLContext(canvas);
      rootLayer = new GroupLayerGL(ctx);
    } catch (RuntimeException re) {
      // Give up. HtmlPlatform will catch the exception and fall back to dom/canvas.
      rootElement.removeChild(canvas);
      throw re;
    }
  }

  @Override @Deprecated
  public CanvasLayer createCanvasLayer(int width, int height) {
    return new CanvasLayerGL(ctx, createImage(width, height));
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new GroupLayerGL(ctx);
  }

  @Override
  public ImageLayer createImageLayer() {
    return new ImageLayerGL(ctx);
  }

  @Override
  public ImageLayer createImageLayer(Image img) {
    return new ImageLayerGL(ctx, img);
  }

  @Override
  public SurfaceLayer createSurfaceLayer(int width, int height) {
    return new SurfaceLayerGL(ctx, width, height);
  }

  @Override
  public GroupLayer rootLayer() {
    return rootLayer;
  }

  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height);
    canvas.setWidth(width);
    canvas.setHeight(height);
    ctx.bindFramebuffer(null, width, height, true);
  }

  @Override
  public int width() {
    return canvas.getWidth();
  }

  @Override
  public int height() {
    return canvas.getHeight();
  }

  @Override
  void paintLayers() {
    ctx.processPending();
    ctx.paint(rootLayer);
  }

  @Override
  Element rootElement() {
    return canvas;
  }
}
