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

import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLTexture;

import static com.google.gwt.webgl.client.WebGLRenderingContext.COLOR_ATTACHMENT0;
import static com.google.gwt.webgl.client.WebGLRenderingContext.FRAMEBUFFER;
import static com.google.gwt.webgl.client.WebGLRenderingContext.RGBA;
import static com.google.gwt.webgl.client.WebGLRenderingContext.TEXTURE_2D;
import static com.google.gwt.webgl.client.WebGLRenderingContext.UNSIGNED_BYTE;

import playn.core.Image;
import playn.core.ResourceCallback;
import playn.core.gl.GLContext;
import playn.core.gl.GLUtil;
import playn.core.gl.ImageGL;

class HtmlImage extends ImageGL {

  private static native boolean isComplete(ImageElement img) /*-{
    return img.complete;
  }-*/;

  private static native void fakeComplete(CanvasElement img) /*-{
   img.complete = true; // CanvasElement doesn't provide a 'complete' property
  }-*/;

  ImageElement img;

  // only used in the WebGL renderer.
  protected WebGLTexture tex, reptex;

  HtmlImage(CanvasElement img) {
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
  public void addCallback(final ResourceCallback<Image> callback) {
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
  public WebGLTexture ensureTexture(GLContext ctx, boolean repeatX, boolean repeatY) {
    // Create requested textures if loaded.
    if (isReady()) {
      if (repeatX || repeatY) {
        scaleTexture((HtmlGLContext) ctx, repeatX, repeatY);
        return reptex;
      } else {
        loadTexture((HtmlGLContext) ctx);
        return tex;
      }
    }

    return null;
  }

  @Override
  public void clearTexture(GLContext ctx) {
    if (tex != null) {
      ctx.destroyTexture(tex);
      tex = null;
    }
    if (reptex != null) {
      ctx.destroyTexture(reptex);
      reptex = null;
    }
  }

  private void loadTexture(HtmlGLContext ctx) {
    if (tex != null)
      return;
    tex = ctx.createTexture(false, false);
    ctx.updateTexture(tex, img);
  }

  private void scaleTexture(HtmlGLContext ctx, boolean repeatX, boolean repeatY) {
    if (reptex != null)
      return;

    // GL requires pow2 on axes that repeat
    int width = GLUtil.nextPowerOfTwo(width()), height = GLUtil.nextPowerOfTwo(height());
    reptex = ctx.createTexture(width, height, repeatX, repeatY);

    // no need to scale if our source data is already a power of two
    if ((width == 0) && (height == 0)) {
      ctx.updateTexture(reptex, img);
      return;
    }

    // otherwise we need to scale our non-repeated texture, which we'll load normally
    loadTexture(ctx);

    // width/height == 0 => already a power of two.
    if (width == 0)
      width = width();
    if (height == 0)
      height = height();

    // point a new framebuffer at it
    WebGLFramebuffer fbuf = ctx.gl.createFramebuffer();
    ctx.gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, reptex, 0);
    ctx.gl.bindTexture(TEXTURE_2D, reptex);

    // render the non-repeated texture into the framebuffer properly scaled
    ctx.drawTexture(tex, width(), height(), HtmlInternalTransform.IDENTITY,
                    0, height, width, -height, false, false, 1);
    ctx.bindFramebuffer();

    ctx.gl.deleteFramebuffer(fbuf);
  }
}
