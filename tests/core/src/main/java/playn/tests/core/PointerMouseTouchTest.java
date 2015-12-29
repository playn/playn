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
import react.UnitSlot;

import playn.core.*;
import playn.scene.*;
import playn.scene.Mouse;
import playn.scene.Pointer;
import playn.scene.Touch;
import static playn.tests.core.TestsGame.game;

class PointerMouseTouchTest extends Test {

  private TextFormat baseFormat = new TextFormat(new Font("Times New Roman", 20));
  private TextFormat logFormat = new TextFormat(new Font("Times New Roman", 12));

  private TextLogger logger;
  private TextMapper motionLabel;

  private TestsGame.Toggle preventDefault, capture;
  // private TestsGame.NToggle<String> propagate;

  public PointerMouseTouchTest (TestsGame game) {
    super(game, "PointerMouseTouch", "Tests the Pointer, Mouse, and Touch interfaces.");
  }

  @Override public void init() {
    float y = 20, x = 20;

    preventDefault = new TestsGame.Toggle("Prevent Default");
    game.rootLayer.addAt(preventDefault.layer, x, y);
    x += preventDefault.layer.width() + 5;

    capture = new TestsGame.Toggle("Capture");
    game.rootLayer.addAt(capture.layer, x, y);
    x += capture.layer.width() + 5;

    // propagate = new TestsGame.NToggle<String>("Propagation", "Off", "On", "On (stop)") {
    //   @Override public void set(int value) {
    //     super.set(value);
    //     platform().setPropagateEvents(value != 0);
    //   }
    // };
    // graphics().rootLayer().addAt(propagate.layer, x, y);

    y += preventDefault.layer.height() + 5;
    x = 20;

    float boxWidth = 300, boxHeight = 110;
    final Box mouse = new Box("Mouse", 0xffff8080, boxWidth, boxHeight);
    game.rootLayer.addAt(mouse.layer, x, y);
    y += mouse.layer.height() + 5;

    final Box pointer = new Box("Pointer", 0xff80ff80, boxWidth, boxHeight);
    game.rootLayer.addAt(pointer.layer, x, y);
    y += pointer.layer.height() + 5;

    final Box touch = new Box("Touch", 0xff8080ff, boxWidth, boxHeight);
    game.rootLayer.addAt(touch.layer, x, y);

    y = mouse.layer.ty();
    x += touch.layer.width() + 5;

    // setup the logger and its layer
    y += createLabel("Event Log", 0, x, y).height();
    logger = new TextLogger(375, 15, logFormat);
    logger.layer.setTranslation(x, y);
    game.rootLayer.add(logger.layer);
    y += logger.layer.height() + 5;

    // setup the motion logger and its layer
    y += createLabel("Motion Log", 0, x, y).height();
    motionLabel = new TextMapper(375, 6, logFormat);
    motionLabel.layer.setTranslation(x, y);
    game.rootLayer.add(motionLabel.layer);

    // add mouse layer listener
    mouse.label.events().connect(new Mouse.Listener() {
      ImageLayer label = mouse.label;

      @Override public void onButton(Mouse.ButtonEvent event, Mouse.Interaction iact) {
        if (event.down) {
          _lstart = label.transform().translation();
          _pstart = new Vector(event.x(), event.y());
          label.setAlpha(0.5f);
          modify(event);
          logger.log(describe(event, "mouse down"));
        } else {
          label.setAlpha(1.0f);
          modify(event);
          logger.log(describe(event, "mouse up"));
        }
      }

      @Override public void onDrag(Mouse.MotionEvent event, Mouse.Interaction iact) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        label.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        modify(event);
        motionLabel.set("mouse drag", describe(event, ""));
      }
      @Override public void onMotion (Mouse.MotionEvent event, Mouse.Interaction iact) {
        modify(event);
        motionLabel.set("mouse move", describe(event, ""));
      }

      @Override public void onHover (Mouse.HoverEvent event, Mouse.Interaction iact) {
        modify(event);
        logger.log(describe(event, event.inside ? "mouse over" : "mouse out"));
      }

      @Override public void onWheel (Mouse.WheelEvent event, Mouse.Interaction iact) {
        modify(event);
        logger.log(describe(event, "mouse wheel"));
      }

      protected Vector _lstart, _pstart;
    });

