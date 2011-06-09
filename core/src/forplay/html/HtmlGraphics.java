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

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.CanvasPattern;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.Repetition;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;

import forplay.core.Asserts;
import forplay.core.CanvasImage;
import forplay.core.Gradient;
import forplay.core.Graphics;
import forplay.core.Image;
import forplay.core.Path;
import forplay.core.Pattern;

public abstract class HtmlGraphics implements Graphics {
  
  static CssColor cssColor(int color) {
    return CssColor.make(cssColorString(color));
  }

  static String cssColorString(int color) {
    double a = ((color >> 24) & 0xff) / 255.0;
    int r = (color >> 16) & 0xff;
    int g = (color >> 8) & 0xff;
    int b = (color >> 0) & 0xff;
    return "rgba(" + r + "," + g + "," + b + "," + a + ")";
  }

  private static native void setLoadHandlers(ImageElement img, EventHandler onload,
      EventHandler onerror) /*-{
    img.onload = function(e) {
      onload.@forplay.html.EventHandler::handleEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);
    };
    img.onerror = function(e) {
      onerror.@forplay.html.EventHandler::handleEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);
    };
  }-*/;

  protected CanvasElement dummyCanvas;
  protected Element rootElement;
  private final Context2d dummyCtx;

  protected HtmlGraphics() {
    Document doc = Document.get();

    dummyCanvas = doc.createCanvasElement();
    dummyCtx = dummyCanvas.getContext2d();

    rootElement = doc.getElementById("forplay-root");
    if (rootElement == null) {
      rootElement = doc.getBody();
    } else {
      // clear the contents of the "forplay-root" element, if present
      rootElement.setInnerHTML("");
    }
  }

  @Override
  public CanvasImage createImage(int w, int h) {
    HtmlCanvas surface = new HtmlCanvas(w, h);
    return new HtmlCanvasImage(surface);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1,
      int[] colors, float[] positions) {
    Asserts.checkArgument(colors.length == positions.length);

    CanvasGradient gradient = dummyCtx.createLinearGradient(x0, y0, x1, y1);
    for (int i = 0; i < colors.length; ++i) {
      gradient.addColorStop(positions[i], cssColorString(colors[i]));
    }
    return new HtmlGradient(gradient);
  }

  @Override
  public Path createPath() {
    return new HtmlPath();
  }

  @Override
  public Pattern createPattern(Image image) {
    Asserts.checkArgument(image instanceof HtmlImage);
    HtmlImage htmlImage = (HtmlImage) image;
    ImageElement elem = htmlImage.img.cast();
    CanvasPattern pattern = dummyCtx.createPattern(elem, Repetition.REPEAT);
    return new HtmlPattern(htmlImage, pattern);
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors,
      float[] positions) {
    Asserts.checkArgument(colors.length == positions.length);

    CanvasGradient gradient = dummyCtx.createRadialGradient(x, y, r, x, y, r);
    for (int i = 0; i < colors.length; ++i) {
      gradient.addColorStop(positions[i], cssColorString(colors[i]));
    }
    return new HtmlGradient(gradient);
  }

  @Override
  public int screenHeight() {
    return Document.get().getDocumentElement().getClientHeight();
  }

  @Override
  public int screenWidth() {
    return Document.get().getDocumentElement().getClientWidth();
  }

  @Override
  public void setSize(int width, int height) {
    rootElement.getStyle().setWidth(width, Unit.PX);
    rootElement.getStyle().setHeight(height, Unit.PX);
  }

  abstract void updateLayers();

  abstract Element getRootElement();
}
