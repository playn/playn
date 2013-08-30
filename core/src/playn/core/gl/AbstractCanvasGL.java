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
package playn.core.gl;

import playn.core.AbstractCanvas;
import playn.core.Canvas;
import playn.core.Image;

/**
 * A base class for {@link Canvas} implementations for GL backends. This mainly takes care of
 * drawing images, but also provides a dirty tracking mechanism, since nearly all canvas
 * implementations need one.
 */
public abstract class AbstractCanvasGL<GC> extends AbstractCanvas {

  protected boolean isDirty;

  public boolean dirty() {
    return isDirty;
  }

  public void clearDirty() {
    isDirty = false;
  }

  @Override
  public Canvas drawImage(Image img, float x, float y) {
    return drawImage(img, x, y, img.width(), img.height());
  }

  @Override
  public Canvas drawImageCentered(Image img, float x, float y) {
    return drawImage(img, x - img.width()/2, y - img.height()/2);
  }

  @Override
  public Canvas drawImage(Image img, float x, float y, float w, float h) {
    @SuppressWarnings("unchecked") AbstractImageGL<GC> d = (AbstractImageGL<GC>) img;
    d.draw(gc(), x, y, w, h);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawImage(Image img, float dx, float dy, float dw, float dh,
                          float sx, float sy, float sw, float sh) {
    @SuppressWarnings("unchecked") AbstractImageGL<GC> d = (AbstractImageGL<GC>) img;
    d.draw(gc(), dx, dy, dw, dh, sx, sy, sw, sh);
    isDirty = true;
    return this;
  }

  protected AbstractCanvasGL(float width, float height) {
    super(width, height);
  }

  protected abstract GC gc();
}
