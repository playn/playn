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
import playn.core.Font;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.Mouse;
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
    Image cat = assets().getImage("images/cat.png");
    Image horns = assets().getImage("images/horns.png");

    createLabel("No Prevent Default", 75, 20);
    createLabel("Yes Prevent Default", 250, 20);
    createLabel("Event Log", 475, 20);
    createLabel("Mouse", 20, 100);
    createLabel("Pointer", 20, 350);


    // setup the logger and its layer
    CanvasImage logImage = graphics().createImage(300, 400);
    ImageLayer logLayer = graphics().createImageLayer(logImage);
    logLayer.setTranslation(400, 40);
    graphics().rootLayer().add(logLayer);
    logger = new TextLogger(logImage, logFormat);


    // add mouse layer listener without prevent default
    final ImageLayer mouse = graphics().createImageLayer(cat);
    mouse.setTranslation(100, 0);
    graphics().rootLayer().add(mouse);
    mouse.addListener(new Mouse.LayerAdapter() {
      @Override
      public void onMouseDown(ButtonEvent event) {
        _lstart = mouse.transform().translation();
        _pstart = new Vector(event.x(), event.y());
        mouse.setAlpha(0.5f);
        logger.log("mouse down (" + event.x() + "," + event.y() + ") button(" + event.button() + ")");
      }
      @Override
      public void onMouseDrag(MotionEvent event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        mouse.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        logger.log("mouse move (" + event.x() + "," + event.y() + ")");
      }
      @Override
      public void onMouseUp(ButtonEvent event) {
        mouse.setAlpha(1.0f);
        logger.log("mouse up (" + event.x() + "," + event.y() + ")");
      }
      protected Vector _lstart, _pstart;
    });


    // add mouse layer listener with prevent default
    final ImageLayer mousePD = graphics().createImageLayer(cat);
    mousePD.setTranslation(275, 0);
    graphics().rootLayer().add(mousePD);
    mousePD.addListener(new Mouse.LayerAdapter() {
      @Override
      public void onMouseDown(ButtonEvent event) {
        _lstart = mousePD.transform().translation();
        _pstart = new Vector(event.x(), event.y());
        mousePD.setAlpha(0.5f);
        event.setPreventDefault(true);
        logger.log("pd mouse down (" + event.x() + "," + event.y() + ") button(" + event.button() + ")");
      }
      @Override
      public void onMouseDrag(MotionEvent event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        mousePD.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        event.setPreventDefault(true);
        logger.log("pd mouse drag (" + event.x() + "," + event.y() + ")");
      }
      @Override
      public void onMouseUp(ButtonEvent event) {
        mousePD.setAlpha(1.0f);
        event.setPreventDefault(true);
        logger.log("pd mouse up (" + event.x() + "," + event.y() + ")");
      }
      protected Vector _lstart, _pstart;
    });


    // add pointer layer listener without prevent default
    final ImageLayer pointer = graphics().createImageLayer(horns);
    pointer.setTranslation(100, 250);
    graphics().rootLayer().add(pointer);
    pointer.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Event event) {
        _lstart = pointer.transform().translation();
        _pstart = new Vector(event.x(), event.y());
        pointer.setAlpha(0.5f);
        logger.log("pointer start (" + event.x() + "," + event.y() + ") touch(" + (event.isTouch() ? "yes" : "no") + ")");
      }
      @Override
      public void onPointerDrag(Event event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        pointer.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        logger.log("pointer drag (" + event.x() + "," + event.y() + ")");
      }
      @Override
      public void onPointerEnd(Event event) {
        pointer.setAlpha(1.0f);
        logger.log("pointer end (" + event.x() + "," + event.y() + ")");
      }
      protected Vector _lstart, _pstart;
    });


    // add pointer layer listener with prevent default
    final ImageLayer pointerPD = graphics().createImageLayer(horns);
    pointerPD.setTranslation(275, 250);
    graphics().rootLayer().add(pointerPD);
    pointerPD.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Event event) {
        _lstart = pointerPD.transform().translation();
        _pstart = new Vector(event.x(), event.y());
        pointerPD.setAlpha(0.5f);
        event.setPreventDefault(true);
        logger.log("pd pointer start (" + event.x() + "," + event.y() + ") touch(" + (event.isTouch() ? "yes" : "no") + ")");
      }
      @Override
      public void onPointerDrag(Event event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        pointerPD.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
        event.setPreventDefault(true);
        logger.log("pd pointer drag (" + event.x() + "," + event.y() + ")");
      }
      @Override
      public void onPointerEnd(Event event) {
        pointerPD.setAlpha(1.0f);
        event.setPreventDefault(true);
        logger.log("pd pointer end (" + event.x() + "," + event.y() + ")");
      }
      protected Vector _lstart, _pstart;
    });
  }

  protected void createLabel(String text, float x, float y) {
    TextLayout layout = graphics().layoutText(text, baseFormat);
    float twidth = layout.width();
    float theight = layout.height();
    CanvasImage image = graphics().createImage(twidth, theight);
    image.canvas().setFillColor(0xFF6699CC);
    image.canvas().fillText(layout, 0, 0);
    ImageLayer imageLayer = graphics().createImageLayer(image);
    imageLayer.setTranslation(x, y);
    graphics().rootLayer().add(imageLayer);
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
}
