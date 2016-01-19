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
import java.util.Arrays;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import playn.core.*;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

public class LWJGLGraphics extends JavaGraphics {

  private final Dimension screenSize = new Dimension();

  public LWJGLGraphics(JavaPlatform plat) {
    super(plat, new LWJGLGL20(), Scale.ONE); // have to get scale after Display.create()
    setDisplayMode(plat.config.width, plat.config.height, plat.config.fullscreen);

    try {
      System.setProperty("org.lwjgl.opengl.Display.enableHighDPI", "true");
      Display.create();
      checkScaleFactor();
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }
  }

  void checkScaleFactor () {
    float scaleFactor = Display.getPixelScaleFactor();
    if (scaleFactor != scale().factor) updateViewport(
      new Scale(scaleFactor), Display.getWidth(), Display.getHeight());
  }

  @Override public void setTitle (String title) { Display.setTitle(title); }

  @Override public IDimension screenSize() {
    DisplayMode mode = Display.getDesktopDisplayMode();
    screenSize.width = scale().invScaled(mode.getWidth());
    screenSize.height = scale().invScaled(mode.getHeight());
    return screenSize;
  }

  @Override public void setSize (int width, int height, boolean fullscreen) {
    setDisplayMode(width, height, fullscreen);
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
    try {
      // check if current mode is suitable
      DisplayMode mode = Display.getDisplayMode();
      if (fullscreen == Display.isFullscreen() &&
          mode.getWidth() == width && mode.getHeight() == height) return;

      if (!fullscreen) {
        DisplayMode deskMode = Display.getDesktopDisplayMode();
        if (width > deskMode.getWidth()) {
          plat.log().debug("Capping window width at desktop width: " + width + " -> " +
                           deskMode.getWidth());
          width = deskMode.getWidth();
        }
        if (height > deskMode.getHeight()) {
          plat.log().debug("Capping window height at desktop height: " + height + " -> " +
                           deskMode.getHeight());
          height = deskMode.getHeight();
        }
        mode = new DisplayMode(width, height);
      } else {
        // try and find a mode matching width and height
        DisplayMode matching = null;
        for (DisplayMode dm : Display.getAvailableDisplayModes()) {
          if (dm.getWidth() == width && dm.getHeight() == height && dm.isFullscreenCapable()) {
            matching = dm;
          }
        }
        if (matching != null) mode = matching;
        else plat.log().info("Could not find a matching fullscreen mode, available: " +
                             Arrays.asList(Display.getAvailableDisplayModes()));
      }

      plat.log().debug("Updating display mode: " + mode + ", fullscreen: " + fullscreen);
      // TODO: fix crashes when fullscreen is toggled repeatedly
      Scale scale;
      if (fullscreen) {
        Display.setDisplayModeAndFullscreen(mode);
        scale = Scale.ONE;
        // TODO: fix alt-tab, maybe add a key listener or something?
      } else {
        Display.setDisplayMode(mode);
        scale = new Scale(Display.getPixelScaleFactor());
      }
      updateViewport(scale, mode.getWidth(), mode.getHeight());

    } catch (LWJGLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void updateViewport (Scale scale, float displayWidth, float displayHeight) {
    scaleChanged(scale);
    viewportChanged(scale.scaledCeil(displayWidth), scale.scaledCeil(displayHeight));
  }
}
