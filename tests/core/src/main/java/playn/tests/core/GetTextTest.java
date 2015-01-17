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

import playn.core.*;
import playn.scene.*;
import react.Slot;

class GetTextTest extends Test {

  public GetTextTest (TestsGame game) {
    super(game, "GetText", "Tests mobile text entry support.");
  }

  @Override public void init() {
    String instructions = "Click one of the buttons below to display the text entry UI.";
    TextureLayer instLayer = new TextureLayer(game.ui.formatText(instructions, false));
    game.rootLayer.addAt(instLayer, 50, 50);

    String last = game.storage.getItem("last_text");
    if (last == null) last = "...";

    final TextureLayer outputLayer = new TextureLayer(game.ui.formatText(last, false));
    game.rootLayer.addAt(outputLayer, 50, 150);

    final Slot<String> onGotText = new Slot<String>() {
      public void onEmit(String text) {
        outputLayer.setTexture(game.ui.formatText(text == null ? "canceled" : text, false));
        if (text != null) game.storage.setItem("last_text", text);
      }
    };

    float x = 50;
    for (final Keyboard.TextType type : Keyboard.TextType.values()) {
      TextureLayer button = game.ui.createButton(type.toString(), new Runnable() {
        public void run() {
          game.input.getText(Keyboard.TextType.DEFAULT, "Enter " + type + " text:", "").
            onSuccess(onGotText);
        }
      });
      game.rootLayer.addAt(button, x, 100);
      x += button.width() + 10;
    }
  }
}
