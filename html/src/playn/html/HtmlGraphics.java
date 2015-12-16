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
import com.google.gwt.webgl.client.WebGLContextAttributes;
import com.google.gwt.webgl.client.WebGLRenderingContext;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.Point;

import playn.core.*;

public class HtmlGraphics extends Graphics {

  private final CanvasElement dummyCanvas;
  private final Context2d dummyCtx;

  private final Element measureElement;
  private final Map<Font,HtmlFontMetrics> fontMetrics = new HashMap<Font,HtmlFontMetrics>();

  final Element rootElement;
  private final CanvasElement canvas;
  private final Point mousePoint = new Point();
  private final Dimension screenSize = new Dimension();
  private final float frameBufferPixelRatio;
  private final float mouseScale;

  private static final String HEIGHT_TEXT =
    "THEQUICKBROWNFOXJUMPEDOVERTHELAZYDOGthequickbrownfoxjumpedoverthelazydog_-+!.,[]0123456789";
  private static final String EMWIDTH_TEXT = "m";

  // Temporary hack to fix mouse coordinates for scaled fullscreen mode.
  static float experimentalScale = 1;

  public HtmlGraphics(Platform plat, HtmlPlatform.Config config) {
    super(plat, new HtmlGL20(), new Scale(config.scaleFactor));

    // note our frame buffer pixel ratio; this is probably either equal to config.scaleFactor (1 ==
    // 1 on a normal display with no funny business, or 2 == 2 on a HiDPI display with standard two
    // frame buffer pixels to one logical pixel, or 1 == 1 on a HiDPI display where we've chosen
    // to "scale down" the frame buffer to reduce strain on the GPU), but it could also be 2x
    // config.scaleFactor in cases where we're on a HiDPI display but we want to treat it like a
    // *really* high resolution normal display and just have super tiny pixels
    frameBufferPixelRatio = config.frameBufferPixelRatio;

    // our mouse scale is our configured scale divided by our device scale factor; when we're
    // displaying at our device scale factor, then it's one and everything is simple
    mouseScale = config.scaleFactor / frameBufferPixelRatio;

    Document doc = Document.get();
    dummyCanvas = doc.createCanvasElement();
    dummyCtx = dummyCanvas.getContext2d();

    Element root = doc.getElementById(config.rootId);
    if (root == null) {
      root = doc.createDivElement();
      root.setAttribute("style", "width: 640px; height: 480px");
      doc.getBody().appendChild(root);
    } else {
      // clear the contents of the root element, if present
      root.setInnerHTML("");
    }
    rootElement = root;

    // create a hidden element used to measure font heights
    measureElement = doc.createDivElement();
    measureElement.getStyle().setVisibility(Style.Visibility.HIDDEN);
    measureElement.getStyle().setPosition(Style.Position.ABSOLUTE);
    measureElement.getStyle().setTop(-500, Unit.PX);
    measureElement.getStyle().setOverflow(Style.Overflow.VISIBLE);
    measureElement.getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
    root.appendChild(measureElement);

    canvas = Document.get().createCanvasElement();
    root.appendChild(canvas);
    setSize(root.getOffsetWidth(), root.getOffsetHeight());

    WebGLContextAttributes attrs = WebGLContextAttributes.create();
    attrs.setAlpha(config.transparentCanvas);
    attrs.setAntialias(config.antiAliasing);

    // if this returns null, the browser doesn't support WebGL on this machine
    WebGLRenderingContext glc = WebGLRenderingContext.getContext(canvas, attrs);
    if (glc == null) throw new RuntimeException("Unable to create GL context");

    // pass our gl context into HtmlGL20
    ((HtmlGL20)gl).init(glc);

    if (config.experimentalFullscreen) {
      Window.addResizeHandler(new ResizeHandler() {
        @Override
        public void onResize(ResizeEvent event) {
          if (fullScreenWidth() == event.getWidth() && fullScreenHeight() == event.getHeight()) {
            float width = viewSize.width(), height = viewSize.height();
            experimentalScale = Math.min(fullScreenWidth() / width, fullScreenHeight() / height);
            // less distance to the top
            int yOfs = (int) ((fullScreenHeight() - height * experimentalScale) / 3.f);
            int xOfs = (int) ((fullScreenWidth() - width * experimentalScale) / 2.f);
            rootElement.setAttribute("style",
                                     "width:" + experimentalScale * width + "px; " +
                                     "height:" + experimentalScale*height + "px; " +
                                     "position:absolute; left:" + xOfs + "px; top:" + yOfs);
            // This is needed to work around a focus bug in Chrome :(
            Window.alert("Switching to fullscreen mode.");
            Document.get().getBody().addClassName("fullscreen");
          } else {
            experimentalScale = 1;
            rootElement.removeAttribute("style");
            Document.get().getBody().removeClassName("fullscreen");
          }
        }});
    }
  }

