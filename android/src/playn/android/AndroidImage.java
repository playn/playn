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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import playn.core.Asserts;
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Image;
import playn.core.ResourceCallback;
import playn.core.StockInternalTransform;
import playn.core.gl.GL20;
import playn.core.gl.GLUtil;
import android.graphics.Bitmap;

/**
 * Android implementation of CanvasImage class. Prioritizes the SoftReference to
 * the bitmap, and only holds a hard reference if the game has requested that a
 * Canvas be created.
 */
class AndroidImage implements CanvasImage {
  private SoftReference<Bitmap> bitmapRef;
  private AndroidCanvas canvas;
  private Bitmap canvasBitmap;
  private List<ResourceCallback<Image>> callbacks = new ArrayList<ResourceCallback<Image>>();
  private int width, height;
  private int tex = -1, pow2tex = -1;
  private String path;
  
  //contextId identifies which GL context the textures were last refreshed in
  private int contextId;

  public AndroidImage(String path, Bitmap bitmap) {
    this.path = path;
    //Use a soft reference if we have a path to restore the bitmap from.
    bitmapRef = new SoftReference<Bitmap>(bitmap);
    width = bitmap.getWidth();
    height = bitmap.getHeight();   
  }

  public AndroidImage(int w, int h, boolean alpha) {
    // TODO: Why not always use the preferredBitmapConfig?  (Preserved from pre-GL code)
    canvasBitmap = Bitmap.createBitmap(w, h, alpha
        ? AndroidPlatform.instance.preferredBitmapConfig : Bitmap.Config.ARGB_8888);
    bitmapRef = null;
    width = w;
    height = h;
  }

  @Override
  public void addCallback(ResourceCallback<Image> callback) {
    callbacks.add(callback);
    if (isReady()) {
      runCallbacks(true);
    }
  }

  @Override
  public Canvas canvas() {
    if (canvas == null) {
      canvasBitmap = getBitmap();
      if (canvasBitmap != null) {
        canvas = new AndroidCanvas(canvasBitmap);
      } else {
        canvas = new AndroidCanvas(width, height);
      }
    }
    bitmapRef = null;
    return canvas;
  }

  boolean canvasDirty() {
    return (canvas != null && canvas.dirty());
  }

