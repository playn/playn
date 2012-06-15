package playn.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import playn.core.gl.GL20;

public abstract class AbstractImage implements Image {
  
  private ByteBuffer getRgba() {
    int w = (int) width();
    int h = (int) height();

    int size = w * h;
    int[] rawPixels = new int[size];
    ByteBuffer pixels = ByteBuffer.allocateDirect(size * 4);
    pixels.order(ByteOrder.nativeOrder());
    IntBuffer rgba = pixels.asIntBuffer();
    
    getRgb(0, 0, w, h, rawPixels, 0, w);
    
    for (int i = 0; i < size; i++) {
      int argb = rawPixels[i];
      // Order is inverted because this is read as a byte array, and we store intel ints.
      rgba.put(i, ((argb >> 16) & 0x0ff) | (argb & 0x0ff00ff00) | ((argb & 0xff) << 16));
    }
    return pixels;
  }
  
  /** 
   * This will work for region, too, because getRGB will respect the
   * region coordinates. Currently only RGBA/GL_UNSIGNED_BYTE is supported 
   */
  @Override
  public void glTexImage2D(GL20 gl, int target, int level, int internalformat, int format, int type) {
    gl.glTexImage2D(target, level, internalformat, (int) width(),  (int) height(), 0, format, type, getRgba());
  }
  
  /** 
   * This will work for region, too, because getRGB will respect the
   * region coordinates. Currently only RGBA/GL_UNSIGNED_BYTE is supported 
   */
  @Override
  public void glTexSubImage2D(GL20 gl, int target, int level, int xOffset, int yOffset, int format, int type) {
    gl.glTexSubImage2D(target, level, xOffset, yOffset, (int) width(),  (int) height(), format, type, getRgba());
  }
}
