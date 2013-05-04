/**
 * Copyright 2012 The PlayN Authors
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
package playn.core.gl;

import playn.core.Image;
import playn.core.InternalTransform;
import playn.core.Pattern;
import playn.core.Tint;
import playn.core.util.Callback;

public class ImageRegionGL<GC> extends AbstractImageGL<GC> implements Image.Region {

  protected final AbstractImageGL<GC> parent;
  protected float x, y;
  protected float width, height;
  protected int tex;

  public ImageRegionGL(AbstractImageGL<GC> parent, float x, float y, float width, float height) {
    super(parent.ctx);
    this.parent = parent;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  @Override
  public Scale scale() {
    return parent.scale();
  }

  @Override
  public int ensureTexture() {
    if (!isReady()) {
      return 0;
    } else if (repeatX || repeatY) {
      return (tex > 0) ? tex : (tex = scaleTexture());
    } else {
      return parent.ensureTexture();
    }
  }

  @Override
  public void clearTexture() {
    if (tex > 0) {
      ctx.destroyTexture(tex);
      tex = 0;
    }
  }

  @Override
  public void reference() {
    parent.reference();
  }

  @Override
  public void release() {
    parent.release();
  }

  @Override
  public float x() {
    return x;
  }

  @Override
  public float y() {
    return y;
  }

  @Override
  public float width() {
    return width;
  }

  @Override
  public float height() {
    return height;
  }

  @Override
  public void setBounds(float x, float y, float width, float height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  @Override
  public Image parent() {
    return parent;
  }

  @Override
  public boolean isReady() {
    return parent.isReady();
  }

  @Override
  public void addCallback(final Callback<? super Image> callback) {
    parent.addCallback(new Callback<Image>() {
      public void onSuccess(Image image) {
        callback.onSuccess(ImageRegionGL.this);
      }
      public void onFailure(Throwable err) {
        callback.onFailure(err);
      }
    });
  }

  @Override
  public Region subImage(float x, float y, float width, float height) {
    // TODO: clamp width, height to our bounds?
    return parent.subImage(x()+x, y()+y, width, height);
  }

  @Override
  public Pattern toPattern() {
    return parent.toSubPattern(this, repeatX, repeatY, x, y, width, height);
  }

  @Override
  public void getRgb(int startX, int startY, int width, int height, int[] rgbArray, int offset,
                     int scanSize) {
    parent.getRgb(startX + (int) x, startY + (int) y, width, height, rgbArray, offset, scanSize);
  }

  @Override
  public Image transform(BitmapTransformer xform) {
    throw new UnsupportedOperationException(
      "Cannot transform subimages. Transform the parent and then obtain a subimage of that.");
  }

  @Override
  public void draw(GC gc, float dx, float dy, float dw, float dh) {
    draw(gc, dx, dy, dw, dh, 0, 0, width, height);
  }

  @Override
  public void draw(GC gc, float dx, float dy, float dw, float dh,
                   float sx, float sy, float sw, float sh) {
    parent.draw(gc, dx, dy, dw, dh, x+sx, y+sy, sw, sh);
  }

  @Override
  void draw(GLShader shader, InternalTransform xform, int tint,
            float dx, float dy, float dw, float dh, float sx, float sy, float sw, float sh) {
    if (repeatX || repeatY) {
      // if we're repeating, then we have our own texture and want to draw it normally
      super.draw(shader, xform, tint, dx, dy, dw, dh, sx, sy, sw, sh);
    } else {
      float texWidth = (tex > 0) ? width : parent.width();
      float texHeight = (tex > 0) ? height : parent.height();
      sx += x();
      sy += y();
      parent.drawImpl(shader, xform, ensureTexture(), tint, dx, dy, dw, dh,
                      sx / texWidth, sy / texHeight, (sx + sw) / texWidth, (sy + sh) / texHeight);
    }
  }

  @Override
  protected Pattern toSubPattern(AbstractImageGL<?> image, boolean repeatX, boolean repeatY,
                                 float x, float y, float width, float height) {
    throw new AssertionError(); // this should never be called
  }

  private int scaleTexture() {
    int scaledWidth = scale().scaledCeil(this.width);
    int scaledHeight = scale().scaledCeil(this.height);

    // GL requires pow2 on axes that repeat
    int width = GLUtil.nextPowerOfTwo(scaledWidth), height = GLUtil.nextPowerOfTwo(scaledHeight);

    // width/height == 0 => already a power of two.
    if (width == 0)
      width = scaledWidth;
    if (height == 0)
      height = scaledHeight;

    // our source image is our parent's texture
    int tex = parent.ensureTexture();

    // create our texture and point a new framebuffer at it
    int reptex = ctx.createTexture(width, height, repeatX, repeatY, mipmapped);
    int fbuf = ctx.createFramebuffer(reptex);
    ctx.pushFramebuffer(fbuf, width, height);
    try {
      // render the parent texture into the framebuffer properly scaled
      ctx.clear(0, 0, 0, 0);
      float tw = parent.width(), th = parent.height();
      float sl = this.x, st = this.y, sr = sl + this.width, sb = st + this.height;
      GLShader shader = ctx.quadShader(null).prepareTexture(tex, Tint.NOOP_TINT);
      shader.addQuad(ctx.createTransform(), 0, height, width, 0,
                     sl / tw, st / th, sr / tw, sb / th);
      shader.flush();
      // if we're mipmapped, we can now generate our mipmaps
      if (mipmapped) ctx.generateMipmap(reptex);
      return reptex;

    } finally {
      // we no longer need this framebuffer; rebind the previous framebuffer and delete ours
      ctx.popFramebuffer();
      ctx.deleteFramebuffer(fbuf);
    }
  }
}
