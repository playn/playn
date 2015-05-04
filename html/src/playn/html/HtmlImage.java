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

import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;

import playn.core.*;
import react.RFuture;
import react.RPromise;

public class HtmlImage extends ImageImpl {

  private static native boolean isComplete (ImageElement img) /*-{ return img.complete; }-*/;

  private ImageElement img;
  CanvasElement canvas; // used for get/setRGB and by HtmlCanvas

  public HtmlImage (Graphics gfx, Scale scale, CanvasElement elem, String source) {
    super(gfx, scale, elem.getWidth(), elem.getHeight(), source, elem);
    this.canvas = elem;
  }

  public HtmlImage (Graphics gfx, Scale scale, ImageElement elem, String source) {
    super(gfx, RPromise.<Image>create(), scale, elem.getWidth(), elem.getHeight(), source);
    img = elem;

    // we know that in this case, our state is a promise
    final RPromise<Image> pstate = ((RPromise<Image>)state);
    if (isComplete(img)) pstate.succeed(this);
    else {
      HtmlInput.addEventListener(img, "load", new EventHandler() {
        @Override public void handleEvent (NativeEvent evt) {
          pixelWidth = img.getWidth();
          pixelHeight = img.getHeight();
          pstate.succeed(HtmlImage.this);
        }
      }, false);
      HtmlInput.addEventListener(img, "error", new EventHandler() {
        @Override public void handleEvent(NativeEvent evt) {
          pstate.fail(new RuntimeException("Error loading image " + img.getSrc()));
        }
      }, false);
    }
  }

  public HtmlImage (Graphics gfx, Throwable error) {
    super(gfx, RFuture.<Image>failure(error), Scale.ONE, 50, 50, "<error>");
    setBitmap(createErrorBitmap(pixelWidth, pixelHeight));
  }

  /** Returns the {@link ImageElement} that underlies this image. This is for games that need to
    * write custom backend code to do special stuff. No promises are made, caveat coder. */
  public ImageElement imageElement () { return img; }

  HtmlImage preload (int prePixelWidth, int prePixelHeight) {
    pixelWidth = prePixelWidth;
    pixelHeight = prePixelHeight;
    return this;
  }

  @Override public Pattern createPattern (boolean repeatX, boolean repeatY) {
    assert isLoaded() : "Cannot createPattern() a non-ready image";
    return new HtmlPattern(img, repeatX, repeatY);
  }

  @Override public void getRgb(int startX, int startY, int width, int height,
                               int[] rgbArray, int offset, int scanSize) {
    assert isLoaded() : "Cannot getRgb() a non-ready image";

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

  @Override public void setRgb(int startX, int startY, int width, int height,
                               int[] rgbArray, int offset, int scanSize) {
    Context2d ctx = canvas.getContext2d();
    ImageData imageData = ctx.createImageData(width, height);
    CanvasPixelArray pixelData = imageData.getData();
    int i = 0;
    int dst = offset;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x ++) {
        int argb = rgbArray[dst + x];
        pixelData.set(i++, (argb >> 16) & 255);
        pixelData.set(i++, (argb >> 8) & 255);
        pixelData.set(i++, (argb) & 255);
        pixelData.set(i++, (argb >> 24) & 255);
      }
      dst += scanSize;
    }
    ctx.putImageData(imageData, startX, startY);
  }

  @Override public Image transform (BitmapTransformer xform) {
    return new HtmlImage(gfx, scale, ((HtmlBitmapTransformer) xform).transform(img), source);
  }

  @Override public void draw(Object ctx, float x, float y, float width, float height) {
    ((Context2d)ctx).drawImage(img, x, y, width, height);
  }

  @Override public void draw(Object ctx, float dx, float dy, float dw, float dh,
                             float sx, float sy, float sw, float sh) {
    // adjust our source rect to account for the scale factor
    sx *= scale.factor;
    sy *= scale.factor;
    sw *= scale.factor;
    sh *= scale.factor;
    ((Context2d)ctx).drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh);
  }

  @Override public String toString () {
    return "Image[src=" + source + ", scale=" + scale + ", size=" + width() + "x" + height() +
      ", psize=" + pixelWidth + "x" + pixelHeight + ", img=" + img + ", canvas=" + canvas + "]";
  }

  @Override protected void setBitmap (Object bitmap) {
    img = (ImageElement)bitmap;
  }

  @Override protected Object createErrorBitmap (int pixelWidth, int pixelHeight) {
    ImageElement img = Document.get().createImageElement();
    img.setWidth(pixelWidth);
    img.setHeight(pixelHeight);
    return img;
  }

  @Override protected void upload (Graphics gfx, Texture tex) {
    ((HtmlGraphics)gfx).updateTexture(tex.id, img);
  }
}
