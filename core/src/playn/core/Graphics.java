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
import react.Function;
import react.RFuture;
import react.UnitSlot;

import static playn.core.GL20.*;

/**
 * Provides access to graphics information and services.
 */
public abstract class Graphics {

  private final Platform plat;
  protected final Dimension viewSizeM = new Dimension();
  protected int viewPixelWidth, viewPixelHeight;
  protected int minFilter = GL_LINEAR, magFilter = GL_LINEAR;
  private Texture colorTex; // created lazily

  /** The filter modes used when converting images to textures. */
  public static enum Filter { LINEAR, NEAREST }

  /** Provides access to GL services. */
  public final GL20 gl;

  /** The display scale factor. This will be {@link Scale#ONE} except on HiDPI devices that have
    * been configured to use HiDPI mode. */
  public final Scale scale;

  /** The current size of the graphics viewport. */
  public final IDimension viewSize = viewSizeM;

  /** The render target for the default framebuffer. */
  public RenderTarget defaultRenderTarget = new RenderTarget(this) {
    public int id () { return gl.defaultFramebuffer(); }
    public int width () { return viewPixelWidth; }
    public int height () { return viewPixelHeight; }
    public boolean flip () { return true; }
    public void close () {} // disable normal destroy-on-close behavior
  };

  /**
   * Returns the size of the screen in display units. On some platforms (like the desktop) the
   * screen size may be larger than the view size.
   */
  public abstract IDimension screenSize ();

  /**
   * Creates a {@link Canvas} with the specified display unit size.
   */
  public abstract Canvas createCanvas (float width, float height);

  /** See {@link #createCanvas(float,float)}. */
  public Canvas createCanvas (IDimension size) {
    return createCanvas(size.width(), size.height());
  }

  /**
   * Configures the filter functions to use when creating textures.
   *
   * @param minFilter the scaling to use when rendering textures that are scaled down.
   * @param magFilter the scaling to use when rendering textures that are scaled up.
   */
  public void setTextureFilter (Filter minFilter, Filter magFilter) {
    this.minFilter = toGL(minFilter);
    this.magFilter = toGL(magFilter);
  }

  /**
   * Creates a managed texture with the contents if {@code image}, with no mipmaps.
   * See {@link #createTexture(Image,boolean,boolean)}.
   */
  public Texture createTexture (Image image) {
    return createTexture(image, true, false);
  }

  /**
   * Uploads {@code image}'s bitmap data to the GPU and returns a handle to the texture. The
   * current filter parameters (per {@link #setTextureFilter}) will be used for the texture.
   *
   * @param managed whether the texture will be reference counted. If the texture will be used in
   * an {@code ImageLayer}, it should be reference counted unless you are doing something special.
   * Otherwise you can decide whether you want to use the reference counting mechanism or not.
   * @param mipmaps whether the created texture should have mipmaps generated.
   *
   * @throws IllegalStateException if {@code image} is not fully loaded.
   */
  public Texture createTexture (Image image, boolean managed, boolean mipmaps) {
    if (!image.state.isCompleteNow()) throw new IllegalStateException(
      "Cannot create texture from unready image.");

    Texture tex = new Texture(this, createTexture(mipmaps), managed, mipmaps,
                              image.pixelWidth(), image.pixelHeight(),
                              image.width(), image.height());
    tex.update(image);
    return tex;
  }

  /**
   * Returns a future which will deliver a texture for {@code image} once its loading has
   * completed. Uses {@link #createTexture(Image)} to create texture.
   */
  public RFuture<Texture> createTextureAsync (Image image) {
    return image.state.map(new Function<Image,Texture>() {
      public Texture apply (Image image) { return createTexture(image); }
    });
  }

  /**
   * Returns a future which will deliver a texture for {@code image} once its loading has
   * completed. Uses {@link #createTexture(Image,boolean,boolean)} to create texture.
   */
  public RFuture<Texture> createTextureAsync (Image image, final boolean managed,
                                              final boolean mipmaps) {
    return image.state.map(new Function<Image,Texture>() {
      public Texture apply (Image image) { return createTexture(image, managed, mipmaps); }
    });
  }

  /**
   * Creates an empty texture into which one can render. The supplied width and height are in
   * display units and will be converted to pixels based on the current scale factor.
   *
   * @param managed whether the texture will be reference counted. If the texture will be used in
   * an {@code ImageLayer}, it should be reference counted unless you are doing something special.
   * Otherwise you can decide whether you want to use the reference counting mechanism or not.
   * @param mipmaps whether the created texture should have mipmaps generated.
   */
  public Texture createTexture (float width, float height, boolean managed, boolean mipmaps) {
    int pixWidth = scale.invScaledCeil(width), pixHeight = scale.invScaledCeil(height);
    int id = createTexture(mipmaps);
    gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, pixWidth, pixHeight,
                    0, GL_RGBA, GL_UNSIGNED_BYTE, null);
    return new Texture(this, id, managed, mipmaps, pixWidth, pixHeight, width, height);
  }

  /** See {@link #createTexture(float,float,boolean,boolean)}. */
  public Texture createTexture (IDimension size, boolean managed, boolean mipmaps) {
    return createTexture(size.width(), size.height(), managed, mipmaps);
  }

  /**
   * Creates a gradient fill pattern.
   */
  public abstract Gradient createGradient (Gradient.Config config);

  /**
   * Creates a font with the specified configuration.
   */
  public abstract Font createFont (Font.Config config);

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
   * Queues the supplied graphics resource for destruction on the next frame tick. This is
   * generally called from finalizers of graphics resource objects which discover that they are
   * being garbage collected, but their GPU resources have not yet been freed.
   */
  public void queueForDestroy (final Disposable resource) {
    plat.frame.connect(new UnitSlot() {
      public void onEmit () { resource.close(); }
    }).once();
  }

  Texture colorTex () {
    if (colorTex == null) {
      Canvas canvas = createCanvas(1, 1);
      canvas.setFillColor(0xFFFFFFFF).fillRect(0, 0, canvas.width, canvas.height);
      colorTex = createTexture(canvas.image, false, false);
    }
    return colorTex;
  }

  protected Graphics (Platform plat, GL20 gl, Scale scale) {
    this.plat = plat;
    this.gl = gl;
    this.scale = scale;
  }

  /**
   * Informs the graphics system that the main viewport size has changed. The supplied size should
   * be in physical pixels.
   */
  protected void viewSizeChanged (int viewWidth, int viewHeight) {
    viewPixelWidth = viewWidth;
    viewPixelHeight = viewHeight;
    viewSizeM.width = scale.invScaled(viewWidth);
    viewSizeM.height = scale.invScaled(viewHeight);
    // TODO: allow listening for view size change?
  }

  private int createTexture (boolean mipmaps) {
    int id = gl.glGenTexture();
    gl.glBindTexture(GL_TEXTURE_2D, id);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mipmapify(minFilter, mipmaps));
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    return id;
  }

  protected abstract void upload (Image image, Texture tex);

  protected static int toGL (Filter filter) {
    switch (filter) {
    default:
    case  LINEAR: return GL_LINEAR;
    case NEAREST: return GL_NEAREST;
    }
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
