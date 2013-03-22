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

import playn.core.ImageLayer;
import playn.core.Keyboard;
import playn.core.util.Callback;
import static playn.core.PlayN.*;

class GetTextTest extends Test {

  @Override
  public String getName() {
    return "GetTextTest";
  }

  @Override
  public String getDescription() {
    return "Tests mobile text entry support.";
  }

  @Override
  public void init() {
    String instructions = "Click one of the buttons below to display the text entry UI.";
    ImageLayer instLayer = graphics().createImageLayer(formatText(instructions, false));
    graphics().rootLayer().addAt(instLayer, 50, 50);

    String last = storage().getItem("last_text");
    if (last == null) last = "...";

    final ImageLayer outputLayer = graphics().createImageLayer(formatText(last, false));
    graphics().rootLayer().addAt(outputLayer, 50, 150);

    final Callback<String> onGotText = new Callback<String>() {
      public void onSuccess(String text) {
        outputLayer.setImage(formatText(text == null ? "canceled" : text, false));
        if (text != null) storage().setItem("last_text", text);
      }
      public void onFailure(Throwable cause) {
        outputLayer.setImage(formatText(cause.getMessage(), false));
      }
    };

    float x = 50;
    for (final Keyboard.TextType type : Keyboard.TextType.values()) {
      ImageLayer button = createButton(type.toString(), new Runnable() {
        public void run() {
          keyboard().getText(Keyboard.TextType.DEFAULT, "Enter " + type + " text:", "", onGotText);
        }
      });
      graphics().rootLayer().addAt(button, x, 100);
      x += button.width() + 10;
    }
  }
}