    // add mouse layer listener to parent
    mouse.layer.events().connect(new Mouse.Listener() {
      double start;
      @Override public void onButton(Mouse.ButtonEvent event, Mouse.Interaction iact) {
        if (event.down) {
          start = event.time;
          logger.log(describe(event, "parent mouse down " + capture.value()));
        }
        else logger.log(describe(event, "parent mouse up"));
      }
      @Override public void onDrag(Mouse.MotionEvent event, Mouse.Interaction iact) {
        motionLabel.set("parent mouse drag", describe(event, ""));
        if (capture.value() && event.time - start > 1000 && !iact.captured()) iact.capture();
      }
      @Override public void onMotion (Mouse.MotionEvent event, Mouse.Interaction iact) {
        motionLabel.set("parent mouse move", describe(event, ""));
      }
      @Override public void onHover (Mouse.HoverEvent event, Mouse.Interaction iact) {
        logger.log(describe(event, "parent mouse " + (event.inside ? "over" : "out")));
      }
      @Override public void onWheel (Mouse.WheelEvent event, Mouse.Interaction iact) {
        logger.log(describe(event, "parent mouse wheel"));
      }
    });

    // add pointer layer listener
    pointer.label.events().connect(new Pointer.Listener() {
      ImageLayer label = pointer.label;
      @Override public void onStart(Pointer.Interaction iact) {
        Pointer.Event event = iact.event;
        _lstart = label.transform().translation();
        _pstart = new Vector(event.x(), event.y());
        label.setAlpha(0.5f);
        modify(event);
        logger.log(describe(event, "pointer start"));
      }
      @Override public void onDrag(Pointer.Interaction iact) {
        Pointer.Event event = iact.event;
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        label.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        modify(event);
        motionLabel.set("pointer drag", describe(event, ""));
      }

      @Override public void onEnd(Pointer.Interaction iact) {
        Pointer.Event event = iact.event;
        label.setAlpha(1.0f);
        modify(event);
        logger.log(describe(event, "pointer end"));
      }
      @Override public void onCancel(Pointer.Interaction iact) {
        Pointer.Event event = iact.event;
        label.setAlpha(1.0f);
        modify(event);
        logger.log(describe(event, "pointer cancel"));
      }
      protected Vector _lstart, _pstart;
    });

    // add pointer listener for parent layer
    pointer.layer.events().connect(new Pointer.Listener() {
      double start;
      @Override public void onStart(Pointer.Interaction iact) {
        Pointer.Event event = iact.event;
        logger.log(describe(event, "parent pointer start"));
        start = event.time;
      }
      @Override public void onDrag(Pointer.Interaction iact) {
        Pointer.Event event = iact.event;
        motionLabel.set("parent pointer drag", describe(event, ""));
        if (capture.value() && event.time - start > 1000 && !iact.captured()) iact.capture();
      }
      @Override public void onEnd(Pointer.Interaction iact) {
        Pointer.Event event = iact.event;
        logger.log(describe(event, "parent pointer end"));
      }
      @Override public void onCancel(Pointer.Interaction iact) {
        Pointer.Event event = iact.event;
        logger.log(describe(event, "parent pointer cancel"));
      }
    });

    // add touch layer listener
    touch.label.events().connect(new Touch.Listener() {
      ImageLayer label = touch.label;
      @Override public void onStart(Touch.Interaction iact) {
        Touch.Event event = iact.event;
        _lstart = label.transform().translation();
        _pstart = new Vector(event.x(), event.y());
        label.setAlpha(0.5f);
        modify(event);
        logger.log(describe(event, "touch start"));
      }
      @Override public void onMove(Touch.Interaction iact) {
        Touch.Event event = iact.event;
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        label.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        modify(event);
        motionLabel.set("touch move", describe(event, ""));
      }

      @Override public void onEnd(Touch.Interaction iact) {
        Touch.Event event = iact.event;
        label.setAlpha(1.0f);
        modify(event);
        logger.log(describe(event, "touch end"));
      }
      @Override public void onCancel(Touch.Interaction iact) {
        Touch.Event event = iact.event;
        label.setAlpha(1.0f);
        modify(event);
        logger.log(describe(event, "touch cancel"));
      }
      protected Vector _lstart, _pstart;
    });

    // add touch parent layer listener
    touch.layer.events().connect(new Touch.Listener() {
      @Override public void onStart(Touch.Interaction iact) {
        Touch.Event event = iact.event;
        logger.log(describe(event, "parent touch start"));
      }
      @Override public void onMove(Touch.Interaction iact) {
        Touch.Event event = iact.event;
        motionLabel.set("parent touch move", describe(event, ""));
      }
      @Override public void onEnd(Touch.Interaction iact) {
        Touch.Event event = iact.event;
        logger.log(describe(event, "parent touch end"));
      }
      @Override public void onCancel(Touch.Interaction iact) {
        Touch.Event event = iact.event;
        logger.log(describe(event, "parent touch cancel"));
      }
    });

