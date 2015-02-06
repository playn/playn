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

import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

import playn.core.*;
import playn.scene.*;
import react.Slot;

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

  private NToggle<Font.Style> style;
  private NToggle<String> draw;
  private NToggle<String> effect;
  private NToggle<TextBlock.Align> align;
  private NToggle<String> font;
  private NToggle<Integer> wrap;
  private NToggle<Boolean> lineBounds;
  private final float outlineWidth = 2;
  private String sample = "The quick brown fox\njumped over the lazy dog.\nEvery good boy deserves fudge.";
  private ImageLayer text;
  private Rectangle row;

  public TextTest (TestsGame game) {
    super(game, "Text", "Tests various text rendering features.");
  }

  @Override public void init() {
    row = new Rectangle(5, 5, 0, 0);
    addToRow((style = new NToggle<Font.Style>("Style", Font.Style.values())).layer);
    addToRow((draw = new NToggle<String>("Draw", "Fill", "Stroke")).layer);
    addToRow((effect = new NToggle<String>(
        "Effect", "None", "ShadowUL", "ShadowLR", "Outline")).layer);
    addToRow((wrap = new NToggle<Integer>("Wrap", 0, 20, 50, 100)).layer);
    addToRow((align = new NToggle<TextBlock.Align>(
        "Align", TextBlock.Align.LEFT, TextBlock.Align.CENTER, TextBlock.Align.RIGHT)).layer);
    addToRow((font = new NToggle<String>("Font", "Times New Roman", "Helvetica")).layer);

    ImageLayer layer = game.ui.createButton("Set Text", new Runnable() {
      public void run () {
        game.input.getText(Keyboard.TextType.DEFAULT, "Test text", sample.replace("\n", "\\n")).
          onSuccess(new Slot<String>() {
            public void onEmit (String text) {
              if (text == null) return;
              // parse \n to allow testing line breaks
              sample = text.replace("\\n", "\n");
              update();
            }
          });
      }
    });
    addToRow(layer);
    addToRow((lineBounds = new NToggle<Boolean>("Lines", Boolean.FALSE, Boolean.TRUE)).layer);

    // test laying out the empty string
    TextLayout layout = game.graphics.layoutText("", new TextFormat());
    ImageLayer empty = new ImageLayer(makeLabel(
      "Empty string size " + layout.size.width() + "x" + layout.size.height()));
    newRow();
    addToRow(empty);

    newRow();

    addToRow((text = new ImageLayer(makeTextImage())));
  }

  protected void addToRow (ImageLayer layer) {
    game.rootLayer.add(layer.setTranslation(row.x + row.width, row.y));
    row.width += layer.width() + 45;
    row.height = Math.max(row.height, layer.height());
    if (row.width > game.graphics.viewSize.width() * .6f) newRow();
  }

  protected void newRow () {
    row.x = 5;
    row.y += row.height + 5;
    row.width = row.height = 0;
  }

  protected void update() {
    if (text == null) return;
    text.setTile(makeTextImage());
  }

  protected Texture makeLabel(String label) {
    TextLayout layout = game.graphics.layoutText(label, new TextFormat());
    Canvas canvas = game.graphics.createCanvas(layout.size);
    canvas.setFillColor(0xFF000000).fillText(layout, 0, 0);
    return canvas.toTexture();
  }

  protected Texture makeTextImage() {
    TextFormat format = new TextFormat(new Font(font.value(), style.value(), 24));
    float wrapWidth = wrap.value() == 0 ?
      Float.MAX_VALUE : game.graphics.viewSize.width()*wrap.value()/100;
    TextBlock block = new TextBlock(
      game.graphics.layoutText(sample, format, new TextWrap(wrapWidth)));
    float awidth = adjustWidth(block.bounds.width()), aheight = adjustHeight(block.bounds.height());
    float pad = 1/game.graphics.scale().factor;
    Canvas canvas = game.graphics.createCanvas(awidth+2*pad, aheight+2*pad);
    canvas.translate(pad, pad);
    canvas.setStrokeColor(0xFFFFCCCC).strokeRect(0, 0, awidth, aheight);
    render(canvas, block, align.value(), lineBounds.value());
    return canvas.toTexture();
  }

  protected float adjustDim (float value) {
    String effect = this.effect.value();
    if (effect.startsWith("Shadow")) {
      value += 2;
    } else if (effect.equals("Outline")) {
      value += outlineWidth*2;
    }
    return value;
  }
  protected float adjustWidth(float width) {
    return adjustDim(width);
  }
  protected float adjustHeight(float height) {
    return adjustDim(height);
  }

  protected void render (Canvas canvas, String strokeFill, TextBlock block, TextBlock.Align align,
                         int color, float x, float y, boolean showBounds) {
    float sy = y + block.bounds.y();
    for (TextLayout layout : block.lines) {
      float sx = x + block.bounds.x() + align.getX(
        layout.size.width(), block.bounds.width()-block.bounds.x());
      if (showBounds) {
        IRectangle lbounds = layout.bounds;
        canvas.setStrokeColor(0xFFFFCCCC).setStrokeWidth(1);
        canvas.strokeRect(sx+lbounds.x(), sy+lbounds.y(), lbounds.width(), lbounds.height());
      }
      if (strokeFill.equals("Fill")) {
        canvas.setFillColor(color).fillText(layout, sx, sy);
      } else {
        canvas.setStrokeColor(color).strokeText(layout, sx, sy);
      }
      sy += layout.ascent() + layout.descent() + layout.leading();
    }
  }

  protected void render (Canvas canvas, TextBlock block, TextBlock.Align align, boolean showBounds) {
    String effect = this.effect.value(), strokeFill = draw.value();
    if (effect.equals("ShadowUL")) {
      render(canvas, strokeFill, block, align, 0xFFCCCCCC, 0, 0, showBounds);
      render(canvas, strokeFill, block, align, 0xFF6699CC, 2, 2, false);
    } else if (effect.equals("ShadowLR")) {
      render(canvas, strokeFill, block, align, 0xFFCCCCCC, 2, 2, false);
      render(canvas, strokeFill, block, align, 0xFF6699CC, 0, 0, showBounds);
    } else if (effect.equals("Outline")) {
      canvas.setStrokeWidth(2*outlineWidth);
      canvas.setLineCap(Canvas.LineCap.ROUND);
      canvas.setLineJoin(Canvas.LineJoin.ROUND);
      render(canvas, "Stroke", block, align, 0xFF336699,
             outlineWidth, outlineWidth, false);
      render(canvas, "Fill", block, align, 0xFF6699CC,
             outlineWidth, outlineWidth, showBounds);
    } else {
      render(canvas, strokeFill, block, align, 0xFF6699CC, 0, 0, showBounds);
    }
  }
}
