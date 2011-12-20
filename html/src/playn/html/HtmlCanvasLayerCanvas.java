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
package playn.html;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;

import playn.core.Asserts;

import playn.core.Canvas;
import playn.core.CanvasLayer;

class HtmlCanvasLayerCanvas extends HtmlLayerCanvas implements CanvasLayer {

  private HtmlCanvas canvas;

  HtmlCanvasLayerCanvas(int width, int height) {
    CanvasElement canvas = Document.get().createCanvasElement();
    canvas.setWidth(width);
    canvas.setHeight(height);

    this.canvas = new HtmlCanvas(canvas, width, height);
  }

  @Override
  public void destroy() {
    super.destroy();
    canvas = null;
  }

  @Override
  public Canvas canvas() {
    return canvas;
  }

  @Override
  public float width() {
    Asserts.checkNotNull(canvas, "Canvas must not be null");
    return canvas.width();
  }

  @Override
  public float height() {
    Asserts.checkNotNull(canvas, "Canvas must not be null");
    return canvas.height();
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
    ctx.drawImage(canvas.canvas(), 0, 0);

    ctx.restore();
  }
}
