/**
 * Copyright 2011 The PlayN Authors
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
package playn.core.gl;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.CanvasLayer;

/**
 * A temporary {@link CanvasLayer} implementation used until we eliminate the whole business.
 */
public class CanvasLayerGL extends ImageLayerGL implements CanvasLayer
{
  public CanvasLayerGL(GLContext ctx, CanvasImage image) {
    super(ctx, (ImageGL)image);
  }

  @Override
  public Canvas canvas() {
    return ((CanvasImage) image()).canvas();
  }
}
