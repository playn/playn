/**
 * Copyright 2010 The PlayN Authors
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
package playn.core;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import react.Closeable;
import react.UnitSlot;

import static playn.core.GL20.*;

/**
 * Provides access to graphics information and services.
 */
public abstract class Graphics {

  protected final Platform plat;
  protected final Dimension viewSizeM = new Dimension();
  private Scale scale;
  private int viewPixelWidth, viewPixelHeight;
  private Texture colorTex; // created lazily

  /** Provides access to GL services. */
  public final GL20 gl;

  /** The current size of the graphics viewport. */
  public final IDimension viewSize = viewSizeM;

  /** The render target for the default framebuffer. */
  public RenderTarget defaultRenderTarget = new RenderTarget(this) {
    public int id () { return defaultFramebuffer(); }
    public int width () { return viewPixelWidth; }
    public int height () { return viewPixelHeight; }
    public float xscale () { return scale.factor; }
    public float yscale () { return scale.factor; }
    public boolean flip () { return true; }
    public void close () {} // disable normal dispose-on-close behavior
  };

  /** Returns the display scale factor. This will be {@link Scale#ONE} except on HiDPI devices that
    * have been configured to use HiDPI mode. */
  public Scale scale () { return scale; }

  /**
   * Returns the size of the screen in display units. On some platforms (like the desktop) the
   * screen size may be larger than the view size.
   */
  public abstract IDimension screenSize ();

  /**
   * Creates a {@link Canvas} with the specified display unit size.
   */
  public Canvas createCanvas (float width, float height) {
    return createCanvasImpl(scale, scale.scaledCeil(width), scale.scaledCeil(height));
  }

  /** See {@link #createCanvas(float,float)}. */
  public Canvas createCanvas (IDimension size) {
    return createCanvas(size.width(), size.height());
  }

  /**
   * Creates an empty texture into which one can render. The supplied width and height are in
   * display units and will be converted to pixels based on the current scale factor.
   */
  public Texture createTexture (float width, float height, Texture.Config config) {
    int texWidth = config.toTexWidth(scale.scaledCeil(width));
    int texHeight = config.toTexHeight(scale.scaledCeil(height));
    if (texWidth <= 0 || texHeight <= 0) throw new IllegalArgumentException(
      "Invalid texture size: " + texWidth + "x" + texHeight);

    int id = createTexture(config);
    gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texWidth, texHeight,
                    0, GL_RGBA, GL_UNSIGNED_BYTE, null);
    return new Texture(this, id, config, texWidth, texHeight, scale, width, height);
  }

  /** See {@link #createTexture(float,float,Texture.Config)}. */
  public Texture createTexture (IDimension size, Texture.Config config) {
    return createTexture(size.width(), size.height(), config);
  }

  /**
   * Lays out a single line of text using the specified format. The text may subsequently be
   * rendered on a canvas via {@link Canvas#fillText (TextLayout,float,float)}.
   */
  public abstract TextLayout layoutText (String text, TextFormat format);

  /**
   * Lays out multiple lines of text using the specified format and wrap configuration. The text
   * may subsequently be rendered on a canvas via {@link Canvas#fillText (TextLayout,float,float)}.
   */
  public abstract TextLayout[] layoutText (String text, TextFormat format, TextWrap wrap);

  /**
   * Queues the supplied graphics resource for disposal on the next frame tick. This is generally
   * called from finalizers of graphics resource objects which discover that they are being garbage
   * collected, but their GPU resources have not yet been freed.
   */
  public void queueForDispose (final Closeable resource) {
    plat.frame.connect(new UnitSlot() {
      public void onEmit () { resource.close(); }
    }).once();
  }

  Texture colorTex () {
    if (colorTex == null) {
      Canvas canvas = createCanvas(1, 1);
      canvas.setFillColor(0xFFFFFFFF).fillRect(0, 0, canvas.width, canvas.height);
      colorTex = canvas.toTexture(Texture.Config.UNMANAGED);
    }
    return colorTex;
  }

  protected Graphics (Platform plat, GL20 gl, Scale scale) {
    this.plat = plat;
    this.gl = gl;
    this.scale = scale;
  }

  /**
   * Returns the id of the default GL framebuffer. On most platforms this is 0, but not iOS.
   */
  protected int defaultFramebuffer () { return 0; }

  /**
   * Creates a {@link Canvas} with the specified pixel size. Because this is used when scaling
   * bitmaps for rendering into POT textures, we need to be precise about the pixel width and
   * height. So make sure this code path uses these exact sizes to make the canvas backing buffer.
   */
  protected abstract Canvas createCanvasImpl (Scale scale, int pixelWidth, int pixelHeight);

  /**
   * Informs the graphics system that the main framebuffer scaled has changed.
   */
  protected void scaleChanged (Scale scale) {
    // TODO: should we allow this to be reacted to? it only happens on the desktop Java backend...
    this.scale = scale;
  }

  /**
   * Informs the graphics system that the main framebuffer size has changed. The supplied size
   * should be in physical pixels.
   */
  protected void viewportChanged (int pixelWidth, int pixelHeight) {
    viewPixelWidth = pixelWidth;
    viewPixelHeight = pixelHeight;
    viewSizeM.width = scale.invScaled(pixelWidth);
    viewSizeM.height = scale.invScaled(pixelHeight);
    plat.log().info("viewPortChanged " + pixelWidth + "x" + pixelHeight + " / " + scale.factor +
                    " -> " + viewSize);
  }

  int createTexture (Texture.Config config) {
    int id = gl.glGenTexture();
    gl.glBindTexture(GL_TEXTURE_2D, id);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, config.magFilter);
    int minFilter = mipmapify(config.minFilter, config.mipmaps);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                       config.repeatX ? GL_REPEAT : GL_CLAMP_TO_EDGE);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                       config.repeatY ? GL_REPEAT : GL_CLAMP_TO_EDGE);
    return id;
  }

  protected static int mipmapify (int filter, boolean mipmaps) {
    if (!mipmaps) return filter;
    // we don't do trilinear filtering (i.e. GL_LINEAR_MIPMAP_LINEAR);
    // it's expensive and not super useful when only rendering in 2D
    switch (filter) {
    case GL_NEAREST: return GL_NEAREST_MIPMAP_NEAREST;
    case GL_LINEAR:  return GL_LINEAR_MIPMAP_NEAREST;
    default: return filter;
    }
  }
}