  /**
   * Sizes or resizes the root element that contains the game view. This is specified in pixels as
   * understood by page elements. If the page is actually being dispalyed on a HiDPI (Retina)
   * device, the actual framebuffer may be 2x (or larger) the specified size.
   */
  public void setSize (int width, int height) {
    rootElement.getStyle().setWidth(width, Unit.PX);
    rootElement.getStyle().setHeight(height, Unit.PX);
    // the frame buffer may be larger (or smaller) than the logical size, depending on whether
    // we're on a HiDPI display, or how the game has configured things (maybe they're scaling down
    // from native resolution to improve performance)
    Scale fbScale = new Scale(frameBufferPixelRatio);
    canvas.setWidth(fbScale.scaledCeil(width));
    canvas.setHeight(fbScale.scaledCeil(height));
    // set the canvas's CSS size to the logical size; the browser works in logical pixels
    canvas.getStyle().setWidth(width, Style.Unit.PX);
    canvas.getStyle().setHeight(height, Style.Unit.PX);
    viewportChanged(canvas.getWidth(), canvas.getHeight());
  }

  /**
   * Registers metrics for the specified font in the specified style and size. This overrides the
   * default font metrics calculation (which is hacky and inaccurate). If you want to ensure
   * somewhat consistent font layout across browsers, you should register font metrics for every
   * combination of font, style and size that you use in your app.
   *
   * @param lineHeight the height of a line of text in the specified font (in pixels).
   */
  public void registerFontMetrics(String name, Font font, float lineHeight) {
    HtmlFontMetrics metrics = getFontMetrics(font); // get emwidth via default measurement
    fontMetrics.put(font, new HtmlFontMetrics(font, lineHeight, metrics.emwidth));
  }

  @Override public IDimension screenSize () {
    // TODO: inverse scale?
    screenSize.width = Document.get().getDocumentElement().getClientWidth();
    screenSize.height = Document.get().getDocumentElement().getClientHeight();
    return screenSize;
  }

  @Override public TextLayout layoutText(String text, TextFormat format) {
    return HtmlTextLayout.layoutText(this, dummyCtx, text, format);
  }

  @Override public TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
    return HtmlTextLayout.layoutText(this, dummyCtx, text, format, wrap);
  }

  @Override protected Canvas createCanvasImpl(Scale scale, int pixelWidth, int pixelHeight) {
    CanvasElement elem = Document.get().createCanvasElement();
    elem.setWidth(pixelWidth);
    elem.setHeight(pixelHeight);
    return new HtmlCanvas(this, new HtmlImage(this, scale, elem, "<canvas>"));
  }

  static String cssColorString(int color) {
    double a = ((color >> 24) & 0xff) / 255.0;
    int r = (color >> 16) & 0xff;
    int g = (color >> 8) & 0xff;
    int b = (color >> 0) & 0xff;
    return "rgba(" + r + "," + g + "," + b + "," + a + ")";
  }

  void updateTexture(int tex, ImageElement img) {
    gl.glBindTexture(GL20.GL_TEXTURE_2D, tex);
    ((HtmlGL20)gl).glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, GL20.GL_RGBA,
                                GL20.GL_UNSIGNED_BYTE, img);
  }

  HtmlFontMetrics getFontMetrics(Font font) {
    HtmlFontMetrics metrics = fontMetrics.get(font);
    if (metrics == null) {
      // TODO: when Context2d.measureText some day returns a height, nix this hackery
      measureElement.getStyle().setFontSize(font.size, Unit.PX);
      measureElement.getStyle().setFontWeight(Style.FontWeight.NORMAL);
      measureElement.getStyle().setFontStyle(Style.FontStyle.NORMAL);
      measureElement.getStyle().setProperty("fontFamily", font.name);
      measureElement.setInnerText(HEIGHT_TEXT);
      switch (font.style) {
      case BOLD:
        measureElement.getStyle().setFontWeight(Style.FontWeight.BOLD);
        break;
      case ITALIC:
        measureElement.getStyle().setFontStyle(Style.FontStyle.ITALIC);
        break;
      case BOLD_ITALIC:
        measureElement.getStyle().setFontWeight(Style.FontWeight.BOLD);
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
    return mousePoint.set(x / mouseScale, y / mouseScale);
  }

  private native int fullScreenWidth () /*-{ return $wnd.screen.width; }-*/;
  private native int fullScreenHeight () /*-{ return $wnd.screen.height; }-*/;
}
