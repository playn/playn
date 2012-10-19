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

import playn.core.Image;
import playn.core.Keyboard.TextType;
import playn.core.Pointer;
import playn.core.Pointer.Event;
import playn.core.PlayN;
import playn.core.TextFormat;
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.TextLayout;
import playn.core.util.Callback;
import static playn.core.PlayN.graphics;
import static playn.core.PlayN.keyboard;

public class TextTest extends Test {

  private TextFormat baseFormat = new TextFormat().
    withFont(graphics().createFont("Times New Roman", Font.Style.PLAIN, 24));
  private final float COL_WIDTH = 120;

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
    float x = 0;
    x += addExamples("Filled", FILL, x);
    x += addExamples("Stroked", STROKE, x);
    x += addExamples("Vector otln", OUTLINE_VEC, x);
    x += addExamples("Shadow UL", SHADOW_UL, x);
    x += addExamples("Shadow LR", SHADOW_LR, x);

    // test laying out the empty string
    TextLayout layout = graphics().layoutText("", new TextFormat());
    ImageLayer layer = makeTextLayer(
      "Empty string size " + layout.width() + "x" + layout.height(), FILL, baseFormat);
    graphics().rootLayer().addAt(layer, 10, 330);

    class Field extends Pointer.Adapter implements Callback<String> {
      final StringBuilder value = new StringBuilder("Click here to change text");
      final ImageLayer layer = makeTextLayer(value.toString(), FILL, baseFormat); {
        layer.addListener(this);
      }
      public void onPointerEnd(Event event) {
        keyboard().getText(TextType.DEFAULT, "Test text", value.toString(), this);
      }
      public void onSuccess(String result) {
        if (result == null) return;
        value.setLength(0);
        value.append(result.replace("\\n", "\n")); // line break parsing for testing
        layer.setImage(makeTextImage(value.toString(), FILL, baseFormat));
      }
      public void onFailure(Throwable cause) {}
    }
    graphics().rootLayer().addAt(new Field().layer, 10, 380);
  }

  protected float addExamples(String name, TextRenderer renderer, float x) {
    GroupLayer root = graphics().rootLayer();
    ImageLayer[] layers = {
      makeTextLayer(name, renderer, baseFormat),
      makeTextLayer("The quick brown fox", renderer,
                    baseFormat.withWrapping(COL_WIDTH, TextFormat.Alignment.LEFT)),
      makeTextLayer("jumped over the lazy dog.", renderer,
                    baseFormat.withWrapping(COL_WIDTH, TextFormat.Alignment.CENTER)),
      makeTextLayer("Every good boy deserves fudge.", renderer,
                    baseFormat.withWrapping(COL_WIDTH, TextFormat.Alignment.RIGHT)),
    };
    float y = 0, maxwid = 0;
    for (ImageLayer layer : layers) {
      root.addAt(layer, x, y);
      maxwid = Math.max(layer.width(), maxwid);
      y += layer.height() + 5;
    }
    return maxwid + 5;
  }

  protected ImageLayer makeTextLayer(String text, TextRenderer renderer, TextFormat format) {
    return graphics().createImageLayer(makeTextImage(text, renderer, format));
  }

  protected Image makeTextImage(String text, TextRenderer renderer, TextFormat format) {
    TextLayout layout = graphics().layoutText(text, format);
    float twidth = renderer.adjustWidth(layout.width());
    float theight = renderer.adjustHeight(layout.height());
    CanvasImage image = graphics().createImage(twidth, theight);
    image.canvas().setStrokeColor(0xFFFFCCCC);
    image.canvas().strokeRect(0, 0, twidth, theight);
    renderer.render(image.canvas(), layout);
    return image;
  }

  protected static abstract class TextRenderer {
    public float adjustWidth(float width) { return width; }
    public float adjustHeight(float height) { return height; }
    public abstract void render(Canvas canvas, TextLayout text);
  }

  protected static TextRenderer STROKE = new TextRenderer() {
    public void render(Canvas canvas, TextLayout text) {
      canvas.setStrokeColor(0xFF6699CC);
      canvas.strokeText(text, 0, 0);
    }
  };
  protected static TextRenderer FILL = new TextRenderer() {
    public void render(Canvas canvas, TextLayout text) {
      canvas.setFillColor(0xFF6699CC);
      canvas.fillText(text, 0, 0);
    }
  };
  protected static TextRenderer SHADOW_UL = new TextRenderer() {
    public float adjustWidth(float width) { return width + 2; }
    public float adjustHeight(float height) { return height + 2; }
    public void render(Canvas canvas, TextLayout text) {
      canvas.setFillColor(0xFFCCCCCC);
      canvas.fillText(text, 0, 0);
      canvas.setFillColor(0xFF6699CC);
      canvas.fillText(text, 2, 2);
    }
  };
  protected static TextRenderer SHADOW_LR = new TextRenderer() {
    public float adjustWidth(float width) { return width + 2; }
    public float adjustHeight(float height) { return height + 2; }
    public void render(Canvas canvas, TextLayout text) {
      canvas.setFillColor(0xFFCCCCCC);
      canvas.fillText(text, 2, 2);
      canvas.setFillColor(0xFF6699CC);
      canvas.fillText(text, 0, 0);
    }
  };
  protected static TextRenderer OUTLINE_VEC = new TextRenderer() {
    public final float outlineWidth = 2;
    public float adjustWidth(float width) { return width + 2*outlineWidth; }
    public float adjustHeight(float height) { return height + 2*outlineWidth; }
    public void render(Canvas canvas, TextLayout text) {
      canvas.setStrokeWidth(2*outlineWidth);
      canvas.setStrokeColor(0xFF336699);
      canvas.setLineCap(Canvas.LineCap.ROUND);
      canvas.setLineJoin(Canvas.LineJoin.ROUND);
      canvas.strokeText(text, outlineWidth, outlineWidth);
      canvas.setFillColor(0xFF6699CC);
      canvas.fillText(text, outlineWidth, outlineWidth);
    }
  };
}
