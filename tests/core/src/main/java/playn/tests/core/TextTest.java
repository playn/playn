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

import playn.core.Font.Style;
import playn.core.Image;
import playn.core.Keyboard.TextType;
import playn.core.Pointer;
import playn.core.Pointer.Event;
import playn.core.TextFormat;
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.ImageLayer;
import playn.core.TextFormat.Alignment;
import playn.core.TextLayout;
import playn.core.util.Callback;
import pythagoras.f.Rectangle;
import static playn.core.PlayN.graphics;
import static playn.core.PlayN.keyboard;

public class TextTest extends Test {
  private class NToggle<T> extends TestsGame.NToggle<T> {
    public NToggle(String name, T...values) {
      super(name, values);
    }
    @Override public void set (int idx) {
      super.set(idx);
      update();
    }
  }

  public class Toggle extends NToggle<Boolean> {
    public Toggle(String name) {
      super(name, Boolean.FALSE, Boolean.TRUE);
    }
  }

  NToggle<Style> style;
  NToggle<String> draw;
  NToggle<String> effect;
  NToggle<Alignment> align;
  NToggle<String> font;
  NToggle<Integer> wrap;
  Toggle lineBounds;
  final float outlineWidth = 2;
  String sample = "The quick brown fox\njumped over the lazy dog.\nEvery good boy deserves fudge.";
  ImageLayer text;
  Rectangle row;

  @Override
  public String getName() {
    return "TextTest";
  }

  @Override
  public String getDescription() {
    return "Tests various text rendering features.";
  }

  @Override
  public void init() {
    row = new Rectangle(5, 5, 0, 0);
    addToRow((style = new NToggle<Style>(
        "Style", Style.PLAIN, Style.BOLD, Style.ITALIC, Style.BOLD_ITALIC)).layer);
    addToRow((draw = new NToggle<String>("Draw", "Fill", "Stroke")).layer);
    addToRow((effect = new NToggle<String>(
        "Effect", "None", "ShadowUL", "ShadowLR", "Outline")).layer);
    addToRow((wrap = new NToggle<Integer>("Wrap", 0, 20, 50, 100)).layer);
    addToRow((align = new NToggle<Alignment>(
        "Align", Alignment.LEFT, Alignment.CENTER, Alignment.RIGHT)).layer);
    addToRow((font = new NToggle<String>("Font", "Times New Roman", "Helvetica")).layer);

    class SetText extends Pointer.Adapter implements Callback<String> {
      final ImageLayer layer = graphics().createImageLayer(TestsGame.makeButtonImage("Set Text"));{
        layer.addListener(this);
      }
      @Override public void onPointerEnd(Event event) {
        keyboard().getText(TextType.DEFAULT, "Test text", sample.replace("\n", "\\n"), this);
      }
      public void onSuccess(String result) {
        if (result == null) return;
        // parse \n to allow testing line breaks
        sample = result.replace("\\n", "\n");
        update();
      }
      public void onFailure(Throwable cause) {}
    }
    addToRow(new SetText().layer);
    addToRow((lineBounds = new Toggle("Lines")).layer);

    // test laying out the empty string
    TextLayout layout = graphics().layoutText("", new TextFormat());
    ImageLayer empty = graphics().createImageLayer(makeLabel(
      "Empty string size " + layout.width() + "x" + layout.height()));
    newRow();
    addToRow(empty);

    newRow();

    addToRow((text = graphics().createImageLayer(makeTextImage())));
  }

  protected void addToRow (ImageLayer layer) {
    graphics().rootLayer().add(layer.setTranslation(row.x + row.width, row.y));
    row.width += layer.width() + 45;
    row.height = Math.max(row.height, layer.height());
    if (row.width > graphics().width() * .6f) newRow();
  }

  protected void newRow () {
    row.x = 5;
    row.y += row.height + 5;
    row.width = row.height = 0;
  }

  protected void update() {
    if (text == null) return;
    text.setImage(makeTextImage());
  }

  protected Image makeLabel(String label) {
    TextLayout layout = graphics().layoutText(label, new TextFormat());
    CanvasImage image = graphics().createImage(layout.width(), layout.height());
    image.canvas().setFillColor(0xFF000000);
    image.canvas().fillText(layout, 0, 0);
    return image;
  }

  protected Image makeTextImage() {
    TextLayout layout = graphics().layoutText(sample, format());
    float twidth = adjustWidth(layout.width());
    float theight = adjustHeight(layout.height());
    CanvasImage image = graphics().createImage(twidth, theight);
    image.canvas().setStrokeColor(0xFFFFCCCC);
    image.canvas().strokeRect(0, 0, twidth, theight);
    render(image.canvas(), layout);
    if (lineBounds.value()) {
      for (int ll = 0; ll < layout.lineCount(); ll++) {
        Rectangle bounds = layout.lineBounds(ll);
        image.canvas().setStrokeColor(0xFFFFCCCC);
        image.canvas().setStrokeWidth(1);
        image.canvas().strokeRect(bounds.x, bounds.y, bounds.width, bounds.height);
      }
    }
    return image;
  }

  protected TextFormat format () {
    TextFormat format = new TextFormat().withFont(
      graphics().createFont(font.value(), style.value(), 24)).withAlignment(align.value());
    return wrap.value() == 0 ? format : format.withWrapWidth(graphics().width()*wrap.value()/100);
  }

  protected float adjustDim (float value) {
    String effect = this.effect.value();
    if (effect.startsWith("Shadow")) {
      value+=2;
    } else if (effect.equals("Outline")) {
      value+=outlineWidth*2;
    }
    return value;
  }
  protected float adjustWidth(float width) {
    return adjustDim(width);
  }
  protected float adjustHeight(float height) {
    return adjustDim(height);
  }
  protected void render (Canvas canvas, TextLayout text, int color, float x, float y) {
    if (draw.value().equals("Fill")) {
      canvas.setFillColor(color);
      canvas.fillText(text, x, y);
    } else {
      canvas.setStrokeColor(color);
      canvas.strokeText(text, x, y);
    }
  }
  protected void render (Canvas canvas, TextLayout text) {
    String effect = this.effect.value();
    if (effect.equals("ShadowUL")) {
      render(canvas, text, 0xFFCCCCCC, 0, 0);
      render(canvas, text, 0xFF6699CC, 2, 2);
    } else if (effect.equals("ShadowLR")) {
      render(canvas, text, 0xFFCCCCCC, 2, 2);
      render(canvas, text, 0xFF6699CC, 0, 0);
    } else if (effect.equals("Outline")) {
      canvas.setStrokeWidth(2*outlineWidth);
      canvas.setStrokeColor(0xFF336699);
      canvas.setLineCap(Canvas.LineCap.ROUND);
      canvas.setLineJoin(Canvas.LineJoin.ROUND);
      canvas.strokeText(text, outlineWidth, outlineWidth);
      render(canvas, text, 0xFF6699CC, outlineWidth, outlineWidth);
    } else {
      render(canvas, text, 0xFF6699CC, 0, 0);
    }
  }
}