    conns.add(game.plat.frame.connect(new UnitSlot() { public void onEmit () {
      logger.paint();
      motionLabel.paint();
    }}));
  }

  @Override public boolean usesPositionalInputs () {
    return true;
  }

  protected ImageLayer createLabel(String text, int bg, float x, float y) {
    return createLabel(text, game.rootLayer, 0xFF202020, bg, x, y, 0);
  }

  protected ImageLayer createLabel(String text, GroupLayer parent,
                                   int fg, int bg, float x, float y, float padding) {
    TextLayout layout = game.graphics.layoutText(text, baseFormat);
    float twidth = layout.size.width() + padding * 2;
    float theight = layout.size.height() + padding * 2;
    Canvas canvas = game.graphics.createCanvas(twidth, theight);
    if (bg != 0) canvas.setFillColor(bg).fillRect(0, 0, twidth, theight);
    canvas.setFillColor(fg).fillText(layout, padding, padding);
    ImageLayer imageLayer = new ImageLayer(canvas.toTexture());
    parent.addAt(imageLayer, x, y);
    return imageLayer;
  }

  protected void modify(Event.XY event) {
    event.updateFlag(Event.F_PREVENT_DEFAULT, preventDefault.value());
    // TODO
    // event.flags().setPropagationStopped(propagate.valueIdx() == 2);
  }

  protected String describe(Event.XY event, String handler) {
    StringBuilder sb = new StringBuilder();
    sb.append("@").append((int)(event.time % 10000)).append(" ");
    sb.append(event.isSet(Event.F_PREVENT_DEFAULT) ? "pd " : "");
    sb.append(handler).append(" (").append(event.x()).append(",").append(event.y()).append(")");
    sb.append(" m[");
    if (event.isAltDown()) sb.append("A");
    if (event.isCtrlDown()) sb.append("C");
    if (event.isMetaDown()) sb.append("M");
    if (event.isShiftDown()) sb.append("S");
    sb.append("]");
    if (event instanceof Pointer.Event) {
      sb.append(" isTouch(").append(((Pointer.Event)event).isTouch).append(")");
    }
    if (event instanceof Mouse.ButtonEvent) {
      sb.append(" button(").append(((Mouse.ButtonEvent)event).button).append(")");
    }
    if (event instanceof Mouse.MotionEvent) {
      Mouse.MotionEvent me = (Mouse.MotionEvent)event;
      sb.append(" d(").append(me.dx).append(",").append(me.dy).append(")");
    }

    return sb.toString();
  }

  protected class Label {
    public final CanvasLayer layer;

    private final TextFormat format;
    private TextLayout[] layout;
    private String text;
    private boolean dirty;

    public Label(float wid, float hei, TextFormat format) {
      layer = new CanvasLayer(game.graphics, wid, hei);
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

      Canvas canvas = layer.begin();
      canvas.clear();
      canvas.setFillColor(0xFF202020);
      layout = game.graphics.layoutText(text, format, TextWrap.MANUAL);
      float yy = 0;
      for (int line = 0; line < layout.length; line++) {
        canvas.fillText(layout[line], 0, yy);
        yy += layout[line].size.height();
      }
      // if (yy > layer.height()) {
      //   game.log.error("Clipped");
      // }
      layer.end();
      dirty = false;
    }
  }

  protected class TextMapper extends Label {
    public Map<String, String> values = new TreeMap<String, String>();
    public TextMapper(float wid, int lines, TextFormat format) {
      super(wid, game.graphics.layoutText(".", format).size.height() * lines, format);
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
      super(wid, game.graphics.layoutText(".", format).size.height() * lines, format);
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

  protected class Box implements Layer.HitTester {
    final GroupLayer layer;
    final ImageLayer label;

    Box (String text, int color, float wid, float hei) {
      layer = new GroupLayer(wid, hei);
      layer.add(new Layer() {
        protected void paintImpl (Surface surface) {
          surface.setFillColor(0xff000000);
          float t = 0.5f, l = 0.5f, b = layer.height() - 0.5f, r = layer.width() - 0.5f;
          surface.drawLine(l, t, l, b, 1);
          surface.drawLine(r, t, r, b, 1);
          surface.drawLine(l, b, r, b, 1);
          surface.drawLine(l, t, r, t, 1);
        }
      });
      label = createLabel(text, layer, 0xff000000, color, 0, 0, 40);
      layer.addAt(label, (wid - label.width()) / 2, (hei - label.height()) / 2);
      layer.setHitTester(this);
    }

    @Override
    public Layer hitTest(Layer layer, Point p) {
      if (p.x >= 0 && p.y >= 0 && p.x < this.layer.width() && p.y < this.layer.height()) {
        return layer.hitTestDefault(p);
      }
      return null;
    }
  }
}
