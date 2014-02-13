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
import com.google.gwt.webgl.client.WebGLContextAttributes;
import com.google.gwt.webgl.client.WebGLRenderingContext;

import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.SurfaceImage;
import playn.core.gl.GL20;
import playn.core.gl.GroupLayerGL;
import playn.core.gl.ImageLayerGL;
import playn.core.gl.ImmediateLayerGL;
import playn.core.gl.Scale;
import playn.core.gl.SurfaceGL;
import playn.core.gl.SurfaceImageGL;

class HtmlGraphicsGL extends HtmlGraphics {

  private final CanvasElement canvas;
  private final HtmlGLContext ctx;
  private final GroupLayerGL rootLayer;

  HtmlGraphicsGL(HtmlPlatform platform, HtmlPlatform.Config config) throws RuntimeException {
    super(config);
    canvas = Document.get().createCanvasElement();
    canvas.setWidth(rootElement.getOffsetWidth());
    canvas.setHeight(rootElement.getOffsetHeight());
    rootElement.appendChild(canvas);

    try {
      WebGLContextAttributes attrs = WebGLContextAttributes.create();
      attrs.setAlpha(config.transparentCanvas);
      attrs.setAntialias(config.antiAliasing);

      // if this returns null, the browser doesn't support WebGL on this machine
      WebGLRenderingContext gl = WebGLRenderingContext.getContext(canvas, attrs);
      if (gl == null)
        throw new RuntimeException("Unable to create GL context");

      // Some systems seem to have a problem where they return a non-null context, but it's in an
      // error state initially. We give up and fall back to Canvas in this case, because nothing
      // seems to work properly.
      int error = gl.getError();
      if (error != WebGLRenderingContext.NO_ERROR)
        throw new RuntimeException("GL context started with errors [err=" + error + "]");

      ctx = new HtmlGLContext(platform, config.scaleFactor, gl, canvas);
      rootLayer = new GroupLayerGL(ctx);

    } catch (RuntimeException re) {
      // Give up. HtmlPlatform will catch the exception and fall back to dom/canvas.
      rootElement.removeChild(canvas);
      throw re;
    }
  }

  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height);
    canvas.setWidth(width);
    canvas.setHeight(height);
    ctx.setSize(width, height);
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new GroupLayerGL(ctx);
  }

  @Override
  public GroupLayer.Clipped createGroupLayer(float width, float height) {
    return new GroupLayerGL.Clipped(ctx, width, height);
  }

  @Override
  public ImageLayer createImageLayer() {
    return new ImageLayerGL(ctx);
  }

  @Override
  public ImageLayer createImageLayer(Image img) {
    return new ImageLayerGL(ctx).setImage(img);
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
  public SurfaceImage createSurface(float width, float height) {
    return new SurfaceImageGL(ctx(), new SurfaceGL(ctx(), width, height));
  }

  @Override
  public GroupLayer rootLayer() {
    return rootLayer;
  }

  @Override
  public int width() {
    return ctx.viewWidth;
  }

  @Override
  public int height() {
    return ctx.viewHeight;
  }

  @Override
  public GL20 gl20() {
    return ctx.gl;
  }

  @Override
  public HtmlGLContext ctx() {
    return ctx;
  }

  @Override
  Scale scale() {
    return ctx.scale;
  }

  @Override
  void paint() {
    ctx.paint(rootLayer);
  }

  @Override
  Element rootElement() {
    return canvas;
  }
}
