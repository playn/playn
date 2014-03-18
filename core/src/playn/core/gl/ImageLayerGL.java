/**
 * Copyright 2010 The PlayN Authors
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
package playn.core.gl;

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.InternalTransform;
import playn.core.Tint;

public class ImageLayerGL extends LayerGL implements ImageLayer {

  private float width, height;
  private boolean widthSet, heightSet;
  private AbstractImageGL<?> img;

  public ImageLayerGL(GLContext ctx) {
    super(ctx);
  }

  @Override
  public void destroy() {
    super.destroy();
    setImage(null);
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
  public Image image() {
    return img;
  }

  @Override
  public ImageLayer setImage(Image img) {
    assert img == null || img instanceof AbstractImageGL<?>;
    // avoid releasing and rereferencing image if nothing changes
    if (this.img != img) {
      if (this.img != null)
        this.img.release();
      this.img = (AbstractImageGL<?>) img;
      if (this.img != null)
        this.img.reference();
    }
    return this;
  }

  @Override
  public void setWidth(float width) {
    assert width >= 0 : "Width must be >= 0";
    widthSet = true;
    this.width = width;
  }

  @Override
  public void setHeight(float height) {
    assert height >= 0 : "Height must be >= 0";
    heightSet = true;
    this.height = height;
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
  public void paint(InternalTransform curTransform, int curTint, GLShader curShader) {
    if (visible() && img != null) {
      if (tint != Tint.NOOP_TINT)
        curTint = Tint.combine(curTint, tint);
      img.draw((shader == null) ? curShader : shader, localTransform(curTransform), curTint,
               0, 0, width(), height());
    }
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
}
