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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import pythagoras.f.Point;
import pythagoras.f.Vector;

import playn.core.CanvasImage;
import playn.core.Events;
import playn.core.Font;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.Layer;
import playn.core.Mouse;
import playn.core.TextWrap;
import playn.core.Mouse.WheelEvent;
import playn.core.Pointer;
import playn.core.Surface;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.Mouse.ButtonEvent;
import playn.core.Mouse.MotionEvent;
import playn.core.Pointer.Event;
import playn.core.Touch;
import playn.tests.core.TestsGame.NToggle;
import playn.tests.core.TestsGame.Toggle;
import static playn.core.PlayN.*;

class PointerMouseTouchTest extends Test {
  private TextFormat baseFormat = new TextFormat().
      withFont(graphics().createFont("Times New Roman", Font.Style.PLAIN, 20));
  private TextFormat logFormat = new TextFormat().
      withFont(graphics().createFont("Times New Roman", Font.Style.PLAIN, 12));

  private TextLogger logger;
  private TextMapper motionLabel;

  private Toggle preventDefault, capture;
  private NToggle<String> propagate;

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
    float y = 20, x = 20;

    preventDefault = new Toggle("Prevent Default");
    graphics().rootLayer().addAt(preventDefault.layer, x, y);
    x += preventDefault.layer.image().width() + 5;

    capture = new Toggle("Capture");
    graphics().rootLayer().addAt(capture.layer, x, y);
    x += capture.layer.image().width() + 5;

    propagate = new NToggle<String>("Propagation", "Off", "On", "On (stop)") {
      @Override
      public void set(int value) {
        super.set(value);
        platform().setPropagateEvents(value != 0);
      }
    };
    graphics().rootLayer().addAt(propagate.layer, x, y);
    y += propagate.layer.image().height() + 5;
    x = 20;

    float boxWidth = 300, boxHeight = 110;
    final Box mouse = new Box("Mouse", 0xffff8080, boxWidth, boxHeight);
    graphics().rootLayer().addAt(mouse.layer, x, y);
    y += mouse.layer.height() + 5;

    final Box pointer = new Box("Pointer", 0xff80ff80, boxWidth, boxHeight);
    graphics().rootLayer().addAt(pointer.layer, x, y);
    y += pointer.layer.height() + 5;

    final Box touch = new Box("Touch", 0xff8080ff, boxWidth, boxHeight);
    graphics().rootLayer().addAt(touch.layer, x, y);

    y = mouse.layer.ty();
    x += touch.layer.width() + 5;

    // setup the logger and its layer
    y += createLabel("Event Log", 0, x, y).height();
    logger = new TextLogger(375, 15, logFormat);
    logger.layer.setTranslation(x, y);
    graphics().rootLayer().add(logger.layer);
    y += logger.layer.height() + 5;

    // setup the motion logger and its layer
    y += createLabel("Motion Log", 0, x, y).height();
    motionLabel = new TextMapper(375, 6, logFormat);
    motionLabel.layer.setTranslation(x, y);
    graphics().rootLayer().add(motionLabel.layer);