  void clearDirty() {
    canvas.clearDirty();
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public int width() {
    return width;
  }
  
  @Override
  public boolean isReady() {
    return bitmapRef != null || canvas != null;
  }

  @Override
  public void replaceWith(Image image) {
    Asserts.checkArgument(image instanceof AndroidImage);
    AndroidImage aimg = (AndroidImage) image;
    bitmapRef = new SoftReference<Bitmap>(aimg.getBitmap());
    width = image.width();
    height = image.height();
    path = aimg.getPath();
    canvas = null;
    tex = pow2tex = -1;
    if (AndroidPlatform.instance != null && AndroidPlatform.instance.graphics() != null) {
      clearTexture(AndroidPlatform.instance.graphics());
    }
  }

  /*
   * getBitmap() can be exceptionally slow, as it will likely
   * need to retrieve the bitmap from the file path.  As such
   * it should only be called when a direct reference to the
   * Bitmap is necessary.  (Note that this is less of an
   * issue for an AndroidImage that has had a canvas built,
   * as a hard reference to the bitmap is held in memory then).
   */
  Bitmap getBitmap() {
    if (canvasBitmap != null) {
      return canvasBitmap;
    }
    if (bitmapRef != null) {
      Bitmap bm = bitmapRef.get();
      if (bm == null && path != null) {
        if (AndroidPlatform.DEBUG_LOGS) log().debug("Bitmap " + path + " fell out of memory");
        bitmapRef = new SoftReference<Bitmap>(
            bm = AndroidPlatform.instance.assetManager().doGetBitmap(path));
      }
      return bm;
    }
    return null;
  }
  
  String getPath() {
    return path;
  }

  private void runCallbacks(boolean success) {
    for (ResourceCallback<Image> cb : callbacks) {
      if (success) {
        cb.done(this);
      } else {
        cb.error(new Exception("Error loading image"));
      }
    }
    callbacks.clear();
  }

  /*
   * Clears textures associated with this image. This does not destroy the image
   * -- a subsequent call to ensureTexture() will recreate them.
   */
  void clearTexture(AndroidGraphics gfx) {
    if (pow2tex == tex) {
      pow2tex = -1;
    }
    if (tex != -1) {
      gfx.destroyTexture(tex);
      tex = -1;
    }
    if (pow2tex != -1) {
      gfx.destroyTexture(pow2tex);
      pow2tex = -1;
    }
  }

  int ensureTexture(AndroidGraphics gfx, boolean repeatX, boolean repeatY) {
    // Create requested textures if loaded.
    if (canvasDirty() || refreshNeeded()) {
      //Force texture refresh
      if (canvas != null) clearDirty();
      clearTexture(gfx);
      contextId = GameViewGL.contextId();
    }
    if (isReady()) {
      if (repeatX || repeatY) {
        scaleTexture(gfx, repeatX, repeatY);
        return pow2tex;
      } else {
        loadTexture(gfx);
        return tex;
      }
    }
    log().error("Image not ready to draw -- cannot ensure texture.");
    return -1;
  }

  /*
   * Should be called from ensureTexture() and scaleTexture()
   */
  private void loadTexture(AndroidGraphics gfx) { 
    boolean isTexture = gfx.gl20.glIsTexture(tex);
    if (isTexture && tex != -1) {
      return;
    }
    if (isTexture) clearTexture(gfx);
    tex = gfx.createTexture(false, false);
    gfx.updateTexture(tex, getBitmap());
  }

  /*
   * Create a pow2 texture for repeating images.
   * Should be called from ensureTexture()
   */
  private void scaleTexture(AndroidGraphics gfx, boolean repeatX, boolean repeatY) {
    // Ensure that 'tex' is loaded. We use it below.
    loadTexture(gfx);
    
    if (pow2tex != -1 && gfx.gl20.glIsTexture(pow2tex)) {
      return;
    }
    
    // GL requires pow2 on axes that repeat.
    int width = GLUtil.nextPowerOfTwo(width()), height = GLUtil.nextPowerOfTwo(height());

    // Don't scale if it's already a power of two.
    if ((width == 0) && (height == 0)) {
      pow2tex = tex;
      return;
    }

    // width/height == 0 => already a power of two.
    if (width == 0) {
      width = width();
    }
    if (height == 0) {
      height = height();
    }

    // TODO: Throw error if the size is bigger than GL_MAX_RENDERBUFFER_SIZE?

    // Create the pow2 texture.
    pow2tex = gfx.createTexture(repeatX, repeatY);
    AndroidGL20 gl20 = gfx.gl20;
    gl20.glBindTexture(GL20.GL_TEXTURE_2D, pow2tex);
    gl20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, width, height, 0, GL20.GL_RGBA,
        GL20.GL_UNSIGNED_BYTE, null);

    // Point a new framebuffer at it.
    int[] fbufBuffer = new int[1];
    gl20.glGenFramebuffers(1, fbufBuffer, 0);
    int fbuf = fbufBuffer[0];
    gfx.bindFramebuffer(fbuf, width, height);
    gl20.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D,
        pow2tex, 0);
    // Render the scaled texture into the framebuffer.
    // (rebind the texture because gfx.bindFramebuffer() may have bound it when
    // flushing)
    gl20.glBindTexture(GL20.GL_TEXTURE_2D, pow2tex);
    gl20.glClearColor(0, 0, 0, 0);
    gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

    gfx.drawTexture(tex, width(), height(), StockInternalTransform.IDENTITY, 0, height, width,
        -height, false, false, 1);
    gfx.flush();
    gfx.bindFramebuffer();

    gl20.glDeleteFramebuffers(1, new int[] {fbuf}, 0);
  }
  
  @Override
  public void finalize() {
    if (AndroidPlatform.instance != null) {
      AndroidGraphics gfx = AndroidPlatform.instance.graphics();
      if (gfx != null) clearTexture(gfx);
    }
  }
  
  private boolean refreshNeeded() {
    return (contextId != GameViewGL.contextId());
  }

}
