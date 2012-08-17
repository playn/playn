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
import java.util.List;
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
import playn.core.Mouse.WheelEvent;
import playn.core.Pointer;
import playn.core.Surface;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.Mouse.ButtonEvent;
import playn.core.Mouse.MotionEvent;
import playn.core.Pointer.Event;
import playn.core.Touch;
import static playn.core.PlayN.*;

class PointerMouseTouchTest extends Test {
  private TextFormat baseFormat = new TextFormat().
      withFont(graphics().createFont("Times New Roman", Font.Style.PLAIN, 20));
  private TextFormat logFormat = new TextFormat().
      withFont(graphics().createFont("Times New Roman", Font.Style.PLAIN, 12));

  private TextLogger logger;
  private TextMapper motionLabel;

  private Toggle<Boolean> preventDefault;
  private Toggle<String> propagate;

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

    preventDefault = new Toggle<Boolean>("Prevent Default", false, true);
    graphics().rootLayer().addAt(preventDefault.layer, 20, y);
    y += preventDefault.layer.image().height() + 5;

    propagate = new Toggle<String>("Propagation", "Off", "On", "On (stop)") {
      @Override
      void set(int value) {
        super.set(value);
        platform().setPropagateEvents(value != 0);
      }
    };
    graphics().rootLayer().addAt(propagate.layer, 20, y);
    y += propagate.layer.image().height() + 5;

    float boxWidth = 300, boxHeight = 110;
    final Box mouse = new Box("Mouse", 0xffff8080, boxWidth, boxHeight);
    graphics().rootLayer().addAt(mouse.layer, 20, y);
    y += mouse.layer.height() + 5;

    final Box pointer = new Box("Pointer", 0xff80ff80, boxWidth, boxHeight);
    graphics().rootLayer().addAt(pointer.layer, 20, y);
    y += pointer.layer.height() + 5;

    final Box touch = new Box("Touch", 0xff8080ff, boxWidth, boxHeight);
    graphics().rootLayer().addAt(touch.layer, 20, y);
    y += touch.layer.height() + 5;

    // setup the logger and its layer
    createLabel("Event Log", 0, 325, 20);
    logger = new TextLogger(375, 300, logFormat);
    logger.layer.setTranslation(325, 40);
    graphics().rootLayer().add(logger.layer);

    // setup the motion logger and its layer
    createLabel("Motion Log", 0, 325, 340);
    motionLabel = new TextMapper(375, 120, logFormat);
    motionLabel.layer.setTranslation(325, 360);
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
      protected Vector _lstart, _pstart;
    });

    // add pointer listener for parent layer
    pointer.layer.addListener(new Pointer.Listener() {
      @Override
      public void onPointerStart(Event event) {
        logger.log(describe(event, "parent pointer start"));
      }
      @Override
      public void onPointerDrag(Event event) {
        motionLabel.set("parent pointer drag", describe(event, ""));
      }
      @Override
      public void onPointerEnd(Event event) {
        logger.log(describe(event, "parent pointer end"));
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
    return createLabel(text, graphics().rootLayer(), 0xFF6699CC, bg, x, y);
  }

  protected ImageLayer createLabel(String text, GroupLayer parent,
                                   int fg, int bg, float x, float y) {
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
    parent.add(imageLayer);
    return imageLayer;
  }

  protected void modify(Events.Position event) {
    event.flags().setPreventDefault(preventDefault.value());
    event.flags().setPropagationStopped(propagate.valueIdx == 2);
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

  protected class Label {
    public final ImageLayer layer;

    private final CanvasImage image;
    private final TextFormat format;
    private TextLayout layout;
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

      layout = graphics().layoutText(text, format);
      if (layout.height() > image.height()) {
        System.out.println("Clipped");
      }
      image.canvas().clear();
      image.canvas().setFillColor(0xFF6699CC);
      image.canvas().fillText(layout, 0, 0);
      dirty = false;
    }
  }

  protected class TextMapper extends Label {
    public Map<String, String> values = new TreeMap<String, String>();
    public TextMapper(float wid, float hei, TextFormat format) {
      super(wid, hei, format);
    }

    public void set(String name, String value) {
      values.put(name, value);
      update();
    }

    public void update () {
      StringBuilder sb = new StringBuilder();
      for (String name : values.keySet()) {
        sb.append(name).append(": ").append(values.get(name)).append('\n');
      }
      set(sb.toString());
    }
  }

  protected class TextLogger extends Label {
    private final ArrayList<String> entries = new ArrayList<String>();
    private final int lineCount;

    public TextLogger(float wid, float hei, TextFormat format) {
      super(wid, hei, format);
      int lineCount = 1;
      for (String maxText = "a\n";;lineCount++, maxText += "a\n") {
        if (graphics().layoutText(maxText, format).height() > hei) {
          break;
        }
      }
      this.lineCount = lineCount - 1;
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

  protected class Toggle<T> {
    final ImageLayer layer = graphics().createImageLayer();
    final String prefix;
    final List<T> values = new ArrayList<T>();
    int valueIdx;

    Toggle(String name, T...values) {
      for (T value : values) {
        this.values.add(value);
      }
      this.prefix = name + ": ";
      layer.addListener(new Pointer.Adapter() {
        @Override
        public void onPointerStart(Event event) {
          set((valueIdx + 1) % Toggle.this.values.size());
        }
      });

      set(0);
    }

    String toString(T value) {
      return value.toString();
    }

    void set(int idx) {
      this.valueIdx = idx;
      layer.setImage(TestsGame.makeButtonImage(prefix + toString(values.get(idx))));
    }

    T value() {
      return values.get(valueIdx);
    }
  }

  protected class Box implements ImmediateLayer.Renderer, Layer.HitTester {
    final GroupLayer.Clipped layer;
    final ImageLayer label;

    Box (String text, int color, float wid, float hei) {
      layer = graphics().createGroupLayer(wid, hei);
      layer.add(graphics().createImmediateLayer(this));
      label = createLabel(text, layer, 0xff000000, color, 0, 0);
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
      surface.drawLine(0, 0, layer.width(), 0, 1);
      surface.drawLine(layer.width(), 0, layer.width(), layer.height(), 1);
      surface.drawLine(layer.width(), layer.height() - 1, 0, layer.height() - 1, 1);
      surface.drawLine(1, layer.height() - 1, 1, 0, 1);
    }
  }
}
