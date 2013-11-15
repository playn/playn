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
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import playn.core.Asserts;
import playn.core.gl.GL20Context;

public class JavaGLContext extends GL20Context {

  private final static boolean CHECK_ERRORS = Boolean.getBoolean("playn.glerrors");

  private ByteBuffer imgBuf = createImageBuffer(1024);

  /** Converts the given image into a format for quick upload to the GPU. */
  static BufferedImage convertImage (BufferedImage image) {
    Asserts.checkNotNull(image);

    switch (image.getType()) {
    case BufferedImage.TYPE_INT_ARGB_PRE:
      return image; // Already good to go
    case BufferedImage.TYPE_4BYTE_ABGR:
      image.coerceData(true); // Just premultiply the alpha and it's fine
      return image;
    }

    // Didn't know an easy thing to do, so create a whole new image in our preferred format
    BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                                                     BufferedImage.TYPE_INT_ARGB_PRE);
    Graphics g = convertedImage.getGraphics();
    g.setColor(new Color(0f, 0f, 0f, 0f));
    g.fillRect(0, 0, image.getWidth(), image.getHeight());
    g.drawImage(image, 0, 0, null);

    return convertedImage;
  }

  public JavaGLContext(JavaPlatform platform, float scaleFactor) {
    super(platform, new JavaGL20(), scaleFactor, CHECK_ERRORS);
  }

  void updateTexture(int tex, BufferedImage image) {
    // Convert the image into a format for quick uploading
    image = convertImage(image);

    DataBuffer dbuf = image.getRaster().getDataBuffer();
    ByteBuffer bbuf;
    int format, type;

    if (image.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
      DataBufferInt ibuf = (DataBufferInt)dbuf;
      bbuf = checkGetImageBuffer(ibuf.getSize()*4);
      bbuf.asIntBuffer().put(ibuf.getData());
      bbuf.flip();
      format = GL12.GL_BGRA;
      type = GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

    } else if (image.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
      DataBufferByte dbbuf = (DataBufferByte)dbuf;
      bbuf = checkGetImageBuffer(dbbuf.getSize());
      bbuf.put(dbbuf.getData());
      bbuf.flip();
      format = GL11.GL_RGBA;
      type = GL12.GL_UNSIGNED_INT_8_8_8_8;

    } else {
      // Something went awry and convertImage thought this image was in a good form already,
      // except we don't know how to deal with it
      throw new RuntimeException("Image type wasn't converted to usable: " + image.getType());
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
