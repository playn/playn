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

import java.util.Map;
import java.util.HashMap;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.CanvasPattern;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;

import playn.core.Asserts;
import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Gradient;
import playn.core.Graphics;
import playn.core.Image;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GL20;

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
      onload.@playn.html.EventHandler::handleEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);
    };
    img.onerror = function(e) {
      onerror.@playn.html.EventHandler::handleEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);
    };
  }-*/;

  protected final CanvasElement dummyCanvas;
  protected Element rootElement;
  private final Context2d dummyCtx;

  private final Element measureElement;
  private final Map<Font,HtmlFontMetrics> fontMetrics = new HashMap<Font,HtmlFontMetrics>();

  private static final String HEIGHT_TEXT =
    "THEQUICKBROWNFOXJUMPEDOVERTHELAZYDOGthequickbrownfoxjumpedoverthelazydog";
  private static final String EMWIDTH_TEXT = "m";

  protected HtmlGraphics() {
    Document doc = Document.get();

    dummyCanvas = doc.createCanvasElement();
    dummyCtx = dummyCanvas.getContext2d();

    rootElement = doc.getElementById("playn-root");
    if (rootElement == null) {
      rootElement = doc.getBody();
    } else {
      // clear the contents of the "playn-root" element, if present
      rootElement.setInnerHTML("");
    }

    // create a hidden element used to measure font heights
    measureElement = doc.createDivElement();
    measureElement.getStyle().setVisibility(Style.Visibility.HIDDEN);
    measureElement.getStyle().setPosition(Style.Position.ABSOLUTE);
    measureElement.getStyle().setTop(-500, Unit.PX);
    measureElement.getStyle().setOverflow(Style.Overflow.VISIBLE);
    rootElement.appendChild(measureElement);
  }

  @Override
  public CanvasImage createImage(int w, int h) {
    return new HtmlCanvasImage(new HtmlCanvas(w, h));
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

  @Override @Deprecated
  public Path createPath() {
    return new HtmlPath();
  }

  @Override @Deprecated
  public Pattern createPattern(Image image) {
    return image.toPattern();
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors,
      float[] positions) {
    Asserts.checkArgument(colors.length == positions.length);

    CanvasGradient gradient = dummyCtx.createRadialGradient(x, y, 0, x, y, r);
    for (int i = 0; i < colors.length; ++i) {
      gradient.addColorStop(positions[i], cssColorString(colors[i]));
    }
    return new HtmlGradient(gradient);
  }

  @Override
  public Font createFont(String name, Font.Style style, float size) {
    return new HtmlFont(name, style, size);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    return new HtmlTextLayout(dummyCtx, text, format);
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

  @Override
  public float scaleFactor() {
    return 1;
  }

  @Override
  public GL20 gl20() {
    throw new UnsupportedOperationException();
  }

  HtmlFontMetrics getFontMetrics(Font font) {
    HtmlFontMetrics metrics = fontMetrics.get(font);
    if (metrics == null) {
      // TODO: when Context2d.measureText some day returns a height, nix this hackery
      measureElement.getStyle().setFontSize(font.size(), Unit.PX);
      measureElement.getStyle().setFontWeight(Style.FontWeight.NORMAL);
      measureElement.getStyle().setFontStyle(Style.FontStyle.NORMAL);
      measureElement.setInnerText(HEIGHT_TEXT);
      switch (font.style()) {
      case BOLD:
        measureElement.getStyle().setFontWeight(Style.FontWeight.BOLD);
        break;
      case ITALIC:
        measureElement.getStyle().setFontStyle(Style.FontStyle.ITALIC);
        break;
      }
      float height = measureElement.getOffsetHeight();
      measureElement.setInnerText(EMWIDTH_TEXT);
      float emwidth = measureElement.getOffsetWidth();
      metrics = new HtmlFontMetrics(height, emwidth);
      fontMetrics.put(font, metrics);
    }
    return metrics;
  }

  abstract Element rootElement();

  abstract void preparePaint();

  abstract void paintLayers();
}
