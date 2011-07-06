/**
 * Copyright 2011 The ForPlay Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.java;

import static forplay.core.ForPlay.*;

import forplay.core.Canvas;
import forplay.core.CanvasImage;
import forplay.core.CanvasLayer;

class JavaCanvasLayer extends JavaLayer implements CanvasLayer {

  private CanvasImage canvas;

  JavaCanvasLayer(int width, int height) {
    super();
    canvas = graphics().createImage(width, height);
  }

  @Override
  public Canvas canvas() {
    return canvas.canvas();
  }

  @Override
  public void destroy() {
    super.destroy();
    canvas = null;
  }

  @Override
  void paint(JavaCanvas surf) {
    if (!visible()) return;

    surf.save();
    transform(surf);
    surf.setAlpha(surf.alpha() * alpha);
    surf.drawImage(canvas, 0, 0);
    surf.restore();
  }
}
