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

class DialogTest extends Test {

  public DialogTest (TestsGame game) {
    super(game, "Dialog", "Tests system dialog & text entry support.");
  }

  @Override public void init() {
    float left = 50, x = left, y = 50;
    String instructions = "Click one of the buttons below to display the text entry UI:";
    ImageLayer instLayer = new ImageLayer(game.ui.formatText(instructions, false));
    game.rootLayer.addAt(instLayer, x, y);
    y += 20;

    String last = game.storage.getItem("last_text");
    if (last == null || last.length() == 0) last = "...";

    final ImageLayer outputLayer = new ImageLayer(game.ui.formatText(last, false));
    final Slot<Object> onDialogResult = new Slot<Object>() {
      public void onEmit(Object result) {
        String text = result == null ? "canceled" : String.valueOf(result);
        if (text.length() > 0) outputLayer.setTile(game.ui.formatText(text, false));
        if (result instanceof String) game.storage.setItem("last_text", (String)result);
      }
    };

    x = left;
    for (final Keyboard.TextType type : Keyboard.TextType.values()) {
      ImageLayer button = game.ui.createButton(type.toString(), new Runnable() {
        public void run() {
          game.input.getText(type, "Enter " + type + " text:", "").onSuccess(onDialogResult);
        }
      });
      game.rootLayer.addAt(button, x, y);
      x += button.width() + 10;
    }
    y += 50;

    game.rootLayer.addAt(outputLayer, left, y);
    y += 40;

    String instr2 = "Click a button below to show a system dialog:";
    game.rootLayer.addAt(new ImageLayer(game.ui.formatText(instr2, false)), left, y);
    y += 20;

    x = left;
    ImageLayer button = game.ui.createButton("OK Only", new Runnable() {
      public void run () {
        game.input.sysDialog("OK Only Dialog", "This in an OK only dialog.\n" +
                             "With hard line broken text.\n\n" +
                             "And hopefully a blank line before this one.", "Cool!", null).
          onSuccess(onDialogResult);
      }
    });
    game.rootLayer.addAt(button, x, y);
    x += button.width() + 10;

    button = game.ui.createButton("OK Cancel", new Runnable() {
      public void run () {
        game.input.sysDialog("OK Cancel Dialog", "This is an OK Cancel dialog.\n" +
                             "With hard line breaks.\n\n" +
                             "And hopefully a blank line before this one.", "Cool!", "Yuck!").
          onSuccess(onDialogResult);
      }
    });
    game.rootLayer.addAt(button, x, y);
    x += button.width() + 10;
    y += 50;
  }
}
