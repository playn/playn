/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.html;

import static com.google.gwt.webgl.client.WebGLRenderingContext.COLOR_ATTACHMENT0;
import static com.google.gwt.webgl.client.WebGLRenderingContext.FRAMEBUFFER;
import static com.google.gwt.webgl.client.WebGLRenderingContext.RGBA;
import static com.google.gwt.webgl.client.WebGLRenderingContext.TEXTURE_2D;
import static com.google.gwt.webgl.client.WebGLRenderingContext.UNSIGNED_BYTE;

import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLTexture;

import forplay.core.Asserts;
import forplay.core.Image;
import forplay.core.ResourceCallback;
import forplay.core.Transform;

class HtmlImage implements Image {

  private static native boolean isComplete(ImageElement img) /*-{
    return img.complete;
  }-*/;

  ImageElement img;

  // only used in the WebGL renderer.
  private WebGLTexture tex, pow2tex;

  HtmlImage(CanvasElement img) {
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
  public void replaceWith(Image image) {
    Asserts.checkArgument(image instanceof HtmlImage);
    img = ((HtmlImage) image).img;
  }

  @Override
  public int width() {
    return img == null ? 0 : img.getWidth();
  }

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
          callback.error(new RuntimeException());
        }
      }, false);
    }
  }

  @Override
  public boolean isReady() {
    return isComplete(this.img);
  }

  /*
   * Clears textures associated with this image. This does not destroy the image -- a subsequent
   * call to ensureTexture() will recreate them.
   */
  void clearTexture(HtmlGraphicsGL gfx) {
    if (pow2tex == tex) {
      pow2tex = null;
    }

    if (tex != null) {
      gfx.destroyTexture(tex);
      tex = null;
    }
    if (pow2tex != null) {
      gfx.destroyTexture(pow2tex);
      pow2tex = null;
    }
  }

  WebGLTexture ensureTexture(HtmlGraphicsGL gfx, boolean repeatX, boolean repeatY) {
    // Create requested textures if loaded.
    if (isReady()) {
      if (repeatX || repeatY) {
        scaleTexture(gfx, repeatX, repeatY);
        return pow2tex;
      } else {
        loadTexture(gfx);
        return tex;
      }
    }

    return null;
  }

  private void loadTexture(HtmlGraphicsGL gfx) {
    if (tex != null) {
      return;
    }

    tex = gfx.createTexture(false, false);
    gfx.updateTexture(tex, img);
  }

  private void scaleTexture(HtmlGraphicsGL gfx, boolean repeatX, boolean repeatY) {
    if (pow2tex != null) {
      return;
    }

    // Ensure that 'tex' is loaded. We use it below.
    loadTexture(gfx);

    // GL requires pow2 on axes that repeat.
    int width = nextPowerOfTwo(width()), height = nextPowerOfTwo(height());

    // Don't scale if it's already a power of two.
    if ((width == 0) && (height == 0)) {
      pow2tex = tex;
      return;
    }

    // width/height == 0 => already a power of two.
    if (width == 0) {
      width = width();
    }
    if (height == 0) {
      height = height();
    }

    // Create the pow2 texture.
    pow2tex = gfx.createTexture(repeatX, repeatY);
    gfx.gl.bindTexture(TEXTURE_2D, pow2tex);
    gfx.gl.texImage2D(TEXTURE_2D, 0, RGBA, width, height, 0, RGBA, UNSIGNED_BYTE, null);

    // Point a new framebuffer at it.
    WebGLFramebuffer fbuf = gfx.gl.createFramebuffer();
    gfx.bindFramebuffer(fbuf, width, height);
    gfx.gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, pow2tex, 0);

    // Render the scaled texture into the framebuffer.
    // (rebind the texture because gfx.bindFramebuffer() may have bound it when flushing)
    gfx.gl.bindTexture(TEXTURE_2D, pow2tex);
    gfx.drawTexture(tex, width(), height(), Transform.IDENTITY, 0, height, width, -height, false,
        false, 1);
    gfx.flush();
    gfx.bindFramebuffer();

    gfx.gl.deleteFramebuffer(fbuf);
  }

  /**
   * Returns the next largest power of two, or zero if x is already a power of two.
   * 
   * TODO(jgw): Is there no better way to do this than all this bit twiddling?
   */
  private int nextPowerOfTwo(int x) {
    Asserts.checkArgument(x < 0x10000);

    int bit = 0x8000, highest = -1, count = 0;
    for (int i = 15; i >= 0; --i, bit >>= 1) {
      if ((x & bit) != 0) {
        ++count;
        if (highest == -1) {
          highest = i;
        }
      }
    }
    if (count <= 1) {
      return 0;
    }
    return 1 << (highest + 1);
  }
}
