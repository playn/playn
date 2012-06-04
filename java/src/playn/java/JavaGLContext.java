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
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import playn.core.Asserts;
import playn.core.gl.GL20Context;

class JavaGLContext extends GL20Context {

  public static final boolean CHECK_ERRORS = Boolean.getBoolean("playn.glerrors");

  JavaGLContext(JavaPlatform platform, float scaleFactor, int screenWidth, int screenHeight) {
    super(platform, new JavaGL20(), scaleFactor, screenWidth, screenHeight, CHECK_ERRORS);
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

  void initGL() {
    try {
      Display.create();
      super.viewWasResized();
      init();
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }
  }

  void updateTexture(int tex, BufferedImage image) {
    ByteBuffer buf = convertImageData(image);
    bindTexture(tex);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0,
                      GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
    checkGLError("updateTexture");
  }

  private ByteBuffer convertImageData(BufferedImage img) {
    Asserts.checkNotNull(img);
    ByteBuffer imageBuffer;

    // TODO(jgw): There has *got* to be a better way. None of the BufferedImage types match
    // GL_RGBA, so we have to go through these stupid contortions to get a color model.
    ColorModel glAlphaColorModel = new ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false,
        Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
    WritableRaster raster = Raster.createInterleavedRaster(
      DataBuffer.TYPE_BYTE, img.getWidth(), img.getHeight(), 4, null);
    BufferedImage texImage = new BufferedImage(glAlphaColorModel, raster, true, null);

    Graphics g = texImage.getGraphics();
    g.setColor(new Color(0f, 0f, 0f, 0f));
    g.fillRect(0, 0, 256, 256);
    g.drawImage(img, 0, 0, null);

    // Build a byte buffer from the temporary image that be used by OpenGL to produce a texture.
    DataBufferByte dbuf = (DataBufferByte) texImage.getRaster().getDataBuffer();
    imageBuffer = ByteBuffer.allocateDirect(dbuf.getSize());
    imageBuffer.order(ByteOrder.nativeOrder());
    imageBuffer.put(dbuf.getData());
    imageBuffer.flip();

    return imageBuffer;
  }
}
