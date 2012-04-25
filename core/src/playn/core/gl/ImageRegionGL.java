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

import pythagoras.f.MathUtil;

import playn.core.Image;
import playn.core.Pattern;
import playn.core.ResourceCallback;

public abstract class ImageRegionGL extends ImageGL implements Image.Region {

  protected final ImageGL parent;
  protected final float x, y;
  protected final float width, height;

  public ImageRegionGL(ImageGL parent, float x, float y, float width, float height) {
    this.parent = parent;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  @Override
  public Object ensureTexture(GLContext ctx, boolean repeatX, boolean repeatY) {
    if (!isReady()) {
      return null;
    } else if (repeatX || repeatY) {
      scaleTexture(ctx, repeatX, repeatY);
      return reptex;
    } else {
      return parent.ensureTexture(ctx, repeatX, repeatY);
    }
  }

  @Override
  public void clearTexture(GLContext ctx) {
    parent.clearTexture(ctx);
  }

  @Override
  public void reference(GLContext ctx) {
    parent.reference(ctx);
  }

  @Override
  public void release(GLContext ctx) {
    parent.release(ctx);
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
  public int width() {
    return (int) width;
  }

  @Override
  public int height() {
    return (int) height;
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
  public void addCallback(final ResourceCallback<? super Image> callback) {
    parent.addCallback(new ResourceCallback<Image>() {
      public void done(Image image) {
        callback.done(ImageRegionGL.this);
      }
      public void error(Throwable err) {
        callback.error(err);
      }
    });
  }

  @Override
  public Region subImage(float x, float y, float width, float height) {
    // TODO: clamp width, height to our bounds?
    return parent.subImage(x()+x, y()+y, width, height);
  }

  @Override
  public Image transform(BitmapTransformer xform) {
    throw new UnsupportedOperationException(
      "Cannot transform subimages. Transform the parent and then obtain a subimage of that.");
  }

  @Override
  protected float texWidth(boolean repeatX) {
    return repeatX ? width : parent.texWidth(repeatX);
  }

  @Override
  protected float texHeight(boolean repeatY) {
    return repeatY ? height : parent.texHeight(repeatY);
  }

  @Override
  protected void updateTexture(GLContext ctx, Object tex) {
    throw new AssertionError("Region.updateTexture should never be called.");
  }

  private void scaleTexture(GLContext ctx, boolean repeatX, boolean repeatY) {
    if (reptex != null)
      return;

    int scaledWidth = ctx.scaledCeil(this.width);
    int scaledHeight = ctx.scaledCeil(this.height);

    // GL requires pow2 on axes that repeat
    int width = GLUtil.nextPowerOfTwo(scaledWidth), height = GLUtil.nextPowerOfTwo(scaledHeight);

    // width/height == 0 => already a power of two.
    if (width == 0)
      width = scaledWidth;
    if (height == 0)
      height = scaledHeight;

    // our source image is our parent's texture
    Object tex = parent.ensureTexture(ctx, false, false);

    // create our texture and point a new framebuffer at it
    reptex = ctx.createTexture(width, height, repeatX, repeatY);
    Object fbuf = ctx.createFramebuffer(reptex);

    // render the parent texture into the framebuffer properly scaled
    ctx.bindFramebuffer(fbuf, width, height);
    ctx.clear(0, 0, 0, 0);
    ctx.drawTexture(tex, texWidth(false), texHeight(false), ctx.createTransform(),
                    0, height, width, -height, this.x, this.y, this.width, this.height, 1);

    // we no longer need this framebuffer; rebind the default framebuffer and delete ours
    ctx.bindFramebuffer();
    ctx.deleteFramebuffer(fbuf);
  }
}