    // add mouse layer listener
    mouse.label.addListener(new Mouse.LayerListener() {
      ImageLayer label = mouse.label;
      @Override
      public void onMouseDown(ButtonEvent event) {
        _lstart = label.transform().translation();
        _pstart = new Vector(event.x(), event.y());
        label.setAlpha(0.5f);
        modify(event);
        logger.log(describe(event, "mouse down"));
      }
      @Override
      public void onMouseDrag(MotionEvent event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        label.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        modify(event);
        motionLabel.set("mouse drag", describe(event, ""));
      }
      @Override
      public void onMouseUp(ButtonEvent event) {
        label.setAlpha(1.0f);
        modify(event);
        logger.log(describe(event, "mouse up"));
      }
      @Override public void onMouseMove (MotionEvent event) {
        modify(event);
        motionLabel.set("mouse move", describe(event, ""));
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

    // add mouse layer listener to parent
    mouse.layer.addListener(new Mouse.LayerListener() {
      @Override
      public void onMouseDown(ButtonEvent event) {
        logger.log(describe(event, "parent mouse down"));
      }
      @Override
      public void onMouseDrag(MotionEvent event) {
        motionLabel.set("parent mouse drag", describe(event, ""));
      }
      @Override
      public void onMouseUp(ButtonEvent event) {
        logger.log(describe(event, "parent mouse up"));
      }
      @Override public void onMouseMove (MotionEvent event) {
        motionLabel.set("parent mouse move", describe(event, ""));
      }
      @Override public void onMouseOver (MotionEvent event) {
        logger.log(describe(event, "parent mouse over"));
      }
      @Override public void onMouseOut (MotionEvent event) {
        logger.log(describe(event, "parent mouse out"));
      }
      @Override public void onMouseWheelScroll (WheelEvent event) {
        logger.log(describe(event, "parent mouse wheel"));
      }
    });

    // add pointer layer listener
    pointer.label.addListener(new Pointer.Listener() {
      ImageLayer label = pointer.label;
      @Override
      public void onPointerStart(Event event) {
        _lstart = label.transform().translation();
        _pstart = new Vector(event.x(), event.y());
        label.setAlpha(0.5f);
        modify(event);
        logger.log(describe(event, "pointer start"));
      }
      @Override
      public void onPointerDrag(Event event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        label.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        modify(event);
        motionLabel.set("pointer drag", describe(event, ""));
      }
      @Override
      public void onPointerEnd(Event event) {
        label.setAlpha(1.0f);
        modify(event);
        logger.log(describe(event, "pointer end"));
      }
      @Override
      public void onPointerCancel(Event event) {
        label.setAlpha(1.0f);
        modify(event);
        logger.log(describe(event, "pointer cancel"));
      }
      protected Vector _lstart, _pstart;
    });

    // add pointer listener for parent layer
    pointer.layer.addListener(new Pointer.Listener() {
      double start;
      @Override
      public void onPointerStart(Event event) {
        logger.log(describe(event, "parent pointer start"));
        start = event.time();
      }
      @Override
      public void onPointerDrag(Event event) {
        motionLabel.set("parent pointer drag", describe(event, ""));
        if (capture.value() && event.time() - start > 2000) {
          event.capture();
        }
      }
      @Override
      public void onPointerEnd(Event event) {
        logger.log(describe(event, "parent pointer end"));
      }
      @Override
      public void onPointerCancel(Event event) {
        logger.log(describe(event, "parent pointer cancel"));
      }
    });

    // add touch layer listener
    touch.label.addListener(new Touch.LayerListener() {
      ImageLayer label = touch.label;
      @Override
      public void onTouchStart(Touch.Event event) {
        _lstart = label.transform().translation();
        _pstart = new Vector(event.x(), event.y());
        label.setAlpha(0.5f);
        modify(event);
        logger.log(describe(event, "touch start"));
      }

      @Override
      public void onTouchMove(Touch.Event event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        label.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        modify(event);
        motionLabel.set("touch move", describe(event, ""));
      }

      @Override
      public void onTouchEnd(Touch.Event event) {
        label.setAlpha(1.0f);
        modify(event);
        logger.log(describe(event, "touch end"));
      }

      @Override
      public void onTouchCancel(Touch.Event event) {
        label.setAlpha(1.0f);
        modify(event);
        logger.log(describe(event, "touch cancel"));
      }

      protected Vector _lstart, _pstart;
    });

    // add touch parent layer listener
    touch.layer.addListener(new Touch.LayerListener() {
      @Override
      public void onTouchStart(Touch.Event event) {
        logger.log(describe(event, "parent touch start"));
      }

      @Override
      public void onTouchMove(Touch.Event event) {
        motionLabel.set("parent touch move", describe(event, ""));
      }

      @Override
      public void onTouchEnd(Touch.Event event) {
        logger.log(describe(event, "parent touch end"));
      }

      @Override
      public void onTouchCancel(Touch.Event event) {
        logger.log(describe(event, "parent touch cancel"));
      }
    });
  }

  @Override public boolean usesPositionalInputs () {
    return true;
  }

  @Override
  public void paint(float alpha) {
    super.paint(alpha);
    logger.paint();
    motionLabel.paint();
  }

  protected ImageLayer createLabel(String text, int bg, float x, float y) {
    return createLabel(text, graphics().rootLayer(), 0xFF202020, bg, x, y, 0);
  }

  protected ImageLayer createLabel(String text, GroupLayer parent,
                                   int fg, int bg, float x, float y, float padding) {
    TextLayout layout = graphics().layoutText(text, baseFormat);
    float twidth = layout.width() + padding * 2;
    float theight = layout.height() + padding * 2;
    CanvasImage image = graphics().createImage(twidth, theight);
    if (bg != 0) {
      image.canvas().setFillColor(bg);
      image.canvas().fillRect(0, 0, twidth, theight);
    }
    image.canvas().setFillColor(fg);
    image.canvas().fillText(layout, padding, padding);
    ImageLayer imageLayer = graphics().createImageLayer(image);
    imageLayer.setTranslation(x, y);
    parent.add(imageLayer);
    return imageLayer;
  }

  protected void modify(Events.Position event) {
    event.flags().setPreventDefault(preventDefault.value());
    event.flags().setPropagationStopped(propagate.valueIdx() == 2);
  }

  protected String describe(Events.Position event, String handler) {
    String time = "@" + (int)(event.time() % 10000);
    String pd = event.flags().getPreventDefault() ? "pd " : "";
    String msg = time + " " + pd + handler + " (" + event.x() + "," + event.y() + ")";
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

  protected class Label {
    public final ImageLayer layer;

    private final CanvasImage image;
    private final TextFormat format;
    private TextLayout[] layout;
    private String text;
    private boolean dirty;

    public Label(float wid, float hei, TextFormat format) {
      image = graphics().createImage(wid, hei);
      layer = graphics().createImageLayer(image);
      this.format = format;
    }

    public void set(String text) {
      this.text = text;
      dirty = true;
    }

    public void paint() {
      if (!dirty) {
        return;
      }

      image.canvas().clear();
      image.canvas().setFillColor(0xFF202020);
      layout = graphics().layoutText(text, format, TextWrap.MANUAL);
      float yy = 0;
      for (int line = 0; line < layout.length; line++) {
          image.canvas().fillText(layout[line], 0, yy);
          yy += layout[line].height();
      }
      if (yy > image.height()) {
        log().error("Clipped");
      }
      dirty = false;
    }
  }

  protected class TextMapper extends Label {
    public Map<String, String> values = new TreeMap<String, String>();
    public TextMapper(float wid, int lines, TextFormat format) {
      super(wid, graphics().layoutText(".", format).height() * lines, format);
    }

    public void set(String name, String value) {
      values.put(name, value);
      update();
    }

    public void update () {
      StringBuilder sb = new StringBuilder();
      Iterator<Map.Entry<String, String>> iter = values.entrySet().iterator();
      if (iter.hasNext()) append(sb, iter.next());
      while (iter.hasNext()) append(sb.append('\n'), iter.next());
      set(sb.toString());
    }
    void append (StringBuilder sb, Map.Entry<String, String> entry) {
      sb.append(entry.getKey()).append(": ").append(entry.getValue());
    }
  }

  protected class TextLogger extends Label {
    private final ArrayList<String> entries = new ArrayList<String>();
    private final int lineCount;

    public TextLogger(float wid, int lines, TextFormat format) {
      super(wid, graphics().layoutText(".", format).height() * lines, format);
      this.lineCount = lines;
    }

    public void log(String text) {
      entries.add(text);
      if (entries.size() > lineCount) {
        entries.remove(0);
      }
      StringBuilder sb = new StringBuilder();
      for (int i = entries.size() - 1; i >=0; i--) {
        sb.append(entries.get(i));
        sb.append('\n');
      }

      set(sb.toString());
    }
  }

  protected class Box implements ImmediateLayer.Renderer, Layer.HitTester {
    final GroupLayer.Clipped layer;
    final ImageLayer label;

    Box (String text, int color, float wid, float hei) {
      layer = graphics().createGroupLayer(wid, hei);
      layer.add(graphics().createImmediateLayer(this));
      label = createLabel(text, layer, 0xff000000, color, 0, 0, 40);
      layer.addAt(label, (wid - label.image().width()) / 2, (hei - label.image().height()) / 2);
      layer.setHitTester(this);
    }

    @Override
    public Layer hitTest(Layer layer, Point p) {
      if (p.x >= 0 && p.y >= 0 && p.x < this.layer.width() && p.y < this.layer.height()) {
        return layer.hitTestDefault(p);
      }
      return null;
    }

    @Override
    public void render(Surface surface) {
      surface.setFillColor(0xff000000);
      float t = 0.5f, l = 0.5f, b = layer.height() - 0.5f, r = layer.width() - 0.5f;
      surface.drawLine(l, t, l, b, 1);
      surface.drawLine(r, t, r, b, 1);
      surface.drawLine(l, b, r, b, 1);
      surface.drawLine(l, t, r, t, 1);
    }
  }
}
