/**
 * Copyright 2012 The PlayN Authors
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
package playn.tests.core;

import java.util.ArrayList;

import pythagoras.f.Vector;

import playn.core.CanvasImage;
import playn.core.Events;
import playn.core.Font;
import playn.core.ImageLayer;
import playn.core.Mouse;
import playn.core.Mouse.WheelEvent;
import playn.core.Pointer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.Mouse.ButtonEvent;
import playn.core.Mouse.MotionEvent;
import playn.core.Pointer.Event;
import static playn.core.PlayN.*;

class PointerMouseTouchTest extends Test {
  private TextFormat baseFormat = new TextFormat().
      withFont(graphics().createFont("Times New Roman", Font.Style.PLAIN, 20));
  private TextFormat logFormat = new TextFormat().
      withFont(graphics().createFont("Times New Roman", Font.Style.PLAIN, 12));

  private TextLogger logger;

  private Toggle preventDefault;

  @Override
  public String getName() {
    return "PointerMouseTouchTest";
  }

  @Override
  public String getDescription() {
    return "Tests the Pointer, Mouse, and Touch interfaces.";
  }

  @Override
  public void init() {
    float y = 20;

    preventDefault = new Toggle("Prevent Default");
    graphics().rootLayer().addAt(preventDefault.layer, 20, y);
    y += preventDefault.layer.image().height() + 5;

    final ImageLayer mouse = createLabel("Mouse", 0xff000000, 0xffff8080, 20, y += 25);
    y += mouse.image().height() + 5;

    final ImageLayer pointer = createLabel("Pointer", 0xff000000, 0xff80ff80, 20, y += 50);
    y += pointer.image().height() + 5;

    createLabel("Event Log", 0, 325, 20);

    // setup the logger and its layer
    CanvasImage logImage = graphics().createImage(375, 400);
    ImageLayer logLayer = graphics().createImageLayer(logImage);
    logLayer.setTranslation(325, 40);
    graphics().rootLayer().add(logLayer);
    logger = new TextLogger(logImage, logFormat);


    // add mouse layer listener
    mouse.addListener(new Mouse.LayerListener() {
      @Override
      public void onMouseDown(ButtonEvent event) {
        _lstart = mouse.transform().translation();
        _pstart = new Vector(event.x(), event.y());
        mouse.setAlpha(0.5f);
        modify(event);
        logger.log(describe(event, "mouse down"));
      }
      @Override
      public void onMouseDrag(MotionEvent event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        mouse.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        modify(event);
        logger.log(describe(event, "mouse drag"));
      }
      @Override
      public void onMouseUp(ButtonEvent event) {
        mouse.setAlpha(1.0f);
        modify(event);
        logger.log(describe(event, "mouse up"));
      }
      @Override public void onMouseMove (MotionEvent event) {
        modify(event);
        logger.log(describe(event, "mouse move"));
      }
      @Override public void onMouseOver (MotionEvent event) {
        modify(event);
        logger.log(describe(event, "mouse over"));
      }
      @Override public void onMouseOut (MotionEvent event) {
        modify(event);
        logger.log(describe(event, "mouse out"));
      }
      @Override public void onMouseWheelScroll (WheelEvent event) {
        modify(event);
        logger.log(describe(event, "mouse wheel"));
      }

      protected Vector _lstart, _pstart;
    });


    // add pointer layer listener
    pointer.addListener(new Pointer.Listener() {
      @Override
      public void onPointerStart(Event event) {
        _lstart = pointer.transform().translation();
        _pstart = new Vector(event.x(), event.y());
        pointer.setAlpha(0.5f);
        modify(event);
        logger.log(describe(event, "pointer start"));
      }
      @Override
      public void onPointerDrag(Event event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        pointer.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        modify(event);
        logger.log(describe(event, "pointer drag"));
      }
      @Override
      public void onPointerEnd(Event event) {
        pointer.setAlpha(1.0f);
        modify(event);
        logger.log(describe(event, "pointer end"));
      }
      protected Vector _lstart, _pstart;
    });
  }

  @Override public boolean usesPositionalInputs () {
    return true;
  }

  protected ImageLayer createLabel(String text, int bg, float x, float y) {
    return createLabel(text, 0xFF6699CC, bg, x, y);
  }

  protected ImageLayer createLabel(String text, int fg, int bg, float x, float y) {
    TextLayout layout = graphics().layoutText(text, baseFormat);
    float twidth = layout.width();
    float theight = layout.height();
    CanvasImage image = graphics().createImage(twidth, theight);
    if (bg != 0) {
      image.canvas().setFillColor(bg);
      image.canvas().fillRect(0, 0, twidth, theight);
    }
    image.canvas().setFillColor(fg);
    image.canvas().fillText(layout, 0, 0);
    ImageLayer imageLayer = graphics().createImageLayer(image);
    imageLayer.setTranslation(x, y);
    graphics().rootLayer().add(imageLayer);
    return imageLayer;
  }

  protected void modify(Events.Position event) {
    event.flags().setPreventDefault(preventDefault.value);
  }

  protected String describe(Events.Position event, String handler) {
    String pd = event.flags().getPreventDefault() ? "pd " : "";
    String msg = pd + handler + " (" + event.x() + "," + event.y() + ")";
    if (event instanceof Pointer.Event) {
      msg += " isTouch(" + ((Pointer.Event)event).isTouch() + ")";
    }
    if (event instanceof Mouse.ButtonEvent) {
      msg += " button(" + ((Mouse.ButtonEvent)event).button() + ")";
    }
    if (event instanceof Mouse.MotionEvent) {
      Mouse.MotionEvent me = (Mouse.MotionEvent)event;
      msg += " d(" + me.dx() + "," + me.dy() + ")";
    }
    return msg;
  }

  protected class TextLogger {
    private CanvasImage image;
    private TextFormat format;
    private ArrayList<String> entries = new ArrayList<String>(0);

    public TextLogger(CanvasImage image, TextFormat format) {
      this.image = image;
      this.format = format;
    }

    public void log(String text) {
      entries.add(text);
      StringBuilder sb = new StringBuilder();
      for (int i = entries.size() - 1; i >=0; i--) {
        sb.append(entries.get(i));
        sb.append('\n');
      }

      TextLayout layout = graphics().layoutText(sb.toString(), format);
      if (layout.height() > image.height() && !entries.isEmpty())
        entries.remove(0);

      image.canvas().clear();
      image.canvas().setFillColor(0xFF6699CC);
      image.canvas().fillText(layout, 0, 0);
    }
  }

  protected class Toggle {
    final ImageLayer layer = graphics().createImageLayer();
    final String prefix;
    boolean value;

    Toggle(String name) {
      this.prefix = name + ": ";
      set(false);
      layer.addListener(new Pointer.Adapter() {
        @Override
        public void onPointerStart(Event event) {
          set(!value);
        }
      });
    }
    void set(boolean value) {
      this.value = value;
      layer.setImage(TestsGame.makeButtonImage(prefix + (value ? "On" : "Off")));
    }
  }
}
