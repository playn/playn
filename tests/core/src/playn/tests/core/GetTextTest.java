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

import playn.core.CanvasImage;
import playn.core.ImageLayer;
import playn.core.Keyboard;
import playn.core.Pointer;
import playn.core.TextFormat;
import playn.core.TextLayout;
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

    final ImageLayer outputLayer = graphics().createImageLayer(formatText("...", false));
    graphics().rootLayer().addAt(outputLayer, 50, 150);

    final Callback<String> onGotText = new Callback<String>() {
      public void onSuccess(String text) {
        outputLayer.setImage(formatText(text == null ? "canceled" : text, false));
      }
      public void onFailure(Throwable cause) {
        outputLayer.setImage(formatText(cause.getMessage(), false));
      }
    };

    float x = 50;
    for (final Keyboard.TextType type : Keyboard.TextType.values()) {
      CanvasImage image = formatText(type.toString(), true);
      ImageLayer button = graphics().createImageLayer(image);
      button.addListener(new Pointer.Adapter() {
        public void onPointerStart(Pointer.Event event) {
          keyboard().getText(Keyboard.TextType.DEFAULT, "Enter " + type + " text:", "", onGotText);
        }
      });

      graphics().rootLayer().addAt(button, x, 100);
      x += image.width() + 10;
    }
  }

  protected CanvasImage formatText (String text, boolean border) {
    TextLayout layout = graphics().layoutText(text, new TextFormat());
    float margin = border ? 10 : 0;
    float width = layout.width()+2*margin, height = layout.height()+2*margin;
    CanvasImage image = graphics().createImage((int)width, (int)height);
    image.canvas().setStrokeColor(0xFF000000);
    image.canvas().setFillColor(0xFF000000);
    image.canvas().drawText(layout, margin, margin);
    if (border)
      image.canvas().strokeRect(0, 0, width-1, height-1);
    return image;
  }
}
