/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.java;

import static forplay.core.ForPlay.graphics;
import forplay.core.CanvasSurface;
import forplay.core.Surface;
import forplay.core.SurfaceLayer;

class JavaSurfaceLayer extends JavaLayer implements SurfaceLayer {

  private JavaImage img;
  private Surface surface;

  JavaSurfaceLayer(int width, int height) {
    super();
    img = (JavaImage) graphics().createImage(width, height);
    surface = new CanvasSurface(img.canvas());
  }

  @Override
  public void destroy() {
    super.destroy();
    surface = null;
    img = null;
  }

  @Override
  public Surface surface() {
    return surface;
  }

  @Override
  void paint(JavaCanvas canvas) {
    if (!visible()) return;

    canvas.save();
    transform(canvas);
    canvas.setAlpha(canvas.alpha() * alpha);
    canvas.drawImage(img, 0, 0);
    canvas.restore();
  }
}

