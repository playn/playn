/**
 * Copyright 2011 The PlayN Authors
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
package playn.android;

import static playn.core.PlayN.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import playn.core.Asserts;
import playn.core.Image;
import playn.core.InternalTransform;
import playn.core.Pattern;
import playn.core.StockInternalTransform;
import playn.core.Surface;
import playn.core.gl.GL20;

class AndroidSurface implements Surface {

  private final AndroidGraphics gfx;
  private final int width, height;
  private int tex = -1, fbuf = -1;
  private final List<InternalTransform> transformStack = new ArrayList<InternalTransform>();
  private File cachedPixels;

  private int fillColor;
  private AndroidPattern fillPattern;

  AndroidSurface(AndroidGraphics gfx, int width, int height) {
    this.gfx = gfx;
    this.width = width;
    this.height = height;
    transformStack.add(new StockInternalTransform());
    refreshGL();
    gfx.addSurface(this);
  }

  private void refreshGL() {
    gfx.flush();
    AndroidGL20 gl20 = gfx.gl20;
    //Generate a texture for the framebuffer object's color buffer
    if (tex != -1 && gl20.glIsTexture(tex)) gfx.destroyTexture(tex);
    tex = gfx.createTexture(false, false);
    gl20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, width, height, 0, GL20.GL_RGBA,
        GL20.GL_UNSIGNED_BYTE, null);
    //Generate the framebuffer and attach the texture
    int[] fbufBuffer = new int[1];
    gl20.glGenFramebuffers(1, fbufBuffer, 0);
    fbuf = fbufBuffer[0];
    gfx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, fbuf);
    gl20.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D,
        tex, 0);
    //Clear the framebuffer of junk pixels.
    clear();
    // Redraw color buffer after the GL context is lost and refreshed.
    if (cachedPixels != null) {
      try {
        ByteBuffer pixelBuffer = ByteBuffer.allocate(width * height * 4);
        FileInputStream in = new FileInputStream(cachedPixels);
        in.read(pixelBuffer.array());
        int bufferTex = gfx.createTexture(false, false);
        gl20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, width, height, 0, GL20.GL_RGBA,
            GL20.GL_UNSIGNED_BYTE, pixelBuffer);
        gfx.drawTexture(bufferTex, width, height, StockInternalTransform.IDENTITY, 0, height,
            width, -height, false, false, 1);
        gfx.destroyTexture(bufferTex);
        pixelBuffer = null;
        cachedPixels.delete();
        cachedPixels = null;
      } catch (IOException e) {
        log().error("Error reading cached surface pixels from file.");
      }
    }
    gfx.bindFramebuffer();
  }

  void checkRefreshGL() {
    if (tex == -1 || fbuf == -1 || !gfx.gl20.glIsFramebuffer(fbuf) || !gfx.gl20.glIsTexture(tex)) {
      refreshGL();
    }
  }

  /*
   * Store the color buffer when the GL context is going to be lost.
   */
  void storePixels() {
    try {
      gfx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, fbuf);
      ByteBuffer pixelBuffer = ByteBuffer.allocate(width * height * 4);
      gfx.gl20.glReadPixels(0, 0, width, height, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixelBuffer);
      try {
        cachedPixels = new File(AndroidPlatform.instance.activity.getCacheDir(), "surface-"
            + Integer.toHexString(hashCode()));
        FileOutputStream out = new FileOutputStream(cachedPixels);
        out.write(pixelBuffer.array());
        out.close();
      } catch (IOException e) {
        log().error("IOException writing cached Surface to file.");
        cachedPixels = null;
      }
      pixelBuffer = null;
      gfx.bindFramebuffer();
      gfx.checkGlError("store Pixels");
    } catch (OutOfMemoryError e) {
      log().error("OutOfMemoryError reading cached Surface to buffer.");
      cachedPixels = null;
    }
    //Force a GL refresh before using this surface again.
    destroyTextureEtc();
  }

  @Override
  public Surface clear() {
    checkRefreshGL();
    gfx.bindFramebuffer(fbuf, width, height);
    gfx.gl20.glClearColor(0, 0, 0, 0);
    gfx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
    return this;
  }

  void destroy() {
    destroyTextureEtc();
    gfx.removeSurface(this);
  }

  int tex() {
    checkRefreshGL();
    return tex;
  }

  private void destroyTextureEtc() {
    gfx.destroyTexture(tex);
    gfx.gl20.glDeleteBuffers(1, new int[] { fbuf }, 0);
    tex = fbuf = -1;
  }

  /*
   * None of these draw* calls rebind the screen's framebuffer for efficiency.
   * The screen's framebuffer is automatically restored in
   * AndroidGraphics.updateLayers() before any drawing to the screen occurs.
   */

  @Override
  public Surface drawImage(Image image, float x, float y) {
    drawImage(image, x, y, image.width(), image.height());
    return this;
  }

  @Override
  public Surface drawImage(Image image, float x, float y, float dw, float dh) {
    gfx.bindFramebuffer(fbuf, width, height);

    Asserts.checkArgument(image instanceof AndroidImage);
    AndroidImage aimage = (AndroidImage) image;

    if (aimage.isReady()) {
      int tex = aimage.ensureTexture(gfx, false, false);
      if (tex != 0) {
        gfx.drawTexture(tex, image.width(), image.height(), topTransform(), x, y, dw, dh, false,
            false, 1);
      }
    }
    return this;
  }

  @Override
  public Surface drawImage(Image image, float dx, float dy, float dw, float dh, float sx, float sy,
      float sw, float sh) {
    checkRefreshGL();
    gfx.bindFramebuffer(fbuf, width, height);

    Asserts.checkArgument(image instanceof AndroidImage);
    AndroidImage aimage = (AndroidImage) image;

    if (aimage.isReady()) {
      int tex = aimage.ensureTexture(gfx, false, false);
      if (tex != 0) {
        gfx.drawTexture(tex, image.width(), image.height(), topTransform(), dx, dy, dw, dh, sx, sy,
            sw, sh, 1);
      }
    }
    return this;
  }

  @Override
  public Surface drawImageCentered(Image img, float x, float y) {
    drawImage(img, x - img.width() / 2, y - img.height() / 2);
    return this;
  }

  @Override
  public Surface drawLine(float x0, float y0, float x1, float y1, float width) {
    checkRefreshGL();
    gfx.bindFramebuffer(fbuf, this.width, this.height);

    float dx = x1 - x0, dy = y1 - y0;
    float len = (float) Math.sqrt(dx * dx + dy * dy);
    dx = dx * (width / 2) / len;
    dy = dy * (width / 2) / len;

    float[] pos = new float[8];
    pos[0] = x0 - dy;
    pos[1] = y0 + dx;
    pos[2] = x1 - dy;
    pos[3] = y1 + dx;
    pos[4] = x1 + dy;
    pos[5] = y1 - dx;
    pos[6] = x0 + dy;
    pos[7] = y0 - dx;
    gfx.fillPoly(topTransform(), pos, fillColor, 1);
    return this;
  }

  @Override
  public Surface fillRect(float x, float y, float width, float height) {
    checkRefreshGL();
    gfx.bindFramebuffer(fbuf, this.width, this.height);

    if (fillPattern != null) {
      AndroidImage image = fillPattern.image;
      int tex = image.ensureTexture(gfx, true, true);
      gfx.fillRect(topTransform(), x, y, width, height, image.width(), image.height(), tex, 1);
    } else {
      gfx.fillRect(topTransform(), x, y, width, height, fillColor, 1);
    }
    return this;
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public Surface restore() {
    Asserts.checkState(transformStack.size() > 1, "Unbalanced save/restore");
    transformStack.remove(transformStack.size() - 1);
    return this;
  }

  @Override
  public Surface rotate(float angle) {
    float sr = (float) Math.sin(angle);
    float cr = (float) Math.cos(angle);
    transform(cr, sr, -sr, cr, 0, 0);
    return this;
  }

  @Override
  public Surface save() {
    transformStack.add(new StockInternalTransform().set(topTransform()));
    return this;
  }

  @Override
  public Surface scale(float sx, float sy) {
    topTransform().scale(sx, sy);
    return this;
  }

  @Override
  public Surface setTransform(float m00, float m01, float m10, float m11, float tx, float ty) {
    topTransform().setTransform(m00, m01, m10, m11, tx, ty);
    return this;
  }

  @Override
  public Surface setFillColor(int color) {
    // TODO: Add it to the state stack.
    this.fillColor = color;
    this.fillPattern = null;
    return this;
  }

  @Override
  public Surface setFillPattern(Pattern pattern) {
    // TODO: Add it to the state stack.
    Asserts.checkArgument(pattern instanceof AndroidPattern);
    this.fillPattern = (AndroidPattern) pattern;
    return this;
  }

  @Override
  public Surface transform(float m00, float m01, float m10, float m11, float tx, float ty) {
    topTransform().concatenate(m00, m01, m10, m11, tx, ty, 0, 0);
    return this;
  }

  @Override
  public Surface translate(float x, float y) {
    topTransform().translate(x, y);
    return this;
  }

  @Override
  public int width() {
    return width;
  }

  private InternalTransform topTransform() {
    return transformStack.get(transformStack.size() - 1);
  }

  @Override
  protected void finalize() throws Throwable {
    destroy();
    super.finalize();
  }

}
