/**
 * Copyright 2013 The PlayN Authors
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
package playn.core.canvas;

import playn.core.Canvas;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.InternalTransform;
import playn.core.Pattern;

public class ImageLayerCanvas extends LayerCanvas implements ImageLayer {

  private float width, height;
  private boolean widthSet, heightSet;

  private Image img;
  private Pattern pattern;
  private boolean patternRepeatX;
  private boolean patternRepeatY;

  public ImageLayerCanvas(InternalTransform xform) {
    super(xform);
  }

  @Override
  public Image image() {
    return img;
  }

  @Override
  public ImageLayer setImage(Image img) {
    this.img = img;
    return this;
  }

  @Override
  public float width() {
    if (widthSet) return width;
    assert img != null : "Image has not yet been set";
    return img.width();
  }

  @Override
  public float height() {
    if (heightSet) return height;
    assert img != null : "Image has not yet been set";
    return img.height();
  }

  @Override
  public float scaledWidth() {
    return scaleX() * width();
  }

  @Override
  public float scaledHeight() {
    return scaleY() * height();
  }

  @Override
  public void setHeight(float height) {
    assert height >= 0 : "Height must be >= 0";
    heightSet = true;
    this.height = height;
  }

  @Override
  public void setWidth(float width) {
    assert width >= 0 : "Width must be >= 0";
    widthSet = true;
    this.width = width;
  }

  @Override
  public void setSize(float width, float height) {
    assert width >= 0 && height >= 0 :
      "Width and height must be >= 0 (got " + width + "x" + height + ")";
    widthSet = true;
    this.width = width;
    heightSet = true;
    this.height = height;
  }

  @Override
  public void clearHeight() {
    heightSet = false;
  }

  @Override
  public void clearWidth() {
    widthSet = false;
  }

  @Override
  public void paint(Canvas canvas, float parentAlpha) {
    if (!visible() || img == null || !img.isReady()) return;

    canvas.save();
    canvas.setAlpha(parentAlpha * alpha());
    transform(canvas);

    float width = width(), height = height();
    boolean repX = img.repeatX(), repY = img.repeatY();
    if (repX || repY) {
      if (pattern == null || repX != patternRepeatX || repY != patternRepeatY) {
        patternRepeatX = repX;
        patternRepeatY = repY;
        pattern = img.toPattern();
      }
      canvas.setFillPattern(pattern);
      float xScale = repX ? 1 : width / img.width(), yScale = repY ? 1 : height / img.height();
      canvas.scale(xScale, yScale);
      canvas.fillRect(0, 0, width / xScale, height / yScale);

    } else {
      canvas.drawImage(img, 0, 0, width, height);
    }

    canvas.restore();
  }
}
