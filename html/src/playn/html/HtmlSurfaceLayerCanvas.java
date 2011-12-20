/**
 * Copyright 2010 The PlayN Authors
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
package playn.html;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;

import playn.core.Asserts;
import playn.core.CanvasSurface;
import playn.core.Surface;
import playn.core.SurfaceLayer;

class HtmlSurfaceLayerCanvas extends HtmlLayerCanvas implements SurfaceLayer {

  private CanvasElement canvas;
  private CanvasSurface surface;

  HtmlSurfaceLayerCanvas(int width, int height) {
    canvas = Document.get().createCanvasElement();
    canvas.setWidth(width);
    canvas.setHeight(height);
    surface = new CanvasSurface(new HtmlCanvas(canvas, width, height));
  }

  @Override
  public void destroy() {
    super.destroy();
    canvas = null;
    surface = null;
  }

  @Override
  public Surface surface() {
    return surface;
  }

  @Override
  public float width() {
    Asserts.checkNotNull(surface, "Surface must not be null");
    return surface.width();
  }

  @Override
  public float height() {
    Asserts.checkNotNull(surface, "Surface must not be null");
    return surface.height();
  }

  @Override
  public float scaledWidth() {
    return transform().scaleX() * width();
  }

  @Override
  public float scaledHeight() {
    return transform().scaleY() * height();
  }

  @Override
  void paint(Context2d ctx, float parentAlpha) {
    ctx.save();
    transform(ctx);

    ctx.setGlobalAlpha(parentAlpha * alpha);
    ctx.drawImage(canvas, 0, 0);

    ctx.restore();
  }
}
