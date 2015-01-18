/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.tests.core;

import playn.core.*;
import playn.scene.*;
import playn.scene.Pointer;

public class UI {

  private final TestsGame game;
  public final TextFormat BUTTON_FMT;
  public final TextFormat TEXT_FMT;

  public UI (TestsGame game) {
    this.game = game;
    BUTTON_FMT = new TextFormat(new Font("Helvetica", 24));
    TEXT_FMT = new TextFormat(new Font("Helvetica", 12));
  }

  public Texture formatText (TextFormat format, String text, boolean border) {
    TextLayout layout = game.graphics.layoutText(text, format);
    float margin = border ? 10 : 0;
    float width = layout.size.width()+2*margin, height = layout.size.height()+2*margin;
    Canvas canvas = game.graphics.createCanvas(width, height);
    if (border) canvas.setFillColor(0xFFCCCCCC).fillRect(0, 0, canvas.width, canvas.height);
    canvas.setFillColor(0xFF000000).fillText(layout, margin, margin);
    if (border) canvas.setStrokeColor(0xFF000000).strokeRect(0, 0, width-1, height-1);
    return canvas.toTexture();
  }

  public Texture formatText (String text, boolean border) {
    return formatText(TEXT_FMT, text, border);
  }

  public Texture wrapText(String text, float width, TextBlock.Align align) {
    TextLayout[] layouts = game.graphics.layoutText(text, TEXT_FMT, new TextWrap(width));
    Canvas canvas = new TextBlock(layouts).toCanvas(game.graphics, align, 0xFF000000);
    return canvas.toTexture();
  }

  public Texture formatButton (String label) {
    return formatText(BUTTON_FMT, label, true);
  }

  public ImageLayer createButton (String label, final Runnable onClick) {
    ImageLayer layer = new ImageLayer(formatButton(label));
    layer.events().connect(new Pointer.Listener() {
      @Override public void onStart (Pointer.Interaction iact) { onClick.run(); }
    });
    return layer;
  }
}
