/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import playn.core.Scale;
import playn.core.Texture;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

public class LWJGLGraphics extends JavaGraphics {

  private final Dimension screenSize = new Dimension();
  private final JavaPlatform.Config config;
  protected final LWJGLWindow window;
  
  public LWJGLGraphics(JavaPlatform plat) {
    super(plat, new LWJGLGL20(), Scale.ONE); // real scale factor set in init()
    this.config = plat.config;
    this.window = new LWJGLWindow(config, plat.log());
  }

  void checkScaleFactor () {
    float scaleFactor = window.calPixelScaleFactor();
    Dimension dim = window.size();
    if (scaleFactor != scale().factor) updateViewport(
        new Scale(scaleFactor), dim.width, dim.height);
  }

  @Override public IDimension screenSize() {
    IDimension size = window.destopSize();
    screenSize.width = scale().invScaled(size.width());
    screenSize.height = scale().invScaled(size.height());
    return screenSize;
  }

  @Override public void setSize (int width, int height, boolean fullscreen) {
    setDisplayMode(width, height, fullscreen);
  }

  @Override protected void init () {
    int width = scale().scaledCeil(config.width);
    int height = scale().scaledCeil(config.height);
    setDisplayMode(width, height,config.fullscreen);
    window.init();
    checkScaleFactor();
  }

  @Override protected void upload (BufferedImage img, Texture tex) {
    // Convert the bitmap into a format for quick uploading (NOOPs if already optimized)
    BufferedImage bitmap = convertImage(img);

    DataBuffer dbuf = bitmap.getRaster().getDataBuffer();
    ByteBuffer bbuf;
    int format, type;

    if (bitmap.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
      DataBufferInt ibuf = (DataBufferInt)dbuf;
      int iSize = ibuf.getSize()*4;
      bbuf = checkGetImageBuffer(iSize);
      bbuf.asIntBuffer().put(ibuf.getData());
      bbuf.position(bbuf.position()+iSize);
      bbuf.flip();
      format = GL12.GL_BGRA;
      type = GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

    } else if (bitmap.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
      DataBufferByte dbbuf = (DataBufferByte)dbuf;
      bbuf = checkGetImageBuffer(dbbuf.getSize());
      bbuf.put(dbbuf.getData());
      bbuf.flip();
      format = GL11.GL_RGBA;
      type = GL12.GL_UNSIGNED_INT_8_8_8_8;

    } else {
      // Something went awry and convertImage thought this image was in a good form already,
      // except we don't know how to deal with it
      throw new RuntimeException("Image type wasn't converted to usable: " + bitmap.getType());
    }

    gl.glBindTexture(GL11.GL_TEXTURE_2D, tex.id);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(),
                      0, format, type, bbuf);
    gl.checkError("updateTexture");
  }

  protected void setDisplayMode(int width, int height, boolean fullscreen) {
    if (!window.setSize(width, height, fullscreen)) return;
    Scale scale = fullscreen ? Scale.ONE : new Scale(window.calPixelScaleFactor());
    IDimension size = window.size();
    updateViewport(scale, size.width(), size.height());
  }

  private void updateViewport (Scale scale, float displayWidth, float displayHeight) {
    scaleChanged(scale);
    viewportChanged(scale.scaledCeil(displayWidth), scale.scaledCeil(displayHeight));
  }
}
