/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.html;

import com.google.gwt.canvas.dom.client.CanvasPattern;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.webgl.client.WebGLTexture;

import pythagoras.f.MathUtil;

import playn.core.Asserts;
import playn.core.Image;
import playn.core.Pattern;
import playn.core.ResourceCallback;
import playn.core.gl.GLContext;
import playn.core.gl.ImageGL;

class HtmlImage extends ImageGL implements HtmlCanvas.Drawable {

  private static native boolean isComplete(ImageElement img) /*-{
    return img.complete;
  }-*/;

  private static native void fakeComplete(CanvasElement img) /*-{
   img.complete = true; // CanvasElement doesn't provide a 'complete' property
  }-*/;

  ImageElement img;
  CanvasElement canvas; // Used internally for getRGB

  HtmlImage(CanvasElement img) {
    this.canvas = img;
    fakeComplete(img);
    this.img = img.cast();
  }

  HtmlImage(ImageElement img) {
    this.img = img;
  }

  @Override
  public int height() {
    return img == null ? 0 : img.getHeight();
  }

  @Override
  public int width() {
    return img == null ? 0 : img.getWidth();
  }

  @Override
  public void addCallback(final ResourceCallback<? super Image> callback) {
    if (isReady()) {
      callback.done(this);
    } else {
      HtmlPlatform.addEventListener(img, "load", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent evt) {
          callback.done(HtmlImage.this);
        }
      }, false);
      HtmlPlatform.addEventListener(img, "error", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent evt) {
          callback.error(new RuntimeException("Error loading image " + img.getSrc()));
        }
      }, false);
    }
  }

  @Override
  public boolean isReady() {
    return isComplete(this.img);
  }

  @Override
  public Pattern toPattern() {
    Asserts.checkState(isReady(), "Cannot toPattern() a non-ready image");
    return new HtmlPattern(this);
  }

  @Override
  public void getRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    Asserts.checkState(isReady(), "Cannot getRgb() a non-ready image");

    if (canvas == null) {
        canvas = img.getOwnerDocument().createCanvasElement();
        canvas.setHeight(img.getHeight());
        canvas.setWidth(img.getWidth());
        canvas.getContext2d().drawImage(img, 0, 0);
        // img.getOwnerDocument().getBody().appendChild(canvas);
    }

    Context2d ctx = canvas.getContext2d();
    ImageData imageData = ctx.getImageData(startX, startY, width, height);
    CanvasPixelArray pixelData = imageData.getData();
    int i = 0;
    int dst = offset;
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x ++) {
          int r = pixelData.get(i++);
          int g = pixelData.get(i++);
          int b = pixelData.get(i++);
          int a = pixelData.get(i++);
          rgbArray [dst + x] = a << 24 | r << 16 | g << 8 | b;
        }
        dst += scanSize;
    }
  }

  @Override
  public Region subImage(float x, float y, float width, float height) {
    return new HtmlImageRegion(this, x, y, width, height);
  }

  @Override
  public void draw(Context2d ctx, float x, float y, float width, float height) {
    draw(ctx, 0, 0, width(), height(), x, y, width, height);
  }

  @Override
  public void draw(Context2d ctx, float sx, float sy, float sw, float sh,
            float dx, float dy, float dw, float dh) {
    ctx.drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh);
  }

  @Override
  public Image transform(BitmapTransformer xform) {
    return new HtmlImage(((HtmlBitmapTransformer) xform).transform(img));
  }

  @Override
  protected void updateTexture(GLContext ctx, Object tex) {
    ((HtmlGLContext)ctx).updateTexture((WebGLTexture)tex, img);
  }

  ImageElement subImageElement(float x, float y, float width, float height) {
    CanvasElement canvas = Document.get().createElement("canvas").<CanvasElement>cast();
    canvas.setWidth(MathUtil.iceil(width));
    canvas.setHeight(MathUtil.iceil(height));
    canvas.getContext2d().drawImage(img, x, y, width, height, 0, 0, width, height);
    return canvas.cast();
  }
}
