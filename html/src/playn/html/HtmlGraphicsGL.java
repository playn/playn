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

import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

import playn.core.CanvasLayer;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.SurfaceLayer;
import playn.core.gl.CanvasLayerGL;
import playn.core.gl.GL20;
import playn.core.gl.GroupLayerGL;
import playn.core.gl.ImageLayerGL;
import playn.core.gl.ImmediateLayerGL;
import playn.core.gl.SurfaceLayerGL;

class HtmlGraphicsGL extends HtmlGraphics {

  private final CanvasElement canvas;
  private final HtmlGLContext ctx;
  private final HtmlGL20 gl20;
  private final GroupLayerGL rootLayer;

  HtmlGraphicsGL() throws RuntimeException {
    canvas = Document.get().createCanvasElement();
    rootElement.appendChild(canvas);
    try {
      ctx = new HtmlGLContext(canvas);
      gl20 = new HtmlGL20(ctx.gl);
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
  public ImmediateLayer.Clipped createImmediateLayer(
      int width, int height, ImmediateLayer.Renderer renderer) {
    return new ImmediateLayerGL.Clipped(ctx, width, height, renderer);
  }

  @Override
  public ImmediateLayer createImmediateLayer(ImmediateLayer.Renderer renderer) {
    return new ImmediateLayerGL(ctx, renderer);
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
    ctx.setSize(width, height);
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
  public float scaleFactor() {
    return ctx.scaleFactor;
  }

  @Override
  public GL20 gl20() {
    return gl20;
  }

  @Override
  void preparePaint() {
    ctx.processPending();
    ctx.preparePaint();
  }

  @Override
  void paintLayers() {
    ctx.paint(rootLayer);
  }

  @Override
  Element rootElement() {
    return canvas;
  }
}
