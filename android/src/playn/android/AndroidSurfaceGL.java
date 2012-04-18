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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import playn.core.PlayN;
import playn.core.StockInternalTransform;
import playn.core.gl.GL20;
import playn.core.gl.SurfaceGL;

/**
 * Extends basic {@link SurfaceGL} and adds hooks for saving and restoring the surface when the GL
 * context is lost.
 */
public class AndroidSurfaceGL extends SurfaceGL
  implements AndroidGLContext.Refreshable
{
  private File cachedPixels;
  private final File cacheDir;

  AndroidSurfaceGL(File cacheDir, AndroidGLContext ctx, int width, int height) {
    super(ctx, width, height);
    this.cacheDir = cacheDir;
    ctx.addRefreshable(this);
  }

  @Override
  protected void destroy() {
    ((AndroidGLContext) ctx).removeRefreshable(this);
    super.destroy();
  }

  @Override
  public void onSurfaceCreated() {
    createTexture();
    if (cachedPixels != null) {
      try {
        AndroidGLContext actx = (AndroidGLContext) ctx;
        ByteBuffer pixelBuffer = ByteBuffer.allocate(width * height * 4);
        FileInputStream in = new FileInputStream(cachedPixels);
        in.read(pixelBuffer.array());
        int bufferTex = actx.createTexture(false, false);
        actx.gl20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, width, height, 0, GL20.GL_RGBA,
                               GL20.GL_UNSIGNED_BYTE, pixelBuffer);
        ctx.drawTexture(bufferTex, width, height, StockInternalTransform.IDENTITY, 0, height,
                        width, -height, false, false, 1);
        ctx.destroyTexture(bufferTex);
        pixelBuffer = null;
        cachedPixels.delete();
        cachedPixels = null;
      } catch (IOException e) {
        PlayN.log().error("Error reading cached surface pixels from file.");
      }
    }
  }

  @Override
  public void onSurfaceLost() {
    try {
      AndroidGLContext actx = (AndroidGLContext) ctx;
      actx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, (Integer) fbuf);
      ByteBuffer pixelBuffer = ByteBuffer.allocate(width * height * 4);
      actx.gl20.glReadPixels(0, 0, width, height, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixelBuffer);
      try {
        cachedPixels = new File(cacheDir, "surface-" + Integer.toHexString(hashCode()));
        FileOutputStream out = new FileOutputStream(cachedPixels);
        out.write(pixelBuffer.array());
        out.close();
      } catch (IOException e) {
        PlayN.log().error("IOException writing cached Surface to file.", e);
        cachedPixels = null;
      }
      pixelBuffer = null;
      actx.checkGLError("store Pixels");
    } catch (OutOfMemoryError e) {
      PlayN.log().error("OutOfMemoryError reading cached Surface to buffer.");
      cachedPixels = null;
    }
    clearTexture();
  }
}
