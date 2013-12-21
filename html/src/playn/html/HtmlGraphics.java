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
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;

import pythagoras.f.Point;

import playn.core.Asserts;
import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Gradient;
import playn.core.Graphics;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.TextWrap;
import playn.core.gl.GL20;
import playn.core.gl.Scale;

public abstract class HtmlGraphics implements Graphics {

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
  private final Point mousePoint = new Point();

  private final Element measureElement;
  private final Map<Font,HtmlFontMetrics> fontMetrics = new HashMap<Font,HtmlFontMetrics>();

  private static final String HEIGHT_TEXT =
    "THEQUICKBROWNFOXJUMPEDOVERTHELAZYDOGthequickbrownfoxjumpedoverthelazydog";
  private static final String EMWIDTH_TEXT = "m";

  // Temporary hack to fix mouse coordinates for scaled fullscreen mode.
  static float experimentalScale = 1;

  /**
   * Sizes or resizes the root element that contains the PlayN view.
   * @param width the new width, in pixels, of the view.
   * @param height the new height, in pixels, of the view.
   */
  public void setSize(int width, int height) {
    rootElement.getStyle().setWidth(width, Unit.PX);
    rootElement.getStyle().setHeight(height, Unit.PX);
  }

  /**
   * Registers metrics for the specified font in the specified style and size. This overrides the
   * default font metrics calculation (which is hacky and inaccurate). If you want to ensure
   * somewhat consistent font layout across browsers, you should register font metrics for every
   * combination of font, style and size that you use in your app.
   *
   * @param lineHeight the height of a line of text in the specified font (in pixels).
   */
  public void registerFontMetrics(String name, Font.Style style, float size, float lineHeight) {
    HtmlFont font = new HtmlFont(this, name, style, size);
    HtmlFontMetrics metrics = getFontMetrics(font); // get emwidth via default measurement
    fontMetrics.put(font, new HtmlFontMetrics(font, lineHeight, metrics.emwidth));
  }

  @Override
  public CanvasImage createImage(float width, float height) {
    return new HtmlCanvasImage(ctx(), scale(), HtmlCanvas.create(scale(), width, height));
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
    return new HtmlFont(this, name, style, size);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    // TEMP: handle multiline in TextFormat until that's removed
    if (format.shouldWrap() || text.indexOf('\n') != -1 ||  text.indexOf('\r') != -1)
      return new OldHtmlTextLayout(dummyCtx, text, format);
    else
      return HtmlTextLayout.layoutText(this, dummyCtx, text, format);
  }

  @Override
  public TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
    return HtmlTextLayout.layoutText(this, dummyCtx, text, format, wrap);
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
  public float scaleFactor() {
    return scale().factor;
  }

  @Override
  public GL20 gl20() {
    throw new UnsupportedOperationException();
  }

  @Override
  public HtmlGLContext ctx() {
    return null;
  }

  protected HtmlGraphics(HtmlPlatform.Config config) {
    Document doc = Document.get();

    dummyCanvas = doc.createCanvasElement();
    dummyCtx = dummyCanvas.getContext2d();

    rootElement = doc.getElementById("playn-root");
    if (rootElement == null) {
      rootElement = doc.createDivElement();
      rootElement.setAttribute("style", "width: 640px; height: 480px");
      doc.getBody().appendChild(rootElement);
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

    if (config.experimentalFullscreen) {
      Window.addResizeHandler(new ResizeHandler() {
        @Override
        public void onResize(ResizeEvent event) {
          if (fullScreenWidth() == event.getWidth() && fullScreenHeight() == event.getHeight()) {
            experimentalScale = Math.min((float) fullScreenWidth() / (float) width(),
                                         (float) fullScreenHeight() / (float) height());
            // less distance to the top
            int yOfs = (int) ((fullScreenHeight() - height() * experimentalScale) / 3.f);
            int xOfs = (int) ((fullScreenWidth() - width() * experimentalScale) / 2.f);
            rootElement().setAttribute(
              "style",
              "width:" + experimentalScale * width() + "px; " +
              "height:" + experimentalScale*height() + "px; " +
              "position:absolute; left:" + xOfs + "px; top:" + yOfs);
            // This is needed to work around a focus bug in Chrome :(
            Window.alert("Switching to fullscreen mode.");
            Document.get().getBody().addClassName("fullscreen");
          } else {
            experimentalScale = 1;
            rootElement().removeAttribute("style");
            Document.get().getBody().removeClassName("fullscreen");
          }
        }});
    }
  }

  abstract Scale scale();

  HtmlFontMetrics getFontMetrics(HtmlFont font) {
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
      default:
        break; // nada
      }
      float height = measureElement.getOffsetHeight();
      measureElement.setInnerText(EMWIDTH_TEXT);
      float emwidth = measureElement.getOffsetWidth();
      metrics = new HtmlFontMetrics(font, height, emwidth);
      fontMetrics.put(font, metrics);
    }
    return metrics;
  }

  Point transformMouse(float x, float y) {
    return mousePoint.set(x / scale().factor, y / scale().factor);
  }

  abstract Element rootElement();

  abstract void paint();

  private native int fullScreenWidth() /*-{
     return $wnd.screen.width;
  }-*/;

  private native int fullScreenHeight() /*-{
    return $wnd.screen.height;
  }-*/;}
