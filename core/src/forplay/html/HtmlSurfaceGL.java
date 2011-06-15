/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.html;

import static com.google.gwt.webgl.client.WebGLRenderingContext.COLOR_BUFFER_BIT;

import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLTexture;

import forplay.core.Asserts;
import forplay.core.Image;
import forplay.core.Pattern;
import forplay.core.Surface;
import forplay.core.Transform;

import java.util.ArrayList;
import java.util.List;

class HtmlSurfaceGL implements Surface {

  private final HtmlGraphicsGL gfx;
  private final WebGLFramebuffer fbuf;
  private final int width;
  private final int height;
  private final List<Transform> transformStack = new ArrayList<Transform>();

  private int fillColor;
  private HtmlPattern fillPattern;

  HtmlSurfaceGL(HtmlGraphicsGL gfx, WebGLFramebuffer fbuf, int width, int height) {
    this.gfx = gfx;
    this.fbuf = fbuf;
    this.width = width;
    this.height = height;
    transformStack.add(new Transform(Transform.IDENTITY));
  }

  @Override
  public void clear() {
    gfx.bindFramebuffer(fbuf, width, height);

    gfx.gl.clearColor(0, 0, 0, 0);
    gfx.gl.clear(COLOR_BUFFER_BIT);
  }

  @Override
  public void drawImage(Image image, float x, float y) {
    drawImage(image, x, y, image.width(), image.height());
  }

  @Override
  public void drawImage(Image image, float x, float y, float dw, float dh) {
    gfx.bindFramebuffer(fbuf, width, height);

    Asserts.checkArgument(image instanceof HtmlImage);
    HtmlImage himage = (HtmlImage) image;

    if (himage.isReady()) {
      WebGLTexture tex = himage.ensureTexture(gfx, false, false);
      if (tex != null) {
        gfx.drawTexture(tex, image.width(), image.height(), topTransform(), x, y, dw, dh, false,
            false, 1);
      }
    }
  }

  @Override
  public void drawImage(Image image, float dx, float dy, float dw, float dh, float sx, float sy,
      float sw, float sh) {
    gfx.bindFramebuffer(fbuf, width, height);

    Asserts.checkArgument(image instanceof HtmlImage);
    HtmlImage himage = (HtmlImage) image;

    if (himage.isReady()) {
      WebGLTexture tex = himage.ensureTexture(gfx, false, false);
      if (tex != null) {
        gfx.drawTexture(tex, image.width(), image.height(), topTransform(), dx, dy, dw, dh, sx, sy,
            sw, sh, 1);
      }
    }
  }

  public void drawImageCentered(Image img, float x, float y) {
    drawImage(img, x - img.width()/2, y - img.height()/2);
  }

  @Override
  public void drawLine(float x0, float y0, float x1, float y1, float width) {
    gfx.bindFramebuffer(fbuf, this.width, this.height);

    float dx = x1 - x0, dy = y1 - y0;
    float len = (float) Math.sqrt(dx * dx + dy * dy);
    dx = dx * (width / 2) / len;
    dy = dy * (width / 2) / len;

    float[] pos = new float[8];
    pos[0] = x0 - dy; pos[1] = y0 + dx;
    pos[2] = x1 - dy; pos[3] = y1 + dx;
    pos[4] = x1 + dy; pos[5] = y1 - dx;
    pos[6] = x0 + dy; pos[7] = y0 - dx;
    gfx.fillPoly(topTransform(), pos, fillColor, 1);
  }

  @Override
  public void fillRect(float x, float y, float width, float height) {
    gfx.bindFramebuffer(fbuf, this.width, this.height);

    if (fillPattern != null) {
      HtmlImage image = fillPattern.image;
      WebGLTexture tex = image.ensureTexture(gfx, true, true);
      gfx.fillRect(topTransform(), x, y, width, height, image.width(), image.height(), tex, 1);
    } else {
      gfx.fillRect(topTransform(), x, y, width, height, fillColor, 1);
    }
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public void restore() {
    Asserts.checkState(transformStack.size() > 1, "Unbalanced save/restore");
    transformStack.remove(transformStack.size() - 1);
  }

  @Override
  public void rotate(float angle) {
    float sr = (float) Math.sin(angle);
    float cr = (float) Math.cos(angle);
    transform(cr, sr, -sr, cr, 0, 0);
  }

  @Override
  public void save() {
    transformStack.add(new Transform(topTransform()));
  }

  @Override
  public void scale(float sx, float sy) {
    topTransform().scale(sx, sy);
  }

  @Override
  public void setTransform(float m00, float m01, float m10, float m11, float tx, float ty) {
    topTransform().set(m00, m01, m10, m11, tx, ty);
  }

  public void setFillColor(int color) {
    // TODO: Add it to the state stack.
    this.fillColor = color;
    this.fillPattern = null;
  }

  @Override
  public void setFillPattern(Pattern pattern) {
    // TODO: Add it to the state stack.
    Asserts.checkArgument(pattern instanceof HtmlPattern);
    this.fillPattern = (HtmlPattern) pattern;
  }

  @Override
  public void transform(float m00, float m01, float m10, float m11, float tx, float ty) {
    topTransform().transform(m00, m01, m10, m11, tx, ty);
  }

  @Override
  public void translate(float x, float y) {
    topTransform().translate(x, y);
  }

  @Override
  public int width() {
    return width;
  }

  private Transform topTransform() {
    return transformStack.get(transformStack.size() - 1);
  }
}
