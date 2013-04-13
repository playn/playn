/**
 * Copyright 2010 The PlayN Authors
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

import com.google.gwt.canvas.dom.client.CanvasPattern;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;

import playn.core.gl.AbstractImageGL;
import playn.core.gl.GLPattern;

class HtmlPattern implements GLPattern {

  private final AbstractImageGL<?> image;
  private final ImageElement patimg;
  private final boolean repeatX, repeatY;

  HtmlPattern(HtmlImage image, boolean repeatX, boolean repeatY) {
    this(image, image.img, repeatX, repeatY);
  }

  HtmlPattern(AbstractImageGL<?> image, ImageElement patimg,
              boolean repeatX, boolean repeatY) {
    this.image = image;
    this.patimg = patimg;
    this.repeatX = repeatX;
    this.repeatY = repeatY;
  }

  public CanvasPattern pattern(Context2d ctx) {
    Context2d.Repetition repeat;
    if (repeatX) {
      if (repeatY) {
        repeat = Context2d.Repetition.REPEAT;
      } else {
        repeat = Context2d.Repetition.REPEAT_X;
      }
    } else if (repeatY) {
      repeat = Context2d.Repetition.REPEAT_Y;
    } else {
      return null;
    }
    return ctx.createPattern(patimg, repeat);
  }

  @Override
  public boolean repeatX() {
    return repeatX;
  }

  @Override
  public boolean repeatY() {
    return repeatY;
  }

  @Override
  public AbstractImageGL<?> image() {
    return image;
  }
}
