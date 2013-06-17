/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.java;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import playn.core.Asserts;
import playn.core.gl.GL20Context;

class JavaGLContext extends GL20Context {

  public static final boolean CHECK_ERRORS = Boolean.getBoolean("playn.glerrors");
  private ByteBuffer imgBuf = createImageBuffer(1024);

  JavaGLContext(JavaPlatform platform, float scaleFactor, int screenWidth, int screenHeight) {
    super(platform, new JavaGL20(), scaleFactor, CHECK_ERRORS);
    setSize(screenWidth, screenHeight);
  }

  @Override
  public void init() {
    try {
      Display.create();
      super.viewWasResized();
      super.init();
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void viewWasResized() {
    try {
      Display.setDisplayMode(new DisplayMode(defaultFbufWidth, defaultFbufHeight));
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }
    if (Display.isCreated())
      super.viewWasResized();
  }

  void updateTexture(int tex, BufferedImage image) {
    Asserts.checkNotNull(image);

    ByteBuffer bbuf;
    int format, type;

    // use a special code path for images that are known to be INT_ARGB (which JavaCanvasImage
    // uses); this uses the GPU to swizzle ARGB to BGRA during the glTexImage2D call
    DataBuffer dbuf = image.getRaster().getDataBuffer();
    if (image.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
      DataBufferInt ibuf = (DataBufferInt)dbuf;
      bbuf = checkGetImageBuffer(ibuf.getSize()*4);
      bbuf.asIntBuffer().put(ibuf.getData());
      bbuf.flip();
      format = GL12.GL_BGRA;
      type = GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
    }
    // otherwise do things the hard way, by rendering the image using a special color model and
    // then uploading the resulting bytes as GL_RGBA
    else {
      ColorModel glAlphaColorModel = new ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false,
        Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
      WritableRaster raster = Raster.createInterleavedRaster(
        DataBuffer.TYPE_BYTE, image.getWidth(), image.getHeight(), 4, null);
      BufferedImage texImage = new BufferedImage(glAlphaColorModel, raster, true, null);

      Graphics g = texImage.getGraphics();
      g.setColor(new Color(0f, 0f, 0f, 0f));
      g.fillRect(0, 0, 256, 256);
      g.drawImage(image, 0, 0, null);

      // build a byte buffer from the temporary image that be used by OpenGL to produce a texture.
      DataBufferByte dbbuf = (DataBufferByte) texImage.getRaster().getDataBuffer();
      bbuf = checkGetImageBuffer(dbbuf.getSize());
      bbuf.put(dbbuf.getData());
      bbuf.flip();
      format = GL11.GL_RGBA;
      type = GL11.GL_UNSIGNED_BYTE;
    }

    bindTexture(tex);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0,
                      format, type, bbuf);
    checkGLError("updateTexture");
  }

  private ByteBuffer checkGetImageBuffer (int byteSize) {
    if (imgBuf.capacity() >= byteSize) {
      imgBuf.clear(); // reuse it!
    } else {
      imgBuf = createImageBuffer(byteSize);
    }
    return imgBuf;
  }

  private static ByteBuffer createImageBuffer (int byteSize) {
    return ByteBuffer.allocateDirect(byteSize).order(ByteOrder.nativeOrder());
  }
}
