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
package playn.html;

import playn.core.CanvasSurface;
import playn.core.Surface;
import playn.core.SurfaceImage;
import playn.core.gl.GLContext;
import playn.core.gl.Scale;

public class HtmlSurfaceImageCanvas extends HtmlCanvasImage implements SurfaceImage {

  private final CanvasSurface surface;

  public HtmlSurfaceImageCanvas(GLContext ctx, Scale scale, HtmlCanvas canvas) {
    super(ctx, scale, canvas);
    surface = new CanvasSurface(canvas);
  }

  @Override
  public Surface surface() {
    return surface;
  }

  @Override
  public void destroy() {
    // nothing to see here, move it along
  }
}
